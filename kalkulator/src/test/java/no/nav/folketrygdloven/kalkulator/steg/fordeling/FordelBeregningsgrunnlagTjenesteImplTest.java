package no.nav.folketrygdloven.kalkulator.steg.fordeling;

import static no.nav.folketrygdloven.kalkulator.OpprettKravPerioderFraInntektsmeldinger.opprett;
import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLTest.GRUNNBELØP;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class FordelBeregningsgrunnlagTjenesteImplTest {

    private static final String ORGNR1 = "995428563";
    private static final String ORGNR2 = "974652269";
    private static final String ORGNR3 = "973861778";
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 4);

    private FordelBeregningsgrunnlagTjenesteImpl fordelBeregningsgrunnlagTjeneste;
    private List<BeregningAktivitetDto> aktiviteter = new ArrayList<>();
    private BeregningAktivitetAggregatDto beregningAktivitetAggregat;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;
    private FordelPerioderTjeneste fordelPerioderTjeneste;


    @BeforeEach
    public void oppsett() {
        fordelPerioderTjeneste = lagTjeneste();
        fordelBeregningsgrunnlagTjeneste = new FordelBeregningsgrunnlagTjenesteImpl();
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(iayGrunnlagBuilder, List.of(ORGNR1, ORGNR2, ORGNR3));
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        aktiviteter.forEach(builder::leggTilAktivitet);
        beregningAktivitetAggregat = builder.build();
    }

    @Test
    public void skal_omfordele_når_refusjon_overstiger_beregningsgrunnlag_for_ein_andel() {
        // Arrange
        // Beregningsgrunnlag fra Foreslå
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(120_000);
        Map<String, BigDecimal> orgnrsBeregnetMap = new HashMap<>();
        orgnrsBeregnetMap.put(ORGNR1, beregnetPrÅr1);
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(180_000);
        orgnrsBeregnetMap.put(ORGNR2, beregnetPrÅr2);
        BigDecimal beregnetPrÅr3 = BigDecimal.valueOf(240_000);
        orgnrsBeregnetMap.put(ORGNR3, beregnetPrÅr3);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(orgnrsBeregnetMap, beregningAktivitetAggregat);

        // Inntektsmelding
        var inntektPrMnd1 = Beløp.fra(10_000);
        var refusjonPrMnd1 = Beløp.fra(20_000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR1, SKJÆRINGSTIDSPUNKT, refusjonPrMnd1, inntektPrMnd1);
        var inntektPrMnd2 = Beløp.fra(15_000);
        var refusjonPrMnd2 = Beløp.fra(15_000);
        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, SKJÆRINGSTIDSPUNKT, refusjonPrMnd2, inntektPrMnd2);
        var inntektPrMnd3 = Beløp.fra(20_000);
        var refusjonPrMnd3 = Beløp.ZERO;
        var im3 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR3, SKJÆRINGSTIDSPUNKT, refusjonPrMnd3, inntektPrMnd3);
        var inntektsmeldinger = List.of(im1, im2, im3);

        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null, opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning()), new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false))
                .medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        BeregningsgrunnlagRegelResultat periodisertBG = fordelPerioderTjeneste.fastsettPerioderForRefusjon(input);
        BeregningsgrunnlagGrunnlagDto periodisertGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag)
                .medBeregningsgrunnlag(periodisertBG.getBeregningsgrunnlag())
                .build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fordelBeregningsgrunnlagTjeneste.omfordelBeregningsgrunnlag(input.medBeregningsgrunnlagGrunnlag(periodisertGrunnlag)).getBeregningsgrunnlag();

        // Assert
        assertThat(nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(3);
        BeregningsgrunnlagPrStatusOgAndelDto andel1 = andeler.stream().filter(a -> a.getBgAndelArbeidsforhold().get().getArbeidsgiver().getIdentifikator().equals(ORGNR1)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andel2 = andeler.stream().filter(a -> a.getBgAndelArbeidsforhold().get().getArbeidsgiver().getIdentifikator().equals(ORGNR2)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andel3 = andeler.stream().filter(a -> a.getBgAndelArbeidsforhold().get().getArbeidsgiver().getIdentifikator().equals(ORGNR3)).findFirst().get();

        // Forventer at ORGNR1 har fått økt sitt brutto bg
        var forventetNyBruttoForArbeid1 = Beløp.fra(240_000);
        assertThat(andel1.getFordeltPrÅr()).isEqualByComparingTo(forventetNyBruttoForArbeid1);
        // Forventer at brutto for arbeid for ORGNR2 er uendret ettersom den ikkje har disponibelt grunnlag å fordele (søker full refusjon)
        assertThat(andel2.getFordeltPrÅr()).isNull();
        // Forventer at ORGNR2 har fått redusert sitt brutto bg
        var forventetNyBruttoForArbeid3 = Beløp.fra(120_000);
        assertThat(andel3.getFordeltPrÅr()).isEqualByComparingTo(forventetNyBruttoForArbeid3);
    }

    private FordelPerioderTjeneste lagTjeneste() {
        return new FordelPerioderTjeneste();
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(Map<String, BigDecimal> orgnrs,
                                                                BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        BeregningsgrunnlagPeriodeDto.Builder beregningsgrunnlagPeriodeBuilder = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, null, orgnrs);
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
        BeregningsgrunnlagDto bg = beregningsgrunnlagBuilder.build();
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagBeregningsgrunnlagPerioderBuilder(LocalDate fom, LocalDate tom, Map<String, BigDecimal> orgnrs) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.ny();
        for (String orgnr : orgnrs.keySet()) {
            Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
            BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                    .medBeregnetPrÅr(Beløp.fra(orgnrs.get(orgnr)))
                    .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                            .medArbeidsgiver(arbeidsgiver)
                            .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1)));
            builder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder);
        }
        return builder
                .medBeregningsgrunnlagPeriode(fom, tom);
    }

    private void leggTilYrkesaktiviteterOgBeregningAktiviteter(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, List<String> orgnrs) {
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        for (String orgnr : orgnrs) {
            Arbeidsgiver arbeidsgiver = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, orgnr);
            fjernOgLeggTilNyBeregningAktivitet(arbeidsperiode1.getFomDato(), arbeidsperiode1.getTomDato(), arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        }
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(registerBuilder);
    }

    private void fjernOgLeggTilNyBeregningAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (fom.isAfter(SKJÆRINGSTIDSPUNKT)) {
            throw new IllegalArgumentException("Kan ikke lage BeregningAktivitet som starter etter skjæringstidspunkt");
        }
        aktiviteter.add(lagAktivitet(fom, tom, arbeidsgiver, arbeidsforholdRef));
    }


    private BeregningAktivitetDto lagAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return BeregningAktivitetDto.builder()
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbeidsforholdRef)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .build();
    }

    private Arbeidsgiver leggTilYrkesaktivitet(Intervall arbeidsperiode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                               String orgnr) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        AktivitetsAvtaleDtoBuilder aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);
        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
        return arbeidsgiver;
    }


}
