package ink.aquar.sml;

import org.jacoco.core.internal.data.CRC64;

public class Test {
	
	public static void main(String[] args) {
		NaiveBasicMessenger messengerA = new NaiveBasicMessenger();
		NaiveBasicMessenger messengerB = ((NaiveBasicMessenger) messengerA).getParterner();
		final IRegistrableInspectedMessenger a = new InspectedMessenger("default", messengerA);
		final IRegistrableInspectedMessenger b = new InspectedMessenger("default", messengerB);
		a.registerReceptor("default", new IInspectedMessageReceptor() {
			
			@Override
			public void onRemoteResetIndex() {
				System.out.println("A: B requsted to reset index");
				try {
					a.respondIndexReset();
				} catch (UnsupportedProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onRemoteDataConfirm(int index) {
				System.out.println("A: B confirmed data that has index of " + index);
			}
			
			@Override
			public void onRemoteDataBroken(int index) {
				System.out.println("A: B reported that data " + index + " has broken on way transmit.");
			}
			
			@Override
			public void onReconnectSuccess() {
				System.out.println("A: Reconnected successfully on B side.");
			}
			
			@Override
			public void onReconnect() {
				System.out.println("A: B requested to reconnect.");
				a.respondReconnect();
			}
			
			@Override
			public void onMessage(int remoteIndex, byte[] data) {
				System.out.println("A: B successfully transmit a packet that has index of " + remoteIndex);
				System.out.println("A: Content converted to String are shown below.");
				System.out.println(new String(data));
			}
			
			@Override
			public void onIndexResetConfirm() {
				System.out.println("A: Index reset has been done on remote B");
			}
		});
		
		b.registerReceptor("default", new IInspectedMessageReceptor() {
			
			@Override
			public void onRemoteResetIndex() {
				System.out.println("B: A requsted to reset index");
				try {
					b.respondIndexReset();
				} catch (UnsupportedProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onRemoteDataConfirm(int index) {
				System.out.println("B: A confirmed data that has index of " + index);
			}
			
			@Override
			public void onRemoteDataBroken(int index) {
				System.out.println("B: A reported that data " + index + " has broken on way transmit.");
			}
			
			@Override
			public void onReconnectSuccess() {
				System.out.println("B: Reconnected successfully on A side.");
			}
			
			@Override
			public void onReconnect() {
				System.out.println("B: A requested to reconnect.");
				b.respondReconnect();
			}
			
			@Override
			public void onMessage(int remoteIndex, byte[] data) {
				System.out.println("B: A successfully transmit a packet that has index of " + remoteIndex);
				System.out.println("B: Content converted to String are shown below.");
				System.out.println(new String(data));
			}
			
			@Override
			public void onIndexResetConfirm() {
				System.out.println("B: Index reset has been done on remote A");
			}
		});
		
		System.out.println("Started communication.");
		
		a.sendData(10, "Hello, bro, nice to meet you!".getBytes());
		
		byte[] data = wrap("Hello, bro, nice to meet you!".getBytes(), 11, (short) 1);
		
		data[27] = 0;
		
		messengerA.sendData(data);
		
	}
	
	public static byte[] wrap(byte[] data, int index, short operation) {
		if(data == null) data = new byte[0];
		byte[] packedData = new byte[data.length + 24];
		fromLongToBytes(0, CRC64.checksum(data), packedData);
		fromShortToBytes(16, (short) 1, packedData);
		fromIntToBytes(18, index, packedData);
		fromShortToBytes(22, operation, packedData);
		fromLongToBytes(8, CRC64.checksum(getHead(packedData)), packedData);
		System.arraycopy(data, 0, packedData, 24, data.length);
		return packedData;
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
}
