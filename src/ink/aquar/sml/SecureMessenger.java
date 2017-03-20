package ink.aquar.sml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 
 * @author KevinIry#Aquarink
 * 
 * I have no power to write this ˊ_>ˋ I need someone to do it with me. 
 * If you need such encryption service, @see 
 */
public class SecureMessenger implements IRegistrableSecureMessenger {
	
	// ProtocolVersion | SessionID | Operation | Datagram{Index | Data}, {?...} 
	
	private final IInspectedMessenger messenger;
	
	private final ReadWriteLock receptorsRWL = new ReentrantReadWriteLock();
	private final Map<String, ISecureMessageReceptor> receptors = new HashMap<String, ISecureMessageReceptor>();
	
	private boolean isConnected;
	private int sessionID;
	
	public SecureMessenger(String listenerName, IRegistrableBasicMessenger basicMessenger) {
		messenger = new InspectedMessenger(listenerName, basicMessenger);
	}
	
	private final List<byte[]> dataQueue = new LinkedList<byte[]>();
	private final Lock queueLock = new ReentrantLock();
	
	private static final short PROTOCOL_VERSION = 1;

	@Override
	public void connect() {
		
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public void expireSession() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keepAlive() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendData(int index, byte[] data) {
		//byte[] wrappedData = wrap(PROTOCOL_VERSION, sessionID, MessageOperations.SEND_PACKET, data);
		queueLock.lock();
		dataQueue.add(data);
		queueLock.unlock();
	}

	@Override
	public void resetIndex() throws UnsupportedProtocolException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void respondIndexReset() throws UnsupportedProtocolException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void respondReconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerReceptor(String name, ISecureMessageReceptor receptor) {
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
	
	private byte[] wrap(short protocolVersion, int sessionID, short operation, byte[] data) {
		byte[] wrapedData = new byte[data.length + 8];
		fromShortToBytes(0, protocolVersion, wrapedData);
		fromIntToBytes(2, sessionID, wrapedData);
		fromShortToBytes(6, operation, wrapedData);
		System.arraycopy(data, 0, wrapedData, 8, data.length);
		return data;
	}
	
	private void resolve(byte[] rawData) {
		// TODO
	}
	
	private void checkConnection() {
		if(!isConnected) {
			throw new NoConnectionException();
		}
	}
	
	private static long fromBytesToLong(int start, int dest, byte[] bytes) {
		long sum = 0x0;
		for(int i=start; i<dest && i<bytes.length; i++) {
			sum = sum<<8 | (0xFF & bytes[i]);
		}
		return sum;
	}
	
	/**
	 * @return a byte array has 8 elements.
	 */
	private static void fromLongToBytes(int start, long value, byte[] dest) {
		for(int i=0;i<8 && i+start < dest.length; i++) {
			dest[i+start] = (byte)(value>>((7-i)<<3));
		}
	}
	
	private static int fromBytesToInt(int start, int dest, byte[] bytes) {
		int sum = 0x0;
		for(int i=start; i<dest && i<bytes.length; i++) {
			sum = sum<<8 | (0xFF & bytes[i]);
		}
		return sum;
	}
	
	/**
	 * @return a byte array has 8 elements.
	 */
	private static void fromIntToBytes(int start, int value, byte[] dest) {
		for(int i=0;i<4 && i+start < dest.length; i++) {
			dest[i+start] = (byte)(value>>((3-i)<<3));
		}
	}
	
	private static short fromBytesToShort(int start, int dest, byte[] bytes) {
		return (short) fromBytesToInt(start, dest, bytes);
	}
	
	private static void fromShortToBytes(int start, short value, byte[] dest) {
		for(int i=0;i<2 && i+start < dest.length; i++) {
			dest[i+start] = (byte)(value>>((1-i)<<3));
		}
	}
	
	public final static class MessageOperations {
		
		public final static short OTHER_OPERATION = 0;
		public final static short PUBLIC_KEY_REQUEST = 1;
		public final static short PUBLIC_KEY_RESPONSE = 2;
		public final static short PUBLIC_KEY_REQUEST_DENY = 3;
		public final static short START_SESSION = 4;
		public final static short SESSION_CONFIRM = 5;
		public final static short START_SESSION_DENY = 6;
		public final static short SEND_PACKET = 7;
		public final static short SUCCESS_TO_DECRYPT = 8;
		public final static short FAIL_TO_DECRYPT = 9;
		public final static short EXPIRE_SESSION = 10;
		public final static short UNSUPPORTED_VERSION = 11;
		
	}

}
