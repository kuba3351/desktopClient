package com.raspberry;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class AutoDiscovery {

    private String raspberryIpAddress;

    private static AutoDiscovery instance;

    public static AutoDiscovery getInstance() {
        if(instance == null)
            instance = new AutoDiscovery();
        return instance;
    }

    public void setRaspberryIpAddress(String raspberryIpAddress) {
        this.raspberryIpAddress = raspberryIpAddress;
    }

    public String getRaspberryIpAddress() {
        return raspberryIpAddress;
    }

    private AutoDiscovery() {

    }

    public void findRaspberryIp() {
        try {
            DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);
            byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 9000);
                c.send(sendPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface)interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                        c.send(sendPacket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            c.receive(receivePacket);
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                raspberryIpAddress = receivePacket.getAddress().getHostAddress();
            }
            c.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
