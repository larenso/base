package io.subutai.core.identity.impl;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.base.Strings;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.exception.IdentityExpiredException;
import io.subutai.common.security.exception.InvalidLoginException;
import io.subutai.common.security.exception.SystemSecurityException;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionOperation;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.security.objects.TokenType;
import io.subutai.common.security.objects.UserStatus;
import io.subutai.common.security.objects.UserType;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.RelationVerificationException;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.security.token.TokenUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.SecurityController;
import io.subutai.core.identity.api.SessionManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.identity.impl.dao.IdentityDataServiceImpl;
import io.subutai.core.identity.impl.model.PermissionEntity;
import io.subutai.core.identity.impl.model.RoleEntity;
import io.subutai.core.identity.impl.model.SessionEntity;
import io.subutai.core.identity.impl.model.UserDelegateEntity;
import io.subutai.core.identity.impl.model.UserEntity;
import io.subutai.core.identity.impl.model.UserTokenEntity;
import io.subutai.core.identity.impl.utils.SecurityUtil;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.model.SecurityKey;


/**
 * Overall Subutai Identity Management
 */
@PermitAll
public class IdentityManagerImpl implements IdentityManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( IdentityManagerImpl.class.getName() );

    private static final String SYSTEM_USERNAME = "internal";
    private static final int IDENTITY_LIFETIME = 240; //days

    private IdentityDataService identityDataService = null;
    private SecurityController securityController = null;
    private DaoManager daoManager = null;
    private SecurityManager securityManager = null;
    private SessionManager sessionManager = null;


    /* *************************************************
     */
    public IdentityManagerImpl()
    {

    }


    //*****************************************
    public void init()
    {
        identityDataService = new IdentityDataServiceImpl( daoManager );
        sessionManager = new SessionManagerImpl( identityDataService );
        securityController = new SecurityControllerImpl();
        sessionManager.startSessionController();

        try
        {
            createDefaultUsers();
        }
        catch ( Exception e )
        {
            LOGGER.error( "***** Error! Error creating users:" + e.toString(), e );
        }
    }


    //*****************************************
    public void destroy()
    {
        sessionManager.stopSessionController();
    }


    //*****************************************************
    private void createDefaultUsers() throws Exception
    {
        if ( identityDataService.getAllUsers().size() < 1 )
        {
            PermissionObject permsp[] = PermissionObject.values();
            Role role;
            Permission per;

            //***Create User ********************************************

            User internal =
                    createUser( SYSTEM_USERNAME, "", "System User", "internal@subutai.io", UserType.System.getId(), 3,
                            false, false );
            //User karaf = createUser( "karaf", "secret", "Karaf Manager", "karaf@subutai.io", 1, 3, false, false );
            User admin =
                    createUser( "admin", "secret", "Administrator", "admin@subutai.io", UserType.Regular.getId(), 3,
                            true, true );
            //***********************************************************

            //***Create Token *******************************************
            createUserToken( internal, "", "", "", TokenType.Permanent.getId(), null );
            //***********************************************************

            //****Create Roles ******************************************
            role = createRole( "Karaf-Manager", UserType.System.getId() );
            //assignUserRole( karaf, role );
            assignUserRole( admin, role );

            per = createPermission( PermissionObject.KarafServerAdministration.getId(), 1, true, true, true, true );
            assignRolePermission( role, per );
            //*********************************************

            //*********************************************
            role = createRole( "Administrator", UserType.Regular.getId() );
            assignUserRole( admin.getId(), role );

            for ( final PermissionObject aPermsp : permsp )
            {
                per = createPermission( aPermsp.getId(), 1, true, true, true, true );
                assignRolePermission( role, per );
            }
            //*********************************************

            //*********************************************
            role = createRole( "Peer-Manager", UserType.System.getId() );

            //*********************************************
            for ( final PermissionObject aPermsp : permsp )
            {
                if ( aPermsp == PermissionObject.PeerManagement || aPermsp == PermissionObject.ResourceManagement )
                {
                    per = createPermission( aPermsp.getId(), 3, true, true, true, true );
                    assignRolePermission( role, per );
                }
            }
            //*********************************************

            //*********************************************
            role = createRole( "Environment-Manager", UserType.System.getId() );

            //*********************************************
            for ( final PermissionObject aPermsp : permsp )
            {
                if ( aPermsp != PermissionObject.IdentityManagement &&
                        aPermsp != PermissionObject.KarafServerAdministration &&
                        aPermsp != PermissionObject.PeerManagement &&
                        aPermsp != PermissionObject.ResourceManagement )
                {
                    per = createPermission( aPermsp.getId(), 3, true, true, true, true );
                    assignRolePermission( role, per );
                }
            }
            //*********************************************

            //*********************************************
            role = createRole( "Internal-System", UserType.System.getId() );
            assignUserRole( internal, role );

            //*********************************************
            for ( final PermissionObject aPermsp : permsp )
            {
                if ( aPermsp != PermissionObject.IdentityManagement
                        && aPermsp != PermissionObject.KarafServerAdministration )
                {
                    per = createPermission( aPermsp.getId(), 1, true, true, true, true );
                    assignRolePermission( role, per );
                }
            }

            //***** setPeer Owner By Default ***************
            setPeerOwner( admin );
            //**********************************************
        }
        else
        {
            User admin = identityDataService.getUserByUsername( "admin" );
            //***** setPeer Owner By Default ***************
            setPeerOwner( admin );
            //**********************************************
        }
    }


    /* *************************************************
     */
    private CallbackHandler getCalbackHandler( final String userName, final String password )
    {

        return new CallbackHandler()
        {
            public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException
            {
                for ( Callback callback : callbacks )
                {
                    if ( callback instanceof NameCallback )
                    {
                        ( ( NameCallback ) callback ).setName( userName );
                    }
                    else if ( callback instanceof PasswordCallback )
                    {
                        ( ( PasswordCallback ) callback ).setPassword( password.toCharArray() );
                    }
                    else
                    {
                        throw new UnsupportedCallbackException( callback );
                    }
                }
            }
        };
    }


    /* ***********************************
     *  Authenticate Internal User
     */
    @PermitAll
    @Override
    public Session loginSystemUser()
    {
        String sptoken = getSystemUserToken();
        Session session = login( "token", sptoken );

        if ( session != null )
        {
            return session;
        }

        else
        {
            return null;
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public Session login( String userName, String password )
    {
        try
        {
            Session userSession = null;

            CallbackHandler ch = getCalbackHandler( userName, password );
            Subject subject = new Subject();
            LoginContext loginContext = new LoginContext( "karaf", subject, ch );
            loginContext.login();

            while ( subject.getPrivateCredentials().iterator().hasNext() )
            {
                Object obj = subject.getPrivateCredentials().iterator().next();

                if ( obj instanceof SessionEntity )
                {
                    userSession = ( Session ) obj;
                    userSession.setSubject( subject );
                    break;
                }
            }

            return userSession;
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public SessionManager getSessionManager()
    {
        return sessionManager;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public Session authenticateSession( String login, String password )
    {
        String sessionId;
        Session session;
        User user = null;

        //-------------------------------------
        if ( login.equals( "token" ) )
        {
            sessionId = password;
        }
        else
        {
            sessionId = UUID.randomUUID() + "-" + System.currentTimeMillis();
        }
        //-------------------------------------

        session = sessionManager.getValidSession( sessionId );

        if ( session == null )
        {
            user = authenticateUser( login, password );

            if ( user == null )
            {
                return null;
            }
        }

        session = sessionManager.startSession( sessionId, session, user );

        return session;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public UserToken createUserToken( User user, String token, String secret, String issuer, int tokenType,
                                      Date validDate )
    {
        try
        {
            UserToken userToken = new UserTokenEntity();

            if ( Strings.isNullOrEmpty( token ) )
            {
                token = UUID.randomUUID().toString();
            }
            if ( Strings.isNullOrEmpty( issuer ) )
            {
                issuer = "io.subutai";
            }
            if ( Strings.isNullOrEmpty( secret ) )
            {
                secret = UUID.randomUUID().toString();
            }
            if ( validDate == null )
            {
                validDate = DateUtils
                        .addMinutes( new Date( System.currentTimeMillis() ), sessionManager.getSessionTimeout() );
            }

            userToken.setTokenId( token );
            userToken.setHashAlgorithm( "HS256" );
            userToken.setIssuer( issuer );
            userToken.setSecret( secret );
            userToken.setUserId( user.getId() );
            userToken.setType( tokenType );
            userToken.setValidDate( validDate );

            identityDataService.persistUserToken( userToken );

            return userToken;
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void logout()
    {
        //reserved for future
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public String getUserToken( String userName, String password )
    {
        String token = "";

        User user = authenticateUser( userName, password );

        if ( user != null )
        {
            UserToken userToken = getUserToken( user.getId() );

            if ( userToken == null )
            {
                userToken = createUserToken( user, "", "", "", TokenType.Session.getId(), null );
            }
            else
            {
                if ( userToken.getType() == TokenType.Session.getId() )
                {
                    removeUserToken( userToken.getTokenId() );

                    userToken = createUserToken( user, "", "", "", TokenType.Session.getId(), null );
                }
            }

            token = userToken.getFullToken();
        }

        return token;
    }


    /* *************************************************
     */
    public UserToken getUserToken( long userId )
    {
        return identityDataService.getUserToken( userId );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public String getSystemUserToken()
    {
        User user = identityDataService.getUserByUsername( SYSTEM_USERNAME );

        UserToken userToken = getUserToken( user.getId() );

        return userToken != null ? userToken.getFullToken() : null;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public String updateUserAuthId( User user, String authId ) throws SystemSecurityException
    {
        if ( user != null )
        {
            if ( Strings.isNullOrEmpty( authId ) )
            {
                authId = UUID.randomUUID().toString();
            }

            if ( authId.length() < 4 )
            {
                throw new IllegalArgumentException( "Password cannot be shorter than 4 characters" );
            }

            if ( user.getAuthId().equals( authId ) )
            {
                throw new IllegalArgumentException( "NewPassword cannot be the same as old one." );
            }


            user.setAuthId( authId );
            user.setValidDate( DateUtils.addDays( new Date( System.currentTimeMillis() ), IDENTITY_LIFETIME ) );
            identityDataService.updateUser( user );

            return authId;
        }

        return "";
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public String getEncryptedUserAuthId( User user ) throws SystemSecurityException
    {
        try
        {
            if ( user != null )
            {
                String authId = user.getAuthId();
                PGPPublicKey pubKey = securityManager.getKeyManager().getPublicKey( user.getSecurityKeyId() );

                if ( pubKey != null )
                {
                    byte enc[] = securityManager.getEncryptionTool().encrypt( authId.getBytes(), pubKey, true );

                    return new String( enc );
                }
                else
                {
                    throw new InvalidLoginException( "User Public Key not found." );
                }
            }
            else
            {
                throw new InvalidLoginException( "User not found." );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( " **** Error creating encrypted userAuth message ****", e );
        }

        return "";
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User authenticateByAuthSignature( final String fingerprint, final String signedAuth )
            throws SystemSecurityException
    {
        KeyManager keyManager = securityManager.getKeyManager();
        EncryptionTool encryptionTool = securityManager.getEncryptionTool();

        PGPPublicKeyRing publicKeyRing = keyManager.getPublicKeyRingByFingerprint( fingerprint.toUpperCase() );

        try
        {
            if ( Strings.isNullOrEmpty( signedAuth ) || !encryptionTool
                    .verifyClearSign( signedAuth.trim().getBytes(), publicKeyRing ) )
            {
                throw new InvalidLoginException( "Signed Auth verification failed." );
            }

            User user = getUserByFingerprint( fingerprint );

            if ( user == null )
            {
                throw new InvalidLoginException( "User not found associated with fingerprint: " + fingerprint );
            }

            String authId = new String( encryptionTool.extractClearSignContent( signedAuth.getBytes() ) );

            if ( !user.isIdentityValid() || !user.getAuthId().equals( authId.trim() ) )
            {
                throw new IdentityExpiredException( "User Credentials are expired" );
            }

            return user;
        }
        catch ( Exception e )
        {
            LOGGER.error( " **** Error authenticating user by signed Message ****", e );
            throw new SystemSecurityException( e.getMessage() );
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User authenticateByToken( String token ) throws SystemSecurityException
    {
        String subject = TokenUtil.getSubject( token );

        UserToken userToken = identityDataService.getValidUserToken( subject );

        if ( userToken != null )
        {
            if ( TokenType.Permanent.getId() == userToken.getType() && TokenUtil
                    .verifySignature( token, userToken.getSecret() ) )
            {
                return getUser( userToken.getUserId() );
            }
            else if ( TokenType.Session.getId() == userToken.getType() && TokenUtil
                    .verifySignatureAndDate( token, userToken.getSecret() ) )
            {
                return getUser( userToken.getUserId() );
            }
        }
        else
        {
            throw new InvalidLoginException();
        }

        return null;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User authenticateUser( String userName, String password ) throws SystemSecurityException
    {
        User user;

        if ( userName.equalsIgnoreCase( "token" ) )
        {
            user = authenticateByToken( password );
        }
        else if ( userName.length() == 40 )
        {
            user = authenticateByAuthSignature( userName, password.trim() );
        }
        else
        {
            user = identityDataService.getUserByUsername( userName );

            if ( user != null && ( user.getTrustLevel() > 1 ) )
            {
                String pswHash = SecurityUtil.generateSecurePassword( password, user.getSalt() );

                if ( !pswHash.equals( user.getPassword() ) || user.getStatus() == UserStatus.Disabled.getId() )
                {
                    throw new InvalidLoginException( "***** Invalid Login for user:" + userName );
                }
                else
                {
                    if ( !user.isIdentityValid() )
                    {
                        throw new IdentityExpiredException( "***** Identity Expired for user:" + userName );
                    }
                }
            }
            else
            {
                return null;
            }
        }

        return user;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void setPeerOwner( User user )
    {
        securityManager.getKeyManager().setPeerOwnerId( user.getSecurityKeyId() );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public String getPeerOwnerId()
    {
        return securityManager.getKeyManager().getPeerOwnerId();
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getUserByKeyId( String keyId )
    {
        return identityDataService.getUserByKeyId( keyId );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getUserByFingerprint( String fingerprint )
    {
        try
        {
            SecurityKey key = securityManager.getKeyManager().getKeyDataByFingerprint( fingerprint );

            if ( key != null )
            {
                return identityDataService.getUserByKeyId( key.getIdentityId() );
            }
            else
            {
                LOGGER.info( "******* User not found with fingerprint:" + fingerprint );
                return null;
            }
        }
        catch ( Exception ex )
        {
            LOGGER.error( "******* Error !! Error getting User by fingerprint", ex );
            return null;
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public UserDelegate getUserDelegate( long userId )
    {
        return identityDataService.getUserDelegateByUserId( userId );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public UserDelegate getUserDelegate( User user )
    {
        if ( user == null )
        {
            return null;
        }
        else
        {
            return identityDataService.getUserDelegateByUserId( user.getId() );
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public UserDelegate getUserDelegate( String id )
    {
        return identityDataService.getUserDelegate( id );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void setUserPublicKey( long userId, String publicKeyASCII )
    {

        User user = identityDataService.getUser( userId );

        if ( user != null )
        {
            String secId = user.getSecurityKeyId();

            if ( Strings.isNullOrEmpty( secId ) )
            {
                secId = userId + "-" + UUID.randomUUID();
                user.setSecurityKeyId( secId );
            }
            publicKeyASCII = publicKeyASCII.trim();
            securityManager.getKeyManager().savePublicKeyRing( secId, 1, publicKeyASCII );
            user.setFingerprint( securityManager.getKeyManager().getFingerprint( secId ) );
            identityDataService.updateUser( user );
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public List<User> getAllUsers()
    {
        List<User> result = new ArrayList<>();
        result.addAll( identityDataService.getAllUsers() );
        return result;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void assignUserRole( long userId, Role role )
    {
        identityDataService.assignUserRole( userId, role );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void assignUserRole( User user, Role role )
    {
        identityDataService.assignUserRole( user, role );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getUser( long userId )
    {
        return identityDataService.getUser( userId );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getSystemUser()
    {
        return getUserByUsername( SYSTEM_USERNAME );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getActiveUser()
    {
        Session session = getActiveSession();

        if ( session != null )
        {
            return session.getUser();
        }
        else
        {
            return null;
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public Session getActiveSession()
    {
        Session session = null;
        try
        {
            Subject subject = getActiveSubject();

            if ( subject != null )
            {
                while ( subject.getPrivateCredentials().iterator().hasNext() )
                {
                    Object obj = subject.getPrivateCredentials().iterator().next();

                    if ( obj instanceof SessionEntity )
                    {
                        session = ( ( Session ) obj );
                        break;
                    }
                }
            }
        }
        catch ( Exception ex )
        {
            LOGGER.error( "*** Error! Cannot find active User. Session is not started" );
        }

        return session;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void runAs( Session userSession, final Callable action )
    {
        if ( userSession != null )
        {
            Subject.doAs( userSession.getSubject(), new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    try
                    {
                        action.call();
                    }
                    catch ( Exception ex )
                    {
                        LOGGER.error( "**** Error!! Error running privileged action.", ex );
                    }
                    return null;
                }
            } );
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void runAs( Session userSession, final Runnable action )
    {
        if ( userSession != null )
        {
            Subject.doAs( userSession.getSubject(), new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    try
                    {
                        action.run();
                    }
                    catch ( Exception ex )
                    {
                        LOGGER.error( "**** Error!! Error running privileged action.", ex );
                    }
                    return null;
                }
            } );
        }
    }


    /* *************************************************
     */
    private Subject getActiveSubject()
    {

        Subject subject = null;

        try
        {
            AccessControlContext acc = AccessController.getContext();

            if ( acc == null )
            {
                throw new RuntimeException( "access control context is null" );
            }

            subject = Subject.getSubject( acc );

            if ( subject == null )
            {
                throw new RuntimeException( "subject is null" );
            }
        }
        catch ( Exception ex )
        {
            LOGGER.error( "*** Error! Cannot get auth.subject." );
        }

        return subject;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public User createTempUser( String userName, String password, String fullName, String email, int type )
    {
        String salt;
        User user = null;

        try
        {
            //***************Cannot use TOKEN keyword *******
            if ( userName.equalsIgnoreCase( "token" ) )
            {
                throw new IllegalArgumentException( "Cannot use TOKEN keyword." );
            }
            //***********************************************

            if ( Strings.isNullOrEmpty( password ) )
            {
                password = Integer.toString( ( new Random() ).nextInt() );
            }

            salt = SecurityUtil.generateSecureRandom();
            password = SecurityUtil.generateSecurePassword( password, salt );

            user = new UserEntity();
            user.setUserName( userName );
            user.setPassword( password );
            user.setSalt( salt );
            user.setEmail( email );
            user.setFullName( fullName );
            user.setType( type );
            user.setAuthId( UUID.randomUUID().toString() );
        }
        catch ( NoSuchAlgorithmException | NoSuchProviderException e )
        {
            LOGGER.warn( "Error in #createTempUser", e );
        }

        return user;
    }


    //**************************************************************
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void setTrustLevel( User user, int trustLevel )
    {
        //TODO Check Public Key for Trust

        user.setTrustLevel( trustLevel );

        identityDataService.updateUser( user );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public UserDelegate createUserDelegate( User user, String delegateUserId, boolean genKeyPair )
    {
        String id = "";

        if ( Strings.isNullOrEmpty( delegateUserId ) )
        {
            id = user.getId() + "-" + UUID.randomUUID();
        }

        UserDelegate userDelegate = new UserDelegateEntity();
        userDelegate.setId( id );
        userDelegate.setUserId( user.getId() );
        identityDataService.persistUserDelegate( userDelegate );


        if ( genKeyPair )
        {
            generateKeyPair( id, SecurityKeyType.UserKey.getId() );
        }

        return userDelegate;
    }


    /* *************************************************
     */
    @Override
    public void approveDelegatedUser( final String trustMessage )
    {
        try
        {
            RelationManager relationManager = ServiceLocator.getServiceNoCache( RelationManager.class );
            if ( relationManager != null )
            {
                User activeUser = getActiveUser();
                UserDelegate delegatedUser = getUserDelegate( activeUser.getId() );
                relationManager.processTrustMessage( trustMessage, delegatedUser.getId() );
            }
        }
        catch ( RelationVerificationException e )
        {
            LOGGER.error( "Message verification failed", e );
        }
    }


    /* *************************************************
     */
    @Override
    public void createIdentityDelegationDocument()
    {
        try
        {
            RelationManager relationManager = ServiceLocator.getServiceNoCache( RelationManager.class );
            KeyManager keyManager = securityManager.getKeyManager();
            EncryptionTool encryptionTool = securityManager.getEncryptionTool();

            User activeUser = getActiveUser();
            UserDelegate delegatedUser = getUserDelegate( activeUser.getId() );
            if ( delegatedUser == null )
            {
                delegatedUser = createUserDelegate( activeUser, null, true );
            }

            if ( keyManager.getPrivateKey( delegatedUser.getId() ) == null )
            {
                generateKeyPair( delegatedUser.getId(), SecurityKeyType.UserKey.getId() );
            }

            assert relationManager != null;
            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );

            Map<String, String> traits = relationInfoMeta.getRelationTraits();
            traits.put( "read", "true" );
            traits.put( "write", "true" );
            traits.put( "update", "true" );
            traits.put( "delete", "true" );
            traits.put( "ownership", Ownership.USER.getName() );

            RelationMeta relationMeta =
                    new RelationMeta( activeUser, delegatedUser, delegatedUser, activeUser.getSecurityKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );

            String relationJson = JsonUtil.toJson( relation );

            PGPPublicKey publicKey = keyManager.getPublicKey( delegatedUser.getId() );
            byte[] relationEncrypted = encryptionTool.encrypt( relationJson.getBytes(), publicKey, true );

            String encryptedMessage = "\n" + new String( relationEncrypted, "UTF-8" );
            delegatedUser.setRelationDocument( encryptedMessage );
            identityDataService.updateUserDelegate( delegatedUser );
            LOGGER.debug( encryptedMessage );
            LOGGER.debug( delegatedUser.getId() );
        }
        catch ( UnsupportedEncodingException e )
        {
            LOGGER.error( "Error decoding byte array", e );
        }
    }


    /* *************************************************
     */
    private void generateKeyPair( String securityKeyId, int type )
    {
        KeyPair kp = securityManager.getKeyManager().generateKeyPair( securityKeyId, false );
        securityManager.getKeyManager().saveKeyPair( securityKeyId, type, kp );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public User createUser( String userName, String password, String fullName, String email, int type, int trustLevel,
                            boolean generateKeyPair, boolean createUserDelegate ) throws Exception
    {
        User user = new UserEntity();
        String salt = "";

        //*********************************
        if ( Strings.isNullOrEmpty( userName ) )
        {
            userName = UUID.randomUUID().toString();
        }
        if ( Strings.isNullOrEmpty( password ) )
        {
            password = UUID.randomUUID().toString();
        }
        //*********************************

        isValidUserName( userName );
        isValidPassword( userName, password );

        try
        {
            salt = SecurityUtil.generateSecureRandom();
            password = SecurityUtil.generateSecurePassword( password, salt );


            user.setUserName( userName );
            user.setPassword( password );
            user.setSalt( salt );
            user.setEmail( email );
            user.setFullName( fullName );
            user.setType( type );
            user.setTrustLevel( trustLevel );
            user.setAuthId( userName );
            user.setValidDate( new Date() );

            identityDataService.persistUser( user );

            //***************************************
            if ( generateKeyPair )
            {
                String securityKeyId = user.getId() + "-" + UUID.randomUUID();
                generateKeyPair( securityKeyId, 1 );
                user.setSecurityKeyId( securityKeyId );
                identityDataService.updateUser( user );
            }
            //***************************************

            //***************************************
            if ( createUserDelegate )
            {
                createUserDelegate( user, null, true );
            }
        }
        catch ( Exception e )
        {
            throw new Exception( "Internal error", e );
        }

        return user;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getUserByUsername( String userName )
    {
        return identityDataService.getUserByUsername( userName );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public User modifyUser( User user, String password ) throws Exception
    {
        //******Cannot update Internal User *************
        if ( user.getType() == UserType.System.getId() )
        {
            throw new AccessControlException( "Internal User cannot be updated" );
        }

        try
        {

            if ( !Strings.isNullOrEmpty( password ) )
            {
                isValidPassword( user.getUserName(), password );

                String salt = user.getSalt();
                password = SecurityUtil.generateSecurePassword( password, salt );

                user.setPassword( password );
            }

            identityDataService.updateUser( user );
        }
        catch ( IllegalArgumentException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            LOGGER.error( "modify user exception", e );
            throw new Exception( "Internal error" );
        }

        return user;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removeUserRole( long userId, Role role )
    {
        identityDataService.removeUserRole( userId, role );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removeUserRole( User user, Role role )
    {
        identityDataService.removeUserRole( user, role );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean changeUserPassword( String userName, String oldPassword, String newPassword ) throws Exception
    {
        User user = identityDataService.getUserByUsername( userName );
        return changeUserPassword( user, oldPassword, newPassword );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean changeUserPassword( long userId, String oldPassword, String newPassword ) throws Exception
    {
        User user = identityDataService.getUser( userId );
        return changeUserPassword( user, oldPassword, newPassword );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean changeUserPassword( User user, String oldPassword, String newPassword ) throws Exception
    {
        String salt;

        if ( oldPassword.equals( newPassword ) )
        {
            throw new IllegalArgumentException( "NewPassword cannot be the same as old one." );
        }

        //******Cannot update Internal User *************
        if ( user.getType() == UserType.System.getId() )
        {
            throw new AccessControlException( "Internal User cannot be updated" );
        }

        String pswHash = SecurityUtil.generateSecurePassword( oldPassword, user.getSalt() );

        if ( !pswHash.equals( user.getPassword() ) )
        {
            throw new InvalidLoginException( "Invalid old password specified" );
        }

        isValidPassword( user.getUserName(), newPassword );

        try
        {
            salt = SecurityUtil.generateSecureRandom();
            newPassword = SecurityUtil.generateSecurePassword( newPassword, salt );
            user.setSalt( salt );
            user.setPassword( newPassword );
            //user.setAuthId( UUID.randomUUID().toString() ); //Update AuthID also
            user.setValidDate( DateUtils.addDays( new Date( System.currentTimeMillis() ), IDENTITY_LIFETIME ) );
            identityDataService.updateUser( user );
        }
        catch ( NoSuchAlgorithmException | NoSuchProviderException e )
        {
            throw new Exception( "Internal error" );
        }

        return true;
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void updateUser( User user )
    {
        //******Cannot update Internal User *************
        if ( user.getType() == UserType.System.getId() )
        {
            throw new AccessControlException( "Internal User cannot be updated" );
        }
        //***********************************************

        identityDataService.updateUser( user );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void updateUser( User user, String publicKey )
    {
        //******Cannot update Internal User *************
        if ( user.getType() == UserType.System.getId() )
        {
            throw new AccessControlException( "Internal User cannot be updated" );
        }
        //***********************************************

        identityDataService.updateUser( user );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Delete" } )
    @Override
    public void removeUser( long userId )
    {
        //******Cannot remove Internal User *************
        User user = identityDataService.getUser( userId );
        if ( user.getType() == UserType.System.getId() )
        {
            throw new AccessControlException( "Internal User cannot be removed" );
        }
        //***********************************************

        identityDataService.removeUser( userId );
    }


    /* *************************************************
     */
    private void isValidUserName( String userName )
    {
        if ( Strings.isNullOrEmpty( userName ) || userName.length() < 4 )
        {
            throw new IllegalArgumentException( "User name cannot be shorter than 4 characters." );
        }

        if ( userName.equalsIgnoreCase( "token" ) ||
                userName.equalsIgnoreCase( "administrator" ) ||
                userName.equalsIgnoreCase( "authmessage" ) ||
                userName.equalsIgnoreCase( "system" ) )
        {
            throw new IllegalArgumentException( "User name is reserved by the system." );
        }
    }


    /* *************************************************
     */
    private void isValidPassword( String userName, String password )
    {
        if ( Strings.isNullOrEmpty( password ) || password.length() < 4 )
        {
            throw new IllegalArgumentException( "Password cannot be shorter than 4 characters" );
        }

        if ( password.equalsIgnoreCase( userName ) ||
                password.equalsIgnoreCase( "password" ) ||
                password.equalsIgnoreCase( "system" ) )
        {
            throw new IllegalArgumentException( "Password doesn't match security measures" );
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean isUserPermitted( User user, PermissionObject permObj, PermissionScope permScope,
                                    PermissionOperation permOp )
    {

        List<Role> roles = user.getRoles();

        for ( Role role : roles )
        {
            for ( Permission permission : role.getPermissions() )
            {
                if ( permission.getObject() == permObj.getId() && permission.getScope() == permScope.getId() )
                {
                    switch ( permOp )
                    {
                        case Read:
                            return permission.isRead();
                        case Write:
                            return permission.isWrite();
                        case Update:
                            return permission.isUpdate();
                        case Delete:
                            return permission.isDelete();
                    }
                }
            }
        }

        return false;
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public Role createRole( String roleName, int roleType )
    {
        Role role = new RoleEntity();
        role.setName( roleName );
        role.setType( roleType );

        identityDataService.persistRole( role );

        return role;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public List<Role> getAllRoles()
    {
        return identityDataService.getAllRoles();
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public Role getRole( long roleId )
    {
        return identityDataService.getRole( roleId );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void updateRole( Role role )
    {
        //******Cannot update Internal Role *************
        if ( role.getType() == UserType.System.getId() )
        {
            throw new AccessControlException( "Internal Role cannot be updated" );
        }
        //***********************************************

        identityDataService.updateRole( role );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removeRole( long roleId )
    {
        //******Cannot remove Internal Role *************
        Role role = identityDataService.getRole( roleId );

        if ( role.getType() == UserType.System.getId() )
        {
            throw new AccessControlException( "Internal Role cannot be removed" );
        }
        //***********************************************

        identityDataService.removeRole( roleId );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public Permission createPermission( int objectId, int scope, boolean read, boolean write, boolean update,
                                        boolean delete )
    {
        Permission permission = new PermissionEntity();
        permission.setObject( objectId );
        permission.setScope( scope );
        permission.setRead( read );
        permission.setWrite( write );
        permission.setUpdate( update );
        permission.setDelete( delete );

        identityDataService.persistPermission( permission );

        return permission;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void assignRolePermission( long roleId, Permission permission )
    {
        identityDataService.assignRolePermission( roleId, permission );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void assignRolePermission( Role role, Permission permission )
    {
        identityDataService.assignRolePermission( role, permission );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void removeAllRolePermissions( long roleId )
    {
        identityDataService.removeAllRolePermissions( roleId );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removePermission( long permissionId )
    {
        identityDataService.removePermission( permissionId );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removeRolePermission( long roleId, Permission permission )
    {
        identityDataService.removeRolePermission( roleId, permission );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public List<Permission> getAllPermissions()
    {
        return identityDataService.getAllPermissions();
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Update" )
    @Override
    public void updatePermission( Permission permission )
    {
        identityDataService.updatePermission( permission );
    }


    /* *************************************************
     */
    @Override
    public IdentityDataService getIdentityDataService()
    {
        return identityDataService;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public List<UserToken> getAllUserTokens()
    {
        return identityDataService.getAllUserTokens();
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void extendTokenTime( UserToken token, int minutes )
    {
        token.setValidDate( DateUtils.addMinutes( new Date( System.currentTimeMillis() ), minutes ) );
        identityDataService.updateUserToken( token );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void updateUserToken( UserToken token )
    {
        identityDataService.updateUserToken( token );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void updateUserToken( String oldName, User user, String token, String secret, String issuer, int tokenType,
                                 Date validDate )
    {
        identityDataService.removeUserToken( oldName );
        createUserToken( user, token, secret, issuer, tokenType, validDate );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Delete" } )
    @Override
    public void removeUserToken( String tokenId )
    {
        identityDataService.removeUserToken( tokenId );
    }


    /* *************************************************
     */
    public void setDaoManager( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     */
    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    /* *************************************************
     */
    @Override
    public SecurityController getSecurityController()
    {
        return securityController;
    }
}
