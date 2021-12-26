package justacommonguy.battleshipgui.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import justacommonguy.battleshipgui.Faction;
import justacommonguy.battleshipgui.Result;
import justacommonguy.battleshipgui.ShipLocation;

public abstract class Cell extends JPanel implements MouseListener, HighlightListener {

	public static final Color DEFAULT = new Color (35, 40, 48);
	protected Color oldColor = DEFAULT;
	protected final ShipLocation location;

	private HighlightInitiator xInitiator;
	private HighlightInitiator yInitiator;

	public Cell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit) {
		addMouseListener(this);
		this.location = location;
		this.xInitiator = xInit;
		this.yInitiator = yInit;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Cell> T getInstance(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit, Faction faction) {
		switch (faction) {
			case ALLY:
				return (T) new AllyCell(location, xInit, yInit);
			case ENEMY:
				return (T) new EnemyCell(location, xInit, yInit);
			default:
				return null;
		}
	}

	public void setCellColor(Color color) {
		if (color == null) {
			throw new IllegalArgumentException("Color must not be null.");
		}
		oldColor = color;
		setBackground(color);
	}

	public void setCellColor(Result result) {
		switch (result) {
			case HIT:
				setCellColor(getHitColor());
				break;
			case KILL:
				setCellColor(getKillColor());
				break;
			case MISS:
				setCellColor(getMissColor());
				break;
			default:
				break;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {
		xInitiator.fire();
		yInitiator.fire();
		highlight(30);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		xInitiator.unfire();
		yInitiator.unfire();
	}

	/** Fixes the increment if it would make the new highlighting values illegal. */
	public int fixIncrement(int increment, Color color) {
		int[] rgb = {color.getRed(), color.getGreen(), color.getBlue()};

		for (int value : rgb) {
			if (value + increment > 255) {
				increment = 255 - value;
			}
			if (value + increment < 0) {
				increment = value;
			}
		}
		return increment;
	}

	public void highlight(int increment) {
		increment = fixIncrement(increment, oldColor);
		setBackground(new Color(oldColor.getRed() + increment, oldColor.getGreen() + increment, 
				oldColor.getBlue() + increment));
	}

	public void unhighlight() {
		setBackground(oldColor);
	}

	@Override
	public void cellHighlighted() {
		highlight(15);
	}

	@Override
	public void cellUnhighlighted() {
		unhighlight();
	}

	public ShipLocation getShipLocation() {
		return location;
	}

	abstract public Color getMissColor();
	abstract public Color getShipColor();
	abstract public Color getHitColor();
	abstract public Color getKillColor();
}
