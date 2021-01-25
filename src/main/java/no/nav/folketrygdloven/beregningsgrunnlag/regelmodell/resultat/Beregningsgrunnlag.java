package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.sp.SykepengerGrunnlag;

public class Beregningsgrunnlag {
    private YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag;
    private final List<AktivitetStatusMedHjemmel> aktivitetStatuser = new ArrayList<>();
    private LocalDate skjæringstidspunkt;
    private Inntektsgrunnlag inntektsgrunnlag;
    @JsonManagedReference
    private final List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = new ArrayList<>();
    private SammenligningsGrunnlag sammenligningsGrunnlag;
    private EnumMap<AktivitetStatus, SammenligningsGrunnlag> sammenligningsGrunnlagPrStatus = new EnumMap<>(AktivitetStatus.class);
    private Dekningsgrad dekningsgrad = Dekningsgrad.DEKNINGSGRAD_100;
    private BigDecimal grunnbeløp;
    /**
     * Ved G-regulering skal gammel G-verdi brukes til å vurdere vilkåret (https://jira.adeo.no/browse/TFP-3599 / https://confluence.adeo.no/display/TVF/G-regulering)
     */
    private BigDecimal uregulertGrunnbeløp;
    private boolean hattMilitærIOpptjeningsperioden = false;
    private Konstanter konstanter = new Konstanter();

    private Beregningsgrunnlag() { }

    public Optional<YtelsesSpesifiktGrunnlag> getYtelsesSpesifiktGrunnlagHvisFinnes() {
        return Optional.ofNullable(ytelsesSpesifiktGrunnlag);
    }

	public YtelsesSpesifiktGrunnlag getYtelsesSpesifiktGrunnlag() {
		return ytelsesSpesifiktGrunnlag;
	}

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public Inntektsgrunnlag getInntektsgrunnlag() {
        return inntektsgrunnlag;
    }

    public List<AktivitetStatusMedHjemmel> getAktivitetStatuser() {
        return Collections.unmodifiableList(aktivitetStatuser);
    }

    public SammenligningsGrunnlag getSammenligningsGrunnlag() {
        return sammenligningsGrunnlag;
    }

