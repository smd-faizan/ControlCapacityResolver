package org.cytoscapeapp.ccresolver.internal.utils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscapeapp.ccresolver.internal.CyActivator;
import org.cytoscapeapp.ccresolver.internal.utils.BipartiteGraph.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class RedundancyResolver  extends Thread{
    public boolean stop = false;
    private final CyNetwork network;
    
    public RedundancyResolver(CyNetwork network) {
        this.network = network;
    }
    
    @Override
    public void run() {
        // assuming that the network is valid 
        BipartiteGraph bGraph = new BipartiteGraph(network);
        
        int matching = HopcroftKarp(bGraph.Out, bGraph.In, bGraph.NIL);
        
        System.out.println("Number of edges in maximum matching = "+matching);
        
        // find redundant nodes
        Set<Node> redundantNodes = new HashSet<Node>();
        Set<Node> M = new HashSet<Node>();
        for(Node u : bGraph.Out){
            M.add(u.pair);
        }
        for(Node i : M){
            Node j = i.pair;
            if(stop)
                return;
            // remove node i and create a new network and bg
            Set<Node> seti = new HashSet<Node>();
            seti.add(i);
            CyNetwork subnetwork = createSubnetworkByRemovingNodeI(network, seti);
            // check if there is augmenting path
            BipartiteGraph bGraph2 = new BipartiteGraph(subnetwork);
            // populate max matching 
            Set<Node> UnmatchedNodes = new HashSet<Node>(bGraph2.In);
            for(Node u1 : bGraph.Out){
                for(Node u2 : bGraph2.Out){
                    if(u2.equals(u1)){
                        u2.pair = u1.pair;
                        UnmatchedNodes.remove(u2.pair);
                    }
                }
            }
            if(!DFSAugmentingPath(j, UnmatchedNodes)){
                // node i is redundant node
                redundantNodes.add(i);
            }
        }
        
        System.out.println("Number of redundant Nodes : "+redundantNodes.size());
        if(!redundantNodes.isEmpty()){
            System.out.print("Redundant Nodes : ");
            for(Node i : redundantNodes){
                String nodeName = network.getRow(i.n).get(CyNetwork.NAME, String.class);
                System.out.print(nodeName + " with SUID "+i.n.getSUID()+", ");
            }
        }
        System.out.println();
        CyNetwork usefulNetwork = createSubnetworkByRemovingNodeI(network, redundantNodes);
        
        List<CyNode> nodeList = network.getNodeList();
        List<CyEdge> edgeList = network.getEdgeList();
        for(Node i : redundantNodes){
            nodeList.remove(i.n);
            edgeList.removeAll(network.getAdjacentEdgeList(i.n, CyEdge.Type.ANY));
        }
        CyNetwork usefulnewNetwork = createNetwork(network, nodeList, edgeList);
        
        ControlCapacityFinder cc = new ControlCapacityFinder(network, bGraph, usefulNetwork, redundantNodes);
        cc.start();
        try {
            cc.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(RedundancyResolver.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(" Finished calculating MMS");
    }
    
    public CyNetwork createSubnetworkByRemovingNodeI(CyNetwork network, Set<Node> seti){
        List<CyNode> nodeList = network.getNodeList();
        List<CyEdge> edgeList = network.getEdgeList();
        for(Node i : seti){
            nodeList.remove(i.n);
            edgeList.removeAll(network.getAdjacentEdgeList(i.n, CyEdge.Type.ANY));
        }
        CyRootNetwork root = ((CySubNetwork)network).getRootNetwork();
        CyNetwork subNetwork = root.addSubNetwork(nodeList, edgeList);
        return subNetwork;
    }
    
    private int HopcroftKarp(Set<Node> Out, Set<Node> In, Node NIL){
        int matching = 0;
        while(BFS(Out, In, NIL)){
            for(Node u : Out){
                if(u.pair == NIL){
                    if(DFS(u,Out,In,NIL) == true)
                        matching++;
                }
            }
        }
        return matching;
    }
    
    private boolean BFS(Set<Node> Out, Set<Node> In, Node NIL){
        Queue<Node> q = new LinkedList<Node>();
        for(Node o : Out){
            if(o.pair == NIL){
                o.dist = 0;
                q.add(o);
            }
            else
                o.dist = Integer.MAX_VALUE;
        }
        NIL.dist = Integer.MAX_VALUE;
        while(!q.isEmpty()){
            Node u = q.remove();
            if(u.dist < NIL.dist){
                for(Node v : u.adj){
                    if(v.pair.dist == Integer.MAX_VALUE){
                        v.pair.dist = u.dist + 1;
                        q.add(v.pair);
                    }
                }
            }
        }
        return NIL.dist != Integer.MAX_VALUE;
    }
    
    private boolean DFS(Node u, Set<Node> Out, Set<Node> In, Node NIL){
        if( u != NIL ){
            for( Node v : u.adj ){
                if( v.pair.dist == u.dist+1 ){
                    if( DFS(v.pair, Out, In, NIL) == true ){
                        v.pair = u;
                        u.pair = v;
                        return true;
                    }
                }
            }
            u.dist = Integer.MAX_VALUE;
            return false;
        }
        return true;
    }
    
    public boolean DFSAugmentingPath(Node j, Set<Node> unMatchedNodes){
        for(Node v : j.adj){
            if(v != j.pair){
                if(unMatchedNodes.contains(v))
                    return true;
                else
                    if(DFSAugmentingPath(v.pair, unMatchedNodes))
                        return true;
            }
        }
        return false;
    }
    
    public CyNetwork createNetwork(CyNetwork network, List<CyNode> stnodeList, List<CyEdge> stedgeList){
        // select the nodes and edges
        CyTable nTable = network.getDefaultNodeTable();
        CyTable eTable = network.getDefaultEdgeTable();
        for(CyEdge e : stedgeList){
            CyRow row = eTable.getRow(e.getSUID());
            row.set("selected", true);
        }
        for(CyNode n : stnodeList){
            CyRow row = nTable.getRow(n.getSUID());
            row.set("selected", true);
        }
        // create the network
        NewNetworkSelectedNodesAndEdgesTaskFactory f = CyActivator.adapter.
                get_NewNetworkSelectedNodesAndEdgesTaskFactory();
        TaskIterator itr = f.createTaskIterator(network);
        CyActivator.adapter.getTaskManager().execute(itr);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(RedundancyResolver.class.getName()).log(Level.SEVERE, null, ex);
        }
        // set the name of the network
        //this.menu.calculatingresult("Created! Renaming the network...");
        String currentNetworkName = network.getRow(network).get(CyNetwork.NAME, String.class);
        Set<CyNetwork> allnetworks = CyActivator.networkManager.getNetworkSet();
        long maxSUID = Integer.MIN_VALUE;
        for(CyNetwork net : allnetworks){
            if(net.getSUID() > maxSUID)
                maxSUID = net.getSUID();
        }
        CyNetwork usefulNetwork = CyActivator.networkManager.getNetwork(maxSUID);
        usefulNetwork.getRow(usefulNetwork).set(CyNetwork.NAME, currentNetworkName + " - useful Network");
        return usefulNetwork;
    }
    
    
    public void end(){
        stop = true;
    }
    
}
