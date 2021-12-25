package justacommonguy.battleshipgui.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import static justacommonguy.battleshipgui.GameLauncher.gameSettings;
import justacommonguy.battleshipgui.ShipLocation;

public class EnemyCell extends Cell{

	private static boolean attackAllowed;
	private static boolean isEnabled;

	private static Color MISS_COLOR = gameSettings.getColor("enemy_miss_color");
	private static Color SHIP_COLOR = gameSettings.getColor("enemy_ship_color");
	private static Color HIT_COLOR = gameSettings.getColor("enemy_hit_color");
	private static Color KILL_COLOR = gameSettings.getColor("enemy_kill_color");

	public EnemyCell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit) {
		super(location, xInit, yInit);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (attackAllowed && isEnabled) {
			//TODO Remember to set enabled to false when already attacked.
		}
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
