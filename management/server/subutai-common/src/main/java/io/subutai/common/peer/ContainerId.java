package io.subutai.common.peer;


import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.host.HostId;


/**
 * Container host identifier
 */

public class ContainerId extends HostId
{
    @JsonProperty( "hostName" )
    private String hostName;
    @JsonProperty( "peerId" )
    private PeerId peerId;
    @JsonProperty( "environmentId" )
    private EnvironmentId environmentId;


    public ContainerId( @JsonProperty( "id" ) final String id )
    {
        super( id );
    }


    public ContainerId( final String id, final String hostName, final PeerId peerId, final EnvironmentId environmentId )
    {
        super( id );
        this.hostName = hostName;
        this.peerId = peerId;
        this.environmentId = environmentId;
    }


    public String getHostName()
    {
        return hostName;
    }


    public PeerId getPeerId()
    {
        return peerId;
    }


    public EnvironmentId getEnvironmentId()
    {
        return environmentId;
    }
}
