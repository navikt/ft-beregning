package no.nav.folketrygdloven.regelmodelloversetter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

class RegelJsonMapperTest {

	@Test
	void skal_mappe_regelandel() {
		var afbfor = BeregningsgrunnlagPrArbeidsforhold.builder()
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
				.medBruttoPrÅr(BigDecimal.valueOf(500000))
				.medAndelNr(1L)
				.build();
		var andel = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.AT)
				.medArbeidsforhold(afbfor)
				.build();

		var json = RegelJsonMapper.asJson(andel);
		assertThat(json).isNotNull();
	}

}
