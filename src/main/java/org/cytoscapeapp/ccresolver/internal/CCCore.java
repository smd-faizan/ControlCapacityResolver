package org.cytoscapeapp.ccresolver.internal;

import java.util.Properties;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscapeapp.ccresolver.internal.visuals.ChangeEdgeAttributeListener;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class CCCore {

    public CyNetwork network;
    public CyNetworkView view;
    public CyApplicationManager cyApplicationManager;
    public CySwingApplication cyDesktopService;
    public CyServiceRegistrar cyServiceRegistrar;
    public CyActivator cyactivator;
    public static CCStartMenu ccstartmenu;

    public CCCore(CyActivator cyactivator) {
        this.cyactivator = cyactivator;
        this.cyApplicationManager = cyactivator.cyApplicationManager;
        this.cyDesktopService = cyactivator.cyDesktopService;
        this.cyServiceRegistrar = cyactivator.cyServiceRegistrar;
        ccstartmenu = createCCStartMenu();
        registerServices();
        updatecurrentnetwork();
    }

    public void updatecurrentnetwork() {
        //get the network view object
        if (view == null) {
            view = null;
            network = null;
        } else {
            view = cyApplicationManager.getCurrentNetworkView();
            //get the network object; this contains the graph  
            network = view.getModel();
        }
    }

    public void closecore() {
        network = null;
        view = null;
    }

    public CCStartMenu createCCStartMenu() {
        CCStartMenu startmenu = new CCStartMenu(cyactivator, this);
        cyServiceRegistrar.registerService(startmenu, CytoPanelComponent.class, new Properties());
        CytoPanel cytopanelwest = cyDesktopService.getCytoPanel(CytoPanelName.WEST);
        int index = cytopanelwest.indexOfComponent(startmenu);
        cytopanelwest.setSelectedIndex(index);
        return startmenu;
    }

    public void closeCCStartMenu() {
        cyServiceRegistrar.unregisterService(ccstartmenu, CytoPanelComponent.class);
    }

    

    public CyApplicationManager getCyApplicationManager() {
        return this.cyApplicationManager;
    }

    public CySwingApplication getCyDesktopService() {
        return this.cyDesktopService;
    }
    
    public static CCStartMenu getCCStartMenu(){
        return ccstartmenu;
    }
    
    void registerServices(){
        ChangeEdgeAttributeListener changeEdgeAttributeListener = new ChangeEdgeAttributeListener();
        cyactivator.cyServiceRegistrar.registerService(changeEdgeAttributeListener, SetCurrentNetworkListener.class, new Properties());
        
    }
    
}
