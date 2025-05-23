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

class AutopunktUtlederFastsettBeregningsaktiviteterTjenesteTest {

    @Test
    void skal_vente_på_meldekort_når_har_AAP_og_meldekort_uten_AAP_status() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 1, 4);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
	    var fom = LocalDate.of(2018, 9, 1);
	    var tom = LocalDate.of(2019, 5, 1);
	    var meldekortFom = LocalDate.of(2018, 12, 1);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(1));
    }

    @Test
    void skal_vente_på_meldekort_når_har_AAP_og_meldekort_uten_AAP_status_grenseverdi() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 1, 4);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
	    var fom = LocalDate.of(2018, 9, 1);
	    var tom = skjæringstidspunktOpptjening.minusDays(1);
	    var meldekortFom = LocalDate.of(2018, 10, 1);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(1));
    }

    @Test
    void skal_vente_på_meldekort_når_har_AAP_og_meldekort_uten_AAP_status_etter_første_utta() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 2, 2);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var fom = LocalDate.of(2018, 9, 1);
	    var tom = LocalDate.of(2019, 5, 1);
	    var meldekortFom = LocalDate.of(2018, 12, 1);
	    var meldekortFom2 = LocalDate.of(2019, 1, 12);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(2));
    }

    @Test
    void skal_vente_på_meldekort_også_når_har_AAP_status() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 2, 9);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var fom = LocalDate.of(2018, 9, 1);
	    var tom = LocalDate.of(2019, 5, 1);
	    var meldekortFom = LocalDate.of(2018, 12, 1);
	    var meldekortFom2 = LocalDate.of(2019, 1, 12);
	    var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(iayGrunnlagBuilder, fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.of(iayGrunnlagBuilder.build()), AktivitetStatus.ARBEIDSAVKLARINGSPENGER);

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(dagensdato.plusDays(1));
    }

    @Test
    void skal_ikke_vente_på_meldekort_når_14_dager_etter_første_uttaksdag() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 2, 16);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
	    var fom = LocalDate.of(2018, 9, 1);
	    var tom = LocalDate.of(2019, 5, 1);
	    var meldekortFom = LocalDate.of(2018, 12, 1);
	    var meldekortFom2 = LocalDate.of(2019, 1, 12);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    void skal_ikke_vente_på_meldekort_når_ikke_har_meldekort_siste_4_måneder() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 1, 4);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
	    var fom = LocalDate.of(2018, 9, 1);
	    var tom = LocalDate.of(2019, 5, 1);
	    var meldekortFom = LocalDate.of(2018, 9, 17);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom,
                YtelseType.ARBEIDSAVKLARINGSPENGER, lagMeldekortPeriode(meldekortFom));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    void skal_ikke_vente_på_meldekort_når_ikke_har_løpende_vedtak() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 1, 4);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
	    var fom = LocalDate.of(2018, 9, 1);
	    var tom = LocalDate.of(2018, 12, 31);
	    var meldekortFom = LocalDate.of(2018, 12, 1);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom,
                YtelseType.ARBEIDSAVKLARINGSPENGER, lagMeldekortPeriode(meldekortFom));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    void skal_ikke_vente_på_meldekort_når_er_vanlig_arbeidstaker() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 1, 4);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.empty(), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    void skal_vente_på_meldekort_når_har_DP_og_meldekort_uten_DP_status() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 1, 4);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
	    var fom = LocalDate.of(2018, 9, 1);
	    var tom = LocalDate.of(2019, 5, 1);
	    var meldekortFom = LocalDate.of(2018, 5, 1);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.DAGPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(1));
    }

    @Test
    void skal_vente_på_meldekort_når_har_DP_status() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 2, 9);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
	    var fom = LocalDate.of(2018, 9, 1);
	    var tom = LocalDate.of(2019, 5, 1);
	    var meldekortFom = LocalDate.of(2018, 12, 1);
	    var meldekortFom2 = LocalDate.of(2019, 1, 12);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.DAGPENGER);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.DAGPENGER,
                lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(dagensdato.plusDays(1));
    }

    @Test
    void skal_vente_på_meldekort_når_ikke_har_løpende_vedtak_men_var_løpende_til_skjæringstidspunkt() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 2, 12);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 11);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
	    var fom = LocalDate.of(2018, 9, 3);
	    var tom = LocalDate.of(2019, 2, 11);
	    var meldekortFom = LocalDate.of(2019, 1, 21);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(dagensdato.plusDays(1));
    }

    @Test
    void skalIkkeVentePåMeldekortNårMeldekortErMottatt() {
        // Arrange
	    var dagensdato = LocalDate.of(2019, 2, 12);
	    var skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 11);
	    var bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
	    var fom = LocalDate.of(2018, 9, 3);
	    var tom = LocalDate.of(2019, 2, 11);
	    var meldekortFom = skjæringstidspunktOpptjening.minusDays(5);
	    var aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), fom, tom, YtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekortPeriode(meldekortFom));

        // Act
	    var resultat = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), dagensdato, bg.getSkjæringstidspunkt(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));

        //Assert
        assertThat(resultat).isNotPresent();
    }

    private Periode lagMeldekortPeriode(LocalDate fom) {
        return Periode.of(fom, fom.plusDays(13));
    }
}
