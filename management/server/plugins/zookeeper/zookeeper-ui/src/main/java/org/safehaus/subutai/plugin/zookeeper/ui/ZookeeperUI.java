/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.ui;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class ZookeeperUI implements PortalModule {

	public static final String MODULE_IMAGE = "zookeeper.png";

	private static Zookeeper manager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Hadoop hadoopManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public ZookeeperUI(AgentManager agentManager, Tracker tracker, Zookeeper manager, Hadoop hadoopManager, CommandRunner commandRunner) {
		ZookeeperUI.agentManager = agentManager;
		ZookeeperUI.tracker = tracker;
		ZookeeperUI.manager = manager;
		ZookeeperUI.hadoopManager = hadoopManager;
		ZookeeperUI.commandRunner = commandRunner;
	}

	public static Hadoop getHadoopManager() {
		return hadoopManager;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Zookeeper getManager() {
		return manager;
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
		manager = null;
		agentManager = null;
		tracker = null;
		hadoopManager = null;
		executor.shutdown();
	}

	@Override
	public String getId() {
		return ZookeeperClusterConfig.PRODUCT_KEY;
	}

	public String getName() {
		return ZookeeperClusterConfig.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(ZookeeperUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new Form();
	}

}