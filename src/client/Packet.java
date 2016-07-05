package client;

import java.util.HashMap;

import security.Handshake;

public class Packet {

	public static final String BROADCAST = "ffffffffffff";
	public static final String MULTICAST = "01005e";
	public static final String IPV6MULTICAST = "3333";
	public static final String BPDU = "01000c";
	public static final String STP = "0180c2";
	public static final String RANDOM = "0cd29";
	String packet = null;

	//radio header
	int radioHeaderLength = 0;
	int macTimestamp = 0;
	int dataRate = 0;
	int channelFrequency = 0;
	int channelType = 0;
	int signal = 0;

	//IEEE 802.11 header
	String protocolVersion = null;
	String frameType = null;
	String frameSubType = null;
	String toDS = null;
	String fromDS = null;
	String moreFragments = null;
	boolean retry = false;
	String powerManagement = "0";
	String moreData = null;
	String wep = null;
	String reversed = null;
	String duration = null;
	String address1 = null;
	String address2 = null;
	String address3 = null;
	int sequenceControl = 0;
	String address4 = null;
	int offset = 0;

	public Packet(String packet) {
		this.packet = packet;
	}


	public void processPacket() {
		/*
		 * Radiotap Header
		 * [0,1] - Header revision - always 0
		 * [2,3] - Header pad - always 0
		 * [4-7] - Radio header length
		 * [8-15] - Radio flags
		 * [16-31] - MAC timestamp
		 * [32-33] - Flags
		 * [34-35] - Data Rate (Mb/s)
		 * [36-39] - Channel frequency
		 * [40-43] - Channel type (802.11 b/g/n)
		 * [44-45] - SSI Signal
		 * [46-47] - antenna = 1 ?
		 */
		/*
		 * 802.11 Header
		 * [68](0-1) - protocol version
		 * [68](2-3) - frame type
		 * [69](0-3) - frame subType
		 * [70](0) - toDS flag
		 * [70](1) - fromDS flag
		 * [70](2) - more fragments flag
		 * [70](3) - retry flag
		 * [71](0) - power management flag
		 * [71](1) - more data flag
		 * [71](2) - WEP flag
		 * [71](3) - reversed flag
		 * [72-75] - duration ID
		 * [76-87] - addr1
		 * [88-99] - addr2
		 * [100-111] - addr3
		 * [112-117] - sequence control
		 * [118-129] - addr4 (optional, if fromDS & toDS flags are 1)
		 */
		try {
			radioHeaderLength = Integer.parseInt(packet.substring(6, 8) + packet.substring(4, 6), 16);
			macTimestamp = Integer.parseInt(packet.substring(30, 32) + packet.substring(28, 30) + packet.substring(26, 28) + packet.substring(24, 26) + packet.substring(22, 24) + packet.substring(20, 22) + packet.substring(18, 20) + packet.substring(16, 18), 16);
			dataRate = Integer.parseInt(packet.substring(34, 36), 16);
			channelFrequency = Integer.parseInt(packet.substring(38, 40) + packet.substring(36, 38), 16);
			channelType = Integer.parseInt(packet.substring(42, 44) + packet.substring(40, 42), 16);
			signal = Integer.parseInt(packet.substring(44, 46), 16) - 256;
		} catch (NumberFormatException e) {
		}
		/*
		 * QoS Data
		 * has MCS information about data rate
		 */
		if (radioHeaderLength == 40) {
			dataRate = Integer.parseInt(packet.substring(56, 58) + packet.substring(54, 56) + packet.substring(52, 54), 16);
			offset = 28;
		}

		try {
			String binaryString = Integer.toBinaryString(Integer.parseInt(packet.substring(radioHeaderLength*2, radioHeaderLength*2 + 2), 16));
			binaryString = ("00000000" + binaryString).substring(binaryString.length());
			protocolVersion = binaryString.substring(6);
			frameType = binaryString.substring(4,6);
			frameSubType = binaryString.substring(0, 4);
		} catch (StringIndexOutOfBoundsException e) {
			
		}
		
		try {
			switch (frameType) {
				case Frame.MANAGEMENT_FRAME:
					processManagementFrame();
					break;
				case Frame.CONTROL_FRAME:
					processControlFrame();
					break;
				case Frame.DATA_FRAME:
					processDataFrame();
					break;
				default:
					System.out.println("Malformed packet. Invalid frame type " + frameType);
					break;
			}
		} catch(StringIndexOutOfBoundsException e) {

		} catch(NullPointerException e) {
			
		}

	}

