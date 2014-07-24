package org.safehaus.subutai.plugin.zookeeper.impl.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class StopNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public StopNodeOperationHandler(ZookeeperImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format("Stopping node %s in %s", lxcHostname, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        ZookeeperClusterConfig config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (node == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
            return;
        }
        if (!config.getNodes().contains(node)) {
            po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
            return;
        }
        po.addLog("Stopping node...");

        Command stopCommand = Commands.getStopCommand(node);
        manager.getCommandRunner().runCommand(stopCommand);
        NodeState state = NodeState.UNKNOWN;
        if (stopCommand.hasCompleted()) {
            AgentResult result = stopCommand.getResults().get(node.getUuid());
            if (result.getStdOut().contains("STOPPED")) {
                state = NodeState.STOPPED;
            }
        }

        if (NodeState.STOPPED.equals(state)) {
            po.addLogDone(String.format("Node on %s stopped", lxcHostname));
        } else {
            po.addLogFailed(String.format("Failed to stop node %s. %s",
                    lxcHostname, stopCommand.getAllErrors()
            ));
        }
    }
}