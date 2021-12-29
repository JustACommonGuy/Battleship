package justacommonguy.battleshipgui.player;

import justacommonguy.battleshipgui.cell.EnemyCell;
import justacommonguy.battleshipgui.utils.Faction;

public class EnemyPlayer extends Player<EnemyCell> {

	public EnemyPlayer(String name) {
		super(name, Faction.ENEMY);
	}
	
}
