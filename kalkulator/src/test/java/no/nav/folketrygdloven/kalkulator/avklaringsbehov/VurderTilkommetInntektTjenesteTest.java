package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.NyttInntektsforholdDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderTilkommetInntektHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderTilkomneInntektsforholdPeriodeDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

class VurderTilkommetInntektTjenesteTest {

    public static final String ORGNR = "974652269";
    public static final String ORGNR2 = "974652269";
    private static final LocalDate STP = LocalDate.now();

    @Test
    void skal_sette_verdier_for_vurdert_innteksforhold() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var yrkesaktiviteter = List.of(yrkesaktivitet, nyYrkesaktivitet);
        var utbetalingsgrader = List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel);
        var andeler = List.of(arbeidstakerandelFraStart, nyAndel);

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));

        var vurderteInntektsforhold = List.of(lagNyttInntektsforhold(ORGNR2, true, 100_000));

        var vudertPeride = new VurderTilkomneInntektsforholdPeriodeDto(vurderteInntektsforhold, periode.getFomDato(), periode.getTomDato());

        VurderTilkommetInntektHåndteringDto dto = new VurderTilkommetInntektHåndteringDto(List.of(vudertPeride));

        BeregningsgrunnlagInput input = lagInput(periode, yrkesaktiviteter, utbetalingsgrader, andeler, vurderteInntektsforhold);

        // Act
        var nyttGr = VurderTilkommetInntektTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));

        var tilkomneInntekter = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0)
                .getTilkomneInntekter();

        // Assert
        assertThat(tilkomneInntekter.size()).isEqualTo(1);
        assertThat(tilkomneInntekter.get(0).getArbeidsgiver().get()).isEqualTo(arbeidsgiver2);
        assertThat(tilkomneInntekter.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(tilkomneInntekter.get(0).getBruttoInntektPrÅr().compareTo(Beløp.fra(100_000))).isEqualTo(0);


    }

    @Test
    void skal_splitte_periode() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var yrkesaktiviteter = List.of(yrkesaktivitet, nyYrkesaktivitet);
        var utbetalingsgrader = List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel);
        var andeler = List.of(arbeidstakerandelFraStart, nyAndel);

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(20));

        var vurderteInntektsforhold = List.of(lagNyttInntektsforhold(ORGNR2, true, 100_000));

        var vurdertPeriode1 = new VurderTilkomneInntektsforholdPeriodeDto(vurderteInntektsforhold, periode.getFomDato(), periode.getTomDato().minusDays(3));

        var vurderteInntektsforhold2 = List.of(lagNyttInntektsforhold(ORGNR2, false, null));

        var vurdertPeriode2 = new VurderTilkomneInntektsforholdPeriodeDto(vurderteInntektsforhold2, periode.getTomDato().minusDays(2), periode.getTomDato());

        VurderTilkommetInntektHåndteringDto dto = new VurderTilkommetInntektHåndteringDto(List.of(vurdertPeriode1, vurdertPeriode2));

        BeregningsgrunnlagInput input = lagInput(periode, yrkesaktiviteter, utbetalingsgrader, andeler, vurderteInntektsforhold);

        // Act
        var nyttGr = VurderTilkommetInntektTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));


        // Assert
        assertThat(nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        var tilkomneInntekter1 = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0)
                .getTilkomneInntekter();
        assertThat(tilkomneInntekter1.size()).isEqualTo(1);
        assertThat(tilkomneInntekter1.get(0).getArbeidsgiver().get()).isEqualTo(arbeidsgiver2);
        assertThat(tilkomneInntekter1.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(tilkomneInntekter1.get(0).getBruttoInntektPrÅr().compareTo(Beløp.fra(100_000))).isEqualTo(0);

        var periodeLagtTil = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(1);
        var tilkomneInntekter2 = periodeLagtTil
                .getTilkomneInntekter();
        assertThat(periodeLagtTil.getPeriodeÅrsaker().contains(PeriodeÅrsak.TILKOMMET_INNTEKT_MANUELT)).isTrue();
        assertThat(periodeLagtTil.getPeriodeÅrsaker().contains(PeriodeÅrsak.TILKOMMET_INNTEKT)).isTrue();
        assertThat(tilkomneInntekter2.size()).isEqualTo(1);
        assertThat(tilkomneInntekter2.get(0).getArbeidsgiver().get()).isEqualTo(arbeidsgiver2);
        assertThat(tilkomneInntekter2.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(tilkomneInntekter2.get(0).skalRedusereUtbetaling()).isFalse();
    }



    @Test
    void skal_splitte_periode_og_ikke_komprimere_like() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var yrkesaktiviteter = List.of(yrkesaktivitet, nyYrkesaktivitet);
        var utbetalingsgrader = List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel);
        var andeler = List.of(arbeidstakerandelFraStart, nyAndel);

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(20));

        var vurderteInntektsforhold = List.of(lagNyttInntektsforhold(ORGNR2, true, 100_000));

        var vurdertPeriode1 = new VurderTilkomneInntektsforholdPeriodeDto(vurderteInntektsforhold, periode.getFomDato(), periode.getTomDato().minusDays(3));

        var vurdertPeriode2 = new VurderTilkomneInntektsforholdPeriodeDto(vurderteInntektsforhold, periode.getTomDato().minusDays(2), periode.getTomDato());

        VurderTilkommetInntektHåndteringDto dto = new VurderTilkommetInntektHåndteringDto(List.of(vurdertPeriode1, vurdertPeriode2));

        BeregningsgrunnlagInput input = lagInput(periode, yrkesaktiviteter, utbetalingsgrader, andeler, vurderteInntektsforhold);

        // Act
        var nyttGr = VurderTilkommetInntektTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));


        // Assert
        assertThat(nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        var tilkomneInntekter1 = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0)
                .getTilkomneInntekter();
        assertThat(tilkomneInntekter1.size()).isEqualTo(1);
        assertThat(tilkomneInntekter1.get(0).getArbeidsgiver().get()).isEqualTo(arbeidsgiver2);
        assertThat(tilkomneInntekter1.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(tilkomneInntekter1.get(0).getBruttoInntektPrÅr().compareTo(Beløp.fra(100_000))).isEqualTo(0);

        var periodeLagtTil = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(1);
        var tilkomneInntekter2 = periodeLagtTil
                .getTilkomneInntekter();
        assertThat(tilkomneInntekter2.size()).isEqualTo(1);
        assertThat(tilkomneInntekter2.get(0).getArbeidsgiver().get()).isEqualTo(arbeidsgiver2);
        assertThat(tilkomneInntekter2.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(tilkomneInntekter2.get(0).getBruttoInntektPrÅr().compareTo(Beløp.fra(100_000))).isEqualTo(0);
    }


    @Test
    void skal_sette_verdier_for_vurdert_næring() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var nyAndel = lagNæringsandel(2L, AndelKilde.PROSESS_PERIODISERING);

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var yrkesaktiviteter = List.of(yrkesaktivitet);
        var utbetalingsgrader = List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel);
        var andeler = List.of(arbeidstakerandelFraStart, nyAndel);

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));

        var vurderteInntektsforhold = List.of(new NyttInntektsforholdDto(
                AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE,
                null, null, 100_000, true));

        var vudertPeride = new VurderTilkomneInntektsforholdPeriodeDto(vurderteInntektsforhold, periode.getFomDato(), periode.getTomDato());

        VurderTilkommetInntektHåndteringDto dto = new VurderTilkommetInntektHåndteringDto(List.of(vudertPeride));

        BeregningsgrunnlagInput input = lagInput(periode, yrkesaktiviteter, utbetalingsgrader, andeler, vurderteInntektsforhold);

        // Act
        var nyttGr = VurderTilkommetInntektTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));

        var tilkomneInntekter = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0)
                .getTilkomneInntekter();

        // Assert
        assertThat(tilkomneInntekter.size()).isEqualTo(1);
        assertThat(tilkomneInntekter.get(0).getArbeidsgiver()).isEmpty();
        assertThat(tilkomneInntekter.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(tilkomneInntekter.get(0).getBruttoInntektPrÅr().compareTo(Beløp.fra(100_000))).isEqualTo(0);


    }

    private BeregningsgrunnlagInput lagInput(Intervall periode, List<YrkesaktivitetDto> yrkesaktiviteter, List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, List<NyttInntektsforholdDto> vurderteInntektsforhold) {
        var input = new BeregningsgrunnlagInput(
                new KoblingReferanseMock(STP),
                lagIAYGrunnlag(yrkesaktiviteter),
                null,
                null,
                new PleiepengerSyktBarnGrunnlag(utbetalingsgrader)
        );

        input = input.medBeregningsgrunnlagGrunnlag(lagBeregningsgrunnlag(periode.getFomDato(), periode.getTomDato(),
                andeler, vurderteInntektsforhold));
        return input;
    }

    private NyttInntektsforholdDto lagNyttInntektsforhold(String orgnr, boolean skalRedusereUtbetaling, Integer bruttoInntektPrÅr) {
        return new NyttInntektsforholdDto(
                AktivitetStatus.ARBEIDSTAKER,
                orgnr, null, bruttoInntektPrÅr, skalRedusereUtbetaling);
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYGrunnlag(List<YrkesaktivitetDto> yrkesaktiviteter) {
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        yrkesaktiviteter.forEach(aktørArbeidBuilder::leggTilYrkesaktivitet);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                        .leggTilAktørArbeid(aktørArbeidBuilder)).build();
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(LocalDate fom, LocalDate tom, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, List<NyttInntektsforholdDto> vurderteInntektsforhold) {
        var periodeBuilder = new BeregningsgrunnlagPeriodeDto.Builder();
        periodeBuilder.medBeregningsgrunnlagPeriode(fom, tom);
        andeler.forEach(periodeBuilder::leggTilBeregningsgrunnlagPrStatusOgAndel);
        if (!vurderteInntektsforhold.isEmpty()) {
            periodeBuilder.leggTilPeriodeÅrsak(PeriodeÅrsak.TILKOMMET_INNTEKT);
        }
        vurderteInntektsforhold.stream().map(v -> new TilkommetInntektDto(v.getAktivitetStatus(), v.getArbeidsgiverIdentifikator() != null ? Arbeidsgiver.virksomhet(v.getArbeidsgiverIdentifikator()) : null, InternArbeidsforholdRefDto.ref(v.getArbeidsforholdId()), null, null, null))
                .forEach(periodeBuilder::leggTilTilkommetInntekt);
        return BeregningsgrunnlagGrunnlagDtoBuilder.nytt()
                .medBeregningsgrunnlag(BeregningsgrunnlagDto.builder()
                        .leggTilBeregningsgrunnlagPeriode(periodeBuilder)
                        .medSkjæringstidspunkt(fom)
                        .medGrunnbeløp(Beløp.fra(100_000))
                        .build())
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagArbeidstakerandel(Arbeidsgiver arbeidsgiver2, long andelsnr, AndelKilde kilde, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(andelsnr)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver2).medArbeidsforholdRef(arbeidsforholdRef))
                .medKilde(kilde)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build();
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagNæringsandel(long andelsnr, AndelKilde kilde) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(andelsnr)
                .medKilde(kilde)
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .build();
    }

    private YrkesaktivitetDto lagYrkesaktivitet(Arbeidsgiver arbeidsgiver2, LocalDate fom, LocalDate tom, InternArbeidsforholdRefDto arbeidsforholdId) {
        return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver2)
                .medArbeidsforholdId(arbeidsforholdId)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                        .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                        .medErAnsettelsesPeriode(true))
                .build();
    }


    private List<PeriodeMedUtbetalingsgradDto> lagUtbetalingsgrader(int i, LocalDate fom, LocalDate tom) {
        return List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(fom, tom), Utbetalingsgrad.valueOf(i)));
    }

    private AktivitetDto lagAktivitet(Arbeidsgiver arbeidsgiver2, InternArbeidsforholdRefDto ref) {
        return new AktivitetDto(arbeidsgiver2, ref, UttakArbeidType.ORDINÆRT_ARBEID);
    }

}
