package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL.mapForSkjæringstidspunktOgStatuser;

import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFRISINN;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse.FrisinnGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.AvklaringsbehovUtlederForeslåBeregning;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;

public class FastsettSkjæringstidspunktOgStatuserFRISINN {

    public static BeregningsgrunnlagRegelResultat fastsett(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktivitetAggregat, List<Grunnbeløp> grunnbeløpSatser) {
        AktivitetStatusModell regelmodell = MapBGStatuserFraVLTilRegel.map(beregningAktivitetAggregat);
        RegelResultat regelResultatFastsettSkjæringstidspunkt = fastsettSkjæringstidspunkt(input, regelmodell);
        if (regelmodell.getSkjæringstidspunktForBeregning() == null) {
            return new BeregningsgrunnlagRegelResultat(null, AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, List.of(regelResultatFastsettSkjæringstidspunkt)));
        }
        RegelResultat regelResultatFastsettStatus = fastsettStatus(input, regelmodell);
        if (regelmodell.getBeregningsgrunnlagPrStatusListe() == null || regelmodell.getBeregningsgrunnlagPrStatusListe().isEmpty()) {
            return new BeregningsgrunnlagRegelResultat(null, List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.INGEN_AKTIVITETER)));
        }

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation)
        List<RegelResultat> regelResultater = List.of(
                regelResultatFastsettSkjæringstidspunkt,
                regelResultatFastsettStatus);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = mapForSkjæringstidspunktOgStatuser(input.getKoblingReferanse(), regelmodell, regelResultater, input.getIayGrunnlag(), grunnbeløpSatser);
        var fastsattBeregningsperiode = FastsettBeregningsperiodeTjenesteFRISINN.fastsettBeregningsperiode(nyttBeregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
        return new BeregningsgrunnlagRegelResultat(fastsattBeregningsperiode, Collections.emptyList());

    }

    private static RegelResultat fastsettSkjæringstidspunkt(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        var inntektsgrunnlagMapper = new MapInntektsgrunnlagVLTilRegelFRISINN();
        Inntektsgrunnlag inntektsgrunnlag = inntektsgrunnlagMapper.map(input, regelmodell.getSkjæringstidspunktForOpptjening());
        AktivitetStatusModellFRISINN aktivitetStatusModellFRISINN = new AktivitetStatusModellFRISINN(inntektsgrunnlag,
                regelmodell,
                FrisinnGrunnlagMapper.mapFrisinnPerioder(input));
        aktivitetStatusModellFRISINN.setInntektsgrunnlag(inntektsgrunnlag);
        var resultat= KalkulusRegler.fastsettSkjæringstidspunktFRISINN(aktivitetStatusModellFRISINN);
        regelmodell.setSkjæringstidspunktForBeregning(aktivitetStatusModellFRISINN.getSkjæringstidspunktForBeregning());
        return resultat;
    }

    private static RegelResultat fastsettStatus(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        var inntektsgrunnlagMapper = new MapInntektsgrunnlagVLTilRegelFRISINN();
        Inntektsgrunnlag inntektsgrunnlag = inntektsgrunnlagMapper.map(input, regelmodell.getSkjæringstidspunktForOpptjening());
        AktivitetStatusModellFRISINN aktivitetStatusModellFRISINN = new AktivitetStatusModellFRISINN(inntektsgrunnlag,
                regelmodell,
                FrisinnGrunnlagMapper.mapFrisinnPerioder(input));
        aktivitetStatusModellFRISINN.setInntektsgrunnlag(inntektsgrunnlag);
        return KalkulusRegler.fastsettStatusVedSkjæringstidspunktFRISINN(aktivitetStatusModellFRISINN);
    }


}
