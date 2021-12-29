package justacommonguy.battleshipgui;

public enum Request {
	SEND_PLAYER,
	START,
	PLACE_SHIPS,
	ATTACK,
	ATTACK_RESULT,	//Sends Location. The rest is calculated by the client.
	FINISH	//Sends result.
}
