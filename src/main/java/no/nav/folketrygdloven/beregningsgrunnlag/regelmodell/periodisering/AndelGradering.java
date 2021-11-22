package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public interface AndelGradering {

    AktivitetStatusV2 getAktivitetStatus();

    List<Gradering> getGraderinger();

	boolean erNyAktivitetPåDato(LocalDate dato);

    Arbeidsforhold getArbeidsforhold();
}
