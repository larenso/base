package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.oozie.api.OozieConfig;
import org.safehaus.subutai.plugin.oozie.impl.Commands;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;


/**
 * Created by bahadyr on 8/25/14.
 */
public class StopServerOperationHandler extends AbstractOperationHandler<OozieImpl> {

    private ProductOperation po;
    private String clusterName;


    public StopServerOperationHandler( final OozieImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( OozieConfig.PRODUCT_KEY,
                String.format( "Starting server on %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( OozieConfig.PRODUCT_KEY,
                String.format( "Stopping cluster %s", clusterName ) );
        manager.getExecutor().execute( new Runnable() {

            public void run() {
                OozieConfig config =
                        manager.getDbManager().getInfo( OozieConfig.PRODUCT_KEY, clusterName, OozieConfig.class );
                if ( config == null ) {
                    po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted",
                            config.getClusterName() ) );
                    return;
                }
                Agent serverAgent = manager.getAgentManager().getAgentByHostname( config.getServer() );
                if ( serverAgent == null ) {
                    po.addLogFailed( String.format( "Server agent %s not connected", config.getServer() ) );
                    return;
                }
                Set<Agent> servers = new HashSet<Agent>();
                servers.add( serverAgent );
                Command stopServiceCommand = Commands.getStopServerCommand( servers );
                manager.getCommandRunner().runCommand( stopServiceCommand );

                if ( stopServiceCommand.hasSucceeded() ) {
                    po.addLogDone( "Stop succeeded" );
                }
                else {
                    po.addLogFailed( String.format( "Stop failed, %s", stopServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}