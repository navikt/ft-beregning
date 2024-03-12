package no.nav.folketrygdloven.kalkulator.modell.gradering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class AndelGradering {
    private AktivitetStatus aktivitetStatus;
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private List<Gradering> graderinger = new ArrayList<>();

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRefDto.nullRef();
    }

    public List<Gradering> getGraderinger() {
        return graderinger;
    }

    public boolean matcher(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (!Objects.equals(getAktivitetStatus(), andel.getAktivitetStatus())) {
            return false;
        }
        if (AktivitetStatus.ARBEIDSTAKER.equals(getAktivitetStatus())) {
            Optional<BGAndelArbeidsforholdDto> bgaOpt = andel.getBgAndelArbeidsforhold();
            Optional<Arbeidsgiver> arbeidsgiverOpt = bgaOpt.map(BGAndelArbeidsforholdDto::getArbeidsgiver);
            if (!arbeidsgiverOpt.isPresent()) {
                return false;
            }
            BGAndelArbeidsforholdDto bga = bgaOpt.get();
            return gjelderFor(arbeidsgiverOpt.get(), bga.getArbeidsforholdRef());
        } else {
            return true;
        }
    }

    public boolean gjelderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return Objects.equals(getArbeidsgiver(), arbeidsgiver)
            && getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AndelGradering)) {
            return false;
        }
        AndelGradering that = (AndelGradering) o;
        return Objects.equals(aktivitetStatus, that.aktivitetStatus)
            && Objects.equals(arbeidsgiver, that.arbeidsgiver)
            && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver, arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "<aktivitetStatus=" + aktivitetStatus
            + (arbeidsgiver == null ? "" : ", arbeidsgiver=" + arbeidsgiver)
            + (arbeidsforholdRef == null ? "" : ", arbeidsforholdRef=" + arbeidsforholdRef)
            + ", graderinger=" + graderinger
            + ">";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AndelGradering kladd;

        private Builder() {
            kladd = new AndelGradering();
        }

        public Builder medStatus(AktivitetStatus aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
            kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder leggTilGradering(Gradering gradering) {
            kladd.graderinger.add(gradering);
            return this;
        }

        public Builder medGradering(LocalDate fom, LocalDate tom, int arbeidstidsprosent) {
            return leggTilGradering(fom, tom, Aktivitetsgrad.fra(arbeidstidsprosent));
        }

        public Builder medGradering(LocalDate fom, LocalDate tom, Aktivitetsgrad arbeidstidsprosent) {
            return leggTilGradering(fom, tom, arbeidstidsprosent);
        }

        public Builder leggTilGradering(LocalDate fom, LocalDate tom, Aktivitetsgrad arbeidstidsprosent) {
            return leggTilGradering(new Gradering(Intervall.fraOgMedTilOgMed(fom, tom), arbeidstidsprosent));
        }

        public AndelGradering build() {
            return kladd;
        }

    }

    public static final class Gradering implements Comparable<Gradering> {

        /**
         * Perioden det gjelder.
         */
        private final Intervall periode;

        /**
         * En arbeidstaker kan kombinere foreldrepenger med deltidsarbeid.
         *
         * Når arbeidstakeren jobber deltid, utgjør foreldrepengene differansen mellom deltidsarbeidet og en 100 prosent stilling.
         * Det er ingen nedre eller øvre grense for hvor mye eller lite arbeidstakeren kan arbeide.
         *
         * Eksempel
         * Arbeidstaker A har en 100 % stilling og arbeider fem dager i uken. Arbeidstakeren ønsker å arbeide to dager i uken i
         * foreldrepengeperioden.
         * Arbeidstids- prosenten blir da 40 %.
         *
         * Arbeidstaker B har en 80 % stilling og arbeider fire dager i uken. Arbeidstakeren ønsker å arbeide to dager i uken i
         * foreldrepengeperioden.
         * Arbeidstidprosenten blir også her 40 %.
         *
         * @return prosentsats
         */
        private final Aktivitetsgrad arbeidstidProsent;

        public Gradering(LocalDate periodeFom, LocalDate periodeTom, Aktivitetsgrad arbeidstidProsent) {
            this.periode = Intervall.fraOgMedTilOgMed(periodeFom, periodeTom);
            this.arbeidstidProsent = Objects.requireNonNull(arbeidstidProsent);
        }

        public Gradering(Intervall periode, Aktivitetsgrad arbeidstidProsent) {
            this(periode.getFomDato(), periode.getTomDato(), arbeidstidProsent);
        }

        public Intervall getPeriode() {
            return periode;
        }

        public Aktivitetsgrad getArbeidstidProsent() {
            return arbeidstidProsent;
        }

        @Override
        public int compareTo(Gradering o) {
            return o == null ? 1 : this.getPeriode().compareTo(o.getPeriode());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj == null || !this.getClass().equals(obj.getClass())) {
                return false;
            }
            var other = (Gradering) obj;
            return Objects.equals(this.getPeriode(), other.getPeriode());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPeriode());
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "<periode=" + periode + ", arbeidstidsprosent=" + arbeidstidProsent + "%>";
        }
    }

}
