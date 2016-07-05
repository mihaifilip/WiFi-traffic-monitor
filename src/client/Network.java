package client;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import security.Handshake;

public class Network {
	static HashMap<String, AccessPoint> accessPoints = new HashMap<String, AccessPoint>();
	static HashMap<String, Station> stations = new HashMap<String, Station>();
	//concatenate AP and Sta mac address as key
	static HashMap<String, Handshake> handshakes = new HashMap<String, Handshake>();
	private static UpdateAccessPoints accessPointThread = null;
	private static UpdateStations stationThread = null;
	static final Object lock_ = new Object();
	
	public static HashMap<String, Station> getStations() {
		return stations;
	}

	public static void setStations(HashMap<String, Station> stations) {
		Network.stations = stations;
	}

	public static HashMap<String, AccessPoint> getAccessPoints() {
		return accessPoints;
	}

	public static void setAccessPoints(HashMap<String, AccessPoint> accessPoints) {
		Network.accessPoints = accessPoints;
	}
	
	public static void addAP(AccessPoint ap) {
		accessPoints.put(ap.address, ap);
	}
	
	public static AccessPoint getAP(String BSSID) {
		return accessPoints.get(BSSID);
	}
	
	public static void addStation(Station d) {
		stations.put(d.address, d);
	}
	
	public static Station getStation(String address) {
		return stations.get(address);
	}

	public static HashMap<String, Handshake> getHandshakes() {
		return handshakes;
	}

	public static void setHandshakes(HashMap<String, Handshake> handshakes) {
		Network.handshakes = handshakes;
	}
	
	public static void startAccessPointThread() {
		accessPointThread = new UpdateAccessPoints();
		new Thread(accessPointThread).start();
	}
	
	public static void startStationThread() {
		stationThread = new UpdateStations();
		new Thread(stationThread).start();
	}
	
	public static void stopAccessPointThread() {
		accessPointThread.stopRunning();
	}
	
	public static void stopStationThread() {
		stationThread.stopRunning();
	}
	
	static class UpdateAccessPoints implements Runnable {
		private static final int REFRESH_RATE = 5;
		private static final int EXPIRY = 10;
		private Timer timer = new Timer();
		private boolean running = true;

		public void stopRunning() {
			running = false;
		}
		
		@Override
		public void run() {

			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (running) {
						TreeSet<String> newSet = new TreeSet<String>(Network.getAccessPoints().keySet());
						for(String address : newSet) {
							AccessPoint ap = Network.getAccessPoints().get(address);
							checkValidity(ap);	
						}
					}
					else {
						timer.cancel();
						timer.purge();
					}
				}
			}, 1000, REFRESH_RATE * 1000);
		}
		
		/*
		 * 5000 milli is the longest time before the NIC cycles through all the channels
		 */
		private void checkValidity(AccessPoint ap) {
			
			if (!ap.isDelete()) {
				return;
			}
			/*
			 * see if access point is in range and active
			 */
			if (ap.getLastActive() != 0) {
				/*
				 * ap is not in range anymore
				 */
				if (System.currentTimeMillis() - ap.getLastActive() > EXPIRY * 1000) {
					ap.setInRange(false);
					/*
					 * lost contact with the access point
					 * release resources
					 */
					if (System.currentTimeMillis() - ap.getLastMentioned() > EXPIRY * 1000) {
						/*
						 * remove stations associated with that AP
						 * (should not be any)
						 */
						for (String address : ap.getAssociatedStations().keySet()) {
							Network.getStations().remove(address);
						}
						Network.getAccessPoints().remove(ap.getAddress());
					}
				}
				else {
					ap.setInRange(true);
				}
			}
			else {
				/*
				 * ap was outside range
				 */
				ap.setInRange(false);
				if (System.currentTimeMillis() - ap.getLastMentioned() > EXPIRY * 1000) {
					/*
					 * remove stations associated with that AP
					 * (should not be any)
					 */
					for (String address : ap.getAssociatedStations().keySet()) {
						Network.getStations().remove(address);
					}
					Network.getAccessPoints().remove(ap.getAddress());
				}
			}
		}
	}
	
	static class UpdateStations implements Runnable {
		private static final int REFRESH_RATE = 5;
		private static final int EXPIRY = 10;
		private Timer timer = new Timer();
		private boolean running = true;

		public void stopRunning() {
			running = false;
		}
		
		@Override
		public void run() {

			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (running) {
						TreeSet<String> newSet = new TreeSet<String>(Network.getStations().keySet());
						for(String address : newSet) {
							Station sta = Network.getStations().get(address);
							checkValidity(sta);	
						}
					}
					else {
						timer.cancel();
						timer.purge();
					}
				}
			}, 1000, REFRESH_RATE * 1000);
		}
		
		private void checkValidity(Station sta) {
			
			if (!sta.isDelete()) {
				return;
			}
			/*
			 * see if the station is in range and active
			 */
			if (sta.getLastActive() != 0) {
				/*
				 * station is not in range anymore
				 */
				if (System.currentTimeMillis() - sta.getLastActive() > EXPIRY * 1000) {
					sta.setInRange(false);
					/*
					 * lost contact with the station
					 * release resources
					 */
					if (System.currentTimeMillis() - sta.getLastMentioned() > EXPIRY * 1000) {
						/*
						 * remove station from ap list
						 */
						sta.setAp(null);
						/*
						 * remove station from network map
						 */
						synchronized (sta.getLock()) {
							Network.getStations().remove(sta.getAddress());
						}
					}
				}
				else {
					sta.setInRange(true);
				}
			}
			else {
				/*
				 * station was outside range
				 */
				sta.setInRange(false);
				if (System.currentTimeMillis() - sta.getLastMentioned() > EXPIRY * 1000) {
					/*
					 * remove station from ap list
					 */
					sta.setAp(null);
					/*
					 * remove station from network map
					 */
					synchronized (sta.getLock()) {
						Network.getStations().remove(sta.getAddress());
					}
				}
			}
		}	
	}
}
