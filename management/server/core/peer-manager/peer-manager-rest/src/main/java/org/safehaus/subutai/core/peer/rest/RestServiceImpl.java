package org.safehaus.subutai.core.peer.rest;


import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger LOG = Logger.getLogger(RestServiceImpl.class.getName());
    private PeerManager peerManager;


    public RestServiceImpl() {
    }


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }


    @Override
    public Peer registerPeer(String config) {
        if (config != null) {
            Peer peer = GSON.fromJson(config, Peer.class);
            peerManager.register(peer);
            return peer;
        } else {
            return null;
        }
    }


    @Override
    public String getPeerJsonFormat() {
        Peer peer = getSamplePeer();
        return GSON.toJson(peer);
    }


    @Override
    public String getId() {

        UUID id = peerManager.getSiteId();
        return GSON.toJson(id);
    }


    private Peer getSamplePeer() {
        Peer peer = new Peer();
        peer.setName("Peer name");
        peer.setIp("10.10.10.10");
        peer.setId(UUID.randomUUID().toString());
        return peer;
    }
}