package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;

public interface AndelGradering {

    AktivitetStatusV2 getAktivitetStatus();

    List<Gradering> getGraderinger();

    boolean erNyAktivitet();

    List<Refusjonskrav> getGyldigeRefusjonskrav();

    Arbeidsforhold getArbeidsforhold();
}
