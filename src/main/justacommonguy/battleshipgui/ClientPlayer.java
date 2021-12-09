package justacommonguy.battleshipgui;

import java.net.Socket;

public class ClientPlayer extends Player{
	private Socket socket;

	public ClientPlayer(String name) {
		super(name);
	}

	@Override
	public void setUpShips() {
		// TODO Auto-generated method stub
		super.setUpShips();
	}

	@Override
	public ShipLocation attack() {
		// TODO Auto-generated method stub
		return super.attack();
	}
}
