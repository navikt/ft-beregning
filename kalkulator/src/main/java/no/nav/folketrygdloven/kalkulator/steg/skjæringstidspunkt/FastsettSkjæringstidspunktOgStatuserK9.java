package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL.mapForSkjæringstidspunktOgStatuser;
import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellK9;

public class FastsettSkjæringstidspunktOgStatuserK9 {

    public static BeregningsgrunnlagRegelResultat fastsett(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktivitetAggregat, List<Grunnbeløp> grunnbeløpSatser) {
        AktivitetStatusModell regelmodell = MapBGStatuserFraVLTilRegel.map(beregningAktivitetAggregat);

        MidlertidigInaktivType midlertidigInaktivType = null;
        if (input.getOpptjeningAktiviteter().erMidlertidigInaktiv()) {
            var midlerTidigInaktivInput = input.getOpptjeningAktiviteter().getMidlertidigInaktivType();
            if (midlerTidigInaktivInput.name().equals(MidlertidigInaktivType.A.name())) {
                List<AktivPeriode> aktiviteterPåStp = regelmodell.getAktivePerioder().stream().filter(aktivPeriode -> aktivPeriode.getPeriode().inneholder(input.getSkjæringstidspunktOpptjening())).collect(Collectors.toList());
                if (!aktiviteterPåStp.isEmpty()) {
                    throw new IllegalArgumentException("Skjæringstidspunktet kan ikke overlappe med aktive perioder for midlertidig inaktiv 8-47-A for: " + aktiviteterPåStp);
                }
            }
            midlertidigInaktivType = MidlertidigInaktivType.valueOf(midlerTidigInaktivInput.name());
        }

        AktivitetStatusModellK9 k9Modell = new AktivitetStatusModellK9(midlertidigInaktivType, regelmodell);

        RegelResultat regelResultatFastsettSkjæringstidspunkt = fastsettSkjæringstidspunkt(k9Modell);
        RegelResultat regelResultatFastsettStatus = fastsettStatus(k9Modell);

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation)
        List<RegelResultat> regelResultater = List.of(
                regelResultatFastsettSkjæringstidspunkt,
                regelResultatFastsettStatus);
        var nyttBeregningsgrunnlag = mapForSkjæringstidspunktOgStatuser(input.getKoblingReferanse(), k9Modell, regelResultater, input.getIayGrunnlag(), grunnbeløpSatser);
        var fastsattBeregningsperiode = FastsettBeregningsperiodeTjeneste.fastsettBeregningsperiode(nyttBeregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());

        return new BeregningsgrunnlagRegelResultat(fastsattBeregningsperiode,
                new RegelSporingAggregat(
                        mapRegelSporingGrunnlag(regelResultatFastsettSkjæringstidspunkt, BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT),
                        mapRegelSporingGrunnlag(regelResultatFastsettStatus, BeregningsgrunnlagRegelType.BRUKERS_STATUS)));
    }

    private static RegelResultat fastsettSkjæringstidspunkt(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        return KalkulusRegler.fastsettSkjæringstidspunktK9(regelmodell);
    }

    private static RegelResultat fastsettStatus(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        return KalkulusRegler.fastsettStatusVedSkjæringstidspunkt(regelmodell);
    }

}
