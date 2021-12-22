package justacommonguy.battleshipgui.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import justacommonguy.battleshipgui.Result;
import justacommonguy.battleshipgui.ShipLocation;

public abstract class Cell extends JPanel implements MouseListener, HighlightListener {

	public final static Color DEFAULT = new Color (35, 40, 48);
	protected Color oldColor = DEFAULT;
	protected ShipLocation location;

	private HighlightInitiator xInitiator;
	private HighlightInitiator yInitiator;

	public Cell(ShipLocation location, HighlightInitiator xInitiator, HighlightInitiator yInitiator) {
		super.addMouseListener(this);
		this.xInitiator = xInitiator;
		this.yInitiator = yInitiator;
		this.location = location;
	}

	public void setCellColor(Color color) {
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

	public void highlight(int increment) {
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
