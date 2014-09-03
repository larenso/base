/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.plugin.hbase.ui.manager.Manager;
import org.safehaus.subutai.plugin.hbase.ui.wizard.Wizard;

/**
 * @author dilshat
 */
public class HBaseForm extends CustomComponent {

	private final Wizard wizard;
	private final Manager manager;

	public HBaseForm() {
		setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		TabSheet mongoSheet = new TabSheet();
		mongoSheet.setSizeFull();
		manager = new Manager();
		wizard = new Wizard();
		mongoSheet.addTab(wizard.getContent(), "Install");
		mongoSheet.addTab(manager.getContent(), "Manage");

		verticalLayout.addComponent(mongoSheet);
		setCompositionRoot(verticalLayout);
		manager.refreshClustersInfo();
	}

}