    public List<BeregningsgrunnlagPeriode> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder.stream()
            .sorted(Comparator.comparing(bg -> bg.getBeregningsgrunnlagPeriode().getFom()))
            .collect(Collectors.toUnmodifiableList());
    }

    public boolean erBesteberegnet() {
        if (ytelsesSpesifiktGrunnlag instanceof ForeldrepengerGrunnlag) {
            return ((ForeldrepengerGrunnlag) ytelsesSpesifiktGrunnlag).erBesteberegnet();
        }
        return false;
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public BigDecimal getGrunnbeløp() {
        return grunnbeløp;
    }

    public BigDecimal getUregulertGrunnbeløp() {
        return uregulertGrunnbeløp;
    }

    public BigDecimal getMinsteinntektMilitærHarKravPå() {
        return grunnbeløp.multiply(BigDecimal.valueOf(getAntallGMilitærHarKravPå()));
    }

    public AktivitetStatusMedHjemmel getAktivitetStatus(AktivitetStatus aktivitetStatus) {
        return aktivitetStatuser.stream().filter(as -> as.inneholder(aktivitetStatus)).findAny()
                .orElseThrow(() -> new IllegalStateException("Beregningsgrunnlaget mangler regel for status " + aktivitetStatus.getBeskrivelse()));
    }

    public long verdiAvG(LocalDate dato) {
        Optional<Grunnbeløp> optional = konstanter.getGrunnbeløpSatser().stream()
            .filter(g -> !dato.isBefore(g.getFom()) && !dato.isAfter(g.getTom()))
            .findFirst();

        if (optional.isPresent()) {
            return optional.get().getGVerdi();
        } else {
            throw new IllegalArgumentException("Kjenner ikke G-verdi for året " + dato.getYear());
        }
    }

    public BigDecimal getAntallGØvreGrenseverdi() {
        return konstanter.getAntallGØvreGrenseverdi();
    }

    public BigDecimal getYtelsedagerPrÅr() {
        return konstanter.getYtelsedagerIPrÅr();
    }

    public BigDecimal getAvviksgrenseProsent() {
        return konstanter.getAvviksgrenseProsent();
    }

    public BigDecimal getAntallGMinstekravVilkår() {
        return konstanter.getAntallGMinstekravVilkår();
    }

	public List<Grunnbeløp> getGrunnbeløpsatser() {
		return konstanter.getGrunnbeløpSatser();
	}

    public long snittverdiAvG(int år) {
        Optional<Grunnbeløp> optional = konstanter.getGrunnbeløpSatser().stream().filter(g -> g.getFom().getYear() == år).findFirst();
        if (optional.isPresent()) {
            return optional.get().getGSnitt();
        } else {
            throw new IllegalArgumentException("Kjenner ikke GSnitt-verdi for året " + år);
        }
    }

    public boolean isBeregningForSykepenger() {
        return ytelsesSpesifiktGrunnlag instanceof SykepengerGrunnlag;
    }

    public boolean harHattMilitærIOpptjeningsperioden() {
        return hattMilitærIOpptjeningsperioden;
    }

    public int getAntallGMilitærHarKravPå() {
        return konstanter.getAntallGMilitærHarKravPå();
    }

    public Map<AktivitetStatus, SammenligningsGrunnlag> getSammenligningsGrunnlagPrAktivitetstatus() {
        return sammenligningsGrunnlagPrStatus;
    }

    public SammenligningsGrunnlag getSammenligningsGrunnlagPrAktivitetstatus(AktivitetStatus aktivitetStatus) {
        return sammenligningsGrunnlagPrStatus.get(aktivitetStatus);
    }

    public boolean isSplitteATFLToggleErPå() {
        return konstanter.isSplitteATFLToggleErPå();
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

        public Builder medInntektsgrunnlag(Inntektsgrunnlag inntektsgrunnlag) {
            beregningsgrunnlagMal.inntektsgrunnlag = inntektsgrunnlag;
            return this;
        }

        public Builder medSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
            beregningsgrunnlagMal.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Builder medAktivitetStatuser(List<AktivitetStatusMedHjemmel> aktivitetStatusList) {
            beregningsgrunnlagMal.aktivitetStatuser.addAll(aktivitetStatusList);
            return this;
        }

        public Builder medSammenligningsgrunnlag(SammenligningsGrunnlag sammenligningsGrunnlag) {
            beregningsgrunnlagMal.sammenligningsGrunnlag = sammenligningsGrunnlag;
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

        public Builder medDekningsgrad(Dekningsgrad dekningsgrad) {
            beregningsgrunnlagMal.dekningsgrad = dekningsgrad;
            return this;
        }

        public Builder medGrunnbeløp(BigDecimal grunnbeløp) {
            beregningsgrunnlagMal.grunnbeløp = grunnbeløp;
            return this;
        }

        public Builder medUregulertGrunnbeløp(BigDecimal uregulertGrunnbeløp) {
            beregningsgrunnlagMal.uregulertGrunnbeløp = uregulertGrunnbeløp;
            return this;
        }

        public Builder medGrunnbeløpSatser(List<Grunnbeløp> grunnbeløpSatser) {
            beregningsgrunnlagMal.konstanter.setGrunnbeløpSatser(grunnbeløpSatser);
            return this;
        }

        //Brukes bare i sykepenger og i enhetstest
        public Builder medBeregningForSykepenger(boolean beregningForSykepenger) {
            if (beregningForSykepenger) {
                beregningsgrunnlagMal.ytelsesSpesifiktGrunnlag = new SykepengerGrunnlag();
            }
            return this;
        }

        public Builder medMilitærIOpptjeningsperioden(boolean hattMilitærIOpptjeningsperioden) {
            beregningsgrunnlagMal.hattMilitærIOpptjeningsperioden = hattMilitærIOpptjeningsperioden;
            return this;
        }

        public Builder medAntallGMilitærHarKravPå(int antallGMilitærHarKravPå) {
            beregningsgrunnlagMal.konstanter.setAntallGMilitærHarKravPå(antallGMilitærHarKravPå);
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

        public Builder medAvviksgrenseProsent(BigDecimal avviksgrenseProsent) {
            beregningsgrunnlagMal.konstanter.setAvviksgrenseProsent(avviksgrenseProsent);
            return this;
        }

        public Builder medAntallGMinstekravVilkår(BigDecimal antallGMinstekravVilkår) {
            beregningsgrunnlagMal.konstanter.setAntallGMinstekravVilkår(antallGMinstekravVilkår);
            return this;
        }

        public Builder medSammenligningsgrunnlagPrStatus(AktivitetStatus aktivitetStatus, SammenligningsGrunnlag sammenligningsGrunnlag) {
            beregningsgrunnlagMal.sammenligningsGrunnlagPrStatus.put(aktivitetStatus, sammenligningsGrunnlag);
            return this;
        }

        public Builder medYtelsesSpesifiktGrunnlag(YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
            if (ytelsesSpesifiktGrunnlag != null) {
                ytelsesSpesifiktGrunnlag.setBeregningsgrunnlag(beregningsgrunnlagMal);
            }
            beregningsgrunnlagMal.ytelsesSpesifiktGrunnlag = ytelsesSpesifiktGrunnlag;
            return this;
        }


        public Builder medSplitteATFLToggleVerdi(boolean splitteATFLToggleErPå) {
            beregningsgrunnlagMal.konstanter.setSplitteATFLToggleErPå(splitteATFLToggleErPå);
            return this;
        }


        public Beregningsgrunnlag build() {
            verifyStateForBuild();
            return beregningsgrunnlagMal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagMal.inntektsgrunnlag, "inntektsgrunnlag");
            Objects.requireNonNull(beregningsgrunnlagMal.skjæringstidspunkt, "skjæringstidspunkt");
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
