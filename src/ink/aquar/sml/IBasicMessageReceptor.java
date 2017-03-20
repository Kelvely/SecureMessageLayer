package ink.aquar.sml;

public interface IBasicMessageReceptor {
	/**
	 * When remote data is received on this terminal.
	 */
	public void onMessage(byte[] data);

}
