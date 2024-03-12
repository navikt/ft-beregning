package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.diff.DiffIgnore;
import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

public class BeregningsgrunnlagPeriodeDto implements IndexKey {

    @DiffIgnore
    private BeregningsgrunnlagDto beregningsgrunnlag;
    @SjekkVedKopiering
    private List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList = new ArrayList<>();
    @SjekkVedKopiering
    private List<TilkommetInntektDto> tilkomneInntekter = new ArrayList<>();

    // Ikkje legg til @SjekkVedKopiering her. Det ødelegger difflogikk ved forlengelse
    private Intervall periode;
    @SjekkVedKopiering
    private Beløp bruttoPrÅr;
    private Beløp avkortetPrÅr;
    private Beløp redusertPrÅr;
    private Long dagsats;
    @SjekkVedKopiering
    private List<BeregningsgrunnlagPeriodeÅrsakDto> beregningsgrunnlagPeriodeÅrsaker = new ArrayList<>();
    private BigDecimal inntektgraderingsprosentBrutto;
    private BigDecimal totalUtbetalingsgradFraUttak;
    private BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
    private BigDecimal reduksjonsfaktorInaktivTypeA;

    private BeregningsgrunnlagPeriodeDto() {
    }

    public BeregningsgrunnlagPeriodeDto(BeregningsgrunnlagPeriodeDto kopiereFra) {
        this.beregningsgrunnlagPrStatusOgAndelList = kopiereFra.beregningsgrunnlagPrStatusOgAndelList.stream().map(a ->
                {
                    BeregningsgrunnlagPrStatusOgAndelDto kopi = new BeregningsgrunnlagPrStatusOgAndelDto(a);
                    kopi.setBeregningsgrunnlagPeriode(this);
                    return kopi;
                }
        ).collect(Collectors.toList());

        this.tilkomneInntekter = kopiereFra.tilkomneInntekter.stream().map(TilkommetInntektDto::new).collect(Collectors.toList());

        this.beregningsgrunnlagPeriodeÅrsaker = kopiereFra.beregningsgrunnlagPeriodeÅrsaker.stream().map(o ->
                BeregningsgrunnlagPeriodeÅrsakDto.Builder.kopier(o).build(this)
        ).collect(Collectors.toList());

        this.periode = kopiereFra.periode;
        this.bruttoPrÅr = kopiereFra.bruttoPrÅr;
        this.avkortetPrÅr = kopiereFra.avkortetPrÅr;
        this.redusertPrÅr = kopiereFra.redusertPrÅr;
        this.dagsats = kopiereFra.dagsats;
        this.inntektgraderingsprosentBrutto = kopiereFra.inntektgraderingsprosentBrutto;
        this.totalUtbetalingsgradFraUttak = kopiereFra.totalUtbetalingsgradFraUttak;
        this.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = kopiereFra.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
        this.reduksjonsfaktorInaktivTypeA = kopiereFra.reduksjonsfaktorInaktivTypeA;
    }

    public static Builder ny() {
        return new Builder();
    }

    public static Builder kopier(BeregningsgrunnlagPeriodeDto eksisterendeBeregningsgrunnlagPeriode) {
        return new Builder(eksisterendeBeregningsgrunnlagPeriode);
    }

    public static Builder oppdater(BeregningsgrunnlagPeriodeDto eksisterendeBeregningsgrunnlagPeriode) {
        return new Builder(eksisterendeBeregningsgrunnlagPeriode, true);
    }

    public List<BeregningsgrunnlagPrStatusOgAndelDto> getBeregningsgrunnlagPrStatusOgAndelList() {
        return Collections.unmodifiableList(beregningsgrunnlagPrStatusOgAndelList);
    }

    public List<TilkommetInntektDto> getTilkomneInntekter() {
        return tilkomneInntekter;
    }

    public Intervall getPeriode() {
        if (periode.getTomDato() == null) {
            return Intervall.fraOgMedTilOgMed(periode.getFomDato(), TIDENES_ENDE);
        }
        return periode;
    }

    public LocalDate getBeregningsgrunnlagPeriodeFom() {
        return periode.getFomDato();
    }

    public LocalDate getBeregningsgrunnlagPeriodeTom() {
        return periode.getTomDato();
    }

