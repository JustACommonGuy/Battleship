package justacommonguy.battleshipgui.player;

import java.util.ArrayList;

import justacommonguy.battleshipgui.cell.AllyCell;
import justacommonguy.battleshipgui.cell.ShipMover;
import justacommonguy.battleshipgui.ship.Ship;
import justacommonguy.battleshipgui.ship.ShipBuilder;
import justacommonguy.battleshipgui.utils.Faction;

public class AllyPlayer extends Player<AllyCell> {

	private ArrayList<Ship> shipList = new ArrayList<Ship>();

	public AllyPlayer(String name) {
		super(name, Faction.ALLY);
	}

	public void buildShips() {
		shipList = new ShipBuilder(HEIGHT, WIDTH).placeShipsRandomly(this);
		// ShipMover needs access to the player's fleet.
		ShipMover.setOwner(this);
	}

	/** Get cells that share the same ship. */
	public ArrayList<AllyCell> getRelatedCells(AllyCell cell) {
		if (!cell.hasShip()) {
			return null;
		}
		return getCellList(cell.getShip().getLocations());
	}

	public ArrayList<Ship> getShipList() {
		return shipList;
	}
}
