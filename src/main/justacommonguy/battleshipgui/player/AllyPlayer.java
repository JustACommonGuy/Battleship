package justacommonguy.battleshipgui.player;

import java.util.ArrayList;

import justacommonguy.battleshipgui.cell.AllyCell;
import justacommonguy.battleshipgui.ship.Ship;
import justacommonguy.battleshipgui.ship.ShipBuilder;
import justacommonguy.battleshipgui.utils.Faction;

public class AllyPlayer extends Player<AllyCell> {

	private ArrayList<Ship> shipList = new ArrayList<Ship>();

	public AllyPlayer(String name) {
		super(name, Faction.ALLY);
	}

	public void buildShips() {
		shipList = new ShipBuilder(HEIGHT, WIDTH).placeShipsRandomly();
	}
	
	// public void placeShipsRandomly() {
	// 	try {
	// 		for (int size : Ship.SHIP_SIZES) {
	// 			Ship randomShip = null;
	// 			while (!isShipValid(randomShip)) {
	// 				randomShip = Ship.getRandomShip(size, HEIGHT, WIDTH);
	// 			}
	// 			shipList.add(randomShip);
	// 			for (ShipLocation location : randomShip.getLocations()) {
	// 				AllyCell cell = getCell(location);
	// 				cell.setShip(randomShip);
	// 			}
	// 		}
	// 	}
	// 	catch (RandomShipFailure e) {
	// 		System.out.println("ERROR: Could not place random ships.");
	// 	}
	// }

	// private boolean isShipValid(Ship ship) {
	// 	if (ship == null) {
	// 		return false;
	// 	}
	// 	for (ShipLocation location : ship.getLocations()) {
	// 		if (isLocationUsed(location)) {
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// private boolean isLocationUsed(ShipLocation location) {
	// 	if (location == null) {
	// 		throw new IllegalArgumentException("Location must not be null.");
	// 	}
	// 	for (Ship existingShip : shipList) {
	// 		if (existingShip.getLocations().contains(location)) {
	// 			return true;
	// 		}
	// 	}
	// 	return false;
	// }

	/** Get cells that share the same ship. */
	public ArrayList<AllyCell> getRelatedCells(AllyCell cell) {
		if (!cell.hasShip()) {
			return null;
		}
		return getCellList(cell.getShip().getLocations());
	}

	public ArrayList<Ship> getShipList() {
		return shipList;
	}
}
