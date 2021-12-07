package justacommonguy.battleshipgui;

import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.UIManager;

import justacommonguy.battleshipgui.gui.BattleshipGUI;

public class GameServer {

	public static Settings settings = new Settings(new File("settings.properties"));
	public static GameServer server;
	public static BattleshipGUI gui;
	private Socket clientSocket;
	private String winner;
	private ServerSocket socket;
	private ClientPlayer host;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new FlatSpacegrayIJTheme());
		}
		catch (Exception ex) {
			System.out.println("Failed to set LaF");
		}

		server = new GameServer();
		server.start(args);
	}

	public void start(String[] args) {
		gui = new BattleshipGUI();
		gui.start(JFrame.EXIT_ON_CLOSE);

		if ((args.length == 0) && (settings.getSetting("username").equals(""))) {
			String newName = gui.askName().toUpperCase();
			host = new ClientPlayer(newName);
			settings.setSetting("username", newName);
			settings.saveSettings();
		}
	}

	private void setUpNetWorking() {
		try {
			ServerSocket serverSocket = new ServerSocket(Integer.parseInt(settings.getSetting("server_port")));
			clientSocket = serverSocket.accept();
			/* TODO Make an enumeration for different orders and make sockets 
			wait or send them. This way, only a few (maybe one?) methods related to 
			networking will be required. */
		}
		catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	private void play() {

	}

	private void makeMove(ShipLocation cell) {

	}

	public void finish() {

	}

	public void hostGame() {
		setUpNetWorking();
	}

	private void changeCells(Result result) {

	}

	private void unlockAttacking(String player) {

	}

	private void unlockPlacing(String player) {

	}
}
