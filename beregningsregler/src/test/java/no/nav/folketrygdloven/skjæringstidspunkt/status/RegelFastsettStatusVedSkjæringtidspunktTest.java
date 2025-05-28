package no.nav.folketrygdloven.skjæringstidspunkt.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

class RegelFastsettStatusVedSkjæringtidspunktTest {
    private static final String ORGNR = "7654";
    private static final Arbeidsforhold ARBEIDSFORHOLD =  Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
    private LocalDate skjæringstidspunktForBeregning;
    private AktivitetStatusModell regelmodell;

    @BeforeEach
    void setup() {
        skjæringstidspunktForBeregning = LocalDate.of(2018, Month.JANUARY, 15);
        regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForBeregning(skjæringstidspunktForBeregning);
    }

    @Test
    void skalFastsetteStatusDPNårAktivitetErDagpengerMottaker(){
        // Arrange
        var aktivPeriode = AktivPeriode.forAndre(Aktivitet.DAGPENGEMOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusWeeks(4), skjæringstidspunktForBeregning.plusWeeks(2)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.DP);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.DP);
    }

    @Test
    void skalFastsetteStatusATFLNårAktivitetErArbeidsinntektOgSykepengerOpphørtToDagerFørSP(){
        // Arrange
        var aktivPeriode = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)), ARBEIDSFORHOLD);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(2), skjæringstidspunktForBeregning.minusDays(2)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.ATFL);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        var bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatus.getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatus.getArbeidsforholdList().get(0).getOrgnr()).isEqualTo(ORGNR);
    }

    @Test
    void skalFastsetteStatusArbeidstakerNårAktivitetErArbeidsinntektOgSykepengerOpphørt1DagFørSP(){
        // Arrange
        var aktivPeriode = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)), ARBEIDSFORHOLD);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(2), skjæringstidspunktForBeregning.minusDays(1)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.ATFL);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        var bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatus.getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatus.getArbeidsforholdList().get(0).getOrgnr()).isEqualTo(ORGNR);
    }

    @Test
    void skalFastsetteStatusKUNTYNårAktivitetErSvangerskapspenger(){
        // Arrange
        var aktivPeriode = AktivPeriode.forAndre(Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(1), skjæringstidspunktForBeregning.plusWeeks(3)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);
        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.KUN_YTELSE);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BA);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().get(0).getArbeidsforholdList()).isEmpty();
    }

    @Test
    void skalFastsetteStatusKUNTYNårAktivitetErKunSykepengerPåSkjæringstidspunktet() {
        var aktivPeriode = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusMonths(12), skjæringstidspunktForBeregning.minusDays(2)), ARBEIDSFORHOLD);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        var aktivPeriode2 = new AktivPeriode(Aktivitet.FRILANSINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusMonths(12), skjæringstidspunktForBeregning.minusMonths(2)), Arbeidsforhold.frilansArbeidsforhold());
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        var aktivPeriode3 = AktivPeriode.forAndre(Aktivitet.SYKEPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(1), skjæringstidspunktForBeregning.plusWeeks(3)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode3);
        assertThat(regelmodell.getAktivePerioder()).hasSize(3);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.KUN_YTELSE);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BA);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().get(0).getArbeidsforholdList()).isEmpty();
    }


    @Test
    void skalFastsetteStatusATFLNårKombinasjonerAvAktivitetErArbeidsinntektOgSykepenger(){
        // Arrange
        var aktivPeriode = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)), ARBEIDSFORHOLD);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.SYKEPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(1), skjæringstidspunktForBeregning.plusWeeks(1)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).hasSize(1);
        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.ATFL);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        var bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatus.getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatus.getArbeidsforholdList().get(0).getOrgnr()).isEqualTo(ORGNR);
    }

    @Test
    void skalFastsetteStatusAAPVedKombinasjonerAvTYOgAAP(){
        // Arrange
        var aktivPeriode = AktivPeriode.forAndre(Aktivitet.AAP_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.FORELDREPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(1), skjæringstidspunktForBeregning.plusWeeks(1)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).hasSize(1);
        assertThat(regelmodell.getAktivitetStatuser()).containsExactlyInAnyOrder(AktivitetStatus.AAP);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        var bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.AAP);
    }

    @Test
    void skalFastsetteStatusDPVedKombinasjonerAvTYOgDP(){
        // Arrange
        var aktivPeriode = AktivPeriode.forAndre(Aktivitet.DAGPENGEMOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusDays(1), skjæringstidspunktForBeregning.plusWeeks(3)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.PLEIEPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusDays(3)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).hasSize(1);
        assertThat(regelmodell.getAktivitetStatuser()).containsExactlyInAnyOrder(AktivitetStatus.DP);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        var bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.DP);
    }

    @Test
    void skalFastsetteStatusATFL_SNVedKombinasjonerAvAktivitetFrilanserOgNæringsinntekt(){
        // Arrange
        var aktivPeriode = new AktivPeriode(Aktivitet.FRILANSINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusDays(3), skjæringstidspunktForBeregning.plusWeeks(2)), Arbeidsforhold.frilansArbeidsforhold());
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = new AktivPeriode(Aktivitet.NÆRINGSINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(1), skjæringstidspunktForBeregning.plusDays(3)), null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.ATFL_SN);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(2);
        var bgPrStatuser = regelmodell.getBeregningsgrunnlagPrStatusListe();
        assertThat(bgPrStatuser.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatuser.get(0).getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatuser.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
        assertThat(bgPrStatuser.get(1).getArbeidsforholdList()).isEmpty();
    }

    @Test
    void skalFastsetteStatusATFL_SNogDPVedKombinasjonerAvAktivitetArbeidsinntektNæringsinntektogMilitær(){
        // Arrange
        var aktivPeriode = new AktivPeriode(Aktivitet.FRILANSINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusDays(3), skjæringstidspunktForBeregning.plusWeeks(2)), Arbeidsforhold.frilansArbeidsforhold());
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = new AktivPeriode(Aktivitet.NÆRINGSINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(1), skjæringstidspunktForBeregning.plusDays(3)), null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = new AktivPeriode(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForBeregning.minusWeeks(4), skjæringstidspunktForBeregning.plusDays(5)), null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(3);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).containsExactlyInAnyOrder(AktivitetStatus.ATFL_SN);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(2);
        var bgPrStatuser = regelmodell.getBeregningsgrunnlagPrStatusListe();
        assertThat(bgPrStatuser.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatuser.get(0).getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatuser.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
        assertThat(bgPrStatuser.get(1).getArbeidsforholdList()).isEmpty();
    }

    @Test
    void skalFastsetteStatusMSNårBareErMilitærPåStp(){
        // Arrange
        var aktivPeriode = new AktivPeriode(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForBeregning.minusMonths(4),
            skjæringstidspunktForBeregning.plusDays(5)), null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);

        // Actautoaut
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
	    assertThat(regelmodell.getAktivitetStatuser()).containsExactlyInAnyOrder(AktivitetStatus.MS);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        var bgPrStatuser = regelmodell.getBeregningsgrunnlagPrStatusListe();
        assertThat(bgPrStatuser.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.MS);
        assertThat(bgPrStatuser.get(0).getArbeidsforholdList()).isEmpty();
    }

    @Test
    void skalFastsetteStatusATFLNårErBådeArbeidstakerOgMilitærPåStp(){
        // Arrange
        var aktivPeriode = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusMonths(8), skjæringstidspunktForBeregning), ARBEIDSFORHOLD);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = new AktivPeriode(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForBeregning.minusMonths(4), skjæringstidspunktForBeregning), null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
	    @SuppressWarnings("unused") var resultat = new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelmodell);

        // Assert
        assertThat(regelmodell.getAktivitetStatuser()).containsExactlyInAnyOrder(AktivitetStatus.ATFL);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        var bgPrStatuser = regelmodell.getBeregningsgrunnlagPrStatusListe();
        assertThat(bgPrStatuser.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatuser.get(0).getArbeidsforholdList()).hasSize(1);
    }

}
