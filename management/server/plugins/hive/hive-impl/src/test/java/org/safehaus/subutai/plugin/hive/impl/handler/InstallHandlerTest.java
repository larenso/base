package org.safehaus.subutai.plugin.hive.impl.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;
import org.safehaus.subutai.plugin.hive.impl.handler.mock.HiveImplMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;

public class InstallHandlerTest {

    private HiveImplMock mock = new HiveImplMock();
    private AbstractHandler handler;

    @Before
    public void setUp() {
        mock = new HiveImplMock();
    }

    @Test(expected = NullPointerException.class)
    public void testWithNullConfig() {
        handler = new InstallHandler(mock, null);
        handler.run();
    }

    @Test
    public void testWithExistingConfig() {
        HiveConfig config = new HiveConfig();
        config.setSetupType(SetupType.OVER_HADOOP);
        config.setClusterName("test-cluster");
        mock.setConfig(config);

        handler = new InstallHandler(mock, config);
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("exists"));
        Assert.assertTrue(po.getLog().contains(config.getClusterName()));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testWithoutServerNode() {
        HiveConfig config = new HiveConfig();
        config.setSetupType(SetupType.OVER_HADOOP);
        config.setClusterName("test-cluster");
        config.setServer(CommonMockBuilder.createAgent());

        handler = new InstallHandler(mock, config);
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

}