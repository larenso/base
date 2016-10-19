package io.subutai.core.lxc.quota.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerResource;
import io.subutai.hub.share.quota.ContainerResourceFactory;
import io.subutai.hub.share.quota.Quota;
import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.ResourceValue;
import io.subutai.hub.share.resource.ResourceValueParser;


@Command( scope = "quota", name = "set", description = "Sets specified quota to container" )
public class SetQuota extends SubutaiShellCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SetQuota.class );
    private final LocalPeer localPeer;
    private QuotaManager quotaManager;

    @Argument( index = 0, name = "container name", required = true, multiValued = false, description = "specify "
            + "container name" )
    private String containerName;

    @Argument( index = 1, name = "resource type", required = true, multiValued = false, description =
            "specify resource " + "type" )
    private String resourceType;

    @Argument( index = 2, name = "quota value", required = true, multiValued = false, description = "set quota value" )
    private String quotaValue;

    @Argument( index = 3, name = "quota threshold", required = true, multiValued = false, description = "set quota "
            + "threshold" )
    private Integer threshold;


    public SetQuota( QuotaManager quotaManager, LocalPeer localPeer )
    {
        this.quotaManager = quotaManager;
        this.localPeer = localPeer;
    }


    public void setContainerName( final String containerName )
    {
        this.containerName = containerName;
    }


    public void setResourceType( final String resourceType )
    {
        this.resourceType = resourceType;
    }


    public void setQuotaValue( final String quotaValue )
    {
        this.quotaValue = quotaValue;
    }


    public void setThreshold( final Integer threshold )
    {
        this.threshold = threshold;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        ContainerResourceType type = ContainerResourceType.parse( resourceType );

        ContainerQuota containerQuota = new ContainerQuota();

        final ResourceValueParser parser = quotaManager.getResourceValueParser( type );

        final ResourceValue value = parser.parse( quotaValue );

        ContainerResource containerResource = ContainerResourceFactory.createContainerResource( type, value );

        containerQuota.add( new Quota( containerResource, threshold ) );
        final ContainerHost container = localPeer.getContainerHostByName( containerName );
        if ( container == null )
        {
            System.out.println( "Container not found by id." );
        }
        else
        {
            final ContainerId id = container.getContainerId();
            quotaManager.setQuota( id, containerQuota );
        }
        return null;
    }
}
