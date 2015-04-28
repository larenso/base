package org.safehaus.subutai.wol.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.RequestBuilder;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class CommandsTest
{
    Commands commands;
    RequestBuilder requestBuilder;
    @Before
    public void setUp() throws Exception
    {
        commands = new Commands();
        requestBuilder = mock(RequestBuilder.class);
    }

    @Test
    public void testGetSendWakeOnLanCommand() throws Exception
    {
        commands.getSendWakeOnLanCommand("test");

        assertNotNull(commands.getSendWakeOnLanCommand("test"));

    }
}