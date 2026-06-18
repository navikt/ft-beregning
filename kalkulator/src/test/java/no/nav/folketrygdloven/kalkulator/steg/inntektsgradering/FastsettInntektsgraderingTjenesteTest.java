package no.nav.folketrygdloven.kalkulator.steg.inntektsgradering;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;

import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

import no.nav.folketrygdloven.kalkulus.typer.AktørId;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

class FastsettInntektsgraderingTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2024, 1, 1);
    private static final Intervall PERIODE = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(10));
    private static final Beløp GRUNNBELØP = Beløp.fra(100_000);
    private static final String ARBEIDSGIVER_ORGNR = "123456789";
    private static final String ARBEIDSGIVER_ORGNR2 = "987654321";
    private static final String ARBEIDSGIVER_ORGNR3 = "192837465";

    @Test
    void skal_sette_tilkommet_inntekt_for_arbeidstaker_og_ignorere_tilkommet_inntekt_som_ikke_skal_redusere_utbetaling() {
        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var arbeidsgiver3 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR3);
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = List.of(
            new UtbetalingsgradPrAktivitetDto(new AktivitetDto(arbeidsgiver, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                List.of(new PeriodeMedUtbetalingsgradDto(PERIODE, Utbetalingsgrad.valueOf(100), Aktivitetsgrad.fra(0)))),
            new UtbetalingsgradPrAktivitetDto(new AktivitetDto(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                List.of(new PeriodeMedUtbetalingsgradDto(PERIODE, Utbetalingsgrad.valueOf(0), Aktivitetsgrad.fra(50)))));
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(utbetalingsgrader, PERIODE.getFomDato());

        var beregningsgrunnlag = lagBeregningsgrunnlag(
            List.of(AktivitetStatus.ARBEIDSTAKER),
            List.of(lagArbeidstakerAndel(1L, arbeidsgiver, 100_000)),
            List.of(
                new TilkommetInntektDto(AktivitetStatus.ARBEIDSTAKER, arbeidsgiver3, InternArbeidsforholdRefDto.nullRef(), Beløp.fra(25_000), null, false),
                new TilkommetInntektDto(AktivitetStatus.ARBEIDSTAKER, arbeidsgiver2, InternArbeidsforholdRefDto.nullRef(), Beløp.fra(100_000), null, true)
            )
        );

        var resultat = fastsettInntektsgradering(beregningsgrunnlag, ytelsespesifiktGrunnlag);

        var periodeResultat = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        assertThat(periodeResultat.getTilkomneInntekter()).hasSize(2);
        var tilkommetInntekt = periodeResultat.getTilkomneInntekter().stream().filter(it -> it.getArbeidsgiver().get().equals(arbeidsgiver2)).findFirst().orElseThrow();
        assertThat(tilkommetInntekt.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(tilkommetInntekt.getTilkommetInntektPrÅr()).isEqualByComparingTo(Beløp.fra(50_000));
        assertThat(periodeResultat.getInntektgraderingsprosentBrutto()).isEqualByComparingTo("50");
    }

    private static BeregningsgrunnlagRegelResultat fastsettInntektsgradering(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                            YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        KoblingReferanse koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.PLEIEPENGER_SYKT_BARN, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(),  Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT).build());
        var input = new BeregningsgrunnlagInput(koblingReferanse, null, null, List.of(), ytelsespesifiktGrunnlag);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(BeregningsgrunnlagTilstand.FASTSATT_INN);
        return FastsettInntektsgraderingTjeneste.fastsettInntektsgradering(input.medBeregningsgrunnlagGrunnlag(grunnlag));
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlag(List<AktivitetStatus> statuser,
                                                               List<BeregningsgrunnlagPrStatusOgAndelDto.Builder> andeler,
                                                               List<TilkommetInntektDto> tilkomneInntekter) {
        var beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP);

        statuser.forEach(status -> beregningsgrunnlagBuilder.leggTilAktivitetStatus(
            BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(status)));

        var periodeBuilder = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(PERIODE.getFomDato(), PERIODE.getTomDato());
        tilkomneInntekter.forEach(periodeBuilder::leggTilTilkommetInntekt);
        andeler.forEach(periodeBuilder::leggTilBeregningsgrunnlagPrStatusOgAndel);

        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(periodeBuilder);
        return beregningsgrunnlagBuilder.build();
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder lagArbeidstakerAndel(Long andelsnr,
                                                                                     Arbeidsgiver arbeidsgiver,
                                                                                     int bruttoPrÅr) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(andelsnr)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(PERIODE)
            .medBeregnetPrÅr(Beløp.fra(bruttoPrÅr))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef()));
    }

}










