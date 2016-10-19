package io.subutai.core.channel.impl.interceptor;


import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Strings;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.channel.impl.util.MessageContentUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;


/**
 * CXF interceptor that controls channel (tunnel)
 */
public class AccessControlInterceptor extends AbstractPhaseInterceptor<Message>
{
    private final IdentityManager identityManager;


    public AccessControlInterceptor( IdentityManager identityManager )
    {
        super( Phase.RECEIVE );
        this.identityManager = identityManager;
    }


    /**
     * Intercepts a message. Interceptors should NOT invoke handleMessage or handleFault on the next interceptor - the
     * interceptor chain will take care of this.
     */
    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            if ( InterceptorState.SERVER_IN.isActive( message ) )
            {
                HttpServletRequest req = ( HttpServletRequest ) message.get( AbstractHTTPDestination.HTTP_REQUEST );
                Session userSession;

                if ( req.getLocalPort() == Common.DEFAULT_PUBLIC_SECURE_PORT )
                {
                    // auth with system user since bi-SSL port is already secured
                    userSession = authenticateAccess( null, null );
                }
                else
                {

                    if ( ChannelSettings.checkURLAccess( req.getRequestURI() ) )
                    {
                        // auth with system user b/c this is a public endpoint
                        userSession = authenticateAccess( null, null );
                    }
                    else
                    {
                        //require token auth
                        userSession = authenticateAccess( message, req );
                    }
                }

                //******Authenticate************************************************
                if ( userSession != null )
                {
                    Subject.doAs( userSession.getSubject(), new PrivilegedAction<Void>()
                    {
                        @Override
                        public Void run()
                        {
                            try
                            {
                                message.getInterceptorChain().doIntercept( message );
                            }
                            catch ( Exception ex )
                            {
                                Throwable t = ExceptionUtils.getRootCause( ex );
                                MessageContentUtil.abortChain( message, t );
                            }
                            return null;
                        }
                    } );
                }
                else
                {
                    MessageContentUtil.abortChain( message, 401, "User is not authorized" );
                }
            }
            //-----------------------------------------------------------------------------------------------
        }
        catch ( Exception e )
        {
            throw new Fault( e );
        }
    }


    //******************************************************************
    protected Session authenticateAccess( Message message, HttpServletRequest req )
    {
        String sptoken = "";

        if ( message == null )
        {
            //***********internal auth ********* for registration , 8444 port and 8443 open REST endpoints
            return identityManager.loginSystemUser();
        }
        else
        {
            sptoken = req.getParameter( "sptoken" );

            if ( Strings.isNullOrEmpty( sptoken ) )
            {
                HttpHeaders headers = new HttpHeadersImpl( message.getExchange().getInMessage() );
                sptoken = headers.getHeaderString( "sptoken" );
            }

            //******************Get sptoken from cookies *****************

            if ( Strings.isNullOrEmpty( sptoken ) )
            {
                Cookie[] cookies = req.getCookies();
                for ( final Cookie cookie : cookies )
                {
                    if ( "sptoken".equals( cookie.getName() ) )
                    {
                        sptoken = cookie.getValue();
                    }
                }
            }

            if ( Strings.isNullOrEmpty( sptoken ) )
            {
                return null;
            }
            else
            {
                return identityManager.login( "token", sptoken );
            }
        }
    }
    //******************************************************************
}