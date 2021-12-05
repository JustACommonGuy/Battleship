package justacommonguy.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import justacommonguy.ShipLocation;

public class EnemyCell extends Cell{

	private static boolean attackAllowed;

	public EnemyCell(ShipLocation location, HighlightInitiator xInitiator, HighlightInitiator yInitiator) {
		super(location, xInitiator, yInitiator);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!attackAllowed) {
			return;
		}
		setBackground(new Color(0, 0, 0));
	}
}
