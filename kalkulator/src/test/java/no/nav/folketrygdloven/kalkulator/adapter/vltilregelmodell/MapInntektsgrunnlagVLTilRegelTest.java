package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

class MapInntektsgrunnlagVLTilRegelTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.now();
    public static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("94632432");
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private MapInntektsgrunnlagVLTilRegelFelles mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();

    @Test
    void skal_mappe_inntektsmelding_for_arbeid_med_fleire_yrkesaktiviteter() {
        // Arrange
        var im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(Beløp.fra(10))
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        var p1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12),
                SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        var p2 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12),
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1));
        var iayGrunnlag = lagIAYGrunnlagMedArbeidIPerioder(
                List.of(p1, p2),
                List.of(im));

        var input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(koblingReferanse, null, iayGrunnlag, Dekningsgrad.DEKNINGSGRAD_100);

        // Act
        var map = mapInntektsgrunnlagVLTilRegel.mapInntektsgrunnlagFørStpBeregning(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        assertThat(map.getPeriodeinntekter()).hasSize(1);
    }


    @Test
    void skal_ikkje_mappe_inntektsmelding_for_arbeid_som_slutter_dagen_før_skjæringstidspunktet() {
        // Arrange
        var im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(Beløp.fra(10))
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        var iayGrunnlag = lagIAYGrunnlagMedArbeidIPeriode(
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1)),
                List.of(im));


        var input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(koblingReferanse, null, iayGrunnlag, Dekningsgrad.DEKNINGSGRAD_100);

        // Act
        var map = mapInntektsgrunnlagVLTilRegel.mapInntektsgrunnlagFørStpBeregning(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        assertThat(map.getPeriodeinntekter()).isEmpty();
    }

    @Test
    void skal_mappe_inntektsmelding_for_arbeid_som_slutter_på_skjæringstidspunktet() {
        // Arrange
        var im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(Beløp.fra(10))
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        var iayGrunnlag = lagIAYGrunnlagMedArbeidIPeriode(
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING),
                List.of(im));

        var input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(koblingReferanse, null, iayGrunnlag, Dekningsgrad.DEKNINGSGRAD_100);


        // Act
        var map = mapInntektsgrunnlagVLTilRegel.mapInntektsgrunnlagFørStpBeregning(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        assertThat(map.getPeriodeinntekter()).hasSize(1);
    }

    @Test
    void skal_mappe_inntektsmelding_for_arbeid_som_slutter_dagen_etter_skjæringstidspunktet() {
        // Arrange
        var im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(Beløp.fra(10))
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        var iayGrunnlag = lagIAYGrunnlagMedArbeidIPeriode(
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1)),
                List.of(im));

        var input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(koblingReferanse, null, iayGrunnlag, Dekningsgrad.DEKNINGSGRAD_100);

        // Act
        var map = mapInntektsgrunnlagVLTilRegel.mapInntektsgrunnlagFørStpBeregning(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        assertThat(map.getPeriodeinntekter()).hasSize(1);
    }

    @Test
    void skal_normalisere_ytelse_vedtak_perioder_til_enkeltdager() {
        // Arrange - Setup en periode på 3 dager med ytelse vedtak
        var fom = SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(2);
        var periode = Intervall.fraOgMedTilOgMed(fom, SKJÆRINGSTIDSPUNKT_BEREGNING);
        var dagsats = Beløp.fra(1000);

        var iayGrunnlag = lagIAYGrunnlagMedOpplæringspengerVedtak(periode, dagsats);
        var input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(
                koblingReferanse,
                null,
                iayGrunnlag,
                Dekningsgrad.DEKNINGSGRAD_100);

        // Act
        var map = mapInntektsgrunnlagVLTilRegel.mapInntektsgrunnlagFørStpBeregning(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        // Assert - Perioden skal være normalisert til 3 separate periodeinntekter (en per dag)
        var ytelsePeriodeinntekter = map.getPeriodeinntekter().stream()
                .filter(pi -> pi.getInntektskilde().equals(
                        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde.YTELSE_VEDTAK))
                .toList();

        assertThat(ytelsePeriodeinntekter).hasSize(3);
        assertThat(ytelsePeriodeinntekter.get(0).getInntekt()).isEqualTo(dagsats.verdi());
        assertThat(ytelsePeriodeinntekter.get(1).getInntekt()).isEqualTo(dagsats.verdi());
        assertThat(ytelsePeriodeinntekter.get(2).getInntekt()).isEqualTo(dagsats.verdi());
    }

    @Test
    void skal_normalisere_ytelse_vedtak_med_en_enkeltdag() {
        // Arrange - Setup en periode på 1 dag med ytelse vedtak
        var dato = SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(2);
        var periode = Intervall.fraOgMedTilOgMed(dato, dato);
        var dagsats = Beløp.fra(500);

        var iayGrunnlag = lagIAYGrunnlagMedOpplæringspengerVedtak(periode, dagsats);
        var input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(
                koblingReferanse,
                null,
                iayGrunnlag,
                Dekningsgrad.DEKNINGSGRAD_100);

        // Act
        var map = mapInntektsgrunnlagVLTilRegel.mapInntektsgrunnlagFørStpBeregning(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        // Assert - En dag skal gi en periodeinntekt
        var ytelsePeriodeinntekter = map.getPeriodeinntekter().stream()
                .filter(pi -> pi.getInntektskilde().equals(
                        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde.YTELSE_VEDTAK))
                .toList();

        assertThat(ytelsePeriodeinntekter).hasSize(1);
        assertThat(ytelsePeriodeinntekter.get(0).getInntekt()).isEqualTo(dagsats.verdi());
        assertThat(ytelsePeriodeinntekter.get(0).getUtbetalingsfaktor()).isPresent();
        assertThat(ytelsePeriodeinntekter.get(0).getUtbetalingsfaktor()).contains(BigDecimal.ONE);
    }

    @Test
    void skal_normalisere_ytelse_vedtak_med_riktig_inntektskategori() {
        // Arrange - Setup ytelse vedtak med spesifikk inntektskategori
        var fom = SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, SKJÆRINGSTIDSPUNKT_BEREGNING);
        var dagsats = Beløp.fra(1500);

        var iayGrunnlag = lagIAYGrunnlagMedOpplæringspengerVedtak(periode, dagsats);
        var input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(
                koblingReferanse,
                null,
                iayGrunnlag,
                Dekningsgrad.DEKNINGSGRAD_100);

        // Act
        var map = mapInntektsgrunnlagVLTilRegel.mapInntektsgrunnlagFørStpBeregning(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        // Assert - Inntektskategorien skal bevares for hver dag
        var ytelsePeriodeinntekter = map.getPeriodeinntekter().stream()
                .filter(pi -> pi.getInntektskilde().equals(
                        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde.YTELSE_VEDTAK))
                .toList();

        assertThat(ytelsePeriodeinntekter).hasSize(2);
        for (var pi : ytelsePeriodeinntekter) {
            assertThat(pi.getInntektskategori()).isEqualTo(
                    no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.DAGPENGER);
        }
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYGrunnlagMedArbeidIPeriode(Intervall periode,
                                                                           List<InntektsmeldingDto> inntektsmeldinger) {
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder();
        aktørArbeidBuilder.leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(periode))
                .medArbeidsgiver(VIRKSOMHET)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef()));
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(inntektsmeldinger)
                .medData(registerBuilder).build();
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYGrunnlagMedArbeidIPerioder(List<Intervall> perioder,
                                                                            List<InntektsmeldingDto> inntektsmeldinger) {
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder();
        perioder.forEach(periode -> aktørArbeidBuilder.leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(periode))
                .medArbeidsgiver(VIRKSOMHET)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef()))
        );
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(inntektsmeldinger)
                .medData(registerBuilder).build();
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYGrunnlagMedOpplæringspengerVedtak(Intervall periode, Beløp dagsats) {
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);

        var antallDager = (int) periode.getFomDato().datesUntil(periode.getTomDato().plusDays(1)).count();
        var ytelseBuilder = YtelseDtoBuilder.ny()
            .medPeriode(periode)
            .medYtelseType(YtelseType.OPPLÆRINGSPENGER)
            .medVedtaksDagsats(dagsats)
            .leggTilYtelseAnvist(YtelseAnvistDtoBuilder.ny()
                .medAnvistPeriode(periode)
                .medBeløp(dagsats.multipliser(BigDecimal.valueOf(antallDager)))
                .medDagsats(dagsats)
                .medUtbetalingsgradProsent(Stillingsprosent.HUNDRED)
                .medAnvisteAndeler(List.of(lagDagpengerAndel(dagsats, antallDager)))
                .build());

        var ytelseAktørBuilder = registerBuilder.getAktørYtelseBuilder();
        ytelseAktørBuilder.leggTilYtelse(ytelseBuilder);
        registerBuilder.leggTilAktørYtelse(ytelseAktørBuilder);

        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
            .medData(registerBuilder)
            .build();
    }

    private AnvistAndel lagDagpengerAndel(Beløp dagsats, int antallDager) {
        return new AnvistAndel(
            VIRKSOMHET,
            InternArbeidsforholdRefDto.nullRef(),
            dagsats.multipliser(BigDecimal.valueOf(antallDager)),
            dagsats,
            Stillingsprosent.ZERO,
            Inntektskategori.DAGPENGER
        );
    }

}
