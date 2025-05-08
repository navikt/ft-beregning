package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

import static org.assertj.core.api.Assertions.assertThat;

class ArbeidUnderAAPTjenesteTest {
	private ArbeidUnderAAPTjeneste arbeidUnderAAPTjeneste;
	private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto;

	@BeforeEach
	void setUp() {
	}

	@Test
	void harAndelForArbeidUnderAAP() {
		var dto = lagBeregningsgrunnlag(List.of(OpptjeningAktivitetType.ARBEID_UNDER_AAP));

		var utledetArbeidUnderAapTilfelle = act(dto);

		assertThat(utledetArbeidUnderAapTilfelle).isTrue();
	}

	@Test
	void harIkkeAndelForArbeidUnderAAP() {
		var dto = lagBeregningsgrunnlag(List.of(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE));

		var utledetArbeidUnderAapTilfelle = act(dto);

		assertThat(utledetArbeidUnderAapTilfelle).isFalse();
	}

	@Test
	void harOgsåAndelForArbeidUnderAAP() {
		var dto = lagBeregningsgrunnlag(List.of(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE, OpptjeningAktivitetType.ARBEID_UNDER_AAP));

		var utledetArbeidUnderAapTilfelle = act(dto);

		assertThat(utledetArbeidUnderAapTilfelle).isTrue();
	}

	private BeregningsgrunnlagDto lagBeregningsgrunnlag(List<OpptjeningAktivitetType> opptjeningAktivitetTypes) {
		BeregningsgrunnlagAktivitetStatusDto.Builder asb = BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER);
		BeregningsgrunnlagAktivitetStatusDto.Builder aap = BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
		BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
				.medSkjæringstidspunkt(LocalDate.now())
				.leggTilAktivitetStatus(asb)
				.leggTilAktivitetStatus(aap)
				.build();
		BeregningsgrunnlagPeriodeDto.Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(LocalDate.now(), null);
		BeregningsgrunnlagPeriodeDto periode = periodeBuilder.build(beregningsgrunnlag);
		for (OpptjeningAktivitetType type : opptjeningAktivitetTypes) {
			BeregningsgrunnlagPrStatusOgAndelDto.ny()
				.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
				.medInntektskategori(Inntektskategori.ARBEIDSTAKER)
				.medBeregnetPrÅr(null)
				.medArbforholdType(type)
				.build(periode);
		}
		return beregningsgrunnlag;
	}

	private boolean act(BeregningsgrunnlagDto beregningsgrunnlag) {
		BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
				.medBeregningsgrunnlag(beregningsgrunnlag)
				.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
		return ArbeidUnderAAPTjeneste.harAndelForArbeidUnderAAP(grunnlag);
	}
}