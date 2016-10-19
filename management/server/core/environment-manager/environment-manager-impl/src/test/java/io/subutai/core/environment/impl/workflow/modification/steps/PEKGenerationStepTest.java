package io.subutai.core.environment.impl.workflow.modification.steps;


import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class PEKGenerationStepTest
{

    PEKGenerationStep step;
    @Mock
    Topology topology;
    EnvironmentImpl environment = TestHelper.ENVIRONMENT();
    @Mock
    PeerManager peerManager;
    @Mock
    SecurityManager securityManager;
    @Mock
    KeyManager keyManager;
    @Mock
    PGPSecretKeyRing secretKeyRing;
    LocalPeer localPeer = TestHelper.LOCAL_PEER();
    @Mock
    PublicKeyContainer publicKeyContainer;
    @Mock
    PGPPublicKeyRing pgpPublicKeyRing;
    Peer peer = TestHelper.PEER();

    @Mock
    PeerUtil<Object> PEER_UTIL;
    @Mock
    PeerUtil.PeerTaskResults<Peer> peerTaskResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;


    @Before
    public void setUp() throws Exception
    {
        step = spy( new PEKGenerationStep( topology, environment, peerManager, securityManager,
                TestHelper.TRACKER_OPERATION() ) );
        step.pekUtil = PEER_UTIL;

        doReturn( secretKeyRing ).when( step ).getEnvironmentKeyRing();
        doReturn( localPeer ).when( peerManager ).getLocalPeer();
        doReturn( publicKeyContainer ).when( localPeer ).createPeerEnvironmentKeyPair( any( RelationLinkDto.class ) );
        doReturn( pgpPublicKeyRing ).when( step ).getLocalPeerPek();
        doReturn( keyManager ).when( securityManager ).getKeyManager();
        doReturn( Sets.newHashSet( peer, TestHelper.PEER() ) ).when( peerManager ).resolve( anySet() );
        TestHelper.bind( environment, peer, PEER_UTIL, peerTaskResults, peerTaskResult );
    }


    @Test( expected = PeerException.class )
    public void testExecute() throws Exception
    {
        doReturn( true ).when( peerTaskResult ).hasSucceeded();

        step.execute();

        verify( PEER_UTIL ).executeParallel();

        doReturn( false ).when( peerTaskResult ).hasSucceeded();
        doReturn( true ).when( peerTaskResults ).hasFailures();

        step.execute();
    }


    @Test
    public void testGetEnvironmentKeyRing() throws Exception
    {
        doCallRealMethod().when( step ).getEnvironmentKeyRing();

        step.getEnvironmentKeyRing();

        verify( securityManager ).getKeyManager();
        verify( keyManager ).getSecretKeyRing( TestHelper.ENV_ID );
    }


    @Test
    public void testGetLocalPeerPek() throws Exception
    {

        doCallRealMethod().when( step ).getLocalPeerPek();

        step.getLocalPeerPek();

        verify( keyManager ).getPublicKeyRing( anyString() );
    }
}
