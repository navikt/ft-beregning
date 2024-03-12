package no.nav.folketrygdloven.kalkulator.steg.fortsettForeslå;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.testutilities.TestHjelper;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@ExtendWith(MockitoExtension.class)
public class FortsettForeslåBeregningsgrunnlagTest {

    private static final String ORGNR = "987123987";
    private static final Beløp MÅNEDSINNTEKT1 = Beløp.fra(12345);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.APRIL, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final Beløp GRUNNBELØP = Beløp.fra(90000);
    private static final String ARBEIDSFORHOLD_ORGNR1 = "654";
    private static final LocalDate ARBEIDSPERIODE_FOM = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final LocalDate ARBEIDSPERIODE_TOM = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(2);
    private final KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private final TestHjelper testHjelper = new TestHjelper();
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;
    private final FortsettForeslåBeregningsgrunnlag fortsettForeslåBeregningsgrunnlag = new FortsettForeslåBeregningsgrunnlag();

    @BeforeEach
    public void setup() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagATFL_SN() {
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.KOMBINERT_AT_SN));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                        .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                        .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1)))
                        .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                        .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                        .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1),
                                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1)))
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                        .medArbforholdType(OpptjeningAktivitetType.UDEFINERT)
                        .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                        .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                        .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                                .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                                .medArbeidsperiodeTom(LocalDate.now().plusYears(2)))));
        return beregningsgrunnlagBuilder.build();
    }

    private BGAndelArbeidsforholdDto.Builder lagBgAndelArbeidsforhold(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver) {
        return BGAndelArbeidsforholdDto.builder().medArbeidsperiodeFom(fom).medArbeidsperiodeTom(tom).medArbeidsgiver(arbeidsgiver);
    }

    private void lagKortvarigArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fomDato, LocalDate tomDato) {
        BeregningsgrunnlagPrStatusOgAndelDto andel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleBuilder = YrkesaktivitetDtoBuilder.nyAktivitetsAvtaleBuilder()
                .medPeriode(Intervall.fraOgMedTilOgMed(fomDato, tomDato));
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder
                .oppdatere(Optional.empty()).leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef())
                .medArbeidsgiver(andel.getArbeidsgiver().get()).build();
        Optional<InntektArbeidYtelseAggregatDto> registerVersjon = iayGrunnlagBuilder.getKladd().getRegisterVersjon();
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(registerVersjon, VersjonTypeDto.REGISTER);

        Optional<AktørArbeidDto> aktørArbeid = registerVersjon.map(InntektArbeidYtelseAggregatDto::getAktørArbeid);
        YrkesaktivitetDto ya = aktørArbeid.get().hentAlleYrkesaktiviteter()
                .stream()
                .filter(y -> y.equals(yrkesaktivitet))
                .findFirst().get();

        Intervall periode = ya.getAlleAktivitetsAvtaler().iterator().next().getPeriode();
        builder.getAktørArbeidBuilder()
                .getYrkesaktivitetBuilderForNøkkelAvType(new OpptjeningsnøkkelDto(yrkesaktivitet), ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato()), true)
                .medPeriode(Intervall.fraOgMedTilOgMed(fomDato, tomDato));

        iayGrunnlagBuilder.medData(builder);
    }

    @Test
    public void skalGiEittAvklaringsbehovForSNNyIArbeidslivetOgKortvarigArbeidsforhold() {
        // Arrange
        BeregningsgrunnlagDto nyttGrunnlag = lagBeregningsgrunnlagATFL_SN();
        InntektArbeidYtelseAggregatBuilder register = testHjelper.initBehandlingFor_AT_SN(MÅNEDSINNTEKT1.multipliser(12),
                2014, SKJÆRINGSTIDSPUNKT_BEREGNING, ARBEIDSFORHOLD_ORGNR1,
                MÅNEDSINNTEKT1, MÅNEDSINNTEKT1,
                iayGrunnlagBuilder);
        iayGrunnlagBuilder.medData(register);
        FaktaAggregatDto faktaAggregat = lagFakta(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), true, true);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        List<InntektsmeldingDto> inntektsmeldinger = Collections.emptyList();

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(nyttGrunnlag, inntektsmeldinger, faktaAggregat);

        // Assert
        BeregningsgrunnlagDto bg = resultat.getBeregningsgrunnlag();
        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(1);
        bg.getBeregningsgrunnlagPerioder().forEach(p -> assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2));
        List<BeregningAvklaringsbehovResultat> aps = resultat.getAvklaringsbehov();
        List<AvklaringsbehovDefinisjon> apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.FASTSETT_BG_SN_NY_I_ARB_LIVT);
    }

    private BeregningsgrunnlagRegelResultat act(BeregningsgrunnlagDto beregningsgrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger, FaktaAggregatDto faktaAggregat) {
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagGrunnlagDtoBuilder bgGrunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medFaktaAggregat(faktaAggregat)
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, OpptjeningAktivitetType.ARBEID));
        FortsettForeslåBeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagFortsettForeslåttBeregningsgrunnlagInput(koblingReferanse, bgGrunnlagBuilder, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);
        return fortsettForeslåBeregningsgrunnlag.fortsettForeslåBeregningsgrunnlag(input);
    }

    private FaktaAggregatDto lagFakta(Arbeidsgiver virksomhet, boolean erTidsbegrenset, Boolean erNyIArbeidslivet) {
        return FaktaAggregatDto.builder()
                .medFaktaAktør(erNyIArbeidslivet == null ? null : FaktaAktørDto.builder().medErNyIArbeidslivetSNFastsattAvSaksbehandler(erNyIArbeidslivet).build())
                .erstattEksisterendeEllerLeggTil(new FaktaArbeidsforholdDto.Builder(virksomhet, InternArbeidsforholdRefDto.nullRef())
                        .medErTidsbegrensetFastsattAvSaksbehandler(erTidsbegrenset)
                        .build()).build();
    }

}
