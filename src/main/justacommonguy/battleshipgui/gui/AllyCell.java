package justacommonguy.battleshipgui.gui;

import static justacommonguy.battleshipgui.Settings.gameSettings;
import static justacommonguy.battleshipgui.gui.BattleshipGUI.gameGUI;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import justacommonguy.battleshipgui.Ship;
import justacommonguy.battleshipgui.ShipLocation;

public class AllyCell extends Cell implements DragListener, MouseWheelListener {

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

	@Override
	public void mousePressed(MouseEvent e) {
		if (placementAllowed && (ship != null)) {
			setOldCell();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);
		if (placementAllowed) {
			dragInitiator.setDragListener(this);
			if (oldCell != null) {
				dragInitiator.dragging();
			}
		}
	}

	@Override
	public void draggingShip() {
		if (oldCell == null) {
			throw new NullPointerException("Static oldCell is null.");
		}
		ArrayList<ShipLocation> newLocations = oldShip.getDraggedLocations(
				oldCell.getShipLocation(), super.location);
		
		if (newLocations != null) {
			ArrayList<AllyCell> newCells = gameGUI.player.getCellList(newLocations);
			if (newCells != null) {
				for (AllyCell newCell : newCells) {
					newCell.highlightPlaceholder(SHIP_COLOR);
				}
			}
		}
	}

	/** Temporary highlighting when the ship is being dragged. */
	public void highlightPlaceholder(Color color) {
		int increment = fixIncrement(-50, color);
		color = new Color(color.getRed() + increment, color.getGreen() + increment, 
				color.getBlue() + increment);

		boolean isOwnShip = hasOldShip();
		if ((oldColor != DEFAULT) && (isOwnShip == false)) {
			setBackground(KILL_COLOR);
		}
		else {
			setBackground(color);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (placementAllowed) {
			if (oldCell != null) {
				dragInitiator.dragged();
			}
		}
	}

	@Override
	public void shipDragged() {
		if (oldCell == null) {
			throw new NullPointerException("Static oldCell is null.");
		}
		if (oldCell == this) {
			return;
		}

		ArrayList<ShipLocation> newLocations = oldShip.getDraggedLocations(
				oldCell.getShipLocation(), super.location);
		
		moveShip(newLocations);
	}

	private void moveShip(ArrayList<ShipLocation> newLocations) {
		ArrayList<AllyCell> newCells = gameGUI.player.getCellList(newLocations);
		boolean placementValid = true;
		// This already checks if the arraylist is null.
		if (!areCellsValid(newCells)) {
			placementValid = false;
		}
		
		if (placementValid) {
			Ship oldShip = AllyCell.oldShip;
			removeOldShip();
			for (AllyCell newCell : newCells) {
				newCell.setShip(oldShip);
			}
			oldShip.setLocations(newLocations);
		}

		if (!placementValid) {
			removeOldCell();
		}
		super.mouseEntered(null);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (placementAllowed && (ship != null)) {
			setOldCell();
			// Direction must have an absolute value of 1.
			int direction = e.getWheelRotation() / Math.abs(e.getWheelRotation());
			ArrayList<ShipLocation> newLocations = ship.getRotatedLocations(direction, location);
			moveShip(newLocations);
		}
	}
	
	public void setShip(Ship ship) {
		this.ship = ship;
		if (ship == null) {
			setCellColor(DEFAULT);
			removeMouseWheelListener(this);
		}
		else {
			addMouseWheelListener(this);
			setCellColor(SHIP_COLOR);
		}
	}

	public void setOldCell() {
		oldCell = this;
		oldCellList = oldCell.getRelatedCells();
		oldShip = oldCell.ship;
	}

	public void removeOldShip() {
		for (AllyCell cell : oldCellList) {
			cell.setShip(null);
		}
		removeOldCell();
	}

	/** Get rid of the static variables when transition is done. */
	public static void removeOldCell() {
		oldCell = null;
		oldCellList = null;
		oldShip = null;
	}

	/** Get cells that share the same ship. */
	public ArrayList<AllyCell> getRelatedCells() {
		if (ship == null) {
			return null;
		}
		return gameGUI.player.getCellList(ship.getLocations());
	}

	/** Placement is not valid if new locations are out of bounds (negative coords or not found), 
	* or if the cells are already occupied. */
	public static boolean areCellsValid(ArrayList<AllyCell> cells) {
		boolean isValid = true;
		if (cells == null) {
			return false;
		}

		for (AllyCell cell : cells) {
			if (cell.hasAnotherShip()) {
				isValid = false;
			}
		}

		return isValid;
	}

	/** We don't consider that it has a ship if it is the same that is being modified. */
	public boolean hasAnotherShip() {
		if (hasOldShip()) {
			return false;
		}
		return ship != null;
	}

	public boolean hasOldShip() {
		if (oldCellList != null) {
			for (AllyCell cell : oldCellList) {
				if (cell.equals(this)) {
					return true;
				}
			}
		}
		return false;
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
