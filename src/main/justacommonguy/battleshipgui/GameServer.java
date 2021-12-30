package justacommonguy.battleshipgui;

import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import justacommonguy.battleshipgui.player.Player;
import justacommonguy.battleshipgui.ship.Ship;
import justacommonguy.battleshipgui.ship.ShipLocation;
import justacommonguy.battleshipgui.utils.Faction;
import justacommonguy.battleshipgui.utils.Result;

// TODO. Should send a message to the client if the game fails.
// TODO. Separate client and host code.
// ? Add WAN connection. https://res.infoq.com/articles/Java-7-Sockets-Direct-Protocol/en/resources/Fig2large.jpg
public class GameServer implements Runnable, NetworkComponent {

	private GameClient local;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private ArrayList<Ship> hostShips = new ArrayList<Ship>();
	private ArrayList<Ship> clientShips = new ArrayList<Ship>();
	private Player host;
	private Player client;

	public GameServer(GameClient local) {
		this.local = local;
	}

	public void start() {
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
		}
		catch (NumberFormatException | IOException e) {
			System.out.println("Could not host game.");
			ois = null;
			oos = null;
		}
	}

	@Override
	public void run() {
		runGame();
	}

	private void runGame() {
		start();
		startGame();
		unlockPlacing();
		String winner = play();
		finish(winner);
	}

	@Override
	public void listenRequests() {
		// TODO. Chat
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
			client = (Player) ois.readObject();
			System.out.println("Received client's player info. Player: " + client);
			local.startGame(client.toString());

			oos.writeObject(Request.START);
			System.out.println("Requested client to start game.");
			host = local.getPlayer();
			oos.writeObject(host.toString());
			System.out.println("Sent host info to client. Player: " + host);
		}
		catch (IOException | ClassNotFoundException e) {
			System.out.println("Failed to start the game.");
		}
	}

	private String play() {
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
		return winner;
	}
	
	@SuppressWarnings("unchecked")
	private void unlockPlacing() {
		// TODO get ships from the player sent
		// hostShips = gameGUI.getShips();
		try {
			oos.writeObject(Request.PLACE_SHIPS);
			clientShips = (ArrayList<Ship>) ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to get client ships.");
		}
	}

	private void hostAttack() {
		ShipLocation guess = local.getAttack();
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

	//TODO This should only check, not update the list
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
				case MISS:
					break;
			}
		}
		return result;
	}

	private void updateLocations(ShipLocation guess, Result result, Faction factionAttacked) {
		local.updateMap(guess, result, factionAttacked);
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
		local.finish(winner);
		try {
			oos.writeObject(Request.FINISH);
			oos.writeObject(winner);
		} catch (IOException e) {
			System.out.println("Failed to finish client's game.");
		}
	}
}
