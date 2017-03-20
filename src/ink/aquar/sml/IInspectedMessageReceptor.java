package ink.aquar.sml;

public interface IInspectedMessageReceptor {
	
	/**
	 * The data is parsed and confirmed that the data is not broken.<br>
	 * If the data is okay, the response is automatically sent, whereas if the data 
	 * is broken, the broken data report is automatically sent.
	 */
	public void onMessage(int remoteIndex, byte[] data);
	
	/**
	 * This would be fired when remote terminal confirmed that the data is OKAY.
	 */
	public void onRemoteDataConfirm(int index);
	
	/**
	 * When remote index(send pool) need to reset, remote terminal will send this to
	 * tell here that index should be reset to 0.
	 */
	public void onRemoteResetIndex();
	
	/**
	 * When remote terminal confirmed that the index is reset.
	 */
	public void onIndexResetConfirm();
	
	/**
	 * This would be fired when remote terminal confirmed that the data is BROKEN.
	 */
	public void onRemoteDataBroken(int index);
	
	/**
	 * When this is called by remote terminal, both side of the data cache and index
	 * map should be reset to nothing inside.
	 */
	public void onReconnect();
	
	/**
	 * When remote side confirmed that all the things are reset. 
	 */
	public void onReconnectSuccess();
	
	
	
}
