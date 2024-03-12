package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettEtterlønnSluttpakkeDto;
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
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class FastsettEtterlønnSluttpakkeOppdatererTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    private static final Beløp GRUNNBELØP = Beløp.fra(85000);


    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet("490830958");
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setup() {
        beregningsgrunnlag = lagBeregningsgrunnlag();
    }

    @Test
    public void skalTesteAtOppdatererSetterKorrektInntektPåSøkerensEtterlønnSluttpakkeAndel() {
        // Arrange
        FastsettEtterlønnSluttpakkeDto fastsettDto = new FastsettEtterlønnSluttpakkeDto(10000);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(singletonList(FaktaOmBeregningTilfelle.FASTSETT_ETTERLØNN_SLUTTPAKKE));
        dto.setFastsettEtterlønnSluttpakke(fastsettDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettEtterlønnSluttpakkeOppdaterer.oppdater(dto, oppdatere);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> bgPerioder = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        Assertions.assertThat(bgPerioder).hasSize(1);
        assertThat(bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(a -> a.getArbeidsforholdType().equals(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Finner ikke forventet andel etter å ha kjørt oppdaterer"));
        Assertions.assertThat(andel.getBeregnetPrÅr().compareTo(Beløp.fra(120000)) == 0).isTrue();
        assertThat(andel.getFastsattAvSaksbehandler()).isTrue();
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();

        BeregningsgrunnlagPeriodeDto periode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
            SKJÆRINGSTIDSPUNKT, null);
        buildBgPrStatusOgAndel(periode);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT);
        return beregningsgrunnlag;
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2))
            .build(beregningsgrunnlagPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2))
            .build(beregningsgrunnlagPeriode);

    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

}