	public void processManagementFrame() {
		String flags = Integer.toBinaryString(Integer.parseInt(packet.substring(54, 56), 16));
		flags = ("00000000" + flags).substring(flags.length());
		toDS = flags.substring(7);
		fromDS = flags.substring(6, 7);
		moreFragments = flags.substring(5, 6);
		retry = flags.substring(4, 5).equals("1");
		powerManagement = flags.substring(3, 4);
		moreData = flags.substring(2, 3);
		wep = flags.substring(1, 2);
		reversed = flags.substring(0, 1);
		address1 = packet.substring(60, 72);
		address2 = packet.substring(72, 84);
		address3 = packet.substring(84, 96);
		if (toDS.equals("1") && fromDS.equals("1")) {
			address4 = packet.substring(100, 112);
		}
		switch(frameSubType) {
		case Frame.ManagementFrame.AITM:
			break;
		case Frame.ManagementFrame.ASSOCIATION_REQUEST:
			//System.out.println("Management frame - Association request");
			processAssociationRequest();
			break;
		case Frame.ManagementFrame.ASSOCIATION_RESPONSE:
			//System.out.println("Management frame - Association response");
			processAssociationResponse();
			break;
		case Frame.ManagementFrame.AUTHENTICATION:
			//System.out.println("Management frame - Authentication");
			processAuthentication();
			break;
		case Frame.ManagementFrame.BEACON:
			//System.out.println("Management frame - Beacon");
			processBeacon();
			break;
		case Frame.ManagementFrame.DEAUTHENTICATION:
			//System.out.println("Management frame - Deauthentication");
			break;
		case Frame.ManagementFrame.DISASSOCIATION:
			System.out.println("Management frame - Disassociation");
			processDisassociation();
			break;
		case Frame.ManagementFrame.PROBE_REQUEST:
			// get supported rates and extended supported rates for station
			//System.out.println("Management frame - Probe Request");
			processProbeRequest();
			break;
		case Frame.ManagementFrame.PROBE_RESPONSE:
			// get supported rated and extended supported rates for AP
			// similar to beacon
			//System.out.println("Management frame - Probe Response");
			processProbeResponse();
			break;
		case Frame.ManagementFrame.REASSOCIATION_REQUEST:
		case Frame.ManagementFrame.REASSOCIATION_RESPONSE:
			//radio NIC roams away from the currently associated access point and finds another access point having a stronger beacon signal
			//implement later
			//System.out.println("Management reassociation packet. Not handled");
			break;
		default:
			System.out.println("Unknown sub type " + frameSubType + " for a management frame.");
		}
	}

	public void processControlFrame() {
		String flags = Integer.toBinaryString(Integer.parseInt(packet.substring(52 + offset, 54 + offset), 16));
		flags = ("00000000" + flags).substring(flags.length());
		toDS = flags.substring(7);
		fromDS = flags.substring(6, 7);
		moreFragments = flags.substring(5, 6);
		retry = flags.substring(4, 5).equals("1");
		powerManagement = flags.substring(3, 4);
		moreData = flags.substring(2, 3);
		wep = flags.substring(1, 2);
		reversed = flags.substring(0, 1);
		address1 = packet.substring(60 + offset, 72 + offset);
		switch(frameSubType) {
		case Frame.ControlFrame.RTS:
			//System.out.println("Contol packet - RTS");
			processRTS();
			break;
		case Frame.ControlFrame.CTS:
			//System.out.println("Contol packet - CTS");
			processCTS();
			break;
		case Frame.ControlFrame.ACK:
			//System.out.println("Contol packet - ACK");
			processACK();
			break;
		case Frame.ControlFrame.BLOCK_ACK:
			//System.out.println("Contol packet - Block ACK");
			break;
		case Frame.ControlFrame.BLOCK_ACK_REQUEST:
			//System.out.println("Contol packet - Block ACK Request");
			break;
		case Frame.ControlFrame.CONTENTION_FREE_END:
			//System.out.println("Contol packet - Contention Free End");
			break;
		case Frame.ControlFrame.CONTENTION_FREE_END_ACK:
			//System.out.println("Contol packet - Contention Free End ACK");
			break;
		case Frame.ControlFrame.POWER_SAVE_POLL:
			//System.out.println("Control packet - Power Save Poll");
			break;
		default:
			//System.out.println("Unknown frame sub type " + frameSubType + " for a control frame.");

		}
	}

