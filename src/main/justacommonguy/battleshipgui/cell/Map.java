package justacommonguy.battleshipgui.cell;
import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import justacommonguy.battleshipgui.ship.ShipLocation;
import justacommonguy.battleshipgui.utils.Result;

public abstract class Map<T extends Cell> {

	// The height and the width need to have an extra column or row for the letters and numbers.
	public static final int HEIGHT = 11;
	public static final int WIDTH = 11;
	private static final int GRID_GAP = Integer.parseInt(gameSettings.getSetting("grid_gap"));

	private final HashMap<ShipLocation, T> cellList = new HashMap<>();

	public T getCell(ShipLocation location) {
		return cellList.get(location);
	}

	public ArrayList<T> getCellList(ArrayList<ShipLocation> locationList) {
		if (locationList == null) {
			return null;
		}
		ArrayList<T> cellList = new ArrayList<>();
		
		for (ShipLocation location : locationList) {
			T cell = getCell(location);
			if (cell == null) {
				return null;
			}
			cellList.add(cell);
		}
		
		return cellList;
	}

	/** We need the result because the map doesn't know if the location is going to be a hit. */
	public void attackCell(ShipLocation location, Result result) {
		//// System.out.println("Attacked: " + location);
		T cell = getCell(location);

		switch (result) {
			case HIT:
				cell.setCellColor(cell.getHitColor());
				break;
			case KILL:
				cell.setCellColor(cell.getKillColor());
				break;
			case MISS:
				cell.setMiss();
				break;
		}
	}

	/* In a perfect world this method would be in a dedicated class and split into several
	methods, but I failed to achieve this after an hour of pain and suffering. I give up. */
	public JPanel makeMap(Dimension size) {
		if (!cellList.isEmpty()) {
			System.out.println("Map has already been created.");
			return null;
		}

		GridLayout grid = new GridLayout(HEIGHT, WIDTH);
		grid.setVgap(GRID_GAP);
		grid.setHgap(GRID_GAP);
		JPanel map = new JPanel(grid);
		map.setPreferredSize(size);
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
				T cell = constructCell(location, xHighlightInitiators[cellY], 
						yHighlightInitiators[cellX]);

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

	protected abstract T constructCell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit);
	
	public abstract void allowInteraction(boolean allow);
}
