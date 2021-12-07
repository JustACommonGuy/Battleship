package justacommonguy.battleshipgui.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import justacommonguy.battleshipgui.ShipLocation;

public abstract class Cell extends JPanel implements MouseListener, HighlightListener {

	private ShipLocation cell;

	private final static Color DEFAULT = new Color (35, 40, 48);
	private Color oldColor = DEFAULT;
	private HighlightInitiator xInitiator;
	private HighlightInitiator yInitiator;

	public Cell(ShipLocation cell, HighlightInitiator xInitiator, HighlightInitiator yInitiator) {
		super.addMouseListener(this);
		this.xInitiator = xInitiator;
		this.yInitiator = yInitiator;
		this.cell = cell;
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

	public ShipLocation getCell() {
		return cell;
	}

	public void setOldColor(Color oldColor) {
		this.oldColor = oldColor;
	}
}