	public void processDataFrame() {
		String flags = Integer.toBinaryString(Integer.parseInt(packet.substring(54 + offset, 56 + offset), 16));
		flags = ("00000000" + flags).substring(flags.length());
		toDS = flags.substring(7);
		fromDS = flags.substring(6, 7);
		moreFragments = flags.substring(5, 6);
		retry = flags.substring(4, 5).equals("1");
		powerManagement = flags.substring(3, 4);
		moreData = flags.substring(2, 3);
		wep = flags.substring(1, 2);
		reversed = flags.substring(0, 1);
		address1 = packet.substring(60 + offset, 72 + offset);
		address2 = packet.substring(72 + offset, 84 + offset);
		address3 = packet.substring(84 + offset, 96 + offset);
		if (toDS.equals("1") && fromDS.equals("1")) {
			address4 = packet.substring(100 + offset, 112 + offset);
		}
		switch(frameSubType) {
		case Frame.DataFrame.DATA:
			//System.out.println("Data packet.");
			processData();
		case Frame.DataFrame.DATA_CF_ACK:
		case Frame.DataFrame.DATA_CF_POLL:
			break;
		case Frame.DataFrame.QoS:
			//System.out.println("Data QoS packet");
			processQoS();
			break;
		case Frame.DataFrame.NULL:
			//System.out.println("Data null packet");
			processNull();
			break;
		default:
			//System.out.println("Other data packet");
			break;
		}
	}

	/*
	 * 
	 * Management Frame Handlers
	 * 
	 */

	public void processBeacon() {
		//address1 will be broadcast
		//address2 = address3 = BSSID
		HashMap<String, String> taggedParameters = parseTaggedParameters(124);
		//System.out.println("Beacon from " + SSID.toString() + " with address " + address2 + " on channel " + Channel.get(channelFrequency));
		AccessPoint ap = Network.getAP(address2);
		if (ap == null) {
			ap = new AccessPoint(signal, channelFrequency, address2, taggedParameters.get(Frame.TaggedParameters.SSID));
			Network.addAP(ap);
		}
		else {
			ap.update(true, signal, channelFrequency, address2, taggedParameters.get(Frame.TaggedParameters.SSID), packet.length(), retry, frameType);
		}
		if (taggedParameters.get(Frame.TaggedParameters.RSN_INFORMATION) != null) {
			ap.setEncryption(AccessPoint.WPA_ENCRYPTION);
		}
	}

	/*
	 * Probe Request
	 * Sent by STA
	 * Tagged Parameters 100 : (packet.length - FCS.length)
	 */
	public void processProbeRequest() {
		/*
		 * broadcasted probe request
		 */
		if (address1.equals(BROADCAST)) {
			Station sta = Network.getStations().get(address2);
			if (sta != null) {
				sta.update(true, sta.getAp(), powerManagement, signal, packet.length(), retry, frameType);
			}
			else {
				sta = new Station(null, address2, signal, powerManagement);
			}
		}
		/*
		 * unicast probe request
		 * not realiable
		 */
		else {
			Station sta = Network.getStations().get(address2);
			AccessPoint ap = Network.getAccessPoints().get(address1); 
			StringBuilder SSID = new StringBuilder();
			if (Integer.parseInt(packet.substring(100, 102), 16) == 0) {
				int SSIDSize = Integer.parseInt(packet.substring(102, 104), 16);
				for (int i = 0 ; i < SSIDSize; i++) {
					SSID.append((char)Integer.parseInt(packet.substring(104 + i + i, 104 + i + i + 2), 16));	
				}
			}
			if (ap != null) {
				ap.update(false, ap.getSignal(), ap.getChannelFrequency(), ap.getAddress(), SSID.toString(), packet.length(), retry, frameType);
			}
			else {
				ap = new AccessPoint(address1, SSID.toString());
			}
			if (sta != null) {
				sta.update(true, sta.getAp(), powerManagement, signal, packet.length(), retry, frameType);
			}
			else {
				sta = new Station(null, address2, signal, powerManagement);
			}
		}
	}

