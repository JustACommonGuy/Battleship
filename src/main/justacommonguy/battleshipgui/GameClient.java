package justacommonguy.battleshipgui;
import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.UIManager;

import justacommonguy.battleshipgui.cell.Map;
import justacommonguy.battleshipgui.cell.Ally.AllyMap;
import justacommonguy.battleshipgui.cell.Enemy.EnemyMap;
import justacommonguy.battleshipgui.network.NetworkComponent;
import justacommonguy.battleshipgui.network.Request;
import justacommonguy.battleshipgui.player.Player;
import justacommonguy.battleshipgui.ship.ShipLocation;
import justacommonguy.battleshipgui.utils.Attack;

public class GameClient implements NetworkComponent {

	private BattleshipGUI gui;
	private GameServer server;
	
	private Player player;
	// ? Only the enemy's name might be needed
	private Player enemy;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private AllyMap allyMap;
	private EnemyMap enemyMap;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new FlatSpacegrayIJTheme());
		}
		catch (Exception e) {
			System.out.println("Failed to set LaF");
		}

		if (args.length == 0) {
			args = new String[1];
			args[0] = "";
		}

		new GameClient(args[0]).start();
	}

	private GameClient(String hostUsername) {
		// ? Add a separate GUI class in config to process settings
		if (gameSettings.getSetting("username").equals("")) {
			if (hostUsername.equals("")) {
				hostUsername = BattleshipGUI.askName();
			}
			gameSettings.setSetting("username", hostUsername);
			gameSettings.saveSettings();
		}
		player = new Player(gameSettings.getSetting("username"));
		enemy = new Player("OPPONENT");
	}

	public void start() {
		gui = new BattleshipGUI(this);
		allyMap = new AllyMap();
		enemyMap = new EnemyMap();
		
		gui.start(JFrame.EXIT_ON_CLOSE);
		gui.initPlayArea(allyMap, enemyMap);
		allyMap.buildShips();
		allyMap.allowInteraction(true);
	}

	public void hostGame() {
		server = new GameServer(this);
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
				Object answer = respondRequest((Request) obj, ois.readObject());
				if (answer != null) {
					oos.writeObject(answer);
				}
				if (obj == Request.FINISH) {
					break;
				}
			}
		}
		catch (IOException | ClassNotFoundException e) {
			System.out.println("Failed to read request or send response.");
			e.printStackTrace();
		}
	}

	@Override
	public Object respondRequest(Request request, Object message) {
		System.out.println("Received " + request + " request.");
		switch (request) {
			case SEND_PLAYER:
				System.out.println("Sending " + player);
				return getPlayer();
			case START:
				startGame((String) message);
				break;
			case PLACE_SHIPS:
				System.out.println("Sending ships.");
				gui.allowPlacement(true);
				return allyMap.sendShips();
			case ATTACK:
				gui.allowAttack(true);
				ShipLocation attackLocation = enemyMap.sendAttackGuess();
				System.out.println("Sending guess: " + attackLocation);
				return attackLocation;
			case ATTACKED:
				Map attackedMap = enemyMap;
				Attack attack = (Attack) message;
				if (attack.isHostAttacked()) {
					attackedMap = allyMap;
				}
				attack.updateMap(attackedMap);
				gui.updateAttackLabel(attack.getResult().toString(), 3000);
				System.out.println("Updated " + attackedMap + ": " + attack);
				break;
			case FINISH:
				finish((String) message);
				break;
		}
		
		return null;
	}
	
	private Player getPlayer() {
		return (Player) player.clone();
	}

	private void startGame(String enemyName) {
		enemy.setName(enemyName);
		gui.startGame(enemyName);
	}

	private void finish(String winner) {
		//TODO
		System.out.println("Finished the game. Winner is " + winner);
	}

	void restart() {
		new Thread(() -> {
			main(new String[0]);
		}, "Main");
	}
}
