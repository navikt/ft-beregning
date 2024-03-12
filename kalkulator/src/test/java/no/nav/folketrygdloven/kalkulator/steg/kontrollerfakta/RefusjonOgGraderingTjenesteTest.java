package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelBeregningsgrunnlagTilfelleInput;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelBeregningsgrunnlagTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

public class RefusjonOgGraderingTjenesteTest {

    private static final String ORG_NUMMER = "974652269";
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.of(2018, 9, 30);
    private static final Beløp GRUNNBELØP = Beløp.fra(100_000);
    private Arbeidsgiver arbeidsgiver1 = Arbeidsgiver.virksomhet(ORG_NUMMER);
    private Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet("999999999");

    @Test
    public void returnererTrueForFLMedGraderingSomTilkommer() {
        // Arrange
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 1000, periode1, false, null, Beløp.fra(10), InternArbeidsforholdRefDto.nullRef(), false);
        lagFLAndel(periode1);

        // Act
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.FRILANSER)
            .medGradering(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusWeeks(18).minusDays(1), 50)
            .build());
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, aktivitetGradering, List.of());

        // Assert
        assertThat(manuellBehandlingForEndringAvBG).containsValue(FordelingTilfelle.NY_AKTIVITET);
    }

    @Test
    public void returnererTrueForSNMedGraderingSomTilkommer() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 1000, periode1, false, null, Beløp.fra(10), InternArbeidsforholdRefDto.nullRef(), false);
        lagSNAndel(periode1, true);

        // Act
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(fom, tom, 50)
            .build());

        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, aktivitetGradering, List.of());

        // Assert
        assertThat(manuellBehandlingForEndringAvBG).containsValue(FordelingTilfelle.NY_AKTIVITET);
    }

    @Test
    public void returnererFalseForNyInntektsmeldingUtenRefusjonskrav() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        BeregningsgrunnlagDto beregningsgrunnlag = lagBg();
        BeregningsgrunnlagPeriodeDto p1 = lagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusWeeks(2), Collections.emptyList(), beregningsgrunnlag);
        lagAndel(Arbeidsgiver.virksomhet(orgnr), null, p1, false, null, Beløp.fra(10), arbId, false);
        BeregningsgrunnlagPeriodeDto p2 = lagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusWeeks(2).plusDays(1), null, Collections.singletonList(PeriodeÅrsak.GRADERING), beregningsgrunnlag);
        lagAndel(Arbeidsgiver.virksomhet(orgnr), null, p2, false, null, Beløp.fra(10), arbId, false);

        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr, arbId, SKJÆRINGSTIDSPUNKT_BEREGNING);


        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(beregningsgrunnlag,
                AktivitetGradering.INGEN_GRADERING, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();
    }

    // Gradering: Ja
    // Refusjon: Nei
    // Tilkom etter skjæringstidspunktet: Ja
    // Returnerer true
    @Test
    public void returnererTrueForGraderingOgArbeidsforholdetTilkomEtterSkjæringstidpunktet() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        var arbId = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, null, periode1, true, null, Beløp.fra(10), arbId, true);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbId)
                .medGradering(fom, tom, 50)
                .build());

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, aktivitetGradering, List.of());

        // Assert
        assertThat(manuellBehandlingForEndringAvBG).containsValue(FordelingTilfelle.NY_AKTIVITET);
    }

    // Gradering: Ja
    // Refusjon: Nei
    // Tilkom etter skjæringstidspunktet: Nei
    // Total refusjon under 6G
    // Returnerer false
    @Test
    public void returnererTrueForGraderingGjeldendeBruttoBGStørreEnnNullBeregningsgrunnlagsandelAvkortetTilNull() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        String orgnr1 = "123456780";
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, 0);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 0, periode1, false, null, Beløp.fra(10), arbId1, false);
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbId1)
                .medGradering(fom, tom, 50)
                .build());

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, aktivitetGradering, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();
    }

    // Gradering: Ja
    // Refusjon: Nei
    // Total refusjon større enn 6G for alle arbeidsforhold
    // Tilkom etter skjæringstidspunktet: Nei
    // Returnerer True
    @Test
    public void returnererTrueForGraderingGjeldendeBruttoBGLikNullTotalRefusjonStørreEnn6G() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);

        int seksG = GRUNNBELØP.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi()).intValue();
        int refusjon2PerÅr = seksG + 12;
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver2.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, refusjon2PerÅr / 12);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, null, periode1, false, null, Beløp.fra(10), arbId1, false);
        lagAndel(arbeidsgiver2, refusjon2PerÅr, periode1, false, null, Beløp.fra(10), arbId2, false);

        // Act
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbeidsgiver(arbeidsgiver1)
            .medArbeidsforholdRef(arbId1)
            .medGradering(fom, tom, 50)
            .build());
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, aktivitetGradering, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG).containsValue(FordelingTilfelle.TOTALT_REFUSJONSKRAV_STØRRE_ENN_6G);

    }

    // Gradering: Ja
    // Refusjon: Nei
    // Total refusjon mindre enn 6G for alle arbeidsforhold
    // Tilkom etter skjæringstidspunktet: Nei
    // Returnerer True
    @Test
    public void returnererFalseForGraderingGjeldendeBruttoBGLikNullTotalRefusjonMindreEnn6G() {
        // Arrange
        int seksG = GRUNNBELØP.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi()).intValue();
        int refusjon2 = seksG - 12;
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver2.getIdentifikator(), arbId2, SKJÆRINGSTIDSPUNKT_BEREGNING, refusjon2 / 12);
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, null, periode1, false, null, Beløp.fra(10), arbId1, false);
        lagAndel(arbeidsgiver2, refusjon2 / 12, periode1, false, null, Beløp.fra(10), arbId2, false);
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbId1)
                .medGradering(fom, tom, 50)
                .build());

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, aktivitetGradering, List.of(im1, im2));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();

    }
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Returnerer True
    @Test
    public void returnererTrueForGraderingOgRefusjonUtenGjeldendeBG() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1,
            SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 100, periode1, true, null, Beløp.fra(10), arbId1, true);
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbId1)
                .medGradering(fom, tom, 50)
                .build());

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, aktivitetGradering, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG).containsValue(FordelingTilfelle.NY_AKTIVITET);
    }

    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Returnerer False
    @Test
    public void returnererFalseForGraderingOgRefusjon() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1,
            SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 100, periode1, false, null, Beløp.fra(10), arbId1, false);
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbId1)
                .medGradering(fom, tom, 50)
                .build());
        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, aktivitetGradering, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();
    }
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Returnerer True

    @Test
    public void returnererTrueForRefusjonArbfholdTilkomEtterStp() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, 1000);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 1000, periode1, true, null, Beløp.fra(10), arbId1, true);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, AktivitetGradering.INGEN_GRADERING, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG).containsValue(FordelingTilfelle.NY_AKTIVITET);
    }

    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Returnerer False
    @Test
    public void returnererFalseForRefusjonGjeldendeBruttoBGStørreEnn0() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, 1000);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 1000, periode1, false, null, Beløp.fra(10), arbId1, false);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, AktivitetGradering.INGEN_GRADERING, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();
    }


    @Test
    public void returnererTrueNårGradertNæringMedArbeidstakerTotalRefusjonUnder6GOgBGOver6G() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var seksG = GRUNNBELØP.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());
        int refusjon = (seksG.intValue() - 1) / 12;
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, refusjon);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, refusjon*12, periode1, false, null, seksG.adder(Beløp.fra(1)), arbId1, false);
        lagSNAndel(periode1, false);
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(fom, tom, 50)
            .build());

        // Act
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(bg, aktivitetGradering, List.of(im1), Collections.emptyList());
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> andelerMedTilfeller = FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandlingForPeriode(periode1, fordelingInput);

        // Assert
        assertThat(andelerMedTilfeller.containsValue(FordelingTilfelle.GRADERT_ANDEL_SOM_VILLE_HA_BLITT_AVKORTET_TIL_0)).isTrue();
    }

    @Test
    public void returnererTrueForSNMedGraderingUtenBeregningsgrunnlag() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagSNAndel(periode1, 0, false);

        // Act
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(fom, tom, 50)
            .build());

        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, aktivitetGradering, List.of());

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.containsValue(FordelingTilfelle.FORESLÅTT_BG_PÅ_GRADERT_ANDEL_ER_0)).isTrue();
    }


    private Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> vurderManuellBehandling(BeregningsgrunnlagDto bg, AktivitetGradering aktivitetGradering, Collection<InntektsmeldingDto> inntektsmeldinger) {
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(bg, aktivitetGradering, inntektsmeldinger, Collections.emptyList());
        return fordelingInput.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .map(p -> FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandlingForPeriode(p, fordelingInput))
                .filter(r -> !r.isEmpty())
                .findFirst().orElse(Collections.emptyMap());
    }

    private BeregningsgrunnlagPeriodeDto lagPeriode(BeregningsgrunnlagDto bg) {
        return BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .build(bg);
    }

    private BeregningsgrunnlagPeriodeDto lagPeriode(LocalDate fom, LocalDate tom, Collection<PeriodeÅrsak> periodeÅrsaker, BeregningsgrunnlagDto bg) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(fom, tom);
        periodeÅrsaker.forEach(builder::leggTilPeriodeÅrsak);
        return builder
            .build(bg);
    }

    private BeregningsgrunnlagDto lagBg() {
        return BeregningsgrunnlagDto.builder().leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
    }

    private void lagAndel(Arbeidsgiver arbeidsgiver,
                          Integer refusjon2,
                          BeregningsgrunnlagPeriodeDto periode1,
                          boolean tilkomEtter,
                          Beløp overstyrtPrÅr,
                          Beløp beregnetPrÅr,
                          InternArbeidsforholdRefDto arbeidsforholdRef,
                          boolean tilkommer) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medKilde(tilkommer ? AndelKilde.PROSESS_PERIODISERING : AndelKilde.PROSESS_START)
            .medBeregnetPrÅr(beregnetPrÅr)
            .medOverstyrtPrÅr(overstyrtPrÅr)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbeidsforholdRef)
                .medRefusjonskravPrÅr(refusjon2 == null ? null : Beløp.fra(refusjon2), Utfall.GODKJENT)
                .medArbeidsperiodeFom(tilkomEtter ? periode1.getBeregningsgrunnlagPeriodeFom() : SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12)))
            .build(periode1);
    }

    private void lagFLAndel(BeregningsgrunnlagPeriodeDto periode1) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medKilde(AndelKilde.PROSESS_PERIODISERING)
            .build(periode1);
    }

    private void lagSNAndel(BeregningsgrunnlagPeriodeDto periode1, boolean tilkommer) {
        lagSNAndel(periode1, 10, tilkommer);
    }

    private void lagSNAndel(BeregningsgrunnlagPeriodeDto periode1, int beregnetPrÅr, boolean tilkommer) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medKilde(tilkommer ? AndelKilde.PROSESS_PERIODISERING : AndelKilde.PROSESS_START)
            .medBeregnetPrÅr(Beløp.fra(beregnetPrÅr))
            .build(periode1);
    }

}
