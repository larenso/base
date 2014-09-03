package org.safehaus.subutai.plugin.flume.impl.handler;

import java.util.Arrays;
import java.util.HashSet;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.impl.CommandType;
import org.safehaus.subutai.plugin.flume.impl.Commands;
import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;

public class StatusHandler extends AbstractOperationHandler<FlumeImpl> {

    private final String hostname;

    public StatusHandler(FlumeImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.productOperation = manager.getTracker().createProductOperation(
                FlumeConfig.PRODUCT_KEY, "Check status of " + hostname);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        if(manager.getCluster(clusterName) == null) {
            po.addLogFailed("Cluster does not exist: " + clusterName);
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname(hostname);
        if(node == null) {
            po.addLogFailed("Node is not connected: " + hostname);
            return;
        }

        po.addLog("Checking node...");
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.STATUS)),
                new HashSet<>(Arrays.asList(node)));
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            AgentResult result = cmd.getResults().get(node.getUuid());
            if(result.getStdOut().contains(Commands.PACKAGE_NAME))
                po.addLogDone("Flume installed on " + node.getHostname());
            else
                po.addLogFailed("Flume not installed on " + node.getHostname());
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to check Flume on " + hostname);
        }
    }

}