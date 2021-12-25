package justacommonguy.battleshipgui;

import justacommonguy.battleshipgui.gui.EnemyCell;

public class EnemyPlayer extends Player<EnemyCell> {

	public EnemyPlayer(String name) {
		super(name, Faction.ENEMY);
	}
	
}
