package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.ArbeidstakerandelUtenIMMottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.MottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class MottarYtelseOppdatererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final String ORGNR = "3482934982384";
    private static final InternArbeidsforholdRefDto ARB_ID = InternArbeidsforholdRefDto.namedRef("TEST-REF");

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(Beløp.fra(91425))
            .leggTilFaktaOmBeregningTilfeller(singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE))
            .build();
        periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    @Test
    public void skal_sette_mottar_ytelse_kun_for_frilans() {
        // Arrange
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE));
        dto.setMottarYtelse(new MottarYtelseDto(true, emptyList()));
        byggFrilansAndel();
        BeregningsgrunnlagPrStatusOgAndelDto arbeidsforholdAndel = byggArbeidsforholdMedBgAndel();

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        MottarYtelseOppdaterer.oppdater(dto, oppdatere);
        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        // Assert
        assertThat(faktaAggregat.get().getFaktaAktør().get().getHarFLMottattYtelseVurdering()).isTrue();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(arbeidsforholdAndel)).isNotPresent();
    }

    @Test
    public void skal_sette_mottar_ytelse_kun_for_frilans_og_arbeidstakerandel() {
        // Arrange
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE));
        byggFrilansAndel();
        BeregningsgrunnlagPrStatusOgAndelDto arbeidsforholdAndel = byggArbeidsforholdMedBgAndel();
        dto.setMottarYtelse(new MottarYtelseDto(false,
            singletonList(new ArbeidstakerandelUtenIMMottarYtelseDto(arbeidsforholdAndel.getAndelsnr(), true))));

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        MottarYtelseOppdaterer.oppdater(dto, oppdatere);

        BeregningsgrunnlagPrStatusOgAndelDto oppdatertArbeidsforholdAndel = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag()
            .getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(a -> a.equals(arbeidsforholdAndel)).findFirst().get();

        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        // Assert
        assertThat(faktaAggregat.get().getFaktaAktør().get().getHarFLMottattYtelseVurdering()).isFalse();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(oppdatertArbeidsforholdAndel).get().getHarMottattYtelseVurdering()).isTrue();
    }

    private void byggFrilansAndel() {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .build(periode);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto byggArbeidsforholdMedBgAndel() {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
            .build(periode);
    }


}
