package justacommonguy.battleshipgui;

import java.io.Serializable;

public abstract class Player implements Serializable {
	private String name;

	public Player(String name) {
		this.name = name;
	}

	public void setUpShips() {
		//TODO
	}

	public ShipLocation attack() {
		//TODO
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
