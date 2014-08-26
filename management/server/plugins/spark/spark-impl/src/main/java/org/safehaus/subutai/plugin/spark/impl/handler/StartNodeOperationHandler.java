package org.safehaus.subutai.plugin.spark.impl.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dilshat on 5/7/14.
 */
public class StartNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {
	private final ProductOperation po;
	private final String lxcHostname;
	private final boolean master;

	public StartNodeOperationHandler(SparkImpl manager, String clusterName, String lxcHostname, boolean master) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		this.master = master;
		po = manager.getTracker().createProductOperation(SparkClusterConfig.PRODUCT_KEY,
				String.format("Starting node %s in %s", lxcHostname, clusterName));
	}

	@Override
	public UUID getTrackerId() {
		return po.getId();
	}

	@Override
	public void run() {
		SparkClusterConfig config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
			return;
		}

		Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (node == null) {
			po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
			return;
		}

		if (!config.getAllNodes().contains(node)) {
			po.addLogFailed(String.format("Node %s does not belong to this cluster", lxcHostname));
			return;
		}

		if (master && !config.getMasterNode().equals(node)) {
			po.addLogFailed(String.format("Node %s is not a master node\nOperation aborted", node.getHostname()));
			return;
		} else if (!master && !config.getSlaveNodes().contains(node)) {
			po.addLogFailed(String.format("Node %s is not a slave node\nOperation aborted", node.getHostname()));
			return;
		}

		po.addLog(String.format("Starting %s on %s...", master ? "master" : "slave", node.getHostname()));

		Command startCommand;
		if (master) {
			startCommand = Commands.getStartMasterCommand(node);
		} else {
			startCommand = Commands.getStartSlaveCommand(node);
		}

		final AtomicBoolean ok = new AtomicBoolean();
		manager.getCommandRunner().runCommand(startCommand, new CommandCallback() {

			@Override
			public void onResponse(Response response, AgentResult agentResult, Command command) {
				if (agentResult.getStdOut().contains("starting")) {
					ok.set(true);
					stop();
				}
			}

		});

		if (ok.get()) {
			po.addLogDone(String.format("Node %s started", node.getHostname()));
		} else {
			po.addLogFailed(String.format("Starting node %s failed, %s", node.getHostname(), startCommand.getAllErrors()));
		}
	}
}