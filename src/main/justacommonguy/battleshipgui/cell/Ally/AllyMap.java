package justacommonguy.battleshipgui.cell.Ally;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import justacommonguy.battleshipgui.cell.HighlightInitiator;
import justacommonguy.battleshipgui.cell.Map;
import justacommonguy.battleshipgui.ship.Ship;
import justacommonguy.battleshipgui.ship.ShipBuilder;
import justacommonguy.battleshipgui.ship.ShipLocation;

public class AllyMap extends Map<AllyCell> implements ActionListener {

	private ArrayList<Ship> shipList;
	private CountDownLatch latch = new CountDownLatch(1);

	@Override
	protected AllyCell constructCell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit) {
		return new AllyCell(location, xInit, yInit);
	}
	
	public void buildShips() {
		shipList = new ShipBuilder(Map.HEIGHT, Map.WIDTH).buildShipsRandomly();
		placeShips(shipList);
	}
	
	private void placeShips(ArrayList<Ship> shipList) {
		for (Ship ship : shipList) {
			for (ShipLocation location : ship.getLocations()) {
				AllyCell cell = getCell(location);
				cell.setShip(ship);
			}
		}
	}

	/** Get cells that share the same ship. */
	ArrayList<AllyCell> getRelatedCells(AllyCell cell) {
		if (!cell.hasShip()) {
			return null;
		}
		return getCellList(cell.getShip().getLocations());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		latch.countDown();
	}

	public ArrayList<Ship> sendShips() {
		if (shipList == null) {
			throw new RuntimeException("Ships have already been sent.");
		}

		try {
			latch.await();
		}
		catch (InterruptedException e) {}

		ArrayList<Ship> shipListClone = new ArrayList<>();
		for (Ship ship : shipList) {
			shipListClone.add(ship.clone());
		}
		// Get rid of the list because only the server needs it.
		shipList = null;
		allowInteraction(false);
		return shipListClone;
	}

	@Override
	public void allowInteraction(boolean allow) {
		// ShipMover needs access to the player's fleet.
		ShipMover.setMap(allow ? this : null);
		AllyCell.setPlacementAllowed(allow);
	}

	@Override
	public String toString() {
		return "AllyMap";
	}
}
