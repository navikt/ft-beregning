package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;


public class FaktaAggregatMigreringDto extends BaseMigreringDto {

    @Valid
    @Size(max=100)
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
