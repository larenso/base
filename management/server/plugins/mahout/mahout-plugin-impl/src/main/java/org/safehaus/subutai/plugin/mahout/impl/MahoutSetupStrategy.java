package org.safehaus.subutai.plugin.mahout.impl;


import org.safehaus.subutai.plugin.mahout.api.MahoutConfig;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;


/**
 * Created by bahadyr on 8/26/14.
 */
public class MahoutSetupStrategy implements ClusterSetupStrategy {

    private Environment environment;
    private MahoutConfig config;
    private ProductOperation productOperation;
    private MahoutImpl mahout;

    public MahoutSetupStrategy( final Environment environment, final MahoutConfig config, final ProductOperation po,
                                final MahoutImpl mahout ) {
        this.environment = environment;
        this.config = config;
        this.productOperation = po;
        this.mahout = mahout;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {
        return config;
    }
}