package security;

import client.AccessPoint;
import client.Station;

public class Handshake {
	
	Station station;
	AccessPoint ap;
	//pairwise master key
	String PMK = null;
	//Pairwise Transient Key
	String PTK = null;
	//Groupwise Transient Key
	String GTK = null;
	//Message Integrity Check
	String MIC = null;
	String apNonce = null;
	String stationNonce = null;
	//4-way handshake is completed
	boolean successfull = false;
	int number = 0;
	
	public boolean isSuccessfull() {
		return successfull;
	}
	
	public void setSuccessfull(boolean successfull) {
		this.successfull = successfull;
		derive();
	}

	public Station getStation() {
		return station;
	}

	public void setStation(Station station) {
		this.station = station;
	}

	public AccessPoint getAp() {
		return ap;
	}

	public void setAp(AccessPoint ap) {
		this.ap = ap;
	}

	public String getPMK() {
		return PMK;
	}

	public void setPMK(String pMK) {
		PMK = pMK;
	}

	public String getApNonce() {
		return apNonce;
	}

	public void setApNonce(String apNonce) {
		this.apNonce = apNonce;
	}

	public String getStationNonce() {
		return stationNonce;
	}

	public void setStationNonce(String stationNonce) {
		this.stationNonce = stationNonce;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getGTK() {
		return GTK;
	}

	public void setGTK(String gTK) {
		GTK = gTK;
	}

	public String getMIC() {
		return MIC;
	}

	public void setMIC(String mIC) {
		MIC = mIC;
	}
	
	public void derive() {
		
	}
	
}
