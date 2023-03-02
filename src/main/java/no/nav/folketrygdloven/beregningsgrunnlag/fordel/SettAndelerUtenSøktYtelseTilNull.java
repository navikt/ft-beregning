package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SettAndelerUtenSøktYtelseTilNull extends LeafSpecification<FordelModell> {


	public static final String ID = "FP_BR 22.3.9";
	public static final String REGEL_BESKRIVELSE = "Sett fordeling for nye andeler uten søkt ytelse til 0.";

	public SettAndelerUtenSøktYtelseTilNull() {
		super(ID, REGEL_BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(FordelModell grunnlag) {
		grunnlag.getInput().getAndeler().stream()
				.filter(a -> !a.erSøktYtelseFor() && a.getBruttoPrÅr().isEmpty())
				.forEach(a -> FordelAndelModell.oppdater(a)
						.medInntektskategori(mapTilInntektskategori(a.getAktivitetStatus()))
						.medFordeltPrÅr(BigDecimal.ZERO));
		return ja();
	}

	private Inntektskategori mapTilInntektskategori(AktivitetStatus aktivitetStatus) {
		return switch (aktivitetStatus) {
			case AT -> Inntektskategori.ARBEIDSTAKER;
			case FL -> Inntektskategori.FRILANSER;
			case SN -> Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE;
			case AAP -> Inntektskategori.ARBEIDSAVKLARINGSPENGER;
			case DP -> Inntektskategori.DAGPENGER;
			default -> Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
		};
	}

	@Override
	public String beskrivelse() {
		return REGEL_BESKRIVELSE;
	}
}
