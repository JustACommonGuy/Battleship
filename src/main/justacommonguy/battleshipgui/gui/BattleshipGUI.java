package justacommonguy.battleshipgui.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import justacommonguy.battleshipgui.ClientPlayer;
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
public class BattleshipGUI implements GUI, NetworkComponent {

	private enum Faction {
		ALLY,
		ENEMY
	}

	private ClientPlayer player;
	//? Only the opponent's name might be needed
	private Player opponent;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	//? Add a counter of guesses?
	// TODO. Make the ships draggable
	// https://stackoverflow.com/questions/874360/swing-creating-a-draggable-component
	// https://www.codeproject.com/articles/116088/draggable-components-in-java-swing
	// https://piped.kavin.rocks/watch?v=aedYlXutIDU&quality=dash&dark_mode=true&subtitles=es%2Cen

	// The height and the width need to have an extra column or row for the letters and numbers.
	private static final int HEIGHT = 11;
	private static final int WIDTH = 11;

	private static final double Y_RESOLUTION = 
			Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	private static final double X_RESOLUTION =
			Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	/* GridLayout does not allow for specific cell sizes, so we set a global
		size to display squared cells. */
	private static final Dimension PANEL_SIZE = 
			new Dimension(((int) Y_RESOLUTION / 3), (int) (Y_RESOLUTION / 3));

	private JFrame frame = new JFrame();;
	private JPanel playerMap;
	private JPanel enemyMap;
	private JLabel enemyGridLabel;
	private ArrayList<Cell> playerCells = new ArrayList<Cell>();
	private ArrayList<Cell> enemyCells = new ArrayList<Cell>();
	

	public BattleshipGUI(String hostUsername) {
		//TODO

		if ((hostUsername == null) && (GameServer.settings.getSetting("username").equals(""))) {
			GameServer.settings.setSetting("username", hostUsername);
			GameServer.settings.saveSettings();
		}
		player = new ClientPlayer(GameServer.settings.getSetting("username"));
		
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

		enemyMap = makeMap(enemyCells, Faction.ENEMY);
		playerMap = makeMap(playerCells, Faction.ALLY);
		JLabel gridLabel = new JLabel("YOUR GRID");
		enemyGridLabel = new JLabel("OPPONENT'S GRID");
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
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JPanel chatPanel = new JPanel();

		JButton hostButton = new JButton("Host game");
		hostButton.addActionListener(new HostGameListener());
		JButton joinButton = new JButton("Join game");
		joinButton.addActionListener(new JoinGameListener());

		buttonPanel.add(hostButton);
		//// buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(joinButton);
		westPanel.add(buttonPanel);
		westPanel.add(chatPanel);

		frame.add(westPanel, BorderLayout.WEST);
		frame.add(eastPanel, BorderLayout.EAST);
		/* For reasons that humanity will never understand, 
		the dumb frame needs to be brought to the front when a popup appears. */
		SwingUtils.frameToFront(frame);
	}

	private JPanel makeMap(ArrayList<Cell> cellList, Faction faction) {
		GridLayout grid = new GridLayout(HEIGHT, WIDTH);
		grid.setVgap(1);
		grid.setHgap(1);
		JPanel map = new JPanel(grid);
		map.setPreferredSize(PANEL_SIZE);
		map.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		
		int cellX = 0;
		int cellY = 0;
		/* Technically speaking, I'm declaring one more initiator than I need 
		(the first column and row are unused as cells),
		but the program goes to hell if I refactor it, so it stays like that. */
		HighlightInitiator[] xHighlightInitiators = new HighlightInitiator[HEIGHT];
		HighlightInitiator[] yHighlightInitiators = new HighlightInitiator[WIDTH];

		for (int i = 0; i < HEIGHT; i++) {
			xHighlightInitiators[i] = new HighlightInitiator();
		}
		for (int i = 0; i < WIDTH; i++) {
			yHighlightInitiators[i] = new HighlightInitiator();
		}

		char rowLetter = 'A';
		int columnNumber = 1;
		for (int i = 0; i < HEIGHT*WIDTH; i++) {
			
			if ((cellX == 0) && (cellY == 0)) {
				JLabel emptyLabel = new JLabel("", SwingConstants.CENTER);
				map.add(emptyLabel);
			}
			
			else if ((cellX <= WIDTH) && (cellY == 0)) {
				JLabel letterLabel = new JLabel(String.valueOf(rowLetter), SwingConstants.CENTER);
				map.add(letterLabel);
				rowLetter++;
			}
			
			else if (cellX == 0) {
				JLabel numberLabel = new JLabel(String.valueOf(columnNumber), SwingConstants.CENTER);
				map.add(numberLabel);
				columnNumber++;
			}
			
			/* The cells need the initiators so they can fire them. 
			Ironically, the initiators need the listener cells as well. */
			else {
				Cell cell = null;
				
				switch (faction) {
					case ALLY:
						cell = new AllyCell(new ShipLocation(cellX, cellY), 
								xHighlightInitiators[cellX], yHighlightInitiators[cellY]);
						break;
					case ENEMY:
						cell = new EnemyCell(new ShipLocation(cellX, cellY), 
								xHighlightInitiators[cellX], yHighlightInitiators[cellY]);
						break;
					default:
						break;
				}

				xHighlightInitiators[cellX].addHighlightListener(cell);
				yHighlightInitiators[cellY].addHighlightListener(cell);

				map.add(cell);
				cellList.add(cell);
			}
			
			cellX++;
			
			if (cellX >= WIDTH) {
				cellX = 0;
				cellY++;
			}
		}

		return map;
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
			Thread jointThread = new Thread(this);
			jointThread.start();
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

			int result = JOptionPane.showConfirmDialog(frame, panel, "Join game", 
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
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
					return getAttack();
				case ATTACK_RESULT:
					updateMap((ShipLocation) ois.readObject(), (Result) ois.readObject());
					break;
				case FINISH:
					finish((String) ois.readObject());
					break;
				case PLACE_SHIPS:
					return getLocations();
				case SEND_PLAYER:
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
	
	/** Client only. */
	public ClientPlayer getPlayer() {
		System.out.println(player.getName());
		return player;
	}

	public void startGame(Player opponent) {
		this.opponent = opponent;
		enemyGridLabel.setText(opponent.getName() + "'S GRID");
	}

	public ArrayList<Ship> getLocations() {
		//TODO.
		return null;
	}

	public ShipLocation getAttack() {
		//TODO.
		return null;
	}
	
	public void updateMap(ShipLocation guess, Result result) {
		//TODO
	}

	public void finish(String winner) {
		//TODO
	}
}
