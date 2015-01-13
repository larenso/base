package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Listens to alerts from remote peers
 */
public class RemoteAlertListener extends RequestListener
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoteAlertListener.class.getName() );

    protected MonitorImpl monitor;


    protected RemoteAlertListener( MonitorImpl monitor )
    {
        super( RecipientType.ALERT_RECIPIENT.name() );
        this.monitor = monitor;
    }


    @Override
    public Object onRequest( final Payload payload )
    {
        ContainerHostMetricImpl containerHostMetric = payload.getMessage( ContainerHostMetricImpl.class );

        try
        {
            monitor.notifyOnAlert( containerHostMetric );
        }
        catch ( MonitorException e )
        {
            LOG.error( "Error in RemoteAlertListener.onMessage", e );
        }

        return null;
    }
}
