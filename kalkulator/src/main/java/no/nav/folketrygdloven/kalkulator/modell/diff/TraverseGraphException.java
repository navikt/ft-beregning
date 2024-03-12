package no.nav.folketrygdloven.kalkulator.modell.diff;


public class TraverseGraphException extends RuntimeException {

    public TraverseGraphException(String message, Throwable t) {
        super(message, t);
    }

    public TraverseGraphException(String message) {
        super(message);
    }
}
