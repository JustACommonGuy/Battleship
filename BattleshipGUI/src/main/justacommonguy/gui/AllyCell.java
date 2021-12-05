package justacommonguy.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import justacommonguy.ShipLocation;

public class AllyCell extends Cell{

	private static boolean placementAllowed;

	public AllyCell(ShipLocation location, HighlightInitiator xInitiator, HighlightInitiator yInitiator) {
		super(location, xInitiator, yInitiator);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!placementAllowed) {
			return;
		}
		//TODO Save it to old_color
		setBackground(new Color(255, 255, 255));
	}
	
}
