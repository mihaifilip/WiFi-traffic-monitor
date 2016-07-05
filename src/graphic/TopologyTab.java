package graphic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import client.AccessPoint;
import client.Application;
import client.Network;
import client.Packet;
import client.Station;
import database.DataBaseConnectivity;

public class TopologyTab extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final String routerImage = "/resources/router_small.jpg";
	private static final String phoneImage = "/resources/phone_small.jpg";
	private static final String notebookImage = "/resources/notebook_small.jpg";
	public static final int ACCESS_POINT = 0;
	public static final int STATION = 1;
	private static final int REFRESH_RATE = 2;
	private JSplitPane splitPane = null;
	private JSplitPane splitPane2 = null;
	private JPanel buttonPanel = null;
	private JPanel apPanel = null;
	private JPanel staPanel = null;
	private ArrayList<DevicePanel> apPanelHolder = new ArrayList<>();
	private DevicePanel[][] panelHolder = null;
	boolean[][] stations = null;
	boolean[] accessPoints = null;
	private int numColAccessPoints = 5;
	private int numRowStations = 5;
	private int numColStations = 10;
	private String address = null;
	private String password = null;
	private AccessPointsStats apThread = null;
	private Process joinNetwork = null;
	private JButton join = null;
	private TopologyTab context = null;
	public HashMap<String, String> ip = new HashMap<String, String>();

	public TopologyTab(String address) {
		super();
		this.context = this;
		this.address = address;
		accessPoints = new boolean[numColAccessPoints];
		for (int i = 0 ; i < numColAccessPoints ; i++) {
			accessPoints[i] = false;
		}
		stations = new boolean[numRowStations][numColStations];
		for (int i = 0 ; i < numRowStations ; i++) {
			for (int j = 0 ; j < numColStations ; j++) {
				stations[i][j] = false;
			}
		}
		this.setLayout(new BorderLayout());
		createButtonPanel();
		createApPanel();
		createStaPanel();

		splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane2.setLeftComponent(apPanel);
		splitPane2.setRightComponent(staPanel);
		splitPane2.setDividerSize(0);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setLeftComponent(buttonPanel);
		splitPane.setRightComponent(splitPane2);
		splitPane.setDividerSize(0);

		this.add(splitPane, BorderLayout.NORTH);
	}	

	public void createButtonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		join = new JButton("Join network");
		join.addActionListener(this);
		buttonPanel.add(join, BorderLayout.WEST);
	}

	public void createApPanel() {
		apPanel = new JPanel();
		apPanel.setPreferredSize(new Dimension(100,100));
		apPanel.setMinimumSize(new Dimension(100,100));
		apPanel.setMaximumSize(new Dimension(100,100));
		apPanel.setLayout(new GridLayout(1, numColAccessPoints));
		DevicePanel devicePanel = new DevicePanel(address, routerImage, ACCESS_POINT);
		apPanelHolder.add(devicePanel);
		apPanel.add(devicePanel);
	}

	public void createStaPanel() {
		staPanel = new JPanel();
		staPanel.setLayout(new GridLayout(numRowStations, numColStations));
		//empty panels
		panelHolder = new DevicePanel[numRowStations][numColStations];
		for(int m = 0; m < numRowStations; m++) {
			for(int n = 0; n < numColStations; n++) {
				panelHolder[m][n] = new DevicePanel(null, notebookImage, STATION);
				staPanel.add(panelHolder[m][n]);
			}
		}
		//single thread for updating both panels
		apThread = new AccessPointsStats();
		new Thread(apThread).start();
	}


	public void destroy() {
		remove(staPanel);
		remove(apPanel);
		remove(buttonPanel);
		apThread.running = false;
		revalidate();
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (join.getText().equals("Disconnect")) {
			String[] joinNetworkString = {"/usr/bin/sudo", "-n", System.getProperty("user.dir") + "/resources/network.sh", "disconnect"};
			joinNetwork = null;
			try {
				joinNetwork = new ProcessBuilder(joinNetworkString).start();
				ip = new HashMap<String, String>();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			while (true) {
				try {
					if (joinNetwork.exitValue() == 0) {
						break;
					}
				} catch (IllegalThreadStateException exeption) {
				}
			}

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
				} catch (IllegalThreadStateException exception) {
				}
			}
			join.setText("Join Network");
			join.revalidate();
			join.repaint();
			return;
		}

		if (Network.getAccessPoints().get(address).getSSID() == null) {
			JOptionPane.showMessageDialog(this, "SSID for this network is null");
			return;
		}

		Network.getAccessPoints().get(address).setDelete(false);
		TreeSet<String> newSet = new TreeSet<String>(Network.getAccessPoints().get(address).getAssociatedStations().keySet());
		for(String address : newSet) {
			Network.getStations().get(address).setDelete(false);
		}

		for (int i = 0 ; i < 13 ; i++) {
			if (AccessPointsTab.boxes.get(i).isSelected()) {
				AccessPointsTab.boxes.get(i).setSelected(false);
			}
		}

		if (Network.getAccessPoints().get(address).getEncryption().equals(AccessPoint.NO_ENCRYPTION)) {
			String[] joinNetworkString = {"/usr/bin/sudo", "-n", System.getProperty("user.dir") + "/resources/network.sh", "connect", Network.getAccessPoints().get(address).getSSID()};
			joinNetwork = null;
			try {
				joinNetwork = new ProcessBuilder(joinNetworkString).start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			while (true) {
				try {
					if (joinNetwork.exitValue() == 0) {
						break;
					}
				} catch (IllegalThreadStateException exeption) {
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			Enumeration<InetAddress> addresses = Application.networkInterface.getInetAddresses();
			boolean connected = false;
			while(addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				String ip = addr.getHostAddress();
				//ipv6 does not count
				if (!ip.startsWith("fe")) {
					connected = true;
				}
			}
			if (connected) {
				JOptionPane.showMessageDialog(context, "Successfully joined network " + Network.getAccessPoints().get(address).getSSID());
				join.setText("Disconnect");
				buttonPanel.revalidate();
				buttonPanel.repaint();
			}
			else {
				JOptionPane.showMessageDialog(context, "Could not join network " + Network.getAccessPoints().get(address).getSSID());
			}

		}
		else {
			if (Network.getAccessPoints().get(address).getEncryption().equals(AccessPoint.WPA_ENCRYPTION)) {				
				new AddValueFrame();
			}
		}

	}

	class AccessPointsStats implements Runnable {
		private Timer timer;
		private boolean running = true;

		public AccessPointsStats() {
			timer = new Timer();
		}

		public void setRunning(boolean running) {
			this.running = running;
		}

		@Override
		public void run() {
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (running) {
						removeAddAccessPoints();
						removeInvalidStations();
					}
					else {
						timer.cancel();
						timer.purge();
					}
				}
			}, 1000, REFRESH_RATE * 1000);
		}

		public void removeAddAccessPoints() {
			AccessPoint ap = Network.getAccessPoints().get(address);
			if (ap == null) {
				running = false;
				destroy();
				return;
			}
			if (ap.getSSID() == null) {
				return;
			}
			//clear panel
			int size = apPanelHolder.size();
			for (int i = 0 ; i < size ; i++) {
				apPanel.remove(apPanelHolder.get(i));
			}
			for (int i = 0 ; i < size ; i++) {
				if (Network.getAccessPoints().get(apPanelHolder.get(i).address) == null) {
					apPanelHolder.remove(i);
					size--;
					i--;
				}
			}

			//add new elements
			TreeSet<String> newSet = new TreeSet<String>(Network.getAccessPoints().keySet());
			for (String newAddress : newSet) {
				if (Network.getAccessPoints().get(newAddress).getSSID() != null && Network.getAccessPoints().get(newAddress).getSSID().equals(ap.getSSID())) {
					// ap grid is full
					if (apPanelHolder.size() == numColAccessPoints) {
						break;
					}
					boolean t = false;
					for (int i = 0 ; i < apPanelHolder.size() ; i++) {
						if (newAddress.equals(apPanelHolder.get(i).address)) {
							t = true;
							break;
						}
					}
					if (t == false) {
						apPanelHolder.add(new DevicePanel(newAddress, routerImage, ACCESS_POINT));
					}
				}
			}
			//redraw panel
			for (int i = 0 ; i < apPanelHolder.size(); i++) {
				apPanel.add(apPanelHolder.get(i));
				apPanel.revalidate();
				apPanel.repaint();
			}			
		}

		private void removeInvalidStations() {

			//remove details window for the stations that left
			for (int i = 0 ; i < numRowStations ; i++) {
				for (int j = 0 ; j < numColStations ; j++) {
					if (stations[i][j]) {
						boolean t = true;
						for (int k = 0 ; k < apPanelHolder.size() ; k++) {
							try {
								if (!(Network.getAccessPoints().get(apPanelHolder.get(k).address).getAssociatedStations().size() == 0 || Network.getAccessPoints().get(apPanelHolder.get(k).address).getAssociatedStations().get(panelHolder[i][j].address) == null)) {
									t = false;
								}
							} catch (NullPointerException e) {

							}
						}
						if (t == true) {
							panelHolder[i][j].destroy();
							stations[i][j] = false;
						}
					}
				}
			}

			//redraw station panel
			for (int i = 0 ; i < numRowStations ; i++) {
				for (int j = 0 ; j < numColStations ; j++) {
					if (stations[i][j]) {
						panelHolder[i][j].remove(panelHolder[i][j].imagePanel);
						panelHolder[i][j].remove(panelHolder[i][j].descriptionPanel);
						panelHolder[i][j].remove(panelHolder[i][j].speedPanel);
						stations[i][j] = false;
					}
				}
			}

			for (int k = 0 ; k < apPanelHolder.size() ; k++) {
				TreeSet<String> newSet = null;
				try {
					newSet = new TreeSet<String>(Network.getAccessPoints().get(apPanelHolder.get(k).address).getAssociatedStations().keySet());
				} catch(NullPointerException e) {
					break;
				}
				for (String staAddress : newSet) {
					boolean t = false;
					for (int i = 0 ; i < numRowStations ; i++) {
						for (int j = k * 2 ; j < k * 2 + 2 ; j++) {
							if (!stations[i][j]) {
								stations[i][j] = true;
								panelHolder[i][j].init(staAddress);
								Station sta = Network.getStations().get(staAddress);
								panelHolder[i][j].updateSpeedPanel(sta.getBytesSent(), sta.getBytesReceived(), REFRESH_RATE);
								System.out.println(sta.getBytesReceived());
								if (sta.getPowerManagementCount() > 5) {
									panelHolder[i][j].changeImage(phoneImage);
								}
								t = true;
								break;
							}
						}
						if (t) {
							break;
						}
					}
				}
			}	
			staPanel.revalidate();
			staPanel.repaint();
		}
	}


	class DevicePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public String address = null;
		private String image = null;
		public ImagePanel imagePanel = null;
		public JPanel descriptionPanel = null;
		private Details details = null;
		private int type = 0;
		public JPanel speedPanel = null;
		public JLabel uploadSpeedLabel = null;
		public JLabel downloadSpeedLabel = null;
		public int prevUploadBytes = 0;
		public int prevDownloadBytes = 0;

		DevicePanel(String address, String image, int type) {
			super();
			if (address != null) {
				this.type = type;
				this.address = address;
				this.image = image;
				this.setLayout(new BorderLayout());
				createImagePanel();
				createDescriptionPanel();
			}
			this.type = type;
		}

		public void init(String address) {
			this.address = address;
			this.setLayout(new BorderLayout());
			createImagePanel();
			createDescriptionPanel();
			createSpeedPanel();
		}

		public void destroy() {
			remove(imagePanel);
			remove(descriptionPanel);
			remove(speedPanel);
			if (details != null) {
				details.setVisible(false);
				details.dispose();
			}
			revalidate();
			repaint();
			address = null;
			imagePanel = null;
			descriptionPanel = null;
		}

		public void changeImage(String image) {
			this.image = image;
			imagePanel.changeImage(image);
			imagePanel.revalidate();
			imagePanel.repaint();
		}

		public void createImagePanel() {
			if (image == null)
				imagePanel = new ImagePanel(notebookImage);
			else {
				imagePanel = new ImagePanel(image);
			}
			imagePanel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						details = new Details(address, address);
						details.init(type);
					}
				}
			});
			add(imagePanel, BorderLayout.NORTH);
			imagePanel.revalidate();
			imagePanel.repaint();
		}

		public void createDescriptionPanel() {
			descriptionPanel = new JPanel();
			descriptionPanel.setLayout(new BorderLayout());
			JLabel addressLabel = new JLabel(address);
			descriptionPanel.add(addressLabel, BorderLayout.WEST);
			add(descriptionPanel, BorderLayout.CENTER);
			descriptionPanel.revalidate();
			descriptionPanel.repaint();
		}

		public void createSpeedPanel() {
			speedPanel = new JPanel();
			speedPanel.setLayout(new BorderLayout());
			
			ImagePanel upload = new ImagePanel("/resources/arrow_up.png");
			ImagePanel download = new ImagePanel("/resources/arrow_down.png");
			JPanel speeds = new JPanel();
			speeds.setLayout(new BorderLayout());
			uploadSpeedLabel = new JLabel("0.0 MB/s", SwingConstants.LEFT);
			downloadSpeedLabel = new JLabel("0.0 MB/s", SwingConstants.RIGHT);
			Font f = uploadSpeedLabel.getFont();
			uploadSpeedLabel.setFont(new Font(f.getName(),f.getStyle(), 10));
			downloadSpeedLabel.setFont(new Font(f.getName(),f.getStyle(), 10));
			speeds.add(uploadSpeedLabel, BorderLayout.NORTH);
			speeds.add(downloadSpeedLabel, BorderLayout.SOUTH);
			
			speedPanel.add(speeds, BorderLayout.CENTER);
			speedPanel.add(upload, BorderLayout.WEST);
			speedPanel.add(download, BorderLayout.EAST);
			
			add(speedPanel, BorderLayout.SOUTH);
		}
		
		public void updateSpeedPanel(int uploadBytes, int downloadBytes, int seconds) {
			float uploadSpeed = ((float)(uploadBytes - prevUploadBytes)) / seconds / 1024 / 1024;
			DecimalFormat df = new DecimalFormat("0.000");
			String uResult = df.format(uploadSpeed);
			prevUploadBytes = uploadBytes;
			float downloadSpeed = ((float)(downloadBytes - prevDownloadBytes)) / seconds / 1024 / 1024;
			String dResult = df.format(downloadSpeed);
			prevDownloadBytes = downloadBytes;
			uploadSpeedLabel.setText(uResult.toString() + " MB/s");
			downloadSpeedLabel.setText(dResult.toString() + " MB/s");
			speedPanel.revalidate();
			speedPanel.repaint();
		}
		
	}

	class ImagePanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private BufferedImage image;
		public int power = 0;

		public ImagePanel(String imagePath) {
			try {                
				image = ImageIO.read(new File(System.getProperty("user.dir") + imagePath));
				this.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
				this.setMinimumSize(new Dimension(image.getWidth(),image.getHeight()));
				this.setMaximumSize(new Dimension(image.getWidth(),image.getHeight()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void changeImage(String imagePath) {
			try {
				image = ImageIO.read(new File(System.getProperty("user.dir") + imagePath));
				this.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
				this.setMinimumSize(new Dimension(image.getWidth(),image.getHeight()));
				this.setMaximumSize(new Dimension(image.getWidth(),image.getHeight()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image, 0, 0, null);          
		}
	}

	class AddValueFrame extends JFrame implements ActionListener {
		private static final long serialVersionUID = 1L;
		JTextField field;
		String ipAddress = null;
		AddValueFrame() {
			super();
			setSize(200, 200);
			setLayout(new BorderLayout());
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);

			JLabel propertyLabel = new JLabel("Password", null, JLabel.LEFT);
			field = new JTextField(20);
			JButton confirm = new JButton("Done");
			confirm.addActionListener(this);

			add(propertyLabel, BorderLayout.NORTH);
			add(field, BorderLayout.CENTER);
			add(confirm, BorderLayout.SOUTH);

			pack();
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			if (field.getText() != null && !field.getText().equals("")) {
				password = field.getText();
				//close frame
				setVisible(false);
				dispose();
				//connect to the network
				String[] joinNetworkString = {"/usr/bin/sudo", "-n", System.getProperty("user.dir") + "/resources/network.sh", "connect", "WPA", Network.getAccessPoints().get(address).getSSID(), password}; 
				joinNetwork = null;
				try {
					joinNetwork = new ProcessBuilder(joinNetworkString).start();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				while (true) {
					try {
						if (joinNetwork.exitValue() == 0) {
							break;
						}
					} catch (IllegalThreadStateException exeption) {
					}
				}

				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
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
				Enumeration<InetAddress> addresses = Application.networkInterface.getInetAddresses();
				boolean connected = false;
				while(addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					String ip = addr.getHostAddress();
					//ipv6 does not count
					if (!ip.startsWith("fe")) {
						connected = true;
						ipAddress = ip;
					}
				}
				if (connected) {
					JOptionPane.showMessageDialog(context, "Successfully joined network " + Network.getAccessPoints().get(address).getSSID());
					join.setText("Disconnect");
					buttonPanel.revalidate();
					buttonPanel.repaint();
					
					TreeSet<String> newSet = new TreeSet<String>(Network.getAccessPoints().get(address).getAssociatedStations().keySet());
					ip.put(address, "dummy");
					for(String address : newSet) {
						ip.put(address, "dummy");
					}

					String[] ipSplit = ipAddress.split("\\.");
					Nmap nmap = new Nmap("match", ipSplit[0] + "." + ipSplit[1] + "." + ipSplit[2] + ".1-255", Network.getAccessPoints().get(address).getSSID());
					new Thread(nmap).start();
				}
				else {
					JOptionPane.showMessageDialog(context, "Could not join network " + Network.getAccessPoints().get(address).getSSID());
				}
			}
			else {
				JOptionPane.showMessageDialog(this, "Password cannot be null");
			}			
		}
	}
	
	class Nmap implements Runnable {

		private String mode = null;
		private String arg = null;
		private boolean running = true;
		private Process nmapProcess = null;
		private String hostName = null;
		private String os = null;
		private String osDetails = null;
		private String SSID = null;
		
		Nmap(String mode, String arg, String SSID) {
			this.mode = mode;
			this.arg = arg;
			this.SSID = SSID;
		}
		
		public void stopProcess() {
			nmapProcess.destroy();
		}
		
		public void setRunning(boolean running) {
			this.running = running;
		}
		
		public void setMode(String mode) {
			this.mode = mode;
		}
		
		public void setArg(String arg) {
			this.arg = arg;
		}
		
		@Override
		public void run() {
			if (arg == null || mode ==null) {
				return;
			}
			
			String[] nmapCmd = {"/usr/bin/sudo", "-n", System.getProperty("user.dir") + "/resources/nmap.sh", mode, arg};
			try {
				nmapProcess = new ProcessBuilder(nmapCmd).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        BufferedReader in = new BufferedReader(new InputStreamReader(nmapProcess.getInputStream()));
	        String nmapOut = null;
	        String ipAddress = null;
			String macAddress = null;
	        try {
				while (running && (nmapOut = in.readLine()) != null ) {
					System.out.println(nmapOut);
					if (nmapOut.startsWith("Nmap scan report for")) {
						String[] split = nmapOut.split(" ");
						if (Character.isDigit(split[4].charAt(0))) {
							ipAddress = split[4];
						}
					}
					if (nmapOut.startsWith("MAC Address:")) {
						String[] split = nmapOut.split(" ");
						macAddress = split[2];
						macAddress = macAddress.substring(0, 2) + macAddress.substring(3, 5) + macAddress.substring(6, 8) + macAddress.substring(9, 11) + macAddress.substring(12, 14) + macAddress.substring(15);
						macAddress = macAddress.toLowerCase();
						if (ip.get(macAddress) != null) {
							ip.put(macAddress, ipAddress);
						}
					}
				}
				in.close();
				nmapProcess.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        for (String address : ip.keySet()) {
	        	if (ip.get(address) == null) {
	        		return;
	        	}
	        	
	        	String[] nmapCmdNew = {"/usr/bin/sudo", "-n", System.getProperty("user.dir") + "/resources/nmap.sh", "details", ip.get(address)};
				try {
					nmapProcess = new ProcessBuilder(nmapCmdNew).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
		        in = new BufferedReader(new InputStreamReader(nmapProcess.getInputStream()));
		        nmapOut = null;
		        try {
					while (running && (nmapOut = in.readLine()) != null ) {
						System.out.println(nmapOut);
						if (nmapOut.startsWith("Nmap scan report for")) {
							String[] split = nmapOut.split(" ");
							if (split.length == 6) {
								hostName = split[5];
							}
						}
						if(nmapOut.startsWith("Running:")) {
							String[] split = nmapOut.split(":,");
							if (split.length > 1) {
								os = split[1];
							}
						}
						if(nmapOut.startsWith("Aggressive OS")) {
							String[] split = nmapOut.split(":,");
							os = split[3];
						}
						if(nmapOut.startsWith("OS details:")) {
							String[] split = nmapOut.split(":,");
							for (int i = 0 ; i < split.length ; i++) {
								if (split[i].startsWith(" Android")) {
									osDetails = split[i];
									break;
								}
							}
							if (osDetails == null) {
								osDetails = split[2];
							}
						}
					}
					in.close();
					nmapProcess.destroy();
					//write everything to DB
					//hostName
					ArrayList<String> entry = new ArrayList<String>();
					entry.add(address);
					entry.add("Host Name");
					entry.add(hostName);
					DataBaseConnectivity.addProperty(entry);
					
					//IP
					entry = new ArrayList<String>();
					entry.add(address);
					entry.add("IP Address");
					entry.add(ipAddress);
					DataBaseConnectivity.addProperty(entry);
					
					//SSID
					entry = new ArrayList<String>();
					entry.add(address);
					entry.add("SSID");
					entry.add(SSID);
					DataBaseConnectivity.addProperty(entry);
					
					//os
					entry = new ArrayList<String>();
					entry.add(address);
					entry.add("Operating System");
					entry.add(os);
					DataBaseConnectivity.addProperty(entry);
					
					//osDetails
					entry = new ArrayList<String>();
					entry.add(address);
					entry.add("Operating System Details");
					entry.add(osDetails);
					DataBaseConnectivity.addProperty(entry);
		        } catch(Exception e) {
	        	
		        }
	        }
		}
	}
	
}

