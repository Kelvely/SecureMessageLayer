package ink.aquar.sml;

public interface IRegistrableInspectedMessenger extends IInspectedMessenger{
	
	public void registerReceptor(String name, IInspectedMessageReceptor receptor);
	
	public void unregisterReceptor(String name);
	
}
