package org.safehaus.subutai.plugin.spark.impl;

import com.google.common.base.Preconditions;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

public class SetupBase {

    final ProductOperation po;
    final SparkImpl manager;
    final SparkClusterConfig config;

    public SetupBase(ProductOperation po, SparkImpl manager, SparkClusterConfig config) {

        Preconditions.checkNotNull(config, "Presto cluster config is null");
        Preconditions.checkNotNull(po, "Product operation tracker is null");
        Preconditions.checkNotNull(manager, "Presto manager is null");

        this.po = po;
        this.manager = manager;
        this.config = config;
    }

    void checkConnected() throws ClusterSetupException {

        String hostname = config.getMasterNode().getHostname();
        if (manager.getAgentManager().getAgentByHostname(hostname) == null)
            throw new ClusterSetupException("Master node is not connected");

        for (Agent a : config.getSlaveNodes()) {
            if (manager.getAgentManager().getAgentByHostname(a.getHostname()) == null)
                throw new ClusterSetupException("Not all slave nodes are connected");
        }
    }

}
