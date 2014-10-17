/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import java.util.UUID;


/**
 */
public class CheckDecommissionStatusTask implements Runnable {

    private final CompleteEvent completeEvent;
    private UUID trackID;
    private HadoopClusterConfig hadoopClusterConfig;
    private Hadoop hadoop;
    private Tracker tracker;



    public CheckDecommissionStatusTask( Hadoop hadoop, Tracker tracker, HadoopClusterConfig hadoopClusterConfig, CompleteEvent completeEvent,
                                        UUID trackID ) {
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.completeEvent = completeEvent;
        this.trackID = trackID;
        this.hadoopClusterConfig = hadoopClusterConfig;
    }


    public void run() {

        String operationLog = "";

        if (hadoopClusterConfig == null ) {
            completeEvent.onComplete( operationLog );
            return;
        }

        if ( trackID != null ) {
            while ( true ) {
                ProductOperationView prevPo =
                        tracker.getProductOperation(HadoopClusterConfig.PRODUCT_KEY, trackID);
                if ( prevPo.getState() == ProductOperationState.RUNNING ) {
                    try {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException ex ) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
        }

        NodeState state = NodeState.UNKNOWN;

        trackID = hadoop.statusNameNode(hadoopClusterConfig);

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() ) {
            ProductOperationView po =
                    tracker.getProductOperation(HadoopClusterConfig.PRODUCT_KEY, trackID);
            if ( po != null && po.getState() != ProductOperationState.RUNNING ) {
                if ( po.getLog().contains( NodeState.STOPPED.toString() ) ) {
                    state = NodeState.STOPPED;
                }
                else if ( po.getLog().contains( NodeState.RUNNING.toString() ) ) {
                    state = NodeState.RUNNING;
                }
                break;
            }

            try {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex ) {
                break;
            }
            if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 ) {
                break;
            }
        }

        if ( state.equals( NodeState.RUNNING ) ) {
            trackID = hadoop.checkDecomissionStatus(hadoopClusterConfig);
            start = System.currentTimeMillis();
            while ( !Thread.interrupted() ) {
                ProductOperationView po =
                        tracker.getProductOperation(HadoopClusterConfig.PRODUCT_KEY, trackID);
                if ( po != null && po.getState() != ProductOperationState.RUNNING ) {
                    operationLog = po.getLog();
                    break;
                }

                try {
                    Thread.sleep( 1000 );
                }
                catch ( InterruptedException ex ) {
                    break;
                }
                if ( System.currentTimeMillis() - start > ( 5 + 3 ) * 1000 ) {
                    break;
                }
            }
        }

        completeEvent.onComplete( operationLog );
    }
}