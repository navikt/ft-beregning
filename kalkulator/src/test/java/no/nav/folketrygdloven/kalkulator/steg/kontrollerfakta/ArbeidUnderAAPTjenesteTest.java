package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

class ArbeidUnderAAPTjenesteTest {
	private ArbeidUnderAAPTjeneste arbeidUnderAAPTjeneste;
	private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto;

	@Test
	void harAndelForArbeidUnderAAP() {
		var dto = lagBeregningsgrunnlagDto(List.of(OpptjeningAktivitetType.ARBEID_UNDER_AAP));

		var harArbeidUnderAapTilfelle = kallHarAndelForArbeidUnderAAP(dto);

		assertThat(harArbeidUnderAapTilfelle).isTrue();
	}

	@Test
	void harIkkeAndelForArbeidUnderAAP() {
		var dto = lagBeregningsgrunnlagDto(List.of(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE));

		var harArbeidUnderAapTilfelle = kallHarAndelForArbeidUnderAAP(dto);

		assertThat(harArbeidUnderAapTilfelle).isFalse();
	}

	@Test
	void harOgsåAndelForArbeidUnderAAP() {
		var beregningsgrunnlagDto = lagBeregningsgrunnlagDto(List.of(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE, OpptjeningAktivitetType.ARBEID_UNDER_AAP));

		var harArbeidUnderAapTilfelle = kallHarAndelForArbeidUnderAAP(beregningsgrunnlagDto);

		assertThat(harArbeidUnderAapTilfelle).isTrue();
	}

	private BeregningsgrunnlagDto lagBeregningsgrunnlagDto(List<OpptjeningAktivitetType> opptjeningAktivitetTyper) {
		BeregningsgrunnlagAktivitetStatusDto.Builder aktivitetStatusArbeidstaker = BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER);
		BeregningsgrunnlagAktivitetStatusDto.Builder aktivitetstatusAAP = BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
		BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
				.medSkjæringstidspunkt(LocalDate.now())
				.leggTilAktivitetStatus(aktivitetStatusArbeidstaker)
				.leggTilAktivitetStatus(aktivitetstatusAAP)
				.build();
		BeregningsgrunnlagPeriodeDto.Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(LocalDate.now(), null);
		BeregningsgrunnlagPeriodeDto periode = periodeBuilder.build(beregningsgrunnlag);
		opptjeningAktivitetTyper.forEach(opptjeningAktivitetType -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
				.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
				.medInntektskategori(Inntektskategori.ARBEIDSTAKER)
				.medBeregnetPrÅr(null)
				.medArbforholdType(opptjeningAktivitetType)
				.build(periode));
		return beregningsgrunnlag;
	}

	private boolean kallHarAndelForArbeidUnderAAP(BeregningsgrunnlagDto beregningsgrunnlag) {
		BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
				.medBeregningsgrunnlag(beregningsgrunnlag)
				.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
		return ArbeidUnderAAPTjeneste.harAndelForArbeidUnderAAP(grunnlag);
	}
}