package justacommonguy.battleshipgui;

public abstract class Player {
	private String name;

	public Player(String name) {
		this.name = name;
	}

	public void setUpShips() {

	}

	public ShipLocation attack() {
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
