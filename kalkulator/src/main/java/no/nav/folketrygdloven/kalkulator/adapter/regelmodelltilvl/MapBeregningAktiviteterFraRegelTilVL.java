package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapOpptjeningAktivitetFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class MapBeregningAktiviteterFraRegelTilVL {

    private MapBeregningAktiviteterFraRegelTilVL() {
        // Skjul
    }

    public static BeregningAktivitetAggregatDto map(AktivitetStatusModell regelmodell) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(regelmodell.getSkjæringstidspunktForOpptjening());
        regelmodell.getAktivePerioder().forEach(aktivPeriode -> builder.leggTilAktivitet(
            mapAktivPeriode(aktivPeriode)
        ));
        return builder.build();
    }

    private static BeregningAktivitetDto mapAktivPeriode(AktivPeriode aktivPeriode) {
        var builder = BeregningAktivitetDto.builder();
        builder.medOpptjeningAktivitetType(MapOpptjeningAktivitetFraRegelTilVL.map(aktivPeriode.getAktivitet()));
        builder.medPeriode(Intervall.fraOgMedTilOgMed(aktivPeriode.getPeriode().getFom(), aktivPeriode.getPeriode().getTom()));
        var arbeidsforhold = aktivPeriode.getArbeidsforhold();
        if (arbeidsforhold != null) {
            var arbeidsgiver = mapArbeidsgiver(arbeidsforhold);
            builder.medArbeidsgiver(arbeidsgiver);
            var arbeidsforholdRef = InternArbeidsforholdRefDto.ref(arbeidsforhold.getArbeidsforholdId());
            builder.medArbeidsforholdRef(arbeidsforholdRef);
        }
        return builder.build();
    }

    private static Arbeidsgiver mapArbeidsgiver(Arbeidsforhold arbeidsforhold) {
        return MapArbeidsforholdFraRegelTilVL.map(arbeidsforhold.getReferanseType(), arbeidsforhold.getOrgnr(), arbeidsforhold.getAktørId());
    }
}
