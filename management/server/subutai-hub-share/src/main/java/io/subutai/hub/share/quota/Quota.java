package io.subutai.hub.share.quota;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import io.subutai.hub.share.resource.ContainerResourceType;


public class Quota
{
    @JsonProperty( "resource" )
    private ContainerResource resource;

    @JsonProperty( "threshold" )
    private Integer threshold;


    public Quota( @JsonProperty( "resource" ) final ContainerResource resource,
                  @JsonProperty( "threshold" ) final Integer threshold )
    {
        Preconditions.checkNotNull( resource );
        Preconditions.checkNotNull( threshold );
        Preconditions.checkArgument( threshold >= 0 );
        Preconditions.checkArgument( threshold <= 100 );

        this.resource = resource;
        this.threshold = threshold;
    }


    public ContainerResource getResource()
    {
        return resource;
    }


    public Integer getThreshold()
    {
        return threshold;
    }


    @JsonIgnore
    public ContainerCpuResource getAsCpuResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.CPU )
        {
            return ( ContainerCpuResource ) resource;
        }
        throw new IllegalStateException( "Could not get as CPU resource." );
    }


    @JsonIgnore
    public ContainerRamResource getAsRamResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.RAM )
        {
            return ( ContainerRamResource ) resource;
        }
        throw new IllegalStateException( "Could not get as RAM resource." );
    }


    @JsonIgnore
    public ContainerDiskResource getAsDiskResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.OPT
                || resource.getContainerResourceType() == ContainerResourceType.HOME
                || resource.getContainerResourceType() == ContainerResourceType.ROOTFS
                || resource.getContainerResourceType() == ContainerResourceType.VAR )
        {
            return ( ContainerDiskResource ) resource;
        }
        throw new IllegalStateException( "Could not get as disk resource." );
    }


    @Override
    public String toString()
    {
        return "Quota{" + "resource=" + resource + ", threshold=" + threshold + '}';
    }
}
