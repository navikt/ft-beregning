package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import java.util.List;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;

public interface AndelGradering {

    AktivitetStatusV2 getAktivitetStatus();

    List<Gradering> getGraderinger();

    boolean erNyAktivitet();

    List<Refusjonskrav> getGyldigeRefusjonskrav();

    Arbeidsforhold getArbeidsforhold();
}
