package io.subutai.core.peer.api;


/**
 * Peer action class
 */
public class PeerAction
{
    private PeerActionType type;
    private Object data;


    public PeerAction( final PeerActionType type )
    {
        this.type = type;
    }


    public PeerAction( final PeerActionType type, final Object data )
    {
        this.type = type;
        this.data = data;
    }


    public PeerActionType getType()
    {
        return type;
    }


    public Object getData()
    {
        return data;
    }
}
