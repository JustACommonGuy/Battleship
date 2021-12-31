package justacommonguy.battleshipgui.cell;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import justacommonguy.battleshipgui.ship.ShipLocation;

public class EnemyMap extends Map<EnemyCell> implements AttackListener, ActionListener {

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

	@Override
	public void actionPerformed(ActionEvent e) {
		latch.countDown();
	}

	public synchronized ShipLocation sendAttackGuess() {
		allowInteraction(true);
		latch = new CountDownLatch(1);
		
		try {
			latch.await();
		}
		catch (InterruptedException e) {}

		allowInteraction(false);
		return attackGuess;
	}

	@Override
	public void attacked(ShipLocation attackGuess) {
		this.attackGuess = attackGuess;
		//// System.out.println("Selected " + attackGuess);
	}

	@Override
	public String toString() {
		return "EnemyMap";
	}
}
