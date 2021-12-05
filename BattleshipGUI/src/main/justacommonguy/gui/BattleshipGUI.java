package justacommonguy.gui;

import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import justacommonguy.ShipLocation;
import justacommonguy.guiutils.GUI;
import justacommonguy.guiutils.SwingUtils;

public class BattleshipGUI implements GUI{

	private enum Faction {
		ALLY,
		ENEMY
	}

	//Whatever the height or the width are, they need to be one more for the letters and numbers.
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

	private JPanel playerMap;
	private JPanel enemyMap;
	private ArrayList<Cell> playerCells = new ArrayList<Cell>();
	private ArrayList<Cell> enemyCells = new ArrayList<Cell>();

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new FlatSpacegrayIJTheme());
		}
		catch (Exception ex) {
			System.out.println("Failed to set LaF");
		}
		new BattleshipGUI().start(JFrame.EXIT_ON_CLOSE);;
	}

	@Override
	public void start(int closeOperation) {
		/* Size of elements is based on resolution. Might blow up in displays that are not 16:9. */
		JFrame frame = new JFrame();
		SwingUtils.setUpJFrame(frame, (int) ((0.75) * X_RESOLUTION), (int) ((0.75) * Y_RESOLUTION));

		JPanel playArea = new JPanel();
		enemyMap = makeMap(enemyCells, Faction.ENEMY);
		playerMap = makeMap(playerCells, Faction.ALLY);
		playArea.add(playerMap);
		playArea.add(enemyMap);

		frame.add(playArea, BorderLayout.EAST);
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
}
