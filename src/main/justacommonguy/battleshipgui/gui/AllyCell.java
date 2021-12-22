package justacommonguy.battleshipgui.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import justacommonguy.battleshipgui.Faction;
import justacommonguy.battleshipgui.GameServer;
import justacommonguy.battleshipgui.Ship;
import justacommonguy.battleshipgui.ShipLocation;

public class AllyCell extends Cell implements DragListener {

	//!
	private static DragInitiator dragInitiator = new DragInitiator();
	private static boolean placementAllowed = true;
	private static AllyCell oldCell;
	private static ArrayList<AllyCell> oldCellList;
	private static Ship oldShip;

	private static Color MISS_COLOR = GameServer.settings.getColor("ally_miss_color");
	private static Color SHIP_COLOR = GameServer.settings.getColor("ally_ship_color");
	private static Color HIT_COLOR = GameServer.settings.getColor("ally_hit_color");
	private static Color KILL_COLOR = GameServer.settings.getColor("ally_kill_color");

	private Ship ship;

	public AllyCell(ShipLocation location, HighlightInitiator xInitiator, HighlightInitiator yInitiator) {
		super(location, xInitiator, yInitiator);
	}

	public void setShip(Ship ship) {
		this.ship = ship;
		if (ship == null) {
			setCellColor(DEFAULT);
		}
		else {
			setCellColor(SHIP_COLOR);
		}
	}

	public void removeOldShip() {
		for (AllyCell cell : oldCellList) {
			cell.setShip(null);
		}
		removeOldCell();
	}

	public void setOldCell() {
		oldCell = this;
		oldCellList = oldCell.getRelatedCells();
		oldShip = oldCell.getShip();
	}

	public void removeOldCell() {
		oldCell = null;
		oldCellList = null;
		oldShip = null;
	}

	@Override
	public void shipDragged() {
		if ((oldCell != null) && (oldCell != this)) {
			ArrayList<ShipLocation> newLocations = oldShip.getNewLocations(
					oldCell.getShipLocation(), super.location);
			
			if (newLocations != null) {
				Ship oldShip = AllyCell.oldShip;
				removeOldShip();
				for (ShipLocation newLocation : newLocations) {
					AllyCell newCell = (AllyCell) GameServer.gui.getCell(newLocation, Faction.ALLY);
					newCell.setShip(oldShip);
				}
				oldShip.setLocations(newLocations);
				super.mouseEntered(null);
			}
			else {
				removeOldCell();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (placementAllowed && (ship != null)) {
			setOldCell();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (placementAllowed) {
			dragInitiator.dragged();
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (placementAllowed) {
			dragInitiator.setDragListener(this);
		}
		super.mouseEntered(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (placementAllowed) {
			//TODO Change ship orientation
		}
	}

	// ? Might not be needed
	public ArrayList<AllyCell> getRelatedCells() {
		ArrayList<AllyCell> cells = new ArrayList<AllyCell>();
		ArrayList<ShipLocation> locations = ship.getLocations();
		for (ShipLocation loc : locations) {
			AllyCell cell = (AllyCell) GameServer.gui.getCell(loc, Faction.ALLY);
			cells.add(cell);
		}
		return cells;
	}

	public boolean hasShip() {
		if (oldCell != null) {
			for (AllyCell cell : oldCellList) {
				if (cell.equals(this)) {
					return false;
				}
			}
		}

		return ship != null;
	}

	public Ship getShip() {
		return ship;
	}

	@Override
	public Color getMissColor() {
		return MISS_COLOR;
	}

	@Override
	public Color getShipColor() {
		return SHIP_COLOR;
	}

	@Override
	public Color getHitColor() {
		return HIT_COLOR;
	}

	@Override
	public Color getKillColor() {
		return KILL_COLOR;
	}
}
