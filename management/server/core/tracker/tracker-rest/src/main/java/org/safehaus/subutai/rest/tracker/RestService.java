package org.safehaus.subutai.rest.tracker;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


public interface RestService {

    @GET
    @Path("get_product_operation/{source}/{uuid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getProductOperation( @PathParam("source") String source, @PathParam("uuid") String uuid );


    @GET
    @Path( "get_product_operations/{source}/{dateFrom}/{dateTo}/{limit}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String getProductOperations( @PathParam( "source" ) String source, @PathParam( "dateFrom" ) String fromDate,
                                        @PathParam( "dateTo" ) String toDate, @PathParam( "limit" ) int limit );

    @GET
    @Path("get_product_operation_sources")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getProductOperationSources();
}