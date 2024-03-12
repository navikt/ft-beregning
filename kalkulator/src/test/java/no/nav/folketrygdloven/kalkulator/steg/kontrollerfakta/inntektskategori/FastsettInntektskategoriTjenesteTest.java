package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.inntektskategori;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;

public class FastsettInntektskategoriTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final Beløp GRUNNBELØP = Beløp.fra(90000);
    private static final String ARBEIDSFORHOLD_ORGNR = "973152351";

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(AktivitetStatus aktivitetStatus) {
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
            .medAktivitetStatus(aktivitetStatus)
            .build(beregningsgrunnlag);
        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        var bga = BGAndelArbeidsforholdDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR));
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(aktivitetStatus)
            .medBeregningsperiode(LocalDate.of(2018,2, 1), LocalDate.of(2018,4,30))
            .build(periode);
        return beregningsgrunnlag;
    }


    private InntektArbeidYtelseGrunnlagDto opprettOppgittOpptjening(List<VirksomhetType> næringtyper) {
        var oob = OppgittOpptjeningDtoBuilder.ny();
        ArrayList<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> egneNæringBuilders = new ArrayList<>();
        LocalDate fraOgMed = LocalDate.now().minusMonths(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        Intervall periode = Intervall.fraOgMedTilOgMed(fraOgMed, tilOgMed);
        for (VirksomhetType type : næringtyper) {
            egneNæringBuilders.add(OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny().medVirksomhetType(type).medPeriode(periode));
        }
        oob.leggTilEgneNæringer(egneNæringBuilders);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medOppgittOpptjening(oob).build();
    }

    private InntektspostDtoBuilder lagInntektspost(int månederFørStp, SkatteOgAvgiftsregelType skatteOgAvgiftsregelType) {
        LocalDate fom = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(månederFørStp).withDayOfMonth(1);
        LocalDate tom = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(månederFørStp).with(TemporalAdjusters.lastDayOfMonth());
        return InntektspostDtoBuilder.ny()
                .medInntektspostType(InntektspostType.LØNN)
                .medPeriode(fom, tom).medBeløp(Beløp.fra(1))
                .medSkatteOgAvgiftsregelType(skatteOgAvgiftsregelType);
    }

    private InntektArbeidYtelseGrunnlagDto lagIAY(InntektDtoBuilder inntektBuilder) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty()).leggTilInntekt(inntektBuilder);
        InntektArbeidYtelseAggregatBuilder data = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER).leggTilAktørInntekt(aktørInntektBuilder);
        var iay = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medData(data).build();
        return iay;
    }

    @Test
    public void arbeidstakerSkalTilRiktigInntektskategori() {
        // Arrange
        var grunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        var periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void frilanserSkalTilRiktigInntektskategori() {
        // Arrange
        var grunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.FRILANSER);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        var periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
    }


    @Test
    public void dagpengerSkalTilRiktigInntektskategori() {
        // Arrange
        var grunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.DAGPENGER);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.DAGPENGER);
    }

    @Test
    public void arbeidsavklaringspengerSkalTilRiktigInntektskategori() {
        // Arrange
        var grunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSAVKLARINGSPENGER);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSAVKLARINGSPENGER);
    }


    @Test
    public void SNUtenFiskeJordbrukEllerDagmammaSkalTilRiktigInntektskategori() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.ANNEN));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    public void SNMedFiskeSkalTilRiktigInntektskategori() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.FISKE));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void SNMedJorbrukSkalTilRiktigInntektskategori() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.JORDBRUK_SKOGBRUK));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void SNMedDagmammaSkalTilRiktigInntektskategori() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.DAGMAMMA));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.DAGMAMMA);
    }

    @Test
    public void SNMedFiskeOgJordbrukSkalMappeTilInntektskategoriFisker() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.FISKE, VirksomhetType.JORDBRUK_SKOGBRUK));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void SNMedFiskeOgDagmammaSkalMappeTilInntektskategoriFisker() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.DAGMAMMA, VirksomhetType.FISKE));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void SNMedJordbrukOgDagmammaSkalMappeTilInntektskategoriJordbruker() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.DAGMAMMA, VirksomhetType.JORDBRUK_SKOGBRUK));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void SNMedJordbrukOgOrdinærNæringSkalMappeTilInntektskategoriJordbruker() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.ANNEN, VirksomhetType.JORDBRUK_SKOGBRUK));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void SNMedDagmammaOgOrdinærNæringSkalMappeTilInntektskategoriJordbruker() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.ANNEN, VirksomhetType.DAGMAMMA));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.DAGMAMMA);
    }

    @Test
    public void SNMedFiskeOgOrdinærNæringSkalMappeTilInntektskategoriFisker() {
        // Arrange
        var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.ANNEN, VirksomhetType.FISKE));
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        var periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void skal_gi_arbeidstaker_inntektskategori_når_ingen_fisker_inntekt_hos_det_arbeidsforholdet() {
        // Arrange
        var inntektBuilder = InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING).medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR));
        inntektBuilder.leggTilInntektspost(lagInntektspost(1,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(2,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(3,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(4,SkatteOgAvgiftsregelType.NETTOLØNN));
        var iay = lagIAY(inntektBuilder);
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, iay);

        // Assert
        var periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void skal_gi_arbeidstaker_inntektskategori_når_ingen_fisker_inntekt_siste_3_mnd() {
        // Arrange
        var inntektBuilder = InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING).medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR));
        inntektBuilder.leggTilInntektspost(lagInntektspost(1,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(2,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(3,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(4,SkatteOgAvgiftsregelType.SÆRSKILT_FRADRAG_FOR_SJØFOLK));
        var iay = lagIAY(inntektBuilder);
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, iay);

        // Assert
        var periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void skal_gi_inntektskategori_sjømann_når_fisker_inntekt_siste_3_mnd() {
        // Arrange
        var inntektBuilder = InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING).medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR));
        inntektBuilder.leggTilInntektspost(lagInntektspost(1,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(2,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        inntektBuilder.leggTilInntektspost(lagInntektspost(3,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(4,SkatteOgAvgiftsregelType.NETTOLØNN));
        var iay = lagIAY(inntektBuilder);
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, iay);

        // Assert
        var periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.SJØMANN);
    }

    @Test
    public void skal_gi_arbeidstaker_inntektskategori_når_fisker_inntekt_siste_3_mnd_i_annet_orgnr() {
        // Arrange
        var inntektBuilder = InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING).medArbeidsgiver(Arbeidsgiver.virksomhet("2333"));
        inntektBuilder.leggTilInntektspost(lagInntektspost(1,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        inntektBuilder.leggTilInntektspost(lagInntektspost(2,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        inntektBuilder.leggTilInntektspost(lagInntektspost(3,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        inntektBuilder.leggTilInntektspost(lagInntektspost(4,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        var iay = lagIAY(inntektBuilder);
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        var medFastsattInntektskategori = FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, iay);

        // Assert
        var periode = medFastsattInntektskategori.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void skalReturnereFiskerSomHøgastPrioriterteInntektskategori() {
        var inntektskategoriList = List.of(Inntektskategori.FISKER, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA, Inntektskategori.JORDBRUKER);
        Optional<Inntektskategori> prioritert = FastsettInntektskategoriTjeneste.finnHøyestPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void skalReturnereJordbrukerSomHøgastPrioriterteInntektskategori() {
        var inntektskategoriList = List.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA, Inntektskategori.JORDBRUKER);
        Optional<Inntektskategori> prioritert = FastsettInntektskategoriTjeneste.finnHøyestPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void skalReturnereDagmammaSomHøgastPrioriterteInntektskategori() {
        var inntektskategoriList = List.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA);
        Optional<Inntektskategori> prioritert = FastsettInntektskategoriTjeneste.finnHøyestPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.DAGMAMMA);
    }

    @Test
    public void skalReturnereSelvstendigNæringsdrivendeSomHøgastPrioriterteInntektskategori() {
        var inntektskategoriList = List.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        Optional<Inntektskategori> prioritert = FastsettInntektskategoriTjeneste.finnHøyestPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }
}
