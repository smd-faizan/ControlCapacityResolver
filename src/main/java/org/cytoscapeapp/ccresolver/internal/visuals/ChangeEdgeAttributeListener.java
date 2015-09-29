package org.cytoscapeapp.ccresolver.internal.visuals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscapeapp.ccresolver.internal.CCCore;
import org.cytoscapeapp.ccresolver.internal.CCStartMenu;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class ChangeEdgeAttributeListener implements SetCurrentNetworkListener{

    @Override
    public void handleEvent(SetCurrentNetworkEvent scne) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        CyNetwork network = scne.getNetwork();
        
        CCStartMenu menu = CCCore.getCCStartMenu();
        //menu.getEdgeAttributeComboBox().getModel().getSelectedItem().toString();
        //menu.getEdgeAttributeComboBox().setModel(new javax.swing.DefaultComboBoxModel(getEdgeAttributes(network).toArray()));
        //menu.getEdgeAttributeComboBox().setSelectedItem("None");

        
    }

    public static List<String> getEdgeAttributes(CyNetwork network){
        Collection<CyColumn> edgeColumns = network.getDefaultEdgeTable().getColumns();
        List<String> columnsToAdd = new ArrayList<String>(1);
        
        int i = 0;
        for(CyColumn c:edgeColumns){
            if(!c.isPrimaryKey()){
                columnsToAdd.add(c.getName());
                i++;
            }
        }
        columnsToAdd.add("None");
        
        return columnsToAdd;
    }
    
}
