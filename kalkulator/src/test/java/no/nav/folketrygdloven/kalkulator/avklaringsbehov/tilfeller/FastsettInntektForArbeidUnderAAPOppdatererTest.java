package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettInntektForArbeidUnderAAPDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class FastsettInntektForArbeidUnderAAPOppdatererTest {
	private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2025,1,1);

	private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
	private BeregningsgrunnlagDto beregningsgrunnlag;
	private BeregningsgrunnlagInput input;

	@BeforeEach
	void setup() {
		beregningsgrunnlag = lagBeregningsgrunnlag();
	}

	@Test
	void skalTesteAtOppdatererSetterKorrektInntektPåKorrektAndel() {
		var fastsettDto = new FastsettInntektForArbeidUnderAAPDto(10000);
		var dto = new FaktaBeregningLagreDto(singletonList(FaktaOmBeregningTilfelle.FASTSETT_INNTEKT_FOR_ARBEID_UNDER_AAP));
		dto.setFastsettInntektForArbeidUnderAAP(fastsettDto);

		var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
		FastsettInntektForArbeidUnderAAPOppdaterer.oppdater(dto, oppdatere);

		var bgPerioder = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
		assertThat(bgPerioder).hasSize(1);
		assertThat(bgPerioder.getFirst().getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        var arbeidUnderAapAndel = getAndel(bgPerioder, OpptjeningAktivitetType.ARBEID_UNDER_AAP);
		assertThat(arbeidUnderAapAndel.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(120000));
		assertThat(arbeidUnderAapAndel.getFastsattAvSaksbehandler()).isTrue();
		assertThat(arbeidUnderAapAndel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID_UNDER_AAP);
        var aapAndel = getAndel(bgPerioder, OpptjeningAktivitetType.AAP);
		assertThat(aapAndel.getBeregnetPrÅr()).isNull();
		assertThat(aapAndel.getFastsattAvSaksbehandler()).isFalse();
		assertThat(aapAndel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.AAP);
	}

	private static BeregningsgrunnlagPrStatusOgAndelDto getAndel(List<BeregningsgrunnlagPeriodeDto> bgPerioder, OpptjeningAktivitetType aktivitetType) {
		return bgPerioder.getFirst().getBeregningsgrunnlagPrStatusOgAndelList()
				.stream()
				.filter(a -> a.getArbeidsforholdType().equals(aktivitetType))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Finner ikke forventet andel etter å ha kjørt oppdaterer"));
	}

	private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
		beregningsgrunnlag = BeregningsgrunnlagDto.builder()
				.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
				.build();

        var periode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
				SKJÆRINGSTIDSPUNKT, null);
		buildBgPrStatusOgAndel(periode);
		input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT);
		return beregningsgrunnlag;
	}

	private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
		BeregningsgrunnlagPrStatusOgAndelDto.ny()
				.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
				.medArbforholdType(OpptjeningAktivitetType.ARBEID_UNDER_AAP)
				.build(beregningsgrunnlagPeriode);
		BeregningsgrunnlagPrStatusOgAndelDto.ny()
				.medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)
				.medArbforholdType(OpptjeningAktivitetType.AAP)
				.build(beregningsgrunnlagPeriode);
	}

	private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fom, LocalDate tom) {
		return BeregningsgrunnlagPeriodeDto.ny()
				.medBeregningsgrunnlagPeriode(fom, tom)
				.build(beregningsgrunnlag);
	}
}
