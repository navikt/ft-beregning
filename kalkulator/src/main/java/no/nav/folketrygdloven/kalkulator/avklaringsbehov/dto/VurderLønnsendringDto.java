package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class VurderLønnsendringDto {

    private Boolean erLønnsendringIBeregningsperioden;

    public VurderLønnsendringDto(Boolean erLønnsendringIBeregningsperioden) { // NOSONAR
        this.erLønnsendringIBeregningsperioden = erLønnsendringIBeregningsperioden;
    }

    public Boolean erLønnsendringIBeregningsperioden() {
        return erLønnsendringIBeregningsperioden;
    }

    public void setErLønnsendringIBeregningsperioden(Boolean erLønnsendringIBeregningsperioden) {
        this.erLønnsendringIBeregningsperioden = erLønnsendringIBeregningsperioden;
    }

    @Override
    public String toString() {
        return "VurderLønnsendringDto{" +
                "erLønnsendringIBeregningsperioden=" + erLønnsendringIBeregningsperioden +
                '}';
    }
}
