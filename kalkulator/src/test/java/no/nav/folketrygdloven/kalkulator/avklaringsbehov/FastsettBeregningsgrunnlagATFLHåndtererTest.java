package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import static no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagATFLDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.InntektPrAndelDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

class FastsettBeregningsgrunnlagATFLHåndtererTest {


    public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;
    private final List<Arbeidsgiver> virksomheter = new ArrayList<>();
    private static final Beløp GRUNNBELØP = Beløp.fra(90000);
    private static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_ID = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    private static final int BRUTTO_PR_AR = 150000;
    private static final int OVERSTYRT_PR_AR = 200000;
    private static final int FRILANSER_INNTEKT = 4000;

    @BeforeEach
    void setup() {
        virksomheter.add(Arbeidsgiver.virksomhet("991825827"));
        virksomheter.add(Arbeidsgiver.virksomhet("974760673"));
    }

    @Test
    void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_AT() {
        //Arrange
        buildOgLagreBeregningsgrunnlag(true, 1, 1);

        //Dto
        var dto = new FastsettBeregningsgrunnlagATFLDto(Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        var grunnlag = FastsettBeregningsgrunnlagATFLHåndterer.håndter(input, dto);

        //Assert
        var beregningsgrunnlagOpt = grunnlag.getBeregningsgrunnlagHvisFinnes();
        Assertions.assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> assertBeregningsgrunnlag(beregningsgrunnlag, 1, OVERSTYRT_PR_AR));
    }

