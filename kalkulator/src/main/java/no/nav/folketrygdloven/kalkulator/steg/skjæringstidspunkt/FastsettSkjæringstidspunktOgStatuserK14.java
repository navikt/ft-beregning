package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL.mapForSkjæringstidspunktOgStatuser;
import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class FastsettSkjæringstidspunktOgStatuserK14 {

    public static BeregningsgrunnlagRegelResultat fastsett(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktivitetAggregat, List<Grunnbeløp> grunnbeløpSatser) {
        var regelmodell = MapBGStatuserFraVLTilRegel.map(beregningAktivitetAggregat, getInntektsgrunnlag(input));
		input.getToggles().forEach(regelmodell::leggTilToggle);
        var regelResultatFastsettSkjæringstidspunkt = fastsettSkjæringstidspunkt(regelmodell);
        var regelResultatFastsettStatus = fastsettStatus(regelmodell);

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation.)
        var regelResultater = List.of(
                regelResultatFastsettSkjæringstidspunkt,
                regelResultatFastsettStatus);
        var nyttBeregningsgrunnlag = mapForSkjæringstidspunktOgStatuser(input.getKoblingReferanse(), regelmodell, regelResultater, input.getIayGrunnlag(), grunnbeløpSatser);
        var fastsattBeregningsperiode = FagsakYtelseType.FORELDREPENGER.equals(input.getFagsakYtelseType())
                ? FastsettBeregningsperiodeTjenesteFP.fastsettBeregningsperiode(nyttBeregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger())
                : FastsettBeregningsperiodeTjeneste.fastsettBeregningsperiode(nyttBeregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
        return new BeregningsgrunnlagRegelResultat(fastsattBeregningsperiode,
                new RegelSporingAggregat(
                        mapRegelSporingGrunnlag(regelResultatFastsettSkjæringstidspunkt, BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT),
                        mapRegelSporingGrunnlag(regelResultatFastsettStatus, BeregningsgrunnlagRegelType.BRUKERS_STATUS)));
    }

	private static Inntektsgrunnlag getInntektsgrunnlag(BeregningsgrunnlagInput input) {
		if (input.isEnabled("aap.praksisendring", false)) {
			var inntektsgrunnlagMapper = new MapInntektsgrunnlagVLTilRegelFelles();
			return inntektsgrunnlagMapper.map(input, input.getSkjæringstidspunktForBeregning());
		}
		return null;
	}

	private static RegelResultat fastsettSkjæringstidspunkt(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        return KalkulusRegler.fastsettSkjæringstidspunkt(regelmodell);
    }

    private static RegelResultat fastsettStatus(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        return KalkulusRegler.fastsettStatusVedSkjæringstidspunkt(regelmodell);
    }

}
