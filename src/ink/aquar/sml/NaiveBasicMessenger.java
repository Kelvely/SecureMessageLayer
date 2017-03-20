package ink.aquar.sml;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NaiveBasicMessenger implements IRegistrableBasicMessenger {
	
	public final NaiveBasicMessenger parterner;
	
	private final ReadWriteLock receptorsRWL = new ReentrantReadWriteLock();
	private final Map<String, IBasicMessageReceptor> receptors = new HashMap<String, IBasicMessageReceptor>();
	
	public NaiveBasicMessenger() {
		this.parterner = new NaiveBasicMessenger(this);
	}
	
	private NaiveBasicMessenger(NaiveBasicMessenger parterner) {
		this.parterner = parterner;
	}
	
	public NaiveBasicMessenger getParterner() {
		return parterner;
	}
	
	@Override
	public void sendData(byte[] data) {
		parterner.receiveData(data);
	}

	@Override
	public void registerReceptor(String name, IBasicMessageReceptor receptor) {
		receptorsRWL.writeLock().lock();
		receptors.put(name, receptor);
		receptorsRWL.writeLock().unlock();
	}
	
	@Override
	public void unregisterReceptor(String name) {
		receptorsRWL.writeLock().lock();
		receptors.remove(name);
		receptorsRWL.writeLock().unlock();
	}
	
	private void receiveData(byte[] data) {
		message(data);
	}
	
	private void message(byte[] data) {
		receptorsRWL.readLock().lock();
		for (IBasicMessageReceptor receptor : receptors.values()) {
			byte[] copiedData = new byte[data.length];
			System.arraycopy(data, 0, copiedData, 0, data.length);
			receptor.onMessage(copiedData);
		}
		receptorsRWL.readLock().unlock();
	}
	
}