    @Test
    void skal_oppdatere_bruttoPrÅr_i_beregningsgrunnlagperiode_når_andel_overstyres_AT() {
        //Arrange
        var overstyrt1 = 1000;
        var overstyrt2 = 2000;
        var antallAndeler = 2;
        buildOgLagreBeregningsgrunnlag(true, 1, antallAndeler);

        List<InntektPrAndelDto> overstyrteVerdier = new ArrayList<>();
        overstyrteVerdier.add(new InntektPrAndelDto(overstyrt1, 1L));
        overstyrteVerdier.add(new InntektPrAndelDto(overstyrt2, 2L));

        //Dto
        var dto = new FastsettBeregningsgrunnlagATFLDto(overstyrteVerdier, FRILANSER_INNTEKT);

        // Act
        var grunnlag = FastsettBeregningsgrunnlagATFLHåndterer.håndter(input, dto);

        //Assert
        var beregningsgrunnlagOpt = grunnlag.getBeregningsgrunnlagHvisFinnes();
        Assertions.assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> {
            var beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
            var andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
            Assertions.assertThat(andeler).hasSize(antallAndeler);
            var nyBruttoBG1 = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0)
                .getBruttoPrÅr().verdi();
            Assertions.assertThat(nyBruttoBG1.intValue()).as("nyBruttoBG").isEqualTo(overstyrt1);
            var nyBruttoBG2 = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(1)
                .getBruttoPrÅr().verdi();
            Assertions.assertThat(nyBruttoBG2.intValue()).as("nyBruttoBG").isEqualTo(overstyrt2);
            assertThat(beregningsgrunnlagPeriode.getBruttoPrÅr().intValue()).as("nyBruttoBGPeriode").isEqualTo(overstyrt1 + overstyrt2);
        });
    }

    @Test
    void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_for_fleire_perioder_med_andeler_med_ulike_inntektskategorier_AT() {
        //Arrange
        var arbeidstakerPrPeriode = List.of(List.of(false, true), List.of(false, true, true, true));
        var inntektskategoriPeriode1 = List.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.ARBEIDSTAKER);
        var inntektskategoriPeriode2 = List.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, Inntektskategori.SJØMANN);
        var inntektskategoriPrPeriode = List.of(inntektskategoriPeriode1, inntektskategoriPeriode2);
        buildOgLagreBeregningsgrunnlag(arbeidstakerPrPeriode, inntektskategoriPrPeriode);

        //Dto
        var dto = new FastsettBeregningsgrunnlagATFLDto(Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 2L)), FRILANSER_INNTEKT);

        // Act
        var grunnlag = FastsettBeregningsgrunnlagATFLHåndterer.håndter(input, dto);

        //Assert
        var beregningsgrunnlagOpt = grunnlag.getBeregningsgrunnlagHvisFinnes();
        var perioder = beregningsgrunnlagOpt.get().getBeregningsgrunnlagPerioder();
        Assertions.assertThat(perioder).hasSize(2);
        var andelerPeriode1 = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        Assertions.assertThat(andelerPeriode1).hasSize(2);
        assertThat(andelerPeriode1.get(1).getBruttoPrÅr().intValue()).isEqualByComparingTo(OVERSTYRT_PR_AR);
        var andelerPeriode2 = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList();
        Assertions.assertThat(andelerPeriode2).hasSize(4);
        assertThat(andelerPeriode2.get(1).getBruttoPrÅr().intValue()).isEqualByComparingTo(OVERSTYRT_PR_AR);
        assertThat(andelerPeriode2.get(2).getBruttoPrÅr().intValue()).isEqualTo(BRUTTO_PR_AR);
        assertThat(andelerPeriode2.get(3).getBruttoPrÅr().intValue()).isEqualTo(BRUTTO_PR_AR);
    }

    @Test
    void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_for_fleire_perioder_AT() {
        //Arrange
        var antallPerioder = 3;
        buildOgLagreBeregningsgrunnlag(true, antallPerioder, 1);

        //Dto
        var dto = new FastsettBeregningsgrunnlagATFLDto(Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        var grunnlag = FastsettBeregningsgrunnlagATFLHåndterer.håndter(input, dto);

        //Assert
        var beregningsgrunnlagOpt = grunnlag.getBeregningsgrunnlagHvisFinnes();
        Assertions.assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> assertBeregningsgrunnlag(beregningsgrunnlag, antallPerioder, OVERSTYRT_PR_AR));
    }

    @Test
    void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_FL() {
        //Arrange
        buildOgLagreBeregningsgrunnlag(false, 1, 1);

        //Dto
        var dto = new FastsettBeregningsgrunnlagATFLDto(Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        var grunnlag = FastsettBeregningsgrunnlagATFLHåndterer.håndter(input, dto);

        //Assert
        var beregningsgrunnlagOpt = grunnlag.getBeregningsgrunnlagHvisFinnes();
        Assertions.assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> assertBeregningsgrunnlag(beregningsgrunnlag, 1, FRILANSER_INNTEKT));
    }

    @Test
    void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_for_fleire_perioder_FL() {
        //Arrange
        var antallPerioder = 3;
        buildOgLagreBeregningsgrunnlag(false, antallPerioder, 1);

        //Dto
        var dto = new FastsettBeregningsgrunnlagATFLDto(Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        var grunnlag = FastsettBeregningsgrunnlagATFLHåndterer.håndter(input, dto);

        //Assert
        var beregningsgrunnlagOpt = grunnlag.getBeregningsgrunnlagHvisFinnes();
        Assertions.assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> assertBeregningsgrunnlag(beregningsgrunnlag, antallPerioder, FRILANSER_INNTEKT));
    }

    private void assertBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag, int antallPerioder, int frilanserInntekt) {
        var beregningsgrunnlagperioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        Assertions.assertThat(beregningsgrunnlagperioder).hasSize(antallPerioder);
        beregningsgrunnlagperioder.forEach(periode -> {
                var nyBruttoBG = periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0)
                    .getBruttoPrÅr();
                Assertions.assertThat(nyBruttoBG.intValue()).as("nyBruttoBG").isEqualTo(frilanserInntekt);
            }
        );
    }


    private void buildOgLagreBeregningsgrunnlag(List<List<Boolean>> erArbeidstakerPrPeriode, List<List<Inntektskategori>> inntektskategoriPrPeriode) {
        var beregningsgrunnlagBuilder = BeregningsgrunnlagDto.Builder.oppdater(Optional.empty())
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5));

        Assertions.assertThat(erArbeidstakerPrPeriode).hasSize(inntektskategoriPrPeriode.size());
        for (var i = 0; i < erArbeidstakerPrPeriode.size(); i++) {
            var fom = LocalDate.now().minusDays(20).plusDays(i * 5).plusDays(i == 0 ? 0 : 1);
            var tom = fom.plusDays(5);
            leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagBuilder, fom, tom, erArbeidstakerPrPeriode.get(i), inntektskategoriPrPeriode.get(i));
        }
        input = lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlagBuilder.build(), BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private void buildOgLagreBeregningsgrunnlag(boolean erArbeidstaker, int antallPerioder, int antallAndeler) {
        var beregningsgrunnlagBuilder = BeregningsgrunnlagDto.Builder.oppdater(Optional.empty())
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);

        for (var i = 0; i < antallPerioder; i++) {
            var fom = LocalDate.now().minusDays(20).plusDays(i * 5).plusDays(i == 0 ? 0 : 1);
            var tom = fom.plusDays(5);
            leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagBuilder, fom, tom, erArbeidstaker, antallAndeler);
        }
        input = lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlagBuilder.build(), BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private void leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder, LocalDate fomDato, LocalDate tomDato, List<Boolean> erArbeidstakerList, List<Inntektskategori> inntektskategoriList) {
        var beregningsgrunnlagPeriodeBuilder = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fomDato, tomDato);
        Assertions.assertThat(erArbeidstakerList).hasSize(inntektskategoriList.size());
        for (var i = 0; i < erArbeidstakerList.size(); i++) {
            if (erArbeidstakerList.get(i)) {
                leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriodeBuilder, AktivitetStatus.ARBEIDSTAKER, virksomheter.get(0), ARBEIDSFORHOLD_ID, (long) (i+1), inntektskategoriList.get(i));
            } else {
                leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriodeBuilder, AktivitetStatus.FRILANSER, null, null, ((long) (i+1)), inntektskategoriList.get(i));
            }
        }
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
    }


    private void leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder, LocalDate fomDato, LocalDate tomDato, boolean erArbeidstaker, int antallAndeler) {
        var beregningsgrunnlagPeriodeBuilder = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fomDato, tomDato);
        for (var i = 0; i < antallAndeler; i++) {
            if (erArbeidstaker) {
                leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriodeBuilder, AktivitetStatus.ARBEIDSTAKER, virksomheter.get(i), ARBEIDSFORHOLD_ID, (long) (i+1), Inntektskategori.ARBEIDSTAKER);
            } else {
                leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriodeBuilder, AktivitetStatus.FRILANSER, null, null, ((long) (i+1)), Inntektskategori.FRILANSER);
            }
        }
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
    }

    private void leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPeriodeDto.Builder beregningsgrunnlagPeriodeBuilder, AktivitetStatus aktivitetStatus,
                                                          Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbforholdId, Long andelsnr, Inntektskategori inntektskategori) {

        var builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(andelsnr)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .medBeregnetPrÅr(Beløp.fra(BRUTTO_PR_AR));
        if (arbeidsgiver != null) {
            var bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsforholdRef(arbforholdId)
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
            builder.medBGAndelArbeidsforhold(bga);
        }
        beregningsgrunnlagPeriodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(
            builder);
    }
}
