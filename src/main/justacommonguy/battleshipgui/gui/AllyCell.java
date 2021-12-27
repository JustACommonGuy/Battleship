package justacommonguy.battleshipgui.gui;

import static justacommonguy.battleshipgui.Settings.gameSettings;
import static justacommonguy.battleshipgui.gui.BattleshipGUI.gameGUI;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import justacommonguy.battleshipgui.Ship;
import justacommonguy.battleshipgui.ShipLocation;

public class AllyCell extends Cell implements MouseWheelListener {

	private static boolean placementAllowed = true;
	private static ShipMover mover;

	private static final Color MISS_COLOR = gameSettings.getColor("ally_miss_color");
	private static final Color SHIP_COLOR = gameSettings.getColor("ally_ship_color");
	private static final Color HIT_COLOR = gameSettings.getColor("ally_hit_color");
	private static final Color KILL_COLOR = gameSettings.getColor("ally_kill_color");

	private Ship ship;

	public AllyCell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit) {
		super(location, xInit, yInit);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (placementAllowed && (ship != null)) {
			mover = new ShipMover(this);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);
		if (placementAllowed && mover != null) {
			mover.setNewLocation(location);
			mover.drag();
		}
	}

	public void mouseEntered() {
		super.mouseEntered(null);
	}

	/** Temporary highlighting when the ship is being dragged. */
	public void highlightDragging() {
		Color color = SHIP_COLOR;
		int increment = fixIncrement(-50, color);
		color = new Color(color.getRed() + increment, color.getGreen() + increment, 
				color.getBlue() + increment);

		boolean isOwnShip = false;
		if ((oldColor != DEFAULT) && (isOwnShip == false)) {
			setBackground(KILL_COLOR);
		}
		else {
			setBackground(color);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (placementAllowed && mover != null) {
			mover.drop();
			mover = null;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (placementAllowed && (ship != null)) {
			mover = new ShipMover(this);
			int direction = e.getWheelRotation() / Math.abs(e.getWheelRotation());
			mover.rotate(direction, location);
			mover = null;
		}
	}
	
	public void setShip(Ship ship) {
		this.ship = ship;
		if (ship == null) {
			setCellColor(DEFAULT);
			removeMouseWheelListener(this);
		}
		else {
			addMouseWheelListener(this);
			setCellColor(SHIP_COLOR);
		}
	}

	public Ship getShip() {
		return ship;
	}

	/** Get cells that share the same ship. */
	public ArrayList<AllyCell> getRelatedCells() {
		if (ship == null) {
			return null;
		}
		return gameGUI.player.getCellList(ship.getLocations());
	}

	/** We don't consider that it has a ship if it is the same that is being modified. */
	public boolean hasShip() {
		return ship != null;
	}

	public boolean hasShip(Ship ship) {
		return this.ship == ship;
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
