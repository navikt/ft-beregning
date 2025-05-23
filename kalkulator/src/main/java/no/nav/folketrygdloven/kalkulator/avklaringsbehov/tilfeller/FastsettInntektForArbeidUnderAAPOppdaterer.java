package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class FastsettInntektForArbeidUnderAAPOppdaterer {

	private FastsettInntektForArbeidUnderAAPOppdaterer() {
	}

	public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
		var fastsettDto = dto.getFastsettInntektForArbeidUnderAAP();
		var fastsattPrMnd = fastsettDto.getFastsattPrMnd();
		if (fastsattPrMnd == null) {
			throw new IllegalStateException("Finner ikke beløp for fastetting av inntekt for arbeid under AAP");
		}

		var arbeidUnderAAPAndel = finnArbeidUnderAAPAndel(grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag());
		var nyVerdiBeregnetPrÅr = Beløp.fra(fastsattPrMnd).multipliser(KonfigTjeneste.getMånederIÅr());

		BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(arbeidUnderAAPAndel)
				.medBeregnetPrÅr(nyVerdiBeregnetPrÅr)
				.medFastsattAvSaksbehandler(true);
	}

	private static BeregningsgrunnlagPrStatusOgAndelDto finnArbeidUnderAAPAndel(BeregningsgrunnlagDto nyttBeregningsgrunnlag) {
		var arbeidUnderAAPAndeler = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()
				.getFirst()
				.getBeregningsgrunnlagPrStatusOgAndelList()
				.stream()
				.filter(bpsa -> bpsa.getArbeidsforholdType().equals(OpptjeningAktivitetType.ARBEID_UNDER_AAP))
				.toList();

		if (arbeidUnderAAPAndeler.size() != 1) {
			throw new IllegalStateException("Det skal være én andel med arbeid under AAP, antall funnet: " + arbeidUnderAAPAndeler.size());
		}
		return arbeidUnderAAPAndeler.getFirst();
	}
}