    public Beløp getBeregnetPrÅr() {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    void updateBruttoPrÅr() {
        bruttoPrÅr = beregningsgrunnlagPrStatusOgAndelList.stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    public Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Beløp getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public Beløp getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public List<BeregningsgrunnlagPeriodeÅrsakDto> getBeregningsgrunnlagPeriodeÅrsaker() {
        return Collections.unmodifiableList(beregningsgrunnlagPeriodeÅrsaker);
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return beregningsgrunnlagPeriodeÅrsaker.stream().map(BeregningsgrunnlagPeriodeÅrsakDto::getPeriodeÅrsak).collect(Collectors.toList());
    }

    void addBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto bgPrStatusOgAndel) {
        Objects.requireNonNull(bgPrStatusOgAndel, "beregningsgrunnlagPrStatusOgAndel");
        if (!beregningsgrunnlagPrStatusOgAndelList.contains(bgPrStatusOgAndel)) {
            beregningsgrunnlagPrStatusOgAndelList.add(bgPrStatusOgAndel);
        }
    }

    void addBeregningsgrunnlagPeriodeÅrsak(BeregningsgrunnlagPeriodeÅrsakDto bgPeriodeÅrsak) {
        Objects.requireNonNull(bgPeriodeÅrsak, "beregningsgrunnlagPeriodeÅrsak");
        if (!beregningsgrunnlagPeriodeÅrsaker.contains(bgPeriodeÅrsak)) {
            beregningsgrunnlagPeriodeÅrsaker.add(bgPeriodeÅrsak);
        }
    }

    @Deprecated
    public BigDecimal getInntektgraderingsprosentBrutto() {
        return inntektgraderingsprosentBrutto;
    }

    public BigDecimal getTotalUtbetalingsgradFraUttak() {
        return totalUtbetalingsgradFraUttak;
    }

    public BigDecimal getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt() {
        return totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
    }

    public BigDecimal getReduksjonsfaktorInaktivTypeA() {
        return reduksjonsfaktorInaktivTypeA;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPeriodeDto)) {
            return false;
        }
        BeregningsgrunnlagPeriodeDto other = (BeregningsgrunnlagPeriodeDto) obj;
        return Objects.equals(this.periode.getFomDato(), other.periode.getFomDato())
                && Objects.equals(this.periode.getTomDato(), other.periode.getTomDato())
                && Objects.equals(this.getBruttoPrÅr(), other.getBruttoPrÅr())
                && Objects.equals(this.getAvkortetPrÅr(), other.getAvkortetPrÅr())
                && Objects.equals(this.getRedusertPrÅr(), other.getRedusertPrÅr())
                && Objects.equals(this.getDagsats(), other.getDagsats());
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, bruttoPrÅr, avkortetPrÅr, redusertPrÅr, dagsats);
    }

    @Override
    public String toString() {
        return "BeregningsgrunnlagPeriodeDto{" +
                "beregningsgrunnlagPrStatusOgAndelList=" + beregningsgrunnlagPrStatusOgAndelList +
                ", periode=" + periode +
                ", bruttoPrÅr=" + bruttoPrÅr +
                ", avkortetPrÅr=" + avkortetPrÅr +
                ", redusertPrÅr=" + redusertPrÅr +
                ", dagsats=" + dagsats +
                ", beregningsgrunnlagPeriodeÅrsaker=" + beregningsgrunnlagPeriodeÅrsaker +
                '}';
    }

    @Override
    public String getIndexKey() {
        return periode.getFomDato() + "_" + periode.getTomDato();
    }

    public static class Builder {
        private BeregningsgrunnlagPeriodeDto kladd;
        private boolean built;
        private boolean oppdater;

        public Builder(BeregningsgrunnlagPeriodeDto eksisterendeBeregningsgrunnlagPeriod, boolean oppdater) {
            this.oppdater = oppdater;
            this.kladd = eksisterendeBeregningsgrunnlagPeriod;
        }

        public Builder() {
            kladd = new BeregningsgrunnlagPeriodeDto();
        }

        public Builder(BeregningsgrunnlagPeriodeDto eksisterendeBeregningsgrunnlagPeriod) {
            this.kladd = new BeregningsgrunnlagPeriodeDto(eksisterendeBeregningsgrunnlagPeriod);
        }


        public static Builder kopier(BeregningsgrunnlagPeriodeDto p) {
            return new Builder(p);
        }

        public Optional<BeregningsgrunnlagPrStatusOgAndelDto.Builder> getBuilderForAndel(Long andelsnr, boolean oppdater) {
            return this.kladd.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(a -> a.getAndelsnr().equals(andelsnr))
                    .findFirst()
                    .map(a -> oppdater ? BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(a) : BeregningsgrunnlagPrStatusOgAndelDto.kopier(a));
        }

        public Builder leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList.add(beregningsgrunnlagPrStatusOgAndel);
            return this;
        }

        public Builder leggTilTilkommetInntekt(TilkommetInntektDto tilkommetInntektDto) {
            verifiserKanModifisere();
            var eksisterende = kladd.tilkomneInntekter.stream().filter(tilkommetInntektDto::matcher).findFirst();
            eksisterende.ifPresent(kladd.tilkomneInntekter::remove);
            kladd.tilkomneInntekter.add(tilkommetInntektDto);
            return this;
        }


        public Builder fjernBeregningsgrunnlagPrStatusOgAndelerSomIkkeLiggerIListeAvAndelsnr(List<Long> listeAvAndelsnr) {
            verifiserKanModifisere();
            List<BeregningsgrunnlagPrStatusOgAndelDto> andelerSomSkalFjernes = new ArrayList<>();
            for (BeregningsgrunnlagPrStatusOgAndelDto andel : kladd.getBeregningsgrunnlagPrStatusOgAndelList()) {
                if (!listeAvAndelsnr.contains(andel.getAndelsnr()) && andel.erLagtTilAvSaksbehandler()) {
                    andelerSomSkalFjernes.add(andel);
                }
            }
            kladd.beregningsgrunnlagPrStatusOgAndelList.removeAll(andelerSomSkalFjernes);
            return this;
        }

        public Builder leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder prStatusOgAndelBuilder) {
            verifiserKanModifisere();
            prStatusOgAndelBuilder.build(kladd);
            return this;
        }

        public Builder medBeregningsgrunnlagPrStatusOgAndel(List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndeler) {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndeler;
            return this;
        }

        public Builder fjernAlleBeregningsgrunnlagPrStatusOgAndeler() {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList = new ArrayList<>();
            return this;
        }

        public Builder fjernBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList.remove(beregningsgrunnlagPrStatusOgAndel);
            return this;
        }

        public Builder medBeregningsgrunnlagPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
            verifiserKanModifisere();
            kladd.periode = tilOgMed == null ? Intervall.fraOgMed(fraOgMed) : Intervall.fraOgMedTilOgMed(fraOgMed, tilOgMed);
            return this;
        }

        public Builder medBruttoPrÅr(Beløp bruttoPrÅr) {
            verifiserKanModifisere();
            kladd.bruttoPrÅr = bruttoPrÅr;
            return this;
        }

        public Builder medAvkortetPrÅr(Beløp avkortetPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(Beløp redusertPrÅr) {
            verifiserKanModifisere();
            kladd.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medInntektsgraderingsprosentBrutto(BigDecimal graderinsprosent) {
            verifiserKanModifisere();
            kladd.inntektgraderingsprosentBrutto = graderinsprosent;
            return this;
        }
        public Builder medTotalUtbetalingsgradFraUttak(BigDecimal totalUtbetalingsgradFraUttak) {
            verifiserKanModifisere();
            kladd.totalUtbetalingsgradFraUttak = totalUtbetalingsgradFraUttak;
            return this;
        }

        public Builder medTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt) {
            verifiserKanModifisere();
            kladd.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
            return this;
        }

        public Builder medReduksjonsfaktorInaktivTypeA(BigDecimal reduksjonsfaktorInaktivTypeA) {
            verifiserKanModifisere();
            kladd.reduksjonsfaktorInaktivTypeA = reduksjonsfaktorInaktivTypeA;
            return this;
        }

        public Builder leggTilPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            verifiserKanModifisere();
            if (!kladd.getPeriodeÅrsaker().contains(periodeÅrsak)) {
                BeregningsgrunnlagPeriodeÅrsakDto.Builder bgPeriodeÅrsakBuilder = new BeregningsgrunnlagPeriodeÅrsakDto.Builder();
                bgPeriodeÅrsakBuilder.medPeriodeÅrsak(periodeÅrsak);
                bgPeriodeÅrsakBuilder.build(kladd);
            }
            return this;
        }

        public Builder leggTilPeriodeÅrsaker(Collection<PeriodeÅrsak> periodeÅrsaker) {
            verifiserKanModifisere();
            periodeÅrsaker.forEach(this::leggTilPeriodeÅrsak);
            return this;
        }

        public Builder fjernPeriodeårsaker() {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPeriodeÅrsaker.clear();
            return this;
        }

        public BeregningsgrunnlagPeriodeDto build() {
            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                    .filter(bgpsa -> bgpsa.getDagsats() != null)
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getDagsats)
                    .reduce(Long::sum)
                    .orElse(null);
            kladd.dagsats = dagsatsSum;
            built = true;
            return kladd;
        }

        public BeregningsgrunnlagPeriodeDto build(BeregningsgrunnlagDto beregningsgrunnlag) {
            kladd.beregningsgrunnlag = beregningsgrunnlag;
            verifyStateForBuild();

            kladd.beregningsgrunnlag.leggTilBeregningsgrunnlagPeriode(kladd);

            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                    .filter(bgpsa -> bgpsa.getDagsats() != null)
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getDagsats)
                    .reduce(Long::sum)
                    .orElse(null);
            kladd.dagsats = dagsatsSum;
            built = true;
            return kladd;
        }

        public BeregningsgrunnlagPeriodeDto buildForKopi() {
            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                    .filter(bgpsa -> bgpsa.getDagsats() != null)
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getDagsats)
                    .reduce(Long::sum)
                    .orElse(null);
            kladd.dagsats = dagsatsSum;
            built = true;
            return kladd;
        }

        private void verifiserKanModifisere() {
            if (built) {
                throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
            }
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.beregningsgrunnlag, "beregningsgrunnlag");
            Objects.requireNonNull(kladd.beregningsgrunnlagPrStatusOgAndelList, "beregningsgrunnlagPrStatusOgAndelList");
            Objects.requireNonNull(kladd.periode, "beregningsgrunnlagPeriodeFom");
        }
    }
}
