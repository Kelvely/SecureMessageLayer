package ink.aquar.sml;

public class NoConnectionException extends RuntimeException {

	private static final long serialVersionUID = 7109328117455539275L;
	
	public NoConnectionException(){
		super();
	}
	
	public NoConnectionException(String message) {
		super(message);
	}

}
