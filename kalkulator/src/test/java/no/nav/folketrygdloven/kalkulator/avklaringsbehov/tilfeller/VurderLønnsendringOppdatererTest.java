package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderLønnsendringDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class VurderLønnsendringOppdatererTest {
    private static final Long ANDELSNR_ARBEIDSTAKER = 2L;
    private static final List<FaktaOmBeregningTilfelle> FAKTA_OM_BEREGNING_TILFELLER = Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING);
    public static final String ORGNR = "8934232423";
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = Beløp.fra(600000);

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPrStatusOgAndelDto frilansAndel;
    private BeregningsgrunnlagPrStatusOgAndelDto arbeidstakerAndel;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setup() {
        Arbeidsgiver virksomheten = Arbeidsgiver.virksomhet(ORGNR);
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()

                .medGrunnbeløp(GRUNNBELØP)
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilFaktaOmBeregningTilfeller(FAKTA_OM_BEREGNING_TILFELLER)
                .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
                .build(beregningsgrunnlag);
        frilansAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(3252L)
            .medInntektskategori(Inntektskategori.FRILANSER)
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .build(periode1);
        arbeidstakerAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT.minusMonths(3), SKJÆRINGSTIDSPUNKT.minusDays(1))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomheten))
                .medAndelsnr(ANDELSNR_ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode1);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                        .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                                        .medArbeidsgiver(virksomheten)
                                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medErAnsettelsesPeriode(true)
                                                .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10))))
                                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medErAnsettelsesPeriode(false)
                                                .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10)))
                                                .medSisteLønnsendringsdato(SKJÆRINGSTIDSPUNKT.minusMonths(1))))))
                .medInformasjon(ArbeidsforholdInformasjonDtoBuilder
                        .oppdatere(Optional.empty())
                        .leggTil(ArbeidsforholdOverstyringDtoBuilder.oppdatere(Optional.empty())
                                .medHandling(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING)
                                .medArbeidsgiver(virksomheten)).build()).build();
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                        .medBeregningsgrunnlag(beregningsgrunnlag),
                BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);
    }

    @Test
    public void skal_sette_lønnsendring_til_true_på_arbeidstakerandel() {
        // Arrange
        VurderLønnsendringDto lønnsendringDto = new VurderLønnsendringDto(true);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        dto.setVurdertLonnsendring(lønnsendringDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderLønnsendringOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);
        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        // Assert
        assertThat(faktaAggregat).isPresent();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold()).hasSize(1);
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(arbeidstakerAndel).get().getHarLønnsendringIBeregningsperiodenVurdering()).isTrue();
        assertThat(frilansAndel.getBgAndelArbeidsforhold()).isNotPresent();
    }

    @Test
    public void skal_ikkje_sette_lønnsendring_til_true_på_arbeidstakerandel() {
        // Arrange
        VurderLønnsendringDto lønnsendringDto = new VurderLønnsendringDto(false);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        dto.setVurdertLonnsendring(lønnsendringDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderLønnsendringOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);
        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        // Assert
        assertThat(faktaAggregat).isPresent();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold()).hasSize(1);
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(arbeidstakerAndel).get().getHarLønnsendringIBeregningsperiodenVurdering()).isFalse();
        assertThat(frilansAndel.getBgAndelArbeidsforhold()).isNotPresent();
    }

    @Test
    public void flere_yrkesaktiviteter_for_andel() {
        // Arrange

        var builder1 = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty()).medArbeidsgiver(Arbeidsgiver.virksomhet("999999999"))
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef());
        builder1.leggTilAktivitetsAvtale(builder1.getAktivitetsAvtaleBuilder().medPeriode(Intervall.fraOgMed(LocalDate.of(2023, 10, 1))).medSisteLønnsendringsdato(LocalDate.of(2023, 10, 20)));
        builder1.leggTilAktivitetsAvtale(builder1.getAktivitetsAvtaleBuilder().medPeriode(Intervall.fraOgMedTilOgMed(LocalDate.of(2023, 10, 20), LocalDate.of(2024,7,1))));

        var builder2 = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty()).medArbeidsgiver(Arbeidsgiver.virksomhet("999999999"))
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef());
        builder2.leggTilAktivitetsAvtale(builder2.getAktivitetsAvtaleBuilder().medPeriode(Intervall.fraOgMed(LocalDate.of(2023, 10, 1))).medSisteLønnsendringsdato(LocalDate.of(2023, 10, 20)));
        builder2.leggTilAktivitetsAvtale(builder2.getAktivitetsAvtaleBuilder().medPeriode(Intervall.fraOgMedTilOgMed(LocalDate.of(2023, 10, 20), LocalDate.of(2024,3,1))));

        var builder3 = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty()).medArbeidsgiver(Arbeidsgiver.virksomhet("999999999"))
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef());
        builder3.leggTilAktivitetsAvtale(builder3.getAktivitetsAvtaleBuilder().medPeriode(Intervall.fraOgMed(LocalDate.of(2023, 12, 1))).medSisteLønnsendringsdato(LocalDate.of(2024, 1, 15)));
        builder3.leggTilAktivitetsAvtale(builder3.getAktivitetsAvtaleBuilder().medPeriode(Intervall.fraOgMedTilOgMed(LocalDate.of(2024, 1, 15), LocalDate.of(2024,2,9))));

        var aa = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        aa.leggTilYrkesaktivitet(builder1).leggTilYrkesaktivitet(builder2).leggTilYrkesaktivitet(builder3);

        Arbeidsgiver virksomheten = Arbeidsgiver.virksomhet("999999999");
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()

                .medGrunnbeløp(GRUNNBELØP)
                .medSkjæringstidspunkt(LocalDate.of(2024,2,29))
                .leggTilFaktaOmBeregningTilfeller(FAKTA_OM_BEREGNING_TILFELLER)
                .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(LocalDate.of(2024,2,29), LocalDate.of(9999,12,31))
                .build(beregningsgrunnlag);
        arbeidstakerAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBeregningsperiode(LocalDate.of(2023,11,1), LocalDate.of(2024,1,31))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomheten))
                .medAndelsnr(ANDELSNR_ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode1);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                        .leggTilAktørArbeid(aa))
                .build();
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                        .medBeregningsgrunnlag(beregningsgrunnlag),
                BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);

        VurderLønnsendringDto lønnsendringDto = new VurderLønnsendringDto(false);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        dto.setVurdertLonnsendring(lønnsendringDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderLønnsendringOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);
        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        // Assert
        assertThat(faktaAggregat).isPresent();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold()).hasSize(1);
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(arbeidstakerAndel).get().getHarLønnsendringIBeregningsperiodenVurdering()).isFalse();
        assertThat(frilansAndel.getBgAndelArbeidsforhold()).isNotPresent();
    }

}
