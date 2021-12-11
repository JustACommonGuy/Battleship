package justacommonguy.battleshipgui.networking;

public enum Request {
	SEND_PLAYER_INFO,
	START,
	PLACE_SHIPS,
	ATTACK,
	ATTACK_RESULT,	//Sends Location. The rest is calculated by the client.
	FINISH	//Sends result.
}
