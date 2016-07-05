package client;

import java.util.Date;

import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

public class PacketHandler<T> implements PcapPacketHandler<T> {

	@Override
	public void nextPacket(PcapPacket packet, Object arg1) {
		System.out.printf("Received packet at %s caplen=%-4d len=%-4d\n",  
                new Date(packet.getCaptureHeader().timestampInMillis()),
                packet.getCaptureHeader().caplen(),  // Length actually captured  
                packet.getCaptureHeader().wirelen() // Original length   
                );
		
        /*
        Ethernet eth = new Ethernet();
        Ip4 ip = new Ip4();
        Tcp tcp = new Tcp();
        Udp udp = new Udp();
        Http http = new Http();
        JBuffer header = null;
            
        if(packet.hasHeader(eth)) {
        	header = packet.getHeader(eth);
        	System.out.println(header.toHexdump());
        }
            
        if (packet.hasHeader(ip)) {
        	header = packet.getHeader(ip);
        	System.out.println(header.toHexdump());
        }
        
        if (packet.hasHeader(tcp)) {
        	header = packet.getHeader(tcp);
        	System.out.println(header.toHexdump());
        }
        
        if (packet.hasHeader(udp)) {
        	header = packet.getHeader(udp);
        	System.out.println(header.toHexdump());
        }
        */
        
        /*
         * parse packet layer by layer
         */
        
        /*
         * ETHERNET
         */
        for(int i = 0 ; i < packet.getCaptureHeader().caplen() ; i++) {
        	System.out.print(convertByteToHex(packet.getUByte(i)) + " ");	
        }
		
	}
	
	public String convertByteToHex(int b) {
		return String.format("%02x ", b);
	}

}
