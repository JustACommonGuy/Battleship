package justacommonguy;

import java.util.ArrayList;

public class Ship {
	private ArrayList<ShipLocation> cells = new ArrayList<ShipLocation>();

	public Ship() {
		//TODO 
	}

	public Result checkHit(ShipLocation attackedCell) {
		Result result = Result.MISS;

		for (ShipLocation cell : cells) {
			if (cell.equals(attackedCell)) {
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
