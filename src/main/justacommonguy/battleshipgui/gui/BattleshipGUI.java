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
import java.util.HashMap;
import java.util.Random;

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
import justacommonguy.battleshipgui.Faction;
// ? Might want to make the import static
import justacommonguy.battleshipgui.GameServer;
import justacommonguy.battleshipgui.Player;
import justacommonguy.battleshipgui.Result;
import justacommonguy.battleshipgui.Ship;
import justacommonguy.battleshipgui.networking.Request;
import justacommonguy.battleshipgui.ShipLocation;
import justacommonguy.battleshipgui.Ship.Axis;
import justacommonguy.battleshipgui.networking.NetworkComponent;
import justacommonguy.guiutils.GUI;
import justacommonguy.guiutils.SwingUtils;

// !Should keep this in mind: https://www.oracle.com/java/technologies/javase/codeconventions-fileorganization.html#1852
// TODO. Add better exception handling
// TODO. Add better logging
public class BattleshipGUI implements GUI, NetworkComponent {

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
	public static final int HEIGHT = 11;
	public static final int WIDTH = 11;
	/** Array for all ship sizes. The order must match with 
	 * {@link justacommonguy.battleshipgui.Ship#SHIP_NAMES SHIP_NAMES}.*/
	public static final int[] SHIP_SIZES = {2, 3, 3, 4, 5};

	private static final double Y_RESOLUTION = 
			Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	private static final double X_RESOLUTION =
			Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	/** GridLayout does not allow for specific cell sizes, so we set a global
		size to display squared cells. */
	private static final Dimension PANEL_SIZE = 
			new Dimension(((int) Y_RESOLUTION / 3), (int) (Y_RESOLUTION / 3));

	private JFrame frame = new JFrame();
	private JPanel playerMap;
	private JPanel enemyMap;
	private JLabel enemyGridLabel;
	//TODO. Change this panel when game has started.
	private JPanel buttonPanel;
	private JButton hostButton;
	private JButton joinButton;

	private ArrayList<Ship> shipList;
	// This is the weirdest thing I've ever typed, but it's the best design I can come up with.
	private HashMap<Faction, HashMap<ShipLocation, Cell>> cells = 
			new HashMap<Faction, HashMap<ShipLocation, Cell>>();
	

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

		enemyMap = makeMap(cells, Faction.ENEMY);
		playerMap = makeMap(cells, Faction.ALLY);
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
		placeShipsRandomly();
	}

	private JPanel makeMap(HashMap<Faction, HashMap<ShipLocation, Cell>> cells, Faction faction) {
		GridLayout grid = new GridLayout(HEIGHT, WIDTH);
		grid.setVgap(1);
		grid.setHgap(1);
		JPanel map = new JPanel(grid);
		map.setPreferredSize(PANEL_SIZE);
		map.setBorder(BorderFactory.createLineBorder(Color.WHITE));

		HashMap<ShipLocation, Cell> cellList = new HashMap<ShipLocation, Cell>();
		cells.put(faction, cellList);
		
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
				ShipLocation location = new ShipLocation(cellX, cellY);
				
				switch (faction) {
					case ALLY:
						cell = new AllyCell(location, 
								xHighlightInitiators[cellY], yHighlightInitiators[cellX]);
						break;
					case ENEMY:
						cell = new EnemyCell(location, 
								xHighlightInitiators[cellY], yHighlightInitiators[cellX]);
						break;
					default:
						break;
				}
				xHighlightInitiators[cellY].addHighlightListener(cell);
				yHighlightInitiators[cellX].addHighlightListener(cell);

				map.add(cell);
				cellList.put(location, cell);
			}
			
			cellX++;
			if (cellX >= WIDTH) {
				cellX = 0;
				cellY++;
			}
		}

		return map;
	}

	private void placeShipsRandomly() {
		shipList = new ArrayList<Ship>();
		try {
			for (int size : SHIP_SIZES) {
				Ship randomShip = getRandomShip(size);
				shipList.add(randomShip);
				for (ShipLocation location : randomShip.getLocations()) {
					AllyCell cell = (AllyCell) getCell(location, Faction.ALLY);
					cell.setShip(randomShip);
				}
			}
		}
		catch (RandomShipFailure e) {
			System.out.println("ERROR: Could not place random ships.");
		}
	}
	
	private Ship getRandomShip(int shipSize) throws RandomShipFailure {
		ArrayList<ShipLocation> locations = new ArrayList<ShipLocation>();
		ShipLocation location = null;
		int attemptsCount = 0;
		boolean isSuccessful = false;
		Axis axis = Axis.X;

		if (new Random().nextBoolean()) {
			axis = Axis.Y;
		}

		while (!isSuccessful && (attemptsCount++ < 3*HEIGHT*WIDTH)) {
			int x = (int) (Math.random() * (WIDTH -1) + 1);
			int y = (int) (Math.random() * (HEIGHT - 1) + 1);
			location = new ShipLocation(x, y);

			int position = 0;
			isSuccessful = true;
			while (isSuccessful && (position++ < shipSize)) {
				isSuccessful = isLocationUsed(location);
				if ((x > (WIDTH - 1)) || (x < 0)) {
					isSuccessful = false;
				}
				if ((y > (HEIGHT - 1)) || (y < 0)) {
					isSuccessful = false;
				}
				if (isSuccessful) {
					locations.add(location);
					switch (axis) {
						case X:
							location = new ShipLocation(++x, y);
							break;
						case Y:
							location = new ShipLocation(x, ++y);
							break;
						default:
							break;
					}
				}
				else {
					locations.clear();
				}
			}
		}

		if (isSuccessful == false) {
			throw new RandomShipFailure("Could not generate random ship.");
		}

		System.out.println("Size of ship: " + locations.size());
		return new Ship(locations);
	}

	/** Only applies for AllyCell. Only used by getRandomShip() */
	private boolean isLocationUsed(ShipLocation location) {
		for (Ship existingShip : shipList) {
			for (ShipLocation existingLocation : existingShip.getLocations()) {
				if (location.equals(existingLocation)) {
					return false;
				}
			}
		}
		return true;
	}

	public Cell getCell(ShipLocation location, Faction faction) {
		HashMap<ShipLocation, Cell> cellList = cells.get(faction);
		if (!cellList.containsKey(location)) {
			System.out.println("Ouch. " + location);
		}
		return cellList.get(location);
	}

	public ArrayList<Cell> getCellList(ArrayList<ShipLocation> locationList, Faction faction) {
		ArrayList<Cell> cellList = new ArrayList<Cell>();
		
		for (ShipLocation location : locationList) {
			cellList.add(getCell(location, faction));
		}
		
		return cellList;
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
	
	public ClientPlayer getPlayer() {
		return player;
	}

	public void startGame(Player opponent) {
		this.opponent = opponent;
		enemyGridLabel.setText(opponent.getName() + "'S GRID");
	}

	public ArrayList<Ship> getShips() {
		//TODO.
		return null;
	}

	public ShipLocation getAttack() {
		//TODO.
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
