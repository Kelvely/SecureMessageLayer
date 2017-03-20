package ink.aquar.sml;

public interface ISecureMessageReceptor extends IInspectedMessageReceptor {
	
	public boolean onHandshake(byte[] publicKey, byte[] extraDatagram);
	
	public void onConnect();
	
	public void onSessionExpire();
	
	public void onKeepAlive();

}
