package justacommonguy.battleshipgui;

import java.util.ArrayList;

public class Ship {
	private ArrayList<ShipLocation> cells = new ArrayList<ShipLocation>();

	public Ship() {
		//TODO 
	}

	public Result checkHit(ShipLocation attackedLocation) {
		Result result = Result.MISS;

		for (ShipLocation cell : cells) {
			if (cell.equals(attackedLocation)) {
				result = Result.HIT;
				cells.remove(cell);
				break;
			}
		}

		if (cells.isEmpty()) {
			result = Result.KILL;
		}

		return result;
	}
}
