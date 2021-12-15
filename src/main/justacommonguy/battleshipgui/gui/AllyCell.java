package justacommonguy.battleshipgui.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import justacommonguy.battleshipgui.GameServer;
import justacommonguy.battleshipgui.ShipLocation;

public class AllyCell extends Cell{

	private static boolean placementAllowed;

	private static Color MISS = GameServer.settings.getColor("ally_miss_color");
	private static Color SHIP = GameServer.settings.getColor("ally_ship_color");
	private static Color HIT = GameServer.settings.getColor("ally_hit_color");
	private static Color KILL = GameServer.settings.getColor("ally_kill_color");

	public AllyCell(ShipLocation location, HighlightInitiator xInitiator, HighlightInitiator yInitiator) {
		super(location, xInitiator, yInitiator);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!placementAllowed) {
			return;
		}
		//TODO Change ship orientation
		setBackground(new Color(255, 255, 255));
	}
	
}
