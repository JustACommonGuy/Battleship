package justacommonguy.battleshipgui;

public interface NetworkComponent {
	
	// ?Chat methods. Maybe listenMessages() and sendMessage()
	public void listenRequests();
	public Object respondRequest(Request request);
}