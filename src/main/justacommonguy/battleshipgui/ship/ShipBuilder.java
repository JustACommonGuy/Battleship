package justacommonguy.battleshipgui.ship;

import java.util.ArrayList;

public class ShipBuilder {

	private final int height;
	private final int width;
	private ArrayList<Ship> shipList;

	/** The builder needs to know the field's dimensions. */
	public ShipBuilder(int height, int width) {
		this.height = height;
		this.width = width;
	}

	public ArrayList<Ship> buildShipsRandomly() {
		shipList = new ArrayList<Ship>();
		try {
			for (int size : Ship.SHIP_SIZES) {
				Ship randomShip = null;
				while (!isShipValid(randomShip)) {
					randomShip = Ship.getRandomShip(size, height, width);
				}
				shipList.add(randomShip);
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
