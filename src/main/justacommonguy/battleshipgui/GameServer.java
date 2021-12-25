package justacommonguy.battleshipgui;

import static justacommonguy.battleshipgui.Settings.gameSettings;
import static justacommonguy.battleshipgui.gui.BattleshipGUI.gameGUI;

import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

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
// ? Add WAN connection. https://res.infoq.com/articles/Java-7-Sockets-Direct-Protocol/en/resources/Fig2large.jpg
public class GameServer implements Runnable, NetworkComponent {

	public static GameServer gameServer = getInstance();

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private ArrayList<Ship> hostShips = new ArrayList<Ship>();
	private ArrayList<Ship> clientShips = new ArrayList<Ship>();
	private AllyPlayer host;
	private AllyPlayer client;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new FlatSpacegrayIJTheme());
		}
		catch (Exception ex) {
			System.out.println("Failed to set LaF");
		}

		gameServer.start(args);
	}

	private GameServer() {}

	public static GameServer getInstance() {
		if (gameServer == null) {
			gameServer = new GameServer();
		}
		return gameServer;
	}

	public void start(String[] args) {
		String hostUsername = null;
		if (args.length != 0) {
			hostUsername = args[0];
		}
		gameGUI = BattleshipGUI.getInstance(hostUsername);
		gameGUI.start(JFrame.EXIT_ON_CLOSE);
	}

	public void hostGame() {
		try (ServerSocket serverSocket = new ServerSocket(
				Integer.parseInt(gameSettings.getSetting("server_port")))) {
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
			gameGUI.startGame(client.toString());

			oos.writeObject(Request.START);
			System.out.println("Requested client to start game.");
			host = gameGUI.getPlayer();
			oos.writeObject(host.toString());
			System.out.println("Sent host info to client. Player: " + host);
		}
		catch (IOException | ClassNotFoundException e) {
			System.out.println("Failed to start the game.");
		}
		unlockPlacing();
	}

	private void play() {
		Random random = new Random();
		String winner = host.toString();

		// Host goes first if true.
		if (random.nextBoolean()) {
			while (!hostShips.isEmpty() || !clientShips.isEmpty()) {
				hostAttack();
				clientAttack();
			}
		}
		else {
			while (!hostShips.isEmpty() || !clientShips.isEmpty()) {
				clientAttack();
				hostAttack();
			}
		}

		if (hostShips.isEmpty()) {
			winner = client.toString();
		}

		finish(winner);
	}
	
	@SuppressWarnings("unchecked")
	private void unlockPlacing() {
		hostShips = gameGUI.getShips();
		try {
			oos.writeObject(Request.PLACE_SHIPS);
			clientShips = (ArrayList<Ship>) ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to get client ships.");
		}
		play();
	}

	private void hostAttack() {
		ShipLocation guess = gameGUI.getAttack();
		Result result = checkGuess(guess, clientShips);
		updateLocations(guess, result, Faction.ENEMY);
		try {
			oos.writeObject(Request.ATTACK_RESULT);
			oos.writeObject(guess);
			oos.writeObject(result);
			oos.writeObject(Faction.ALLY);
		} catch (IOException e) {
			System.out.println("Failed to send host guess.");
		}
	}

	private void clientAttack() {
		try {
			oos.writeObject(Request.ATTACK);
			ShipLocation guess = (ShipLocation) ois.readObject();
			Result result = checkGuess(guess, hostShips);

			oos.writeObject(Request.ATTACK_RESULT);
			oos.writeObject(guess);
			oos.writeObject(result);
			oos.writeObject(Faction.ENEMY);
			updateLocations(guess, result, Faction.ALLY);
		}
		catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to get client guess.");
		}
	}

	private static Result checkGuess(ShipLocation guess, ArrayList<Ship> shipList) {
		Result result = Result.MISS;
		loop:
		for (Ship ship : shipList) {
			result = ship.checkHit(guess);
			switch (result) {
				case KILL:
					shipList.remove(ship);
				case HIT:
					break loop;
				case MISS: default:
					break;
			}
		}
		return result;
	}

	private void updateLocations(ShipLocation guess, Result result, Faction factionAttacked) {
		gameGUI.updateMap(guess, result, factionAttacked);
		try {
			oos.writeObject(Request.ATTACK_RESULT);
			oos.writeObject(guess);
			oos.writeObject(result);
			oos.writeObject(factionAttacked);
		} catch (IOException e) {
			System.out.println("Failed to send attack result.");
		}
	}

	public void finish(String winner) {
		gameGUI.finish(winner);
		try {
			oos.writeObject(Request.FINISH);
			oos.writeObject(winner);
		} catch (IOException e) {
			System.out.println("Failed to finish client's game.");
		}
	}
}
