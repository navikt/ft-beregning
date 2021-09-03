package no.nav.folketrygdloven.skjæringstidspunkt.regelmodell;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;

public class BeregningsgrunnlagPrStatus {
    private final AktivitetStatus aktivitetStatus;
    private final List<Arbeidsforhold> arbeidsforholdList;

    public BeregningsgrunnlagPrStatus(AktivitetStatus aktivitetStatus, Arbeidsforhold... arbeidsforhold) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsforholdList = (arbeidsforhold != null)
            ? Arrays.stream(arbeidsforhold).filter(Objects::nonNull).collect(Collectors.toList())
            : Collections.emptyList();
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    void leggTilArbeidsforhold(List<Arbeidsforhold> arbeidsforhold) {
	    arbeidsforholdList.addAll(arbeidsforhold);
    }

    public List<Arbeidsforhold> getArbeidsforholdList() {
        return Collections.unmodifiableList(arbeidsforholdList);
    }
}
