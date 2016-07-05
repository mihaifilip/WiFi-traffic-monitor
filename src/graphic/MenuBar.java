package graphic;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import client.Application;

public class MenuBar extends JMenuBar{

	private static final long serialVersionUID = 1L;
	JMenu menu1 = null;
	JMenu menu2 = null;

	public MenuBar() {
		super();
		menu1 = new JMenu("Home");

		JMenuItem menuItem = new JMenuItem(new AbstractAction("Exit") {
			private static final long serialVersionUID = 1L;

			//stop application
			public void actionPerformed(ActionEvent e) {
				Application.setApplicationIsRunning(false);
				Application.stopMonitoring();
				synchronized (Application.getLock()) {
					Application.getLock().notifyAll();
				}
			}
		});
		menu1.add(menuItem);
		this.add(menu1);
	}

}
