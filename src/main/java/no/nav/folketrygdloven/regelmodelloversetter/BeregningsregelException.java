package no.nav.folketrygdloven.regelmodelloversetter;

public class BeregningsregelException extends RuntimeException {

    public BeregningsregelException(String message) {
		super(message);
    }

	public BeregningsregelException(String message, Throwable cause) {
		super(message, cause);
	}

}
