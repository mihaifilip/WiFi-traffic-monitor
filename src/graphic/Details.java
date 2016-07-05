package graphic;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import client.AccessPoint;
import client.Channel;
import client.Network;
import client.Station;
import database.DataBaseConnectivity;

public class Details extends JFrame implements ActionListener {

	protected static final long serialVersionUID = 1L;
	protected String address = null;
	protected JButton topology = null;
	protected JPanel panel = null;
	protected DetailsPanel detailsPanel = null;
	protected DataBasePanel dataBasePanel = null;
	protected JTable detailsTable = null;
	protected JTable dataBaseTable = null;
	protected JPanel buttonPanel = null;
	protected AccessPointDetails apDetails = null;
	protected StationDetails staDetails = null;
	protected JScrollPane pane = null;
	protected JScrollPane dbPane = null;


	public Details(String title, String address) {
		super(title);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (apDetails != null) {
					apDetails.stop();
				}
				if (staDetails != null) {
					staDetails.stop();
				}
				super.windowClosing(windowEvent);
			}
		});

		this.address = address;
		setSize(250, 400);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);

		AccessPoint ap = Network.getAccessPoints().get(address);
		Station sta = Network.getStations().get(address);
		if (ap == null && sta == null) {
			return;
		}

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		add(panel);
		createDetailsPanel();
		createDataBasePanel();

		pack();
	}

	public void init(int type) {
		if (type == TopologyTab.ACCESS_POINT) {
			apDetails = new AccessPointDetails();
			new Thread(apDetails).start();
		}
		else {
			if (type == TopologyTab.STATION) {
				staDetails = new StationDetails();
				new Thread(staDetails).start();
			}
		}
	}

	private void createDetailsPanel() {
		detailsPanel = new DetailsPanel();
		panel.add(detailsPanel, BorderLayout.NORTH);
	}

	private void createDataBasePanel() {
		dataBasePanel = new DataBasePanel();
		panel.add(dataBasePanel, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		GUI.addTopologyTab(address, Network.getAccessPoints().get(address).getSSID());
		/*
		 * close the frame
		 */
		setVisible(false);
		dispose();
	}

	class DetailsPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		DetailsPanel() {
			super();
			setLayout(new BorderLayout());
			JLabel description = new JLabel("Live details: ", null, JLabel.LEFT);
			add(description, BorderLayout.NORTH);
			detailsTable = new JTable(new DefaultTableModel(new String[] {"Property name", "Value"},0)) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
				
				@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
				public Class getColumnClass(int column) {
					switch (column) {
						default:
							return String.class;
					}
				}
			};
			pane = new JScrollPane(detailsTable);
			add(pane, BorderLayout.CENTER);
			JLabel dataBaseInformation = new JLabel("Data Base Information: ", null, JLabel.LEFT);
			add(dataBaseInformation, BorderLayout.SOUTH);
		}

	}

	class DataBasePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		int selectedRow = -1;

		DataBasePanel() {
			super();
			setLayout(new BorderLayout());
			dataBaseTable = new JTable(new DefaultTableModel(new String[] {"Property name", "Value"}, 0)) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			dataBaseTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					JTable target = (JTable)e.getSource();
					selectedRow = target.getSelectedRow();
				}
			});
			dbPane = new JScrollPane(dataBaseTable);
			JButton addButton = new JButton("Add Property");
			JButton removeButton = new JButton("Remove Property");

			if (!DataBaseConnectivity.checkTable(DataBaseConnectivity.PROPERTIES_TABLE)) {
				DataBaseConnectivity.createPropertiesTable();
			}

			ArrayList<ArrayList<String>> properties = DataBaseConnectivity.getProperties(address);
			for (int i = 0 ; i < properties.size() ; i++) {
				DefaultTableModel model = (DefaultTableModel) dataBaseTable.getModel();
				model.addRow(new String[] {properties.get(i).get(0), properties.get(i).get(1)});
				model.fireTableDataChanged();
			}

			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new AddValueFrame();
				}
			});

			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ArrayList<String> values = new ArrayList<String>();
					DefaultTableModel model = (DefaultTableModel) dataBaseTable.getModel();
					values.add(address);
					values.add((String) model.getValueAt(selectedRow, 0));
					values.add((String) model.getValueAt(selectedRow, 1));
					DataBaseConnectivity.deleteProperty(values);
					model.removeRow(selectedRow);
					model.fireTableDataChanged();
				}
			});

			add(dbPane, BorderLayout.CENTER);
			add(addButton, BorderLayout.WEST);
			add(removeButton, BorderLayout.EAST);
		}
	}

	class AddValueFrame extends JFrame implements ActionListener {
		private static final long serialVersionUID = 1L;
		JPanel leftPanel;
		JPanel rightPanel;
		JTextField valueField;
		JTextField propertyField;

		AddValueFrame() {
			super();
			setSize(200, 200);
			setLayout(new BorderLayout());
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);

			leftPanel = new JPanel();
			leftPanel.setLayout(new BorderLayout());
			JLabel propertyLabel = new JLabel("Property name", null, JLabel.LEFT);
			propertyField = new JTextField(20);
			leftPanel.add(propertyLabel, BorderLayout.NORTH);
			leftPanel.add(propertyField, BorderLayout.SOUTH);

			rightPanel = new JPanel();
			rightPanel.setLayout(new BorderLayout());
			JLabel valueLabel = new JLabel("Value", null, JLabel.LEFT);
			valueField = new JTextField(20);
			rightPanel.add(valueLabel, BorderLayout.NORTH);
			rightPanel.add(valueField, BorderLayout.SOUTH);

			JButton confirm = new JButton("Done");
			confirm.addActionListener(this);

			add(leftPanel, BorderLayout.WEST);
			add(rightPanel, BorderLayout.EAST);
			add(confirm, BorderLayout.SOUTH);

			pack();
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			if (propertyField.getText() != null && !propertyField.getText().equals("") && valueField.getText() != null && !propertyField.getText().equals("")) {
				DefaultTableModel model = (DefaultTableModel) dataBaseTable.getModel();
				model.addRow(new String[] {propertyField.getText(), valueField.getText()});
				ArrayList<String> array = new ArrayList<String>();
				array.add(address);
				array.add(propertyField.getText());
				array.add(valueField.getText());
				DataBaseConnectivity.addProperty(array);
				//close frame
				setVisible(false);
				dispose();
			}
			else {
				JOptionPane.showMessageDialog(this, "Property name and value cannot be null");
			}			
		}
	}

	class AccessPointDetails implements Runnable {

		private Timer timer;
		private boolean running = true;

		public AccessPointDetails() {
			timer = new Timer();
		}

		@Override
		public void run() {
			AccessPoint ap = Network.getAccessPoints().get(address);
			if (ap != null) {
				if (!DataBaseConnectivity.checkTable(DataBaseConnectivity.VENDOR_TABLE)) {
					DataBaseConnectivity.createVendorTable();
				}
				ap.setVendor(DataBaseConnectivity.getVendor(address));
			}
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (running) {
						updateAccessPoint();
						//detailsPanel.validate();
						//detailsPanel.repaint();
					}
					else {
						timer.cancel();
						timer.purge();
					}
				}
			}, 1000, 3000);
		}

		public void stop() {
			running = false;
		}

		private void updateAccessPoint() {
			AccessPoint ap = Network.getAccessPoints().get(address);
			if (ap == null) {
				return;
			}

			DefaultTableModel model = (DefaultTableModel) detailsTable.getModel();
			int rowCount = model.getRowCount();
			for (int i = 0; i < rowCount; ) {
				model.removeRow(i);
				rowCount--;
			}

			//add row
			String channel = Channel.get(ap.getChannelFrequency());
			double loss = 0;
			double retried = 0;
			if (ap.getPacketsReceived() > 0) {
				loss = ((double)ap.getPacketsLost() / (ap.getPacketsReceived() + ap.getPacketsLost())) * 100;
				loss = (double)Math.round(loss * 100) / 100;
			}
			if (ap.getPacketsSent() > 0) {
				retried = ((double) ap.getPacketsRetried() / (ap.getPacketsSent() + ap.getPacketsRetried())) * 100;
				retried = (double)Math.round(retried * 100) / 100;
			}
			model.addRow(new String[] {"Address", address});
			model.addRow(new String[] {"SSID", ap.getSSID()});
			model.addRow(new String[] {"Vendor", ap.getVendor()});
			model.addRow(new String[] {"Channel", channel});
			model.addRow(new String[] {"Signal", String.valueOf((ap.getSignal()))});
			model.addRow(new String[] {"Connected Stations", String.valueOf(ap.getAssociatedStations().size())});
			model.addRow(new String[] {"Bytes Sent", String.valueOf(ap.getBytesSent())});
			model.addRow(new String[] {"Bytes Received", String.valueOf(ap.getBytesReceived())});
			model.addRow(new String[] {"Packets Sent", String.valueOf(ap.getPacketsSent())});
			model.addRow(new String[] {"Packets Received", String.valueOf(ap.getPacketsReceived())});
			model.addRow(new String[] {"Lost Packets", String.valueOf(ap.getPacketsLost()) + " (" + String.valueOf(loss) + "% Loss)"});
			model.addRow(new String[] {"Resent Packets", String.valueOf(ap.getPacketsRetried()) + " (" + String.valueOf(retried) + "% Retried)"});
			model.addRow(new String[] {"Control Packets Sent", String.valueOf(ap.getControlPacketsSent())});
			model.addRow(new String[] {"Control Packets Received", String.valueOf(ap.getControlPacketsReceived())});
			model.addRow(new String[] {"Management Packets Sent", String.valueOf(ap.getManagementPacketsSent())});
			model.addRow(new String[] {"Management Packets Received", String.valueOf(ap.getManagementPacketsReceived())});
			model.addRow(new String[] {"Data Packets Sent", String.valueOf(ap.getDataPacketsSent())});
			model.addRow(new String[] {"Data Packets Received", String.valueOf(ap.getDataPacketsReceived())});

			model.fireTableDataChanged();
		}
	}

	class StationDetails implements Runnable {

		private Timer timer;
		private boolean running = true;

		public StationDetails() {
			timer = new Timer();
		}

		@Override
		public void run() {
			Station sta = Network.getStations().get(address);
			if (sta != null) {
				if (!DataBaseConnectivity.checkTable(DataBaseConnectivity.VENDOR_TABLE)) {
					DataBaseConnectivity.createVendorTable();
				}
				sta.setVendor(DataBaseConnectivity.getVendor(address));
			}
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (running) {
						updateStation();
					}
					else {
						timer.cancel();
						timer.purge();
					}
				}
			}, 1000, 3000);
		}

		public void stop() {
			running = false;
		}

		private void updateStation() {
			Station sta = Network.getStations().get(address);
			if (sta == null) {
				return;
			}
			DefaultTableModel model = (DefaultTableModel) detailsTable.getModel();
			int rowCount = model.getRowCount();
			for (int i = 0; i < rowCount; ) {
				model.removeRow(i);
				rowCount--;
			}
			
			String channel = Channel.get(sta.getChannelFrequency());
			double loss = 0;
			double retried = 0;
			if (sta.getPacketsReceived() > 0) {
				loss = ((double)sta.getPacketsLost() / (sta.getPacketsReceived() + sta.getPacketsLost())) * 100;
			}
			if (sta.getPacketsSent() > 0) {
				retried = ((double)sta.getPacketsRetried() / (sta.getPacketsSent() + sta.getPacketsRetried())) * 100;
			}
			
			model.addRow(new String[] {"Address", address});
			model.addRow(new String[] {"Vendor", sta.getVendor()});
			model.addRow(new String[] {"Channel", channel});
			model.addRow(new String[] {"Signal", String.valueOf(sta.getSignal())});
			model.addRow(new String[] {"SSID", String.valueOf(sta.getAp().getSSID())});
			model.addRow(new String[] {"Bytes Sent", String.valueOf(sta.getBytesSent())});
			model.addRow(new String[] {"Bytes Received", String.valueOf(sta.getBytesReceived())});
			model.addRow(new String[] {"Packets Sent", String.valueOf(sta.getPacketsSent())});
			model.addRow(new String[] {"Packets Received", String.valueOf(sta.getPacketsReceived())});
			model.addRow(new String[] {"Lost Packets", String.valueOf(sta.getPacketsLost()) + " (" + String.valueOf(loss) + "% Loss)"});
			model.addRow(new String[] {"Resent Packets", String.valueOf(sta.getPacketsRetried()) + " (" + String.valueOf(retried) + "% Retried)"});
			model.addRow(new String[] {"Control Packets Sent", String.valueOf(sta.getControlPacketsSent())});
			model.addRow(new String[] {"Control Packets Received", String.valueOf(sta.getControlPacketsReceived())});
			model.addRow(new String[] {"Management Packets Sent", String.valueOf(sta.getManagementPacketsSent())});
			model.addRow(new String[] {"Management Packets Received", String.valueOf(sta.getManagementPacketsReceived())});
			model.addRow(new String[] {"Data Packets Sent", String.valueOf(sta.getDataPacketsSent())});
			model.addRow(new String[] {"Data Packets Received", String.valueOf(sta.getDataPacketsReceived())});
			model.addRow(new String[] {"Data Rate", String.valueOf(sta.getDataRate())});

			model.fireTableDataChanged();

		}
	}
}

class ExtendedDetails extends Details {
	private static final long serialVersionUID = 1L;

	public ExtendedDetails(String title, String address) {
		super(title, address);
		createButtonPanel();
		pack();
	}

	public void createButtonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());

		topology = new JButton("Topology");
		topology.addActionListener(this);
		buttonPanel.add(topology, BorderLayout.SOUTH);

		panel.add(topology,BorderLayout.SOUTH);
	}
}
