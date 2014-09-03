package org.safehaus.subutai.plugin.shark.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.shark.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.plugin.shark.impl.mock.SharkImplMock;

public class UninstallOperationHandlerTest {
    private SharkImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new SharkImplMock();
        handler = new UninstallOperationHandler(mock, "test-cluster");
    }

    @Test
    public void testWithoutCluster() {
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not exist" ) );
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}