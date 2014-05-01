/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.presto.wizard;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.presto.PrestoUI;

import java.util.*;

/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

    final Property.ValueChangeListener coordinatorComboChangeListener;
    final Property.ValueChangeListener workersSelectChangeListener;
    private final ComboBox hadoopClustersCombo;
    private final TwinColSelect workersSelect;
    private final ComboBox coordinatorNodeCombo;

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 4);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        hadoopClustersCombo = new ComboBox("Hadoop cluster");
        coordinatorNodeCombo = new ComboBox("Coordinator");
        workersSelect = new TwinColSelect("Workers", new ArrayList<Agent>());

        coordinatorNodeCombo.setMultiSelect(false);
        coordinatorNodeCombo.setImmediate(true);
        coordinatorNodeCombo.setTextInputAllowed(false);
        coordinatorNodeCombo.setRequired(true);
        coordinatorNodeCombo.setNullSelectionAllowed(false);

        hadoopClustersCombo.setMultiSelect(false);
        hadoopClustersCombo.setImmediate(true);
        hadoopClustersCombo.setTextInputAllowed(false);
        hadoopClustersCombo.setRequired(true);
        hadoopClustersCombo.setNullSelectionAllowed(false);

        workersSelect.setItemCaptionPropertyId("hostname");
        workersSelect.setRows(7);
//        workersSelect.setNullSelectionAllowed(false);
        workersSelect.setMultiSelect(true);
        workersSelect.setImmediate(true);
        workersSelect.setLeftColumnCaption("Available Nodes");
        workersSelect.setRightColumnCaption("Selected Nodes");
        workersSelect.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        workersSelect.setRequired(true);

        List<Config> clusters = PrestoUI.getHadoopManager().getClusters();

        //populate hadoop clusters combo
        if (clusters.size() > 0) {
            for (Config hadoopClusterInfo : clusters) {
                hadoopClustersCombo.addItem(hadoopClusterInfo);
                hadoopClustersCombo.setItemCaption(hadoopClusterInfo,
                        hadoopClusterInfo.getClusterName());
            }
        }

        Config info = PrestoUI.getHadoopManager().getCluster(wizard.getConfig().getClusterName());

        if (info != null) {
            //restore cluster
            hadoopClustersCombo.setValue(info);
        } else if (clusters.size() > 0) {
            hadoopClustersCombo.setValue(clusters.iterator().next());
        }

        //populate selection controls
        if (hadoopClustersCombo.getValue() != null) {
            Config hadoopInfo = (Config) hadoopClustersCombo.getValue();
            wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
            workersSelect.setContainerDataSource(
                    new BeanItemContainer<Agent>(
                            Agent.class, hadoopInfo.getAllNodes())
            );
            for (Agent agent : hadoopInfo.getAllNodes()) {
                coordinatorNodeCombo.addItem(agent);
                coordinatorNodeCombo.setItemCaption(agent, agent.getHostname());
            }
        }
        //restore coordinator
        if (wizard.getConfig().getCoordinatorNode() != null) {
            coordinatorNodeCombo.setValue(wizard.getConfig().getCoordinatorNode());
            workersSelect.getContainerDataSource().removeItem(wizard.getConfig().getCoordinatorNode());
        }

        //restore workers
        if (!Util.isCollectionEmpty(wizard.getConfig().getWorkers())) {
            workersSelect.setValue(wizard.getConfig().getWorkers());
            for (Agent worker : wizard.getConfig().getWorkers()) {
                coordinatorNodeCombo.removeItem(worker);
            }
        }

        //hadoop cluster selection change listener
        hadoopClustersCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Config hadoopInfo = (Config) event.getProperty().getValue();
                    workersSelect.setValue(null);
                    workersSelect.setContainerDataSource(
                            new BeanItemContainer<Agent>(
                                    Agent.class, hadoopInfo.getAllNodes())
                    );
                    coordinatorNodeCombo.setValue(null);
                    coordinatorNodeCombo.removeAllItems();
                    for (Agent agent : hadoopInfo.getAllNodes()) {
                        coordinatorNodeCombo.addItem(agent);
                        coordinatorNodeCombo.setItemCaption(agent, agent.getHostname());
                    }
                    wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
                    wizard.getConfig().setWorkers(new HashSet<Agent>());
                    wizard.getConfig().setCoordinatorNode(null);
                }
            }
        });

        //coordinator selection change listener
        coordinatorComboChangeListener = new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Agent coordinator = (Agent) event.getProperty().getValue();
                    wizard.getConfig().setCoordinatorNode(coordinator);

                    //clear workers
                    Config hadoopInfo = (Config) hadoopClustersCombo.getValue();
                    if (!Util.isCollectionEmpty(wizard.getConfig().getWorkers())) {
                        wizard.getConfig().getWorkers().remove(coordinator);
                    }
                    List<Agent> hadoopNodes = hadoopInfo.getAllNodes();
                    hadoopNodes.remove(coordinator);
                    workersSelect.getContainerDataSource().removeAllItems();
                    for (Agent agent : hadoopNodes) {
                        workersSelect.getContainerDataSource().addItem(agent);
                    }
                    workersSelect.removeListener(workersSelectChangeListener);
                    workersSelect.setValue(wizard.getConfig().getWorkers());
                    workersSelect.addListener(workersSelectChangeListener);

                }
            }
        };
        coordinatorNodeCombo.addListener(coordinatorComboChangeListener);

        //workers selection change listener
        workersSelectChangeListener = new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
                    wizard.getConfig().setWorkers(agentList);

                    //clear workers
                    if (wizard.getConfig().getCoordinatorNode() != null
                            && wizard.getConfig().getWorkers().contains(wizard.getConfig().getCoordinatorNode())) {

                        wizard.getConfig().setCoordinatorNode(null);
                        coordinatorNodeCombo.removeListener(coordinatorComboChangeListener);
                        coordinatorNodeCombo.setValue(null);
                        coordinatorNodeCombo.addListener(coordinatorComboChangeListener);

                    }
                    Config hadoopInfo = (Config) hadoopClustersCombo.getValue();
                    List<Agent> hadoopNodes = hadoopInfo.getAllNodes();
                    hadoopNodes.removeAll(wizard.getConfig().getWorkers());
                    coordinatorNodeCombo.removeAllItems();
                    for (Agent agent : hadoopNodes) {
                        coordinatorNodeCombo.addItem(agent);
                        coordinatorNodeCombo.setItemCaption(agent, agent.getHostname());
                    }
                    if (wizard.getConfig().getCoordinatorNode() != null) {
                        coordinatorNodeCombo.removeListener(coordinatorComboChangeListener);
                        coordinatorNodeCombo.setValue(wizard.getConfig().getCoordinatorNode());
                        coordinatorNodeCombo.addListener(coordinatorComboChangeListener);
                    }
                }
            }
        };
        workersSelect.addListener(workersSelectChangeListener);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Strings.isNullOrEmpty(wizard.getConfig().getClusterName())) {
                    show("Please, select Hadoop cluster");
                } else if (wizard.getConfig().getCoordinatorNode() == null) {
                    show("Please, select coordinator node");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getWorkers())) {
                    show("Please, select worker nodes");
                } else {
                    wizard.next();
                }
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.addComponent(new Label("Please, specify installation settings"));
        layout.addComponent(content);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(next);

        content.addComponent(hadoopClustersCombo);
        content.addComponent(coordinatorNodeCombo);
        content.addComponent(workersSelect);
        content.addComponent(buttons);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
