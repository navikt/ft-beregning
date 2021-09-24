package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;

public class GraderingPrAktivitet {

	private List<Periode> perioder;
	private Arbeidsforhold arbeidsforhold;
	private Periode ansettelsesperiode;
	private AktivitetStatusV2 aktivitetStatus;
	private Long andelsnr;

	public GraderingPrAktivitet(List<Periode> perioder,
	                            Arbeidsforhold arbeidsforhold,
	                            Periode ansettelsesperiode, AktivitetStatusV2 aktivitetStatus,
	                            Long andelsnr) {
		this.perioder = perioder;
		this.arbeidsforhold = arbeidsforhold;
		this.ansettelsesperiode = ansettelsesperiode;
		this.aktivitetStatus = aktivitetStatus;
		this.andelsnr = andelsnr;
	}

	public List<Periode> getPerioder() {
		return perioder;
	}

	public Arbeidsforhold getArbeidsforhold() {
		return arbeidsforhold;
	}

	public AktivitetStatusV2 getAktivitetStatus() {
		return aktivitetStatus;
	}

	public long getAndelsnr() {
		return andelsnr;
	}

	public boolean erNyAktivitet() {
		return andelsnr == null;
	}

}
