package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.FeatureToggles;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Toggle;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Konstanter;

public class Beregningsgrunnlag {
	private YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag;
	private final List<AktivitetStatusMedHjemmel> aktivitetStatuser = new ArrayList<>();
	@JsonManagedReference
	private final List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = new ArrayList<>();
	private BigDecimal grunnbeløp;
	private final Konstanter konstanter = new Konstanter();

	private final FeatureToggles toggles = new FeatureToggles();


	/**
	 * Type for midlertidig inaktiv dersom bruker er midlertidig inaktiv
	 */
	private MidlertidigInaktivType midlertidigInaktivType;


	private Beregningsgrunnlag() {
	}

	public List<AktivitetStatusMedHjemmel> getAktivitetStatuser() {
		return Collections.unmodifiableList(aktivitetStatuser);
	}

	public YtelsesSpesifiktGrunnlag getYtelsesSpesifiktGrunnlag() {
		return ytelsesSpesifiktGrunnlag;
	}


	public List<BeregningsgrunnlagPeriode> getBeregningsgrunnlagPerioder() {
		return beregningsgrunnlagPerioder.stream()
				.sorted(Comparator.comparing(bg -> bg.getBeregningsgrunnlagPeriode().getFom())).toList();
	}


	public BigDecimal getGrunnbeløp() {
		return grunnbeløp;
	}


	public AktivitetStatusMedHjemmel getAktivitetStatus(AktivitetStatus aktivitetStatus) {
		return aktivitetStatuser.stream().filter(as -> as.inneholder(aktivitetStatus)).findAny()
				.orElseThrow(() -> new IllegalStateException("Beregningsgrunnlaget mangler regel for status " + aktivitetStatus.getBeskrivelse()));
	}

	public BigDecimal getYtelsedagerPrÅr() {
		return konstanter.getYtelsedagerIPrÅr();
	}

	public BigDecimal getMidlertidigInaktivTypeAReduksjonsfaktor() {
		return konstanter.getMidlertidigInaktivTypeAReduksjonsfaktor();
	}

	public MidlertidigInaktivType getMidlertidigInaktivType() {
		return midlertidigInaktivType;
	}

	public FeatureToggles getToggles() {
		return toggles;
	}

	public static Builder builder() {
		return new Builder();
	}

	// FIXME: Dette er en skjult mutator siden den endrer på oppgitt beregningsgrunnlag. Endre metode navn eller pattern?
	public static Builder builder(Beregningsgrunnlag beregningsgrunnlag) {
		return new Builder(beregningsgrunnlag);
	}

	public static class Builder {
		private Beregningsgrunnlag beregningsgrunnlagMal;

		private Builder() {
			beregningsgrunnlagMal = new Beregningsgrunnlag();
		}

		private Builder(Beregningsgrunnlag beregningsgrunnlag) {
			beregningsgrunnlagMal = beregningsgrunnlag;
		}

		public Builder medAktivitetStatuser(List<AktivitetStatusMedHjemmel> aktivitetStatusList) {
			beregningsgrunnlagMal.aktivitetStatuser.addAll(aktivitetStatusList);
			return this;
		}


		public Builder medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
			beregningsgrunnlagMal.beregningsgrunnlagPerioder.add(beregningsgrunnlagPeriode);
			beregningsgrunnlagPeriode.setBeregningsgrunnlag(beregningsgrunnlagMal);
			return this;
		}

		public Builder medBeregningsgrunnlagPerioder(List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder) {
			beregningsgrunnlagMal.beregningsgrunnlagPerioder.addAll(beregningsgrunnlagPerioder);
			beregningsgrunnlagPerioder.forEach(bgPeriode -> bgPeriode.setBeregningsgrunnlag(beregningsgrunnlagMal));
			return this;
		}

		public Builder medGrunnbeløp(BigDecimal grunnbeløp) {
			beregningsgrunnlagMal.grunnbeløp = grunnbeløp;
			return this;
		}

		public Builder medYtelsesSpesifiktGrunnlag(YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
			beregningsgrunnlagMal.ytelsesSpesifiktGrunnlag = ytelsesSpesifiktGrunnlag;
			return this;
		}


		public Builder medAntallGØvreGrenseverdi(BigDecimal grenseverdi) {
			beregningsgrunnlagMal.konstanter.setAntallGØvreGrenseverdi(grenseverdi);
			return this;
		}

		public Builder medYtelsesdagerIEtÅr(BigDecimal ytelsesdagerIEtÅr) {
			beregningsgrunnlagMal.konstanter.setYtelsedagerIPrÅr(ytelsesdagerIEtÅr);
			return this;
		}

		public Builder medMidlertidigInaktivType(MidlertidigInaktivType midlertidigInaktivType) {
			beregningsgrunnlagMal.midlertidigInaktivType = midlertidigInaktivType;
			return this;
		}

		public Builder leggTilToggle(String feature, boolean value) {
			beregningsgrunnlagMal.toggles.leggTilToggle(new Toggle(feature, value));
			return this;
		}

		public Beregningsgrunnlag build() {
			verifyStateForBuild();
			return beregningsgrunnlagMal;
		}

		private void verifyStateForBuild() {
			Objects.requireNonNull(beregningsgrunnlagMal.aktivitetStatuser, "aktivitetStatuser");
			if (beregningsgrunnlagMal.beregningsgrunnlagPerioder.isEmpty()) {
				throw new IllegalStateException("Beregningsgrunnlaget må inneholde minst 1 periode");
			}
			if (beregningsgrunnlagMal.aktivitetStatuser.isEmpty()) {
				throw new IllegalStateException("Beregningsgrunnlaget må inneholde minst 1 status");
			}
		}
	}
}
