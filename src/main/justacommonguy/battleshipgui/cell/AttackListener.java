package justacommonguy.battleshipgui.cell;

import justacommonguy.battleshipgui.ship.ShipLocation;

public interface AttackListener {
	
	public void attacked(ShipLocation attackGuess);
}
