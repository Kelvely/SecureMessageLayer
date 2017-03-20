package ink.aquar.sml;

public interface IRegistrableSecureMessenger extends ISecureMessenger {
	
	public void registerReceptor(String name, ISecureMessageReceptor receptor);
	
	public void unregisterReceptor(String name);

}
