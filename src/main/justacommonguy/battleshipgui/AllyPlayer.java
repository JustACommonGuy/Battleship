package justacommonguy.battleshipgui;

import java.util.ArrayList;
import java.util.Random;

import justacommonguy.battleshipgui.Ship.Axis;
import justacommonguy.battleshipgui.gui.AllyCell;
import justacommonguy.battleshipgui.gui.RandomShipFailure;

public class AllyPlayer extends Player<AllyCell> {

	/** Array for all ship sizes. The order must match with 
	 * {@link justacommonguy.battleshipgui.Ship#SHIP_NAMES SHIP_NAMES}.*/
	public static final int[] SHIP_SIZES = {2, 3, 3, 4, 5};

	private ArrayList<Ship> shipList = new ArrayList<Ship>();

	public AllyPlayer(String name) {
		super(name, Faction.ALLY);
	}
	
	public void placeShipsRandomly() {
		try {
			for (int size : SHIP_SIZES) {
				Ship randomShip = getRandomShip(size);
				shipList.add(randomShip);
				for (ShipLocation location : randomShip.getLocations()) {
					AllyCell cell = getCell(location);
					cell.setShip(randomShip);
				}
			}
		}
		catch (RandomShipFailure e) {
			System.out.println("ERROR: Could not place random ships.");
		}
	}

	private Ship getRandomShip(int shipSize) throws RandomShipFailure {
		ArrayList<ShipLocation> locations = new ArrayList<ShipLocation>();
		ShipLocation location = null;
		int attemptsCount = 0;
		boolean isSuccessful = false;
		Axis axis = Axis.X;

		if (new Random().nextBoolean()) {
			axis = Axis.Y;
		}

		while (!isSuccessful && (attemptsCount++ < 3*HEIGHT*WIDTH)) {
			int x = (int) (Math.random() * (WIDTH -1) + 1);
			int y = (int) (Math.random() * (HEIGHT - 1) + 1);
			location = new ShipLocation(x, y);

			int position = 0;
			isSuccessful = true;
			while (isSuccessful && (position++ < shipSize)) {
				isSuccessful = isLocationUsed(location);
				if ((x > (WIDTH - 1)) || (x < 0)) {
					isSuccessful = false;
				}
				if ((y > (HEIGHT - 1)) || (y < 0)) {
					isSuccessful = false;
				}
				if (isSuccessful) {
					locations.add(location);
					switch (axis) {
						case X:
							location = new ShipLocation(++x, y);
							break;
						case Y:
							location = new ShipLocation(x, ++y);
							break;
						default:
							break;
					}
				}
				else {
					locations.clear();
				}
			}
		}

		if (isSuccessful == false) {
			throw new RandomShipFailure("Could not generate random ship.");
		}

		//// System.out.println("Size of ship: " + locations.size());
		return new Ship(locations);
	}

	private boolean isLocationUsed(ShipLocation location) {
		for (Ship existingShip : shipList) {
			for (ShipLocation existingLocation : existingShip.getLocations()) {
				if (location.equals(existingLocation)) {
					return false;
				}
			}
		}
		return true;
	}
}
