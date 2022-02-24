package justacommonguy.battleshipgui.utils;

import java.io.Serializable;

import justacommonguy.battleshipgui.cell.Map;
import justacommonguy.battleshipgui.ship.ShipLocation;

public class Attack implements Serializable {
	
	private final ShipLocation guess;
	private final Result result;
	private final boolean isHostAttacked;

	public Attack(ShipLocation guess, Result result, boolean isHostAttacked) {
		this.guess = guess;
		this.result = result;
		this.isHostAttacked = isHostAttacked;
	}

	public void updateMap(Map map) {
		map.attackCell(guess, result);
	}

	public Result getResult() {
		return result;
	}

	public boolean isHostAttacked() {
		return isHostAttacked;
	}

	@Override
	public String toString() {
		String attacked = "Client";
		if (isHostAttacked) {
			attacked = "Host";
		}
		return "Attack [" + guess + ", on " + attacked + ". " + result + "]";
	}
}
