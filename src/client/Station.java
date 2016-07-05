package client;

public class Station extends Device {

	AccessPoint ap = null;
	boolean isPhone = false;
	int powerManagementCount = 0;
	private final Object lock = new Object();

	public Station(String address) {
		lastMentioned = System.currentTimeMillis();
		this.address = address;
		Network.addStation(this);
	}

	public Station(AccessPoint ap, String address) {
		lastMentioned = System.currentTimeMillis();
		this.ap = ap;
		this.address = address;
		if (ap != null) {
			this.ap.addStation(this);
		}
		Network.addStation(this);
	}

	public Station(AccessPoint ap, String address, int signal, String powerSave) {
		lastActive = System.currentTimeMillis();
		this.ap = ap;
		this.address = address;
		this.signal = signal;
		this.powerManagement = powerSave;
		if (powerSave.equals("1")) {
			powerManagementCount++;
		}
		inRange = true;
		if (ap != null) {
			this.ap.addStation(this);
		}
		Network.addStation(this);
	}

	public AccessPoint getAp() {
		return ap;
	}

	public void setAp(AccessPoint ap) {
		if (this.ap != null) {
			this.ap.removeStation(this);
		}
		this.ap = ap;
		if (ap != null) {
			ap.addStation(this);
		}
	}

	public void update(boolean sender, AccessPoint ap, String powerManagement, int signal, int packetSize, boolean retry, String type) {
		synchronized (getLock()) {
			if (sender) {
				inRange = true;
				lastActive = System.currentTimeMillis();
				this.powerManagement = powerManagement;
				if (powerManagement.equals("1")) {
					powerManagementCount++;
				}
				this.signal = signal;
				bytesSent += packetSize;
				packetsSent++;
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
						managementPacketsReceived++;
						bytesManagementPacketsReceived += packetSize;
						break;
					case Frame.CONTROL_FRAME:
						controlPacketsReceived++;
						bytesControlPacketsReceived += packetSize;
						break;
					case Frame.DATA_FRAME:
						dataPacketsReceived++;
						bytesDataPacketsReceived += packetSize;
						break;
					}
				}
			}
			if (this.ap != ap) {
				this.setAp(ap);
			}
		}
	}

	public void setIsPhone(boolean isPhone) {
		this.isPhone = isPhone;
	}

	public boolean isPhone() {
		return isPhone;
	}

	public int getPowerManagementCount() {
		return powerManagementCount;
	}

	public void setPowerManagementCount(int powerManagementCount) {
		this.powerManagementCount = powerManagementCount;
	}

	public Object getLock() {
		return lock;
	}

}
