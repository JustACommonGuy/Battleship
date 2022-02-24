package justacommonguy.battleshipgui.cell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import justacommonguy.battleshipgui.ship.ShipLocation;

public abstract class Cell extends JPanel implements MouseListener, HighlightListener {

	private static final Color DEFAULT = new Color (35, 40, 48);
	
	private final HighlightInitiator xInitiator;
	private final HighlightInitiator yInitiator;
	private boolean isHighlighted;
	protected Color oldColor = DEFAULT;
	protected final ShipLocation location;

	public Cell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit) {
		addMouseListener(this);
		this.location = location;
		this.xInitiator = xInit;
		this.yInitiator = yInit;
	}

	public void setCellColor(Color color) {
		if (color == null) {
			throw new IllegalArgumentException("Color must not be null.");
		}
		oldColor = color;
		setBackground(color);
	}

	public void setMiss() {
		oldColor = null;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		if (oldColor == null) {
			Graphics2D g = (Graphics2D) graphics;
			int x = (getWidth()/2) - 2;
			int y = (getHeight()/2) - 2;
			g.fillOval(x, y, 3, 3);
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
		highlightHover();
	}

	public void highlightHover() {
		xInitiator.fire();
		yInitiator.fire();
		highlight(30);
	}

	@Override
	public void cellHighlighted() {
		highlight(15);
	}

	/** Fixes the increment if it would make the new highlighting values illegal. */
	public static int fixIncrement(int increment, Color color) {
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

	private void highlight(int increment) {
		isHighlighted = true;
		increment = fixIncrement(increment, oldColor);
		setBackground(new Color(oldColor.getRed() + increment, oldColor.getGreen() + increment, 
				oldColor.getBlue() + increment));
	}

	@Override
	public void cellUnhighlighted() {
		unhighlight();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		xInitiator.unfire();
		yInitiator.unfire();
	}

	private void unhighlight() {
		isHighlighted = false;
		setBackground(oldColor);
	}

	public ShipLocation getShipLocation() {
		return location;
	}

	public boolean isHighlighted() {
		return isHighlighted;
	}

	public Color getDefaultColor() {
		return new Color(DEFAULT.getRGB());
	}

	abstract public Color getMissColor();
	abstract public Color getShipColor();
	abstract public Color getHitColor();
	abstract public Color getKillColor();
}
