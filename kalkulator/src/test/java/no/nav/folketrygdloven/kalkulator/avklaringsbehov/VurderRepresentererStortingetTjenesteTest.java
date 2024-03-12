package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderRepresentererStortingetHåndteringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

class VurderRepresentererStortingetTjenesteTest {


    public static final LocalDate STP = LocalDate.now();


    @Test
    void skal_ikke_sette_periodeårsak_på_periode_dersom_ikke_representerer_stortinget() {
        var dto = new VurderRepresentererStortingetHåndteringDto(STP, STP.plusDays(10), false);
        var input = lagInput(List.of(
                Intervall.fraOgMedTilOgMed(STP, STP.plusDays(10)),
                Intervall.fraOgMedTilOgMed(STP.plusDays(11), TIDENES_ENDE)
        ));

        // Act
        var nyttGr = VurderRepresentererStortingetTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));
        var perioder = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder();

        // Assert
        assertThat(perioder.size()).isEqualTo(2);
        assertThat(perioder.get(0).getPeriodeÅrsaker().size()).isEqualTo(0);
        assertThat(perioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(0);
    }

    @Test
    void skal_sette_periodeårsak_på_periode() {
        var dto = new VurderRepresentererStortingetHåndteringDto(STP, STP.plusDays(10), true);
        var input = lagInput(List.of(
                Intervall.fraOgMedTilOgMed(STP, STP.plusDays(10)),
                Intervall.fraOgMedTilOgMed(STP.plusDays(11), TIDENES_ENDE)
        ));

        // Act
        var nyttGr = VurderRepresentererStortingetTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));
        var perioder = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder();

        // Assert
        assertThat(perioder.size()).isEqualTo(2);
        assertThat(perioder.get(0).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(0).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET);
        assertThat(perioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(1).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET_AVSLUTTET);


    }

    @Test
    void skal_sette_periodeårsak_på_siste_periode() {
        var dto = new VurderRepresentererStortingetHåndteringDto(STP.plusDays(11), TIDENES_ENDE, true);
        var input = lagInput(List.of(
                Intervall.fraOgMedTilOgMed(STP, STP.plusDays(10)),
                Intervall.fraOgMedTilOgMed(STP.plusDays(11), TIDENES_ENDE)
        ));

        // Act
        var nyttGr = VurderRepresentererStortingetTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));
        var perioder = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder();

        // Assert
        assertThat(perioder.size()).isEqualTo(2);
        assertThat(perioder.get(0).getPeriodeÅrsaker().size()).isEqualTo(0);
        assertThat(perioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(1).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET);
    }

    @Test
    void skal_sette_periodeårsak_og_splitte_ved_start_av_periode() {
        var dto = new VurderRepresentererStortingetHåndteringDto(STP, STP.plusDays(10), true);
        var input = lagInput(List.of(
                Intervall.fraOgMedTilOgMed(STP, TIDENES_ENDE)
        ));

        // Act
        var nyttGr = VurderRepresentererStortingetTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));
        var perioder = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder();

        // Assert
        assertThat(perioder.size()).isEqualTo(2);
        assertThat(perioder.get(0).getPeriode().getFomDato()).isEqualTo(STP);
        assertThat(perioder.get(0).getPeriode().getTomDato()).isEqualTo(STP.plusDays(10));
        assertThat(perioder.get(0).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(0).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET);

        assertThat(perioder.get(1).getPeriode().getFomDato()).isEqualTo(STP.plusDays(11));
        assertThat(perioder.get(1).getPeriode().getTomDato()).isEqualTo(TIDENES_ENDE);
        assertThat(perioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(1).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET_AVSLUTTET);

    }

    @Test
    void skal_sette_periodeårsak_og_splitte_ved_slutt_av_periode() {
        var dto = new VurderRepresentererStortingetHåndteringDto(STP.plusDays(5), STP.plusDays(10), true);
        var input = lagInput(List.of(
                Intervall.fraOgMedTilOgMed(STP, STP.plusDays(10))
        ));

        // Act
        var nyttGr = VurderRepresentererStortingetTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));
        var perioder = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder();

        // Assert
        assertThat(perioder.size()).isEqualTo(2);
        assertThat(perioder.get(0).getPeriode().getFomDato()).isEqualTo(STP);
        assertThat(perioder.get(0).getPeriode().getTomDato()).isEqualTo(STP.plusDays(4));
        assertThat(perioder.get(0).getPeriodeÅrsaker().size()).isEqualTo(0);

        assertThat(perioder.get(1).getPeriode().getFomDato()).isEqualTo(STP.plusDays(5));
        assertThat(perioder.get(1).getPeriode().getTomDato()).isEqualTo(STP.plusDays(10));
        assertThat(perioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(1).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET);
    }

    @Test
    void skal_sette_periodeårsak_og_splitte_ved_midt_i_periode() {
        var dto = new VurderRepresentererStortingetHåndteringDto(STP.plusDays(5), STP.plusDays(8), true);
        var input = lagInput(List.of(
                Intervall.fraOgMedTilOgMed(STP, STP.plusDays(10))
        ));

        // Act
        var nyttGr = VurderRepresentererStortingetTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));
        var perioder = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder();

        // Assert
        assertThat(perioder.size()).isEqualTo(3);
        assertThat(perioder.get(0).getPeriode().getFomDato()).isEqualTo(STP);
        assertThat(perioder.get(0).getPeriode().getTomDato()).isEqualTo(STP.plusDays(4));
        assertThat(perioder.get(0).getPeriodeÅrsaker().size()).isEqualTo(0);

        assertThat(perioder.get(1).getPeriode().getFomDato()).isEqualTo(STP.plusDays(5));
        assertThat(perioder.get(1).getPeriode().getTomDato()).isEqualTo(STP.plusDays(8));
        assertThat(perioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(1).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET);

        assertThat(perioder.get(2).getPeriode().getFomDato()).isEqualTo(STP.plusDays(9));
        assertThat(perioder.get(2).getPeriode().getTomDato()).isEqualTo(STP.plusDays(10));
        assertThat(perioder.get(2).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(2).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET_AVSLUTTET);

    }

    @Test
    void skal_sette_periodeårsak_og_dersom_hele_perioden_faller_innenfor_stortingsperioden() {
        var dto = new VurderRepresentererStortingetHåndteringDto(STP, STP.plusDays(10), true);
        var input = lagInput(List.of(
                Intervall.fraOgMedTilOgMed(STP, STP.plusDays(2)),
                Intervall.fraOgMedTilOgMed(STP.plusDays(3), STP.plusDays(5)),
                Intervall.fraOgMedTilOgMed(STP.plusDays(6), STP.plusDays(10))

        ));

        // Act
        var nyttGr = VurderRepresentererStortingetTjeneste.løsAvklaringsbehov(dto, new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.FASTSATT_INN));
        var perioder = nyttGr.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder();

        // Assert
        assertThat(perioder.size()).isEqualTo(3);
        assertThat(perioder.get(0).getPeriode().getFomDato()).isEqualTo(STP);
        assertThat(perioder.get(0).getPeriode().getTomDato()).isEqualTo(STP.plusDays(2));
        assertThat(perioder.get(0).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(0).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET);

        assertThat(perioder.get(1).getPeriode().getFomDato()).isEqualTo(STP.plusDays(3));
        assertThat(perioder.get(1).getPeriode().getTomDato()).isEqualTo(STP.plusDays(5));
        assertThat(perioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(1).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET);

        assertThat(perioder.get(2).getPeriode().getFomDato()).isEqualTo(STP.plusDays(6));
        assertThat(perioder.get(2).getPeriode().getTomDato()).isEqualTo(STP.plusDays(10));
        assertThat(perioder.get(2).getPeriodeÅrsaker().size()).isEqualTo(1);
        assertThat(perioder.get(2).getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.REPRESENTERER_STORTINGET);
    }

    private BeregningsgrunnlagInput lagInput(List<Intervall> perioder) {
        var input = new BeregningsgrunnlagInput(
                new KoblingReferanseMock(STP),
                null,
                null,
                null,
                null
        );

        input = input.medBeregningsgrunnlagGrunnlag(lagBeregningsgrunnlag(perioder));
        return input;
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(List<Intervall> perioder) {
        var bgBuilder = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(perioder.stream().map(Intervall::getFomDato).min(Comparator.naturalOrder()).orElse(null))
                .medGrunnbeløp(Beløp.fra(100_000));
        perioder.forEach(p -> {
            var periodeBuilder = new BeregningsgrunnlagPeriodeDto.Builder();
            periodeBuilder.medBeregningsgrunnlagPeriode(p.getFomDato(), p.getTomDato());
            periodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(lagFrilansandel());
            bgBuilder.leggTilBeregningsgrunnlagPeriode(periodeBuilder);
        });
        return BeregningsgrunnlagGrunnlagDtoBuilder.nytt()
                .medBeregningsgrunnlag(bgBuilder.build())
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagFrilansandel() {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(1L)
                .medKilde(AndelKilde.PROSESS_START)
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .build();
    }

}
