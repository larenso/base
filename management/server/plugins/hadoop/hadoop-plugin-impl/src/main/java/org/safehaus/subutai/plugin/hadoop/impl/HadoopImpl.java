package org.safehaus.subutai.plugin.hadoop.impl;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.networkmanager.NetworkManager;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.operation.Adding;
import org.safehaus.subutai.plugin.hadoop.impl.operation.Deletion;
import org.safehaus.subutai.plugin.hadoop.impl.operation.Installation;
import org.safehaus.subutai.plugin.hadoop.impl.operation.configuration.*;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daralbaev on 02.04.14.
 */
public class HadoopImpl implements Hadoop {
	public static final String MODULE_NAME = "Hadoop";

	private static CommandRunner commandRunner;
	private static AgentManager agentManager;
	private static DbManager dbManager;
	private static Tracker tracker;
	private static ContainerManager containerManager;
	private static NetworkManager networkManager;
	private static ExecutorService executor;
	private static EnvironmentManager environmentManager;

	public HadoopImpl(AgentManager agentManager,
	                  Tracker tracker,
	                  CommandRunner commandRunner,
	                  DbManager dbManager,
	                  NetworkManager networkManager,
	                  ContainerManager containerManager,
	                  EnvironmentManager environmentManager) {

		HadoopImpl.agentManager = agentManager;
		HadoopImpl.tracker = tracker;
		HadoopImpl.commandRunner = commandRunner;
		HadoopImpl.dbManager = dbManager;
		HadoopImpl.networkManager = networkManager;
		HadoopImpl.containerManager = containerManager;
		HadoopImpl.environmentManager = environmentManager;
	}

	public void init() {
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		executor.shutdown();
		commandRunner = null;
	}

	public static CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public static DbManager getDbManager() {
		return dbManager;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static ContainerManager getContainerManager() {
		return containerManager;
	}

	public static NetworkManager getNetworkManager() {
		return networkManager;
	}

	public static ExecutorService getExecutor() {
		return executor;
	}

	public static AgentManager getAgentManager() {
		return agentManager;
	}

	public static EnvironmentManager getEnvironmentManager() {
		return environmentManager;
	}

	@Override
	public UUID installCluster(final HadoopClusterConfig hadoopClusterConfig) {
		return new Installation(this, hadoopClusterConfig).execute();
	}

	@Override
	public UUID uninstallCluster(final String clusterName) {
		return new Deletion(this).execute(clusterName);
	}

	@Override
	public UUID startNameNode(HadoopClusterConfig hadoopClusterConfig) {
		return new NameNode(this, hadoopClusterConfig).start();
	}

	@Override
	public UUID stopNameNode(HadoopClusterConfig hadoopClusterConfig) {
		return new NameNode(this, hadoopClusterConfig).stop();
	}

	@Override
	public UUID restartNameNode(HadoopClusterConfig hadoopClusterConfig) {
		return new NameNode(this, hadoopClusterConfig).restart();
	}

	@Override
	public UUID statusNameNode(HadoopClusterConfig hadoopClusterConfig) {
		return new NameNode(this, hadoopClusterConfig).status();
	}

	@Override
	public UUID statusSecondaryNameNode(HadoopClusterConfig hadoopClusterConfig) {
		return new SecondaryNameNode(this, hadoopClusterConfig).status();
	}

	@Override
	public UUID statusDataNode(Agent agent) {
		return new DataNode(this, null).status(agent);
	}

	@Override
	public UUID startJobTracker(HadoopClusterConfig hadoopClusterConfig) {
		return new JobTracker(this, hadoopClusterConfig).start();
	}

	@Override
	public UUID stopJobTracker(HadoopClusterConfig hadoopClusterConfig) {
		return new JobTracker(this, hadoopClusterConfig).stop();
	}

	@Override
	public UUID restartJobTracker(HadoopClusterConfig hadoopClusterConfig) {
		return new JobTracker(this, hadoopClusterConfig).restart();
	}

	@Override
	public UUID statusJobTracker(HadoopClusterConfig hadoopClusterConfig) {
		return new JobTracker(this, hadoopClusterConfig).status();
	}

	@Override
	public UUID statusTaskTracker(Agent agent) {
		return new TaskTracker(this, null).status(agent);
	}

	@Override
	public UUID addNode(String clusterName) {
		return new Adding(this, clusterName).execute();
	}

	@Override
	public UUID blockDataNode(HadoopClusterConfig hadoopClusterConfig, Agent agent) {
		return new DataNode(this, hadoopClusterConfig).block(agent);
	}

	@Override
	public UUID blockTaskTracker(HadoopClusterConfig hadoopClusterConfig, Agent agent) {
		return new TaskTracker(this, hadoopClusterConfig).block(agent);
	}

	@Override
	public UUID unblockDataNode(HadoopClusterConfig hadoopClusterConfig, Agent agent) {
		return new DataNode(this, hadoopClusterConfig).unblock(agent);
	}

	@Override
	public UUID unblockTaskTracker(HadoopClusterConfig hadoopClusterConfig, Agent agent) {
		return new TaskTracker(this, hadoopClusterConfig).unblock(agent);
	}

	@Override
	public List<HadoopClusterConfig> getClusters() {
		return dbManager.getInfo(HadoopClusterConfig.PRODUCT_KEY, HadoopClusterConfig.class);
	}

	@Override
	public HadoopClusterConfig getCluster(String clusterName) {
		return dbManager.getInfo(HadoopClusterConfig.PRODUCT_KEY, clusterName, HadoopClusterConfig.class);
	}

	@Override
	public ClusterSetupStrategy getClusterSetupStrategy(ProductOperation po, HadoopClusterConfig hadoopClusterConfig) {
		return new HadoopDbSetupStrategy(po, this, containerManager, hadoopClusterConfig);
	}
}