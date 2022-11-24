package no.nav.folketrygdloven.skjæringstidspunkt.regelmodell;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class AktivPeriode {

    private Aktivitet aktivitet;
    private Periode periode;
    private Arbeidsforhold arbeidsforhold;

    AktivPeriode() {
        // for jackson
    }

    public AktivPeriode(Aktivitet aktivitet, Periode periode, Arbeidsforhold arbeidsforhold) {
        this.arbeidsforhold = arbeidsforhold;
        this.aktivitet = aktivitet;
        this.periode = periode;
    }

    public Periode getPeriode() {
        return periode;
    }

    public void setVarighet(LocalDate tom) {
        this.periode = Periode.of(this.periode.getFom(), tom);
    }

    public boolean inneholder(LocalDate dato) {
        return periode.inneholder(dato);
    }

    public Aktivitet getAktivitet() {
        return aktivitet;
    }

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
            + "aktivitet=" + aktivitet
            + ", periode=" + periode
            + (arbeidsforhold != null ? ", arbeidsforhold=" + arbeidsforhold : "")
            + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AktivPeriode that = (AktivPeriode) o;
        return aktivitet == that.aktivitet &&
            Objects.equals(arbeidsforhold, that.arbeidsforhold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitet, arbeidsforhold);
    }

    public void oppdaterFra(AktivPeriode annen) {
        LocalDate fom = periode.getFom();
        if (fom.isAfter(annen.getPeriode().getFom())) {
            fom = annen.getPeriode().getFom();
        }
        LocalDate tom = periode.getTom();
        if (tom.isBefore(annen.getPeriode().getTom())) {
            tom = annen.getPeriode().getTom();
        }
        periode = Periode.of(fom, tom);
    }

    public static AktivPeriode forArbeidstakerHosVirksomhet(Periode periode, String orgnr, String arbeidsforholdId) {
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, arbeidsforholdId);
        return new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, periode, arbeidsforhold);
    }

	public static AktivPeriode forArbeidstakerHosPrivatperson(Periode periode, String aktørId) {
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(aktørId);
		return new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, periode, arbeidsforhold);
	}

    public static AktivPeriode forFrilanser(Periode periode) {
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        return new AktivPeriode(Aktivitet.FRILANSINNTEKT, periode, arbeidsforhold);
    }

    /**
     * Hvis aktiviteten er noe annet enn en åpenbar arbeidstakeraktivitet eller frilansaktivitet.
     * Aktivitetene VARTPENGER, VENTELØNN, ETTERLØNN, VIDERE_ETTERUTDANNING, SLUTTPAKKE og UTDANNINGSPERMISJON
     * skal behandles som om de var arbeidstakere som vi ikke nødvendigvis klarer å finne arbeidsgiver for,
     * derfor lages et anonymt arbeidsforhold for disse.
     */
    public static AktivPeriode forAndre(Aktivitet aktivitet, Periode periode) {
        List<Aktivitet> arbeidstakerUtenArbeidsforhold = Arrays.asList(
            Aktivitet.VENTELØNN_VARTPENGER, Aktivitet.ETTERLØNN_SLUTTPAKKE, Aktivitet.VIDERE_ETTERUTDANNING,
            Aktivitet.UTDANNINGSPERMISJON);
        if (arbeidstakerUtenArbeidsforhold.contains(aktivitet)) {
            Arbeidsforhold arbeidsforhold = Arbeidsforhold.anonymtArbeidsforhold(aktivitet);
            return new AktivPeriode(aktivitet, periode, arbeidsforhold);
        } else
            return new AktivPeriode(aktivitet, periode, null);
    }
}
