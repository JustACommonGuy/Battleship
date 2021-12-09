package justacommonguy.battleshipgui.networking;

public interface NetworkComponent {
	//TODO. Might not be needed.
	/* public Request sendRequest(); */
	/* Yes, I know that an object return type leads to a terrible software design. */
	public Object respondRequest();
}
