package ink.aquar.sml;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jacoco.core.internal.data.CRC64;

public final class InspectedMessenger implements IRegistrableInspectedMessenger {
	
	private static final Map<Short, IInspectHandler> INSPECT_HANDLERS = new HashMap<Short, IInspectHandler>();
	static {
		INSPECT_HANDLERS.put((short) 1, new InspectHandler_1());
	}
	
	private static final short DEFAULT_VERSION = 1;
	
	private final IBasicMessenger basicMessenger;
	
	private final ReadWriteLock receptorsRWL = new ReentrantReadWriteLock();
	private final Map<String, IInspectedMessageReceptor> receptors = new HashMap<String, IInspectedMessageReceptor>();
	
	private IInspectHandler inspectHandler;
	
	public InspectedMessenger(String listenerName, IRegistrableBasicMessenger basicMessenger) {
		this(listenerName, basicMessenger,  DEFAULT_VERSION);
	}
	
	public InspectedMessenger(String listenerName, IRegistrableBasicMessenger basicMessenger, short protocolVersion) {
		inspectHandler = INSPECT_HANDLERS.get(protocolVersion);
		if(inspectHandler == null) inspectHandler = INSPECT_HANDLERS.get(DEFAULT_VERSION);
		basicMessenger.registerReceptor(listenerName, new BasicMessageReceptor());
		this.basicMessenger = basicMessenger;
	}

	@Override
	public void sendData(int index, byte[] data) {
		IInspectHandler inspectHandler = this.inspectHandler;
		basicMessenger.sendData(
				inspectHandler.wrap(data, index, BasicMessageOperations.SEND_PACKET)
				);
		//Then wait for timeout.
	}
	
	@Override
	public void resetIndex() throws UnsupportedProtocolException {
		IInspectHandler inspectHandler = this.inspectHandler;
		checkSupport(inspectHandler, MessageOperations.RESET_INDEX);
		basicMessenger.sendData(
				inspectHandler.wrap(null, 0, MessageOperations.RESET_INDEX)
				);
	}
	
	@Override
	public void respondIndexReset() throws UnsupportedProtocolException {
		IInspectHandler inspectHandler = this.inspectHandler;
		checkSupport(inspectHandler, MessageOperations.RESET_INDEX_RESPONSE);
		basicMessenger.sendData(
				inspectHandler.wrap(null, 0, MessageOperations.RESET_INDEX_RESPONSE)
				);
	}

	@Override
	public void reconnect() {
		IInspectHandler inspectHandler = this.inspectHandler;
		basicMessenger.sendData(
				inspectHandler.wrap(null, 0, BasicMessageOperations.RECONNECT)
				);
	}
	
	@Override
	public void respondReconnect() {
		IInspectHandler inspectHandler = this.inspectHandler;
		basicMessenger.sendData(
				inspectHandler.wrap(null, 0, BasicMessageOperations.RECONNECT_RESPONSE)
				);
	}
	
	private static void checkSupport(IInspectHandler inspectHandler, short operation) throws UnsupportedProtocolException {
		if(!inspectHandler.isOperationSupported(operation)) 
			throw new UnsupportedProtocolException(
					operation, 
					"Unsupported Operation \"" + operation + "\" in Version " + inspectHandler.getProtocolVersion()
					);
	}
	
	private void resolve(byte[] rawData){
		if(rawData.length < 24) return; //This must be a bad data and make system explode :P
		long reportedSum = fromBytesToLong(0, 8, rawData);
		byte[] data = getData(rawData);
		long checksum = CRC64.checksum(data);
		long headReportedSum = fromBytesToLong(8, 16, rawData);
		byte[] head = getHead(rawData);
		long headsum = CRC64.checksum(head);
		int index = fromBytesToInt(2, 6, head);
		if(headsum != headReportedSum) {
			return;
		}
		short protocolVersion = fromBytesToShort(0, 2, head);
		IInspectHandler inspectHandler = this.inspectHandler;
		if(protocolVersion != inspectHandler.getProtocolVersion()){
			IInspectHandler newInspectHandler = INSPECT_HANDLERS.get(protocolVersion);
			/*
			 * Because when sending broken data message, remote data will change to corresponding
			 * protocol automatically, in which broken data messages also contain version info.
			 */
			if(newInspectHandler == null) {
				reportBrokenData(index);
			} else {
				inspectHandler = newInspectHandler;
				this.inspectHandler = inspectHandler;
			}
		}
		if(checksum != reportedSum && data.length > 0) reportBrokenData(index);
		else inspectHandler.handle(rawData, this);
	}
	
	private void reportBrokenData(int index) {
		IInspectHandler inspectHandler = this.inspectHandler;
		basicMessenger.sendData(
				inspectHandler.wrap(null, index, BasicMessageOperations.REPORT_BROKEN)
				);
	}
	
	private void receiveData(int index, byte[] data){
		receptorsRWL.readLock().lock();
		for (IInspectedMessageReceptor receptor : receptors.values()) {
			receptor.onMessage(index, data);
		}
		receptorsRWL.readLock().unlock();
	}
	
	private void confirmRemoteData(int index){
		receptorsRWL.readLock().lock();
		for (IInspectedMessageReceptor receptor : receptors.values()) {
			receptor.onRemoteDataConfirm(index);
		}
		receptorsRWL.readLock().unlock();
	}
	
	private void messageRemoteDataBroken(int index) {
		receptorsRWL.readLock().lock();
		for (IInspectedMessageReceptor receptor : receptors.values()) {
			receptor.onRemoteDataBroken(index);
		}
		receptorsRWL.readLock().unlock();
	}
	
