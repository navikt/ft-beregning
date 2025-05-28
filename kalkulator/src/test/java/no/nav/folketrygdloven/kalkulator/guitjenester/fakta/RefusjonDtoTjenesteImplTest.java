package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.utils.Tuple;

class RefusjonDtoTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);

    private final static InternArbeidsforholdRefDto ARB_ID = InternArbeidsforholdRefDto.nyRef();
    private final static String ORGNR = "123456780";
    private final static Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet(ORGNR);
    private final static String ORGNR_2 = "3242521";
    private final static Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet(ORGNR_2);
    private static final Beløp GRUNNBELØP = Beløp.fra(100_000);
    private static final Beløp SEKS_G = GRUNNBELØP.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());


    @Test
    void skal_ikkje_kunne_endre_refusjon_for_andel_uten_gradering_og_uten_refusjon() {
        //Arrange
        var bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(bg);
        var andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER))
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2)
            .medRefusjonskravPrÅr(SEKS_G.adder(Beløp.fra(100)), Utfall.GODKJENT))
        .build(periode);

        // Act
        var skalKunneEndreRefusjon = RefusjonDtoTjeneste.skalKunneEndreRefusjon(andel, periode, AktivitetGradering.INGEN_GRADERING, GRUNNBELØP);

        // Assert
        assertThat(skalKunneEndreRefusjon).isFalse();
    }

    @Test
    void skal_ikkje_kunne_endre_refusjon_for_andel_med_gradering_og_med_refusjon() {
        //Arrange
        var bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(bg);
        var refPrMnd = Beløp.fra(100);
        var andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER)
                .medRefusjonskravPrÅr(refPrMnd.multipliser(KonfigTjeneste.getMånederIÅr()), Utfall.GODKJENT))
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjonskravPrÅr(SEKS_G.adder(Beløp.fra(100)), Utfall.GODKJENT))
            .build(periode);
        // Act
        var skalKunneEndreRefusjon = RefusjonDtoTjeneste.skalKunneEndreRefusjon(andel, periode, AktivitetGradering.INGEN_GRADERING, GRUNNBELØP);

        // Assert
        assertThat(skalKunneEndreRefusjon).isFalse();
    }

    @Test
    void skal_ikkje_kunne_endre_refusjon_for_andel_med_gradering_og_uten_refusjon_totalrefusjon_under_6G() {
        //Arrange
        var bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(bg);
        var andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER)
                .medRefusjonskravPrÅr(Beløp.ZERO, Utfall.GODKJENT))
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjonskravPrÅr(SEKS_G.subtraher(Beløp.fra(100)), Utfall.GODKJENT))
            .build(periode);

        // Act
        var skalKunneEndreRefusjon = RefusjonDtoTjeneste.skalKunneEndreRefusjon(andel, periode, AktivitetGradering.INGEN_GRADERING, GRUNNBELØP);

        // Assert
        assertThat(skalKunneEndreRefusjon).isFalse();
    }

    @Test
    void skal_kunne_endre_refusjon_for_andel_med_gradering_og_uten_refusjon_totalrefusjon_lik_6G() {
        //Arrange
        var bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(bg);
        var andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER)
                .medRefusjonskravPrÅr(Beløp.ZERO, Utfall.GODKJENT))
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjonskravPrÅr(SEKS_G, Utfall.GODKJENT))
            .build(periode);

        var andelGradering = AndelGradering.builder()
            .medArbeidsgiver(ARBEIDSGIVER)
            .medArbeidsforholdRef(ARB_ID)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .leggTilGradering(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, TIDENES_ENDE, Aktivitetsgrad.fra(10)))
            .build();


        // Act
        var skalKunneEndreRefusjon = RefusjonDtoTjeneste.skalKunneEndreRefusjon(andel,
                periode, new AktivitetGradering(andelGradering), GRUNNBELØP);

        // Assert
        assertThat(skalKunneEndreRefusjon).isTrue();
    }

    @Test
    void skal_kunne_endre_refusjon_for_andel_med_gradering_og_uten_refusjon_totalrefusjon_over_6G() {
        //Arrange
        var bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(bg);
        var andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER)
                .medRefusjonskravPrÅr(Beløp.ZERO, Utfall.GODKJENT))
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjonskravPrÅr(SEKS_G.adder(Beløp.fra(100)), Utfall.GODKJENT))
            .build(periode);

        var andelGradering = AndelGradering.builder()
            .medArbeidsgiver(ARBEIDSGIVER)
            .medArbeidsforholdRef(ARB_ID)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .leggTilGradering(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, TIDENES_ENDE, Aktivitetsgrad.fra(10)))
            .build();


        // Act
        var skalKunneEndreRefusjon = RefusjonDtoTjeneste.skalKunneEndreRefusjon(andel,
                periode, new AktivitetGradering(andelGradering), GRUNNBELØP);

        // Assert
        assertThat(skalKunneEndreRefusjon).isTrue();
    }

    @Test
    void skal_slå_sammen_refusjon_for_andeler_i_samme_arbeidsforhold() {
        Integer refusjonskrav1 = 10000;
        Integer refusjonskrav2 = 15000;
        Integer refusjonskrav3 = 20000;
        var refusjon = List.of(new Tuple<>(true, refusjonskrav1), new Tuple<>(false, refusjonskrav2), new Tuple<>(false, refusjonskrav3));
        var andeler = lagAndeler(refusjon, 50000);
        RefusjonDtoTjeneste.slåSammenRefusjonForAndelerISammeArbeidsforhold(andeler);

        var andelSomIkkjeErLagtTilManuelt = andeler.stream().filter(a -> !a.getLagtTilAvSaksbehandler()).findFirst().get();
        assertThat(andelSomIkkjeErLagtTilManuelt.getRefusjonskravPrAar()).isEqualByComparingTo(ModellTyperMapper.beløpTilDto(Beløp.fra(refusjonskrav1+refusjonskrav2+refusjonskrav3)));
        var andelSomErLagtTilManuelt = andeler.stream().filter(FaktaOmBeregningAndelDto::getLagtTilAvSaksbehandler).findFirst().get();
        assertThat(andelSomErLagtTilManuelt.getRefusjonskravPrAar()).isNull();
    }

    private List<FordelBeregningsgrunnlagAndelDto> lagAndeler(List<Tuple<Boolean, Integer>> refusjonskrav, Integer refusjonFraInntektsmelding) {
        var arbeidsforholdDto = new BeregningsgrunnlagArbeidsforholdDto();
        arbeidsforholdDto.setArbeidsgiverIdent("432423423");
        return refusjonskrav.stream().map(tuple -> {
            var andel = new FordelBeregningsgrunnlagAndelDto(new FaktaOmBeregningAndelDto());
            andel.setArbeidsforhold(arbeidsforholdDto);
            andel.setLagtTilAvSaksbehandler(tuple.getElement1());
            andel.setRefusjonskravPrAar(ModellTyperMapper.beløpTilDto(Beløp.fra(tuple.getElement2())));
            if (tuple.getElement1()) {
                andel.setRefusjonskravFraInntektsmeldingPrÅr(ModellTyperMapper.beløpTilDto(Beløp.fra(refusjonFraInntektsmelding)));
            }
            return andel;
        }).collect(Collectors.toList());
    }
}
