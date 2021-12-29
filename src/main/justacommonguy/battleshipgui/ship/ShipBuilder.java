package justacommonguy.battleshipgui.ship;

import static justacommonguy.battleshipgui.BattleshipGUI.gameGUI;

import java.util.ArrayList;

import justacommonguy.battleshipgui.cell.AllyCell;

public class ShipBuilder {

	private final int height;
	private final int width;
	private ArrayList<Ship> shipList = new ArrayList<Ship>();

	/** The builder needs to know the field's dimensions. */
	public ShipBuilder(int height, int width) {
		this.height = height;
		this.width = width;
	}

	public ArrayList<Ship> placeShipsRandomly() {
		try {
			for (int size : Ship.SHIP_SIZES) {
				Ship randomShip = null;
				while (!isShipValid(randomShip)) {
					randomShip = Ship.getRandomShip(size, height, width);
				}
				shipList.add(randomShip);
				for (ShipLocation location : randomShip.getLocations()) {
					AllyCell cell = gameGUI.player.getCell(location);
					cell.setShip(randomShip);
				}
			}
			return shipList;
		}
		catch (RandomShipFailure e) {
			System.out.println("ERROR: Could not place random ships.");
			return null;
		}
	}

	private boolean isShipValid(Ship ship) {
		if (ship == null) {
			return false;
		}
		for (ShipLocation location : ship.getLocations()) {
			if (isLocationUsed(location)) {
				return false;
			}
		}
		return true;
	}

	private boolean isLocationUsed(ShipLocation location) {
		if (location == null) {
			throw new IllegalArgumentException("Location must not be null.");
		}
		for (Ship existingShip : shipList) {
			if (existingShip.getLocations().contains(location)) {
				return true;
			}
		}
		return false;
	}
}
