package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;

class FastsettStatusOgAndelPrPeriodeFRISINNTest {


    public static final LocalDate STP = LocalDate.now();

    @Test
    void skal_ikke_ta_med_frilans_om_man_ikke_har_inntekt_siste_12_mnd_og_ikke_har_oppgitt_inntekt_i_søknadsperiode_søker_ikke_frilans() {
        // Arrange
        AktivitetStatusModellFRISINN regelmodell = new AktivitetStatusModellFRISINN();
        regelmodell.setSkjæringstidspunktForOpptjening(STP);
        regelmodell.setSkjæringstidspunktForBeregning(STP);
        regelmodell.setFrisinnPerioder(List.of(new FrisinnPeriode(Periode.of(STP, TIDENES_ENDE), false, true)));
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.leggTilPeriodeinntekt(lagSNInntekt());
        regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forFrilanser(Periode.of(STP.minusMonths(36), STP.plusMonths(12))));
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

        // Act
        List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

        // Assert
        assertThat(statusListe).hasSize(1);
        assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);

    }

    @Test
    void skal_ikke_ta_med_frilans_om_man_ikke_har_inntekt_siste_12_mnd_og_ikke_har_oppgitt_inntekt_i_søknadsperiode_søker_frilans() {
        // Arrange
        AktivitetStatusModellFRISINN regelmodell = new AktivitetStatusModellFRISINN();
        regelmodell.setSkjæringstidspunktForOpptjening(STP);
        regelmodell.setSkjæringstidspunktForBeregning(STP);
        regelmodell.setFrisinnPerioder(List.of(new FrisinnPeriode(Periode.of(STP, TIDENES_ENDE), true, true)));
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.leggTilPeriodeinntekt(lagSNInntekt());
        regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forFrilanser(Periode.of(STP.minusMonths(36), STP.plusMonths(12))));
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

        // Act
        List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

        // Assert
        assertThat(statusListe).hasSize(1);
        assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);

    }

    @Test
    void skal_ta_med_frilans_om_man_har_inntekt_siste_12_mnd_uten_oppgitt_inntekt_søker_frilans() {
        // Arrange
        AktivitetStatusModellFRISINN regelmodell = new AktivitetStatusModellFRISINN();
        regelmodell.setSkjæringstidspunktForOpptjening(STP);
        regelmodell.setSkjæringstidspunktForBeregning(STP);
        regelmodell.setFrisinnPerioder(List.of(new FrisinnPeriode(Periode.of(STP, TIDENES_ENDE), true, true)));
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.leggTilPeriodeinntekt(lagSNInntekt());
        inntektsgrunnlag.leggTilPeriodeinntekt(lagInntektForFL(Periode.of(STP.minusMonths(12), STP.minusMonths(11))));
        regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forFrilanser(Periode.of(STP.minusMonths(36), STP.plusMonths(12))));
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

        // Act
        List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

        // Assert
        assertThat(statusListe).hasSize(2);
        assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(statusListe.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);

    }

    @Test
    void skal_ikke_ta_med_andeler_som_starter_på_stp() {
        // Arrange
        AktivitetStatusModellFRISINN regelmodell = new AktivitetStatusModellFRISINN();
        regelmodell.setSkjæringstidspunktForOpptjening(STP);
        regelmodell.setSkjæringstidspunktForBeregning(STP);
        regelmodell.setFrisinnPerioder(List.of(new FrisinnPeriode(Periode.of(STP, TIDENES_ENDE), true, true)));
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.leggTilPeriodeinntekt(lagSNInntekt());
        inntektsgrunnlag.leggTilPeriodeinntekt(lagInntektForFL(Periode.of(STP.minusMonths(12), STP.minusMonths(11))));
        regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(STP, STP.plusMonths(12)), "999999", null));

        // Act
        List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

        // Assert
        assertThat(statusListe).hasSize(1);
        assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
    }

    @Test
    void skal_ta_med_andeler_som_slutter_på_stp() {
        // Arrange
        AktivitetStatusModellFRISINN regelmodell = new AktivitetStatusModellFRISINN();
        regelmodell.setSkjæringstidspunktForOpptjening(STP);
        regelmodell.setSkjæringstidspunktForBeregning(STP);
        regelmodell.setFrisinnPerioder(List.of(new FrisinnPeriode(Periode.of(STP, TIDENES_ENDE), true, true)));
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.leggTilPeriodeinntekt(lagSNInntekt());
        inntektsgrunnlag.leggTilPeriodeinntekt(lagInntektForFL(Periode.of(STP.minusMonths(12), STP.minusMonths(11))));
        regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(STP.minusMonths(12), STP), "999999", null));

        // Act
        List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

        // Assert
        assertThat(statusListe).hasSize(2);
        assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
        assertThat(statusListe.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
    }

    private Periodeinntekt lagInntektForFL(Periode periode) {
        return Periodeinntekt.builder()
            .medPeriode(periode)
            .medInntekt(BigDecimal.TEN)
            .medArbeidsgiver(Arbeidsforhold.frilansArbeidsforhold())
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build();
    }

    private Periodeinntekt lagSNInntekt() {
        return Periodeinntekt.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medPeriode(Periode.of(STP.minusMonths(24), STP.minusMonths(12)))
            .medInntekt(BigDecimal.TEN)
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build();
    }

    private List<BeregningsgrunnlagPrStatus> kjørRegel(AktivitetStatusModellFRISINN regelmodell) {
        FastsettStatusOgAndelPrPeriodeFRISINN regel = new FastsettStatusOgAndelPrPeriodeFRISINN();
        regel.evaluate(regelmodell);
        return regelmodell.getBeregningsgrunnlagPrStatusListe();
    }
}
