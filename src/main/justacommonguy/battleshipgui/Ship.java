package justacommonguy.battleshipgui;

import java.util.ArrayList;

public class Ship {
	private ArrayList<ShipLocation> locations = new ArrayList<ShipLocation>();

	public Ship() {
		//TODO 
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
}
