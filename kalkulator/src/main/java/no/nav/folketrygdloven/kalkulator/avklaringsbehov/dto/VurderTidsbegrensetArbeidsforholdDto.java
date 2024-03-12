package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.ArrayList;
import java.util.List;

public class VurderTidsbegrensetArbeidsforholdDto {

    private List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold;

    public VurderTidsbegrensetArbeidsforholdDto(List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold) { // NOSONAR
        this.fastsatteArbeidsforhold = new ArrayList<>(fastsatteArbeidsforhold);
    }

    public List<VurderteArbeidsforholdDto> getFastsatteArbeidsforhold() {
        return fastsatteArbeidsforhold;
    }

    public void setFastsatteArbeidsforhold(List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold) {
        this.fastsatteArbeidsforhold = fastsatteArbeidsforhold;
    }
}
