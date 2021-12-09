package justacommonguy.battleshipgui.gui;

import justacommonguy.battleshipgui.networking.Request;

public enum GUIRequest implements Request {
	STARTED,	//Sends ClientPlayer.
	PLACED_SHIPS,	//Sends ShipLocations.
	ATTACKED,	//Sends Location.
}
