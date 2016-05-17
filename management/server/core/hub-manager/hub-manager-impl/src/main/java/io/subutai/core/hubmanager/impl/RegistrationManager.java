package io.subutai.core.hubmanager.impl;


import java.io.IOException;

import javax.ws.rs.core.Form;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.hubmanager.impl.model.ConfigEntity;
import io.subutai.hub.share.dto.PeerInfoDto;
import io.subutai.hub.share.dto.RegistrationDto;
import io.subutai.hub.share.pgp.key.PGPKeyHelper;

import static java.lang.String.format;


public class RegistrationManager
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final HubManagerImpl hubManager;

    private final ConfigManager configManager;

    private final String hubIp;

    private final String peerId;

    private final HubRestClient restClient;


    public RegistrationManager( HubManagerImpl hubManager, ConfigManager configManager, String hupIp )
    {
        this.hubManager = hubManager;
        this.configManager = configManager;
        this.hubIp = hupIp;
        this.peerId = configManager.getPeerId();

        restClient = new HubRestClient( configManager );
    }


    public void registerPeer( String email, String password ) throws HubPluginException
    {
        registerPeerPubKey();

        register( email, password );
    }


    private String readKeyText( PGPPublicKey key ) throws HubPluginException
    {
        try
        {
            return PGPEncryptionUtil.armorByteArrayToString( key.getEncoded() );
        }
        catch ( PGPException | IOException e )
        {
            throw new HubPluginException( "Error to read PGP key as text", e );
        }
    }


    private void registerPeerPubKey() throws HubPluginException
    {
        log.info( "Registering peer public key to Hub..." );

        Form form = new Form( "keytext", readKeyText( configManager.getPeerPublicKey() ) );

        RestResult<Object> restResult = restClient.postPlain( "/pks/add", form );

        if ( !restResult.isSuccess() )
        {
            throw new HubPluginException( "Error to register peer public to Hub: " + restResult.getError() );
        }

        log.info( "Public key successfully registered" );
    }


    private RegistrationDto getRegistrationDto( String email, String password )
    {
        PeerInfoDto peerInfoDto = new PeerInfoDto();

        peerInfoDto.setId( configManager.getPeerId() );
        peerInfoDto.setVersion( String.valueOf( SubutaiInfo.getVersion() ) );
        peerInfoDto.setName( configManager.getPeerManager().getLocalPeer().getName() );

        RegistrationDto dto = new RegistrationDto( PGPKeyHelper.getFingerprint( configManager.getOwnerPublicKey() ) );

        dto.setOwnerEmail( email );
        dto.setOwnerPassword( password );
        dto.setPeerInfo( peerInfoDto );
        dto.setToken( configManager.getPermanentToken() );

        return dto;
    }


    private void register( String email, String password ) throws HubPluginException
    {
        log.info( "Registering peer to Hub..." );

        String path = format( "/rest/v1/peers/%s", peerId );

        RegistrationDto regDto = getRegistrationDto( email, password );

        RestResult<Object> restResult = restClient.post( path, regDto );

        if ( !restResult.isSuccess() )
        {
            throw new HubPluginException( "Error to register peer: " + restResult.getError() );
        }

        Config config = new ConfigEntity( regDto.getPeerInfo().getId(), hubIp, hubManager.getPeerInfo().get( "OwnerId" ), email );

        hubManager.getConfigDataService().saveHubConfig( config );

        log.info( "Peer registered successfully" );
    }


    public void unregister() throws HubPluginException
    {
        log.info( "Unregistering peer..." );

        String path = format( "/rest/v1/peers/%s/delete", peerId );

        RestResult<Object> restResult = restClient.delete( path );

        if ( restResult.getStatus() == HttpStatus.SC_FORBIDDEN )
        {
            log.info( "Peer or its pubic key not found on Hub. Unregistered anyway." );
        }
        else if ( !restResult.isSuccess() )
        {
            throw new HubPluginException( "Error to unregister peer: " + restResult.getError() );
        }

        hubManager.getConfigDataService().deleteConfig( configManager.getPeerId() );

        log.info( "Uregistered successfully" );
    }
}
