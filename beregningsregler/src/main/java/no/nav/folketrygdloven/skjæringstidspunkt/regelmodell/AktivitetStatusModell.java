package no.nav.folketrygdloven.skjæringstidspunkt.regelmodell;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.FeatureToggles;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Toggle;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class AktivitetStatusModell {

	private LocalDate skjæringstidspunktForBeregning;
	protected LocalDate skjæringstidspunktForOpptjening;
	protected List<AktivPeriode> aktivePerioder = new ArrayList<>();
	private List<AktivitetStatus> aktivitetStatuser = new ArrayList<>();
	private List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatusListe = new ArrayList<>();
	private Inntektsgrunnlag inntektsgrunnlag;
	private FeatureToggles toggles = new FeatureToggles();

	public AktivitetStatusModell() {
	}

	public AktivitetStatusModell(AktivitetStatusModell kopi) {
		this.skjæringstidspunktForBeregning = kopi.skjæringstidspunktForBeregning;
		this.skjæringstidspunktForOpptjening = kopi.skjæringstidspunktForOpptjening;
		this.aktivePerioder = kopi.aktivePerioder;
		this.aktivitetStatuser = kopi.aktivitetStatuser;
		this.beregningsgrunnlagPrStatusListe = kopi.beregningsgrunnlagPrStatusListe;
		this.inntektsgrunnlag = kopi.inntektsgrunnlag;
		this.toggles = new FeatureToggles(kopi.toggles);
	}

	public LocalDate getBeregningstidspunkt() {
		return getSkjæringstidspunktForBeregning().minusDays(1);
	}

	public LocalDate getSkjæringstidspunktForBeregning() {
		return skjæringstidspunktForBeregning;
	}

	public LocalDate getSkjæringstidspunktForOpptjening() {
		return skjæringstidspunktForOpptjening;
	}

	/**
	 * Hent aktivitet statuser.  Listen er ikke muterbar, bruk mutatorer i stedet.
	 */
	public List<AktivitetStatus> getAktivitetStatuser() {
		return Collections.unmodifiableList(aktivitetStatuser);
	}

	/**
	 * Hent beregningsgrunnlag per status.  Listen er ikke muterbar, bruk mutatorer i stedet.
	 */
	public List<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagPrStatusListe() {
		return Collections.unmodifiableList(beregningsgrunnlagPrStatusListe);
	}

	public Inntektsgrunnlag getInntektsgrunnlag() {
		return inntektsgrunnlag;
	}

	public void setSkjæringstidspunktForBeregning(LocalDate skjæringstidspunktForBeregning) {
		this.skjæringstidspunktForBeregning = skjæringstidspunktForBeregning;
	}

	public void setSkjæringstidspunktForOpptjening(LocalDate skjæringstidspunktForOpptjening) {
		this.skjæringstidspunktForOpptjening = skjæringstidspunktForOpptjening;
	}

	public void setInntektsgrunnlag(Inntektsgrunnlag inntektsgrunnlag) {
		this.inntektsgrunnlag = inntektsgrunnlag;
	}

	public void leggTilEllerOppdaterAktivPeriode(AktivPeriode aktivPeriode) {
		if (Aktivitet.FRILANSINNTEKT.equals(aktivPeriode.getAktivitet())) {
			leggTilFrilansPeriode(aktivPeriode);
		} else if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivPeriode.getAktivitet()) && aktivPeriode.getArbeidsforhold() != null) {
			leggTilArbeidstakerPeriode(aktivPeriode);
		} else {
			leggTilEllerOppdater(aktivPeriode, this::getAktivPeriodeMedTypeUtenArbeidsforhold);
		}
	}

	public void leggTilAktivitetStatus(AktivitetStatus aktivitetStatus) {
		this.aktivitetStatuser.add(aktivitetStatus);
	}

	public void leggTilBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus bgPrStatus) {
		if (bgPrStatus.getAktivitetStatus().equals(ATFL)) {
			beregningsgrunnlagPrStatusListe.stream().filter(a -> a.getAktivitetStatus().equals(ATFL)).findFirst()
					.ifPresentOrElse(atflStatus -> atflStatus.leggTilArbeidsforhold(bgPrStatus.getArbeidsforholdList()),
							() -> beregningsgrunnlagPrStatusListe.add(bgPrStatus));
		} else {
			beregningsgrunnlagPrStatusListe.add(bgPrStatus);
		}
	}

	public void leggTilToggle(String feature, boolean value) {
		toggles.leggTilToggle(new Toggle(feature, value));
	}

	public FeatureToggles getToggles() {
		return toggles;
	}

	public LocalDate sisteAktivitetsdato() {
		return finnSisteAktivitetsdatoFraSistePeriode();
	}


	protected LocalDate finnSisteAktivitetsdatoFraSistePeriode() {
		AktivPeriode sistePeriode = aktivePerioder.stream().min(this::slutterEtter)
				.orElseThrow(() -> new IllegalStateException("Klarte ikke å finne siste avsluttede aktivitet"));
		if (sistePeriode.inneholder(skjæringstidspunktForOpptjening)) {
			return skjæringstidspunktForOpptjening;
		}
		return sistePeriode.getPeriode().getTom();
	}

	private void leggTilArbeidstakerPeriode(AktivPeriode aktivPeriode) {
		if (aktivPeriode.getArbeidsforhold().getOrgnr() != null) {
			leggTilEllerOppdater(aktivPeriode, this::getAktivPeriodeForArbeidsforholdIVirksomhet);
		} else if (aktivPeriode.getArbeidsforhold().getAktørId() != null) {
			leggTilEllerOppdater(aktivPeriode, this::getAktivPeriodeForArbeidsforholdHosPrivatperson);
		}
	}

	private void leggTilNyPeriode(AktivPeriode aktivPeriode) {
		this.aktivePerioder.add(aktivPeriode);
	}

	private void leggTilFrilansPeriode(AktivPeriode aktivPeriode) {
		Optional<AktivPeriode> frilans = aktivePerioder.stream().filter(ap -> Aktivitet.FRILANSINNTEKT.equals(ap.getAktivitet())).findFirst();
		frilans.ifPresentOrElse(aktivPeriode1 -> aktivPeriode1.oppdaterFra(aktivPeriode), () -> leggTilNyPeriode(aktivPeriode));
	}

	private void leggTilEllerOppdater(AktivPeriode aktivPeriode, FinnAktivPeriode finnAktivPeriode) {
		Optional<AktivPeriode> aktivPeriodeForArbeidsforhold = finnAktivPeriode.finn(aktivPeriode);
		if (aktivPeriodeForArbeidsforhold.isPresent()) {
			aktivPeriodeForArbeidsforhold.get().oppdaterFra(aktivPeriode);
		} else {
			leggTilNyPeriode(aktivPeriode);
		}
	}

	public void fjernAktivitetStatus(List<AktivitetStatus> statuser) {
		this.aktivitetStatuser.removeAll(statuser);
	}

	private Optional<AktivPeriode> getAktivPeriodeMedTypeUtenArbeidsforhold(AktivPeriode ap) {
		return this.getAktivePerioder()
				.stream()
				.filter(aktivPeriode -> aktivPeriode.getAktivitet().equals(ap.getAktivitet()) &&
						aktivPeriode.getArbeidsforhold() == null)
				.findFirst();
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


	private int slutterEtter(AktivPeriode ap1, AktivPeriode ap2) {
		return ap1.getPeriode().slutterEtter(ap2.getPeriode()) ? -1 : 1;
	}

	/**
	 * Hent aktive perioder.  Listen er ikke muterbar, bruk mutatorer i stedet.
	 */
	public List<AktivPeriode> getAktivePerioder() {
		return Collections.unmodifiableList(aktivePerioder);
	}

	@FunctionalInterface
	private interface FinnAktivPeriode {
		Optional<AktivPeriode> finn(AktivPeriode ap);
	}

}
