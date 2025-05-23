package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

public class BeregningsgrunnlagPrStatus {
    @JsonBackReference
    protected BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;
    private AktivitetStatus aktivitetStatus;
    protected List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = new ArrayList<>();

	// Brukes i regler
	private BigDecimal overstyrtPrÅr;
	private BigDecimal fordeltPrÅr;
	private BigDecimal beregnetPrÅr;
	private BigDecimal besteberegningPrÅr;
	private Boolean nyIArbeidslivet; // Kun SN
	private Inntektskategori inntektskategori;
    private boolean fastsattAvSaksbehandler = false;
	private BigDecimal gjennomsnittligPGI;
	private Periode beregningsperiode;

	// Kun brukt i output
	private BigDecimal årsbeløpFraTilstøtendeYtelse;
	private Long andelNr;
    private Long orginalDagsatsFraTilstøtendeYtelse;
	private List<BigDecimal> pgiListe = new ArrayList<>();
	private BigDecimal avkortetPrÅr;
	private BigDecimal redusertPrÅr;

    // Disse er kun brukt av FRISINN i denne modellen
    private Boolean erSøktYtelseFor;
    private BigDecimal utbetalingsprosent = BigDecimal.valueOf(100);

    @JsonIgnore
    public BeregningsgrunnlagPeriode getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Periode getBeregningsperiode() {
        return beregningsperiode;
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr != null ? avkortetPrÅr : getArbeidsforhold().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr != null ? redusertPrÅr : getArbeidsforhold().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getRedusertPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
    }

    public BigDecimal getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public BigDecimal getFordeltPrÅr() {
        return fordeltPrÅr != null ? fordeltPrÅr : getArbeidsforhold().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getFordeltPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
    }

    public boolean erArbeidstakerEllerFrilanser() {
            return AktivitetStatus.erArbeidstaker(aktivitetStatus) || AktivitetStatus.erFrilanser(aktivitetStatus);
    }

    public BigDecimal samletNaturalytelseBortfaltMinusTilkommetPrÅr() {
        var sumBortfaltNaturalYtelse = getArbeidsforhold().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getNaturalytelseBortfaltPrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var sumTilkommetNaturalYtelse = getArbeidsforhold().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getNaturalytelseTilkommetPrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumBortfaltNaturalYtelse.subtract(sumTilkommetNaturalYtelse);
    }

    public BigDecimal samletGradertNaturalytelseBortfaltMinusTilkommetPrÅr() {
        var sumBortfaltNaturalYtelse = getArbeidsforhold().stream()
                .map(BeregningsgrunnlagPrArbeidsforhold::getGradertNaturalytelseBortfaltPrÅr)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var sumTilkommetNaturalYtelse = getArbeidsforhold().stream()
                .map(BeregningsgrunnlagPrArbeidsforhold::getGradertNaturalytelseTilkommetPrÅr)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumBortfaltNaturalYtelse.subtract(sumTilkommetNaturalYtelse);
    }

