package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

public class MapFullføreBeregningsgrunnlagFraVLTilRegelTest {

	private MapFullføreBeregningsgrunnlagFraVLTilRegel mapFullføreBeregningsgrunnlagFraVLTilRegel =
			new MapFullføreBeregningsgrunnlagFraVLTilRegel();

	private static final List<FaktaOmBeregningTilfelle> FAKTA_OM_BEREGNING_TILFELLER = Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL);
	private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
	private final Beløp GRUNNBELØP = Beløp.fra(600000);

	private final LocalDate fom = SKJÆRINGSTIDSPUNKT;
	private final LocalDate tom = SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1);

	private final Intervall delvisAktivPeriode = Intervall.fraOgMedTilOgMed(fom, tom.minusDays(10));
	private final Intervall aktivitetBortfaltPeriode = Intervall.fraOgMedTilOgMed(tom.minusDays(9), tom);
	private final Intervall aktivitetEtterSøknadPeriode = Intervall.fraOgMedTilOgMed(tom.plusDays(1), TIDENES_ENDE);


	@Test
	public void aktivitetsgrad_skal_mappes_riktig_for_forskjellig_sn_status() {
		// Arrange
        var input = lagBeregningsgrunnlagInput(delvisAktivPeriode, aktivitetBortfaltPeriode, aktivitetEtterSøknadPeriode)
				.medBeregningsgrunnlagGrunnlag(lagBeregningsgrunnlagDto());
        var beregningsgrunnlag = lagBeregningsgrunnlagMedSelvstendigNæringsdrivende();

		// Act
		var samletBeregningsgrunnlag = mapFullføreBeregningsgrunnlagFraVLTilRegel.map(input, beregningsgrunnlag);

		// Assert
		var beregningsGrunnlagPerioder = samletBeregningsgrunnlag.getBeregningsgrunnlagPerioder().iterator();
		assertEquals(hentNesteAktivitetsgrad(beregningsGrunnlagPerioder), BigDecimal.valueOf(50));
		assertEquals(hentNesteAktivitetsgrad(beregningsGrunnlagPerioder), BigDecimal.ZERO);
		assertEquals(hentNesteAktivitetsgrad(beregningsGrunnlagPerioder), BigDecimal.valueOf(100));
	}

	private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlagDto() {
		var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
				.medGrunnbeløp(GRUNNBELØP)
				.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
				.leggTilFaktaOmBeregningTilfeller(FAKTA_OM_BEREGNING_TILFELLER)
				.build();
        var periode1 = BeregningsgrunnlagPeriodeDto.ny()
				.medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, tom)
				.build(beregningsgrunnlag);
		@SuppressWarnings("unused")
		var frilansAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
				.medAndelsnr(1L)
				.medInntektskategori(Inntektskategori.FRILANSER)
				.medAktivitetStatus(AktivitetStatus.FRILANSER)
				.build(periode1);
		return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()).medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
	}

	private BeregningsgrunnlagInput lagBeregningsgrunnlagInput(Intervall delvisAktivPeriode, Intervall aktivitetBortfaltPeriode, Intervall aktivitetEtterSøknadPeriode) {
		var utbetalingsgrad1 = new UtbetalingsgradPrAktivitetDto(new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE),
				List.of(new PeriodeMedUtbetalingsgradDto(delvisAktivPeriode, null, Aktivitetsgrad.fra(50))));
		var utbetalingsgrad2 = new UtbetalingsgradPrAktivitetDto(new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE_IKKE_AKTIV),
				List.of(new PeriodeMedUtbetalingsgradDto(aktivitetBortfaltPeriode, null, Aktivitetsgrad.fra(0))));
		var utbetalingsgrad3 = new UtbetalingsgradPrAktivitetDto(new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE_IKKE_AKTIV),
				List.of(new PeriodeMedUtbetalingsgradDto(aktivitetEtterSøknadPeriode, null, Aktivitetsgrad.fra(100))));
        var ytelsesspesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgrad1, utbetalingsgrad2, utbetalingsgrad3));
		return new BeregningsgrunnlagInput(new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT), null, null, List.of(), ytelsesspesifiktGrunnlag);
	}

	private BigDecimal hentNesteAktivitetsgrad(Iterator<BeregningsgrunnlagPeriode> beregningsGrunnlagPerioder) {
		var beregningsperiode = beregningsGrunnlagPerioder.next();
		return beregningsperiode.getBeregningsgrunnlagPrStatus().stream().findAny().get().getAktivitetsgrad().get();
	}

	private BeregningsgrunnlagDto lagBeregningsgrunnlagMedSelvstendigNæringsdrivende() {
        var bg = BeregningsgrunnlagDto.builder()
				.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
				.medGrunnbeløp(GRUNNBELØP)
				.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
				.build();

		var bgp1 = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(delvisAktivPeriode.getFomDato(), delvisAktivPeriode.getTomDato()).medBruttoPrÅr(new Beløp(BigDecimal.valueOf(233315.47))).build(bg);
		var bgp2 = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(aktivitetBortfaltPeriode.getFomDato(), aktivitetBortfaltPeriode.getTomDato()).medBruttoPrÅr(new Beløp(BigDecimal.valueOf(233315.47))).build(bg);
		var bgp3 = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(aktivitetEtterSøknadPeriode.getFomDato(), TIDENES_ENDE).medBruttoPrÅr(new Beløp(BigDecimal.valueOf(233315.47))).build(bg);

		beregningsgrunnlagPerStatusOgAndel(bgp1, bg);
		beregningsgrunnlagPerStatusOgAndel(bgp2, bg);
		beregningsgrunnlagPerStatusOgAndel(bgp3, bg);

		return bg;
	}

	private static void beregningsgrunnlagPerStatusOgAndel(BeregningsgrunnlagPeriodeDto bgp, BeregningsgrunnlagDto bg) {
		BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
				.medAndelsnr(1L)
				.medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
				.medBeregnetPrÅr(Beløp.fra(100_000))
				.medRedusertPrÅr(Beløp.ZERO)
				.medRedusertBrukersAndelPrÅr(Beløp.ZERO)
				.medRedusertRefusjonPrÅr(Beløp.ZERO)
				.medAvkortetPrÅr(Beløp.ZERO)
				.build(bgp);
		bg.leggTilBeregningsgrunnlagPeriode(bgp);
	}
}
