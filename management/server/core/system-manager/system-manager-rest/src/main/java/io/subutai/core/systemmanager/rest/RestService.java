package io.subutai.core.systemmanager.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConfigurationException;


public interface RestService
{
    @GET
    @Path( "about" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getSubutaiInfo() throws ConfigurationException;

    @GET
    @Path( "peer_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPeerSettings();

    @POST
    @Path( "update_peer_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setPeerSettings();


    @GET
    @Path( "peer_policy" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPeerPolicy();

    @POST
    @Path( "update_peer_policy" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setPeerPolicy( @FormParam( "peerId" ) String peerId,
                                   @FormParam( "diskUsageLimit" ) String diskUsageLimit,
                                   @FormParam( "cpuUsageLimit" ) String cpuUsageLimit,
                                   @FormParam( "memoryUsageLimit" ) String memoryUsageLimit,
                                   @FormParam( "environmentLimit" ) String environmentLimit,
                                   @FormParam( "containerLimit" ) String containerLimit );


    @GET
    @Path( "network_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getNetworkSettings() throws ConfigurationException;

    @POST
    @Path( "update_network_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setNetworkSettings( @FormParam( "publicUrl" ) String publicUrl,
                                        @FormParam( "publicSecurePort" ) String publicSecurePort,
                                        @FormParam( "startRange" ) String startRange,
                                        @FormParam( "endRange" ) String endRange ) throws ConfigurationException;

    @GET
    @Path( "advanced_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getAdvancedSettings();


    @GET
    @Path( "management_updates" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getManagementUpdates();


    @POST
    @Path( "update_management" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response update();
}
