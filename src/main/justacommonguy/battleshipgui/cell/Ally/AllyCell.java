package justacommonguy.battleshipgui.cell.Ally;

import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import justacommonguy.battleshipgui.cell.Cell;
import justacommonguy.battleshipgui.cell.HighlightInitiator;
import justacommonguy.battleshipgui.ship.Ship;
import justacommonguy.battleshipgui.ship.ShipLocation;

class AllyCell extends Cell implements MouseWheelListener {

	private static boolean placementAllowed;
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
		highlightHover();
		if (placementAllowed && mover != null) {
			mover.setNewLocation(location);
			mover.drag();
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
			mover.rotate(direction);
			mover = null;
		}
	}

	/** Temporary highlighting when the ship is being dragged. */
	void highlightDragging(Ship shipDragged) {
		Color color = SHIP_COLOR;
		int increment = fixIncrement(-50, color);
		color = new Color(color.getRed() + increment, color.getGreen() + increment, 
				color.getBlue() + increment);

		if (hasShip() && !hasShip(shipDragged)) {
			setBackground(KILL_COLOR);
		}
		else {
			setBackground(color);
		}
	}

	public boolean hasShip() {
		return ship != null;
	}

	public boolean hasShip(Ship ship) {
		return this.ship == ship;
	}
	
	void setShip(Ship ship) {
		this.ship = ship;
		if (ship == null) {
			setCellColor(getDefaultColor());
			removeMouseWheelListener(this);
		}
		else {
			addMouseWheelListener(this);
			setCellColor(SHIP_COLOR);
		}
	}

	Ship getShip() {
		return ship;
	}

	static void setPlacementAllowed(boolean placementAllowed) {
		AllyCell.placementAllowed = placementAllowed;
	}

	@Override
	public Color getMissColor() {
		return new Color(MISS_COLOR.getRGB());
	}

	@Override
	public Color getShipColor() {
		return new Color(SHIP_COLOR.getRGB());
	}

	@Override
	public Color getHitColor() {
		return new Color(HIT_COLOR.getRGB());
	}

	@Override
	public Color getKillColor() {
		return new Color(KILL_COLOR.getRGB());
	}
}
