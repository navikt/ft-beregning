package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class FastsettMånedsinntektUtenInntektsmeldingOppdatererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    public static final String ORGNR = "974652269";
    private static final String ORGNR2 = "999999999";
    private static final int ARBEIDSINNTEKT = 120000;

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private Arbeidsgiver arbeidsgiver;
    private Arbeidsgiver arbeidsgiver2;

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setUp() {
        arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);

        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(Beløp.fra(91425))
            .leggTilFaktaOmBeregningTilfeller(singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE))
            .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2))
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPeriodeDto periode2 = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2).plusDays(1), null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(1L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver)).build(periode1);
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(5L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver)).build(periode2);
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(2L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver2)).build(periode1);
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(1L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver2)).build(periode2);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    @Test
    public void skal_sette_inntekt_på_riktige_andeler_i_alle_perioder(){
        // Arrange
        FastsettMånedsinntektUtenInntektsmeldingDto dto = new FastsettMånedsinntektUtenInntektsmeldingDto();
        FastsettMånedsinntektUtenInntektsmeldingAndelDto andelDto = new FastsettMånedsinntektUtenInntektsmeldingAndelDto(1L,
            FastsatteVerdierDto.Builder.ny().medFastsattBeløpPrMnd(ARBEIDSINNTEKT/12).build());
        List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe = singletonList(andelDto);
        dto.setAndelListe(andelListe);
        FaktaBeregningLagreDto faktaLagreDto = new FaktaBeregningLagreDto(singletonList(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING));
        faktaLagreDto.setFastsattUtenInntektsmelding(dto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettMånedsinntektUtenInntektsmeldingOppdaterer.oppdater(faktaLagreDto, Optional.empty(), oppdatere);

        // Assert
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedFastsattInntekt = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getBgAndelArbeidsforhold().isPresent() && andel.getBgAndelArbeidsforhold().get().getArbeidsgiver().equals(arbeidsgiver))
            .collect(Collectors.toList());
        andelerMedFastsattInntekt.forEach(andel -> assertThat(andel.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(ARBEIDSINNTEKT)));
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenFastsattInntekt = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getBgAndelArbeidsforhold().isPresent() && andel.getBgAndelArbeidsforhold().get().getArbeidsgiver().equals(arbeidsgiver2))
            .collect(Collectors.toList());
        andelerUtenFastsattInntekt.forEach(andel -> assertThat(andel.getBeregnetPrÅr()).isNull());
        }
}
