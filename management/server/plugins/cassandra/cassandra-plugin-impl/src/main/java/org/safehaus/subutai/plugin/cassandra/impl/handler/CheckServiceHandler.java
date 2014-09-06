package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;

import com.google.common.collect.Sets;


/**
 * Created by bahadyr on 8/25/14.
 */
public class CheckServiceHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    //    private CassandraConfig config;
    private String agentUUID;

    private String clusterName;


    public CheckServiceHandler( final CassandraImpl manager, final String clusterName, final String agentUUID ) {
        super( manager, clusterName );
        this.agentUUID = agentUUID;
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Checking status of Cassandra service on %s", agentUUID ) );
        manager.getExecutor().execute( new Runnable() {
            Agent agent = manager.getAgentManager().getAgentByUUID( UUID.fromString( agentUUID ) );


            public void run() {
                Command statusServiceCommand = Commands.getStatusCommand( Sets.newHashSet( agent ) );
                manager.getCommandRunner().runCommand( statusServiceCommand );
                if ( statusServiceCommand.hasSucceeded() ) {
                    AgentResult ar = statusServiceCommand.getResults().get( agent.getUuid() );
                    if ( ar.getStdOut().contains( "is running" ) ) {
                        po.addLogDone( "Cassandra is running" );
                    }
                    else {
                        po.addLogFailed( "Cassandra is not running" );
                    }
                }
                else {
                    po.addLogFailed( "Cassandra is not running" );
                    //                    po.addLogFailed(String.format("Status check failed, %s",
                    // statusServiceCommand.getAllErrors()));
                }
            }
        } );
    }
}