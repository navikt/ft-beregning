package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

public class BeregningsgrunnlagDto {

    @SjekkVedKopiering
    private LocalDate skjæringstidspunkt;
    @SjekkVedKopiering
    private List<BeregningsgrunnlagAktivitetStatusDto> aktivitetStatuser = new ArrayList<>();
    @SjekkVedKopiering
    private List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder = new ArrayList<>();
    @SjekkVedKopiering
    private List<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatusListe = new ArrayList<>();
    @SjekkVedKopiering
    private Beløp grunnbeløp;
    @SjekkVedKopiering
    private List<BeregningsgrunnlagFaktaOmBeregningTilfelleDto> faktaOmBeregningTilfeller = new ArrayList<>();
    private boolean overstyrt = false;

    public BeregningsgrunnlagDto() {
    }

    public BeregningsgrunnlagDto(BeregningsgrunnlagDto kopiereFra) {

        this.skjæringstidspunkt = kopiereFra.skjæringstidspunkt;

        this.sammenligningsgrunnlagPrStatusListe = kopiereFra.getSammenligningsgrunnlagPrStatusListe().stream().map(s -> {
            SammenligningsgrunnlagPrStatusDto.Builder builder = SammenligningsgrunnlagPrStatusDto.Builder.kopier(s);
            SammenligningsgrunnlagPrStatusDto build = builder.build();
            return build;
        }).collect(Collectors.toList());

        this.beregningsgrunnlagPerioder = kopiereFra.getBeregningsgrunnlagPerioder().stream().map(p -> {
            BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.Builder.kopier(p);
            return builder.build(this);
        }).collect(Collectors.toList());

        this.faktaOmBeregningTilfeller = kopiereFra.getFaktaOmBeregningTilfeller().stream().map(p -> {
            BeregningsgrunnlagFaktaOmBeregningTilfelleDto.Builder builder = BeregningsgrunnlagFaktaOmBeregningTilfelleDto.Builder.kopier(p);
            return builder.build(this);
        }).collect(Collectors.toList());

        this.aktivitetStatuser = kopiereFra.getAktivitetStatuser().stream().map(p -> {
            BeregningsgrunnlagAktivitetStatusDto.Builder builder = BeregningsgrunnlagAktivitetStatusDto.Builder.kopier(p);
            return builder.build(this);
        }).collect(Collectors.toList());

        this.grunnbeløp = kopiereFra.getGrunnbeløp();
        this.overstyrt = kopiereFra.overstyrt;
    }


    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<BeregningsgrunnlagAktivitetStatusDto> getAktivitetStatuser() {
        return Collections.unmodifiableList(aktivitetStatuser);
    }

