package no.nav.folketrygdloven.kalkulator.adapter;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnAktivitetsgradForAndel;
import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Avgjør om  det er søkt ytelse for aktivitet.
 */
public class ErSøktYtelseFor {

	public static boolean erSøktYtelseFor(BeregningsgrunnlagPrStatusOgAndelDto bgAndel, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, boolean skalIgnorereIkkeYrkesaktivStatus) {
		return harOverNullProsentUtbetalingsgrad(finnUtbetalingsgradForAndel(bgAndel, periode, ytelsespesifiktGrunnlag, skalIgnorereIkkeYrkesaktivStatus)) ||
				harMindreEnnHundreProsentAktivitetsgrad(finnAktivitetsgradForAndel(bgAndel, periode, ytelsespesifiktGrunnlag, skalIgnorereIkkeYrkesaktivStatus));
	}

	public static boolean erSøktYtelseFor(PeriodeMedUtbetalingsgradDto p) {
		return harOverNullProsentUtbetalingsgrad(p.getUtbetalingsgrad()) || harMindreEnnHundreProsentAktivitetsgrad(p.getAktivitetsgrad());
	}

	private static boolean harOverNullProsentUtbetalingsgrad(Utbetalingsgrad utbetalingsgrad) {
		return utbetalingsgrad != null && utbetalingsgrad.compareTo(Utbetalingsgrad.ZERO) > 0;
	}

	private static boolean harMindreEnnHundreProsentAktivitetsgrad(Optional<Aktivitetsgrad> aktivitetsgrad) {
		return aktivitetsgrad.map(ag -> ag.compareTo(Aktivitetsgrad.HUNDRE) < 0).orElse(false);
	}

}
