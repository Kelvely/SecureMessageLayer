package ink.aquar.sml;

public class UnsupportedProtocolException extends Exception {

	private static final long serialVersionUID = 5909400968923911669L;
	
	public final short operation;
	
	public UnsupportedProtocolException(short operation) {
		super();
		this.operation = operation;
	}
	
	public UnsupportedProtocolException(short operation, String message) {
		super(message);
		this.operation = operation;
	}

}
