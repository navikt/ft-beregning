package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

public class KortvarigArbeidsforholdTjenesteImplTest {

    @Test
    public void under_6_måneder_sammenhengende() {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(LocalDate.now().minusMonths(1))
            .build();

        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(LocalDate.now().minusMonths(5), LocalDate.now()), true);
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder);

        YrkesaktivitetDto yrkesaktivitet = yrkesaktivitetBuilder.build();

        assertThat(KortvarigArbeidsforholdTjeneste.erKortvarigYrkesaktivitetSomAvsluttesEtterSkjæringstidspunkt(new YrkesaktivitetFilterDto(Optional.empty(), yrkesaktivitet), beregningsgrunnlag, yrkesaktivitet)).isTrue();
    }

    @Test
    public void under_6_måneder_delt_opp_i_flere_deler() {
        LocalDate date = LocalDate.now();
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(date.minusMonths(1))
            .build();

        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(5), date.minusMonths(4)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(4), date.minusMonths(3)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(3), date.minusMonths(2)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(2), date.minusMonths(1)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(1), date), true));

        YrkesaktivitetDto yrkesaktivitet = yrkesaktivitetBuilder.build();

        assertThat(KortvarigArbeidsforholdTjeneste.erKortvarigYrkesaktivitetSomAvsluttesEtterSkjæringstidspunkt(new YrkesaktivitetFilterDto(Optional.empty(), yrkesaktivitet), beregningsgrunnlag, yrkesaktivitet)).isTrue();
    }

    @Test
    public void over_6_måneder_delt_opp_i_flere_deler() {
        LocalDate date = LocalDate.now();
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(date.minusMonths(1))
            .build();

        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(5), date.minusMonths(4)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(4), date.minusMonths(3)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(3), date.minusMonths(2)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(2), date.minusMonths(1)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(1), date), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date, date.plusMonths(1)), true));

        YrkesaktivitetDto yrkesaktivitet = yrkesaktivitetBuilder.build();

        assertThat(KortvarigArbeidsforholdTjeneste.erKortvarigYrkesaktivitetSomAvsluttesEtterSkjæringstidspunkt(new YrkesaktivitetFilterDto(Optional.empty(), yrkesaktivitet), beregningsgrunnlag, yrkesaktivitet)).isFalse();
    }


    @Test
    public void over_6_måneder_delt_opp_i_flere_deler_men_med_huller() {
        LocalDate date = LocalDate.now();
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(date.minusMonths(1))
            .build();

        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(5), date.minusMonths(4)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(4), date.minusMonths(3)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(3), date.minusMonths(2)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(2), date.minusMonths(1)), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.minusMonths(1), date), true));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(date.plusMonths(1), date.plusMonths(2)), true));

        YrkesaktivitetDto yrkesaktivitet = yrkesaktivitetBuilder.build();

        assertThat(KortvarigArbeidsforholdTjeneste.erKortvarigYrkesaktivitetSomAvsluttesEtterSkjæringstidspunkt(new YrkesaktivitetFilterDto(Optional.empty(), yrkesaktivitet), beregningsgrunnlag, yrkesaktivitet)).isTrue();
    }
}
