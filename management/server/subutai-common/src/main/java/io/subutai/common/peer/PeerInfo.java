package io.subutai.common.peer;


import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.settings.Common;


/**
 * Holds info about peer
 */
public class PeerInfo implements Serializable
{
    @JsonProperty( "id" )
    private String id;

    @JsonProperty( "ownerId" )
    private String ownerId;

    @JsonProperty( "publicUrl" )
    private String publicUrl;

    @JsonProperty( "ip" )
    private String ip;

    @JsonProperty( "name" )
    private String name;

    @JsonProperty( "publicSecurePort" )
    private int publicSecurePort = Common.DEFAULT_PUBLIC_SECURE_PORT;

    @JsonProperty( "manualSetting" )
    private boolean manualSetting;


    public PeerInfo( @JsonProperty( "id" ) final String id, @JsonProperty( "ownerId" ) final String ownerId,
                     @JsonProperty( "publicUrl" ) final String publicUrl, @JsonProperty( "ip" ) final String ip,
                     @JsonProperty( "name" ) final String name,
                     @JsonProperty( "publicSecurePort" ) final int publicSecurePort,
                     @JsonProperty( "manualSetting" ) final boolean manualSetting )
    {
        this.id = id;
        this.ownerId = ownerId;
        this.publicUrl = publicUrl;
        this.ip = ip;
        this.name = name;
        this.publicSecurePort = publicSecurePort;
        this.manualSetting = manualSetting;
    }


    public PeerInfo()
    {

    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getIp()
    {
        return ip;
    }


    public String getPublicUrl()
    {
        return publicUrl;
    }


    @JsonIgnore
    public int getPort()
    {
        try
        {
            URL url = new URL( publicUrl );

            return url.getPort();
        }
        catch ( MalformedURLException e )
        {
            //ignore
        }

        return Common.DEFAULT_PUBLIC_PORT;
    }


    public void setPublicUrl( final String publicUrl )
    {
        try
        {
            URL url = new URL( publicUrl );
            this.ip = url.getHost();
            this.publicUrl = publicUrl;
        }
        catch ( MalformedURLException e )
        {
            // assume this is IP or domain name
            final String u = String.format( "https://%s:%s/", publicUrl, Common.DEFAULT_PUBLIC_PORT );
            try
            {
                URL url = new URL( u );
                this.ip = url.getHost();
                this.publicUrl = u;
            }
            catch ( MalformedURLException e1 )
            {
                throw new IllegalArgumentException( "Invalid public URL." );
            }
        }
    }


    public int getPublicSecurePort()
    {
        return publicSecurePort;
    }


    public void setPublicSecurePort( final int publicSecurePort )
    {
        this.publicSecurePort = publicSecurePort;
    }


    public boolean isManualSetting()
    {
        return manualSetting;
    }


    public void setManualSetting( final boolean manualSetting )
    {
        this.manualSetting = manualSetting;
    }


    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode( this.id );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof PeerInfo )
        {
            PeerInfo other = ( PeerInfo ) obj;
            return Objects.equals( this.id, other.id );
        }
        return false;
    }


    @Override
    public String toString()
    {
        return "PeerInfo{" + "ip='" + ip + '\'' + ", name='" + name + '\'' + ", id=" + id + ", ownerId=" + ownerId
                + '}';
    }
}

