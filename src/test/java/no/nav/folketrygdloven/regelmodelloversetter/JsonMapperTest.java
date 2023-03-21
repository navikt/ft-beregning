package no.nav.folketrygdloven.regelmodelloversetter;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class JsonMapperTest {

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

		var json = JsonMapper.asJson(andel);
		assertThat(json).isNotNull();
	}

}