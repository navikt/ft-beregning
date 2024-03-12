package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.NaturalYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

public class FastsettNaturalytelsePerioderTjenesteImplTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 4);
    private static final Beløp GRUNNBELØP = Beløp.fra(90000L);
    private static final String ORG_NUMMER = "974652269";
    private static final Intervall ARBEIDSPERIODE = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
    private BeregningAktivitetAggregatDto beregningAktivitetAggregat;
    private List<BeregningAktivitetDto> aktiviteter = new ArrayList<>();
    private FastsettNaturalytelsePerioderTjeneste tjeneste;
    private KoblingReferanse behandlingRef = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;

    @BeforeEach
    public void setUp() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        tjeneste = new FastsettNaturalytelsePerioderTjeneste();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER), iayGrunnlagBuilder);

    }

    private void leggTilYrkesaktiviteterOgBeregningAktiviteter(List<String> orgnrs, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getAktørArbeidFraRegister());
        for (String orgnr : orgnrs) {
            Arbeidsgiver arbeidsgiver = leggTilYrkesaktivitet(ARBEIDSPERIODE, aktørArbeidBuilder, orgnr);
            fjernOgLeggTilNyBeregningAktivitet(ARBEIDSPERIODE.getFomDato(), ARBEIDSPERIODE.getTomDato(), arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        }

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER)
                .leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(inntektArbeidYtelseAggregatBuilder);
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

    private void fjernOgLeggTilNyBeregningAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (fom.isAfter(SKJÆRINGSTIDSPUNKT)) {
            throw new IllegalArgumentException("Kan ikke lage BeregningAktivitet som starter etter skjæringstidspunkt");
        }
        fjernAktivitet(arbeidsgiver, arbeidsforholdRef);
        aktiviteter.add(lagAktivitet(fom, tom, arbeidsgiver, arbeidsforholdRef));
        lagAggregatEntitetFraListe(aktiviteter);
    }

    private void fjernAktivitet(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        aktiviteter.stream()
                .filter(a -> a.gjelderFor(arbeidsgiver, arbeidsforholdRef)).findFirst()
                .ifPresent(a -> aktiviteter.remove(a));
        lagAggregatEntitetFraListe(aktiviteter);
    }

    private void lagAggregatEntitetFraListe(List<BeregningAktivitetDto> aktiviteter) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        aktiviteter.forEach(builder::leggTilAktivitet);
        beregningAktivitetAggregat = builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
    }

    private BeregningAktivitetDto lagAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return BeregningAktivitetDto.builder()
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbeidsforholdRef)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .build();
    }

    private BeregningsgrunnlagDto fastsettPerioderForNaturalytelse(KoblingReferanse ref,
                                                                   BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                   BeregningsgrunnlagDto beregningsgrunnlag) {
        var input = new BeregningsgrunnlagInput(ref, iayGrunnlagBuilder.build(), null, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        return tjeneste.fastsettPerioderForNaturalytelse(input, beregningsgrunnlag).getBeregningsgrunnlag();
    }

    @Test
    public void lagPeriodeForNaturalytelseTilkommer() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        var inntekt = Beløp.fra(40000);
        NaturalYtelseDto naturalYtelseTilkommer = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.plusDays(30), TIDENES_ENDE, Beløp.fra(350),
                NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
                naturalYtelseTilkommer);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag
        );

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(29));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(30), TIDENES_ENDE, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
    }

    @Test
    public void lagPeriodeForNaturalytelseBortfalt() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        var inntekt = Beløp.fra(40000);
        NaturalYtelseDto naturalYtelseBortfall = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusDays(30),
                Beløp.fra(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
                naturalYtelseBortfall);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag
        );

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(30));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(31), TIDENES_ENDE, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
    }

    @Test
    public void ikkeLagPeriodeForNaturalytelseBortfaltPåStp() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        var inntekt = Beløp.fra(40000);
        NaturalYtelseDto naturalYtelseBortfall = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.minusDays(1),
                Beløp.fra(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
                naturalYtelseBortfall);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag
        );

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
    }

    @Test
    public void lagPeriodeForNaturalytelseBortfaltDagenEtterStp() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        var inntekt = Beløp.fra(40000);
        NaturalYtelseDto naturalYtelseBortfall = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT, Beløp.fra(350),
                NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
                naturalYtelseBortfall);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag
        );

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT);
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
    }

    @Test
    public void lagPerioderForNaturalytelseBortfaltOgTilkommer() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        var inntekt = Beløp.fra(40000);
        NaturalYtelseDto naturalYtelseBortfalt = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusDays(30),
                Beløp.fra(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        NaturalYtelseDto naturalYtelseTilkommer = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.plusDays(90), TIDENES_ENDE, Beløp.fra(350),
                NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
                naturalYtelseBortfalt, naturalYtelseTilkommer);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag
        );

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(30));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(31), SKJÆRINGSTIDSPUNKT.plusDays(89), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusDays(90), TIDENES_ENDE, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
    }

    @Test
    public void skalKasteFeilHvisAntallPerioderErMerEnn1() {
        // Arrange
        BeregningsgrunnlagPeriodeDto.Builder periode1 = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT, List.of(ORG_NUMMER));
        BeregningsgrunnlagPeriodeDto.Builder periode2 = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT.plusDays(1), null, List.of(ORG_NUMMER));
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilBeregningsgrunnlagPeriode(periode1)
                .leggTilBeregningsgrunnlagPeriode(periode2)
                .build();
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Assert
        // Act
        Assertions.assertThrows(IllegalStateException.class, () -> {
            var input = new BeregningsgrunnlagInput(behandlingRef, null, null, List.of(), null)
                    .medBeregningsgrunnlagGrunnlag(grunnlag);
            tjeneste.fastsettPerioderForNaturalytelse(input, beregningsgrunnlag);
        });
    }

    private void assertBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, LocalDate expectedFom, LocalDate expectedTom,
                                                 PeriodeÅrsak... perioderÅrsaker) {
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()).as("fom").isEqualTo(expectedFom);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()).as("tom").isEqualTo(expectedTom);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).as("periodeÅrsaker").containsExactlyInAnyOrder(perioderÅrsaker);
    }


    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlagMedOverstyring(List<String> orgnrs,
                                                                              BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        return lagBeregningsgrunnlag(orgnrs, beregningAktivitetAggregat, null);
    }


    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(List<String> orgnrs,
                                                                BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                BeregningAktivitetOverstyringerDto BeregningAktivitetOverstyringer) {
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
                .medOverstyring(BeregningAktivitetOverstyringer)
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagBeregningsgrunnlagPerioderBuilder(LocalDate fom, LocalDate tom, List<String> orgnrs) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.ny();
        for (String orgnr : orgnrs) {
            Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
            BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                            .medArbeidsgiver(arbeidsgiver)
                            .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1)));
            builder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder);
        }
        return builder
                .medBeregningsgrunnlagPeriode(fom, tom);
    }

}
