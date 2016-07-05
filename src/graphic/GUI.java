package graphic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import client.Application;

public class GUI extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static JTabbedPane tabbedPane = null;
	public static ArrayList<TopologyTab> tabs = new ArrayList<TopologyTab>();

	public GUI() {
		this.setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	Application.setApplicationIsRunning(false);
				Application.stopMonitoring();
				synchronized (Application.getLock()) {
					Application.getLock().notifyAll();
				}
		        super.windowClosing(windowEvent);
		    }
		});
		
		//full screen
		this.setExtendedState(Frame.MAXIMIZED_BOTH);  
		
	    setTitle("WiFi Monitor");
	    setBackground(Color.gray);

	    //create menu
	    MenuBar menuBar = new MenuBar();
	    this.setJMenuBar(menuBar);
	    
		//create Tabbed Panel
		tabbedPane = new JTabbedPane();
		
		//create first tab
	    AccessPointsTab tab1 = new AccessPointsTab();
	    
	    //add tabs to the Tabbed Pannel
	    tabbedPane.addTab("AP", null, tab1,"Access Points");
	    tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
	    
	    getContentPane().add(tabbedPane);
	    
	    pack();
	    setVisible(true);
	}
	
	
	public static void addTopologyTab(String address, String SSID) {
	    TopologyTab tab2 = new TopologyTab(address);
	    if (SSID == null) {
	    	SSID = "Topology";
	    }
	    tabs.add(tab2);
	    tabbedPane.addTab(SSID, null, tab2,"Network of " + address); 
	}
}