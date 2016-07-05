package client;

public class Device {
	
	long lastActive = 0;
	long lastMentioned = 0;
	String address = null;
	String vendor = null;
	int signal = -100;
	boolean inRange = false;
	String powerManagement = "0";
	int channelFrequency = 0;
	int dataRate = 0;
	int packetsSent = 0;
	int bytesSent = 0;
	int packetsReceived = 0;
	int bytesReceived = 0;
	int packetsLost = 0;
	int packetsRetried = 0;
	int managementPacketsSent = 0;
	int controlPacketsSent = 0;
	int dataPacketsSent = 0;
	int managementPacketsReceived = 0;
	int controlPacketsReceived = 0;
	int dataPacketsReceived = 0;
	int bytesManagementPacketsSent = 0;
	int bytesControlPacketsSent = 0;
	int bytesDataPacketsSent = 0;
	int bytesManagementPacketsReceived = 0;
	int bytesControlPacketsReceived = 0;
	int bytesDataPacketsReceived = 0;
	boolean delete = true;

	public long getLastMentioned() {
		return lastMentioned;
	}

	public void setLastMentioned(long lastMentioned) {
		this.lastMentioned = lastMentioned;
	}

	public boolean isInRange() {
		return inRange;
	}

	public void setInRange(boolean inRange) {
		this.inRange = inRange;
	}

	public int getPacketsSent() {
		return packetsSent;
	}

	public void setPacketsSent(int packetsSent) {
		this.packetsSent = packetsSent;
	}

	public int getBytesSent() {
		return bytesSent;
	}

	public void setBytesSent(int bytesSent) {
		this.bytesSent = bytesSent;
	}

	public int getPacketsReceived() {
		return packetsReceived;
	}

	public void setPacketsReceived(int packetsReceived) {
		this.packetsReceived = packetsReceived;
	}

	public int getBytesReceived() {
		return bytesReceived;
	}

	public void setBytesReceived(int bytesReceived) {
		this.bytesReceived = bytesReceived;
	}

	public long getLastActive() {
		return lastActive;
	}

	public void setLastActive(long time) {
		this.lastActive = time;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getSignal() {
		return signal;
	}

	public void setSignal(int signal) {
		this.signal = signal;
	}
	
	public String getPowerManagement() {
		return powerManagement;
	}

	public void setPowerManagement(String powerManagement) {
		this.powerManagement = powerManagement;
	}
	
	public int getChannelFrequency() {
		return channelFrequency;
	}

	public void setChannelFrequency(int channelFrequency) {
		this.channelFrequency = channelFrequency;
	}

	public int getDataRate() {
		return dataRate;
	}

	public void setDataRate(int dataRate) {
		this.dataRate = dataRate;
	}

	public int getPacketsLost() {
		return packetsLost;
	}

	public void setPacketsLost(int packetsLost) {
		this.packetsLost = packetsLost;
	}

	public int getPacketsRetried() {
		return packetsRetried;
	}

	public void setPacketsRetried(int packetsRetried) {
		this.packetsRetried = packetsRetried;
	}

	public int getManagementPacketsSent() {
		return managementPacketsSent;
	}

	public void setManagementPacketsSent(int managementPacketsSent) {
		this.managementPacketsSent = managementPacketsSent;
	}

	public int getControlPacketsSent() {
		return controlPacketsSent;
	}

	public void setControlPacketsSent(int controlPacketsSent) {
		this.controlPacketsSent = controlPacketsSent;
	}

	public int getDataPacketsSent() {
		return dataPacketsSent;
	}

	public void setDataPacketsSent(int dataPacketsSent) {
		this.dataPacketsSent = dataPacketsSent;
	}

	public int getManagementPacketsReceived() {
		return managementPacketsReceived;
	}

	public void setManagementPacketsReceived(int managementPacketsReceived) {
		this.managementPacketsReceived = managementPacketsReceived;
	}

	public int getControlPacketsReceived() {
		return controlPacketsReceived;
	}

	public void setControlPacketsReceived(int controlPacketsReceived) {
		this.controlPacketsReceived = controlPacketsReceived;
	}

	public int getDataPacketsReceived() {
		return dataPacketsReceived;
	}

	public void setDataPacketsReceived(int dataPacketsReceived) {
		this.dataPacketsReceived = dataPacketsReceived;
	}

	public int getBytesManagementPacketsSent() {
		return bytesManagementPacketsSent;
	}

	public void setBytesManagementPacketsSent(int bytesManagementPacketsSent) {
		this.bytesManagementPacketsSent = bytesManagementPacketsSent;
	}

	public int getBytesControlPacketsSent() {
		return bytesControlPacketsSent;
	}

	public void setBytesControlPacketsSent(int bytesControlPacketsSent) {
		this.bytesControlPacketsSent = bytesControlPacketsSent;
	}

	public int getBytesDataPacketsSent() {
		return bytesDataPacketsSent;
	}

	public void setBytesDataPacketsSent(int bytesDataPacketsSent) {
		this.bytesDataPacketsSent = bytesDataPacketsSent;
	}

	public int getBytesManagementPacketsReceived() {
		return bytesManagementPacketsReceived;
	}

	public void setBytesManagementPacketsReceived(int bytesManagementPacketsReceived) {
		this.bytesManagementPacketsReceived = bytesManagementPacketsReceived;
	}

	public int getBytesControlPacketsReceived() {
		return bytesControlPacketsReceived;
	}

	public void setBytesControlPacketsReceived(int bytesControlPacketsReceived) {
		this.bytesControlPacketsReceived = bytesControlPacketsReceived;
	}

	public int getBytesDataPacketsReceived() {
		return bytesDataPacketsReceived;
	}

	public void setBytesDataPacketsReceived(int bytesDataPacketsReceived) {
		this.bytesDataPacketsReceived = bytesDataPacketsReceived;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}
	
}
