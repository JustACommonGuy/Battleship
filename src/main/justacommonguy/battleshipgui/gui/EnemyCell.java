package justacommonguy.battleshipgui.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import justacommonguy.battleshipgui.GameServer;
import justacommonguy.battleshipgui.ShipLocation;

public class EnemyCell extends Cell{

	private static boolean attackAllowed;

	private static Color MISS = GameServer.settings.getColor("enemy_miss_color");
	private static Color SHIP = GameServer.settings.getColor("enemy_ship_color");
	private static Color HIT = GameServer.settings.getColor("enemy_hit_color");
	private static Color KILL = GameServer.settings.getColor("enemy_kill_color");

	public EnemyCell(ShipLocation location, HighlightInitiator xInitiator, HighlightInitiator yInitiator) {
		super(location, xInitiator, yInitiator);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!attackAllowed) {
			return;
		}
		//TODO
	}
}
