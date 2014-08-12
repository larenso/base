package org.safehaus.subutai.plugin.presto.impl.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.plugin.presto.api.Config;
import org.safehaus.subutai.plugin.presto.impl.Commands;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dilshat on 5/7/14.
 */
public class StartNodeOperationHandler extends AbstractOperationHandler<PrestoImpl> {
	private final ProductOperation po;
	private final String lxcHostname;

	public StartNodeOperationHandler(PrestoImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Starting node %s in %s", lxcHostname, clusterName));
	}

	@Override
	public UUID getTrackerId() {
		return po.getId();
	}

	@Override
	public void run() {
		Config config = manager.getCluster(clusterName);
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

		po.addLog(String.format("Starting node %s...", node.getHostname()));

		Command startNodeCommand = Commands.getStartCommand(Util.wrapAgentToSet(node));
		final AtomicBoolean ok = new AtomicBoolean();
		manager.getCommandRunner().runCommand(startNodeCommand, new CommandCallback() {

			@Override
			public void onResponse(Response response, AgentResult agentResult, Command command) {
				if (agentResult.getStdOut().contains("Started")) {
					ok.set(true);
					stop();
				}
			}

		});

		if (ok.get()) {
			po.addLogDone(String.format("Node %s started", node.getHostname()));
		} else {
			po.addLogFailed(String.format("Starting node %s failed, %s", node.getHostname(), startNodeCommand.getAllErrors()));
		}
	}
}