package graphic;


import graphic.AccessPointsTab.PA.PreviousValues;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import client.AccessPoint;
import client.Application;
import client.Channel;
import client.Network;
import client.Station;
import database.DataBaseConnectivity;
import processing.core.PApplet;

public class AccessPointsTab extends JPanel implements ItemListener {

	private static final long serialVersionUID = 1L;
	private static final int REFRESH_RATE = 2;
	private JSplitPane splitPaneV1 = null;
	private JSplitPane splitPaneV2 = null;
	private JPanel panel1 = null;
	public static ArrayList<JCheckBox> boxes = null;
	private JPanel panel2 = null;
	JScrollPane tableContainer = null;
	private boolean initPanel2 = false;
	private JPanel panel3 = null;
	private int selectedChannel = -1;
	private PA chart = null;
	private JTable table = null;
	private AccessPointStats apThread = null;
	public ImagePanel image = null;

	public AccessPointsTab() {
		super();

		this.setLayout(new BorderLayout());

		// Create the panels
		createPanel1();
		createPanel2();
		createPanel3();

		// Create a splitter pane
		splitPaneV1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.add(splitPaneV1);

		splitPaneV2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPaneV2.setLeftComponent(panel1);
		splitPaneV2.setRightComponent(panel2);

		splitPaneV1.setLeftComponent(splitPaneV2);
		splitPaneV1.setRightComponent(panel3);
	}

