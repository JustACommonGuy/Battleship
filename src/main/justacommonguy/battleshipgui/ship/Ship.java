package justacommonguy.battleshipgui.ship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import justacommonguy.battleshipgui.utils.Result;

public class Ship implements Serializable, Cloneable {

	private enum Axis {
		X,
		Y,
	}

	/** Array for all ship sizes. The order must match with 
	 * {@link Ship#SHIP_NAMES SHIP_NAMES}.*/
	static final int[] SHIP_SIZES = {2, 3, 3, 4, 5};
	/** String array for all ship names. The order must match with 
	 * {@link Ship#SHIP_SIZES SHIP_SIZES}. 
	 * Submarine is not used since that would overcomplicate toString().*/
	private static final String[] SHIP_NAMES = 
			{"Destroyer", "Cruiser", "Submarine", "Battleship", "Carrier"};
	
	private ArrayList<ShipLocation> locations = new ArrayList<>();
	private final int size;


	private Ship(ArrayList<ShipLocation> locations) {
		if ((locations.size() < SHIP_SIZES[0]) || 
				(locations.size() > SHIP_SIZES[SHIP_SIZES.length - 1])) {
			throw new IllegalArgumentException("Ship's size is out of bounds.");
		}
		this.locations = locations;
		size = locations.size();
	}

	public static Ship getRandomShip(int shipSize, int height, int width) throws RandomShipFailure {
		ArrayList<ShipLocation> locations = new ArrayList<>();
		ShipLocation location = null;
		int attemptsCount = 0;
		boolean isSuccessful = false;
		Axis axis = Axis.X;
	
		if (new Random().nextBoolean()) {
			axis = Axis.Y;
		}
	
		while (!isSuccessful && (attemptsCount++ < 3*height*width)) {
			int x = (int) (Math.random() * (width -1) + 1);
			int y = (int) (Math.random() * (height - 1) + 1);
			location = new ShipLocation(x, y);
	
			int position = 0;
			isSuccessful = true;
			while (isSuccessful && (position++ < shipSize)) {
				isSuccessful = true;
				if ((x > (width - 1)) || (x < 0)) {
					isSuccessful = false;
				}
				else if ((y > (height - 1)) || (y < 0)) {
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

	@Override
	public String toString() {
		for (int i = 0; i < SHIP_SIZES.length; i++) {
			if (size == SHIP_SIZES[i]) {
				return SHIP_NAMES[i];
			}
		}
		return null;
	}

	public Ship clone() {
		ArrayList<ShipLocation> locationsClone = new ArrayList<>();
		for (ShipLocation location : locations) {
			locationsClone.add(location);
		}
		return new Ship(locationsClone);
	}

	public ArrayList<ShipLocation> getDraggedLocations(ShipLocation oldLoc, ShipLocation newLoc) {
		if (!locations.contains(oldLoc)) {
			throw new IllegalArgumentException("OldLoc does not match with Ship's old location.");
		}
		
		int xChange = newLoc.getX() - oldLoc.getX();
		int yChange = newLoc.getY() - oldLoc.getY();
		ArrayList<ShipLocation> newLocations = new ArrayList<>();

		//// System.out.println("");
		for (ShipLocation loc : locations) {
			ShipLocation updatedLocation = null;
			try {
				updatedLocation = new ShipLocation(loc.getX() + xChange, loc.getY() + yChange);
				newLocations.add(updatedLocation);
			}
			catch (IllegalArgumentException e) {
				//// System.out.println("Could not generate new locations. Negative coords.");
				return null;
			}
		}
		
		return newLocations;
	}

	public ArrayList<ShipLocation> getRotatedLocations(int direction, ShipLocation center) {
		if (!locations.contains(center)) {
			throw new IllegalArgumentException("OldLoc does not match with Ship's old location.");
		}
		
		//// System.out.println("");
		try {
			ArrayList<ShipLocation> newLocations = new ArrayList<>();
			for (ShipLocation loc : locations) {
				int xChange = loc.getX() - center.getX();
				int yChange = loc.getY() - center.getY();

				ShipLocation newLocation = new ShipLocation(center.getX() - (direction * yChange), 
						center.getY() + (direction * xChange));
				newLocations.add(newLocation);
				//// System.out.println("New Location: " + newLocation);
			}
			return newLocations;
		}
		catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}

	public Result checkHit(ShipLocation attackedLocation) {
		Result result = Result.MISS;

		if (locations.contains(attackedLocation)) {
			result = Result.HIT;
			locations.remove(attackedLocation);
		}
		System.out.println(toString() + " length " + locations.size());
		if (locations.isEmpty()) {
			result = Result.KILL;
		}

		return result;
	}

	public void setLocations(ArrayList<ShipLocation> locations) {
		if (locations == null) {
			throw new IllegalArgumentException("List must not be null");
		}
		this.locations = locations;
	}

	public ArrayList<ShipLocation> getLocations() {
		return new ArrayList<ShipLocation>(locations);
	}

	public boolean hasLocation(ShipLocation location) {
		if (locations.contains(location)) {
			return true;
		}
		return false;
	}
}
