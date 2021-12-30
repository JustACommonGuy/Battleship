package justacommonguy.battleshipgui.player;

import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import justacommonguy.battleshipgui.BattleshipGUI;
import justacommonguy.battleshipgui.cell.Cell;
import justacommonguy.battleshipgui.cell.HighlightInitiator;
import justacommonguy.battleshipgui.ship.ShipLocation;
import justacommonguy.battleshipgui.utils.Faction;

public abstract class Player<T extends Cell> implements Serializable, Cloneable {

	// The height and the width need to have an extra column or row for the letters and numbers.
	public static final int HEIGHT = 11;
	public static final int WIDTH = 11;

	private static final int GRID_GAP = Integer.parseInt(gameSettings.getSetting("grid_gap"));
	/** GridLayout does not allow for specific cell sizes, so we set a global
		size to display squared cells. */
	private static final Dimension PANEL_SIZE = new Dimension(
			(int) ( BattleshipGUI.Y_RESOLUTION / 3), (int) (BattleshipGUI.Y_RESOLUTION / 3));

	protected String name;
	protected HashMap<ShipLocation, T> cellList = new HashMap<ShipLocation, T>();
	private Faction faction;

	public Player(String name, Faction faction) {
		this.name = name;
		this.faction = faction;
	}

	public T getCell(ShipLocation location) {
		//// if (!cellList.containsKey(location)) {
		//// 	System.out.println("Ouch. " + location);
		//// }
		return cellList.get(location);
	}

	public ArrayList<T> getCellList(ArrayList<ShipLocation> locationList) {
		if (locationList == null) {
			return null;
		}
		ArrayList<T> cellList = new ArrayList<T>();
		
		for (ShipLocation location : locationList) {
			T cell = getCell(location);
			if (cell == null) {
				return null;
			}
			cellList.add(cell);
		}
		
		return cellList;
	}

	/* In a perfect world this method would be in a dedicated class and split into several
	methods, but after an hour of pain and suffering I just give up. */
	public JPanel makeMap() {
		if (!cellList.isEmpty()) {
			System.out.println("Map has already been created.");
			return null;
		}

		GridLayout grid = new GridLayout(HEIGHT, WIDTH);
		grid.setVgap(GRID_GAP);
		grid.setHgap(GRID_GAP);
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
		
		char columnChar = 'A';
		/* Unsupported feature. It just follows the ASCII table. It will use other symbols 
		 after 9. */
		if (Boolean.parseBoolean(gameSettings.getSetting("column_numbers"))) {
			columnChar = '1';
		}
		int rowNumber = 1;

		for (int i = 0; i < HEIGHT*WIDTH; i++) {
			if ((cellX == 0) && (cellY == 0)) {
				JLabel emptyLabel = new JLabel("", SwingConstants.CENTER);
				map.add(emptyLabel);
			}
			else if (cellY == 0) {
				JLabel letterLabel = new JLabel(Character.toString(columnChar++), SwingConstants.CENTER);
				map.add(letterLabel);
			}
			else if (cellX == 0) {
				JLabel numberLabel = new JLabel(Integer.toString(rowNumber++), SwingConstants.CENTER);
				map.add(numberLabel);
			}
			
			/* The cells need the initiators so they can fire them. 
			Ironically, the initiators need the listener cells as well. */
			else {
				ShipLocation location = new ShipLocation(cellX, cellY);
				T cell = Cell.getInstance(location, xHighlightInitiators[cellY], 
						yHighlightInitiators[cellX], faction);

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

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException ex) {
			throw new RuntimeException("Could not clone player.");
		}
	}

	public void setName(String name) {
		if ((name == null) || (name.equals(""))) {
			throw new IllegalArgumentException("Name must not be null or empty.");
		}
		this.name = name;
	}
}
