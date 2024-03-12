package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import static java.util.Collections.singletonList;
import static no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil.lagHåndteringInputMedBeregningsgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelFastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.RedigerbarAndelDto;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.folketrygdloven.utils.Tuple;

public class FordelBeregningsgrunnlagHåndtererTest {
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = Beløp.fra(600000);
    private static final String ORG_NUMMER = "974652269";
    public KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private HåndterBeregningsgrunnlagInput input;

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        return BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(GRUNNBELØP)
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
    }

    private BeregningsgrunnlagPeriodeDto lagPeriode(BeregningsgrunnlagDto forrigeBG, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(fom, tom)
                .build(forrigeBG);
    }

    @Test
    public void skal_fjerne_fordele_naturalytelser_der_det_finnes() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        Long andelsnr2 = 2L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel = buildArbeidstakerAndel(
                arbId,
                andelsnr,
                periode,
                null,

                false,
                Inntektskategori.ARBEIDSTAKER,
                false,
                Beløp.fra(200_000),
                Beløp.fra(10_000));
        BeregningsgrunnlagPrStatusOgAndelDto andel2 = buildArbeidstakerAndel(
                arbId2,
                andelsnr2,
                periode, null,
                false,
                Inntektskategori.ARBEIDSTAKER,
                false,
                null,
                null);
        input = lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                Tuple.of(beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING),
                BeregningsgrunnlagTilstand.FASTSATT_INN);

        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndelInklNaturalytelse(andel, arbId, andelsnr, 0, inntektskategori);
        FordelBeregningsgrunnlagAndelDto fordeltAndel2 = lagFordeltAndelInklNaturalytelse(andel2, arbId2, andelsnr2, 210_000, inntektskategori);

        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(List.of(fordeltAndel, fordeltAndel2), SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);

        BeregningsgrunnlagDto grunnlagEtterOppdatering = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();

        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = grunnlagEtterOppdatering.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();

        BeregningsgrunnlagPrStatusOgAndelDto andelOppdatert1 = andeler.stream().filter(a -> a.matchUtenInntektskategori(andel) && a.getGjeldendeInntektskategori().equals(inntektskategori)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andelOppdatert2 = andeler.stream().filter(a -> a.matchUtenInntektskategori(andel2) && a.getGjeldendeInntektskategori().equals(inntektskategori)).findFirst().get();

        // Assert
        assertThat(andelOppdatert1.getAndelsnr()).isEqualByComparingTo(andelsnr);
        assertThat(andelOppdatert1.getBgAndelArbeidsforhold().get().getNaturalytelseBortfaltPrÅr()).isNotPresent();
        assertThat(andelOppdatert1.getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.ZERO);

        assertThat(andelOppdatert2.getAndelsnr()).isEqualByComparingTo(andelsnr2);
        assertThat(andelOppdatert2.getBgAndelArbeidsforhold().get().getNaturalytelseBortfaltPrÅr()).isNotPresent();
        assertThat(andelOppdatert2.getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(210_000));
    }

    @Test
    public void skal_opprettholde_andelsnummer() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        Long andelsnr2 = 2L;
        Long andelsnr3 = 12L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel = buildArbeidstakerAndel(arbId, andelsnr, periode, null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel2 = buildArbeidstakerAndel(arbId2, andelsnr2, periode, null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel3 = buildArbeidstakerAndel(arbId2, andelsnr3, periode, null, true, Inntektskategori.FISKER, false, null, null);
        input = lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                Tuple.of(beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING),
                BeregningsgrunnlagTilstand.FASTSATT_INN);

        Integer fastsatt = 10_000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Inntektskategori inntektskategori2 = Inntektskategori.DAGPENGER;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndel(andel, arbId, andelsnr, false, false, null, fastsatt, inntektskategori);
        FordelBeregningsgrunnlagAndelDto fordeltAndel2 = lagFordeltAndel(andel2, arbId2, andelsnr2, false, false, null, fastsatt, inntektskategori);
        FordelBeregningsgrunnlagAndelDto fordeltAndel3 = lagFordeltAndel(andel3, arbId2, andelsnr3, false, true, null, fastsatt, inntektskategori2);
        FordelBeregningsgrunnlagAndelDto fordeltAndel4 = lagFordeltAndel(null, arbId, andelsnr, true, true, null, fastsatt, inntektskategori2);

        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(List.of(fordeltAndel4, fordeltAndel3, fordeltAndel, fordeltAndel2), SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);

        BeregningsgrunnlagDto grunnlagEtterOppdatering = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();

        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = grunnlagEtterOppdatering.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();

        BeregningsgrunnlagPrStatusOgAndelDto andelOppdatert1 = andeler.stream().filter(a -> a.matchUtenInntektskategori(andel) && a.getGjeldendeInntektskategori().equals(inntektskategori)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andelOppdatert2 = andeler.stream().filter(a -> a.matchUtenInntektskategori(andel2) && a.getGjeldendeInntektskategori().equals(inntektskategori)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andelOppdatert3 = andeler.stream().filter(a -> a.matchUtenInntektskategori(andel3) && a.getGjeldendeInntektskategori().equals(inntektskategori2)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andelOppdatert4 = andeler.stream().filter(a -> a.matchUtenInntektskategori(andel) && a.getGjeldendeInntektskategori().equals(inntektskategori2)).findFirst().get();

        // Assert
        assertThat(andelOppdatert1.getAndelsnr()).isEqualByComparingTo(andelsnr);
        assertThat(andelOppdatert2.getAndelsnr()).isEqualByComparingTo(andelsnr2);
        assertThat(andelOppdatert3.getAndelsnr()).isEqualByComparingTo(andelsnr3);
        assertThat(andelOppdatert4.getAndelsnr()).isEqualByComparingTo(andelsnr3 + 1L);

    }

    @Test
    public void skal_sette_verdier_på_DP_lagt_til_av_saksbehandler() {
        // Arrange
        Long andelsnr = 1L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel = buildAPAndel(andelsnr, periode, true, true, Beløp.fra(100_000));
        input = lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                Tuple.of(beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING),
                BeregningsgrunnlagTilstand.FASTSATT_INN);

        Integer fastsatt = 10_000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltDPAndel(andel, andelsnr, fastsatt, inntektskategori);
        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(singletonList(fordeltAndel), SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);

        BeregningsgrunnlagDto grunnlagEtterOppdatering = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
        BeregningsgrunnlagPrStatusOgAndelDto andelEtterOppdatering = grunnlagEtterOppdatering.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        // Assert
        assertThat(andelEtterOppdatering.getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(andelEtterOppdatering.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);

    }

    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_med_1_periode_og_1_andel_refusjon_lik_null() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel = buildArbeidstakerAndel(arbId, andelsnr, periode, null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        input = lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                Tuple.of(beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING),
                BeregningsgrunnlagTilstand.FASTSATT_INN);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = null;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndel(andel, arbId, andelsnr, nyAndel, lagtTilAvSaksbehandler, refusjon, fastsatt, inntektskategori);
        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(singletonList(fordeltAndel), SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);

        BeregningsgrunnlagDto grunnlagEtterOppdatering = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
        BeregningsgrunnlagPrStatusOgAndelDto andelEtterOppdatering = grunnlagEtterOppdatering.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        // Assert
        assertThat(andelEtterOppdatering.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(Beløp.ZERO)).isEqualByComparingTo(Beløp.ZERO);
        assertThat(andelEtterOppdatering.getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(andelEtterOppdatering.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);

    }

    @Test
    public void skal_fordele_refusjon_etter_totalrefusjon_om_lik_null() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        Long andelsnr2 = 2L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        var refusjonskravPrÅr = Beløp.fra(120000);
        BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel = buildArbeidstakerAndel(arbId, andelsnr, periode, refusjonskravPrÅr, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel2 = buildArbeidstakerAndel(arbId2, andelsnr2, periode, refusjonskravPrÅr, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        input = lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                Tuple.of(beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING),
                BeregningsgrunnlagTilstand.FASTSATT_INN);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = null;
        Integer fastsatt = 30000;
        Integer fastsatt2 = 5000;
        Integer fastsatt3 = 5000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndel(eksisterendeAndel, arbId, andelsnr, nyAndel, lagtTilAvSaksbehandler, refusjon, fastsatt, inntektskategori);
        FordelBeregningsgrunnlagAndelDto fordeltAndel2 = lagFordeltAndel(eksisterendeAndel2, arbId2, andelsnr2, nyAndel, lagtTilAvSaksbehandler, refusjon, fastsatt2, inntektskategori);
        FordelBeregningsgrunnlagAndelDto fordeltAndel3 = lagFordeltAndel(null, arbId2, andelsnr2, true, true, refusjon, fastsatt3, Inntektskategori.FRILANSER);
        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(List.of(fordeltAndel, fordeltAndel2, fordeltAndel3), SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);

        BeregningsgrunnlagDto nyttBG = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBG.getBeregningsgrunnlagPerioder();
        Assertions.assertThat(perioder).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andel = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andel2 = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr2)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andel3 = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).findFirst().get();
        assertThat(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).get()).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(andel.getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(andel.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        var halvRefusjon = Beløp.fra(refusjonskravPrÅr.verdi().divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP));
        assertThat(andel2.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).get()).isEqualByComparingTo(halvRefusjon);
        assertThat(andel2.getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt2 * 12));
        assertThat(andel2.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(andel3.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).get()).isEqualByComparingTo(halvRefusjon);
        assertThat(andel3.getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt3 * 12));
        assertThat(andel3.getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
    }

    private FordelBeregningsgrunnlagAndelDto lagFordeltAndelInklNaturalytelse(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                              InternArbeidsforholdRefDto arbId,
                                                                              Long andelsnr,
                                                                              Integer fastsatt,
                                                                              Inntektskategori inntektskategori) {
        FordelFastsatteVerdierDto fastsatteVerdier = FordelFastsatteVerdierDto.Builder.ny()
                .medFastsattBeløpPrÅrInklNaturalytelse(fastsatt)
                .medInntektskategori(inntektskategori)
                .build();
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto(false, ORG_NUMMER, arbId, andelsnr, AktivitetStatus.ARBEIDSTAKER, OpptjeningAktivitetType.ARBEID, AndelKilde.PROSESS_START);
        return new FordelBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier, Inntektskategori.ARBEIDSTAKER,
                andel != null ? andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(Beløp.ZERO).intValue() : null,
                andel != null ? finnBrutto(andel) : null);
    }

    private FordelBeregningsgrunnlagAndelDto lagFordeltAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                               InternArbeidsforholdRefDto arbId,
                                                               Long andelsnr,
                                                               boolean nyAndel,
                                                               boolean lagtTilAvSaksbehandler,
                                                               Integer refusjon,
                                                               Integer fastsatt,
                                                               Inntektskategori inntektskategori) {
        FordelFastsatteVerdierDto fastsatteVerdier = FordelFastsatteVerdierDto.Builder.ny()
                .medRefusjonPrÅr(refusjon == null ? null : refusjon * 12)
                .medFastsattBeløpPrMnd(fastsatt)
                .medInntektskategori(inntektskategori)
                .build();
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto(nyAndel, ORG_NUMMER, arbId, andelsnr, AktivitetStatus.ARBEIDSTAKER, OpptjeningAktivitetType.ARBEID, lagtTilAvSaksbehandler ? AndelKilde.SAKSBEHANDLER_FORDELING : AndelKilde.PROSESS_START);
        return new FordelBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier, Inntektskategori.ARBEIDSTAKER,
                andel != null ? andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(Beløp.ZERO).intValue() : null,
                andel != null ? finnBrutto(andel) : null);
    }

    private FordelBeregningsgrunnlagAndelDto lagFordeltDPAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, Long andelsnr, Integer fastsatt, Inntektskategori inntektskategori) {
        FordelFastsatteVerdierDto fastsatteVerdier = FordelFastsatteVerdierDto.Builder.ny()
                .medFastsattBeløpPrMnd(fastsatt)
                .medInntektskategori(inntektskategori)
                .build();
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto(false, andelsnr, AktivitetStatus.DAGPENGER, OpptjeningAktivitetType.DAGPENGER, AndelKilde.SAKSBEHANDLER_FORDELING);
        return new FordelBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier, Inntektskategori.DAGPENGER, null,
                andel != null ? finnBrutto(andel) : null);
    }

    private Integer finnBrutto(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBruttoPrÅr() == null ? null : andel.getBruttoPrÅr().intValue();
    }

    private BeregningsgrunnlagPrStatusOgAndelDto buildArbeidstakerAndel(InternArbeidsforholdRefDto arbId2,
                                                                        Long andelsnr2,
                                                                        BeregningsgrunnlagPeriodeDto periode,
                                                                        Beløp refusjonskravPrÅr,
                                                                        boolean lagtTilAvSaksbehandler,
                                                                        Inntektskategori inntektskategori,
                                                                        boolean fastsattAvSaksbehandler,
                                                                        Beløp beregnetPrÅr,
                                                                        Beløp naturalytelseBortfaltPrÅr) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(Arbeidsgiver.virksomhet(ORG_NUMMER))
                        .medArbeidsforholdRef(arbId2).medRefusjonskravPrÅr(refusjonskravPrÅr, Utfall.GODKJENT)
                        .medNaturalytelseBortfaltPrÅr(naturalytelseBortfaltPrÅr))
                .medAndelsnr(andelsnr2)
                .medBeregningsperiode(LocalDate.of(2019, 7, 1), LocalDate.of(2019, 10, 1))
                .medBeregnetPrÅr(beregnetPrÅr)
                .medKilde(lagtTilAvSaksbehandler ? AndelKilde.SAKSBEHANDLER_KOFAKBER : AndelKilde.PROSESS_START)
                .medFastsattAvSaksbehandler(fastsattAvSaksbehandler)
                .medInntektskategori(inntektskategori)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto buildAPAndel(Long andelsnr2, BeregningsgrunnlagPeriodeDto periode,
                                                              boolean lagtTilAvSaksbehandler,
                                                              boolean fastsattAvSaksbehandler, Beløp beregnetPrÅr) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(andelsnr2)
                .medBeregningsperiode(LocalDate.of(2019, 7, 1), LocalDate.of(2019, 10, 1))
                .medBeregnetPrÅr(beregnetPrÅr)
                .medKilde(lagtTilAvSaksbehandler ? AndelKilde.SAKSBEHANDLER_KOFAKBER : AndelKilde.PROSESS_START)
                .medFastsattAvSaksbehandler(fastsattAvSaksbehandler)
                .medInntektskategori(Inntektskategori.ARBEIDSAVKLARINGSPENGER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)
                .build(periode);
    }

    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_med_1_periode_og_2_andeler() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        Long andelsnr2 = 2L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel1 = buildArbeidstakerAndel(arbId, andelsnr, periode, null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        buildArbeidstakerAndel(arbId2, andelsnr2, periode, null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        input = lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                Tuple.of(beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING),
                BeregningsgrunnlagTilstand.FASTSATT_INN);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndel(andel1, arbId, andelsnr, nyAndel, lagtTilAvSaksbehandler, refusjon, fastsatt, inntektskategori);
        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(singletonList(fordeltAndel), SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);
        BeregningsgrunnlagDto nyttBG = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
        BeregningsgrunnlagPrStatusOgAndelDto andelEtterOppdatering = nyttBG.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        // Assert
        assertThat(andelEtterOppdatering.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(null)).isEqualByComparingTo(Beløp.fra(refusjon * 12));
        assertThat(andelEtterOppdatering.getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(andelEtterOppdatering.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
    }


    @Test
    public void skal_sette_verdier_på_ny_andel_med_1_periode_og_1_andel() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel = buildArbeidstakerAndel(arbId, andelsnr, periode, null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        input = lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                Tuple.of(beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING),
                BeregningsgrunnlagTilstand.FASTSATT_INN);

        final boolean nyAndel = false;
        final boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndel(andel, arbId, andelsnr, nyAndel, lagtTilAvSaksbehandler, refusjon, fastsatt, inntektskategori);

        final boolean nyAndel2 = true;
        final boolean lagtTilAvSaksbehandler2 = true;
        Integer refusjon2 = 3000;
        Integer fastsatt2 = 20000;
        Inntektskategori inntektskategori2 = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
        FordelBeregningsgrunnlagAndelDto fordeltAndel2 = lagFordeltAndel(null, arbId, andelsnr, nyAndel2, lagtTilAvSaksbehandler2, refusjon2, fastsatt2, inntektskategori2);

        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(List.of(fordeltAndel2, fordeltAndel), SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);
        BeregningsgrunnlagDto nyttBG = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();

        // Assert
        double totalFastsatt = fastsatt + fastsatt2;
        double totalRefusjon = refusjon + refusjon2;

        assertThat(nyttBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(nyttBG.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);

        List<BeregningsgrunnlagPrStatusOgAndelDto> eksisterendeAndel = nyttBG.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.matchUtenInntektskategori(andel) && a.getGjeldendeInntektskategori().equals(inntektskategori)).collect(Collectors.toList());

        Assertions.assertThat(eksisterendeAndel).hasSize(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr)
                .orElse(null))
                .isEqualByComparingTo(Beløp.fra(BigDecimal.valueOf((fastsatt / totalFastsatt) * totalRefusjon * 12)));
        assertThat(eksisterendeAndel.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndelDto> andelLagtTil = nyttBG.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());

        Assertions.assertThat(andelLagtTil).hasSize(1);
        assertThat(andelLagtTil.get(0).getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(andelLagtTil.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(null))
                .isEqualByComparingTo(Beløp.fra(BigDecimal.valueOf((fastsatt2 / totalFastsatt) * totalRefusjon * 12)));
        assertThat(andelLagtTil.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt2 * 12));
        assertThat(andelLagtTil.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori2);
        assertThat(andelLagtTil.get(0).getBeregningsperiodeFom()).isEqualTo(eksisterendeAndel.get(0).getBeregningsperiodeFom());
        assertThat(andelLagtTil.get(0).getBeregningsperiodeTom()).isEqualTo(eksisterendeAndel.get(0).getBeregningsperiodeTom());
    }


    @Test
    public void skal_sette_verdier_på_andeler_for_tilbakehopp_til_KOFAKBER_med_1_periode_og_1_andel() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        Long andelsnrForAndelLagtTilAvSaksbehandler = 2L;

        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel = buildArbeidstakerAndel(arbId, andelsnr, periode, null, false, Inntektskategori.ARBEIDSTAKER,
                false, null, null);
        BeregningsgrunnlagDto forrigeGrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        buildArbeidstakerAndel(arbId, andelsnrForAndelLagtTilAvSaksbehandler, forrigeGrunnlag.getBeregningsgrunnlagPerioder().get(0),
                null, false, Inntektskategori.SJØMANN, false, null, null);
        input = BeregningsgrunnlagInputTestUtil.lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                new Tuple<>(beregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT),
                new Tuple<>(forrigeGrunnlag, BeregningsgrunnlagTilstand.FASTSATT_INN));

        final boolean nyAndel = false;
        final boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndel(andel, arbId, andelsnr, nyAndel, lagtTilAvSaksbehandler,
                refusjon, fastsatt, inntektskategori);

        final boolean nyAndel2 = false;
        final boolean lagtTilAvSaksbehandler2 = true;
        Integer refusjon2 = 3000;
        Integer fastsatt2 = 20000;
        Inntektskategori inntektskategori2 = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
        FordelBeregningsgrunnlagAndelDto fordeltAndel2 = lagFordeltAndel(null, arbId, andelsnrForAndelLagtTilAvSaksbehandler, nyAndel2, lagtTilAvSaksbehandler2,
                refusjon2, fastsatt2, inntektskategori2);


        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(List.of(fordeltAndel2, fordeltAndel),
                SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);
        BeregningsgrunnlagDto nyttBG = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
        // Assert
        double totalRefusjon = refusjon + refusjon2;
        double totalFastsatt = fastsatt + fastsatt2;
        assertThat(nyttBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(nyttBG.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);

        List<BeregningsgrunnlagPrStatusOgAndelDto> eksisterendeAndel = nyttBG.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.matchUtenInntektskategori(andel) && a.getGjeldendeInntektskategori().equals(inntektskategori)).collect(Collectors.toList());

        Assertions.assertThat(eksisterendeAndel).hasSize(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(null))
                .isEqualByComparingTo(Beløp.fra(BigDecimal.valueOf(fastsatt / totalFastsatt * totalRefusjon * 12)));
        assertThat(eksisterendeAndel.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndelDto> andelLagtTil = nyttBG.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());

        Assertions.assertThat(andelLagtTil).hasSize(1);
        assertThat(andelLagtTil.get(0).getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(andelLagtTil.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(null))
                .isEqualByComparingTo(Beløp.fra(BigDecimal.valueOf(fastsatt2 / totalFastsatt * totalRefusjon * 12)));
        assertThat(andelLagtTil.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt2 * 12));
        assertThat(andelLagtTil.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori2);
        assertThat(andelLagtTil.get(0).getBeregningsperiodeFom()).isEqualTo(eksisterendeAndel.get(0).getBeregningsperiodeFom());
        assertThat(andelLagtTil.get(0).getBeregningsperiodeTom()).isEqualTo(eksisterendeAndel.get(0).getBeregningsperiodeTom());

    }


    @Test
    public void skal_sette_verdier_på_andeler_for_tilbakehopp_til_steg_før_FORDEL() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        Long andelsnr2 = 2L;
        var forrigeFastsatt = Beløp.fra(200000);
        Inntektskategori forrigeInntektskategori = Inntektskategori.SJØMANN;
        BeregningsgrunnlagDto forrigeBG = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periodeForrigeBG = lagPeriode(forrigeBG, SKJÆRINGSTIDSPUNKT, null);
        buildArbeidstakerAndel(arbId, andelsnr, periodeForrigeBG, null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        buildArbeidstakerAndel(arbId, andelsnr2, periodeForrigeBG, null, true, forrigeInntektskategori,
                true, forrigeFastsatt, null);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel = buildArbeidstakerAndel(arbId, andelsnr, periode, null, false, Inntektskategori.ARBEIDSTAKER,
                false, null, null);

        input = BeregningsgrunnlagInputTestUtil.lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                new Tuple<>(beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING),
                new Tuple<>(forrigeBG, BeregningsgrunnlagTilstand.FASTSATT_INN));

        final boolean nyAndel = false;
        final boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndel(andel, arbId, andelsnr, nyAndel, lagtTilAvSaksbehandler, refusjon, fastsatt, inntektskategori);

        final boolean nyAndel2 = false;
        final boolean lagtTilAvSaksbehandler2 = true;
        Integer refusjon2 = 3000;
        Integer fastsatt2 = 20000;
        Inntektskategori inntektskategori2 = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
        FordelBeregningsgrunnlagAndelDto fordeltAndel2 = lagFordeltAndel(null, arbId, andelsnr2, nyAndel2, lagtTilAvSaksbehandler2, refusjon2, fastsatt2, inntektskategori2);
        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(List.of(fordeltAndel, fordeltAndel2), SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);
        BeregningsgrunnlagDto nyttBG = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();

        // Assert
        double totalRefusjon = refusjon + refusjon2;
        double totalFastsatt = fastsatt + fastsatt2;

        assertThat(nyttBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(nyttBG.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);

        List<BeregningsgrunnlagPrStatusOgAndelDto> eksisterendeAndel = nyttBG.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr)).collect(Collectors.toList());

        Assertions.assertThat(eksisterendeAndel).hasSize(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(null))
                .isEqualByComparingTo(Beløp.fra(BigDecimal.valueOf(fastsatt / totalFastsatt * totalRefusjon * 12)));
        assertThat(eksisterendeAndel.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndelDto> andelLagtTil = nyttBG.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());

        Assertions.assertThat(andelLagtTil).hasSize(1);
        assertThat(andelLagtTil.get(0).getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(andelLagtTil.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(null))
                .isEqualByComparingTo(Beløp.fra(BigDecimal.valueOf(fastsatt2 / totalFastsatt * totalRefusjon * 12)));
        assertThat(andelLagtTil.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt2 * 12));
        assertThat(andelLagtTil.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori2);
        assertThat(andelLagtTil.get(0).getBeregningsperiodeFom()).isEqualTo(eksisterendeAndel.get(0).getBeregningsperiodeFom());
        assertThat(andelLagtTil.get(0).getBeregningsperiodeTom()).isEqualTo(eksisterendeAndel.get(0).getBeregningsperiodeTom());
    }


    @Test
    public void skal_ikkje_legge_til_slettet_andel_ved_tilbakehopp_til_steg_før_KOFAKBER() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        Long andelsnr2 = 2L;
        var forrigeFastsatt = Beløp.fra(200000);
        Inntektskategori forrigeInntektskategori = Inntektskategori.SJØMANN;
        BeregningsgrunnlagDto forrigeBG = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periodeForrigeBG = lagPeriode(forrigeBG, SKJÆRINGSTIDSPUNKT, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel = buildArbeidstakerAndel(arbId, andelsnr, periodeForrigeBG, null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        buildArbeidstakerAndel(arbId, andelsnr2, periodeForrigeBG, null, true, forrigeInntektskategori,
                true, forrigeFastsatt, null);

        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT, null);
        buildArbeidstakerAndel(arbId, andelsnr, periode, null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);

        input = BeregningsgrunnlagInputTestUtil.lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                new Tuple<>(beregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT),
                new Tuple<>(forrigeBG, BeregningsgrunnlagTilstand.FASTSATT_INN));

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndel(andel, arbId, andelsnr, nyAndel, lagtTilAvSaksbehandler, refusjon, fastsatt, inntektskategori);

        FordelBeregningsgrunnlagPeriodeDto endretPeriode = new FordelBeregningsgrunnlagPeriodeDto(List.of(fordeltAndel), SKJÆRINGSTIDSPUNKT, null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(singletonList(endretPeriode));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);
        BeregningsgrunnlagDto nyttBG = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
        // Assert
        assertThat(nyttBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(nyttBG.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);

        List<BeregningsgrunnlagPrStatusOgAndelDto> eksisterendeAndel = nyttBG.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr)).collect(Collectors.toList());

        Assertions.assertThat(eksisterendeAndel).hasSize(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(null))
                .isEqualByComparingTo(Beløp.fra(refusjon * 12));
        assertThat(eksisterendeAndel.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndelDto> andelLagtTil = nyttBG.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler)
                .collect(Collectors.toList());

        Assertions.assertThat(andelLagtTil).isEmpty();
    }


    @Test
    public void skal_sette_verdier_på_andeler_for_tilbakehopp_til_steg_før_KOFAKBER_med_nye_andeler_og_eksisterende_andeler_i_ulike_arbeidsforhold() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;
        Long andelsnr2 = 2L;
        Long andelsnr3 = 3L;

        var forrigeFastsatt = Beløp.fra(200000);
        Inntektskategori forrigeInntektskategori = Inntektskategori.SJØMANN;
        BeregningsgrunnlagDto forrigeBG = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periodeForrigeBG = lagPeriode(forrigeBG, SKJÆRINGSTIDSPUNKT, null);
        buildArbeidstakerAndel(arbId, andelsnr, periodeForrigeBG,
                null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        buildArbeidstakerAndel(arbId, andelsnr2, periodeForrigeBG,
                null, true, forrigeInntektskategori, true, forrigeFastsatt, null);

        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
                .build(beregningsgrunnlag);
        buildArbeidstakerAndel(arbId, andelsnr, periode1,
                null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        BeregningsgrunnlagPeriodeDto periode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT.plusMonths(2), null)
                .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto andel = buildArbeidstakerAndel(arbId, andelsnr, periode2,
                null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);
        BeregningsgrunnlagPrStatusOgAndelDto andel3 = buildArbeidstakerAndel(arbId2, andelsnr3, periode2,
                null, false, Inntektskategori.ARBEIDSTAKER, false, null, null);

        input = BeregningsgrunnlagInputTestUtil.lagHåndteringInputMedBeregningsgrunnlag(koblingReferanse,
                new Tuple<>(beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING),
                new Tuple<>(forrigeBG, BeregningsgrunnlagTilstand.FASTSATT_INN));


        final boolean nyAndel = false;
        final boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel = lagFordeltAndel(andel, arbId, andelsnr, nyAndel, lagtTilAvSaksbehandler, refusjon, fastsatt, inntektskategori);

        final boolean nyAndel2 = false;
        final boolean lagtTilAvSaksbehandler2 = true;
        Integer refusjon2 = 3000;
        Integer fastsatt2 = 20000;
        Inntektskategori inntektskategori2 = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
        FordelBeregningsgrunnlagAndelDto fordeltAndel2 = lagFordeltAndel(null, arbId, andelsnr2, nyAndel2, lagtTilAvSaksbehandler2, refusjon2, fastsatt2, inntektskategori2);

        final boolean nyAndel3 = true;
        final boolean lagtTilAvSaksbehandler3 = true;
        Integer refusjon3 = 2000;
        Integer fastsatt3 = 30000;
        Inntektskategori inntektskategori3 = Inntektskategori.JORDBRUKER;
        FordelBeregningsgrunnlagAndelDto fordeltAndel3 = lagFordeltAndel(null, arbId, andelsnr, nyAndel3, lagtTilAvSaksbehandler3, refusjon3, fastsatt3, inntektskategori3);

        FordelBeregningsgrunnlagPeriodeDto endretPeriode1 = new FordelBeregningsgrunnlagPeriodeDto(List.of(fordeltAndel3, fordeltAndel, fordeltAndel2), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1));

        final boolean nyAndel4 = false;
        final boolean lagtTilAvSaksbehandler4 = false;
        Integer refusjon4 = 10000;
        Integer fastsatt4 = 40000;
        Inntektskategori inntektskategori4 = Inntektskategori.SJØMANN;
        FordelBeregningsgrunnlagAndelDto fordeltAndel4 = lagFordeltAndel(andel3, arbId2, andelsnr3, nyAndel4, lagtTilAvSaksbehandler4, refusjon4, fastsatt4, inntektskategori4);
        FordelBeregningsgrunnlagPeriodeDto endretPeriode2 = new FordelBeregningsgrunnlagPeriodeDto(List.of(fordeltAndel3, fordeltAndel, fordeltAndel4), SKJÆRINGSTIDSPUNKT.plusMonths(2), null);
        FordelBeregningsgrunnlagDto dto = new FordelBeregningsgrunnlagDto(List.of(endretPeriode2, endretPeriode1));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FordelBeregningsgrunnlagHåndterer.håndter(dto, input);
        BeregningsgrunnlagDto nyttBG = grunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();

        // Assert
        double totalRefusjon1 = refusjon + refusjon2 + refusjon3;
        double totalFastsatt1 = fastsatt + fastsatt2 + fastsatt3;
        double totalRefusjon2 = refusjon + refusjon3;
        double totalFastsatt2 = fastsatt + fastsatt3;

        assertThat(nyttBG.getBeregningsgrunnlagPerioder()).hasSize(2);

        BeregningsgrunnlagPeriodeDto periode1Oppdatert = nyttBG.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPeriodeDto periode2Oppdatert = nyttBG.getBeregningsgrunnlagPerioder().get(1);

        assertThat(periode1Oppdatert.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);

        List<BeregningsgrunnlagPrStatusOgAndelDto> eksisterendeAndel = periode1Oppdatert
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.matchUtenInntektskategori(andel) && a.getGjeldendeInntektskategori().equals(fordeltAndel.getFastsatteVerdier().getInntektskategori())).collect(Collectors.toList());

        Assertions.assertThat(eksisterendeAndel).hasSize(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).map(Beløp::verdi).orElse(null))
                .isEqualByComparingTo(BigDecimal.valueOf(fastsatt / totalFastsatt1 * totalRefusjon1 * 12));
        assertThat(eksisterendeAndel.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndelDto> andelLagtTil = periode1Oppdatert
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());

        Assertions.assertThat(andelLagtTil).hasSize(2);
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> fraForrige = andelLagtTil.stream().filter(lagtTil -> lagtTil.getGjeldendeInntektskategori().equals(inntektskategori2)).findFirst();
        Assertions.assertThat(fraForrige.isPresent()).isTrue();
        assertThat(fraForrige.get().getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(fraForrige.get().getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).map(Beløp::verdi).orElse(null))
                .isEqualByComparingTo(BigDecimal.valueOf(fastsatt2 / totalFastsatt1 * totalRefusjon1 * 12));
        assertThat(fraForrige.get().getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt2 * 12));
        assertThat(fraForrige.get().getGjeldendeInntektskategori()).isEqualTo(inntektskategori2);

        Optional<BeregningsgrunnlagPrStatusOgAndelDto> ny = andelLagtTil.stream().filter(lagtTil -> lagtTil.getGjeldendeInntektskategori().equals(inntektskategori3)).findFirst();
        Assertions.assertThat(ny.isPresent()).isTrue();
        assertThat(ny.get().getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(ny.get().getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).map(Beløp::verdi).orElse(null))
                .isEqualByComparingTo(BigDecimal.valueOf(fastsatt3 / totalFastsatt1 * totalRefusjon1 * 12));
        assertThat(ny.get().getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt3 * 12));
        assertThat(ny.get().getGjeldendeInntektskategori()).isEqualTo(inntektskategori3);

        assertThat(periode2Oppdatert.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);

        List<BeregningsgrunnlagPrStatusOgAndelDto> eksisterendeAndel2 = periode2Oppdatert
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.matchUtenInntektskategori(andel) && a.getGjeldendeInntektskategori().equals(fordeltAndel.getFastsatteVerdier().getInntektskategori())).collect(Collectors.toList());
        Assertions.assertThat(eksisterendeAndel2).hasSize(1);
        assertThat(eksisterendeAndel2.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).map(Beløp::verdi).orElse(null))
                .isEqualByComparingTo(BigDecimal.valueOf(fastsatt / totalFastsatt2 * totalRefusjon2 * 12));
        assertThat(eksisterendeAndel2.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt * 12));
        assertThat(eksisterendeAndel2.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndelDto> eksisterendeAndel3 = periode2Oppdatert
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.matchUtenInntektskategori(andel3) && a.getGjeldendeInntektskategori().equals(fordeltAndel4.getFastsatteVerdier().getInntektskategori())).collect(Collectors.toList());
        Assertions.assertThat(eksisterendeAndel3).hasSize(1);
        assertThat(eksisterendeAndel3.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).map(Beløp::verdi).orElse(null))
                .isEqualByComparingTo(BigDecimal.valueOf(refusjon4 * 12));
        assertThat(eksisterendeAndel3.get(0).getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt4 * 12));
        assertThat(eksisterendeAndel3.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori4);


        List<BeregningsgrunnlagPrStatusOgAndelDto> andelLagtTil2 = periode2Oppdatert
                .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());

        Assertions.assertThat(andelLagtTil2).hasSize(1);
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> fraForrige2 = andelLagtTil2.stream()
                .filter(lagtTil -> lagtTil.getGjeldendeInntektskategori().equals(inntektskategori2)).findFirst();
        Assertions.assertThat(fraForrige2.isPresent()).isFalse();

        Optional<BeregningsgrunnlagPrStatusOgAndelDto> ny2 = andelLagtTil2.stream().filter(lagtTil -> lagtTil.getGjeldendeInntektskategori().equals(inntektskategori3)).findFirst();
        Assertions.assertThat(ny2.isPresent()).isTrue();
        assertThat(ny2.get().getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(ny2.get().getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).map(Beløp::verdi).orElse(null))
                .isEqualByComparingTo(BigDecimal.valueOf(fastsatt3 / totalFastsatt2 * totalRefusjon2 * 12));
        assertThat(ny2.get().getManueltFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt3 * 12));
        assertThat(ny2.get().getGjeldendeInntektskategori()).isEqualTo(inntektskategori3);
    }

}

