package io.subutai.core.systemmanager.api;


import org.apache.commons.configuration.ConfigurationException;

import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;


public interface SystemManager
{

    NetworkSettings getNetworkSettings() throws ConfigurationException;

    SystemInfo getSystemInfo();

    void setPeerSettings();

    PeerSettings getPeerSettings();

    void setNetworkSettings( final String publicUrl, final String publicSecurePort, final String startRange,
                             final String endRange ) throws ConfigurationException;

    AdvancedSettings getAdvancedSettings();


    SystemInfo getManagementUpdates();

    boolean updateManagement();
}
