package justacommonguy.battleshipgui;

import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.UIManager;

import justacommonguy.battleshipgui.gui.BattleshipGUI;
import justacommonguy.battleshipgui.networking.NetworkComponent;
import justacommonguy.battleshipgui.networking.Request;
// TODO. Should send a message to the client if the game fails.
// TODO. Implement the catch blocks for better handling.
// ? Add WAN connection. https://res.infoq.com/articles/Java-7-Sockets-Direct-Protocol/en/resources/Fig2large.jpg
public class GameServer implements Runnable, NetworkComponent {

	public static Settings settings = new Settings(new File("settings.properties"));
	public static GameServer server;
	public static BattleshipGUI gui;
	
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private AllyPlayer host;
	private AllyPlayer client;

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
		try (ServerSocket serverSocket = new ServerSocket(
				Integer.parseInt(settings.getSetting("server_port")))) {
			System.out.println("Waiting for connection.");
			Socket clientSocket = null;
			clientSocket = serverSocket.accept();
			
			// *Output streams need to be constructed before the input streams.
			// *https://stackoverflow.com/a/32940236
			// *I've just spent two hours debugging this. The only I had to do was to switch these.
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			ois = new ObjectInputStream(clientSocket.getInputStream());
			System.out.println("Connection started.");

			startGame();
		}
		catch (NumberFormatException | IOException e) {
			System.out.println("Could not host game.");
			ois = null;
			oos = null;
		}
	}

	@Override
	public void run() {
		hostGame();
	}

	@Override
	public void listenRequests() {
		// TODO
	}

	@Override
	public Object respondRequest(Request request) {
		// TODO Might send location list to make an excel of the results.
		return null;
	}

	private void startGame() {
		try {
			oos.writeObject(Request.SEND_PLAYER);
			System.out.println("Requested client to send player info.");
			client = (AllyPlayer) ois.readObject();
			System.out.println("Received client's player info. Player: " + client);
			gui.startGame(client);

			oos.writeObject(Request.START);
			System.out.println("Requested client to start game.");
			host = gui.getPlayer();
			oos.writeObject(host);
			System.out.println("Sent host info to client. Player: " + host);
		}
		catch (IOException | ClassNotFoundException e) {
			System.out.println("Failed to start the game.");
		}
		unlockPlacing();
	}

	private void play() {
		Random random = new Random();
		String winner = host.getName();

		// Host goes first if true.
		if (random.nextBoolean()) {
			while (!hostLocations.isEmpty() || !clientLocations.isEmpty()) {
				hostAttack();
				clientAttack();
			}
		}
		else {
			while (!hostLocations.isEmpty() || !clientLocations.isEmpty()) {
				clientAttack();
				hostAttack();
			}
		}

		if (hostLocations.isEmpty()) {
			winner = client.getName();
		}

		finish(winner);
	}
	
	@SuppressWarnings("unchecked")
	private void unlockPlacing() {
		hostLocations = gui.getShips();
		try {
			oos.writeObject(Request.PLACE_SHIPS);
			clientLocations = (ArrayList<Ship>) ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to get client ships.");
		}
		play();
	}

	private void hostAttack() {
		ShipLocation guess = gui.getAttack();
		updateLocations(guess, checkGuess(guess));
	}

	private void clientAttack() {
		try {
			oos.writeObject(Request.ATTACK);
			ShipLocation guess = (ShipLocation) ois.readObject();
			updateLocations(guess, checkGuess(guess));
		}
		catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to get client guess.");
		}
	}

	private Result checkGuess(ShipLocation guess) {
		Result result = Result.MISS;
		loop:
		for (Ship ship : clientLocations) {
			result = ship.checkHit(guess);
			switch (result) {
				case KILL:
					clientLocations.remove(ship);
				case HIT:
					break loop;
				case MISS: default:
					break;
			}
		}
		return result;
	}

	private void updateLocations(ShipLocation guess, Result result) {
		gui.updateMap(guess, result);
		try {
			oos.writeObject(Request.ATTACK_RESULT);
			oos.writeObject(guess);
			oos.writeObject(result);
		} catch (IOException e) {
			System.out.println("Failed to send attack result.");
		}
	}

	public void finish(String winner) {
		gui.finish(winner);
		try {
			oos.writeObject(Request.FINISH);
			oos.writeObject(winner);
		} catch (IOException e) {
			System.out.println("Failed to finish client's game.");
		}
	}
}
