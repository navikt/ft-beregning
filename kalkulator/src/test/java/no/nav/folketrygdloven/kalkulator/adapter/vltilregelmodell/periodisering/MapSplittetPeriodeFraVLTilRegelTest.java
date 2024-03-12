package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

public class MapSplittetPeriodeFraVLTilRegelTest {
    @Test
    public void ingenPeriodeårsak() {
        // Arrange
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(6);
        BeregningsgrunnlagDto beregningsgrunnlag = mock(BeregningsgrunnlagDto.class);
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);

        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet("abc");
        InternArbeidsforholdRefDto arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medAndelsnr(1L)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbeidsforholdRef))
            .build(bgPeriode);

        // Act
        SplittetPeriode splittetPeriode = MapSplittetPeriodeFraVLTilRegel.map(bgPeriode);

        // Assert
        assertThat(splittetPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(splittetPeriode.getPeriode().getTom()).isEqualTo(tom);
        assertThat(splittetPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(splittetPeriode.getEksisterendePeriodeAndeler()).hasSize(1);
        EksisterendeAndel bgPrArbeidsforhold = splittetPeriode.getEksisterendePeriodeAndeler().get(0);
        assertThat(bgPrArbeidsforhold.getAndelNr()).isEqualTo(1L);
        assertThat(bgPrArbeidsforhold.getArbeidsforhold().getOrgnr()).isEqualTo("abc");
        assertThat(bgPrArbeidsforhold.getArbeidsforhold().getArbeidsforholdId()).isNotNull();
        assertThat(bgPrArbeidsforhold.getArbeidsforhold().getArbeidsforholdId()).isEqualTo(arbeidsforholdRef.getReferanse());
        assertThat(splittetPeriode.getNyeAndeler()).isEmpty();
    }

    @Test
    public void enPeriodeårsak() {
        // Arrange
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(6);
        BeregningsgrunnlagDto beregningsgrunnlag = mock(BeregningsgrunnlagDto.class);
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.REFUSJON_OPPHØRER)
            .build(beregningsgrunnlag);

        // Act
        SplittetPeriode splittetPeriode = MapSplittetPeriodeFraVLTilRegel.map(bgPeriode);

        // Assert
        assertThat(splittetPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(splittetPeriode.getPeriode().getTom()).isEqualTo(tom);
        assertThat(splittetPeriode.getPeriodeÅrsaker()).hasSize(1);
        assertThat(splittetPeriode.getPeriodeÅrsaker().get(0)).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.REFUSJON_OPPHØRER);
    }

    @Test
    public void toPeriodeårsaker() {
        // Arrange
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(6);
        BeregningsgrunnlagDto beregningsgrunnlag = mock(BeregningsgrunnlagDto.class);
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.GRADERING)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .build(beregningsgrunnlag);

        // Act
        SplittetPeriode splittetPeriode = MapSplittetPeriodeFraVLTilRegel.map(bgPeriode);

        // Assert
        assertThat(splittetPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(splittetPeriode.getPeriode().getTom()).isEqualTo(tom);
        assertThat(splittetPeriode.getPeriodeÅrsaker()).hasSize(2);
        assertThat(splittetPeriode.getPeriodeÅrsaker().get(0)).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.GRADERING);
        assertThat(splittetPeriode.getPeriodeÅrsaker().get(1)).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }
}