	/*
	 * Probe Response
	 * sent from AP
	 * Fixed Parameters 100 : 124
	 * Tagged Parameters 124 : (packet.length - FCS.length)
	 */
	public void processProbeResponse() {
		AccessPoint ap = Network.getAccessPoints().get(address2);
		Station sta = Network.getStations().get(address1);
		StringBuilder SSID = new StringBuilder();
		if (Integer.parseInt(packet.substring(124, 126), 16) == 0) {
			int SSIDSize = Integer.parseInt(packet.substring(126, 128), 16);
			for (int i = 0 ; i < SSIDSize; i++) {
				SSID.append((char)Integer.parseInt(packet.substring(128 + i + i, 128 + i + i + 2), 16));	
			}
		}
		if (ap != null) {
			ap.update(true, signal, channelFrequency, address2, SSID.toString(), packet.length(), retry, frameType);
		}
		else {
			ap = new AccessPoint(signal, channelFrequency, address2, SSID.toString());
		}
		if (sta != null) {
			sta.update(false, sta.getAp(), sta.getPowerManagement(), sta.getSignal(), packet.length(), retry, frameType);
		}
		else {
			sta = new Station(address1);
		}
	}

	/*
	 * Authentication
	 * don't know if AP or STA is sending the message until checking Fixed Parameters
	 * Fixed Parameters 100 : (packet.length - FCS.length)
	 */
	public void processAuthentication() {
		AccessPoint ap = null;
		Station sta = null;
		/*
		 * parse the fixed parameters
		 */
		String authenticationAlgorithm = packet.substring(100, 104);
		String authenticationSequence = packet.substring(104, 108);
		boolean successful = packet.substring(108, 112).equals("0000");
		/*
		 * STA sent the message
		 */
		if (authenticationSequence.equals("0100")) {
			ap = Network.getAccessPoints().get(address1);
			sta = Network.getStations().get(address2);
			if (sta != null) {
				sta.update(true, sta.getAp(), powerManagement, signal, packet.length(), retry, frameType);
			}
			else {
				sta = new Station(null, address2, signal, powerManagement);
			}
			if (ap != null) {
				ap.update(false, ap.getSignal(), channelFrequency, address1, ap.getSSID(), packet.length(), retry, frameType);
			}
			else {
				ap = new AccessPoint(address1);
			}
		}
		else { 
			/*
			 * AP sent the message
			 */
			if (authenticationSequence.equals("0200")) {
				ap = Network.getAccessPoints().get(address2);
				sta = Network.getStations().get(address1);
				if (ap != null) {
					ap.update(true, signal, channelFrequency, address2, ap.getSSID(), packet.length(), retry, frameType);
				}
				else {
					ap = new AccessPoint(signal, channelFrequency, address2, null);
				}
				if (sta != null) {
					if (successful) {
						sta.update(false, ap, sta.getPowerManagement(), sta.getSignal(), packet.length(), retry, frameType);
					}
					else {
						//authentication was not successful
						sta.update(false, null, sta.getPowerManagement(), sta.getSignal(), packet.length(), retry, frameType);
					}
				}
				else {
					if (successful) {
						sta = new Station(ap, address1);
					}
					else {
						sta = new Station(address1);
					}
				}
			}
			else {
				//malformed sequence number
				return;
			}
		}
	}

