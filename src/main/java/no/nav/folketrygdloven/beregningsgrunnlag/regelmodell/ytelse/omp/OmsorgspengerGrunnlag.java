package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class OmsorgspengerGrunnlag extends YtelsesSpesifiktGrunnlag {

	private final boolean finnesArbeidsandelIkkeSøktOm;

	private final boolean harBrukerSøkt;

    public OmsorgspengerGrunnlag(boolean finnesArbeidsandelIkkeSøktOm,
                                 boolean harBrukerSøkt) {
        super("OMP");
	    this.finnesArbeidsandelIkkeSøktOm = finnesArbeidsandelIkkeSøktOm;
	    this.harBrukerSøkt = harBrukerSøkt;
    }


	/**
	 * Brukere som søker omsorgspenger omfattes enten av § 9-8 (omsorgspenger til arbeidsgiver) eller § 9-9 (direkte utbetaling).
	 * Under § 9-9 omfattes også de brukere som har flere arbeidsforhold der det kun er enkelte arbeidsgivere/arbeidsforhold som søker refusjon.
	 *
	 * @return Omfattes bruker av § 9-9
	 */
    public boolean omfattesAvKap9Paragraf9() {
		return harBrukerSøkt || finnesArbeidsandelIkkeSøktOm;
    }


}
