package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;


public class FaktaAggregatMigreringDto extends BaseMigreringDto {

    @Valid
    private List<FaktaArbeidsforholdMigreringDto> faktaArbeidsforholdListe = new ArrayList<>();

    @Valid
    private FaktaAktørMigreringDto faktaAktør;

    public FaktaAggregatMigreringDto() {
    }

    public FaktaAggregatMigreringDto(List<FaktaArbeidsforholdMigreringDto> faktaArbeidsforholdListe, FaktaAktørMigreringDto faktaAktør) {
        this.faktaArbeidsforholdListe = faktaArbeidsforholdListe;
        this.faktaAktør = faktaAktør;
    }

    public List<FaktaArbeidsforholdMigreringDto> getFaktaArbeidsforholdListe() {
        return faktaArbeidsforholdListe;
    }

    public FaktaAktørMigreringDto getFaktaAktør() {
        return faktaAktør;
    }
}
