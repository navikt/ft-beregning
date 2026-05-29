package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.ytelse.fp;

import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.TilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseKilde;


public class VurderBesteberegningTilfelleUtleder implements TilfelleUtleder {

    private static final Set<OpptjeningAktivitetType> AKTIVITETER_SOM_KAN_AUTOMATISK_BESTEBEREGNES = Set.of(OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetType.DAGPENGER, OpptjeningAktivitetType.SYKEPENGER,
            OpptjeningAktivitetType.FORELDREPENGER, OpptjeningAktivitetType.SVANGERSKAPSPENGER);

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        var kanAutomatiskBesteberegnes = input.getOpptjeningAktiviteterForBeregning().stream()
                .allMatch(a -> AKTIVITETER_SOM_KAN_AUTOMATISK_BESTEBEREGNES.contains(a.getOpptjeningAktivitetType()));
        if (kanAutomatiskBesteberegnes && !harVLSPYtelseSiste10Måneder(input)) {
            return Optional.empty();
        }
        var harKunYtelse = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow(() -> new IllegalArgumentException("Skal ha beregningsgrunnlag"))
                .getAktivitetStatuser()
                .stream()
                .anyMatch(s -> AktivitetStatus.KUN_YTELSE.equals(s.getAktivitetStatus()));
        return ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).isKvalifisererTilBesteberegning() && !harKunYtelse && !harFjernetDagpenger(beregningsgrunnlagGrunnlag) ?
                Optional.of(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING) : Optional.empty();
    }

    private boolean harVLSPYtelseSiste10Måneder(FaktaOmBeregningInput input) {
        if (input.getIayGrunnlag() == null) {
            return false;
        }
        var sisteTiHeleMåneder = input.getSkjæringstidspunktForBeregning().minusMonths(11).withDayOfMonth(1);
        var periode = Intervall.fraOgMedTilOgMed(sisteTiHeleMåneder, input.getSkjæringstidspunktForBeregning());
        var harVlspYtelse = input.getIayGrunnlag().getAktørYtelseFraRegister()
                .map(aktørYtelse -> aktørYtelse.getAlleYtelser().stream()
                        .filter(y -> YtelseKilde.VLSP.equals(y.getYtelseKilde()))
                        .anyMatch(y -> periode.overlapper(y.getPeriode())))
                .orElse(false);
        var harSPInntekt = input.getIayGrunnlag().getAktørInntektFraRegister()
                .map(aktørInntekt -> aktørInntekt.getInntekt().stream()
                        .flatMap(inntekt -> inntekt.getAlleInntektsposter().stream())
                        .filter(ip -> InntektYtelseType.SYKEPENGER.equals(ip.getInntektYtelseType()))
                        .anyMatch(ip -> periode.overlapper(ip.getPeriode())))
                .orElse(false);
        return harVlspYtelse && harSPInntekt;
    }

    private boolean harFjernetDagpenger(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        var saksbehandletAktiviteter = beregningsgrunnlagGrunnlag.getSaksbehandletAktiviteter();
        if (saksbehandletAktiviteter.isEmpty()) {
            return false;
        }
        var harDagpengerIRegister = beregningsgrunnlagGrunnlag.getRegisterAktiviteter().getBeregningAktiviteter().stream()
                .anyMatch(ba -> ba.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.DAGPENGER));
        var harIkkeDagpengerISaksbehandlet = saksbehandletAktiviteter.get().getBeregningAktiviteter().stream()
                .noneMatch(ba -> ba.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.DAGPENGER));
        return harDagpengerIRegister && harIkkeDagpengerISaksbehandlet;
    }

}
