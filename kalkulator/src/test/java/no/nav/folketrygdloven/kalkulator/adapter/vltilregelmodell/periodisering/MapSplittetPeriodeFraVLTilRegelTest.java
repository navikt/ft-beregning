package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

class MapSplittetPeriodeFraVLTilRegelTest {
    @Test
    void ingenPeriodeårsak() {
        // Arrange
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(6);
        var beregningsgrunnlag = new BeregningsgrunnlagDto();
        var bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);

        var arbeidsgiver = Arbeidsgiver.virksomhet("abc");
        var arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medAndelsnr(1L)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbeidsforholdRef))
            .build(bgPeriode);

        // Act
        var splittetPeriode = MapSplittetPeriodeFraVLTilRegel.map(bgPeriode);

        // Assert
        assertThat(splittetPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(splittetPeriode.getPeriode().getTom()).isEqualTo(tom);
        assertThat(splittetPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(splittetPeriode.getEksisterendePeriodeAndeler()).hasSize(1);
        var bgPrArbeidsforhold = splittetPeriode.getEksisterendePeriodeAndeler().get(0);
        assertThat(bgPrArbeidsforhold.getAndelNr()).isEqualTo(1L);
        assertThat(bgPrArbeidsforhold.getArbeidsforhold().getOrgnr()).isEqualTo("abc");
        assertThat(bgPrArbeidsforhold.getArbeidsforhold().getArbeidsforholdId()).isNotNull();
        assertThat(bgPrArbeidsforhold.getArbeidsforhold().getArbeidsforholdId()).isEqualTo(arbeidsforholdRef.getReferanse());
        assertThat(splittetPeriode.getNyeAndeler()).isEmpty();
    }

    @Test
    void enPeriodeårsak() {
        // Arrange
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(6);
        var beregningsgrunnlag = new BeregningsgrunnlagDto();
        var bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.REFUSJON_OPPHØRER)
            .build(beregningsgrunnlag);

        // Act
        var splittetPeriode = MapSplittetPeriodeFraVLTilRegel.map(bgPeriode);

        // Assert
        assertThat(splittetPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(splittetPeriode.getPeriode().getTom()).isEqualTo(tom);
        assertThat(splittetPeriode.getPeriodeÅrsaker()).hasSize(1);
        assertThat(splittetPeriode.getPeriodeÅrsaker().get(0)).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.REFUSJON_OPPHØRER);
    }

    @Test
    void toPeriodeårsaker() {
        // Arrange
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(6);
        var beregningsgrunnlag = new BeregningsgrunnlagDto();
        var bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.GRADERING)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .build(beregningsgrunnlag);

        // Act
        var splittetPeriode = MapSplittetPeriodeFraVLTilRegel.map(bgPeriode);

        // Assert
        assertThat(splittetPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(splittetPeriode.getPeriode().getTom()).isEqualTo(tom);
        assertThat(splittetPeriode.getPeriodeÅrsaker()).hasSize(2);
        assertThat(splittetPeriode.getPeriodeÅrsaker().get(0)).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.GRADERING);
        assertThat(splittetPeriode.getPeriodeÅrsaker().get(1)).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }
}
