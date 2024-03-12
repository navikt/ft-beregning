package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.folketrygdloven.utils.TestKonfigurasjonVerdiProvider;

public class VurderMottarYtelseTjenesteTest {

    private static final TestKonfigurasjonVerdiProvider TEST_KONFIG = new TestKonfigurasjonVerdiProvider();
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final String ORGNR = "3482934982384";
    private static final InternArbeidsforholdRefDto ARB_ID = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;

    @BeforeAll
    public static void initAll() {
        TEST_KONFIG.clear();
        KonfigurasjonVerdi.clear();
        KonfigurasjonVerdi.configure(TEST_KONFIG);
    }

    @AfterAll
    public static void teardownAll() {
        KonfigurasjonVerdi.clear();
        TEST_KONFIG.clear();
    }

    @BeforeEach
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(91425))
                .build();
        periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(beregningsgrunnlag);
        TEST_KONFIG.put("VURDER_MOTTAR_YTELSE_AT_FILTRERING", "false");
        TEST_KONFIG.put("VURDER_MOTTAR_YTELSE_FL_FILTRERING", "false");
    }

    @Test
    public void skal_gi_frilanser() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(periode);

        // Act
        boolean erFrilanser = VurderMottarYtelseTjeneste.erFrilanser(beregningsgrunnlag);

        // Assert
        assertThat(erFrilanser).isTrue();
    }

    @Test
    public void skal_ikkje_gi_frilanser() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                .build(periode);

        // Act
        boolean erFrilanser = VurderMottarYtelseTjeneste.erFrilanser(beregningsgrunnlag);

        // Assert
        assertThat(erFrilanser).isFalse();
    }

    @Test
    public void skal_ikke_vurdere_mottar_ytelse_for_frilans_uten_ytelse() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(periode);

        // Act
        boolean skalVurdereMottarYtelse = VurderMottarYtelseTjeneste.skalVurdereMottattYtelse(beregningsgrunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), Collections.emptyList());

        // Assert
        assertThat(skalVurdereMottarYtelse).isFalse();
    }

    @Test
    public void skal_vurdere_mottar_ytelse_for_frilans_med_ytelse() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(periode);

        // Act
        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlagDto = lagIAYMedYtelseMedFrilansAnvisning();
        boolean skalVurdereMottarYtelse = VurderMottarYtelseTjeneste.skalVurdereMottattYtelse(beregningsgrunnlag, inntektArbeidYtelseGrunnlagDto, Collections.emptyList());

        // Assert
        assertThat(skalVurdereMottarYtelse).isTrue();
    }

    @Test
    public void skal_ikke_vurdere_mottar_ytelse_for_frilans_uten_mottatt_ytelse_for_frilans() {
        TEST_KONFIG.put("VURDER_MOTTAR_YTELSE_FL_FILTRERING", "true");

        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(periode);

        // Act
        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlagDto = lagIAYMedYtelse(Arbeidsgiver.virksomhet(ORGNR), true);
        boolean skalVurdereMottarYtelse = VurderMottarYtelseTjeneste.skalVurdereMottattYtelse(beregningsgrunnlag, inntektArbeidYtelseGrunnlagDto, Collections.emptyList());

        // Assert
        assertThat(skalVurdereMottarYtelse).isFalse();
    }

    @Test
    public void skal_vurdere_mottar_ytelse_for_frilans_med_mottatt_ytelse_for_frilans() {
        TEST_KONFIG.put("VURDER_MOTTAR_YTELSE_FL_FILTRERING", "true");

        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(periode);

        // Act
        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlagDto = lagIAYMedYtelseMedFrilansAnvisning();
        boolean skalVurdereMottarYtelse = VurderMottarYtelseTjeneste.skalVurdereMottattYtelse(beregningsgrunnlag, inntektArbeidYtelseGrunnlagDto, Collections.emptyList());

        // Assert
        assertThat(skalVurdereMottarYtelse).isTrue();
    }

    @Test
    public void skal_vurdere_mottary_ytelse_for_frilans_og_arbeidstaker_uten_inntektsmelding_med_ytelse() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(periode);

        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        leggTilAktivitet(ARB_ID, ORGNR, Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10)), aktørArbeidBuilder, Optional.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2L)));
        oppdatere.leggTilAktørInntekt(lagAktørInntektMedYtelse());
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(oppdatere)
                .build();

        // Act
        boolean skalVurdereMottarYtelse = VurderMottarYtelseTjeneste.skalVurdereMottattYtelse(beregningsgrunnlag, iayGrunnlag, Collections.emptyList());

        // Assert
        assertThat(skalVurdereMottarYtelse).isTrue();
    }


    @Test
    public void skal_vurdere_mottar_ytelse_for_arbeidstaker_uten_inntektsmelding_med_ytelse() {
        TEST_KONFIG.put("VURDER_MOTTAR_YTELSE_AT_FILTRERING", "true");

        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(periode);

        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        leggTilAktivitet(ARB_ID, ORGNR, Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10)), aktørArbeidBuilder, Optional.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2L)));
        oppdatere.leggTilAktørInntekt(lagAktørInntektMedYtelse());
        oppdatere.leggTilAktørYtelse(lagAktørYtelseForArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR), false));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(oppdatere)
                .build();

        // Act
        boolean skalVurdereMottarYtelse = VurderMottarYtelseTjeneste.skalVurdereMottattYtelse(beregningsgrunnlag, iayGrunnlag, Collections.emptyList());

        // Assert
        assertThat(skalVurdereMottarYtelse).isTrue();
    }

    @Test
    public void skal_ikke_vurdere_mottar_ytelse_for_arbeidstaker_uten_inntektsmelding_når_ytelse_er_utbetalt_til_arbeidsgiver() {
        TEST_KONFIG.put("VURDER_MOTTAR_YTELSE_AT_FILTRERING", "true");

        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(periode);

        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        leggTilAktivitet(ARB_ID, ORGNR, Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10)), aktørArbeidBuilder, Optional.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2L)));
        oppdatere.leggTilAktørInntekt(lagAktørInntektMedYtelse());
        oppdatere.leggTilAktørYtelse(lagAktørYtelseForArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR), true));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(oppdatere)
                .build();

        // Act
        boolean skalVurdereMottarYtelse = VurderMottarYtelseTjeneste.skalVurdereMottattYtelse(beregningsgrunnlag, iayGrunnlag, Collections.emptyList());

        // Assert
        assertThat(skalVurdereMottarYtelse).isFalse();
    }


    private InntektArbeidYtelseGrunnlagDto lagIAYMedYtelseMedFrilansAnvisning() {
        InntektArbeidYtelseAggregatBuilder register = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        register.leggTilAktørInntekt(lagAktørInntektMedYtelse()).build();
        register.leggTilAktørYtelse(lagAktørYtelseForFrilans());
        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlagDto = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(register)
                .build();
        return inntektArbeidYtelseGrunnlagDto;
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYMedYtelse(Arbeidsgiver arbeidsgiver, boolean fullRefusjon) {
        InntektArbeidYtelseAggregatBuilder register = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        register.leggTilAktørInntekt(lagAktørInntektMedYtelse()).build();
        register.leggTilAktørYtelse(lagAktørYtelseForArbeidsgiver(arbeidsgiver, fullRefusjon));
        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlagDto = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(register)
                .build();
        return inntektArbeidYtelseGrunnlagDto;
    }


    private InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder lagAktørYtelseForFrilans() {
        return InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty())
                .leggTilYtelse(YtelseDtoBuilder.ny().medYtelseType(YtelseType.SYKEPENGER)
                        .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING))
                        .leggTilYtelseAnvist(YtelseAnvistDtoBuilder.ny()
                                .medBeløp(Beløp.fra(10))
                                .medDagsats(Beløp.fra(10))
                                .medUtbetalingsgradProsent(Stillingsprosent.HUNDRED)
                                .medAnvistPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING))
                                .medAnvisteAndeler(List.of(lagAnvistAndelFrilans()))
                                .build()));
    }

    private InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder lagAktørYtelseForArbeidsgiver(Arbeidsgiver arbeidsgiver, boolean fullRefusjon) {
        return InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty())
                .leggTilYtelse(YtelseDtoBuilder.ny().medYtelseType(YtelseType.SYKEPENGER)
                        .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING))
                        .leggTilYtelseAnvist(YtelseAnvistDtoBuilder.ny()
                                .medBeløp(Beløp.fra(10))
                                .medDagsats(Beløp.fra(10))
                                .medAnvistPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING))
                                .medUtbetalingsgradProsent(Stillingsprosent.HUNDRED)
                                .medAnvisteAndeler(List.of(lagAnvistAndelArbeid(arbeidsgiver, fullRefusjon)))
                                .build()));
    }

    private AnvistAndel lagAnvistAndelArbeid(Arbeidsgiver arbeidsgiver, boolean fullRefusjon) {
        return new AnvistAndel(arbeidsgiver,
                Beløp.fra(10),
                fullRefusjon ? Stillingsprosent.HUNDRED : Stillingsprosent.fra(BigDecimal.TEN),
                Inntektskategori.ARBEIDSTAKER);
    }

    private AnvistAndel lagAnvistAndelFrilans() {
        return new AnvistAndel(null,
                Beløp.fra(10),
                Stillingsprosent.ZERO,
                Inntektskategori.FRILANSER);
    }

    private InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder lagAktørInntektMedYtelse() {
        return InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty())
                .leggTilInntekt(InntektDtoBuilder.oppdatere(Optional.empty())
                        .medInntektsKilde(InntektskildeType.INNTEKT_SAMMENLIGNING)
                        .leggTilInntektspost(InntektspostDtoBuilder.ny()
                                .medPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1).withDayOfMonth(1),
                                        SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                                .medInntektspostType(InntektspostType.YTELSE)
                                .medBeløp(Beløp.fra(10))));
    }


    private void leggTilAktivitet(InternArbeidsforholdRefDto arbId, String orgnr, Intervall periode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder, Optional<LocalDate> sisteLønnsendringsdato) {
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(arbId)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, true));
        sisteLønnsendringsdato.ifPresent(d -> yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, false).medSisteLønnsendringsdato(d)));
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
    }


}
