/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;
import org.safehaus.subutai.plugin.mongodb.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import com.google.common.base.Preconditions;


/**
 * @author dilshat
 */
public class MongoImpl implements Mongo {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ContainerManager containerManager;
    private ExecutorService executor;


    public MongoImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
                      ContainerManager containerManager ) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        this.containerManager = containerManager;

        Commands.init( commandRunner );
    }


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public DbManager getDbManager() {
        return dbManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public UUID installCluster( MongoClusterConfig config ) {

        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( final String clusterName, final NodeType nodeType ) {

        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<MongoClusterConfig> getClusters() {

        return dbManager.getInfo( MongoClusterConfig.PRODUCT_KEY, MongoClusterConfig.class );
    }


    @Override
    public MongoClusterConfig getCluster( String clusterName ) {
        return dbManager.getInfo( MongoClusterConfig.PRODUCT_KEY, clusterName, MongoClusterConfig.class );
    }


    public UUID startNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final MongoClusterConfig config, final ProductOperation po ) {
        return new MongoDbSetupStrategy( po, agentManager, this, commandRunner, containerManager, config );
    }
}