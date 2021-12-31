package justacommonguy.battleshipgui;

import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import justacommonguy.battleshipgui.player.Player;
import justacommonguy.battleshipgui.ship.Ship;
import justacommonguy.battleshipgui.ship.ShipLocation;
import justacommonguy.battleshipgui.utils.Result;

// TODO. Should send a message to the client if the game fails.
// TODO. Separate client and host code.
// ? Add WAN connection. https://res.infoq.com/articles/Java-7-Sockets-Direct-Protocol/en/resources/Fig2large.jpg
public class GameServer implements Runnable, NetworkComponent {

	private GameClient local;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private ArrayList<Ship> hostShips = new ArrayList<>();
	private ArrayList<Ship> clientShips = new ArrayList<>();
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
	public Object respondRequest(Request request, Object message) {
		// ? Send location list to make an excel of the results.
		return null;
	}

	private void startGame() {
		try {
			oos.writeObject(Request.SEND_PLAYER);
			oos.writeObject(null);
			System.out.println("Requested client to send player info.");
			client = (Player) ois.readObject();
			System.out.println("Received client's player info. Player: " + client);
			local.respondRequest(Request.START, client.toString());

			oos.writeObject(Request.START);
			System.out.println("Requested client to start game.");
			host = (Player) local.respondRequest(Request.SEND_PLAYER, null);
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
		hostShips = (ArrayList<Ship>) local.respondRequest(Request.PLACE_SHIPS, null);
		try {
			oos.writeObject(Request.PLACE_SHIPS);
			oos.writeObject(null);
			clientShips = (ArrayList<Ship>) ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to get client ships.");
		}
	}

	private void hostAttack() {
		ShipLocation guess = (ShipLocation) local.respondRequest(Request.ATTACK, null);
		Result result = checkGuess(guess, clientShips);
		updateLocations(new Attack(guess, result), false);
	}

	private void clientAttack() {
		try {
			oos.writeObject(Request.ATTACK);
			oos.writeObject(null);
			ShipLocation guess = (ShipLocation) ois.readObject();
			Result result = checkGuess(guess, hostShips);
			updateLocations(new Attack(guess, result), true);
		}
		catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to get client guess.");
		}
	}

	//TODO This should only check, not update the list
	private static Result checkGuess(ShipLocation guess, ArrayList<Ship> shipList) {
		Result result = Result.MISS;
		Iterator<Ship> iterator = shipList.iterator();

		loop:
		while (iterator.hasNext()) {
			result = iterator.next().checkHit(guess);
			switch (result) {
				case KILL:
					iterator.remove();
					break loop;
				case HIT:
					break loop;
				case MISS:
					break;
			}
		}
		return result;
	}

	/**
	 * Prompts the clients to update their maps.
	 * @param attack
	 * @param isHostAttacked Whether the player attacked is the host.
	 */
	private void updateLocations(Attack attack, boolean isHostAttacked) {
		Request hostRequest = Request.ATTACK_ENEMY;
		Request clientRequest = Request.ATTACK_ALLY;
		if (isHostAttacked) {
			hostRequest = Request.ATTACK_ALLY;
			clientRequest = Request.ATTACK_ENEMY;
		}

		local.respondRequest(hostRequest, attack);
		try {
			oos.writeObject(clientRequest);
			oos.writeObject(attack);
		} catch (IOException e) {
			System.out.println("Failed to send attack result.");
		}
	}

	public void finish(String winner) {
		local.respondRequest(Request.FINISH, winner);
		try {
			oos.writeObject(Request.FINISH);
			oos.writeObject(winner);
		} catch (IOException e) {
			System.out.println("Failed to finish client's game.");
		}
	}
}
