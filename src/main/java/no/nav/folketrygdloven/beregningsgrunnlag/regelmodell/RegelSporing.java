package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

public class RegelSporing {
    private String input;
    private String sporing;

    RegelSporing(String input, String sporing) {
        this.input = input;
        this.sporing = sporing;
    }

    public String getInput() {
        return input;
    }

    public String getSporing() {
        return sporing;
    }
}
