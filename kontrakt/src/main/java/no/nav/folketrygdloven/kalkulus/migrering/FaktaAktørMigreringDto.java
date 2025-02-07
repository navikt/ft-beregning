package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;

public class FaktaAktørMigreringDto extends BaseMigreringDto {

    @Valid
    private FaktaVurderingMigreringDto erNyIArbeidslivetSN;

    @Valid
    private FaktaVurderingMigreringDto erNyoppstartetFL;

    @Valid
    private FaktaVurderingMigreringDto harFLMottattYtelse;

    @Valid
    private FaktaVurderingMigreringDto skalBesteberegnes;

    @Valid
    private FaktaVurderingMigreringDto mottarEtterlønnSluttpakke;

    @Valid
    private FaktaVurderingMigreringDto skalBeregnesSomMilitær;

    public FaktaAktørMigreringDto() {
        // Bruker heller settere her siden det er så mange like felter
    }

    public FaktaVurderingMigreringDto getErNyIArbeidslivetSN() {
        return erNyIArbeidslivetSN;
    }

    public void setErNyIArbeidslivetSN(FaktaVurderingMigreringDto erNyIArbeidslivetSN) {
        this.erNyIArbeidslivetSN = erNyIArbeidslivetSN;
    }

    public FaktaVurderingMigreringDto getErNyoppstartetFL() {
        return erNyoppstartetFL;
    }

    public void setErNyoppstartetFL(FaktaVurderingMigreringDto erNyoppstartetFL) {
        this.erNyoppstartetFL = erNyoppstartetFL;
    }

    public FaktaVurderingMigreringDto getHarFLMottattYtelse() {
        return harFLMottattYtelse;
    }

    public void setHarFLMottattYtelse(FaktaVurderingMigreringDto harFLMottattYtelse) {
        this.harFLMottattYtelse = harFLMottattYtelse;
    }

    public FaktaVurderingMigreringDto getSkalBesteberegnes() {
        return skalBesteberegnes;
    }

    public void setSkalBesteberegnes(FaktaVurderingMigreringDto skalBesteberegnes) {
        this.skalBesteberegnes = skalBesteberegnes;
    }

    public FaktaVurderingMigreringDto getMottarEtterlønnSluttpakke() {
        return mottarEtterlønnSluttpakke;
    }

    public void setMottarEtterlønnSluttpakke(FaktaVurderingMigreringDto mottarEtterlønnSluttpakke) {
        this.mottarEtterlønnSluttpakke = mottarEtterlønnSluttpakke;
    }

    public FaktaVurderingMigreringDto getSkalBeregnesSomMilitær() {
        return skalBeregnesSomMilitær;
    }

    public void setSkalBeregnesSomMilitær(FaktaVurderingMigreringDto skalBeregnesSomMilitær) {
        this.skalBeregnesSomMilitær = skalBeregnesSomMilitær;
    }
}
