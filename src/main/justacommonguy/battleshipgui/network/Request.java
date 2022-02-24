package justacommonguy.battleshipgui.network;

public enum Request {
	/** Expects {@link justacommonguy.battleshipgui.player.Player Player} */
	SEND_PLAYER,
	/** Sends enemy's {@link justacommonguy.battleshipgui.player.Player Player} */
	START,
	/** Expects an ArrayList of {@link justacommonguy.battleshipgui.ship.ShipLocation ShipLocation} */
	PLACE_SHIPS,
	/** Expects {@link justacommonguy.battleshipgui.ship.ShipLocation ShipLocation} */
	ATTACK,
	/** Sends {@link justacommonguy.battleshipgui.utils.Attack Attack} to signal an attack to a map. */
	ATTACKED,	//Send Attack
	/** Sends the winner in a String. */
	FINISH
}
