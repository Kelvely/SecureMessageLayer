package ink.aquar.sml;

public interface ISecureMessenger extends IInspectedMessenger {
	
	/**
	 * Connections are automatically handled.
	 * if you want to disable a connection, just 
	 */
	public void connect();
	
	public boolean isConnected();
	
	public void expireSession();
	
	public void keepAlive();

}
