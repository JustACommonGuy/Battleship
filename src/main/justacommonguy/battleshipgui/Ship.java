package justacommonguy.battleshipgui;

import java.util.ArrayList;

import justacommonguy.battleshipgui.gui.AllyCell;

public class Ship {

	public enum Axis {
		X,
		Y
	}
	
	private ArrayList<ShipLocation> locations = new ArrayList<ShipLocation>();
	private String owner;
	private int size;

	public Ship(ArrayList<ShipLocation> locations) {
		//TODO
		this.locations = locations;
	}

	public ArrayList<ShipLocation> getNewLocations(ShipLocation oldLoc, ShipLocation newLoc) {
		if (!locations.contains(oldLoc)) {
			throw new IllegalArgumentException("OldLoc does not match with Ship's old location.");
		}
		
		int xIncrement = newLoc.getX() - oldLoc.getX();
		int yIncrement = newLoc.getY() - oldLoc.getY();
		ArrayList<ShipLocation> newLocations = new ArrayList<ShipLocation>();

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
				System.out.println("location: " + updatedLocation);
				newLocations.add(updatedLocation);
			}
			else {
				//!
				System.out.println("Failed update. " + targetCell.hasShip());
				return null;
			}
		}
		
		System.out.println("");
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
