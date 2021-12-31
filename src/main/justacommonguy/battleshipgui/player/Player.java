package justacommonguy.battleshipgui.player;
import java.io.Serializable;

public class Player implements Serializable, Cloneable {

	private String name;

	public Player(String name) {
		this.name = name.toUpperCase();
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException("Could not clone player.");
		}
	}

	public void setName(String name) {
		if ((name == null) || (name.equals(""))) {
			throw new IllegalArgumentException("Name must not be null or empty.");
		}
		this.name = name;
	}
}
