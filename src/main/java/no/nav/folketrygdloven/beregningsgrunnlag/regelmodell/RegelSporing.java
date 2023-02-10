package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

public record RegelSporing(String input, String sporing) {

	public String getInput() {
        return input;
    }

    public String getSporing() {
        return sporing;
    }
}
