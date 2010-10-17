/*
 * This file is part of Elios.
 *
 * Copyright 2010 Jeremie GASTON-RAOUL & Alexandre COLLIGNON
 *
 * Elios is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Elios is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Elios. If not, see <http://www.gnu.org/licenses/>.
 */

package net.eliosoft.elios.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.eliosoft.elios.gui.controllers.LogsController;
import net.eliosoft.elios.gui.controllers.PrefsController;
import net.eliosoft.elios.gui.controllers.RemoteController;
import net.eliosoft.elios.gui.models.LocaleComboBoxModel;
import net.eliosoft.elios.gui.models.RemoteModel;
import net.eliosoft.elios.gui.models.RemoteModel.BroadCastAddress;
import net.eliosoft.elios.gui.views.AboutView;
import net.eliosoft.elios.gui.views.LogsLineView;
import net.eliosoft.elios.gui.views.LogsView;
import net.eliosoft.elios.gui.views.Messages;
import net.eliosoft.elios.gui.views.PrefsView;
import net.eliosoft.elios.gui.views.RemoteView;
import net.eliosoft.elios.gui.views.ViewInterface;
import net.eliosoft.elios.server.ArtNetServerManager;
import net.eliosoft.elios.server.HttpServerManager;



/**
 * Main Class of Elios Software contains the main method
 * used to launch the application.
 *
 * @author Jeremie GASTON-RAOUL
 */
public final class Elios {

	/**
	 * Do nothing more than ensure that no object
	 * can be construct.
	 */
	private Elios() {
		// nothing
	}

	/**
	 * Launches Elios.
	 * @param args command-line argument. Currently unused !
	 */
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			LoggersManager.getInstance().getLogger(Elios.class.getName()).info("Can not load system look and feel");
		} 

		final Preferences prefs = Preferences.userNodeForPackage(Elios.class);
		
		String lang = prefs.get("ui.lang", Locale.getDefault().getLanguage());
		Locale locale = new Locale(lang);
		Locale.setDefault(locale);

        final RemoteModel remoteModel = createRemoteModel(prefs);
		final RemoteView remoteView = new RemoteView(remoteModel);
		//used to make relation between view and model
		new RemoteController(remoteModel, remoteView);

		final LocaleComboBoxModel localeModel = new LocaleComboBoxModel();
		localeModel.setSelectedItem(locale);
		PrefsView prefsView = new PrefsView(remoteModel, localeModel);
		//used to make relation between view and model
		new PrefsController(remoteModel, localeModel, prefsView);

		LogsView logsView = new LogsView(remoteModel);
		//used to make relation between view and model
		new LogsController(remoteModel, logsView);

		LogsLineView logsLineView = new LogsLineView(remoteModel);
		AboutView aboutView = new AboutView();
		
		for(Logger l : LoggersManager.getInstance().getLoggersList()){
			remoteModel.getLogsListModel().addLogger(l);
		}

		JFrame frame = new JFrame(Messages.getString("ui.title"));
		final JTabbedPane tabbedPane = new JTabbedPane();
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(new ImageIcon(Elios.class.getResource("/net/eliosoft/elios/server/handler/files/favicon.ico")).getImage());

		contentPane.add(tabbedPane, BorderLayout.CENTER);
		contentPane.add(logsLineView.getViewComponent(), BorderLayout.SOUTH);
		addViewToTab(tabbedPane, remoteView);
		addViewToTab(tabbedPane, prefsView);
		addViewToTab(tabbedPane, logsView);
		addViewToTab(tabbedPane, aboutView);

		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
		tabbedPane.getSelectedComponent().requestFocusInWindow();
			}
		});
		tabbedPane.setSelectedIndex(0);

		frame.addWindowListener(new WindowAdapter() {
            /**
             *
		     */
            @Override
            public void windowClosing(WindowEvent e) {
                persistRemoteModel(remoteModel, prefs);
                Locale l = (Locale)localeModel.getSelectedItem();
                prefs.put("ui.lang", l.getLanguage());
            }
		});

		frame.pack();
		frame.setVisible(true);

		tabbedPane.getSelectedComponent().requestFocusInWindow();
	}

	/**
	 * Create a <code>RemoteModel</code> and retrieve the configuration
	 * from the given <code>Preferences</code>.
	 * If any configuration is found for a specific parameter, the default
	 * value is used according to <code>RemoteModel</code>.
	 *
	 * @param prefs <code>Preferences</code> used to retrieve the configuration
	 * @return a configured <code>RemoteModel</code>
	 */
	public static RemoteModel createRemoteModel(Preferences prefs) {
	    RemoteModel model = new RemoteModel();
	    model.setSubnet(prefs.getInt("server.subnet", 0));
        model.setUniverse(prefs.getInt("server.universe", 0));
        model.setBroadCastAddress(Enum.valueOf(
                BroadCastAddress.class,
                prefs.get("server.broadcast.address",
                BroadCastAddress.PRIMARY.name())));

	    model.setInPort(prefs.getInt("server.inport",
	            ArtNetServerManager.DEFAULT_ARTNET_PORT));
        model.setOutputPort(prefs.getInt("server.outport",
                ArtNetServerManager.DEFAULT_ARTNET_PORT));

        model.setHttpServerEnabled(
                prefs.getBoolean("server.httpserver.enable", false));
		model.setAdditiveModeEnabled(
                prefs.getBoolean("server.additivemode.enable", false));
        model.setHttpPort(
                prefs.getInt("server.httpserver.port",
                HttpServerManager.DEFAULT_HTTP_PORT));

        return model;
	}

	/**
	 * Persits a remote model to the given <code>Preferences</code>.
	 *
	 * @param model the <code>RemoteModel</code> to store
	 * @param prefs the <code>Preferences</code> used to persist
	 */
	public static void persistRemoteModel(RemoteModel model, Preferences prefs) {
	    prefs.putInt("server.subnet", model.getSubnet());
	    prefs.putInt("server.universe", model.getUniverse());
	    prefs.put("server.broadcast.address",
	            model.getBroadCastAddress().name());

	    prefs.putInt("server.inport", model.getInPort());
        prefs.putInt("server.outport", model.getOutPort());

        prefs.putBoolean("server.httpserver.enable",
                model.isHttpServerEnabled());
		prefs.putBoolean("server.additivemode.enable",
                model.isAdditiveModeEnabled());
        prefs.putInt("server.httpserver.port",
                model.getHttpPort());
	}
	
	/**
	 * Adds a {@link ViewInterface} to a {@link JTabbedPane}. The localized 
	 * title of the {@link ViewInterface} is used to define the title of the pane.
	 * 
	 * @param pane the {@link JTabbedPane}
	 * @param v the {@link ViewInterface} to add
	 */
	private static void addViewToTab(JTabbedPane pane, ViewInterface v) {
	  pane.addTab(v.getLocalizedTitle(), v.getViewComponent());
	}
}
