package no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class BeregningAktivitetTestUtil {

    public static BeregningAktivitetAggregatDto opprettBeregningAktiviteter(LocalDate skjæringstidspunkt, OpptjeningAktivitetType... opptjeningAktivitetTypes) {
        Intervall periode = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusYears(2), skjæringstidspunkt);
        return opprettBeregningAktiviteter(skjæringstidspunkt, periode, opptjeningAktivitetTypes);
    }

    public static BeregningAktivitetAggregatDto opprettBeregningAktiviteter(LocalDate skjæringstidspunkt, Intervall periode, OpptjeningAktivitetType... opptjeningAktivitetTypes) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(skjæringstidspunkt);
        for (OpptjeningAktivitetType aktivitet : opptjeningAktivitetTypes) {
            BeregningAktivitetDto beregningAktivitet = BeregningAktivitetDto.builder()
                .medPeriode(periode)
                .medOpptjeningAktivitetType(aktivitet)
                .build();
            builder.leggTilAktivitet(beregningAktivitet);
        }
        BeregningAktivitetAggregatDto aggregat = builder.build();
        return aggregat;
    }

}
