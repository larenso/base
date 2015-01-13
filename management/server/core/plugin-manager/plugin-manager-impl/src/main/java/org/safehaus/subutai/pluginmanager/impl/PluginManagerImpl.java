package org.safehaus.subutai.pluginmanager.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.pluginmanager.api.PluginInfo;
import org.safehaus.subutai.pluginmanager.api.PluginManager;
import org.safehaus.subutai.pluginmanager.api.PluginManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class PluginManagerImpl implements PluginManager

{
    private static final Logger LOG = LoggerFactory.getLogger( PluginManagerImpl.class.getName() );
    private Commands commands;
    private ManagerHelper managerHelper;
    protected Tracker tracker;
    protected ExecutorService executor;

    public static final String PRODUCT_KEY = "Plugin";


    public PluginManagerImpl( final PeerManager peerManager, final Tracker tracker )
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( tracker );

        this.managerHelper = new ManagerHelper( peerManager );
        this.tracker = tracker;
        commands = new Commands();
        executor = Executors.newCachedThreadPool();
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public Commands getCommands()
    {
        return commands;
    }


    @Override
    public UUID installPlugin( final String pluginName )
    {
        PluginOperationHandler handler =
                new PluginOperationHandler( this, managerHelper, pluginName, OperationType.INSTALL );
        executor.execute( handler );
        return handler.getTrackerId();
    }


    @Override
    public UUID removePlugin( final String pluginName )
    {
        PluginOperationHandler handler =
                new PluginOperationHandler( this, managerHelper, pluginName, OperationType.REMOVE );
        executor.execute( handler );
        return handler.getTrackerId();
    }


    @Override
    public UUID upgradePlugin( final String pluginName )
    {
        PluginOperationHandler handler =
                new PluginOperationHandler( this, managerHelper, pluginName, OperationType.UPGRADE );
        executor.execute( handler );
        return handler.getTrackerId();
    }


    @Override
    public Set<PluginInfo> getInstalledPlugins()
    {
        String result = null;
        try
        {
            result = managerHelper.execute( commands.makeCheckCommand() );
        }
        catch ( PluginManagerException e )
        {
            LOG.error( e.getMessage() );
            e.printStackTrace();
        }

        return managerHelper.parsePluginNamesAndVersions( result );
    }


    @Override
    public Set<PluginInfo> getAvailablePlugins()
    {
        return managerHelper.getDifferenceBetweenPlugins( getInstalledPlugins(), managerHelper.parseJson() );
    }


    @Override
    public List<String> getAvailablePluginNames()
    {
        return null;
    }


    @Override
    public List<String> getAvaileblePluginVersions()
    {
        return null;
    }


    @Override
    public List<String> getInstalledPluginVersions()
    {

        List<String> versions = new ArrayList<>();
        for ( PluginInfo p : getInstalledPlugins() )
        {
            versions.add( p.getVersion() );
        }
        return versions;
    }


    @Override
    public List<String> getInstalledPluginNames()
    {
        List<String> names = new ArrayList<>();
        for ( PluginInfo p : getInstalledPlugins() )
        {
            names.add( p.getPluginName() );
        }
        return names;
    }


    @Override
    public String getPluginVersion( final String pluginName )
    {
        return null;
    }


    @Override
    public boolean isUpgradeAvailable( final String pluginName )
    {
        boolean upgrade = false;
        String currentVersion = managerHelper.findVersion( getInstalledPlugins(), pluginName );
        String newVersion = managerHelper.findVersion( getAvailablePlugins(), pluginName );

        if ( !currentVersion.equals( newVersion ) )
        {
            upgrade = true;
        }
        return upgrade;
    }


    @Override
    public String getProductKey()
    {
        return PRODUCT_KEY;
    }
}
