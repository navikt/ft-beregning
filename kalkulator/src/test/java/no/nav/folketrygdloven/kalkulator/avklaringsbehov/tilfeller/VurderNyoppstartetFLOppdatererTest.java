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
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderNyoppstartetFLDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class VurderNyoppstartetFLOppdatererTest {


    private static final List<FaktaOmBeregningTilfelle> FAKTA_OM_BEREGNING_TILFELLER = Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL);
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = Beløp.fra(600000);

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;


    @BeforeEach
    public void setup() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilFaktaOmBeregningTilfeller(FAKTA_OM_BEREGNING_TILFELLER)
            .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(beregningsgrunnlag);
        @SuppressWarnings("unused")
        var frilansAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(1L)
            .medInntektskategori(Inntektskategori.FRILANSER)
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .build(periode1);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

    }

    @Test
    public void skal_sette_nyoppstartet_til_true() {
        // Arrange
        VurderNyoppstartetFLDto nyoppstartetDto = new VurderNyoppstartetFLDto(true);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        dto.setVurderNyoppstartetFL(nyoppstartetDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderNyoppstartetFLOppdaterer.oppdater(dto, oppdatere);
        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        // Assert
        assertThat(faktaAggregat.get().getFaktaAktør().get().getErNyoppstartetFLVurdering()).isTrue();
    }

    @Test
    public void skal_sette_nyoppstartet_til_false() {
        // Arrange
        VurderNyoppstartetFLDto nyoppstartetDto = new VurderNyoppstartetFLDto(false );
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        dto.setVurderNyoppstartetFL(nyoppstartetDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderNyoppstartetFLOppdaterer.oppdater(dto, oppdatere);
        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        // Assert
        assertThat(faktaAggregat.get().getFaktaAktør().get().getErNyoppstartetFLVurdering()).isFalse();
    }

}
