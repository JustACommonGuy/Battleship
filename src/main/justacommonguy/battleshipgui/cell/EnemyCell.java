package justacommonguy.battleshipgui.cell;

import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import java.awt.Color;
import java.awt.event.MouseEvent;

import justacommonguy.battleshipgui.ship.ShipLocation;

public class EnemyCell extends Cell{

	private static boolean attackAllowed;
	private boolean isEnabled;

	private static final Color MISS_COLOR = gameSettings.getColor("enemy_miss_color");
	private static final Color SHIP_COLOR = gameSettings.getColor("enemy_ship_color");
	private static final Color HIT_COLOR = gameSettings.getColor("enemy_hit_color");
	private static final Color KILL_COLOR = gameSettings.getColor("enemy_kill_color");

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
