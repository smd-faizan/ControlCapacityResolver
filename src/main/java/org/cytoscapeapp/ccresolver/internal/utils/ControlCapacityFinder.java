package org.cytoscapeapp.ccresolver.internal.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscapeapp.ccresolver.internal.utils.BipartiteGraph.Node;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class ControlCapacityFinder extends Thread{
    
    public boolean stop = false;
    public CyNetwork actualNetwork ;
    public CyNetwork usefulnetwork ;
    public Set<Node> redundantNodes ;
    public BipartiteGraph bGraph;
    public static final String PHI = "Phi";
    
    protected ControlCapacityFinder(CyNetwork actualNetwork, BipartiteGraph bGraph, 
           CyNetwork usefulnetwork, Set<Node> redundantNodes){
        this.actualNetwork = actualNetwork;
        this.usefulnetwork = usefulnetwork;
        this.redundantNodes = redundantNodes;
        this.bGraph = bGraph;
    }
    
    @Override
    public void run() {
        printCCOfRedundantNodes(actualNetwork, redundantNodes);
        removeRedundantNodesFromGraph(bGraph, redundantNodes);
        
        Set<Node> M = new HashSet<Node>();
        for(Node u : bGraph.Out){
            if(u.pair != null)
                M.add(u.pair);
        }
        // initial MMS
        System.out.println("-------------------------");
        System.out.println("    Maximum matchings    ");
        System.out.println("-------------------------");
        System.out.println("Initial MMS : ");
        System.out.print("MMS : ");
        for(Node u : bGraph.Out){
            String nodeName = actualNetwork.getRow(u.n).get(CyNetwork.NAME, String.class);
            System.out.print(nodeName+", ");
            if(u.pair != null){
                String nodeName2 = actualNetwork.getRow(u.pair.n).get(CyNetwork.NAME, String.class);
                System.out.print(nodeName2+", ");
            }
        }
        System.out.println();
        System.out.print("M = {");
        for(Node i : M){
            String nodeName = actualNetwork.getRow(i.n).get(CyNetwork.NAME, String.class);
            System.out.print(nodeName + ", ");
        }
        System.out.println("}");
        
        for(Node i : M){
            String nodeiName = actualNetwork.getRow(i.n).get(CyNetwork.NAME, String.class);
            System.out.println("Calculating all the MMS by removing "+nodeiName+": ");
            // remove node i and create a new network and bg
            Set<Node> seti = new HashSet<Node>();
            seti.add(i);
            CyNetwork subnetwork = createSubnetworkByRemovingNodeI(usefulnetwork, seti);
            // check if there is augmenting path
            BipartiteGraph bGraph2 = new BipartiteGraph(subnetwork);
            // populate max matching 
            Set<Node> UnmatchedNodes = new HashSet<Node>(bGraph.In);
            UnmatchedNodes.removeAll(M);
            Node j = null;
            for(Node u1 : bGraph.Out){
                for(Node u2 : bGraph2.Out){
                    if(u2.equals(u1)){
                        if(u1.equals(i.pair))
                            j = u2;
                        else{
                            for(Node v2 : bGraph2.In){
                                if(v2.equals(u1.pair)){
                                    u2.pair = v2;
                                    v2.pair = u2;
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            Node k = null;
            while( (k=DFSMMS(j, UnmatchedNodes, bGraph2))  != null){
                // print bgraph2
                System.out.print("MMS : ");
                for(Node u : bGraph2.Out){
                    String nodeName = actualNetwork.getRow(u.n).get(CyNetwork.NAME, String.class);
                    System.out.print(nodeName+", ");
                    if(u.pair != null){
                        String nodeName2 = actualNetwork.getRow(u.pair.n).get(CyNetwork.NAME, String.class);
                        System.out.print(nodeName2+", ");
                    }
                }
                System.out.println();
                
                j = k.pair;
                removeNode(bGraph2, k);
            }
        }
        
    }
    
    public Node DFSMMS(Node j, Set<Node> unMatchedNodes, BipartiteGraph bGraph2){
        for(Node v : j.adj){
            if(!v.equals(j.pair)){
                if(unMatchedNodes.contains(v)){
                    j.pair = v;
                    v.pair = j;
                    unMatchedNodes.remove(v);
                    return v;
                }
                else{
                    Node k = null;
                    if( (k=DFSMMS(v.pair, unMatchedNodes, bGraph2)) != null){
                        j.pair = v;
                        v.pair = j;
                        return k;
                    }
                }
            }
//            if(v != j.pair){
//                if(unMatchedNodes.contains(v)){
//                    // out Nodes and their pairs along with v are the MMS in the bGraph2
//                    System.out.print("MMS : ");
//                    for(Node u : bGraph2.Out){
//                        String nodeName = actualNetwork.getRow(u.n).get(CyNetwork.NAME, String.class);
//                        System.out.print(nodeName+", ");
//                        if(!u.equals(j) && u.pair != null){
//                            String nodeName2 = actualNetwork.getRow(u.pair.n).get(CyNetwork.NAME, String.class);
//                            System.out.print(nodeName2+", ");
//                        }
//                    }
//                    String nodeName = actualNetwork.getRow(v.n).get(CyNetwork.NAME, String.class);
//                    System.out.println(" and "+nodeName);
//                    //return true;
//                }
//                else
//                    DFSMMS(v.pair, unMatchedNodes, bGraph2);
//            }
        }
        return null;
    }
    
    public void removeNode(BipartiteGraph bGraph2, Node k){
        // we cant remove from node k from bgraph2.network - not required
        for(Node u : bGraph2.Out){
            u.adj.remove(k);
            if(u.pair == k)
                u.pair = null;
        }
        for(Node v : bGraph2.In){
            v.adj.remove(k);
            if(v.pair == k)
                v.pair = null;
        }
        bGraph2.Out.remove(k);
        bGraph2.In.remove(k);
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
    
    public void removeRedundantNodesFromGraph(BipartiteGraph bGraph, Set<Node> redundantNodes){
        for(Node i : redundantNodes){
            for(Node u : bGraph.Out){
                if( u.pair == i)
                    u.pair = null;
                if(u.adj.contains(i))
                    u.adj.remove(i);
            }
        }
        bGraph.In.removeAll(redundantNodes);
    }
    
    public void printCCOfRedundantNodes(CyNetwork network, Set<Node> redundantNodes){
        for(Node i : redundantNodes){
            String nodeName = network.getRow(i.n).get(CyNetwork.NAME, String.class);
            System.out.println(PHI+" = 0, for Node with SUID = "+i.n.getSUID()+" and name "+ nodeName);
        }
    }
    
    public void end(){
        stop = true;
    }
}

