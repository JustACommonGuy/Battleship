package justacommonguy.battleshipgui.cell;

import java.util.ArrayList;

import justacommonguy.battleshipgui.ship.Ship;
import justacommonguy.battleshipgui.ship.ShipLocation;

public class AllyMap extends Map<AllyCell> {

	@Override
	protected AllyCell constructCell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit) {
		return new AllyCell(location, xInit, yInit);
	}
	
	public void placeShips(ArrayList<Ship> shipList) {
		for (Ship ship : shipList) {
			for (ShipLocation location : ship.getLocations()) {
				AllyCell cell = getCell(location);
				cell.setShip(ship);
			}
		}
	}

	/** Get cells that share the same ship. */
	public ArrayList<AllyCell> getRelatedCells(AllyCell cell) {
		if (!cell.hasShip()) {
			return null;
		}
		return getCellList(cell.getShip().getLocations());
	}
}
