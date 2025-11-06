package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErTotaltRefusjonskravStørreEnnBruttoTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2025, 10, 1);
    private static final String ORGNR_1 = "123";
    private static final String ORGNR_2 = "321";

    @Test
    void skal_gi_true_når_refusjon_overstiger_brutto_og_false_ellers() {
        var periodisertBg1 = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(9)))
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.AT)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR_1))
                .medBruttoPrÅr(BigDecimal.valueOf(300_000))
                .build())
            .build();
        var periodisertBg2 = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.plusDays(10), DateUtil.TIDENES_ENDE))
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.AT)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR_1))
                .medBruttoPrÅr(BigDecimal.valueOf(300_000))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.AT)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR_2))
                .medBruttoPrÅr(BigDecimal.valueOf(0))
                .medRefusjonPrÅr(BigDecimal.valueOf(350_000))
                .build())
            .build();
        var vurdering1 = ErTotaltRefusjonskravStørreEnnBrutto.vurder(List.of(periodisertBg1, periodisertBg2), SKJÆRINGSTIDSPUNKT.plusDays(1));
        var vurdering2 = ErTotaltRefusjonskravStørreEnnBrutto.vurder(List.of(periodisertBg1, periodisertBg2), SKJÆRINGSTIDSPUNKT.plusDays(15));

        assertThat(vurdering1).isFalse();
        assertThat(vurdering2).isTrue();
    }
}
