package justacommonguy.battleshipgui.cell;

import java.util.ArrayList;

import justacommonguy.battleshipgui.ship.Ship;
import justacommonguy.battleshipgui.ship.ShipLocation;

class ShipMover {
	
	private static AllyMap map;
	private ArrayList<AllyCell> oldCellList;
	private Ship oldShip;
	private ShipLocation newLocation;
	private ShipLocation oldLocation;

	ShipMover(AllyCell oldCell) {
		if (oldCell == null) {
			throw new IllegalArgumentException("AllyCell must not be null.");
		}
		if (map == null) {
			throw new RuntimeException("Map has not been initialized.");
		}
		oldCellList = map.getRelatedCells(oldCell);
		oldShip = oldCell.getShip();
		this.oldLocation = oldCell.getShipLocation();
		/* Unless otherwise stated, the ship does not move. */
		newLocation = oldCell.getShipLocation();
	}

	public static void setMap(AllyMap map) {
		ShipMover.map = map;
	} 

	public void drag() {
		ArrayList<ShipLocation> newLocations = oldShip.getDraggedLocations(
				oldLocation, newLocation);
		
		if (newLocations != null) {
			ArrayList<AllyCell> newCells = map.getCellList(newLocations);
			if (newCells != null) {
				for (AllyCell newCell : newCells) {
					newCell.highlightDragging();
				}
			}
		}
	}

	public void drop() {
		if (oldLocation.equals(newLocation)) {
			return;
		}

		ArrayList<ShipLocation> newLocations = oldShip.getDraggedLocations(
				oldLocation, newLocation);
		
		moveShip(newLocations);
	}

	/** Rotates the ship around the center. Direction must have an absolute value of 1. */
	public void rotate(int direction, ShipLocation center) {
		setNewLocation(center);
		ArrayList<ShipLocation> newLocations = oldShip.getRotatedLocations(direction, center);
		moveShip(newLocations);
	}

	private void moveShip(ArrayList<ShipLocation> newLocations) {
		ArrayList<AllyCell> newCells = map.getCellList(newLocations);
		boolean placementValid = true;
		// This already checks if the arraylist is null.
		if (!areCellsValid(newCells)) {
			placementValid = false;
		}
		
		if (placementValid) {
			updateShip(newLocations);
		}
		else {
			failureHighlight();
		}
		map.getCell(newLocation).highlightHover();
	}

	private void failureHighlight() {
		new Thread(() -> {
			for (AllyCell cell : oldCellList) {
				cell.setCellColor(cell.getKillColor());
			}

			try {
				Thread.sleep(300);
			}
			catch (InterruptedException e) {}

			for (AllyCell cell : oldCellList) {
				// Check if the cell still has a ship because it might have moved during the sleep.
				if (cell.hasShip()) {
					cell.setCellColor(cell.getShipColor());
				}
				boolean highlighted = cell.isHighlighted();
				if (highlighted) {
					cell.cellHighlighted();
				}
			}
		}, "FailureHighlighter").start();
	}

	private void updateShip(ArrayList<ShipLocation> newLocations) {
		ArrayList<AllyCell> newCells = map.getCellList(newLocations);
		for (AllyCell cell : oldCellList) {
			cell.setShip(null);
		}
		for (AllyCell newCell : newCells) {
			newCell.setShip(oldShip);
		}
		oldShip.setLocations(newLocations);
	}

	/** Placement is not valid if new locations are out of bounds (negative coords or not found), 
	* or if the cells are already occupied. */
	public boolean areCellsValid(ArrayList<AllyCell> cells) {
		boolean isValid = true;
		if (cells == null) {
			return false;
		}
	
		for (AllyCell cell : cells) {
			if (cell.hasShip() && !cell.hasShip(oldShip)) {
				isValid = false;
			}
		}
	
		return isValid;
	}

	public void setNewLocation(ShipLocation newLocation) {
		this.newLocation = newLocation;
	}
}
