package justacommonguy;

public class ShipLocation {
	private int x;
	private int y;
	private int[] coords = new int[2];
	private int cellNum;

	public ShipLocation(int x, int y) {
		this.x = x;
		this.y = y;
		coords[0] = x;
		coords[1] =y;
		cellNum = this.y * 10 + this.x;
	}

	@Override
	public boolean equals(Object obj) {
		ShipLocation location = (ShipLocation) obj;
		return this.cellNum == location.cellNum;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int[] getCoords() {
		return coords;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
}
