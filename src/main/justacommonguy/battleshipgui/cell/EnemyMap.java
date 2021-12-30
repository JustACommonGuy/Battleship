package justacommonguy.battleshipgui.cell;

import justacommonguy.battleshipgui.ship.ShipLocation;

public class EnemyMap extends Map<EnemyCell> {

	@Override
	protected EnemyCell constructCell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit) {
		return new EnemyCell(location, xInit, yInit);
	}
	
}
