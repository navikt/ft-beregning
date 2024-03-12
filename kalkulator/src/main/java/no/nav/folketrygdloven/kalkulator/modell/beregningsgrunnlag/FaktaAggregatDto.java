package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.FaktaVurdering;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public class FaktaAggregatDto {

    private List<FaktaArbeidsforholdDto> faktaArbeidsforholdListe = new ArrayList<>();
    private FaktaAktørDto faktaAktør;

    public FaktaAggregatDto() {
    }

    public FaktaAggregatDto(FaktaAggregatDto faktaAggregatDto) {
        this.faktaArbeidsforholdListe = faktaAggregatDto.getFaktaArbeidsforhold().stream().map(FaktaArbeidsforholdDto::new).collect(Collectors.toList());
        this.faktaAktør = faktaAggregatDto.getFaktaAktør().map(FaktaAktørDto::new).orElse(null);
    }

    public List<FaktaArbeidsforholdDto> getFaktaArbeidsforhold() {
        return faktaArbeidsforholdListe.stream()
                .collect(Collectors.toUnmodifiableList());
    }

    public Optional<FaktaArbeidsforholdDto> getFaktaArbeidsforhold(BGAndelArbeidsforholdDto bgAndelArbeidsforholdDto) {
        return faktaArbeidsforholdListe.stream()
                .filter(fa -> fa.gjelderFor(bgAndelArbeidsforholdDto.getArbeidsgiver(), bgAndelArbeidsforholdDto.getArbeidsforholdRef()))
                .findFirst();
    }

    public Optional<FaktaArbeidsforholdDto> getFaktaArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (andel.getBgAndelArbeidsforhold().isEmpty()) {
            return Optional.empty();
        }
        return faktaArbeidsforholdListe.stream()
                .filter(fa -> fa.gjelderFor(andel.getBgAndelArbeidsforhold().get().getArbeidsgiver(), andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef()))
                .findFirst();
    }


    public Optional<FaktaAktørDto> getFaktaAktør() {
        return Optional.ofNullable(faktaAktør);
    }

    private void leggTilFaktaArbeidsforholdOgErstattEksisterende(FaktaArbeidsforholdDto faktaArbeidsforhold) {
        var eksisterende = this.faktaArbeidsforholdListe.stream()
                .filter(fa -> fa.gjelderFor(faktaArbeidsforhold.getArbeidsgiver(), faktaArbeidsforhold.getArbeidsforholdRef()))
                .findFirst();
        eksisterende.ifPresent(this.faktaArbeidsforholdListe::remove);
        this.faktaArbeidsforholdListe.add(faktaArbeidsforhold);
    }

    private void leggTilFaktaArbeidsforholdOgKopierEksisterende(FaktaArbeidsforholdDto faktaArbeidsforhold) {
        var eksisterende = this.faktaArbeidsforholdListe.stream()
                .filter(fa -> fa.gjelderFor(faktaArbeidsforhold.getArbeidsgiver(), faktaArbeidsforhold.getArbeidsforholdRef()))
                .findFirst();
        eksisterende.ifPresentOrElse(kopier(faktaArbeidsforhold), () -> this.faktaArbeidsforholdListe.add(faktaArbeidsforhold));
    }

    private Consumer<FaktaArbeidsforholdDto> kopier(FaktaArbeidsforholdDto faktaArbeidsforhold) {
        return e -> {
            var faktaBuilder = FaktaArbeidsforholdDto.builder(e);
            kopierVurdering(faktaBuilder::medErTidsbegrenset, faktaArbeidsforhold.getErTidsbegrenset());
            kopierVurdering(faktaBuilder::medHarMottattYtelse, faktaArbeidsforhold.getHarMottattYtelse());
            kopierVurdering(faktaBuilder::medHarLønnsendringIBeregningsperioden, faktaArbeidsforhold.getHarLønnsendringIBeregningsperioden());
        };
    }

    private void kopierVurdering(Function<FaktaVurdering, FaktaArbeidsforholdDto.Builder> builderFunction, FaktaVurdering vurdering) {
        if (vurdering != null && vurdering.getVurdering() != null) {
            builderFunction.apply(vurdering);
        }
    }


    void setFaktaAktør(FaktaAktørDto faktaAktør) {
        this.faktaAktør = faktaAktør;
    }

    @Override
    public String toString() {
        return "FaktaAggregatDto{" +
                "faktaArbeidsforholdListe=" + faktaArbeidsforholdListe +
                ", faktaAktør=" + faktaAktør +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final FaktaAggregatDto kladd;

        private Builder() {
            kladd = new FaktaAggregatDto();
        }

        private Builder(FaktaAggregatDto faktaAggregatDto) {
            kladd = new FaktaAggregatDto(faktaAggregatDto);
        }

        static Builder oppdater(FaktaAggregatDto faktaAggregatDto) { // NOSONAR
            return new FaktaAggregatDto.Builder(faktaAggregatDto);
        }

        public FaktaAktørDto.Builder getFaktaAktørBuilder() {
            return kladd.getFaktaAktør().map(FaktaAktørDto.Builder::oppdater).orElse(FaktaAktørDto.builder());
        }

        public FaktaArbeidsforholdDto.Builder getFaktaArbeidsforholdBuilderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
            return kladd.faktaArbeidsforholdListe.stream().filter(f -> f.gjelderFor(arbeidsgiver, arbeidsforholdRef)).findFirst()
                    .map(FaktaArbeidsforholdDto.Builder::oppdater)
                    .orElse(new FaktaArbeidsforholdDto.Builder(arbeidsgiver, arbeidsforholdRef));
        }

        public Builder erstattEksisterendeEllerLeggTil(FaktaArbeidsforholdDto faktaArbeidsforhold) { // NOSONAR
            kladd.leggTilFaktaArbeidsforholdOgErstattEksisterende(faktaArbeidsforhold);
            return this;
        }

        public Builder kopierTilEksisterendeEllerLeggTil(FaktaArbeidsforholdDto faktaArbeidsforhold) { // NOSONAR
            kladd.leggTilFaktaArbeidsforholdOgKopierEksisterende(faktaArbeidsforhold);
            return this;
        }

        public Builder medFaktaAktør(FaktaAktørDto faktaAktør) { // NOSONAR
            kladd.setFaktaAktør(faktaAktør);
            return this;
        }

        public FaktaAggregatDto build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            if (manglerFakta()) {
                throw new IllegalStateException("Må ha satt enten faktaArbeidsforhold eller faktaAktør");
            }
        }

        // Brukes i fp-sak
        public boolean manglerFakta() {
            return kladd.faktaArbeidsforholdListe.isEmpty() && kladd.faktaAktør == null;
        }

    }
}