	/*
	 * Authentication Request
	 * sent from STA
	 * Fixed Parameters 100 : 108
	 * Tagged Parameters 108 : packet.length - FCS.length
	 * TODO parse RSN information
	 */
	public void processAssociationRequest() {
		AccessPoint ap = Network.getAccessPoints().get(address1);
		Station sta = Network.getStations().get(address2);

		StringBuilder SSID = new StringBuilder();
		if (Integer.parseInt(packet.substring(108, 110), 16) == 0) {
			int SSIDSize = Integer.parseInt(packet.substring(110, 112), 16);
			for (int i = 0 ; i < SSIDSize; i++) {
				SSID.append((char)Integer.parseInt(packet.substring(112 + i + i, 112 + i + i + 2), 16));	
			}
		}
		if (ap == null) {
			ap = new AccessPoint(0, channelFrequency, address1, SSID.toString());
		}
		ap.update(false, ap.getSignal(), channelFrequency, address1, SSID.toString(), packet.length(), retry, frameType);


		if (sta == null) {
			sta = new Station(ap, address2, signal, powerManagement);
		}
		sta.update(true, ap, powerManagement, signal, packet.length(), retry, frameType);		
	}

	/*
	 * Authentication Response
	 * sent from AP
	 * Fixed Parameters 100 : 112
	 * Tagged Parameters 112 : packet.length - FCS.length
	 */
	public void processAssociationResponse() {
		AccessPoint ap = Network.getAccessPoints().get(address2);
		Station sta = Network.getStations().get(address1);
		boolean successful = packet.substring(104, 108).equals("0000");
		int authenticationId = Integer.parseInt(packet.substring(110, 112) + packet.substring(108, 110), 16);

		if (ap == null) {
			ap = new AccessPoint(signal, channelFrequency, address2, null);
		}
		ap.update(true, signal, channelFrequency, address2, ap.getSSID(), packet.length(), retry, frameType);

		if (sta != null) {
			if (successful) {
				sta.update(false, ap, sta.getPowerManagement(), sta.getSignal(), packet.length(), retry, frameType);
			}
			else {
				sta.update(false, null, sta.getPowerManagement(), sta.getSignal(), packet.length(), retry, frameType);
			}
		}
		else {
			if (successful) {
				sta = new Station(ap, address1);
				sta.update(false, ap, sta.getPowerManagement(), sta.getSignal(), packet.length(), retry, frameType);
			}
			else {
				sta = new Station(address1);
				sta.update(false, null, sta.getPowerManagement(), sta.getSignal(), packet.length(), retry, frameType);
			}
		}
	}

	/*
	 * Disassociation 
	 * sent from STA
	 */
	public void processDisassociation() {
		AccessPoint ap = Network.getAccessPoints().get(address1);
		Station sta = Network.getStations().get(address2);

		if (ap == null) {
			ap = new AccessPoint(0, channelFrequency, address1, null);
		}
		ap.update(false, ap.getSignal(), channelFrequency, address1, ap.getSSID(), packet.length(), retry, frameType);

		if (sta == null) {
			sta = new Station(null, address2, signal, powerManagement);
		}
		sta.update(true, null, powerManagement, signal, packet.length(), retry, frameType);
	}


	/*
	 * 
	 * Control Frame Handlers
	 * 
	 */

