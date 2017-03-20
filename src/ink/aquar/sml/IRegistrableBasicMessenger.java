package ink.aquar.sml;

public interface IRegistrableBasicMessenger extends IBasicMessenger {
	
	public void registerReceptor(String name, IBasicMessageReceptor receptor);
	
	public void unregisterReceptor(String name);

}
