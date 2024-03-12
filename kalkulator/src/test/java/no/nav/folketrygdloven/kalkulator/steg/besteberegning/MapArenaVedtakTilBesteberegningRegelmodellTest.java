package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

class MapArenaVedtakTilBesteberegningRegelmodellTest {
    private static final LocalDate STP = LocalDate.of(2021,5,1);

    @Test
    public void skal_mappe_et_meldekort_dagpenger() {
        List<YtelseDto> vedtak = Collections.singletonList(lagVedtak(STP, stpPluss(30), YtelseType.DAGPENGER,
                lagMeldekort(STP, stpPluss(14), 500)));

        List<Periodeinntekt> inntekter = kjørMapping(vedtak);

        assertThat(inntekter).hasSize(1);
        assertInntekt(inntekter, STP, stpPluss(14), 500);
    }

    @Test
    public void meldekort_skal_ikke_starte_før_vedtak() {
        List<YtelseDto> vedtak = Collections.singletonList(lagVedtak(STP, stpPluss(30), YtelseType.DAGPENGER,
                lagMeldekort(STP.plusDays(10), stpPluss(14), 500)));

        List<Periodeinntekt> inntekter = kjørMapping(vedtak);

        assertThat(inntekter).hasSize(1);
        assertInntekt(inntekter, STP.plusDays(10), stpPluss(14), 500);
    }

    @Test
    public void skal_mappe_flere_meldekort_ulike_vedtak_aap() {
        List<YtelseDto> vedtak = Arrays.asList(lagVedtak(STP, stpPluss(30), YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekort(STP, stpPluss(14), 500),
                lagMeldekort(stpPluss(15), stpPluss(35), 600)),
        lagVedtak(stpPluss(40), stpPluss(60), YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekort(stpPluss(40), stpPluss(54), 700)));

        List<Periodeinntekt> inntekter = kjørMapping(vedtak);

        assertThat(inntekter).hasSize(3);
        assertInntekt(inntekter, STP, stpPluss(14), 500);
        assertInntekt(inntekter, stpPluss(15), stpPluss(35), 600);
        assertInntekt(inntekter, stpPluss(40), stpPluss(54), 700);
    }

    private void assertInntekt(List<Periodeinntekt> inntekter, LocalDate fom, LocalDate tom, int inntekt) {
        Periodeinntekt matchendeInntekt = inntekter.stream()
                .filter(it -> it.getPeriode().getFom().equals(fom))
                .filter(it -> it.getPeriode().getTom().equals(tom))
                .filter(it -> it.getInntekt().compareTo(BigDecimal.valueOf(inntekt)) == 0)
                .findFirst()
                .orElse(null);
        assertThat(matchendeInntekt).isNotNull();
    }

    private List<Periodeinntekt> kjørMapping(List<YtelseDto> vedtak) {
        return MapArenaVedtakTilBesteberegningRegelmodell.lagInntektFraArenaYtelser(new YtelseFilterDto(vedtak));
    }

    private LocalDate stpPluss(int dager) {
        return STP.plusDays(dager);
    }

    private YtelseAnvistDto lagMeldekort(LocalDate fom, LocalDate tom, int beløp) {
        return YtelseAnvistDtoBuilder.ny()
                .medAnvistPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medBeløp(Beløp.fra(beløp))
                .build();
    }

    private YtelseDto lagVedtak(LocalDate fom, LocalDate tom, YtelseType ytelse, YtelseAnvistDto... meldekort) {
        YtelseDtoBuilder builder = YtelseDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medYtelseType(ytelse);
        Arrays.asList(meldekort).forEach(builder::leggTilYtelseAnvist);
        return builder.build();
    }

}
