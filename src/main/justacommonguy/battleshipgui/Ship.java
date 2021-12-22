package justacommonguy.battleshipgui;

import java.util.ArrayList;

import justacommonguy.battleshipgui.gui.AllyCell;
import justacommonguy.battleshipgui.gui.BattleshipGUI;

public class Ship {

	public enum Axis {
		X,
		Y
	}

	/** String array for all ship names. The order must match with 
	 * {@link justacommonguy.battleshipgui.gui.BattleshipGUI#SHIP_SIZES SHIP_SIZES}. 
	 * Submarine is not used since that would overcomplicate toString().*/
	private static final String[] SHIP_NAMES = 
			{"Destroyer", "Cruiser", "Submarine", "Battleship", "Carrier"};
	
	private ArrayList<ShipLocation> locations = new ArrayList<ShipLocation>();
	private int size;

	public Ship(ArrayList<ShipLocation> locations) {
		//TODO
		if ((locations.size() < BattleshipGUI.SHIP_SIZES[0]) || 
				(locations.size() > BattleshipGUI.SHIP_SIZES[BattleshipGUI.SHIP_SIZES.length-1])) {
			throw new IllegalArgumentException("Ship's size is out of bounds.");
		}
		this.locations = locations;
		size = locations.size();
	}

	@Override
	public String toString() {
		for (int i = 0; i < BattleshipGUI.SHIP_SIZES.length; i++) {
			if (size == BattleshipGUI.SHIP_SIZES[i]) {
				return SHIP_NAMES[i];
			}
		}
		return null;
	}

	public ArrayList<ShipLocation> getNewLocations(ShipLocation oldLoc, ShipLocation newLoc) {
		if (!locations.contains(oldLoc)) {
			throw new IllegalArgumentException("OldLoc does not match with Ship's old location.");
		}
		
		int xIncrement = newLoc.getX() - oldLoc.getX();
		int yIncrement = newLoc.getY() - oldLoc.getY();
		ArrayList<ShipLocation> newLocations = new ArrayList<ShipLocation>();

		System.out.println("");
		for (ShipLocation location : locations) {
			ShipLocation updatedLocation = null;
			try {
				updatedLocation = new ShipLocation(location.getX() + xIncrement, 
						location.getY() + yIncrement);
			}
			catch (IllegalArgumentException e) {
				return null;
			}

			AllyCell targetCell = (AllyCell) GameServer.gui.getCell(updatedLocation, Faction.ALLY);
			if ((targetCell != null) && targetCell.hasShip() == false) {
				System.out.println("New location of " + this + ": " + updatedLocation);
				newLocations.add(updatedLocation);
			}
			else {
				return null;
			}
		}
		
		return newLocations;
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
		this.locations = locations;
	}

	public ArrayList<ShipLocation> getLocations() {
		return locations;
	}
}