	private void messageReconnect(){
		receptorsRWL.readLock().lock();
		for (IInspectedMessageReceptor receptor : receptors.values()) {
			receptor.onReconnect();
		}
		receptorsRWL.readLock().unlock();
	}
	
	private void confirmReconnect(){
		receptorsRWL.readLock().lock();
		for (IInspectedMessageReceptor receptor : receptors.values()) {
			receptor.onReconnectSuccess();
		}
		receptorsRWL.readLock().unlock();
	}
	
	private void messageResetIndex(){
		receptorsRWL.readLock().lock();
		for (IInspectedMessageReceptor receptor : receptors.values()) {
			receptor.onRemoteResetIndex();
		}
		receptorsRWL.readLock().unlock();
	}
	
	private void confirmIndexReset(){
		receptorsRWL.readLock().lock();
		for (IInspectedMessageReceptor receptor : receptors.values()) {
			receptor.onIndexResetConfirm();
		}
		receptorsRWL.readLock().unlock();
	}
	
	private static byte[] getData(byte[] rawData) {
		byte[] datagram = new byte[rawData.length-24];
		System.arraycopy(rawData, 24, datagram, 0, datagram.length);
		return datagram;
	}
	
	private static byte[] getHead(byte[] rawData) {
		byte[] head = new byte[8];
		System.arraycopy(rawData, 16, head, 0, 8);
		return head;
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
	
	private static interface IInspectHandler {
		
		public byte[] wrap(byte[] data, int index, short operation);
		
		public void handle(byte[] rawData, InspectedMessenger messenger);
		
		public short getProtocolVersion();
		
		public boolean isOperationSupported(short operation);
		
	}
	
	private static final class InspectHandler_1 implements IInspectHandler {
		
		private final static byte[] EMPTY_BYTE_ARRAY = new byte[0];

		@Override
		public byte[] wrap(byte[] data, int index, short operation) {
			if(data == null) data = EMPTY_BYTE_ARRAY;
			byte[] packedData = new byte[data.length + 24];
			fromLongToBytes(0, CRC64.checksum(data), packedData);
			fromShortToBytes(16, getProtocolVersion(), packedData);
			fromIntToBytes(18, index, packedData);
			fromShortToBytes(22, operation, packedData);
			fromLongToBytes(8, CRC64.checksum(getHead(packedData)), packedData);
			System.arraycopy(data, 0, packedData, 24, data.length);
			return packedData;
		}

		@Override
		public void handle(byte[] rawData, InspectedMessenger messenger) {
			short operation = fromBytesToShort(22, 24, rawData);
			int index = fromBytesToInt(18, 22, rawData);
			if (operation == BasicMessageOperations.OTHER_OPERATION) {
				return; //Here is no complex other-operation on version 1 :D
			} else if(operation == BasicMessageOperations.SEND_PACKET) {
				messenger.basicMessenger.sendData(
						wrap(null, index, BasicMessageOperations.RESPONSE_RECEIVE)
						);
				messenger.receiveData(index, getData(rawData));
			} else if(operation == BasicMessageOperations.RESPONSE_RECEIVE) {
				messenger.confirmRemoteData(index);
			} else if(operation == BasicMessageOperations.REPORT_BROKEN) {
				messenger.messageRemoteDataBroken(index);
			} else if(operation == BasicMessageOperations.RECONNECT) {
				messenger.messageReconnect();
			} else if(operation == BasicMessageOperations.RECONNECT_RESPONSE){
				messenger.confirmReconnect();
			} else if(operation == MessageOperations.RESET_INDEX) {
				messenger.messageResetIndex();
			} else if(operation == MessageOperations.RESET_INDEX_RESPONSE){
				messenger.confirmIndexReset();
			} else {
				messenger.reportBrokenData(index); //Unknown operation :P
			}
		}

		@Override
		public short getProtocolVersion() {
			return 1;
		}

		@Override
		public boolean isOperationSupported(short operation) {
			return ! (operation < 0 | operation > 7);
		}
		
		/**
		 * This getData is different with 
		 */
		private static byte[] getData(byte[] rawData) {
			byte[] datagram = new byte[rawData.length-24];
			System.arraycopy(rawData, 24, datagram, 0, datagram.length);
			return datagram;
		}
		
	}

	@Override
	public void registerReceptor(String name, IInspectedMessageReceptor receptor) {
		receptorsRWL.writeLock().lock();
		receptors.put(name, receptor);
		receptorsRWL.writeLock().unlock();
	}
	
	@Override
	public void unregisterReceptor(String name){
		receptorsRWL.writeLock().lock();
		receptors.remove(name);
		receptorsRWL.writeLock().unlock();
	}
	
	public final class BasicMessageReceptor implements IBasicMessageReceptor {

		@Override
		public void onMessage(byte[] data) {
			resolve(data);
		}
		
	}
	
	private final static class BasicMessageOperations {
		
		public final static short OTHER_OPERATION = 0;
		public final static short SEND_PACKET = 1;
		public final static short RESPONSE_RECEIVE = 2;
		public final static short REPORT_BROKEN = 3;
		public final static short RECONNECT = 4;
		public final static short RECONNECT_RESPONSE = 5;
		
	}
	
	private final static class MessageOperations {
		public final static short RESET_INDEX = 6;
		public final static short RESET_INDEX_RESPONSE = 7;
	}

}
