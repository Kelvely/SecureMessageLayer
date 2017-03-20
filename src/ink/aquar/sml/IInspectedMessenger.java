package ink.aquar.sml;

public interface IInspectedMessenger {
	
	public void sendData(int index, byte[] data);
	
	public void resetIndex() throws UnsupportedProtocolException;
	
	public void respondIndexReset() throws UnsupportedProtocolException;
	
	public void reconnect();
	
	public void respondReconnect();

}
