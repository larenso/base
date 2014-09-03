package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;


/**
 * Created by bahadyr on 8/25/14.
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    private CassandraConfig config;


    public CheckNodeOperationHandler( final CassandraImpl manager, final CassandraConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public void run() {
        // TODO
    }
}