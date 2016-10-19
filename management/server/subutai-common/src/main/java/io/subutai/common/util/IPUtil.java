package io.subutai.common.util;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.host.HostInterface;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.settings.Common;


public class IPUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger( IPUtil.class );


    private static long ipToLong( InetAddress ip )
    {
        byte[] octets = ip.getAddress();
        long result = 0;

        for ( byte octet : octets )
        {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }


    public static boolean isValidIPRange( String ipStart, String ipEnd, String ipToCheck )
    {
        try
        {
            if ( "*".equals( ipStart ) || "".equals( ipStart ) )
            {
                return true;
            }
            else
            {
                long ipLo = ipToLong( InetAddress.getByName( ipStart ) );
                long ipHi = ipToLong( InetAddress.getByName( ipEnd ) );
                long ipToTest = ipToLong( InetAddress.getByName( ipToCheck ) );
                return ( ipToTest >= ipLo && ipToTest <= ipHi );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error parsing InetAddress", e );
            return false;
        }
    }


    public static boolean isValid( final String ip )
    {
        return !Strings.isNullOrEmpty( ip ) && ip.matches( Common.IP_REGEX );
    }


    public static String getLocalIpByInterfaceName( String interfaceName ) throws SocketException
    {
        Enumeration<InetAddress> addressEnumeration = NetworkInterface.getByName( interfaceName ).getInetAddresses();
        while ( addressEnumeration.hasMoreElements() )
        {
            InetAddress address = addressEnumeration.nextElement();
            if ( address instanceof Inet4Address )
            {
                return address.getHostAddress();
            }
        }
        return null;
    }


    public static Set<String> getLocalIps() throws SocketException
    {
        Set<String> localIps = Sets.newHashSet();
        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();

        while ( netInterfaces.hasMoreElements() )
        {
            NetworkInterface networkInterface = netInterfaces.nextElement();
            Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
            while ( addressEnumeration.hasMoreElements() )
            {
                InetAddress address = addressEnumeration.nextElement();
                if ( address instanceof Inet4Address )
                {
                    localIps.add( address.getHostAddress() );
                }
            }
        }

        return localIps;
    }


    public static String getNetworkAddress( String ip )
    {
        SubnetUtils subnetUtils = new SubnetUtils( ip, "255.255.255.0" );
        return subnetUtils.getInfo().getNetworkAddress();
    }


    public static HostInterface findAddressableInterface( Set<HostInterface> allInterfaces, String hostId )
    {
        LocalPeer localPeer = ServiceLocator.getServiceNoCache( LocalPeer.class );

        HostInterface result = NullHostInterface.getInstance();

        try
        {
            //try to obtain MNG-NET interface first
            for ( HostInterface hostInterface : allInterfaces )
            {
                if ( Common.MNG_NET_INTERFACE.equals( hostInterface.getName() ) )
                {
                    //check if this is not an RH-with-MH and MNG-NET IP ends with 254
                    //in this case we need skip and use WAN ip
                    if ( localPeer != null && !localPeer.getManagementHost().getId().equals( hostId ) && hostInterface
                            .getIp().endsWith( "254" ) )
                    {
                        break;
                    }

                    return hostInterface;
                }
            }

            //otherwise return WAN interface ip
            for ( HostInterface hostInterface : allInterfaces )
            {
                if ( Common.WAN_INTERFACE.equals( hostInterface.getName() ) )
                {
                    return hostInterface;
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Error obtaining addressable net interface", e );
        }

        return result;
    }
}

