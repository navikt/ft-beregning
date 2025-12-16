package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.StillingsprosentDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class BeregningsgrunnlagDtoUtilTest {

    private final InntektArbeidYtelseGrunnlagDto tomtIayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();

    @Test
    void skal_returnere_empty_om_ingen_opptjeningaktivitet_på_andel() {
        var andel = lagAndelUtenArbeidsgiver();
        var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), tomtIayGrunnlag);
        assertThat(arbeidsforhold).isEmpty();
    }

    @Test
    void skal_returnere_arbeidsforholdDto_om_virksomhet_som_arbeidsgiver_på_andel() {
        var orgnr = "973093681";
        var andel = lagAndelMedArbeidsgiver(Arbeidsgiver.virksomhet(orgnr), InternArbeidsforholdRefDto.nyRef());

        var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), tomtIayGrunnlag);

        assertThat(arbeidsforhold).isPresent();
        assertThat(arbeidsforhold.get().getArbeidsgiverIdent()).isEqualTo(orgnr);
    }

    @Test
    void skal_returnere_arbeidsforholdDto_om_privatperson_som_arbeidsgiver_på_andel() {
        var aktørId = AktørId.dummy();
        var andel = lagAndelMedArbeidsgiver(Arbeidsgiver.person(aktørId), InternArbeidsforholdRefDto.nyRef());

        var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), tomtIayGrunnlag);

        assertThat(arbeidsforhold).isPresent();
        assertThat(arbeidsforhold.get().getArbeidsgiverIdent()).isEqualTo(aktørId.getAktørId());
    }

    @Test
    void skal_gi_arbeidsforholdDto_med_stillingsprosenter_og_sisteLønnsendringsdato() {
        var stp = LocalDate.now();
        var orgnr = "973093681";
        var arbRef = InternArbeidsforholdRefDto.nyRef();
        var sisteLønnsendringsdato = stp.minusMonths(1);
        var periode = Intervall.fraOgMedTilOgMed(stp.minusMonths(10), stp.minusMonths(2));
        var periodeNy = Intervall.fraOgMedTilOgMed(periode.getTomDato().plusDays(1), TIDENES_ENDE);
        var stillingsprosent = BigDecimal.valueOf(80);
        var stillingsprosentNy = BigDecimal.valueOf(100);

        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder();
        var yaBuilder = lagYrkesaktivitetBuilder(arbRef, orgnr);
        leggTilAktivitet(yaBuilder, aktørArbeidBuilder, periode, sisteLønnsendringsdato, stillingsprosent);
        leggTilAktivitet(yaBuilder, aktørArbeidBuilder, periodeNy, null, stillingsprosentNy);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty()).medData(registerBuilder).build();

        var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagUtvidetArbeidsforholdDto(lagAndelMedArbeidsgiver(Arbeidsgiver.virksomhet(orgnr), arbRef),
            Optional.empty(), iayGrunnlag, Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(stp).build());

        assertThat(arbeidsforhold).isPresent();
        assertThat(arbeidsforhold.get().getArbeidsgiverIdent()).isEqualTo(orgnr);
        assertThat(arbeidsforhold.get().getArbeidsforholdId()).isEqualTo(arbRef.getReferanse());
        assertThat(arbeidsforhold.get().getSisteLønnsendringsdato()).isEqualTo(sisteLønnsendringsdato);
        assertThat(arbeidsforhold.get().getStillingsprosenter()).hasSize(2)
            .containsExactlyInAnyOrder(new StillingsprosentDto(stillingsprosent, periode.getFomDato(), periode.getTomDato()),
                new StillingsprosentDto(stillingsprosentNy, periodeNy.getFomDato(), periodeNy.getTomDato()));
    }

    @Test
    void skal_gi_arbeidsforholdDto_uten_stillingsprosenter_og_sisteLønnsendringsdato_ved_ingen_arbeidsavtaler() {
        var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagUtvidetArbeidsforholdDto(
            lagAndelMedArbeidsgiver(Arbeidsgiver.virksomhet("973093681"), InternArbeidsforholdRefDto.nyRef()), Optional.empty(), tomtIayGrunnlag,
            Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(LocalDate.now()).build());

        assertThat(arbeidsforhold).isPresent();
        assertThat(arbeidsforhold.get().getSisteLønnsendringsdato()).isNull();
        assertThat(arbeidsforhold.get().getStillingsprosenter()).isNull();
    }

    @Test
    void skal_gi_arbeidsforholdDto_med_seneste_sisteLønnsendringsdato_ved_flere_avtaler() {
        var stp = LocalDate.now();
        var orgnr = "973093681";
        var arbRef = InternArbeidsforholdRefDto.nyRef();
        var periode = Intervall.fraOgMedTilOgMed(stp.minusMonths(10), stp.plusMonths(5));
        var stillingsprosent = BigDecimal.valueOf(100);
        var sisteLønnsendringsdato = stp.minusMonths(9);
        var sisteLønnsendringsdatoNy = stp.minusMonths(2);

        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder();
        var yaBuilder = lagYrkesaktivitetBuilder(arbRef, orgnr);
        leggTilAktivitet(yaBuilder, aktørArbeidBuilder, periode, sisteLønnsendringsdato, stillingsprosent);
        leggTilAktivitet(yaBuilder, aktørArbeidBuilder, periode, sisteLønnsendringsdatoNy, stillingsprosent);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty()).medData(registerBuilder).build();

        var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagUtvidetArbeidsforholdDto(lagAndelMedArbeidsgiver(Arbeidsgiver.virksomhet(orgnr), arbRef),
            Optional.empty(), iayGrunnlag, Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(stp).build());

        assertThat(arbeidsforhold).isPresent();
        assertThat(arbeidsforhold.get().getSisteLønnsendringsdato()).isEqualTo(sisteLønnsendringsdatoNy);
    }

    @Test
    void skal_gi_arbeidsforholdDto_uten_stillingsprosenter_og_sisteLønnsendringsdato_ved_yrkesaktivitet_ikke_aktiv_på_skjæringstidspunkt() {
        var stp = LocalDate.now();
        var orgnr = "973093681";
        var arbRef = InternArbeidsforholdRefDto.nyRef();
        var periode = Intervall.fraOgMedTilOgMed(stp.minusYears(2), stp.minusMonths(8));

        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder();
        var yaBuilder = lagYrkesaktivitetBuilder(arbRef, orgnr);
        leggTilAktivitet(yaBuilder, aktørArbeidBuilder, periode, stp.minusMonths(10), BigDecimal.valueOf(70));
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty()).medData(registerBuilder).build();

        var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagUtvidetArbeidsforholdDto(lagAndelMedArbeidsgiver(Arbeidsgiver.virksomhet(orgnr), arbRef),
            Optional.empty(), iayGrunnlag, Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(stp).build());

        assertThat(arbeidsforhold).isPresent();
        assertThat(arbeidsforhold.get().getSisteLønnsendringsdato()).isNull();
        assertThat(arbeidsforhold.get().getStillingsprosenter()).isNull();
    }

    @Test
    void skal_gi_arbeidsforholdDto_uten_stillingsprosenter_og_sisteLønnsendringsdato_ved_skjæringstidspunkt_null() {
        var stp = LocalDate.now();
        var orgnr = "973093681";
        var arbRef = InternArbeidsforholdRefDto.nyRef();
        var periode = Intervall.fraOgMedTilOgMed(stp.minusMonths(10), stp.minusMonths(2));

        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder();
        var yaBuilder = lagYrkesaktivitetBuilder(arbRef, orgnr);
        leggTilAktivitet(yaBuilder, aktørArbeidBuilder, periode, stp.minusMonths(1), BigDecimal.valueOf(80));
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty()).medData(registerBuilder).build();

        var arbeidsforhold = BeregningsgrunnlagDtoUtil.lagUtvidetArbeidsforholdDto(lagAndelMedArbeidsgiver(Arbeidsgiver.virksomhet(orgnr), arbRef),
            Optional.empty(), iayGrunnlag, null);

        assertThat(arbeidsforhold).isPresent();
        assertThat(arbeidsforhold.get().getSisteLønnsendringsdato()).isNull();
        assertThat(arbeidsforhold.get().getStillingsprosenter()).isNull();
    }

    private YrkesaktivitetDtoBuilder lagYrkesaktivitetBuilder(InternArbeidsforholdRefDto arbRef, String orgnr) {
        return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsforholdId(arbRef)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr));
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagAndelUtenArbeidsgiver() {
        return lagAndelMedArbeidsgiver(null, null);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagAndelMedArbeidsgiver(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbRef) {
        var stp = LocalDate.now();
        var bg = BeregningsgrunnlagDto.builder().medGrunnbeløp(Beløp.fra(10)).medSkjæringstidspunkt(stp).build();
        var periode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(stp, null).build(bg);

        var builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(1L)
            .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        if (arbeidsgiver != null) {
            builder.medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdRef(arbRef));
        }

        return builder.build(periode);
    }

    private void leggTilAktivitet(YrkesaktivitetDtoBuilder yaBuilder,
                                  InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                  Intervall periode,
                                  LocalDate sisteLønnsendringsdato,
                                  BigDecimal stillingsprosent) {
        yaBuilder.leggTilAktivitetsAvtale(yaBuilder.getAktivitetsAvtaleBuilder(periode, true));
        yaBuilder.leggTilAktivitetsAvtale(yaBuilder.getAktivitetsAvtaleBuilder(periode, false)
            .medSisteLønnsendringsdato(sisteLønnsendringsdato)
            .medStillingsprosent(Stillingsprosent.fra(stillingsprosent)));
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
    }
}
