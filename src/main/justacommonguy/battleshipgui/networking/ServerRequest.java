package justacommonguy.battleshipgui.networking;

public enum ServerRequest implements Request {
	START,
	PLACE_SHIPS,
	ATTACK,
	ATTACK_RESULT,	//Sends Location. The rest is calculated by the client.
	FINISH	//Sends result.
}
