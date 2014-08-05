/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author dilshat
 */
public class AccumuloUI implements PortalModule {

	public static final String MODULE_IMAGE = "accumulo.png";

	private static Accumulo accumuloManager;
	private static Hadoop hadoopManager;
	private static Zookeeper zookeeperManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static ExecutorService executor;
	private static CommandRunner commandRunner;


	public AccumuloUI(AgentManager agentManager, Tracker tracker, Accumulo accumuloManager, Hadoop hadoopManager,
	                  Zookeeper zookeeperManager, CommandRunner commandRunner) {
		AccumuloUI.agentManager = agentManager;
		AccumuloUI.tracker = tracker;
		AccumuloUI.accumuloManager = accumuloManager;
		AccumuloUI.hadoopManager = hadoopManager;
		AccumuloUI.zookeeperManager = zookeeperManager;
		AccumuloUI.commandRunner = commandRunner;
	}


	public static Zookeeper getZookeeperManager() {
		return zookeeperManager;
	}


	public static Hadoop getHadoopManager() {
		return hadoopManager;
	}


	public static Tracker getTracker() {
		return tracker;
	}


	public static Accumulo getAccumuloManager() {
		return accumuloManager;
	}


	public static ExecutorService getExecutor() {
		return executor;
	}


	public static AgentManager getAgentManager() {
		return agentManager;
	}

	public static CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		accumuloManager = null;
		agentManager = null;
		tracker = null;
		hadoopManager = null;
		zookeeperManager = null;
		executor.shutdown();
	}


	@Override
	public String getId() {
		return AccumuloClusterConfig.PRODUCT_KEY;
	}

	public String getName() {
		return AccumuloClusterConfig.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(AccumuloUI.MODULE_IMAGE, this);
	}


	public Component createComponent() {
		return new AccumuloForm();
	}
}