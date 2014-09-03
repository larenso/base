/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui.wizard;


import java.util.ArrayList;
import java.util.UUID;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class StepSetBackupMasters extends VerticalLayout {

	public StepSetBackupMasters(final Wizard wizard) {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();
		verticalLayout.setMargin(true);

		GridLayout grid = new GridLayout(10, 10);
		grid.setSpacing(true);
		grid.setSizeFull();

		Label menu = new Label("Cluster Installation Wizard");

		menu.setContentMode(ContentMode.HTML);
		grid.addComponent(menu, 0, 0, 2, 1);
		grid.setComponentAlignment(menu, Alignment.TOP_CENTER);

		VerticalLayout verticalLayoutForm = new VerticalLayout();
		verticalLayoutForm.setSizeFull();
		verticalLayoutForm.setSpacing(true);

		Label configServersLabel = new Label("<strong>Choose hosts that will act as BackUpMaster");
		configServersLabel.setContentMode(ContentMode.HTML);
		verticalLayoutForm.addComponent(configServersLabel);

		final TwinColSelect select = new TwinColSelect("", new ArrayList<UUID>());
//        select.setItemCaptionPropertyId( "hostname" );
		select.setRows(7);
		select.setNullSelectionAllowed(false);
		select.setMultiSelect(false);
		select.setImmediate(true);
		select.setLeftColumnCaption("Available Nodes");
		select.setRightColumnCaption("BackUpMaster");
		select.setWidth(100, Unit.PERCENTAGE);
		select.setRequired(true);

		verticalLayoutForm.addComponent(select);

		grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
		grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

		Button next = new Button("Next");
		next.addStyleName("default");
		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				wizard.getConfig().setBackupMasters((String) select.getValue());
				if (wizard.getConfig().getBackupMasters() == null) {
					show("Please add backup servers");
				} else {
					wizard.next();
				}
			}
		});

		Button back = new Button("Back");
		back.addStyleName("default");
		back.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				wizard.back();
			}
		});

		verticalLayout.addComponent(grid);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addComponent(back);
		horizontalLayout.addComponent(next);
		verticalLayout.addComponent(horizontalLayout);

		addComponent(verticalLayout);
		select.setContainerDataSource(new BeanItemContainer<>(String.class, wizard.getConfig().getNodes()));
		select.setValue(wizard.getConfig().getBackupMasters());
	}


	private void show(String notification) {
		Notification.show(notification);
	}

}