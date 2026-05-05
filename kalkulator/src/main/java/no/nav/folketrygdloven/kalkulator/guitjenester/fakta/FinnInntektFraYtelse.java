package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
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

        var nyesteMeldekortUtbetalingsgrad = MeldekortUtils.snittUtbetalingsgradSistePeriodeFørStp(ytelseFilter, nyesteVedtak.get(),
				skjæringstidspunkt,
				Set.of(mapTilYtelseType(andel.getAktivitetStatus()))
		);

		// Hvis søker kun har status DP / AAP tar ikke beregning hensyn til utbetalingsfaktor
        var antallUnikeStatuserIPeriode = andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPrStatusOgAndelList().stream()
				.map(BeregningsgrunnlagPrStatusOgAndelDto::getAktivitetStatus)
				.collect(Collectors.toSet())
				.size();

        var dagsats = nyesteVedtak.flatMap(YtelseDto::getVedtaksDagsats)
            .or(() -> MeldekortUtils.sisteAnvisteDagsatsFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(mapTilYtelseType(andel.getAktivitetStatus()))))
            .orElse(Beløp.ZERO);
        var årsbeløp = finnÅrsbeløp(dagsats);
		if (antallUnikeStatuserIPeriode > 1) {
			return nyesteMeldekortUtbetalingsgrad.map(årsbeløp::multipliser)
                .or(() -> Optional.of(årsbeløp));
		} else {
			return Optional.of(årsbeløp);
		}

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

	private static Beløp finnÅrsbeløp(Beløp dagsats) {
		return dagsats.multipliser(VIRKEDAGER_I_1_ÅR);
	}


}
