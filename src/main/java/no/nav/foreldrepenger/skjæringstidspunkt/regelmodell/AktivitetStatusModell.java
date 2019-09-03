package no.nav.foreldrepenger.skjæringstidspunkt.regelmodell;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class AktivitetStatusModell {

    private LocalDate skjæringstidspunktForBeregning;
    private LocalDate skjæringstidspunktForOpptjening;
    private List<AktivPeriode> aktivePerioder = new ArrayList<>();
    private List<AktivitetStatus> aktivitetStatuser = new ArrayList<>();
    private List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatusListe = new ArrayList<>();

    public LocalDate getSkjæringstidspunktForBeregning() {
        return skjæringstidspunktForBeregning;
    }

    public LocalDate getSkjæringstidspunktForOpptjening() {
        return skjæringstidspunktForOpptjening;
    }

    public List<AktivitetStatus> getAktivitetStatuser() {
        return aktivitetStatuser;
    }

    public List<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagPrStatusListe() {
        return beregningsgrunnlagPrStatusListe;
    }

    public void setSkjæringstidspunktForBeregning(LocalDate skjæringstidspunktForBeregning) {
        this.skjæringstidspunktForBeregning = skjæringstidspunktForBeregning;
    }

    public void setSkjæringstidspunktForOpptjening(LocalDate skjæringstidspunktForOpptjening) {
        this.skjæringstidspunktForOpptjening = skjæringstidspunktForOpptjening;
    }

    public void leggTilAktivPeriode(AktivPeriode aktivPeriode) {
        if (Aktivitet.FRILANSINNTEKT.equals(aktivPeriode.getAktivitet())) {
            Optional<AktivPeriode> frilans = aktivePerioder.stream().filter(ap -> Aktivitet.FRILANSINNTEKT.equals(ap.getAktivitet())).findFirst();
            if (frilans.isPresent()) {
                frilans.get().oppdaterFra(aktivPeriode);
                return;
            }
        }
        if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivPeriode.getAktivitet()) && aktivPeriode.getArbeidsforhold() != null) {
            if (aktivPeriode.getArbeidsforhold().getOrgnr() != null) {
                leggTilEllerOppdater(aktivPeriode, this::getAktivPeriodeForArbeidsforholdIVirksomhet);
            } else if (aktivPeriode.getArbeidsforhold().getAktørId() != null) {
                leggTilEllerOppdater(aktivPeriode, this::getAktivPeriodeForArbeidsforholdHosPrivatperson);
            }
            return;
        }
        this.aktivePerioder.add(aktivPeriode);
    }

    private void leggTilEllerOppdater(AktivPeriode aktivPeriode, FinnAktivPeriode finnAktivPeriode) {
        Optional<AktivPeriode> aktivPeriodeForArbeidsforhold = finnAktivPeriode.finn(aktivPeriode);
        if (aktivPeriodeForArbeidsforhold.isPresent()) {
            aktivPeriodeForArbeidsforhold.get().oppdaterFra(aktivPeriode);
        } else {
            this.aktivePerioder.add(aktivPeriode);
        }
    }

    private Optional<AktivPeriode> getAktivPeriodeForArbeidsforholdIVirksomhet(AktivPeriode ap) {
        return this.getAktivePerioder().stream().filter(aktivPeriode -> aktivPeriode.getArbeidsforhold() != null &&
            Objects.equals(aktivPeriode.getArbeidsforhold().getOrgnr(), ap.getArbeidsforhold().getOrgnr()) &&
            Objects.equals(aktivPeriode.getArbeidsforhold().getArbeidsforholdId(), ap.getArbeidsforhold().getArbeidsforholdId())).findFirst();
    }

    private Optional<AktivPeriode> getAktivPeriodeForArbeidsforholdHosPrivatperson(AktivPeriode ap) {
        return this.getAktivePerioder().stream().filter(aktivPeriode -> aktivPeriode.getArbeidsforhold() != null &&
            Objects.equals(aktivPeriode.getArbeidsforhold().getAktørId(), ap.getArbeidsforhold().getAktørId())).findFirst();
    }


    public void leggTilAktivitetStatus(AktivitetStatus aktivitetStatus){
        this.aktivitetStatuser.add(aktivitetStatus);
    }

    public void leggTilBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus bgPrStatus){
        beregningsgrunnlagPrStatusListe.add(bgPrStatus);
    }

    public void justerAktiveperioder() {
        List<AktivPeriode> fjernes = aktivePerioder.stream().filter(ap -> !skjæringstidspunktForOpptjening.isAfter(ap.getPeriode().getFom())).collect(Collectors.toList());
        aktivePerioder.removeAll(fjernes);
        if (aktivePerioder.isEmpty()) {
            // TODO (Safir): her trengs litt mer kontekst info når det oppstår feil. Gi gjerne klassen her en toString og dump denne?
            throw new IllegalStateException("Valideringsfeil, bruker har ingen aktivitetsperioder, skjæringstidspunkt for beregning kan ikke fastsettes.");
        }
    }

    public LocalDate sisteAktivitetsdato() {
        // TODO (Safir): her trengs litt mer kontekst info når det oppstår feil. Gi gjerne klassen her en toString og dump denne?
        AktivPeriode sistePeriode = aktivePerioder.stream().min(this::slutterEtter)
            .orElseThrow(() -> new IllegalStateException("Fant ikke siste aktivitetsdato"));
        if (sistePeriode.inneholder(skjæringstidspunktForOpptjening)) {
            return skjæringstidspunktForOpptjening;
        }
        return sistePeriode.getPeriode().getTom();
    }

    private int slutterEtter(AktivPeriode ap1, AktivPeriode ap2) {
        return ap1.getPeriode().slutterEtter(ap2.getPeriode()) ? -1 : 1;
    }

    public List<AktivPeriode> getAktivePerioder() {
        return aktivePerioder;
    }

    @FunctionalInterface
    private interface FinnAktivPeriode {
        Optional<AktivPeriode> finn(AktivPeriode ap);
    }

}
