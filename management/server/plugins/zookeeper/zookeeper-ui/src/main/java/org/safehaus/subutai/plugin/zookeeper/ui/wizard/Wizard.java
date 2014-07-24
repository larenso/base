/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.ui.wizard;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;


/**
 * @author dilshat
 */
public class Wizard {

	private final GridLayout grid;
	private int step = 1;
	private ZookeeperClusterConfig config = new ZookeeperClusterConfig();

	public Wizard() {
		grid = new GridLayout(1, 20);
		grid.setMargin(true);
		grid.setSizeFull();

		putForm();
	}

	public Component getContent() {
		return grid;
	}

	protected void next() {
		step++;
		putForm();
	}

	protected void back() {
		step--;
		putForm();
	}

	protected void init() {
		step = 1;
		config = new ZookeeperClusterConfig();
		putForm();
	}

	public ZookeeperClusterConfig getConfig() {
		return config;
	}

	private void putForm() {
		grid.removeComponent(0, 1);
		Component component = null;
		switch (step) {
			case 1: {
				component = new WelcomeStep(this);
				break;
			}
			case 2: {
				component = new ConfigurationStep(this);
				break;
			}
			case 3: {
				component = new VerificationStep(this);
				break;
			}
			default: {
				break;
			}
		}

		if (component != null) {
			grid.addComponent(component, 0, 1, 0, 19);
		}
	}

}