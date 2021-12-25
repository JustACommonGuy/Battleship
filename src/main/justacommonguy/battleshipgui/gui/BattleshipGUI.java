package justacommonguy.battleshipgui.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import justacommonguy.battleshipgui.AllyPlayer;
import justacommonguy.battleshipgui.EnemyPlayer;
import justacommonguy.battleshipgui.Faction;
// ? Might want to make the import static
import justacommonguy.battleshipgui.GameServer;
import justacommonguy.battleshipgui.Player;
import justacommonguy.battleshipgui.Result;
import justacommonguy.battleshipgui.Ship;
import justacommonguy.battleshipgui.networking.Request;
import justacommonguy.battleshipgui.ShipLocation;
import justacommonguy.battleshipgui.networking.NetworkComponent;
import justacommonguy.guiutils.GUI;
import justacommonguy.guiutils.SwingUtils;

// !Should keep this in mind: https://www.oracle.com/java/technologies/javase/codeconventions-fileorganization.html#1852
// TODO. Add better exception handling
// TODO. Add better logging
public class BattleshipGUI implements GUI, NetworkComponent {

	public AllyPlayer player;
	public Player<EnemyCell> enemy;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	//? Add a counter of guesses?

	public static final double Y_RESOLUTION = 
			Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	public static final double X_RESOLUTION =
			Toolkit.getDefaultToolkit().getScreenSize().getWidth();

	private JFrame frame = new JFrame();
	private JPanel playerMap;
	private JPanel enemyMap;
	private JLabel enemyGridLabel;
	//TODO. Change this panel when game has started.
	private JPanel buttonPanel;
	private JButton hostButton;
	private JButton joinButton;


	public BattleshipGUI(String hostUsername) {
		//TODO

		if ((hostUsername == null) && (GameServer.settings.getSetting("username").equals(""))) {
			GameServer.settings.setSetting("username", hostUsername);
			GameServer.settings.saveSettings();
		}
		player = new AllyPlayer(GameServer.settings.getSetting("username"));
		enemy = new EnemyPlayer("OPPONENT");
	}

	public String askName() {
		JOptionPane popup = new JOptionPane("Please input your username.", JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
		popup.setWantsInput(true);
		JDialog dialog = popup.createDialog(frame, "Welcome");

		popup.selectInitialValue();
		dialog.setVisible(true);
		dialog.dispose();
		
		return (String) popup.getInputValue();
	}

	@Override
	public void start(int closeOperation) {
		//? Maybe disable close button so the player will quit appropiately.
		//https://www.coderanch.com/t/344419/java/deactivate-close-minimise-resizable-window
		
		/* Size of the elements is based on resolution. 
		GUI might blow up in displays that do not have 16:9 resolutions. */
		SwingUtils.setUpJFrame(frame, (int) ((0.75) * X_RESOLUTION), (int) ((0.75) * Y_RESOLUTION));

		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(3, 3, 3, 3);
		JPanel playArea = new JPanel(new GridBagLayout());
		Font bold = new Font(Font.SANS_SERIF, Font.BOLD, 20);

		playerMap = player.makeMap();
		enemyMap = enemy.makeMap();
		JLabel gridLabel = new JLabel("YOUR GRID");
		enemyGridLabel = new JLabel(enemy.getName() + "'S GRID");
		gridLabel.setFont(bold);
		enemyGridLabel.setFont(bold);

		SwingUtils.setGridBagConstraintsValues(gbc, 0, 0, 0, 0, insets);
		playArea.add(gridLabel, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 1, 0, 0, 0, insets);
		playArea.add(enemyGridLabel, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 0, 1, 0, 0, insets);
		playArea.add(playerMap, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 1, 1, 0, 0, insets);
		playArea.add(enemyMap, gbc);
		JPanel eastPanel = new JPanel();
		eastPanel.add(playArea);


		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JPanel chatPanel = new JPanel();

		hostButton = new JButton("Host game");
		hostButton.addActionListener(new HostGameListener());
		joinButton = new JButton("Join game");
		joinButton.addActionListener(new JoinGameListener());

		buttonPanel.add(hostButton);
		//// buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(joinButton);
		westPanel.add(buttonPanel);
		westPanel.add(chatPanel);

		frame.add(westPanel, BorderLayout.WEST);
		frame.add(eastPanel, BorderLayout.EAST);
		/* For reasons that humanity will never know, 
		the dumb frame needs to be brought to the front when a popup appears. */
		SwingUtils.frameToFront(frame);
		player.placeShipsRandomly();
	}

	public class HostGameListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// The server would be stuck waiting, so the GUI goes crazy if I call it directly. 
			Thread hostThread = new Thread(GameServer.server);
			hostThread.start();
		}
	}

	public class JoinGameListener implements ActionListener, Runnable {

		@Override
		public void actionPerformed(ActionEvent e) {
			Thread joinThread = new Thread(this);
			joinThread.start();
		}

		@Override
		public void run() {
			JLabel info = new JLabel("Please input the host's IP Address and Port.");
			info.setAlignmentX(Component.CENTER_ALIGNMENT);
			JTextField addressField = new JTextField();
			addressField.putClientProperty("JTextField.placeholderText", "IP Address");
			addressField.setText(GameServer.settings.getSetting("default_ip_address"));
			JTextField portField = new JTextField();
			portField.putClientProperty("JTextField.placeholderText", "Port");
			portField.setText(GameServer.settings.getSetting("default_port"));
			JCheckBox saveInfoCheck = new JCheckBox("Save to default settings", true);
			
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(info);
			panel.add(addressField);
			panel.add(portField);
			panel.add(saveInfoCheck);

			int option = JOptionPane.showConfirmDialog(frame, panel, "Join game", 
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (option == JOptionPane.OK_OPTION) {
				if (saveInfoCheck.isSelected()) {
					GameServer.settings.setSetting("default_ip_address", addressField.getText());
					GameServer.settings.setSetting("default_port", portField.getText());
					GameServer.settings.saveSettings();
				}
				joinGame(addressField.getText(), Integer.parseInt(portField.getText()));
			}
		}
	}

	public void joinGame(String ipAddress, int port) {
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
					updateMap((ShipLocation) ois.readObject(), (Result) ois.readObject());
					break;
				case FINISH:
					finish((String) ois.readObject());
					break;
				case PLACE_SHIPS:
					System.out.println("Sending ships.");
					return getShips();
				case SEND_PLAYER:
					System.out.println("Sending " + player.getName());
					return getPlayer();
				case START:
					startGame((Player) ois.readObject());
					break;
				default:
					throw new IOException();
			}
		}
		catch (ClassNotFoundException | IOException e) {
			System.out.println("Failed to respond server requests.");
		}
		
		return null;
	}
	
	public AllyPlayer getPlayer() {
		return player;
	}

	public void startGame(Player opponent) {
		this.opponent = opponent;
		enemyGridLabel.setText(opponent.getName() + "'S GRID");
	}

	public ArrayList<Ship> getShips() {
		//TODO
		return null;
	}

	public ShipLocation getAttack() {
		//TODO
		return null;
	}
	
	public void updateMap(ShipLocation guess, Result result) {
		//TODO
		System.out.println("Updated user map. Guess Location: " + guess + ". Result: " + result);
	}

	public void finish(String winner) {
		//TODO
		System.out.println("Finished the game. Winner is " + winner);
	}
}
