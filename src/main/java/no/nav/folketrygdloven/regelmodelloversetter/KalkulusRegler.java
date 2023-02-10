package no.nav.folketrygdloven.regelmodelloversetter;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.RegelFordelBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn.RegelForeslåBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå.RegelFortsettForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering.FastsettPerioderGraderingRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse.FastsettPerioderNaturalytelseRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon.FastsettPerioderRefusjonRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad.FastsettPerioderForUtbetalingsgradRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeModellNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.vurder.RegelVurderBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn.RegelVurderBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn.RegelFinnGrenseverdiFRISINN;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn.RegelFullføreBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp.RegelFinnGrenseverdi;
import no.nav.folketrygdloven.besteberegning.RegelForeslåBesteberegning;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.RegelFastsettSkjæringstidspunkt;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.RegelFastsettSkjæringstidspunktFrisinn;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.k9.RegelFastsettSkjæringstidspunktK9;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.status.RegelFastsettStatusVedSkjæringstidspunkt;
import no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn.RegelFastsettStatusVedSkjæringstidspunktFRISINN;

/**
 * Samling av regler som kalles fra kalkulus - slik at de er enkle å finne tilbake til
 */
public final class KalkulusRegler {

	private KalkulusRegler() {
	}

	public static RegelResultat RegelFastsettSkjæringstidspunkt(AktivitetStatusModell input) {
		return new RegelFastsettSkjæringstidspunkt().evaluerRegel(input);
	}

	public static RegelResultat RegelFastsettSkjæringstidspunktK9(AktivitetStatusModell input) {
		return new RegelFastsettSkjæringstidspunktK9().evaluerRegel(input);
	}

	public static RegelResultat RegelFastsettStatusVedSkjæringstidspunkt(AktivitetStatusModell input) {
		return new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(input);
	}

	public static RegelResultat RegelForeslåBeregningsgrunnlag(BeregningsgrunnlagPeriode input) {
		return new RegelForeslåBeregningsgrunnlag(input).evaluerRegel(input);
	}

	public static RegelResultat RegelFortsettForeslåBeregningsgrunnlag(BeregningsgrunnlagPeriode input) {
		return new RegelFortsettForeslåBeregningsgrunnlag(input).evaluerRegel(input);
	}


	public static RegelResultat RegelFordelBeregningsgrunnlag(FordelPeriodeModell input, Object perioder) {
		return new RegelFordelBeregningsgrunnlag().evaluerRegel(input, perioder);
	}

	public static RegelResultat FastsettPerioderGraderingRegel(PeriodeModellGradering input, Object perioder) {
		return new FastsettPerioderGraderingRegel().evaluerRegel(input, perioder);
	}

	public static RegelResultat FastsettPerioderNaturalytelseRegel(PeriodeModellNaturalytelse input, Object perioder) {
		return new FastsettPerioderNaturalytelseRegel().evaluerRegel(input, perioder);
	}

	public static RegelResultat FastsettPerioderRefusjonRegel(PeriodeModellRefusjon input, Object perioder) {
		return new FastsettPerioderRefusjonRegel().evaluerRegel(input, perioder);
	}

	public static RegelResultat FastsettPerioderForUtbetalingsgradRegel(PeriodeModellUtbetalingsgrad input, Object perioder) {
		return new FastsettPerioderForUtbetalingsgradRegel().evaluerRegel(input, perioder);
	}

	public static RegelResultat RegelVurderBeregningsgrunnlag(BeregningsgrunnlagPeriode input) {
		return new RegelVurderBeregningsgrunnlag().evaluerRegel(input);
	}

	public static RegelResultat RegelFinnGrenseverdi(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode input) {
		return new RegelFinnGrenseverdi(input).evaluerRegel(input);
	}



	public static RegelResultat RegelFullføreBeregningsgrunnla(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode input) {
		return new RegelFullføreBeregningsgrunnlag(input).evaluerRegel(input);
	}

	/**
	 * FORELDREPENGER
	 */
	public static RegelResultat RegelForeslåBesteberegning(BesteberegningRegelmodell input) {
		return new RegelForeslåBesteberegning().evaluerRegel(input);
	}

	/**
	 * FRISINN
	 */
	public static RegelResultat regelFastsettSkjæringstidspunktFrisinn(AktivitetStatusModellFRISINN input) {
		return new RegelFastsettSkjæringstidspunktFrisinn().evaluerRegel(input);
	}

	public static RegelResultat regelFastsettStatusVedSkjæringstidspunktFRISINN(AktivitetStatusModell input) {
		return new RegelFastsettStatusVedSkjæringstidspunktFRISINN().evaluerRegel(input);
	}

	public static RegelResultat regelForeslåBeregningsgrunnlagFRISINN(BeregningsgrunnlagPeriode input) {
		return new RegelForeslåBeregningsgrunnlagFRISINN(input).evaluerRegel(input);
	}

	public static RegelResultat regelVurderBeregningsgrunnlagFRISINN(BeregningsgrunnlagPeriode input) {
		return new RegelVurderBeregningsgrunnlagFRISINN().evaluerRegel(input);
	}

	public static RegelResultat regelFinnGrenseverdiFRISINN(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode input) {
		return new RegelFinnGrenseverdiFRISINN().evaluerRegel(input);
	}

	public static RegelResultat regelFullføreBeregningsgrunnlagFRISINN(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode input) {
		return new RegelFullføreBeregningsgrunnlagFRISINN().evaluerRegel(input);
	}

}
