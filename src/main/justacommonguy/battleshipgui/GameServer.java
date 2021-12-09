package justacommonguy.battleshipgui;

import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.UIManager;

import justacommonguy.battleshipgui.gui.BattleshipGUI;

public class GameServer {

	public static Settings settings = new Settings(new File("settings.properties"));
	public static GameServer server;
	public static BattleshipGUI gui;
	//TODO. Would the streams work if we don't keep a reference to the socket?
	private Socket clientSocket;
	private ObjectInputStream clientInput;
	private ObjectOutputStream clientOutput;
	private String winner;

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
		String hostUsername = null;
		if (args.length != 0) {
			hostUsername = args[0];
		}
		gui = new BattleshipGUI(hostUsername);
		gui.start(JFrame.EXIT_ON_CLOSE);
	}

	public void hostGame() {
		try {
			ServerSocket serverSocket = new ServerSocket(Integer.parseInt(settings.getSetting("server_port")));
			clientSocket = serverSocket.accept();
			clientInput = new ObjectInputStream(clientSocket.getInputStream());
			clientOutput = new ObjectOutputStream(clientSocket.getOutputStream());
			System.out.println("Connection started.");
		}
		catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	private void play() {

	}
	
	private void unlockPlacing(String player) {

	}

	private void unlockAttacking(String player) {

	}

	private void updateLocations(Result result) {

	}

	public void finish() {

	}
}
