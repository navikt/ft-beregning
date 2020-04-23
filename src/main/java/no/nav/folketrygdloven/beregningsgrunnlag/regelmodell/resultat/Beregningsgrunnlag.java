package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
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
    private List<Grunnbeløp> grunnbeløpSatser = new ArrayList<>();
    private boolean hattMilitærIOpptjeningsperioden = false;
    private int antallGMilitærHarKravPå = 3;
    private BigDecimal antallGØvreGrenseverdi;
    private BigDecimal antallGMinstekravVilkår;
    private BigDecimal ytelsedagerIPrÅr;
    private BigDecimal avviksgrenseProsent;

    private Beregningsgrunnlag() { }

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
        if (ytelsesSpesifiktGrunnlag != null && ytelsesSpesifiktGrunnlag instanceof ForeldrepengerGrunnlag) {
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

    public BigDecimal getMinsteinntektMilitærHarKravPå() {
        return grunnbeløp.multiply(BigDecimal.valueOf(antallGMilitærHarKravPå));
    }

    public AktivitetStatusMedHjemmel getAktivitetStatus(AktivitetStatus aktivitetStatus) {
        return aktivitetStatuser.stream().filter(as -> as.inneholder(aktivitetStatus)).findAny()
                .orElseThrow(() -> new IllegalStateException("Beregningsgrunnlaget mangler regel for status " + aktivitetStatus.getBeskrivelse()));
    }

    public long verdiAvG(LocalDate dato) {
        Optional<Grunnbeløp> optional = grunnbeløpSatser.stream()
            .filter(g -> !dato.isBefore(g.getFom()) && !dato.isAfter(g.getTom()))
            .findFirst();

        if (optional.isPresent()) {
            return optional.get().getGVerdi();
        } else {
            throw new IllegalArgumentException("Kjenner ikke G-verdi for året " + dato.getYear());
        }
    }

    public BigDecimal getAntallGØvreGrenseverdi() {
        return antallGØvreGrenseverdi;
    }

    public BigDecimal getYtelsedagerPrÅr() {
        if (ytelsedagerIPrÅr == null) {
            return BigDecimal.valueOf(260);
        }
        return ytelsedagerIPrÅr;
    }

    public BigDecimal getAvviksgrenseProsent() {
        if (avviksgrenseProsent == null) {
            return BigDecimal.valueOf(25);
        }
        return avviksgrenseProsent;
    }

    public BigDecimal getAntallGMinstekravVilkår() {
        if (antallGMinstekravVilkår == null) {
            return BigDecimal.valueOf(0.5);
        }
        return antallGMinstekravVilkår;
    }


    public long snittverdiAvG(int år) {
        Optional<Grunnbeløp> optional = grunnbeløpSatser.stream().filter(g -> g.getFom().getYear() == år).findFirst();
        if (optional.isPresent()) {
            return optional.get().getGSnitt();
        } else {
            throw new IllegalArgumentException("Kjenner ikke GSnitt-verdi for året " + år);
        }
    }

    public boolean isBeregningForSykepenger() {
        return ytelsesSpesifiktGrunnlag != null && ytelsesSpesifiktGrunnlag instanceof SykepengerGrunnlag;
    }

    public boolean harHattMilitærIOpptjeningsperioden() {
        return hattMilitærIOpptjeningsperioden;
    }

    public int getAntallGMilitærHarKravPå() {
        return antallGMilitærHarKravPå;
    }

    public EnumMap<AktivitetStatus, SammenligningsGrunnlag> getSammenligningsGrunnlagPrAktivitetstatus() {
        return sammenligningsGrunnlagPrStatus;
    }

    public SammenligningsGrunnlag getSammenligningsGrunnlagPrAktivitetstatus(AktivitetStatus aktivitetStatus) {
        return sammenligningsGrunnlagPrStatus.get(aktivitetStatus);
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

        public Builder medGrunnbeløpSatser(List<Grunnbeløp> grunnbeløpSatser) {
            beregningsgrunnlagMal.grunnbeløpSatser.clear();
            beregningsgrunnlagMal.grunnbeløpSatser.addAll(grunnbeløpSatser);
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
            beregningsgrunnlagMal.antallGMilitærHarKravPå = antallGMilitærHarKravPå;
            return this;
        }

        public Builder medAntallGØvreGrenseverdi(BigDecimal grenseverdi) {
            beregningsgrunnlagMal.antallGØvreGrenseverdi = grenseverdi;
            return this;
        }

        public Builder medYtelsesdagerIEtÅr(BigDecimal ytelsesdagerIEtÅr) {
            beregningsgrunnlagMal.ytelsedagerIPrÅr = ytelsesdagerIEtÅr;
            return this;
        }

        public Builder medAvviksgrenseProsent(BigDecimal avviksgrenseProsent) {
            beregningsgrunnlagMal.avviksgrenseProsent = avviksgrenseProsent;
            return this;
        }

        public Builder medAntallGMinstekravVilkår(BigDecimal antallGMinstekravVilkår) {
            beregningsgrunnlagMal.antallGMinstekravVilkår = antallGMinstekravVilkår;
            return this;
        }

        public Builder medSammenligningsgrunnlagPrStatus(AktivitetStatus aktivitetStatus, SammenligningsGrunnlag sammenligningsGrunnlag) {
            beregningsgrunnlagMal.sammenligningsGrunnlagPrStatus.put(aktivitetStatus, sammenligningsGrunnlag);
            return this;
        }

        public Builder medYtelsesSpesifiktGrunnlag(YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
            beregningsgrunnlagMal.ytelsesSpesifiktGrunnlag = ytelsesSpesifiktGrunnlag;
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
            if (beregningsgrunnlagMal.grunnbeløpSatser.isEmpty()) {
                throw new IllegalStateException("Beregningsgrunnlaget må inneholde grunnbeløpsatser");
            }
        }
    }
}