	/**
	 * Finner brutto pr år for status. Om dette er en arbeidstaker eller frilanserstatus hentes brutto fra arbeidsforhold.
	 *
	 * Prioritering av felter framgår av prosessen i kalkulus.
	 *
	 * @return Brutto pr år
	 */
	// TODO (TFP-3955) Denne må vere uavhengig av prosessen i kalkulus.
    public BigDecimal getBruttoPrÅr() {
        var bruttoPrÅr = getBruttoFraStatus();
	    return bruttoPrÅr != null ? bruttoPrÅr : getArbeidsforhold().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getBruttoPrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

	private BigDecimal getBruttoFraStatus() {
		BigDecimal bruttoPrÅr = null;
		if (fordeltPrÅr != null) {
			bruttoPrÅr = fordeltPrÅr;
		} else if (besteberegningPrÅr != null) {
			bruttoPrÅr = besteberegningPrÅr;
		} else if (overstyrtPrÅr != null) {
			bruttoPrÅr = overstyrtPrÅr;
		} else if (beregnetPrÅr != null) {
			bruttoPrÅr = beregnetPrÅr;
		}
		return bruttoPrÅr;
	}

    public BigDecimal getBruttoInkludertNaturalytelsePrÅr() {
        var brutto = getBruttoPrÅr();
        var samletNaturalytelse = samletNaturalytelseBortfaltMinusTilkommetPrÅr();
        return brutto.add(samletNaturalytelse);
    }

    public List<BeregningsgrunnlagPrArbeidsforhold> getArbeidsforhold() {
        return Collections.unmodifiableList(arbeidsforhold);
    }

    public List<BeregningsgrunnlagPrArbeidsforhold> getArbeidsforholdSomSkalBrukes() {
        return getArbeidsforhold().stream().filter(BeregningsgrunnlagPrArbeidsforhold::getErSøktYtelseFor).toList();
    }

    public List<BeregningsgrunnlagPrArbeidsforhold> getArbeidsforholdIkkeFrilans() {
        return getArbeidsforhold().stream().filter(af -> !af.erFrilanser()).toList();
    }

    public Optional<BeregningsgrunnlagPrArbeidsforhold> getFrilansArbeidsforhold() {
        return getArbeidsforhold().stream().filter(BeregningsgrunnlagPrArbeidsforhold::erFrilanser).findAny();
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr != null ? beregnetPrÅr : getArbeidsforhold().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getBeregnetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
    }

    public BigDecimal getGjennomsnittligPGI() {
        return gjennomsnittligPGI;
    }

    public List<BigDecimal> getPgiListe() {
        return pgiListe;
    }

    public Long getAndelNr() {
        return andelNr;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public BigDecimal getÅrsbeløpFraTilstøtendeYtelse() {
        return årsbeløpFraTilstøtendeYtelse;
    }

    public Boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    public BigDecimal getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public boolean erFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public Long getOrginalDagsatsFraTilstøtendeYtelse() {
        return orginalDagsatsFraTilstøtendeYtelse;
    }

    public boolean erSøktYtelseFor() {
        return  (erSøktYtelseFor != null && erSøktYtelseFor)
            || (erSøktYtelseFor == null && utbetalingsprosent.compareTo(BigDecimal.ZERO) > 0)
            || aktivitetStatus.equals(AktivitetStatus.ATFL);
    }

    public void setErSøktYtelseFor(boolean erSøktYtelseFor) {
        this.erSøktYtelseFor = erSøktYtelseFor;
    }

    void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
        return new Builder(beregningsgrunnlagPrStatus);
    }

    public static class Builder {
        private BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatusMal;

        public Builder() {
            beregningsgrunnlagPrStatusMal = new BeregningsgrunnlagPrStatus();
        }

        public Builder(BeregningsgrunnlagPrStatus eksisterendeBGPrStatusMal) {
            beregningsgrunnlagPrStatusMal = eksisterendeBGPrStatusMal;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            beregningsgrunnlagPrStatusMal.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medBeregningsperiode(Periode beregningsperiode) {
            beregningsgrunnlagPrStatusMal.beregningsperiode = beregningsperiode;
            return this;
        }

        public Builder medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold) {
            beregningsgrunnlagPrStatusMal.arbeidsforhold.add(beregningsgrunnlagPrArbeidsforhold);
            return this;
        }

        public Builder medBeregnetPrÅr(BigDecimal beregnetPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.beregnetPrÅr = beregnetPrÅr;
            return this;
        }

        public Builder medOverstyrtPrÅr(BigDecimal overstyrtPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.overstyrtPrÅr = overstyrtPrÅr;
            return this;
        }

        public Builder medFordeltPrÅr(BigDecimal fordeltPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.fordeltPrÅr = fordeltPrÅr;
            return this;
        }

        public Builder medAvkortetPrÅr(BigDecimal avkortetPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(BigDecimal redusertPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
            beregningsgrunnlagPrStatusMal.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
            beregningsgrunnlagPeriode.addBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatusMal);
            return this;
        }

        public Builder medÅrsbeløpFraTilstøtendeYtelse(BigDecimal årsbeløpFraTilstøtendeYtelse) {
            beregningsgrunnlagPrStatusMal.årsbeløpFraTilstøtendeYtelse = årsbeløpFraTilstøtendeYtelse;
            return this;
        }

        public Builder medErNyIArbeidslivet(Boolean nyIArbeidslivet) {
            beregningsgrunnlagPrStatusMal.nyIArbeidslivet = nyIArbeidslivet;
            return this;
        }

        public Builder medBesteberegningPrÅr(BigDecimal besteberegningPrÅr){
            beregningsgrunnlagPrStatusMal.besteberegningPrÅr = besteberegningPrÅr;
            return this;
        }

        public Builder medFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
            if (fastsattAvSaksbehandler != null) {
                beregningsgrunnlagPrStatusMal.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
            }
            return this;
        }

        private void sjekkIkkeArbeidstaker() {
            if (beregningsgrunnlagPrStatusMal.aktivitetStatus == null || AktivitetStatus.erArbeidstaker(beregningsgrunnlagPrStatusMal.aktivitetStatus)) {
                throw new IllegalArgumentException("Kan ikke overstyre aggregert verdi for status ATFL");
            }
        }

        public Builder medArbeidsforhold(List<Arbeidsforhold> arbeidsforhold) {
            if (arbeidsforhold != null) {
                var andelNr = 1;
                for (var af : arbeidsforhold) {
                    beregningsgrunnlagPrStatusMal.arbeidsforhold.add(BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(af).medAndelNr(andelNr++).build());
                }
            }
            return this;
        }

        // Kun brukt i test
        public Builder medArbeidsforhold(List<Arbeidsforhold> arbeidsforhold, List<BigDecimal> refusjonskravPrÅr, LocalDate skjæringstidspunkt) {
            if (arbeidsforhold != null) {
                if (!refusjonskravPrÅr.isEmpty() && arbeidsforhold.size() != refusjonskravPrÅr.size()) {
                    throw new IllegalArgumentException("Lengde på arbeidsforhold og refusjonskravPrÅr må vere like");
                }
                var beregningsperiode = Periode.of(skjæringstidspunkt.minusMonths(3).withDayOfMonth(1), skjæringstidspunkt.withDayOfMonth(1).minusDays(1));
                var andelNr = 1;
                for (var i = 0; i < arbeidsforhold.size(); i++) {
                    beregningsgrunnlagPrStatusMal.arbeidsforhold.add(BeregningsgrunnlagPrArbeidsforhold.builder()
                        .medArbeidsforhold(arbeidsforhold.get(i))
                        .medAndelNr(andelNr++)
                        .medGjeldendeRefusjonPrÅr(refusjonskravPrÅr.isEmpty() ? null : refusjonskravPrÅr.get(i))
                        .medBeregningsperiode(beregningsperiode)
                        .build());
                }
            }
            return this;
        }

        public Builder medGjennomsnittligPGI(BigDecimal gjennomsnittligPGI) {
            beregningsgrunnlagPrStatusMal.gjennomsnittligPGI = gjennomsnittligPGI;
            return this;
        }

        public Builder medAndelNr(Long andelNr) {
            beregningsgrunnlagPrStatusMal.andelNr = andelNr;
            return this;
        }

        public Builder medPGI(List<BigDecimal> pgiListe) {
            Objects.requireNonNull(beregningsgrunnlagPrStatusMal.aktivitetStatus, "pgiListe");
            if (pgiListe.isEmpty() || pgiListe.size() == 3) {
                beregningsgrunnlagPrStatusMal.pgiListe = pgiListe;
            } else {
                throw new IllegalArgumentException("Liste over PGI må være tom eller inneholde eksakt 3 årsverdier");
            }
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            beregningsgrunnlagPrStatusMal.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medOrginalDagsatsFraTilstøtendeYtelse(Long orginalDagsatsFraTilstøtendeYtelse) {
            beregningsgrunnlagPrStatusMal.orginalDagsatsFraTilstøtendeYtelse = orginalDagsatsFraTilstøtendeYtelse;
            return this;
        }

        public Builder medUtbetalingsprosent(BigDecimal utbetalingsprosentSVP){
            beregningsgrunnlagPrStatusMal.utbetalingsprosent = utbetalingsprosentSVP;
            return this;
        }

		public BeregningsgrunnlagPrStatus build() {
            verifyStateForBuild();
            return beregningsgrunnlagPrStatusMal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagPrStatusMal.aktivitetStatus, "aktivitetStatus");
            if (AktivitetStatus.ATFL.equals(beregningsgrunnlagPrStatusMal.aktivitetStatus) || AktivitetStatus.AT.equals(beregningsgrunnlagPrStatusMal.aktivitetStatus)) {
                if (beregningsgrunnlagPrStatusMal.andelNr != null) {
                    throw new IllegalArgumentException("Andelsnr kan ikke angis for andel med status " + beregningsgrunnlagPrStatusMal.aktivitetStatus);
                }
            } else {
                Objects.requireNonNull(beregningsgrunnlagPrStatusMal.andelNr, "andelNr");
            }
        }
    }
}
