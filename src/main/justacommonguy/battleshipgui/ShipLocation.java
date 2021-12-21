package justacommonguy.battleshipgui;

import java.io.Serializable;

public class ShipLocation implements Serializable {
	
	private int x;
	private int y;
	private int[] coords = new int[2];
	private int cellNum;

	public ShipLocation(int x, int y) {
		if ((x < 0) || (y < 0)) {
			throw new IllegalArgumentException("Arguments cannot be negative integers.");
		}
		this.x = x;
		this.y = y;
		coords[0] = x;
		coords[1] = y;
		cellNum = this.y * 10 + this.x;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		ShipLocation location = (ShipLocation) obj;
		return this.cellNum == location.cellNum;
	}

	@Override
	public int hashCode() {
		final int prime = 42;
		int result = 3;
		result = prime * result + cellNum;
		return result;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
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

	public int getCellNum() {
		return cellNum;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
}
