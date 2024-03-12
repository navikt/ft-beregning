package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

class MapFraFordelingsmodellTest {
    private static final LocalDate STP = LocalDate.of(2021,10,1);
    private BeregningsgrunnlagDto.Builder bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(STP);

    @Test
    public void skal_teste_mapping_av_enkel_andel() {
        // Arrange
        var ref = UUID.randomUUID();
        lagBGPeriode(STP, STP.plusMonths(1),
                lagBGAndel(1L, AktivitetStatus.ARBEIDSTAKER,
                        "123", ref.toString(), Inntektskategori.ARBEIDSTAKER));
        var regelPeriode = lagRegelPeriode(STP, STP.plusMonths(1),
                lagRegelAndel(1L, AktivitetStatus.ARBEIDSTAKER,
                        "123", ref.toString(), 100000, null, Inntektskategori.ARBEIDSTAKER));
        var mappetBG = map(regelPeriode);

        assertThat(mappetBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        var andeler = mappetBG.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertAndel(andeler, 1L, 100000, null, Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void skal_teste_mapping_av_to_andeler_blir_tre() {
        // Arrange
        var ref = UUID.randomUUID();
        lagBGPeriode(STP, STP.plusMonths(1),
                lagBGAndel(1L, AktivitetStatus.ARBEIDSTAKER,
                        "123", ref.toString(), Inntektskategori.ARBEIDSTAKER),
                lagBGAndel(2L, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE,
                        null, null, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE));
        var regelPeriode = lagRegelPeriode(STP, STP.plusMonths(1),
                lagRegelAndel(1L, AktivitetStatus.ARBEIDSTAKER,
                        "123", ref.toString(),
                        100000, null, Inntektskategori.ARBEIDSTAKER),
                lagRegelAndel(2L, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE,
                        null, null,
                        0, null, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE),
                lagRegelAndel(null, AktivitetStatus.ARBEIDSTAKER,
                        "123", ref.toString(),
                        100000, null, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE).erNytt(true));
        var mappetBG = map(regelPeriode);

        assertThat(mappetBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        var andeler = mappetBG.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(3);
        assertAndel(andeler, 1L, 100000, null, Inntektskategori.ARBEIDSTAKER);
        assertAndel(andeler, 2L, 0, null, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertAndel(andeler, 3L, 100000, null, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    public void skal_teste_mapping_av_tre_andeler_blir_fire_med_ny_andel_frilans() {
        // Arrange
        lagBGPeriode(STP, STP.plusMonths(1),
                lagBGAndel(1L, AktivitetStatus.BRUKERS_ANDEL,
                        null, null, Inntektskategori.ARBEIDSTAKER),
                lagBGAndel(2L, AktivitetStatus.BRUKERS_ANDEL,
                        null, null, Inntektskategori.FRILANSER),
                lagBGAndel(3L, AktivitetStatus.FRILANSER,
                        null, null, Inntektskategori.UDEFINERT));
        var regelPeriode = lagRegelPeriode(STP, STP.plusMonths(1),
                lagRegelAndel(1L, AktivitetStatus.BRUKERS_ANDEL,
                        null, null,
                        0, null, Inntektskategori.ARBEIDSTAKER),
                lagRegelAndel(2L, AktivitetStatus.BRUKERS_ANDEL,
                        null, null,
                        0, null, Inntektskategori.FRILANSER),
                lagRegelAndel(3L, AktivitetStatus.FRILANSER,
                        null, null,
                        100000, null, Inntektskategori.ARBEIDSTAKER),
                lagRegelAndel(null, AktivitetStatus.FRILANSER,
                        null, null,
                        100000, null, Inntektskategori.FRILANSER).erNytt(true));
        var mappetBG = map(regelPeriode);

        assertThat(mappetBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        var andeler = mappetBG.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(4);
        assertAndel(andeler, 1L, 0, null, Inntektskategori.ARBEIDSTAKER);
        assertAndel(andeler, 2L, 0, null, Inntektskategori.FRILANSER);
        assertAndel(andeler, 3L, 100000, null, Inntektskategori.ARBEIDSTAKER);
        assertAndel(andeler, 4L, 100000, null, Inntektskategori.FRILANSER);
    }

    @Test
    public void skal_teste_mapping_av_tre_andeler_blir_fem() {
        // Arrange
        var ref = UUID.randomUUID();
        var ref2 = UUID.randomUUID();

        lagBGPeriode(STP, STP.plusMonths(1),
                lagBGAndel(1L, AktivitetStatus.ARBEIDSTAKER,
                        "123", ref.toString(), Inntektskategori.ARBEIDSTAKER),
                lagBGAndel(2L, AktivitetStatus.ARBEIDSTAKER,
                        "321", ref2.toString(), Inntektskategori.ARBEIDSTAKER),
                lagBGAndel(3L, AktivitetStatus.DAGPENGER,
                        null, null, Inntektskategori.DAGPENGER));
        var regelPeriode = lagRegelPeriode(STP, STP.plusMonths(1),
                lagRegelAndel(1L, AktivitetStatus.ARBEIDSTAKER,
                        "123", ref.toString(),
                        100000, 100000, Inntektskategori.ARBEIDSTAKER),
                lagRegelAndel(null, AktivitetStatus.ARBEIDSTAKER,
                        "123", ref.toString(),
                        100000, 100000, Inntektskategori.DAGPENGER).erNytt(true),
                lagRegelAndel(2L, AktivitetStatus.ARBEIDSTAKER,
                        "321", ref2.toString(),
                        200000, 200000, Inntektskategori.ARBEIDSTAKER),
                lagRegelAndel(null, AktivitetStatus.ARBEIDSTAKER,
                        "321", ref2.toString(),
                        200000, 200000, Inntektskategori.DAGPENGER).erNytt(true),
                lagRegelAndel(3L, AktivitetStatus.DAGPENGER,
                        null, null,
                        0, null, Inntektskategori.DAGPENGER));
        var mappetBG = map(regelPeriode);

        assertThat(mappetBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        var andeler = mappetBG.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(5);
        assertAndel(andeler, 1L, 100000, 100000, Inntektskategori.ARBEIDSTAKER);
        assertAndel(andeler, 2L, 200000, 200000, Inntektskategori.ARBEIDSTAKER);
        assertAndel(andeler, 3L, 0, null, Inntektskategori.DAGPENGER);
        assertAndel(andeler, 4L, 100000, 100000, Inntektskategori.DAGPENGER);
        assertAndel(andeler, 5L, 200000, 200000, Inntektskategori.DAGPENGER);
    }

    private void assertAndel(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, long andelsnr, Integer fordeltPrÅr, Integer fordeltRefPrÅr, Inntektskategori ik) {
        var matchendeAndel = andeler.stream().filter(a -> a.getAndelsnr().equals(andelsnr)).findFirst().orElseThrow();
        if (fordeltRefPrÅr != null) {
            assertThat(matchendeAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getFordeltRefusjonPrÅr).orElse(Beløp.ZERO))
                    .isEqualByComparingTo(Beløp.fra(fordeltRefPrÅr));
        }
        assertThat(matchendeAndel.getGjeldendeInntektskategori()).isEqualTo(ik);
        assertThat(matchendeAndel.getFordeltPrÅr()).isEqualByComparingTo(Beløp.fra(fordeltPrÅr));
    }

    private BeregningsgrunnlagDto map(FordelPeriodeModell... regelPeriode) {
        var regelperioder = Arrays.asList(regelPeriode);
        var regelresultater = regelperioder.stream()
                .map(rp -> new RegelResultat(null, null, null, null))
                .collect(Collectors.toList());
        return MapFraFordelingsmodell.map(regelperioder, regelresultater, bg.build());
    }

    private FordelPeriodeModell lagRegelPeriode(LocalDate fom, LocalDate tom, FordelAndelModell.Builder... andeler) {
        var regelandeler = Arrays.stream(andeler).map(a -> a.build()).collect(Collectors.toList());
        return new FordelPeriodeModell(Periode.of(fom, tom), regelandeler);
    }

    private FordelAndelModell.Builder lagRegelAndel(Long andelsnr,
                                                    AktivitetStatus aktivitetStatus,
                                                    String orgnr,
                                                    String referanse,
                                                    Integer fordeltPrÅr,
                                                    Integer fordeltRefPrÅr,
                                                    Inntektskategori ik) {
        var regelBuilder = FordelAndelModell.builder()
                .medAktivitetStatus(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.valueOf(aktivitetStatus.getKode()))
                .medAndelNr(andelsnr)
                .medUtbetalingsgrad(BigDecimal.valueOf(100))
                .medInntektskategori(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.valueOf(ik.getKode()))
                .medFordeltPrÅr(fordeltPrÅr == null ? null : BigDecimal.valueOf(fordeltPrÅr))
                .medFordeltRefusjonPrÅr(fordeltRefPrÅr == null ? null : BigDecimal.valueOf(fordeltRefPrÅr));
        if (orgnr != null) {
            regelBuilder.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, referanse));
        }
        if (aktivitetStatus.erFrilanser()) {
            regelBuilder.medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold());
        }
        return regelBuilder;
    }

    private void lagBGPeriode(LocalDate fom, LocalDate tom, BeregningsgrunnlagPrStatusOgAndelDto.Builder... andeler) {
        var periodeBuilder = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(fom, tom);
        Arrays.asList(andeler).forEach(periodeBuilder::leggTilBeregningsgrunnlagPrStatusOgAndel);
        bg.leggTilBeregningsgrunnlagPeriode(periodeBuilder);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagBGAndel(Long andelsnr,
                                                                    AktivitetStatus aktivitetStatus,
                                                                    String orgnr,
                                                                    String referanse,
                                                                    Inntektskategori ik) {
        var andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(andelsnr)
                .medInntektskategori(ik)
                .medAktivitetStatus(aktivitetStatus);
        if (orgnr != null) {
            var arbfor = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                    .medArbeidsforholdRef(referanse);
            andelBuilder.medBGAndelArbeidsforhold(arbfor);
        }
        return andelBuilder;
    }

}
