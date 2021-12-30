package justacommonguy.battleshipgui;
import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.UIManager;

import justacommonguy.battleshipgui.player.AllyPlayer;
import justacommonguy.battleshipgui.player.EnemyPlayer;
import justacommonguy.battleshipgui.ship.ShipLocation;
import justacommonguy.battleshipgui.utils.Faction;
import justacommonguy.battleshipgui.utils.Result;

public class GameClient implements NetworkComponent {

	private BattleshipGUI gui = new BattleshipGUI(this);
	private GameServer server = new GameServer(this);
	
	// ? Maybe player should be in GameLogic
	private AllyPlayer player;
	// ? Only the enemy's name might be needed
	private EnemyPlayer enemy;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new FlatSpacegrayIJTheme());
		}
		catch (Exception ex) {
			System.out.println("Failed to set LaF");
		}

		if (args.length == 0) {
			args = new String[1];
			args[0] = "";
		}

		new GameClient(args[0]).start();
	}

	public GameClient(String hostUsername) {
		// TODO
		if (gameSettings.getSetting("username").equals("")) {
			if (hostUsername == null) {
				hostUsername = gui.askName();
			}
			gameSettings.setSetting("username", hostUsername);
			gameSettings.saveSettings();
		}
		player = new AllyPlayer(gameSettings.getSetting("username"));
		enemy = new EnemyPlayer("OPPONENT");
	}

	public void start() {
		gui.start(JFrame.EXIT_ON_CLOSE);
		player.buildShips();
	}

	public void hostGame() {
		// The server would be stuck waiting, so the GUI goes crazy if I call it in the same thread.
		new Thread(server, "Server").start();
	}

	public void joinGame(String ipAddress, int port) {
		new Thread(() -> {
			try (Socket socket = new Socket(ipAddress, port)) {
				oos = new ObjectOutputStream(socket.getOutputStream());
				ois = new ObjectInputStream(socket.getInputStream());
				System.out.println("Connection started.");
				listenRequests();
			}
			catch (IOException e) {
				System.out.println("Connection failed.");
				ois = null;
				oos = null;
			}
		}, "Client").start();
	}

	@Override
	public void listenRequests() {
		Object obj;
		try {
			while ((obj = ois.readObject()) != null) {
				Object answer = respondRequest((Request) obj);
				if (answer != null) {
					oos.writeObject(answer);
				}
			}
		}
		catch (IOException | ClassNotFoundException e) {
			System.out.println("Failed to read object.");
			e.printStackTrace();
		}
		
	}

	@Override
	public Object respondRequest(Request request) {
		try {
			System.out.println("Received " + request + " request.");
			switch (request) {
				case ATTACK:
					ShipLocation attackLocation = getAttack();
					System.out.println("Sending " + attackLocation);
					return attackLocation;
				case ATTACK_RESULT:
					updateMap((ShipLocation) ois.readObject(), (Result) ois.readObject(), (Faction) ois.readObject());
					break;
				case FINISH:
					finish((String) ois.readObject());
					break;
				case PLACE_SHIPS:
					System.out.println("Sending ships.");
					allowShipPlacement();
				case SEND_PLAYER:
					System.out.println("Sending " + player);
					return getPlayer();
				case START:
					startGame((String) ois.readObject());
					break;
			}
		}
		catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to respond server requests.");
		}
		
		return null;
	}
	
	public AllyPlayer getPlayer() {
		return (AllyPlayer) player.clone();
	}

	public void startGame(String enemyName) {
		enemy.setName(enemyName);
		gui.startGame(enemyName);
	}

	public void allowShipPlacement() {
		// TODO
	}

	public ShipLocation getAttack() {
		//TODO
		return null;
	}

	public void updateMap(ShipLocation guess, Result result, Faction factionAttacked) {
		//TODO
		System.out.println("Updated user map. Guess Location: " + guess + ". Result: " + result);
	}

	public void finish(String winner) {
		//TODO
		System.out.println("Finished the game. Winner is " + winner);
	}
}
