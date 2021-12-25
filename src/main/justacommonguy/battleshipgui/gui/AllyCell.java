package justacommonguy.battleshipgui.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static justacommonguy.battleshipgui.GameLauncher.gameGUI;
import static justacommonguy.battleshipgui.GameLauncher.gameSettings;
import justacommonguy.battleshipgui.Ship;
import justacommonguy.battleshipgui.ShipLocation;

public class AllyCell extends Cell implements DragListener {

	//!
	private static DragInitiator dragInitiator = new DragInitiator();
	private static boolean placementAllowed = true;
	private static AllyCell oldCell;
	private static ArrayList<AllyCell> oldCellList;
	private static Ship oldShip;

	private static final Color MISS_COLOR = gameSettings.getColor("ally_miss_color");
	private static final Color SHIP_COLOR = gameSettings.getColor("ally_ship_color");
	private static final Color HIT_COLOR = gameSettings.getColor("ally_hit_color");
	private static final Color KILL_COLOR = gameSettings.getColor("ally_kill_color");

	private Ship ship;

	public AllyCell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit) {
		super(location, xInit, yInit);
	}

	public void setShip(Ship ship) {
		this.ship = ship;
		if (ship == null) {
			setCellColor(DEFAULT);
		}
		else {
			setCellColor(SHIP_COLOR);
		}
	}

	public void removeOldShip() {
		for (AllyCell cell : oldCellList) {
			cell.setShip(null);
		}
		removeOldCell();
	}

	public void setOldCell() {
		oldCell = this;
		oldCellList = oldCell.getRelatedCells();
		oldShip = oldCell.getShip();
	}

	public static void removeOldCell() {
		oldCell = null;
		oldCellList = null;
		oldShip = null;
	}

	@Override
	public void shipDragged() {
		if ((oldCell != null) && (oldCell != this)) {
			ArrayList<ShipLocation> newLocations = oldShip.getNewLocations(
					oldCell.getShipLocation(), super.location);
			
			boolean placementValid = true;
			if (newLocations != null) {
				ArrayList<Cell> newCells = GameServer.gui.getCellList(newLocations, Faction.ALLY);
				for (Cell cell : newCells) {
					AllyCell newCell = (AllyCell) cell;
					if (newCell.hasShip()) {
						placementValid = false;
					}
				}
				
				if (placementValid) {
					Ship oldShip = AllyCell.oldShip;
					removeOldShip();
					for (Cell cell : newCells) {
						AllyCell newCell = (AllyCell) cell;
						newCell.setShip(oldShip);
					}
					oldShip.setLocations(newLocations);
					mouseExited(null);
				}
			}
			else {
				placementValid = false;
			}
			if (!placementValid) {
				removeOldCell();
			}
		}
	}

	@Override
	public void draggingShip() {
		if (oldCell != null) {
			ArrayList<ShipLocation> newLocations = oldShip.getNewLocations(
					oldCell.getShipLocation(), super.location);
			
			if (newLocations != null) {
				ArrayList<Cell> newCells = GameServer.gui.getCellList(newLocations, Faction.ALLY);
				for (Cell cell : newCells) {
					AllyCell newCell = (AllyCell) cell;
					newCell.highlightPlaceholder(SHIP_COLOR);
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (placementAllowed && (ship != null)) {
			setOldCell();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (placementAllowed) {
			dragInitiator.dragged();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (placementAllowed) {
			dragInitiator.setDragListener(this);
			if (oldCell != null) {
				super.mouseEntered(null);
				dragInitiator.dragging();
			}
		}
		else {
			super.mouseEntered(e);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (placementAllowed) {
			//TODO Change ship orientation
		}
	}

	public void highlightPlaceholder(Color color) {
		int increment = fixIncrement(-50, color);
		color = new Color(color.getRed()+increment, color.getGreen()+increment, color.getBlue()+increment);

		boolean isOwnShip = false;
		for (AllyCell cell : oldCellList) {
			if (cell.equals(this)) {
				isOwnShip = true;
			}
		}

		if ((oldColor != DEFAULT) && (isOwnShip == false)) {
			setBackground(KILL_COLOR);
		}
		else {
			setBackground(color);
		}
	}

	public ArrayList<AllyCell> getRelatedCells() {
		if (ship == null) {
			return null;
		}

		ArrayList<AllyCell> allyCells = new ArrayList<AllyCell>();
		ArrayList<Cell> cells = GameServer.gui.getCellList(ship.getLocations(), Faction.ALLY);
		for (Cell cell : cells) {
			allyCells.add((AllyCell) cell);
		}

		return allyCells;
	}

	public boolean hasShip() {
		if (oldCell != null) {
			for (AllyCell cell : oldCellList) {
				if (cell.equals(this)) {
					return false;
				}
			}
		}

		return ship != null;
	}

	public Ship getShip() {
		return ship;
	}

	@Override
	public Color getMissColor() {
		return MISS_COLOR;
	}

	@Override
	public Color getShipColor() {
		return SHIP_COLOR;
	}

	@Override
	public Color getHitColor() {
		return HIT_COLOR;
	}

	@Override
	public Color getKillColor() {
		return KILL_COLOR;
	}
}
