package org.cytoscapeapp.ccresolver.internal;

import java.awt.event.ActionEvent;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscapeapp.ccresolver.internal.CyActivator;
import org.cytoscapeapp.ccresolver.internal.CCCore;

/**
 * Creates a new menu item under Apps menu section.
 *
 */
public class CCMenuAction extends AbstractCyAction {

    public CyApplicationManager cyApplicationManager;
    public CySwingApplication cyDesktopService;
    public CyActivator cyactivator;

    public CCMenuAction(CyApplicationManager cyApplicationManager, final String menuTitle, CyActivator cyactivator) {
        super(menuTitle, cyApplicationManager, null, null);
        setPreferredMenu("Apps");
        this.cyactivator = cyactivator;
        this.cyApplicationManager = cyApplicationManager;
        this.cyDesktopService = cyactivator.getcytoscapeDesktopService();
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("Starting Control Capacity Resolver menu in control panel");
        CCCore cccore = new CCCore(cyactivator);
    }
}
