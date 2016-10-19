package io.subutai.hub.share.common;


import java.util.List;


public interface HubAdapter
{
    boolean isRegistered();
    //
    // Environments
    //

    String getUserEnvironmentsForPeer();

    void destroyContainer( String envId, String containerId );

    void uploadEnvironment( String json );

    void removeEnvironment( String envId );

    //
    // Plugins
    //

    <T> List<T> getPluginData( String pluginKey, Class<T> clazz );

    <T> T getPluginDataByKey( String pluginKey, String key, Class<T> clazz );

    boolean uploadPluginData( String pluginKey, String key, Object data );

    boolean deletePluginData( String pluginKey, String key );

    void onContainerStart( String envId, String contId );

    void onContainerStop( String envId, String contId );
}
