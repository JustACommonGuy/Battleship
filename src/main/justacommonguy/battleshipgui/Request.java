package justacommonguy.battleshipgui;

public enum Request {
	/** Expects {@link justacommonguy.battleshipgui.player.Player Player} */
	SEND_PLAYER,
	/** Sends enemy's {@link justacommonguy.battleshipgui.player.Player Player} */
	START,
	/** Expects an ArrayList of {@link justacommonguy.battleshipgui.ship.ShipLocation ShipLocation} */
	PLACE_SHIPS,
	/** Expects {@link justacommonguy.battleshipgui.ship.ShipLocation ShipLocation} */
	ATTACK,
	/** Sends {@link Attack Attack} to signal an attack to the player's map. */
	ATTACK_ALLY,	//Send Attack
	/** Sends {@link Attack Attack} to signal an attack to the enemy's map. */
	ATTACK_ENEMY,
	/** Sends the winner in a String. */
	FINISH
}
