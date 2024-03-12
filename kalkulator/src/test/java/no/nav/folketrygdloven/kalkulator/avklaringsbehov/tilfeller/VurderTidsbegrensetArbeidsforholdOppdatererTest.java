package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderteArbeidsforholdDto;
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
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class VurderTidsbegrensetArbeidsforholdOppdatererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final Beløp GRUNNBELØP = Beløp.fra(90000);

    private List<VurderteArbeidsforholdDto> tidsbestemteArbeidsforhold;
    private final long FØRSTE_ANDELSNR = 1L;
    private final long ANDRE_ANDELSNR = 2L;
    private final long TREDJE_ANDELSNR = 3L;
    private final LocalDate FOM = LocalDate.now().minusDays(100);
    private final LocalDate TOM = LocalDate.now();
    private final List<Arbeidsgiver> virksomheter = new ArrayList<>();
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setup() {
        virksomheter.add(Arbeidsgiver.virksomhet("123"));
        virksomheter.add(Arbeidsgiver.virksomhet("456"));
        virksomheter.add(Arbeidsgiver.virksomhet("789"));
        tidsbestemteArbeidsforhold = lagFastsatteAndelerListe();


    }

    private List<VurderteArbeidsforholdDto> lagFastsatteAndelerListe() {

        VurderteArbeidsforholdDto førsteForhold = new VurderteArbeidsforholdDto(
            FØRSTE_ANDELSNR,
            true
        );

        VurderteArbeidsforholdDto andreForhold = new VurderteArbeidsforholdDto(
            ANDRE_ANDELSNR,
            false
        );

        VurderteArbeidsforholdDto tredjeForhold = new VurderteArbeidsforholdDto(
            TREDJE_ANDELSNR,
            true
        );

        return new ArrayList<>(List.of(førsteForhold, andreForhold, tredjeForhold));
    }


    @Test
    public void skal_markere_korrekte_andeler_som_tidsbegrenset() {
        //Arrange
        lagBehandlingMedBeregningsgrunnlag();

        //Dto
        var faktaBeregningLagreDto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD));
        faktaBeregningLagreDto.setVurderTidsbegrensetArbeidsforhold(new VurderTidsbegrensetArbeidsforholdDto( tidsbestemteArbeidsforhold));


        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FaktaOmBeregningTilfellerOppdaterer.oppdater(faktaBeregningLagreDto, Optional.empty(), input, oppdatere);
        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        //Assert
        assertThat(faktaAggregat).isPresent();
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(andeler.get(0)).get().getErTidsbegrensetVurdering()).isTrue();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(andeler.get(1)).get().getErTidsbegrensetVurdering()).isFalse();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(andeler.get(2)).get().getErTidsbegrensetVurdering()).isTrue();
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Arbeidsgiver virksomhet) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
            .builder()
            .medArbeidsgiver(virksomhet)
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

    private void lagBehandlingMedBeregningsgrunnlag() {

        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();

        BeregningsgrunnlagPeriodeDto periode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
            FOM, TOM);
        buildBgPrStatusOgAndel(periode, virksomheter.get(0));
        buildBgPrStatusOgAndel(periode, virksomheter.get(1));
        buildBgPrStatusOgAndel(periode, virksomheter.get(2));

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }
}
