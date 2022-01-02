package justacommonguy.battleshipgui.cell.Enemy;

import justacommonguy.battleshipgui.ship.ShipLocation;

interface AttackListener {
	
	void attacked(ShipLocation attackGuess);
}
