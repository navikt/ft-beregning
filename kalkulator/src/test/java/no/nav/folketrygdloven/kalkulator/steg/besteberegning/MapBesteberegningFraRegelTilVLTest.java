package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelseAktivitetType;
import no.nav.folketrygdloven.besteberegning.modell.output.AktivitetNøkkel;
import no.nav.folketrygdloven.besteberegning.modell.output.BeregnetMånedsgrunnlag;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetAndel;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetGrunnlag;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegningOutput;
import no.nav.folketrygdloven.besteberegning.modell.output.Inntekt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class MapBesteberegningFraRegelTilVLTest {

    @Test
    public void en_andel_bb_mappes_mot_matchende_andel_bg() {
        // Arrange
        String arbRef = UUID.randomUUID().toString();
        var bgp = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(LocalDate.now(), null);
        var andeler = Arrays.asList(byggBgAndel(AktivitetStatus.ARBEIDSTAKER, "123", arbRef));
        andeler.forEach(bgp::leggTilBeregningsgrunnlagPrStatusOgAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(LocalDate.now());
        var bbAndeler = Arrays.asList(lagBBAndel("123", arbRef, 400000));
        bg.leggTilBeregningsgrunnlagPeriode(bgp);

        // Act
        BeregningsgrunnlagDto mappedBg = mapTilBeregningsgrunnlag(true, bg, bbAndeler);

        // Assert
        assertAntalAndeler(mappedBg, 1);
        assertAndel(mappedBg, AktivitetStatus.ARBEIDSTAKER, 400000, "123", arbRef);
    }

    @Test
    public void en_andel_bb_mappes_mot_flere_arbeidsandeler() {
        // Arrange
        String arbRef = UUID.randomUUID().toString();
        String arbRef2 = UUID.randomUUID().toString();
        var bgp = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(LocalDate.now(), null);
        var andeler = Arrays.asList(byggBgAndel(AktivitetStatus.ARBEIDSTAKER, "123", arbRef),
                byggBgAndel(AktivitetStatus.ARBEIDSTAKER, "123", arbRef2));
        andeler.forEach(bgp::leggTilBeregningsgrunnlagPrStatusOgAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(LocalDate.now());
        var bbAndeler = List.of(lagBBAndel("123", null, 400000));
        bg.leggTilBeregningsgrunnlagPeriode(bgp);

        // Act
        BeregningsgrunnlagDto mappedBg = mapTilBeregningsgrunnlag(true, bg, bbAndeler);

        // Assert
        assertAntalAndeler(mappedBg, 2);
        assertAndel(mappedBg, AktivitetStatus.ARBEIDSTAKER, 200000, "123", arbRef);
        assertAndel(mappedBg, AktivitetStatus.ARBEIDSTAKER, 200000, "123", arbRef2);
    }

    @Test
    public void to_andel_bb_mappes_mot_matchende_andel_bg() {
        // Arrange
        String arbRef = UUID.randomUUID().toString();
        var bgp = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(LocalDate.now(), null);
        var andeler = Arrays.asList(byggBgAndel(AktivitetStatus.ARBEIDSTAKER, "123", arbRef));
        andeler.forEach(bgp::leggTilBeregningsgrunnlagPrStatusOgAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(LocalDate.now());
        var bbAndeler = Arrays.asList(lagBBAndel("123", arbRef, 400000), lagBBAndel("321", null, 57000));
        bg.leggTilBeregningsgrunnlagPeriode(bgp);

        // Act
        BeregningsgrunnlagDto mappedBg = mapTilBeregningsgrunnlag(true, bg, bbAndeler);

        // Assert
        assertAntalAndeler(mappedBg, 2);
        assertAndel(mappedBg, AktivitetStatus.ARBEIDSTAKER, 400000, "123", arbRef);
        assertAndel(mappedBg, AktivitetStatus.DAGPENGER, 57000);
    }

    @Test
    public void skal_ikke_mappe_andeler_når_det_ikke_skal_brukes_besteberegning() {
        // Arrange
        String arbRef = UUID.randomUUID().toString();
        var bgp = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(LocalDate.now(), null);
        var andeler = Arrays.asList(byggBgAndel(AktivitetStatus.ARBEIDSTAKER, "123", arbRef));
        andeler.forEach(bgp::leggTilBeregningsgrunnlagPrStatusOgAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(LocalDate.now());
        var bbAndeler = Arrays.asList(lagBBAndel("123", arbRef, 400000));
        bg.leggTilBeregningsgrunnlagPeriode(bgp);

        // Act
        BeregningsgrunnlagDto mappedBg = mapTilBeregningsgrunnlag(false, bg, bbAndeler);

        // Assert
        assertThat(mappedBg).isEqualTo(bg.build());
    }

    @Test
    public void bg_andel_uten_bb_andel_skal_få_besteberegnet_lik_0() {
        // Arrange
        String arbRef = UUID.randomUUID().toString();
        var bgp = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(LocalDate.now(), null);
        var bgAndeler = Arrays.asList(byggBgAndel(AktivitetStatus.ARBEIDSTAKER, "123", arbRef));
        bgAndeler.forEach(bgp::leggTilBeregningsgrunnlagPrStatusOgAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(LocalDate.now());
        var bbAndeler = Arrays.asList(lagBBAndel("321", arbRef, 400000));
        bg.leggTilBeregningsgrunnlagPeriode(bgp);

        // Act
        BeregningsgrunnlagDto mappedBg = mapTilBeregningsgrunnlag(true, bg, bbAndeler);

        // Assert
        assertAntalAndeler(mappedBg, 2);
        assertAndel(mappedBg, AktivitetStatus.ARBEIDSTAKER, 0, "123", arbRef);
        assertAndel(mappedBg, AktivitetStatus.DAGPENGER, 400000);
    }

    @Test
    public void bb_ytelse_for_arbeid_mappes_til_ytelseandel() {
        // Arrange
        var arbRef = UUID.randomUUID().toString();
        var bgp = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(LocalDate.now(), null);
        var bgAndeler = Arrays.asList(byggBgAndel(AktivitetStatus.ARBEIDSTAKER, "123", arbRef), byggBgAndel(AktivitetStatus.DAGPENGER));
        bgAndeler.forEach(bgp::leggTilBeregningsgrunnlagPrStatusOgAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(LocalDate.now());
        var bbAndeler = Arrays.asList(lagBBAndel(Aktivitet.SYKEPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_ARBEID, 350000),
                lagBBAndel(Aktivitet.DAGPENGEMOTTAKER, 100000));
        bg.leggTilBeregningsgrunnlagPeriode(bgp);

        // Act
        var mappedBg = mapTilBeregningsgrunnlag(true, bg, bbAndeler);

        // Assert
        assertAntalAndeler(mappedBg, 2);
        assertAndel(mappedBg, AktivitetStatus.ARBEIDSTAKER, 350000, "123", arbRef);
        assertAndel(mappedBg, AktivitetStatus.DAGPENGER, 100000);
    }

    @Test
    public void skal_mappe_seks_beste_måneder() {
        // Arrange
        var bbOutput = new BesteberegningOutput();
        bbOutput.setSkalBeregnesEtterSeksBesteMåneder(true);
        var startÅrMåned = YearMonth.of(2021, 1);
        var inntektArbeid = new Inntekt(AktivitetNøkkel.forOrganisasjon("123", UUID.randomUUID().toString()), BigDecimal.valueOf(45000));
        var inntektYtelse = new Inntekt(AktivitetNøkkel.forType(Aktivitet.DAGPENGEMOTTAKER), BigDecimal.valueOf(10000));
        var inntekter = Arrays.asList(inntektArbeid, inntektYtelse);
        List<BeregnetMånedsgrunnlag> bbMåneder = new ArrayList<>();
        for (int i = 0; i<6; i++) {
            YearMonth periode = startÅrMåned.plusMonths(i);
            var bbMåned = new BeregnetMånedsgrunnlag(periode);
            inntekter.forEach(bbMåned::leggTilInntekt);
            bbMåneder.add(bbMåned);
        }
        bbOutput.setBesteMåneder(bbMåneder);
        var besteMåneder = MapBesteberegningFraRegelTilVL.mapSeksBesteMåneder(bbOutput);
        assertThat(besteMåneder).isNotNull();
        assertThat(besteMåneder).hasSize(6);
        besteMåneder.forEach(måned -> {
            assertThat(måned.getInntekter()).hasSize(2);
            var arbeidsandel = måned.getInntekter().stream().filter(innt -> innt.getArbeidsgiver().getIdentifikator().equals("123")).findFirst();
            assertThat(arbeidsandel).isPresent();
            assertThat(arbeidsandel.get().getInntekt().intValue()).isEqualTo(45000);

            var ytelseAndel = måned.getInntekter().stream().filter(innt -> OpptjeningAktivitetType.DAGPENGER.equals(innt.getOpptjeningAktivitetType())).findFirst();
            assertThat(ytelseAndel).isPresent();
            assertThat(ytelseAndel.get().getInntekt().intValue()).isEqualTo(10000);
        });
    }

    private void assertAndel(BeregningsgrunnlagDto mappedBg, AktivitetStatus status, int inntekt) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = hentAndeler(mappedBg);
        var matchendeAndel = andeler.stream()
                .filter(a -> a.getAktivitetStatus().equals(status))
                .findFirst();
        assertThat(matchendeAndel).isPresent();
        assertThat(matchendeAndel.get().getBesteberegningPrÅr()).isNotNull();
        assertThat(matchendeAndel.get().getBesteberegningPrÅr().intValue()).isEqualTo(inntekt);
        assertThat(matchendeAndel.get().getBruttoPrÅr()).isNotNull();
        assertThat(matchendeAndel.get().getBruttoPrÅr().intValue()).isEqualTo(inntekt);
    }

    private void assertAntalAndeler(BeregningsgrunnlagDto mappedBg, int antall) {
        assertThat(hentAndeler(mappedBg)).hasSize(antall);
    }

    private void assertAndel(BeregningsgrunnlagDto mappedBg, AktivitetStatus status, int inntekt, String orgnr, String arbRef) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = hentAndeler(mappedBg);
        var matchendeAndel = andeler.stream()
                .filter(a -> a.getAktivitetStatus().equals(status))
                .filter(a -> a.getArbeidsgiver().get().getIdentifikator().equals(orgnr))
                .filter(a -> a.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()).gjelderFor(InternArbeidsforholdRefDto.ref(arbRef)))
                .findFirst();
        assertThat(matchendeAndel).isPresent();
        assertThat(matchendeAndel.get().getBesteberegningPrÅr()).isNotNull();
        assertThat(matchendeAndel.get().getBesteberegningPrÅr().intValue()).isEqualTo(inntekt);
        assertThat(matchendeAndel.get().getBruttoPrÅr()).isNotNull();
        assertThat(matchendeAndel.get().getBruttoPrÅr().intValue()).isEqualTo(inntekt);

    }

    private List<BeregningsgrunnlagPrStatusOgAndelDto> hentAndeler(BeregningsgrunnlagDto mappedBg) {
        return mappedBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
    }


    private BeregningsgrunnlagDto mapTilBeregningsgrunnlag(boolean skalBeregnesEtterBesteberegning, BeregningsgrunnlagDto.Builder bg, List<BesteberegnetAndel> bbAndeler) {
        BeregningsgrunnlagGrunnlagDto gr = BeregningsgrunnlagGrunnlagDtoBuilder.nytt()
                .medBeregningsgrunnlag(bg.build())
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);
        BesteberegningOutput bbOutput = new BesteberegningOutput();
        bbOutput.setBesteberegnetGrunnlag(new BesteberegnetGrunnlag(bbAndeler));
        bbOutput.setSkalBeregnesEtterSeksBesteMåneder(skalBeregnesEtterBesteberegning);
        return MapBesteberegningFraRegelTilVL.mapTilBeregningsgrunnlag(gr, bbOutput);
    }

    private BesteberegnetAndel lagBBAndel(String orgnr, String arbId, int beløp) {
        return new BesteberegnetAndel(AktivitetNøkkel.forOrganisasjon(orgnr, arbId), BigDecimal.valueOf(beløp));
    }

    private BesteberegnetAndel lagBBAndel(Aktivitet aktivitet, YtelseAktivitetType ytelseaktivitet, int beløp) {
        return new BesteberegnetAndel(AktivitetNøkkel.forYtelseFraSammenligningsfilter(aktivitet, ytelseaktivitet), BigDecimal.valueOf(beløp));
    }

    private BesteberegnetAndel lagBBAndel(Aktivitet aktivitet, int beløp) {
        return new BesteberegnetAndel(AktivitetNøkkel.forType(aktivitet), BigDecimal.valueOf(beløp));
    }


    private BeregningsgrunnlagPrStatusOgAndelDto.Builder byggBgAndel(AktivitetStatus aktivitetStatus, String orgnr, String arbId) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(aktivitetStatus)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsforholdRef(arbId)
                        .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr)));
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder byggBgAndel(AktivitetStatus aktivitetStatus) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(aktivitetStatus);
    }

}
