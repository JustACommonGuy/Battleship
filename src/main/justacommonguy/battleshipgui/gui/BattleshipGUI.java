package justacommonguy.battleshipgui.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import justacommonguy.battleshipgui.ClientPlayer;
import justacommonguy.battleshipgui.GameServer;
import justacommonguy.battleshipgui.Player;
import justacommonguy.battleshipgui.ShipLocation;
import justacommonguy.battleshipgui.networking.NetworkComponent;
import justacommonguy.guiutils.GUI;
import justacommonguy.guiutils.SwingUtils;

public class BattleshipGUI implements GUI, NetworkComponent {

	private enum Faction {
		ALLY,
		ENEMY
	}

	private ClientPlayer host;
	private Player opponent;
	private String ipAddress = "127.0.0.1";

	//The height and the width need to have an extra column or row for the letters and numbers.
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
			hostUsername = askName().toUpperCase();
			GameServer.settings.setSetting("username", hostUsername);
			GameServer.settings.saveSettings();
		}
		host = new ClientPlayer(hostUsername);
		
	}

	@Override
	public void start(int closeOperation) {
		//TODO. Maybe disable close button so the player will quit appropiately.
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

		frame.add(eastPanel, BorderLayout.EAST);
		/* For reasons that humanity will never understand, 
		the dumb frame needs to be brought to the front when a popup appears. */
		SwingUtils.frameToFront(frame);
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

	public void joinGame(String ipAddress, int port) {
		//TODO Add a socket to connect to the server.
	}
	
	@Override
	public Object respondRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	public void startGame(Player opponent) {
		this.opponent = opponent;
		enemyGridLabel.setText(opponent.getName() + "'S GRID");
	}

	/** Client only. */
	public ClientPlayer getPlayerInfo() {
		//TODO.
		return null;
	}

	public ArrayList<ShipLocation> getLocations() {
		//TODO.
		return null;
	}
	public ShipLocation getAttack() {
		//TODO.
		return null;
	}
}
