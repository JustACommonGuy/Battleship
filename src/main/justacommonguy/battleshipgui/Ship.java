package justacommonguy.battleshipgui;

import java.util.ArrayList;

public class Ship {

	public enum Axis {
		X,
		Y
	}

	/** String array for all ship names. The order must match with 
	 * {@link justacommonguy.battleshipgui.AllyPlayer#SHIP_SIZES SHIP_SIZES}. 
	 * Submarine is not used since that would overcomplicate toString().*/
	private static final String[] SHIP_NAMES = 
			{"Destroyer", "Cruiser", "Submarine", "Battleship", "Carrier"};
	
	private ArrayList<ShipLocation> locations = new ArrayList<ShipLocation>();
	private int size;

	public Ship(ArrayList<ShipLocation> locations) {
		if ((locations.size() < AllyPlayer.SHIP_SIZES[0]) || 
				(locations.size() > AllyPlayer.SHIP_SIZES[AllyPlayer.SHIP_SIZES.length-1])) {
			throw new IllegalArgumentException("Ship's size is out of bounds.");
		}
		this.locations = locations;
		size = locations.size();
	}

	@Override
	public String toString() {
		for (int i = 0; i < AllyPlayer.SHIP_SIZES.length; i++) {
			if (size == AllyPlayer.SHIP_SIZES[i]) {
				return SHIP_NAMES[i];
			}
		}
		return null;
	}

	public ArrayList<ShipLocation> getDraggedLocations(ShipLocation oldLoc, ShipLocation newLoc) {
		if (!locations.contains(oldLoc)) {
			throw new IllegalArgumentException("OldLoc does not match with Ship's old location.");
		}
		
		int xChange = newLoc.getX() - oldLoc.getX();
		int yChange = newLoc.getY() - oldLoc.getY();
		ArrayList<ShipLocation> newLocations = new ArrayList<ShipLocation>();

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
			ArrayList<ShipLocation> newLocations = new ArrayList<ShipLocation>();
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
		catch (IllegalArgumentException | NullPointerException ex) {
			return null;
		}
	}

	public Result checkHit(ShipLocation attackedLocation) {
		Result result = Result.MISS;

		for (ShipLocation location : locations) {
			if (location.equals(attackedLocation)) {
				result = Result.HIT;
				locations.remove(location);
				break;
			}
		}

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
}
