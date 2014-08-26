package org.safehaus.subutai.plugin.spark.impl.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {
	private final ProductOperation po;
	private final String lxcHostname;

	public CheckNodeOperationHandler(SparkImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		po = manager.getTracker().createProductOperation(SparkClusterConfig.PRODUCT_KEY,
				String.format("Checking state of %s in %s", lxcHostname, clusterName));
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

		po.addLog("Checking node...");

		Command checkNodeCommand = Commands.getStatusAllCommand(node);
		manager.getCommandRunner().runCommand(checkNodeCommand);

		AgentResult res = checkNodeCommand.getResults().get(node.getUuid());
		if (checkNodeCommand.hasSucceeded()) {
			po.addLogDone(String.format("%s", res.getStdOut()));
		} else {
			po.addLogFailed(String.format("Faied to check status, %s", checkNodeCommand.getAllErrors()));
		}
	}
}