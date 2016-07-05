package client;

import java.util.HashMap;

public class AccessPoint extends Device {
	
	public static final String NO_ENCRYPTION = "noencryption";
	public static final String WEP_ENCRYPTION = "WEP";
	public static final String WPA_ENCRYPTION = "WPA";
	String SSID = null;
	boolean inRange = false;
	HashMap<String, Station> associatedStations = null;
	String encryption = NO_ENCRYPTION;
	
	public AccessPoint(String BSSID) {
		lastMentioned = System.currentTimeMillis();
		associatedStations = new HashMap<String, Station>();
		this.address = BSSID;
		Network.addAP(this);
	}
	
	public AccessPoint(String BSSID, String SSID) {
		lastMentioned = System.currentTimeMillis();
		associatedStations = new HashMap<String, Station>();
		this.address = BSSID;
		this.SSID = SSID;
		Network.addAP(this);
	}
	
	public AccessPoint(int signal, int channelFrequency, String BSSID, String SSID) {
		lastActive = System.currentTimeMillis();
		this.signal = signal;
		this.channelFrequency = channelFrequency;
		this.address = BSSID;
		this.SSID = SSID;
		associatedStations = new HashMap<String, Station>();
		inRange = true;
		Network.addAP(this);
	}

	public String getSSID() {
		return SSID;
	}

	public void setSSID(String SSID) {
		this.SSID = SSID;
	}

	public HashMap<String, Station> getAssociatedStations() {
		return associatedStations;
	}

	public void setAssociatedDevices(HashMap<String, Station> associatedStations) {
		this.associatedStations = associatedStations;
	}

	public void addStation(Station d) {
		associatedStations.put(d.address, d);
	}
	
	public void removeStation(Station d) {
		associatedStations.remove(d.address);
	}	
	
	public String getEncryption() {
		return encryption;
	}

	public void setEncryption(String encryption) {
		this.encryption = encryption;
	}

	public void update(boolean sender, int signal, int channelFrequency, String BSSID, String SSID, int packetSize, boolean retry, String type) {
		if (sender) {
			this.inRange = true;
			lastActive = System.currentTimeMillis();
			this.signal = signal;
			this.channelFrequency = channelFrequency;
			this.address = BSSID;
			if (SSID != null) {
				this.SSID = SSID;
			}
			packetsSent++;
			bytesSent += packetSize;
			if (retry) {
				packetsRetried++;
			}
			switch (type) {
				case Frame.MANAGEMENT_FRAME:
					managementPacketsSent++;
					bytesManagementPacketsSent += packetSize;
					break;
				case Frame.CONTROL_FRAME:
					controlPacketsSent++;
					bytesControlPacketsSent += packetSize;
					break;
				case Frame.DATA_FRAME:
					dataPacketsSent++;
					bytesDataPacketsSent += packetSize;
					break;
			}
		}
		else {
			lastMentioned = System.currentTimeMillis();
			if (retry) {
				packetsLost++;
			}
			else {
				packetsReceived++;
				bytesReceived += packetSize;
				switch (type) {
					case Frame.MANAGEMENT_FRAME:
						managementPacketsSent++;
						bytesManagementPacketsSent += packetSize;
						break;
					case Frame.CONTROL_FRAME:
						controlPacketsSent++;
						bytesControlPacketsSent += packetSize;
						break;
					case Frame.DATA_FRAME:
						dataPacketsSent++;
						bytesDataPacketsSent += packetSize;
						break;
				}
			}
		}
	}
	
}
