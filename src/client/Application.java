package client;

import graphic.GUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;  
import java.net.UnknownHostException;
import java.util.Enumeration;   

import database.DataBaseConnectivity;
  

public class Application {  
   
	public static Process tcpDumpProcess = null;
	//sprivate static PcapIf device = null;
	private static boolean running = true;
	private static boolean applicationIsRunning = true;
	private static final Object lock = new Object();
	private static GUI gui = null;
	private static int channel = 0; 
	private static String device = "wlan0";
	public static NetworkInterface networkInterface = null;
	
    public static void main(String[] args) {  

    	Thread guiThread = new Thread() {
    		@Override
    		public void run() {
    			gui = new GUI();
    		}
    	};
    	guiThread.start();         
  
        Enumeration<NetworkInterface> networkInterfaces = null;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
        while (networkInterfaces.hasMoreElements())
        {
            NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
            if(networkInterface.getName().startsWith("wlan")) {
            	Application.networkInterface = networkInterface;
                break;
            }
        }
        
        // start button not pressed
        synchronized(lock) {
	        try {
				lock.wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
        }
        
        //start thread that verifies the availability of the devices
        Network.startAccessPointThread();
        Network.startStationThread();
        
        while (applicationIsRunning) {
        	startMonitoring(channel);
        	//wait for resume signal
        	synchronized (lock) {
	        	try {
	        		System.out.println("monitor is paused");
					lock.wait();
					System.out.println("monitor resumed");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        }
        
        //stop threads
        Network.stopAccessPointThread();
        Network.stopStationThread();
        
        gui.setVisible(false);
        gui.dispose();
        System.exit(0);
        
    }
    
    public static void startMonitoring(int channel) {
    	if (channel == 0) {
    		return;
    	}
        running = true;
    	/*
         * put NIC in monitor mode
         */
        String[] channelChangerInit = {"/usr/bin/sudo", "-n", System.getProperty("user.dir") + "/resources/monitor.sh", "init"}; 
        Process channelChanger = null;
        try {
			channelChanger = new ProcessBuilder(channelChangerInit).start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        while (true) {
        	try {
        		if (channelChanger.exitValue() == 0) {
        			break;
        		}
        	} catch (IllegalThreadStateException e) {
        	}
        }
        
        String[] channelChangerStart = {"/usr/bin/sudo", "-n", System.getProperty("user.dir") + "/resources/monitor.sh", "start", Integer.toString(channel)}; 
        channelChanger = null;
        try {
			channelChanger = new ProcessBuilder(channelChangerStart).start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        /*
         * For detailed 802.11 header use tcpDump
         * TODO change with lib2pcap
         */
        String[] tcpdumpCmd = {"/usr/bin/sudo", "-n", "/usr/sbin/tcpdump", "-l", "-i", device, "-s 0", "-e", "-xx"};
		try {
			tcpDumpProcess = new ProcessBuilder(tcpdumpCmd).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
        BufferedReader in = new BufferedReader(new InputStreamReader(tcpDumpProcess.getInputStream()));
        String tcpdumpOut = null;
        try {
        	StringBuilder sb = null;
        	Packet packet = null;
			while (running && (tcpdumpOut = in.readLine()) != null ) {
				if (tcpdumpOut.startsWith("\t0x")) {
				    sb.append(tcpdumpOut.substring(10).replaceAll(" ", ""));
				}
				else {
					if (sb != null && !sb.toString().isEmpty()) {
						packet = new Packet(sb.toString());
						packet.processPacket();
					}
					sb = new StringBuilder();
				}
			}
			in.close();
	    	tcpDumpProcess.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void stopMonitoring() {

    	running = false;
    	
        // put network adapter in managed mode
        String[] stopMonitoringString = {"/usr/bin/sudo", "-n", System.getProperty("user.dir") + "/resources/monitor.sh", "reset"};
        try {
			new ProcessBuilder(stopMonitoringString).start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		Application.running = running;
	}

	public static boolean isApplicationIsRunning() {
		return applicationIsRunning;
	}

	public static void setApplicationIsRunning(boolean applicationIsRunning) {
		Application.applicationIsRunning = applicationIsRunning;
	}

	public static Object getLock() {
		return lock;
	}

	public static int getChannel() {
		return channel;
	}

	public static void setChannel(int channel) {
		Application.channel = channel;
	}
    
}