package org.safehaus.subutai.impl.cassandra;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.CassandraConfig;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.networkmanager.NetworkManager;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.impl.cassandra.handler.CheckAllNodesOperationHandler;
import org.safehaus.subutai.impl.cassandra.handler.CheckCassandraServiceStatusOperationHandler;
import org.safehaus.subutai.impl.cassandra.handler.InstallOperationHandler;
import org.safehaus.subutai.impl.cassandra.handler.StartAllNodesOperationHandler;
import org.safehaus.subutai.impl.cassandra.handler.StartCassandraServiceOperationHandler;
import org.safehaus.subutai.impl.cassandra.handler.StopAllNodesOperationHandler;
import org.safehaus.subutai.impl.cassandra.handler.StopCassandraServiceOperationHandler;
import org.safehaus.subutai.impl.cassandra.handler.UninstallOperationHandler;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.shared.protocol.NodeGroup;
import org.safehaus.subutai.shared.protocol.PlacementStrategy;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class CassandraImpl implements Cassandra {

    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;
    private NetworkManager networkManager;
    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;


    public CassandraImpl( final DbManager dbManager, final Tracker tracker, final LxcManager lxcManager,
                          final ExecutorService executor, final NetworkManager networkManager,
                          final CommandRunner commandRunner, final AgentManager agentManager,
                          final EnvironmentManager environmentManager, final ContainerManager containerManager ) {
        this.dbManager = dbManager;
        this.tracker = tracker;
        this.lxcManager = lxcManager;
        this.executor = executor;
        this.networkManager = networkManager;
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.environmentManager = environmentManager;
        this.containerManager = containerManager;
    }


    public LxcManager getLxcManager() {
        return lxcManager;
    }


    public ExecutorService getExecutor() {
        return executor;
    }


    public void setExecutor( final ExecutorService executor ) {
        this.executor = executor;
    }


    public NetworkManager getNetworkManager() {
        return networkManager;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public DbManager getDbManager() {
        return dbManager;
    }


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager ) {
        this.containerManager = containerManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager ) {
        this.environmentManager = environmentManager;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void init() {
        Commands.init( commandRunner );
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public void setLxcManager( LxcManager lxcManager ) {
        this.lxcManager = lxcManager;
    }


    public void setDbManager( DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    public void setNetworkManager( NetworkManager networkManager ) {
        this.networkManager = networkManager;
    }


    public void setCommandRunner( CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public UUID installCluster( final CassandraConfig config ) {
        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startAllNodes( final String clusterName ) {
        AbstractOperationHandler operationHandler = new StartAllNodesOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopAllNodes( final String clusterName ) {

        AbstractOperationHandler operationHandler = new StopAllNodesOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startCassandraService( final String clusterName, final String agentUUID ) {
        AbstractOperationHandler operationHandler =
                new StartCassandraServiceOperationHandler( this, clusterName, agentUUID );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopCassandraService( final String clusterName, final String agentUUID ) {

        AbstractOperationHandler operationHandler =
                new StopCassandraServiceOperationHandler( this, clusterName, agentUUID );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusCassandraService( final String clusterName, final String agentUUID ) {

        AbstractOperationHandler operationHandler =
                new CheckCassandraServiceStatusOperationHandler( this, clusterName, agentUUID );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final CassandraConfig config,
                                                         final ProductOperation po ) {
        return new CassandraSetupStrategy( environment, config, po, this );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final CassandraConfig config ) {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", config.PRODUCT_KEY, UUID.randomUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        environmentBlueprint.setExchangeSshKeys( true );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        nodeGroup.setNumberOfNodes( config.getNumberOfNodes() );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );

        return environmentBlueprint;
    }


    @Override
    public UUID checkAllNodes( final String clusterName ) {

        AbstractOperationHandler operationHandler = new CheckAllNodesOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<CassandraConfig> getClusters() {

        return dbManager.getInfo( CassandraConfig.PRODUCT_KEY, CassandraConfig.class );
    }


    @Override
    public CassandraConfig getCluster( String clusterName ) {
        return dbManager.getInfo( CassandraConfig.PRODUCT_KEY, clusterName, CassandraConfig.class );
    }
}