package org.safehaus.subutai.common.security;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiThreadContextTest
{
    private SubutaiThreadContext threadContext = new SubutaiThreadContext();

    @Mock
    SubutaiLoginContext context;

    @Test
    public void testSet() throws Exception
    {
        SubutaiThreadContext.set( context );
        SubutaiThreadContext.get();
        SubutaiThreadContext.unset();
    }
}