package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public class VurderBesteberegningTilfelleDtoTjenesteTest {

    private static final LocalDate STP = LocalDate.of(2019, 1, 1);
    private static final Intervall OPPTJENINGSPERIODE = Intervall.fraOgMedTilOgMed(STP.minusYears(1), STP.plusYears(10));
    private static final BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder();
    private VurderBesteberegningTilfelleDtoTjeneste dtoTjeneste;

    @BeforeEach
    public void setUp() {
        var orgnr = "347289324";
        bgAndelArbeidsforholdBuilder
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr));
        dtoTjeneste = new VurderBesteberegningTilfelleDtoTjeneste();
    }

    @Test
    public void skal_ikke_sette_verdier_på_dto_om_man_ikkje_har_tilfelle() {
        // Arrange
        var beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(STP, OPPTJENINGSPERIODE,
            OpptjeningAktivitetType.ARBEID);
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(STP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)).build();
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRegisterAktiviteter(beregningAktiviteter)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        var faktaOmBeregningDto = new FaktaOmBeregningDto();

         var input = new BeregningsgrunnlagGUIInput(lagReferanse(), null, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        dtoTjeneste.lagDto(input, faktaOmBeregningDto);

        // Assert
        assertThat(faktaOmBeregningDto.getVurderBesteberegning()).isNull();

    }

    @Test
    public void skal_sette_verdier_på_dto() {
        // Arrange
        var beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(STP, OPPTJENINGSPERIODE,
            OpptjeningAktivitetType.ARBEID);
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(STP)
            .leggTilFaktaOmBeregningTilfeller(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING))
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)).build();
        var periode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(STP, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(bgAndelArbeidsforholdBuilder)
            .medInntektskategori(Inntektskategori.JORDBRUKER)
            .build(periode);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRegisterAktiviteter(beregningAktiviteter)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        var faktaOmBeregningDto = new FaktaOmBeregningDto();

         var input = new BeregningsgrunnlagGUIInput(lagReferanse(), null, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        dtoTjeneste.lagDto(input, faktaOmBeregningDto);

        // Assert
        assertThat(faktaOmBeregningDto.getVurderBesteberegning().getSkalHaBesteberegning()).isNull();
    }

    private KoblingReferanse lagReferanse() {
        return new KoblingReferanseMock(STP);
    }
}
