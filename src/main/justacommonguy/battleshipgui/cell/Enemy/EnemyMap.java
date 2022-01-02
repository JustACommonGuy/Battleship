package justacommonguy.battleshipgui.cell.Enemy;

import java.util.concurrent.CountDownLatch;

import justacommonguy.battleshipgui.cell.HighlightInitiator;
import justacommonguy.battleshipgui.cell.Map;
import justacommonguy.battleshipgui.ship.ShipLocation;

public class EnemyMap extends Map<EnemyCell> implements AttackListener {

	private Attacker attacker = new Attacker();
	private ShipLocation attackGuess;
	// TODO Use a CyclicBarrier to reset latch
	private CountDownLatch latch;

	public EnemyMap() {
		attacker.addAttackListener(this);
	}

	@Override
	protected EnemyCell constructCell(ShipLocation location, HighlightInitiator xInit, HighlightInitiator yInit) {
		return new EnemyCell(location, xInit, yInit);
	}

	@Override
	public void allowInteraction(boolean allow) {
		EnemyCell.setAttackAllowed(allow);
	}

	public ShipLocation sendAttackGuess() {
		allowInteraction(true);
		latch = new CountDownLatch(1);
		
		try {
			latch.await();
		}
		catch (InterruptedException e) {}

		allowInteraction(false);
		ShipLocation guess = attackGuess;
		attackGuess = null;
		return guess;
	}

	@Override
	public void attacked(ShipLocation attackGuess) {
		if (attackGuess != null) {
			this.attackGuess = attackGuess;
			latch.countDown();
			//// System.out.println("Selected " + attackGuess);
		}
	}

	@Override
	public String toString() {
		return "EnemyMap";
	}
}
