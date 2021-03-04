package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.fpsak.tidsserie.LocalDateSegment;

public class AndelGraderingImpl implements AndelEndring {
    private AktivitetStatusV2 aktivitetStatus;
    private List<Gradering> graderinger = new ArrayList<>();
    private Arbeidsforhold arbeidsforhold;
    private Long andelsnr;

    private AndelGraderingImpl() {
    }

	@Override
	public boolean filterForEksisterendeAktiviteter() {
		return false;
	}

	@Override
    public AktivitetStatusV2 getAktivitetStatus() {
        return aktivitetStatus;
    }

    @Override
    public List<Gradering> getGraderinger() {
        return graderinger;
    }

    @Override
    public boolean erNyAktivitet(PeriodeModell input, LocalDate periodeFom) {
	    var manglerAndelIPeriode = input.getPeriodisertBruttoBeregningsgrunnlagList().stream()
			    .filter(p -> p.getPeriode().inneholder(periodeFom))
			    .findFirst()
			    .map(p -> p.getBruttoBeregningsgrunnlag().stream().noneMatch(andel -> matcherGraderingOgAndel(this, andel)))
			    .orElse(true);

        return andelsnr == null || manglerAndelIPeriode;
    }

	private static boolean matcherGraderingOgAndel(AndelEndring andelGradering, BruttoBeregningsgrunnlag andel) {
		return andel.getAktivitetStatus().equals(andelGradering.getAktivitetStatus()) &&
				(andelGradering.getArbeidsforhold() == null ||
						andel.getArbeidsforhold().map(arbeidsforhold -> arbeidsforhold.getArbeidsgiverId().equals(andelGradering.getArbeidsforhold().getArbeidsgiverId())
								&& (arbeidsforhold.getArbeidsforholdId() == null
								|| andelGradering.getArbeidsforhold().getArbeidsforholdId() == null
								|| arbeidsforhold.getArbeidsforholdId().equals(andelGradering.getArbeidsforhold().getArbeidsforholdId()))).orElse(true));
	}

    @Override
    public List<Refusjonskrav> getGyldigeRefusjonskrav() {
        return Collections.emptyList();
    }

    @Override
    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

	@Override
	public boolean filterForNyeAktiviteter(LocalDate skjæringstidspunkt, LocalDate periodeFom) {
		return harGraderingFørPeriode(periodeFom);
	}

	private boolean harGraderingFørPeriode(LocalDate periodeFom) {
		return getGraderinger().stream()
				.anyMatch(gradering -> !gradering.getPeriode().getFom().isAfter(periodeFom));
	}

	@Override
	public EksisterendeAndel mapForEksisterendeAktiviteter(LocalDate fom) {
		return EksisterendeAndel.builder()
				.medAndelNr(andelsnr)
				.medArbeidsforhold(getArbeidsforhold())
				.build();
	}


	@Override
	public SplittetAndel mapForNyeAktiviteter(LocalDate periodeFom) {
		return mapSplittetAndel();
	}

	private SplittetAndel mapSplittetAndel() {
		SplittetAndel.Builder builder = SplittetAndel.builder()
				.medAktivitetstatus(getAktivitetStatus())
				.medArbeidsforhold(getArbeidsforhold());
		return builder.build();
	}

	public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AndelGraderingImpl kladd;

        private Builder() {
            kladd = new AndelGraderingImpl();
        }

        public Builder medAktivitetStatus(AktivitetStatusV2 aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medGraderinger(List<Gradering> graderinger) {
            kladd.graderinger = graderinger;
            return this;
        }

        public Builder medAndelsnr(Long andelsnr) {
            kladd.andelsnr = andelsnr;
            return this;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            if (arbeidsforhold != null) {
            	medAktivitetStatus(AktivitetStatusV2.AT);
            }
            return this;
        }

        public AndelGraderingImpl build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return "AndelGraderingImpl{" +
            "aktivitetStatus=" + aktivitetStatus +
            ", graderinger=" + graderinger +
            ", arbeidsforhold=" + arbeidsforhold +
            ", andelsnr=" + andelsnr +
            '}';
    }
}
