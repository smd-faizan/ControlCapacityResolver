/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cytoscapeapp.ccresolver.internal.utils;

import java.util.List;
import javax.swing.JOptionPane;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class NetworkValidator extends Thread{
    public boolean stop = false;
    private final CyNetwork network;
    
    public NetworkValidator(CyNetwork network) {
        this.network = network;
    }

    @Override
    public void run() {
        
        List<CyNode> nodeList = network.getNodeList();
        for(CyNode node1 : nodeList){
            if(stop)
                return;
            List<CyNode> incomingNodes = network.getNeighborList(node1, CyEdge.Type.INCOMING);
            List<CyNode> outgoingNodes = network.getNeighborList(node1, CyEdge.Type.OUTGOING);
            if( incomingNodes.isEmpty() && outgoingNodes.isEmpty() ){
                System.out.println("Network is not connected. Multiple components exists! Please input a connected network");
                JOptionPane.showMessageDialog(null, "Network is not connected. Multiple components exists! Please input a connected network", "Unconnected network!", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if( !incomingNodes.isEmpty() && !outgoingNodes.isEmpty() ){
                System.out.println("Network is not Valid. One of the nodes with SUID ["+node1.getSUID()+"] contains both incoming and outgoing edges.");
                JOptionPane.showMessageDialog(null, "Network is not Valid. One of the nodes with SUID ["+node1.getSUID()+"] contains both incoming and outgoing edges.",
                        "Invalid network!", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        System.out.println("Network is Valid.");
        JOptionPane.showMessageDialog(null, "Network is Valid.", "Valid network!", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void end(){
        stop = true;
    }
    
}
