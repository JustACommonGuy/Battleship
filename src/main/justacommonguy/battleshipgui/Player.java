package justacommonguy.battleshipgui;

import java.io.Serializable;

public abstract class Player implements Serializable {
	
	protected String name;

	public Player(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