	public void processRTS() {
		address2 = packet.substring(72 + offset, 84 + offset);
		AccessPoint ap = Network.getAccessPoints().get(address2);
		Station sta = Network.getStations().get(address1);

		//ap sent the RTS
		if (ap != null) {
			ap.update(true, signal, channelFrequency, address2, ap.getSSID(), packet.length(), retry, frameType);
			if (sta == null) {
				sta = new Station(ap, address1);
			}
			// sta will send a CTS and an ACK that cannot be traced
			sta.update(true, ap, sta.getPowerManagement(), sta.getSignal(), 96, false, frameType);
			sta.update(true, ap, sta.getPowerManagement(), sta.getSignal(), 96, false, frameType);
		}
		//ap received the frame
		else {
			ap = Network.getAccessPoints().get(address1);
			sta = Network.getStations().get(address2);
			if (ap != null) {
				ap.update(false, signal, channelFrequency, ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
				if (sta == null) {
					sta = new Station(ap, address2, signal, powerManagement);
				}
				sta.update(true, ap, powerManagement, signal, packet.length(), retry, frameType);
			}
		}
	}

	/*
	 * don't know if station or AP sent the packet
	 */
	public void processCTS() {
		AccessPoint ap = Network.getAccessPoints().get(address1);
		Station sta = Network.getStations().get(address1);
		if (ap != null) {
			ap.update(false, signal, channelFrequency, ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
		}
		if (sta != null) {
			sta.update(false, sta.getAp(), powerManagement, signal, packet.length(), retry, frameType);
			ap = sta.getAp();
			if (ap != null) {
				ap.update(true, signal, channelFrequency, ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
			}
		}
	}

	/*
	 * don't know if station or AP sent the packet
	 */
	public void processACK() {
		AccessPoint ap = Network.getAccessPoints().get(address1);
		Station sta = Network.getStations().get(address1);
		if (ap != null) {
			ap.update(false, signal, channelFrequency, ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
		}
		if (sta != null) {
			sta.update(false, sta.getAp(), powerManagement, signal, packet.length(), retry, frameType);
			ap = sta.getAp();
			if (ap != null) {
				ap.update(true, signal, channelFrequency, ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
			}
		}
	}

	public void processBlockACK() {
		address2 = packet.substring(72 + offset, 84 + offset);
		AccessPoint ap = Network.getAccessPoints().get(address2);
		Station sta = Network.getStations().get(address1);

		//ap sent the BlockACK
		if (ap != null) {
			ap.update(true, signal, channelFrequency, address2, ap.getSSID(), packet.length(), retry, frameType);
			if (sta == null) {
				sta = new Station(ap, address1);
			}
		}
		//ap received the BlockACK
		else {
			ap = Network.getAccessPoints().get(address1);
			sta = Network.getStations().get(address2);
			if (ap != null) {
				ap.update(false, signal, channelFrequency, ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
				if (sta == null) {
					sta = new Station(ap, address2, signal, powerManagement);
				}
				sta.update(true, ap, powerManagement, signal, packet.length(), retry, frameType);
			}
		}
	}

	/*
	 * 
	 * Data Frame Handlers
	 * 
	 */
	public void processData() {
		if (fromDS.equals("1")) {
			if (toDS.equals("1")) {
				System.out.println("Bridge connection");
			}
			else {
				//download
				AccessPoint ap = Network.getAccessPoints().get(address2);
				if (ap == null) {
					ap = new AccessPoint(signal, channelFrequency, address2, null);
				}
				ap.update(true, signal, channelFrequency, address2, ap.getSSID(), packet.length(), retry, frameType);
				if (!address1.equals(BROADCAST) && !address1.startsWith(MULTICAST) && !address1.startsWith(IPV6MULTICAST) && !address1.startsWith(BPDU) && !address1.startsWith(STP) && !address1.startsWith(RANDOM)) {
					Station d = Network.getStation(address1);
					if (d == null) {
						d = new Station(ap, address1);
					}
					d.update(false, ap, d.getPowerManagement(), d.getSignal(), packet.length(), retry, frameType);
					d.setDataRate(dataRate);
				}
			}
		}
		else {
			if (toDS.equals("0")) {
				if (address1.equals(BROADCAST)  && !address1.startsWith(MULTICAST) && !address1.startsWith(IPV6MULTICAST) && !address1.startsWith(BPDU) && !address1.startsWith(STP) && !address1.startsWith(RANDOM)) {
					AccessPoint ap = Network.getAccessPoints().get(address2);
					Station sta = Network.getStations().get(address2);
					if (ap != null) {
						ap.update(true, signal, channelFrequency, ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
					}
					if (sta != null) {
						sta.update(true, sta.getAp(), powerManagement, signal, packet.length(), retry, frameType);
					}
				}
				else {
					//TODO handle adhoc connections
					System.out.println("AdHoc connection");
				}
			}
			else {
				//upload
				AccessPoint ap = Network.getAP(address1);
				if (ap == null) {
					ap = new AccessPoint(address1);
				}
				ap.update(false, ap.getSignal(), ap.getChannelFrequency(), ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
				if (!address2.equals(BROADCAST)) {
					Station d = Network.getStation(address2);
					if (d == null) {
						d = new Station(ap, address2, signal, powerManagement);
					}
					d.update(true, ap, powerManagement, signal, packet.length(), retry, frameType);
					d.setDataRate(dataRate);
				}
			}
		}
	}

	public void processQoS() {
		if (packet.substring(132 + offset, 136 + offset).equals("888e")) {
			System.out.println("802.1x authentication");
			//handshake captured
			String flags = Integer.toBinaryString(Integer.parseInt(packet.substring(148 + offset, 150 + offset), 16));
			flags = ("00000000" + flags).substring(flags.length());
			//010 - HMAC-SHA1 for MIC and AES key wrap for encryption (AES)
			//001 - HMAC-MD5 for MIC and RC4 for encryption (TKIP)
			if (flags.substring(5).equals("010") || flags.substring(5).equals("001")) {
				String key = null;
				if (toDS.equals("1") && fromDS.equals("0")) {
					key = address1 + address2;
				}
				if (toDS.equals("0") && fromDS.equals("1")) {
					key = address2 + address1;
				}
				if ((toDS.equals("1") && fromDS.equals("1")) || (toDS.equals("0") && fromDS.equals("0"))) {
					System.out.println("Malformed authentication frame");
					return;
				}
				Handshake handshake = Network.getHandshakes().get(key);
				if (handshake == null  || handshake.getNumber() == 4) {
					handshake = new Handshake();
					Network.getHandshakes().put(key, handshake);
				}
				//first step AP->STA
				if (handshake.getNumber() == 0) {
					if (!(toDS.equals("0") && fromDS.equals("1"))) {
						//lost first step of the  handshake -> ap not in range
						return;
					}
					handshake.setAp(Network.getAccessPoints().get(address2));
					handshake.setStation(Network.getStations().get(address1));
					handshake.setApNonce(packet.substring(170 + offset, 234 + offset));
					handshake.setNumber(1);
					return;
				}
				if (handshake.getNumber() == 1) {
					if (toDS.equals("0") && fromDS.equals("1")) {
						//received two consecutive messages from ap -> station not in range
						//remove entry from handshakes map
						Network.getHandshakes().remove(key);
						return;
					}
					handshake.setStationNonce(packet.substring(170 + offset, 234 + offset));
					handshake.setNumber(2);
					//TODO also parse MIC
					return;
				}
				if (handshake.getNumber() == 2) {
					//TODO parse GTK + MIC
					handshake.setNumber(3);
					return;
				}
				if (handshake.getNumber() == 3) {
					handshake.setNumber(4);
					handshake.setSuccessfull(true);
					System.out.println("Successfully captured handshake!");
					return;
				}
			}
		}
		else {
			if (fromDS.equals("1")) {
				if (toDS.equals("1")) {
					System.out.println("Bridge connection");
				}
				else {
					//download
					AccessPoint ap = Network.getAccessPoints().get(address2);
					if (ap == null) {
						ap = new AccessPoint(signal, channelFrequency, address2, null);
					}
					ap.update(true, signal, channelFrequency, address2, ap.getSSID(), packet.length(), retry, frameType);
					if (!address1.equals(BROADCAST) && !address1.startsWith(MULTICAST) && !address1.startsWith(IPV6MULTICAST) && !address1.startsWith(BPDU) && !address1.startsWith(STP) && !address1.startsWith(RANDOM)) {
						Station d = Network.getStation(address1);
						if (d == null) {
							d = new Station(ap, address1);
						}
						d.update(false, ap, d.getPowerManagement(), d.getSignal(), packet.length(), retry, frameType);
						d.setDataRate(dataRate);
					}
				}
			}
			else {
				if (toDS.equals("0")) {
					if (address1.equals(BROADCAST)  && !address1.startsWith(MULTICAST) && !address1.startsWith(IPV6MULTICAST) && !address1.startsWith(BPDU) && !address1.startsWith(STP) && !address1.startsWith(RANDOM)) {
						AccessPoint ap = Network.getAccessPoints().get(address2);
						Station sta = Network.getStations().get(address2);
						if (ap != null) {
							ap.update(true, signal, channelFrequency, ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
						}
						if (sta != null) {
							sta.update(true, sta.getAp(), powerManagement, signal, packet.length(), retry, frameType);
						}
					}
					else {
						//TODO handle adhoc connections
						System.out.println("AdHoc connection");
					}
				}
				else {
					//upload
					AccessPoint ap = Network.getAP(address1);
					if (ap == null) {
						ap = new AccessPoint(address1);
					}
					ap.update(false, ap.getSignal(), ap.getChannelFrequency(), ap.getAddress(), ap.getSSID(), packet.length(), retry, frameType);
					if (!address2.equals(BROADCAST)) {
						Station d = Network.getStation(address2);
						if (d == null) {
							d = new Station(ap, address2, signal, powerManagement);
						}
						d.update(true, ap, powerManagement, signal, packet.length(), retry, frameType);
						d.setDataRate(dataRate);
					}
				}
			}
		}
	}
	/*
	 * Null
	 * sent by STA only for flag updates (ex. power management)
	 */
	public void processNull() {
		AccessPoint ap = Network.getAccessPoints().get(address1);
		Station sta = Network.getStations().get(address2);

		if (ap != null) {
			ap.update(false, ap.getSignal(), channelFrequency, address1, ap.getSSID(), packet.length(), retry, frameType);
		}
		else {
			ap = new AccessPoint(0, channelFrequency, address1, null);
			ap.update(false, ap.getSignal(), channelFrequency, address1, ap.getSSID(), packet.length(), retry, frameType);
		}

		if (sta != null) {
			sta.update(true, sta.getAp(), powerManagement, signal, packet.length(), retry, frameType);
		}
		else {
			sta = new Station(ap, address2, signal, powerManagement);
			sta.update(true, sta.getAp(), powerManagement, signal, packet.length(), retry, frameType);
		}
	}

	public HashMap<String, String> parseTaggedParameters(int offset) {

		HashMap<String, String> taggedParameters = new HashMap<String, String>();
		String tag = null;
		while(true) {
			tag = packet.substring(offset, offset+2);

			switch(tag) {
			case Frame.TaggedParameters.SSID:
				StringBuilder SSID = new StringBuilder();
				int SSIDSize = Integer.parseInt(packet.substring(offset+2, offset+4), 16);
				for (int i = 0 ; i < SSIDSize; i++) {
					SSID.append((char)Integer.parseInt(packet.substring(offset+4 + i + i, offset+4 + i + i + 2), 16));	
				}
				taggedParameters.put(Frame.TaggedParameters.SSID, SSID.toString());
				break;
			case Frame.TaggedParameters.VENDOR_SPECIFIC:
				break;
			case Frame.TaggedParameters.SUPPORTED_RATES:
				break;
			case Frame.TaggedParameters.EXTENDED_SUPPORTED_RATES:
				break;
			case Frame.TaggedParameters.HT_CAPABILITIES:
				break;
			case Frame.TaggedParameters.DS_PARAMETER:
				break;
			case Frame.TaggedParameters.ERP_INFORMATION:
				break;
			case Frame.TaggedParameters.HT_INFORMATION:
				break;
			case Frame.TaggedParameters.SECONDARY_CHANNEL_OFFSET:
				break;
			case Frame.TaggedParameters.RSN_INFORMATION: 
				String pairwiseCipherSuite = packet.substring(offset + 8, offset+14);
				if (pairwiseCipherSuite.equals("000fac")) {
					pairwiseCipherSuite = "TKIP";
				}
				taggedParameters.put(Frame.TaggedParameters.RSN_INFORMATION, pairwiseCipherSuite);
				break;
			case Frame.TaggedParameters.EXTENDED_CAPABILITIES:
				break;
			case Frame.TaggedParameters.COUNTRY_INFORMATION:
				break;
			default:
				//increment offset
				break;
			}
			offset = offset + 4 + Integer.parseInt(packet.substring(offset+2, offset+4), 16) * 2;
			/*
			 * 8 = FCS length
			 */
			if (packet.length() - 8 == offset) {
				break;
			}
		}

		return taggedParameters;
	}

}