	public void createPanel1(){
		panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		panel1.setPreferredSize(new Dimension(600,125));
		panel1.setMinimumSize(new Dimension(600,125));
		panel1.setMaximumSize(new Dimension(600,125));

		image = new ImagePanel("/resources/startButton.jpg");
		image.setToolTipText("start monitoring");
		panel1.add(image, BorderLayout.NORTH);
		image.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				//application is not in monitoring mode
				if (image.getImagePath() == null || image.getImagePath().equals("/resources/startButton.jpg")) {
					boolean channel = false;
					for (int i = 0 ; i < boxes.size(); i++) {
						if (boxes.get(i).isSelected()) {
							Application.setChannel(i+1);
							channel = true;
						}
					}
					if (channel) {
						synchronized (Application.getLock()) {
							Application.getLock().notifyAll();
						}
						image.changeImage("/resources/stopButton.jpg");
						image.setToolTipText("stop monitoring");
						splitPaneV2.remove(panel2);
						splitPaneV1.remove(panel3);
						if (apThread != null) {
							apThread.setRunning(false);
						}
						Network.setAccessPoints(new HashMap<String, AccessPoint>());
						Network.setStations(new HashMap<String, Station>());
						revalidate();
						createPanel2();
						createPanel3();
						splitPaneV2.setRightComponent(panel2);
						splitPaneV1.setRightComponent(panel3);
						revalidate();
						repaint();
					}
					else {
						JOptionPane.showMessageDialog(panel1, "You must choose a channel!");
					}
				}
				//application is monitoring
				else {
					image.changeImage("/resources/startButton.jpg");
					image.setToolTipText("start monitoring");
					revalidate();
					repaint();
					Application.stopMonitoring();
				}

			}
		});

		JLabel description = new JLabel("Channel filter: ", null, JLabel.LEFT);
		panel1.add(description, BorderLayout.WEST);

		//add checkboxes for channel filtering
		JPanel checkPanel = new JPanel(new GridLayout(1,13));

		boxes = new ArrayList<JCheckBox>();
		for (int i = 0 ; i < 13 ; i++) {
			JCheckBox checkBox = new JCheckBox(Integer.toString(i + 1));
			checkBox.addItemListener(this);
			checkPanel.add(checkBox);
			boxes.add(checkBox);
		}
		
		panel1.add(checkPanel, BorderLayout.CENTER);
	}

	public void itemStateChanged(ItemEvent e) {
		JCheckBox target = (JCheckBox)e.getSource();
		if (target.isSelected()) {
			for (int i = 0 ; i < 13 ; i++) {
				if (boxes.get(i) == target) {
					selectedChannel = i;
				}
			}
			
			for (int i = 0 ; i < 13 ; i++) {
				if (boxes.get(i) != target && boxes.get(i).isSelected()) {
					boxes.get(i).setSelected(false);
				}
			}
			if (image.getImagePath().equals("/resources/stopButton.jpg")) {
				image.changeImage("/resources/startButton.jpg");
				Application.stopMonitoring();
				Application.setChannel(selectedChannel + 1);
				apThread.setRunning(false);
				chart.clear();
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				int rowCount = model.getRowCount();
				for (int i = 0; i < rowCount; ) {
				    model.removeRow(i);
				    rowCount--;
				}
				model.fireTableDataChanged();
				revalidate();
				repaint();
			}
		}
		else {
			for (int i = 0 ; i < 13 ; i++) {
				if (boxes.get(i) == target && i == selectedChannel) {
					if (image.getImagePath().equals("/resources/stopButton.jpg")) {
						image.changeImage("/resources/startButton.jpg");
						Application.stopMonitoring();
						apThread.setRunning(false);
						chart.clear();
						DefaultTableModel model = (DefaultTableModel) table.getModel();
						int rowCount = model.getRowCount();
						for (int j = 0; j < rowCount; ) {
						    model.removeRow(j);
						    rowCount--;
						}
						model.fireTableDataChanged();
						revalidate();
						repaint();
					}
					break;
				}
			}
		}
	}

	public void createPanel2(){
		panel2 = new JPanel();
		panel2.setLayout(new BorderLayout());
		panel2.setPreferredSize(new Dimension(600,200));
		panel2.setMinimumSize(new Dimension(600,200));
		panel2.setMaximumSize(new Dimension(600,200));
		//create table for AP info

		table = new JTable(new DefaultTableModel(TableColumns.columnNames,0)) {
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				//  Alternate row color
				if (!isRowSelected(row))
					//c.setBackground(row % 2 == 0 ? getBackground() : new Color(0,0,0));
					c.setBackground(new Color(0,0,0));

				return c;
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Class getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		table.setDefaultRenderer(Object.class, new MyCellRenderer());
		tableContainer = new JScrollPane(table);
		/*
		 * start thread to populate the table
		 */
		apThread = new AccessPointStats();
		new Thread(apThread).start();

		/*
		 * add listener on table rows
		 */
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable)e.getSource();
					int row = target.getSelectedRow();
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					ExtendedDetails d = new ExtendedDetails((String)model.getValueAt(row, TableColumns.SSID), (String)model.getValueAt(row, TableColumns.MAC));
					d.init(TopologyTab.ACCESS_POINT);
				}
			}
		});

		panel2.add(tableContainer, BorderLayout.CENTER);
	}

	public void addRow(String[] values) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.addRow(new Object[]{Boolean.parseBoolean(values[0]), values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12]});
	}

	public void updateRow(String[] values, int row) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setValueAt(Boolean.parseBoolean(values[0]), row, 0);
		for(int i = 1 ; i < TableColumns.columnNames.length ; i++) {
			if (values[i] == null) {
				model.setValueAt("Unknown", row, i);
			}
			else {
				if (!values[i].equals("noUpdate")){
					model.setValueAt(values[i], row, i);
				}
			}
		}
	}

	public void deleteRow(int row) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.removeRow(row);
	}

	public void createPanel3(){
		panel3 = new JPanel();
		panel3.setLayout(new BorderLayout());
		panel3.setPreferredSize(new Dimension(600,350));
		panel3.setMinimumSize(new Dimension(600,350));
		panel3.setMaximumSize(new Dimension(600,350));
		chart = new PA(1300, 350);
		panel3.add(chart);
	}

	public void removeUnmarked() {
		TreeSet<String> newSet = new TreeSet<String>(Network.getAccessPoints().keySet());
		for(String address : newSet) {
			if (Network.getAccessPoints().get(address).isDelete()) {
				Network.getAccessPoints().remove(address);
			}
		}
		newSet = new TreeSet<String>(Network.getStations().keySet());
		for(String address : newSet) {
			if (Network.getStations().get(address).isDelete()) {
				Network.getStations().remove(address);
			}
		}
	}
	
	class PA extends PApplet {

		private static final long serialVersionUID = 1L;
		int w, h;
		HashMap<String, PreviousValues> accessPoints;
		//common
		float prevX = 20;
		float distanceX = 10;
		Color color;

		public PA(int w, int h) {
			super();
			this.w = w;
			this.h = h;
			accessPoints = new HashMap<String, PreviousValues>();
			color = new Color();
			init();
		}

		public void setup() {
			size(w, h);
			frameRate((float)1/REFRESH_RATE); //To plot the graph at 1 point per second
			drawStuff();
			background(0);
		}

		public void draw() {
			strokeWeight(1);
			drawStuff();
			strokeWeight(4);
			fill(170);
			if (initPanel2) {
				for (String address : Network.getAccessPoints().keySet()) {
					if (accessPoints.get(address) == null) {
						float y = ((float)(h-15)/80) * (Network.getAccessPoints().get(address).getSignal() * (-1) - 20 );
						accessPoints.put(address, new PreviousValues(color.nextColor(), y));
						DefaultTableModel model = (DefaultTableModel) table.getModel();
						model.fireTableDataChanged();
					}
					else {
						float y = ((float)(h-15)/80) * (Network.getAccessPoints().get(address).getSignal() * (-1) - 20);
						String color[] = accessPoints.get(address).getColor().split(":");
						stroke(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
						line(prevX, accessPoints.get(address).getPrevY(), prevX + distanceX, y);
						accessPoints.get(address).setPrevY(y);
					}
				}
				prevX += distanceX;
			}
		}

		void drawStuff() {
			for (int i = 30; i <= width; i += (width - 30) / ((width - 30) / (5 * REFRESH_RATE * distanceX))) {
				fill(0, 255, 0);
				text(10 * (int)((i-30) / ((width - 30) / ((width - 30) / (5 * REFRESH_RATE * distanceX)))), i-10, height);
				stroke(255);
				line(i, height-15, i, 0);
			}
			for (int j = 0; j < height - 15; j += (height-15)/8) {
				fill(0, 255, 0);
				text((int)(10 * (-j/(float)((height-15)/8) - 2)), 0, j);
				stroke(255);
				line(25, j, width, j);
			}
		}

		class Color {
			int r = 0;
			int g = 0;
			int b = 0;
			int contor = 0;

			String[] ColorValues = new String[] { 
					"255:0:0", "0:255:0", "0:0:255", "255:255:0", "255:0:255", "0:255:255", "255:255:255", 
					"128:0:0", "0:128:0", "0:0:128", "128:128:0", "128:0:128", "0:128:128", "128:128:128", 
					"64:0:0", "0:64:0", "0:0:64", "64:64:0", "64:0:64", "0:64:64", "64:64:64", 
					"32:0:0", "0:32:0", "0:0:32", "32:32:0", "32:0:32", "0:32:32", "32:32:32"
			};

			String nextColor() {
				contor++;
				return ColorValues[(contor-1) % ColorValues.length];
			}
		}

		class PreviousValues {
			float prevY = 0;
			String color = "0:0:0";

			PreviousValues(String color, float y) {
				this.color = color;
				prevY = y;
			}

			public float getPrevY() {
				return prevY;
			}
			public void setPrevY(float prevY) {
				this.prevY = prevY;
			}
			public String getColor() {
				return color;
			}
			public void setColor(String color) {
				this.color = color;
			}
		}

	}

	class AccessPointStats implements Runnable {

		private Timer timer;
		private boolean running = true;

		public AccessPointStats() {
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
						updateAccessPoints();
					}
					else {
						timer.cancel();
						timer.purge();
					}
				}
			}, 1000, REFRESH_RATE * 1000);
		}

		private void updateAccessPoints() {
			try {
				TreeSet<String> newSet = new TreeSet<String>(Network.getAccessPoints().keySet());
				for(String address : newSet) {
					AccessPoint ap = Network.getAccessPoints().get(address);
					if (table.getModel().getRowCount() == 0) {
						//add row
						if (!DataBaseConnectivity.checkTable(DataBaseConnectivity.VENDOR_TABLE)) {
							DataBaseConnectivity.createVendorTable();
						}
						String vendor = DataBaseConnectivity.getVendor(address);
						ap.setVendor(vendor);
						String channel = Channel.get(ap.getChannelFrequency());
						double loss = 0;
						if (ap.getPacketsReceived() > 0) {
							loss = ((double)ap.getPacketsLost() / (ap.getPacketsReceived() + ap.getPacketsLost())) * 100;
							loss = (double)Math.round(loss * 100) / 100;
						}
						addRow(new String[]{String.valueOf(true), ap.getSSID(), ap.getAddress(), vendor, channel, String.valueOf(ap.getSignal()), String.valueOf(ap.getPacketsSent()), String.valueOf(ap.getBytesSent()/2), String.valueOf(ap.getPacketsReceived()), String.valueOf(ap.getBytesReceived()/2), String.valueOf(loss), String.valueOf(ap.getAssociatedStations().size()), String.valueOf(ap.isInRange())});
					}
					else {
						boolean found = false;
						for (int i = 0 ; i < table.getModel().getRowCount() ; i++) {
							if (table.getModel().getValueAt(i, TableColumns.MAC).equals(address)) {
								if (ap != null) {
									//update row
									double loss = 0;
									if (ap.getPacketsReceived() > 0) {
										loss = ((double)ap.getPacketsLost() / (ap.getPacketsSent() + ap.getPacketsLost())) * 100;
										loss = (double)Math.round(loss * 100) / 100;
									}
									updateRow(new String[]{String.valueOf(true), ap.getSSID(), "noUpdate", "noUpdate", "noUpdate", String.valueOf(ap.getSignal()), String.valueOf(ap.getPacketsSent()), String.valueOf(ap.getBytesSent()/2), String.valueOf(ap.getPacketsReceived()), String.valueOf(ap.getBytesReceived()/2), String.valueOf(loss), String.valueOf(ap.getAssociatedStations().size()), String.valueOf(ap.isInRange())} ,i);
								}
								else {
									//remove row
									deleteRow(i);
								}
								if (!newSet.contains(table.getModel().getValueAt(i, TableColumns.MAC))) {
									deleteRow(i);
								}
								found = true;
								break;
							}
						}
						if (!found) {
							//add row
							String vendor = DataBaseConnectivity.getVendor(address);
							ap.setVendor(vendor);
							String channel = Channel.get(ap.getChannelFrequency());
							double loss = 0;
							if (ap.getPacketsReceived() > 0) {
								loss = ((double)ap.getPacketsLost() / ap.getPacketsSent() + ap.getPacketsLost()) * 100;
								loss = (double)Math.round(loss * 100) / 100;
							}
							addRow(new String[]{String.valueOf(true), ap.getSSID(), ap.getAddress(), vendor, channel, String.valueOf(ap.getSignal()), String.valueOf(ap.getPacketsSent()), String.valueOf(ap.getBytesSent()/2), String.valueOf(ap.getPacketsReceived()), String.valueOf(ap.getBytesReceived()/2), String.valueOf(loss), String.valueOf(ap.getAssociatedStations().size()), String.valueOf(ap.isInRange())});
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			initPanel2 = true;
		}
	}

	class ImagePanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private BufferedImage image;
		private String imagePath = null;

		public ImagePanel(String imagePath) {
			try {   
				this.imagePath = imagePath;
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
		
		public void changeImage(String imagePath) {
			this.imagePath = imagePath;
			try {
				image = ImageIO.read(new File(System.getProperty("user.dir") + imagePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
			this.setMinimumSize(new Dimension(image.getWidth(),image.getHeight()));
			this.setMaximumSize(new Dimension(image.getWidth(),image.getHeight()));
		}
		
		public String getImagePath() {
			return imagePath;
		}
		
	}

	interface TableColumns {
		static final String[] columnNames = {"Plot", "SSID", "MAC", "Vendor", "Channel", "Signal", "Packets Sent", "Bytes sent", "Packets Received", "Bytes Received", "Loss", "Stations", "In Range"};
		static final int PLOT = 0;
		static final int SSID = 1;
		static final int MAC = 2;
		static final int VENDOR = 3;
		static final int CHANNEL = 4;
		static final int SIGNAL = 5;
		static final int PACKETS_SENT = 6;
		static final int BYTES_SENT = 7;
		static final int PACKETS_RECEIVED = 8;
		static final int BYTES_RECEIVED = 9;
		static final int LOSS = 10;
		static final int STATIONS = 11;
		static final int IN_RANGE = 12;
	}
	
	class MyCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, java.lang.Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final java.awt.Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            cellComponent.setFont(new Font(cellComponent.getFont().getName(), Font.BOLD, cellComponent.getFont().getSize()));
            
            String val = (String)table.getValueAt(row, 2);
            PreviousValues p;
            if (chart != null && chart.accessPoints != null && chart.accessPoints.get(val) != null) {
            	p = chart.accessPoints.get(val);
            	String color[] = p.getColor().split(":");
            	cellComponent.setForeground(new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
            }
            else {
            	cellComponent.setForeground(new Color(0,0,0));
            }
            
            return cellComponent;
        }
    }
	
}
