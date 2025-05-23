package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class BeregningsgrunnlagDtoUtilTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final Beløp GRUNNBELØP = Beløp.fra(10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("123456789");
    public static final BeregningsgrunnlagPrStatusOgAndelDto ARBEIDSTAKER_ANDEL = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(1L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER))
            .build();


    @Test
    void arbeidsprosenter_for_uavsluttet_periode() {
        // Arrange
        var arbeidsprosent1 = Aktivitetsgrad.fra(20);
        List<AndelGradering.Gradering> graderinger = new ArrayList<>();
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));

        // Act
	    var arbeidsandeler = new FinnArbeidsprosenterFP().finnArbeidsprosenterIPeriode(ARBEIDSTAKER_ANDEL, lagForeldrepengerGrunnlag(graderinger), Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING));

        // Assert
        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1.verdi());
    }

    private ForeldrepengerGrunnlag lagForeldrepengerGrunnlag(List<AndelGradering.Gradering> graderinger) {
        var graderingBuilder = AndelGradering.builder();
        graderinger.forEach(graderingBuilder::leggTilGradering);
        graderingBuilder.medArbeidsgiver(ARBEIDSGIVER);
        graderingBuilder.medStatus(AktivitetStatus.ARBEIDSTAKER);
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false, new AktivitetGradering(List.of(graderingBuilder.build())));
        return foreldrepengerGrunnlag;
    }

    @Test
    void arbeidsprosenter_for_uavsluttet_periode_og_uavsluttet_gradering() {
        // Arrange
        var arbeidsprosent1 = Aktivitetsgrad.fra(20);
        List<AndelGradering.Gradering> graderinger = new ArrayList<>();
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, TIDENES_ENDE, arbeidsprosent1));

        // Act
	    var arbeidsandeler = new FinnArbeidsprosenterFP().finnArbeidsprosenterIPeriode(ARBEIDSTAKER_ANDEL, lagForeldrepengerGrunnlag(graderinger), Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING));

        // Assert
        assertThat(arbeidsandeler).containsExactly(arbeidsprosent1.verdi());
    }

    @Test
    void arbeidsprosenter_for_samanhengande_gradering_med_hull_på_slutten() {
        // Arrange
        var arbeidsprosent1 = Aktivitetsgrad.fra(20);
        var arbeidsprosent2 = Aktivitetsgrad.fra(30);
        var arbeidsprosent3 = Aktivitetsgrad.fra(40);
        var arbeidsprosent4 = Aktivitetsgrad.fra(50);
        List<AndelGradering.Gradering> graderinger = new ArrayList<>();
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), arbeidsprosent2));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3), arbeidsprosent3));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4), arbeidsprosent4));

        // Act
	    var arbeidsandeler = new FinnArbeidsprosenterFP().finnArbeidsprosenterIPeriode(ARBEIDSTAKER_ANDEL, lagForeldrepengerGrunnlag(graderinger), Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4).plusDays(1)));

        // Assert
        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1.verdi(), arbeidsprosent2.verdi(), arbeidsprosent3.verdi(), arbeidsprosent4.verdi());
    }

    @Test
    void arbeidsprosenter_for_ikkje_samanhengande_gradering() {
        // Arrange
        var arbeidsprosent1 = Aktivitetsgrad.fra(20);
        var arbeidsprosent2 = Aktivitetsgrad.fra(30);
        var arbeidsprosent3 = Aktivitetsgrad.fra(40);
        var arbeidsprosent4 = Aktivitetsgrad.fra(50);
        var arbeidsprosent5 = Aktivitetsgrad.fra(60);
        List<AndelGradering.Gradering> graderinger = new ArrayList<>();
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), arbeidsprosent2));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3), arbeidsprosent3));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4), arbeidsprosent4));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4).plusDays(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5), arbeidsprosent5));

        // Act
	    var arbeidsandeler = new FinnArbeidsprosenterFP().finnArbeidsprosenterIPeriode(ARBEIDSTAKER_ANDEL, lagForeldrepengerGrunnlag(graderinger), Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5)));

        // Assert
        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1.verdi(), arbeidsprosent2.verdi(), arbeidsprosent3.verdi(), arbeidsprosent4.verdi(), arbeidsprosent5.verdi());
    }

    @Test
    void skal_returnere_empty_om_ingen_opptjeningaktivitet_på_andel() {
        long andelsnr = 1;
        var bg = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);
        var andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(andelsnr)
            .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medArbforholdType(null)
            .build(periode);
	    var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build());
        assertThat(arbeidsforhold.isPresent()).isFalse();
    }

    @Test
    void skal_returnere_arbeidsforholdDto_om_virksomhet_som_arbeidsgiver_på_andel() {
        long andelsnr = 1;
	    var orgnr = "973093681";
	    var bg = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
	    var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);

	    var virksomhet = Arbeidsgiver.virksomhet(orgnr);
	    var andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(andelsnr)
            .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
            .build(periode);

	    var builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();

	    var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), builder.build());
        assertThat(arbeidsforhold.isPresent()).isTrue();
        assertThat(arbeidsforhold.get().getArbeidsgiverIdent()).isEqualTo(orgnr);
    }

    @Test
    void skal_returnere_arbeidsforholdDto_om_privatperson_som_arbeidsgiver_på_andel() {
        long andelsnr = 1;
	    var bg = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
	    var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);

	    var aktørId = AktørId.dummy();
	    var person = Arbeidsgiver.person(aktørId);
	    var andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(andelsnr)
            .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(person))
            .build(periode);
	    var builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();

	    var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), builder.build());
        assertThat(arbeidsforhold.isPresent()).isTrue();
        assertThat(arbeidsforhold.get().getArbeidsgiverIdent()).isEqualTo(aktørId.getAktørId());
    }
}
