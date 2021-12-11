package justacommonguy.battleshipgui.networking;

public interface NetworkComponent {
	/* Yes, I know that an object return type leads to a terrible software design. */
	public Object respondRequest(Request request);
}
