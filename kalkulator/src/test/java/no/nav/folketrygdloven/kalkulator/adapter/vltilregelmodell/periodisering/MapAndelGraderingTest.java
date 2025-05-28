package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.gradering.MapAndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

class MapAndelGraderingTest {

    private KoblingReferanse ref;

    @BeforeEach
    void setUp() {
        ref = new KoblingReferanseMock(LocalDate.now(), FagsakYtelseType.FORELDREPENGER);
    }

    @Test
    void skalMappeAndelGraderingSN() {
        // Arrange
        var fom1 = LocalDate.now();
        var tom1 = fom1.plusWeeks(6);
        var p1 = Intervall.fraOgMedTilOgMed(fom1, tom1);
        var fom2 = fom1.plusMonths(3);
        var tom2 = tom1.plusMonths(3);
        var p2 = Intervall.fraOgMedTilOgMed(fom2, tom2);
        var vlAndelGradering = AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .leggTilGradering(new AndelGradering.Gradering(p1, Aktivitetsgrad.fra(50)))
            .leggTilGradering(new AndelGradering.Gradering(p2, Aktivitetsgrad.fra(25)))
            .build();
        var filter = new YrkesaktivitetFilterDto(Optional.empty(), Optional.empty());
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(fom1)
                .build();
        var bgperiode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(fom1, TIDENES_ENDE)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .build(bgperiode);
        // Act
        var regelAndelGradering = MapAndelGradering.mapGraderingForFLSN(bg,
                vlAndelGradering, filter, ref.getSkjæringstidspunktBeregning());

        // Assert
        assertThat(regelAndelGradering.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2.SN);
        assertThat(regelAndelGradering.getGraderinger()).hasSize(2);
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom1);
            assertThat(periode.getTom()).isEqualTo(tom1);
        });
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom2);
            assertThat(periode.getTom()).isEqualTo(tom2);
        });
    }

    @Test
    void skalMappeAndelGraderingFL() {
// Arrange
        var fom1 = LocalDate.now();
        var tom1 = fom1.plusWeeks(6);
        var p1 = Intervall.fraOgMedTilOgMed(fom1, tom1);
        var fom2 = fom1.plusMonths(3);
        var tom2 = tom1.plusMonths(3);
        var p2 = Intervall.fraOgMedTilOgMed(fom2, tom2);
        var vlAndelGradering = AndelGradering.builder()
            .medStatus(AktivitetStatus.FRILANSER)
            .leggTilGradering(new AndelGradering.Gradering(p1, Aktivitetsgrad.fra(50)))
            .leggTilGradering(new AndelGradering.Gradering(p2, Aktivitetsgrad.fra(25)))
                .build();
        var filter = new YrkesaktivitetFilterDto(Optional.empty(), Optional.empty());
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(fom1)
                .build();
        var bgperiode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(fom1, TIDENES_ENDE)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .build(bgperiode);

        // Act
        var regelAndelGradering = MapAndelGradering.mapGraderingForFLSN(bg, vlAndelGradering, filter, ref.getSkjæringstidspunktBeregning());

        // Assert
        assertThat(regelAndelGradering.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2.FL);
        assertThat(regelAndelGradering.getGraderinger()).hasSize(2);
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom1);
            assertThat(periode.getTom()).isEqualTo(tom1);
        });
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom2);
            assertThat(periode.getTom()).isEqualTo(tom2);
        });
    }

}
