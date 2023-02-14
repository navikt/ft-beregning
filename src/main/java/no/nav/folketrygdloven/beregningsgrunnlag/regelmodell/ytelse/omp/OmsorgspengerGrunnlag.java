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


    public boolean skalAvviksvurdere() {
		return harBrukerSøkt || finnesArbeidsandelIkkeSøktOm;
    }


}
