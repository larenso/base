package org.safehaus.subutai.cli.commands;


import java.io.IOException;
import java.util.UUID;

import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.CassandraConfig;
import org.safehaus.subutai.api.tracker.Tracker;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "service-cassandra-status", description = "Command to check Cassandra service")
public class StatusServiceCommand extends OsgiCommandSupport {

    private static Cassandra cassandraManager;
    private static Tracker tracker;


    public Tracker getTracker() {
        return tracker;
    }


    public void setTracker( Tracker tracker ) {
        StatusServiceCommand.tracker = tracker;
    }


    public void setCassandraManager( Cassandra cassandraManager ) {
        StatusServiceCommand.cassandraManager = cassandraManager;
    }


    public static Cassandra getCassandraManager() {
        return cassandraManager;
    }


    @Argument( index = 0, name = "clusterName", description = "Name of the cluster.", required = true,
            multiValued = false )
    String clusterName = null;
    @Argument(index = 1, name = "agentUUID", description = "UUID of the agent.", required = true, multiValued = false)
    String agentUUID = null;


    protected Object doExecute() throws IOException {

        UUID uuid = cassandraManager.statusCassandraService( clusterName, agentUUID );
        tracker.printOperationLog( CassandraConfig.PRODUCT_KEY, uuid, 30000 );

        return null;
    }
}