    public List<BeregningsgrunnlagPeriodeDto> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder
                .stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom))
                .collect(Collectors.toUnmodifiableList());
    }

    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }


    public void leggTilBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto bgAktivitetStatus) {
        Objects.requireNonNull(bgAktivitetStatus, "beregningsgrunnlagAktivitetStatus");
        aktivitetStatuser.remove(bgAktivitetStatus); // NOSONAR
        aktivitetStatuser.add(bgAktivitetStatus);
    }

    public void leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto bgPeriode) {
        Objects.requireNonNull(bgPeriode, "beregningsgrunnlagPeriode");
        if (!beregningsgrunnlagPerioder.contains(bgPeriode)) { // NOSONAR
            beregningsgrunnlagPerioder.add(bgPeriode);
        }
    }

    public Hjemmel getHjemmel() {
        if (aktivitetStatuser.isEmpty()) {
            return Hjemmel.UDEFINERT;
        }
        if (aktivitetStatuser.size() == 1) {
            return aktivitetStatuser.get(0).getHjemmel();
        }
        Optional<BeregningsgrunnlagAktivitetStatusDto> dagpenger = aktivitetStatuser.stream()
                .filter(as -> Hjemmel.F_14_7_8_49.equals(as.getHjemmel()))
                .findFirst();
        if (dagpenger.isPresent()) {
            return dagpenger.get().getHjemmel();
        }
        Optional<BeregningsgrunnlagAktivitetStatusDto> gjelder = aktivitetStatuser.stream()
                .filter(as -> !Hjemmel.F_14_7.equals(as.getHjemmel()))
                .findFirst();
        return gjelder.isPresent() ? gjelder.get().getHjemmel() : Hjemmel.F_14_7;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller
                .stream()
                .map(BeregningsgrunnlagFaktaOmBeregningTilfelleDto::getFaktaOmBeregningTilfelle)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<SammenligningsgrunnlagPrStatusDto> getSammenligningsgrunnlagPrStatusListe() {
        return sammenligningsgrunnlagPrStatusListe;
    }

    public Optional<SammenligningsgrunnlagPrStatusDto> getSammenligningsgrunnlagForStatus(SammenligningsgrunnlagType type) {
        return sammenligningsgrunnlagPrStatusListe.stream()
                .filter(sg -> sg.getSammenligningsgrunnlagType().equals(type))
                .findFirst();
    }

    public boolean isOverstyrt() {
        return overstyrt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagDto)) {
            return false;
        }
        BeregningsgrunnlagDto other = (BeregningsgrunnlagDto) obj;
        return Objects.equals(this.getSkjæringstidspunkt(), other.getSkjæringstidspunkt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skjæringstidspunkt);
    }

    @Override
    public String toString() {
        return "BeregningsgrunnlagDto{" +
                "skjæringstidspunkt=" + skjæringstidspunkt +
                ", aktivitetStatuser=" + aktivitetStatuser +
                ", beregningsgrunnlagPerioder=" + beregningsgrunnlagPerioder +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagDto original) {
        return new Builder(original);
    }

    public static class Builder {
        private boolean built;
        private BeregningsgrunnlagDto kladd;
        private boolean erOppdatering;

        private Builder() {
            kladd = new BeregningsgrunnlagDto();
        }

        private Builder(BeregningsgrunnlagDto original) {
            kladd = new BeregningsgrunnlagDto(original);
        }

        private Builder(BeregningsgrunnlagDto original, boolean erOppdatering) {
            kladd = original;
            this.erOppdatering = erOppdatering;
        }

        public static Builder oppdater(Optional<BeregningsgrunnlagDto> beregningsgrunnlag) {
            return beregningsgrunnlag.map(beregningsgrunnlagDto -> new Builder(beregningsgrunnlagDto, true)).orElse(new Builder());
        }

        public Builder medSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
            verifiserKanModifisere();
            kladd.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Builder medGrunnbeløp(Beløp grunnbeløp) {
            verifiserKanModifisere();
            kladd.grunnbeløp = grunnbeløp;
            return this;
        }

        public Builder leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.Builder aktivitetStatusBuilder) {
            verifiserKanModifisere();
            aktivitetStatusBuilder.build(kladd);
            return this;
        }

        public Builder leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.Builder beregningsgrunnlagPeriodeBuilder) {
            verifiserKanModifisere();
            beregningsgrunnlagPeriodeBuilder.build(kladd);
            return this;
        }

        public Builder fjernAllePerioder() {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPerioder = new ArrayList<>();
            return this;
        }

        public Builder fjernAktivitetstatus(AktivitetStatus status) {
            verifiserKanModifisere();
            List<BeregningsgrunnlagAktivitetStatusDto> statuserSomSkalFjernes = kladd.aktivitetStatuser.stream().filter(a -> Objects.equals(a.getAktivitetStatus(), status)).collect(Collectors.toList());
            if (statuserSomSkalFjernes.size() != 1) {
                throw new IllegalStateException("Ikke entydig hvilken status som skal fjernes fra beregningsgrunnlaget.");
            }
            kladd.aktivitetStatuser.remove(statuserSomSkalFjernes.get(0));
            return this;
        }

        public Builder leggTilFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
            verifiserKanModifisere();
            faktaOmBeregningTilfeller.forEach(this::leggTilFaktaOmBeregningTilfeller);
            return this;
        }

        private void leggTilFaktaOmBeregningTilfeller(FaktaOmBeregningTilfelle tilfelle) {
            verifiserKanModifisere();
            BeregningsgrunnlagFaktaOmBeregningTilfelleDto b = BeregningsgrunnlagFaktaOmBeregningTilfelleDto.builder().medFaktaOmBeregningTilfelle(tilfelle).build(kladd);
            this.kladd.faktaOmBeregningTilfeller.add(b);
        }

        public Builder leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusDto sgPrStatus) {
            verifiserIngenLikeSammenligningsgrunnlag(sgPrStatus);
            kladd.sammenligningsgrunnlagPrStatusListe.add(sgPrStatus);
            return this;
        }

        private void verifiserIngenLikeSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusDto sgPrStatus) {
            var finnesAlleredeSGAForStatus = kladd.sammenligningsgrunnlagPrStatusListe.stream()
                    .anyMatch(sg -> sg.getSammenligningsgrunnlagType().equals(sgPrStatus.getSammenligningsgrunnlagType()));
            if (finnesAlleredeSGAForStatus) {
                throw new IllegalStateException("FEIL: Prøver legge til sammenligningsgrunnlag med status " + sgPrStatus.getSammenligningsgrunnlagType() +
                        " men et sammenligningsgrunnlag med denne statusen finnes allerede på grunnlaget!");
            }
        }

        public Builder medOverstyring(boolean overstyrt) {
            verifiserKanModifisere();
            kladd.overstyrt = overstyrt;
            return this;
        }

        public BeregningsgrunnlagDto getBeregningsgrunnlag() {
            return kladd;
        }

        public Optional<BeregningsgrunnlagPeriodeDto.Builder> getPeriodeBuilderFor(Intervall periode) {
            return kladd.getBeregningsgrunnlagPerioder().stream().filter(p -> p.getPeriode().equals(periode))
                    .findFirst()
                    .map(BeregningsgrunnlagPeriodeDto::oppdater);
        }

        public BeregningsgrunnlagDto build() {
            verifyStateForBuild();
            built = true;
            return kladd;
        }

        private void verifiserKanModifisere() {
            if (built) {
                throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
            }
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(kladd.skjæringstidspunkt, "skjæringstidspunkt");
        }
    }
}
