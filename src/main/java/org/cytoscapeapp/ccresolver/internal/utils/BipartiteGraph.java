package org.cytoscapeapp.ccresolver.internal.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class BipartiteGraph {
    
    public CyNetwork network;
    public Set<Node> Out;
    public Set<Node> In;
    public Node NIL;
    
    public BipartiteGraph(CyNetwork network){
        this.network = network;
        Out = new HashSet<Node>();
        In = new HashSet<Node>();
        NIL = new Node(null, null, Integer.MAX_VALUE);
        init(Out, In, NIL);
    }
    
    
    public static class Node{
        Node(CyNode n, Node pair, int dist){
            this.n = n;
            this.pair = pair;
            this.dist = dist;
        }
        CyNode n;
        Node pair;
        int dist;
        List<Node> adj = new ArrayList<Node>();
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((n == null) ? 0 : n.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Node other = (Node) obj;
            if (n == null) {
                if (other.n != null)
                    return false;
            } else if (!n.equals(other.n))
                return false;
            return true;
        }
    }
    
    private void init(Set<Node> Out, Set<Node> In, Node NIL){
        List<CyNode> nodelist = network.getNodeList();
        for(CyNode n : nodelist){
            List<CyNode> incomingNodes = network.getNeighborList(n, CyEdge.Type.INCOMING);
            if(incomingNodes.isEmpty())
                Out.add(new Node(n, NIL, Integer.MAX_VALUE) );
            else
                In.add(new Node(n, NIL, Integer.MAX_VALUE));
        }
        // set Adj
        for(Node n1: Out){
            CyNode cn = n1.n;
            List<CyNode> outgoingNodes = network.getNeighborList(cn, CyEdge.Type.OUTGOING);
            for(Node n2 : In){
                if(outgoingNodes.contains(n2.n)){
                    n1.adj.add(n2);
                    n2.adj.add(n1);
                }
            }
        }
    }
}
