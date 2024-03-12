package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.folketrygdloven.utils.BeregningsgrunnlagTestUtil;

public class AutopunktUtlederFastsettBeregningsaktiviteterTjenesteTest {

    @Test
    public void skal_vente_på_meldekort_når_har_AAP_og_meldekort_uten_AAP_status() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(1));
    }

    @Test
    public void skal_vente_på_meldekort_når_har_AAP_og_meldekort_uten_AAP_status_grenseverdi() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = skjæringstidspunktOpptjening.minusDays(1);
        LocalDate meldekortFom = LocalDate.of(2018, 10, 1);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(1));
    }

    @Test
    public void skal_vente_på_meldekort_når_har_AAP_og_meldekort_uten_AAP_status_etter_første_utta() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 2);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        LocalDate meldekortFom2 = LocalDate.of(2019, 1, 12);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(2));
    }

    @Test
    public void skal_vente_på_meldekort_også_når_har_AAP_status() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 9);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        LocalDate meldekortFom2 = LocalDate.of(2019, 1, 12);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(iayGrunnlagBuilder, fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.of(iayGrunnlagBuilder.build()), AktivitetStatus.ARBEIDSAVKLARINGSPENGER);

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(dagensdato.plusDays(1));
    }

    @Test
    public void skal_ikke_vente_på_meldekort_når_14_dager_etter_første_uttaksdag() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 16);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        LocalDate meldekortFom2 = LocalDate.of(2019, 1, 12);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    public void skal_ikke_vente_på_meldekort_når_ikke_har_meldekort_siste_4_måneder() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 9, 17);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom,
                YtelseType.ARBEIDSAVKLARINGSPENGER, lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    public void skal_ikke_vente_på_meldekort_når_ikke_har_løpende_vedtak() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2018, 12, 31);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom,
                YtelseType.ARBEIDSAVKLARINGSPENGER, lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    public void skal_ikke_vente_på_meldekort_når_er_vanlig_arbeidstaker() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.empty(), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    public void skal_vente_på_meldekort_når_har_DP_og_meldekort_uten_DP_status() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 5, 1);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.DAGPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(1));
    }

    @Test
    public void skal_vente_på_meldekort_når_har_DP_status() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 9);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        LocalDate meldekortFom2 = LocalDate.of(2019, 1, 12);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.DAGPENGER);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.DAGPENGER,
                lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(dagensdato.plusDays(1));
    }

    @Test
    public void skal_vente_på_meldekort_når_ikke_har_løpende_vedtak_men_var_løpende_til_skjæringstidspunkt() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 12);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 11);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 3);
        LocalDate tom = LocalDate.of(2019, 2, 11);
        LocalDate meldekortFom = LocalDate.of(2019, 1, 21);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(dagensdato.plusDays(1));
    }

    @Test
    public void skalIkkeVentePåMeldekortNårMeldekortErMottatt() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 12);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 11);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 3);
        LocalDate tom = LocalDate.of(2019, 2, 11);
        LocalDate meldekortFom = skjæringstidspunktOpptjening.minusDays(5);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    private Periode lagMeldekortPeriode(LocalDate fom) {
        return Periode.of(fom, fom.plusDays(13));
    }
}
