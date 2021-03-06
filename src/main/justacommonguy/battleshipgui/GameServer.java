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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import justacommonguy.battleshipgui.network.NetworkComponent;
import justacommonguy.battleshipgui.network.Request;
import justacommonguy.battleshipgui.player.Player;
import justacommonguy.battleshipgui.ship.Ship;
import justacommonguy.battleshipgui.ship.ShipLocation;
import justacommonguy.battleshipgui.utils.Attack;
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
		requestPlayers();
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

	private void requestPlayers() {
		try {
			oos.writeObject(Request.SEND_PLAYER);
			oos.writeObject(null);
			System.out.println("Requested client to send player info.");
			client = (Player) ois.readObject();
			System.out.println("Received client's player info. Player: " + client);
	
			host = (Player) local.respondRequest(Request.SEND_PLAYER, null);
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to request or receive player info.");
		}
	}

	private void startGame() {
		try {			
			local.respondRequest(Request.START, client.toString());
			
			oos.writeObject(Request.START);
			System.out.println("Requested client to start game.");
			oos.writeObject(host.toString());
			System.out.println("Sent host info to client. Player: " + host);
		}
		catch (IOException e) {
			System.out.println("Failed to start the game.");
		}
	}

	@SuppressWarnings("unchecked")
	private void unlockPlacing() {
		CyclicBarrier barrier = new CyclicBarrier(2);

		new Thread(() -> {
			try {
				oos.writeObject(Request.PLACE_SHIPS);
				oos.writeObject(null);
				clientShips = (ArrayList<Ship>) ois.readObject();
				barrier.await();
			}
			catch (BrokenBarrierException | ClassNotFoundException | InterruptedException | IOException e) {
				System.out.println("Failed to get client ships.");
			}
		}).start();

		hostShips = (ArrayList<Ship>) local.respondRequest(Request.PLACE_SHIPS, null);
		try {
			barrier.await();
		}
		catch (BrokenBarrierException |InterruptedException e) {}
	}

	private String play() {
		Random random = new Random();
		String winner = host.toString();

		boolean isHostFirst = false;
		if (random.nextBoolean()) {
			isHostFirst = true;
		}

		while (true) {
			if (isHostFirst) {
				hostAttack();
				isHostFirst = false;
			}

			if (isGameFinished()) {
				break;
			}
			clientAttack();

			if (isGameFinished()) {
				break;
			}
			hostAttack();
		}

		if (hostShips.isEmpty()) {
			winner = client.toString();
		}
		return winner;
	}

	private void hostAttack() {
		ShipLocation guess = (ShipLocation) local.respondRequest(Request.ATTACK, null);
		Result result = checkGuess(guess, clientShips);
		updateLocations(new Attack(guess, result, false));
	}

	private void clientAttack() {
		try {
			oos.writeObject(Request.ATTACK);
			oos.writeObject(null);
			ShipLocation guess = (ShipLocation) ois.readObject();
			Result result = checkGuess(guess, hostShips);
			updateLocations(new Attack(guess, result, true));
		}
		catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to get client guess.");
		}
	}

	//TODO This should only check, not update the list
	static Result checkGuess(ShipLocation guess, ArrayList<Ship> shipList) {
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
	 */
	private void updateLocations(Attack attack) {
		local.respondRequest(Request.ATTACKED, attack);
		try {
			oos.writeObject(Request.ATTACKED);
			oos.writeObject(attack);
		} catch (IOException e) {
			System.out.println("Failed to send attack result.");
		}
	}

	public boolean isGameFinished() {
		return hostShips.isEmpty() || clientShips.isEmpty();
	}

	private void finish(String winner) {
		local.respondRequest(Request.FINISH, winner);
		try {
			oos.writeObject(Request.FINISH);
			oos.writeObject(winner);
		} catch (IOException e) {
			System.out.println("Failed to finish client's game.");
		}
	}

	void restart() {
		local.restart();
	}
}
