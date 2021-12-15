package justacommonguy.battleshipgui;

import java.io.Serializable;

public abstract class Player implements Serializable {
	private String name;

	public Player(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
