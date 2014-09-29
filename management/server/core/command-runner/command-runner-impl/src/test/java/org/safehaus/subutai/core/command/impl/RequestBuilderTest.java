package org.safehaus.subutai.core.command.impl;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;


public class RequestBuilderTest
{

    private static final UUID AGENT_ID = UUID.randomUUID();
    private static final UUID TASK_ID = UUID.randomUUID();
    private static final String COMMAND = "cmd";
    private static final String CWD = "/";
    private static final String RUN_AS = "user";
    private static final Integer TIMEOUT = 10;
    private static final Integer PID = 1234;
    private static final RequestType REQUEST_TYPE = RequestType.EXECUTE_REQUEST;
    private static final List<String> CMD_ARGS = Lists.newArrayList( "arg1", "arg2" );
    private static final Map<String, String> ENV_VARS =
            Maps.asMap( Sets.newHashSet( "var1", "var2" ), new Function<String, String>()
            {
                @Override
                public String apply( final String s )
                {
                    if ( s.equals( "var1" ) )
                    {
                        return "val1";
                    }
                    else
                    {
                        return "val2";
                    }
                }
            } );

    private static final String ERR_PATH = "err/path";
    private static final String STD_OUT_PATH = "out/path";
    private static final OutputRedirection STD_REDIRECTION = OutputRedirection.RETURN;
    private static final OutputRedirection ERR_REDIRECTION = OutputRedirection.RETURN;


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailEmptyOrNullCommand()
    {
        RequestBuilder requestBuilder = new RequestBuilder( null );
    }


    @Test
    public void shouldReturnSameProperties()
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( COMMAND ).withCwd( CWD ).withRunAs( RUN_AS ).withTimeout( TIMEOUT ).withPid( PID )
                                             .withType( REQUEST_TYPE ).withCmdArgs( CMD_ARGS ).withEnvVars( ENV_VARS )
                                             .withErrPath( ERR_PATH ).withStdOutPath( STD_OUT_PATH )
                                             .withStdErrRedirection( ERR_REDIRECTION )
                                             .withStdOutRedirection( STD_REDIRECTION );

        Request request = requestBuilder.build( AGENT_ID, TASK_ID );

        assertEquals( AGENT_ID, request.getUuid() );
        assertEquals( TASK_ID, request.getTaskUuid() );
        assertEquals( COMMAND, request.getProgram() );
        assertEquals( CWD, request.getWorkingDirectory() );
        assertEquals( RUN_AS, request.getRunAs() );
        assertEquals( TIMEOUT, requestBuilder.getTimeout() );
        assertEquals( TIMEOUT, request.getTimeout() );
        assertEquals( PID, request.getPid() );
        assertEquals( REQUEST_TYPE, request.getType() );
        assertEquals( CMD_ARGS, request.getArgs() );
        assertEquals( ENV_VARS, request.getEnvironment() );
        assertEquals( ERR_PATH, request.getStdErrPath() );
        assertEquals( STD_OUT_PATH, request.getStdOutPath() );
        assertEquals( ERR_REDIRECTION, request.getStdErr() );
        assertEquals( STD_REDIRECTION, request.getStdOut() );



    }
}