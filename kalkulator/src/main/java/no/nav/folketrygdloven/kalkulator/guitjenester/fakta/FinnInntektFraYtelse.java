package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;


class FinnInntektFraYtelse {

	private static final BigDecimal VIRKEDAGER_I_1_ÅR = KonfigTjeneste.getYtelsesdagerIÅr();

	private FinnInntektFraYtelse() {
		// Skjul konstruktør
	}

	static Optional<Beløp> finnÅrbeløpFraMeldekortForAndel(KoblingReferanse ref,
	                                                       BeregningsgrunnlagPrStatusOgAndelDto andel,
	                                                       YtelseFilterDto ytelseFilter) {
        var skjæringstidspunkt = ref.getSkjæringstidspunktBeregning();
		if (ytelseFilter.isEmpty()) {
			return Optional.empty();
		}

        var nyesteVedtak = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(mapTilYtelseType(andel.getAktivitetStatus())));
		if (nyesteVedtak.isEmpty()) {
			return Optional.empty();
		}

        var nyesteMeldekort = MeldekortUtils.sisteHeleMeldekortFørStp(ytelseFilter, nyesteVedtak.get(),
				skjæringstidspunkt,
				Set.of(mapTilYtelseType(andel.getAktivitetStatus()))
		);

		// Hvis søker kun har status DP / AAP tar ikke beregning hensyn til utbetalingsfaktor
        var antallUnikeStatuserIPeriode = andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPrStatusOgAndelList().stream()
				.map(BeregningsgrunnlagPrStatusOgAndelDto::getAktivitetStatus)
				.collect(Collectors.toSet())
				.size();

		if (antallUnikeStatuserIPeriode > 1) {
			return Optional.of(finnÅrsbeløpMedHensynTilUtbetalingsfaktor(nyesteVedtak.get(), nyesteMeldekort));
		} else {
			return Optional.of(finnÅrsbeløp(nyesteVedtak.get(), nyesteMeldekort));
		}

	}

	static Optional<Beløp> finnÅrbeløpForDagpenger(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
	                                               YtelseFilterDto ytelseFilter) {
		return finnÅrbeløpFraMeldekortForAndel(ref, andel, ytelseFilter);
	}


	private static YtelseType mapTilYtelseType(AktivitetStatus aktivitetStatus) {
		if (AktivitetStatus.DAGPENGER.equals(aktivitetStatus)) {
			return YtelseType.DAGPENGER;
		}
		if (AktivitetStatus.ARBEIDSAVKLARINGSPENGER.equals(aktivitetStatus)) {
			return YtelseType.ARBEIDSAVKLARINGSPENGER;
		}
		return YtelseType.UDEFINERT;
	}

	private static Beløp finnÅrsbeløpMedHensynTilUtbetalingsfaktor(YtelseDto ytelse, Optional<YtelseAnvistDto> ytelseAnvist) {
		var årsbeløpUtenHensynTilUtbetalingsfaktor = finnÅrsbeløp(ytelse, ytelseAnvist);

		var utbetalingsfaktor = ytelseAnvist
				.flatMap(YtelseAnvistDto::getUtbetalingsgradProsent)
				.map(verdi -> ytelse.harKildeKelvin() ? verdi.tilNormalisertGrad(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_KELVIN) : verdi.tilNormalisertGrad(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG_ARENA))
				.orElse(BigDecimal.ONE);

		return årsbeløpUtenHensynTilUtbetalingsfaktor.multipliser(utbetalingsfaktor);
	}

	private static Beløp finnÅrsbeløp(YtelseDto ytelse, Optional<YtelseAnvistDto> ytelseAnvist) {
		var dagsats = ytelse.getVedtaksDagsats()
				.orElse(ytelseAnvist.flatMap(YtelseAnvistDto::getDagsats).orElse(Beløp.ZERO));
		return dagsats.multipliser(VIRKEDAGER_I_1_ÅR);
	}


}
