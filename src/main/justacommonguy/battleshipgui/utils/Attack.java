package justacommonguy.battleshipgui.utils;

import java.io.Serializable;

import justacommonguy.battleshipgui.cell.Map;
import justacommonguy.battleshipgui.ship.ShipLocation;

public class Attack implements Serializable {
	
	private final ShipLocation guess;
	private final Result result;

	public Attack(ShipLocation guess, Result result) {
		this.guess = guess;
		this.result = result;
	}

	public void updateMap(Map map) {
		map.attackCell(guess, result);
	}

	public Result getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "Attack [" + guess + ", " + result + "]";
	}
}
