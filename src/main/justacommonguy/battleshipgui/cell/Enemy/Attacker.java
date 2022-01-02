package justacommonguy.battleshipgui.cell.Enemy;

import java.util.ArrayList;

import justacommonguy.battleshipgui.ship.ShipLocation;

class Attacker {

	private ArrayList<AttackListener> listenerList = new ArrayList<>();

	public Attacker() {
		EnemyCell.setAttacker(this);
	}

	public void attacked(ShipLocation location) {
		for (AttackListener listener : listenerList) {
			listener.attacked(location);
		}
	}

	public void addAttackListener(AttackListener listener) {
		listenerList.add(listener);
	}

	public void removeAttackListener(AttackListener listener) {
		listenerList.remove(listener);
	}
}
