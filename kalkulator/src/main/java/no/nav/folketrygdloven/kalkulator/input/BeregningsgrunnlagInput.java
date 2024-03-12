package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto.OpptjeningPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * Inputstruktur for beregningsgrunnlag tjenester.
 */
public class BeregningsgrunnlagInput {

    /**
     * Data som referer behandlingen beregningsgrunnlag inngår i.
     */
    private KoblingReferanse koblingReferanse;

    /**
     * Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen. Settes på av modulen.
     */
    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag;

    /**
     * Refusjonsperioder pr mottattt krav pr Arbeidsgiver
     */
    private List<KravperioderPrArbeidsforholdDto> kravPrArbeidsgiver;

    /**
     * IAY grunnlag benyttet av beregningsgrunnlag. Merk kan bli modifisert av innhenting av inntekter for beregning, sammenligning.
     */
    private final InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    /**
     * Aktiviteter til grunnlag for opptjening.
     */
    private final OpptjeningAktiviteterDto opptjeningAktiviteter;

    private final YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag;

    private List<Intervall> forlengelseperioder;


    private Map<String, Boolean> toggles = new HashMap<>();

    private Map<String, Object> konfigverdier = new HashMap<>();

    public BeregningsgrunnlagInput(KoblingReferanse koblingReferanse,
                                   InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                   OpptjeningAktiviteterDto opptjeningAktiviteter,
                                   List<KravperioderPrArbeidsforholdDto> kravPrArbeidsgiver,
                                   YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        this.koblingReferanse = Objects.requireNonNull(koblingReferanse, "koblingReferanse");
        this.iayGrunnlag = iayGrunnlag;
        this.opptjeningAktiviteter = opptjeningAktiviteter;
        this.kravPrArbeidsgiver = kravPrArbeidsgiver;
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
    }

    protected BeregningsgrunnlagInput(BeregningsgrunnlagInput input) {
        this(input.getKoblingReferanse(), input.getIayGrunnlag(), input.getOpptjeningAktiviteter(), input.getKravPrArbeidsgiver(), input.getYtelsespesifiktGrunnlag());
        this.beregningsgrunnlagGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        this.toggles = input.getToggles();
        this.konfigverdier = input.getKonfigverdier();
        this.forlengelseperioder = input.getForlengelseperioder();
    }

    public Map<String, Boolean> getToggles() {
        return toggles;
    }

    public void leggTilToggle(String feature, Boolean isEnabled) {
        toggles.put(feature, isEnabled);
    }


    public void leggTilKonfigverdi(String konfig, Object verdi) {
        konfigverdier.put(konfig, verdi);
    }

    public Map<String, Object> getKonfigverdier() {
        return konfigverdier;
    }

    public Object getKonfigVerdi(String konfig) {
        return konfigverdier.get(konfig);
    }

    public boolean isEnabled(String feature, boolean defaultValue) {
        return toggles.getOrDefault(feature, defaultValue);
    }

    public List<Intervall> getForlengelseperioder() {
        return forlengelseperioder == null ? Collections.emptyList() : forlengelseperioder;
    }

    public void setForlengelseperioder(List<Intervall> forlengelseperioder) {
        this.forlengelseperioder = forlengelseperioder;
    }

    public void setToggles(Map<String, Boolean> toggles) {
        this.toggles = toggles;
    }

    public KoblingReferanse getKoblingReferanse() {
        return koblingReferanse;
    }

    public Long getKoblingId() {
        return koblingReferanse.getKoblingId();
    }

    public BeregningsgrunnlagGrunnlagDto getBeregningsgrunnlagGrunnlag() {
        return beregningsgrunnlagGrunnlag;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlagGrunnlag == null ? null : beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return koblingReferanse.getFagsakYtelseType();
    }

    public InntektArbeidYtelseGrunnlagDto getIayGrunnlag() {
        return iayGrunnlag;
    }

    public Collection<InntektsmeldingDto> getInntektsmeldinger() {
        LocalDate skjæringstidspunktOpptjening = getSkjæringstidspunktOpptjening();
        if (skjæringstidspunktOpptjening == null) return Collections.emptyList();
        return new InntektsmeldingFilter(iayGrunnlag).hentInntektsmeldingerBeregning(skjæringstidspunktOpptjening);
    }

    public OpptjeningAktiviteterDto getOpptjeningAktiviteter() {
        return opptjeningAktiviteter;
    }

    private boolean periodefilter(LocalDate skjæringstidspunktOpptjening, OpptjeningPeriodeDto p) {
        if (getOpptjeningAktiviteter().erMidlertidigInaktiv()) {
            return !p.getPeriode().getFomDato().isAfter(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktOpptjening)) ||
                    p.getPeriode().getFomDato().isEqual(skjæringstidspunktOpptjening);
        }
        return !p.getPeriode().getFomDato().isAfter(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktOpptjening));
    }

    public Collection<OpptjeningPeriodeDto> getOpptjeningAktiviteterForBeregning() {
        LocalDate skjæringstidspunktOpptjening = getSkjæringstidspunktOpptjening();
        if (skjæringstidspunktOpptjening == null) return Collections.emptyList();
        var aktivitetFilter = new OpptjeningsaktiviteterPerYtelse(getFagsakYtelseType());
        return opptjeningAktiviteter.getOpptjeningPerioder()
                .stream()
                .filter(p -> periodefilter(skjæringstidspunktOpptjening, p))
                .filter(p -> aktivitetFilter.erRelevantAktivitet(p.getOpptjeningAktivitetType()))
                .collect(Collectors.toList());
    }

    public Skjæringstidspunkt getSkjæringstidspunkt() {
        return koblingReferanse.getSkjæringstidspunkt();
    }

    public LocalDate getSkjæringstidspunktForBeregning() {
        return koblingReferanse.getSkjæringstidspunktBeregning();
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return getSkjæringstidspunkt().getSkjæringstidspunktOpptjening();
    }

    public List<KravperioderPrArbeidsforholdDto> getKravPrArbeidsgiver() {
        return kravPrArbeidsgiver;
    }


    /**
     * Sjekk fagsakytelsetype før denne kalles.
     */
    @SuppressWarnings("unchecked")
    public <V extends YtelsespesifiktGrunnlag> V getYtelsespesifiktGrunnlag() {
        return (V) ytelsespesifiktGrunnlag;
    }

    public BeregningsgrunnlagInput medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new BeregningsgrunnlagInput(this);
        newInput.beregningsgrunnlagGrunnlag = grunnlag;
        newInput = grunnlag.getBeregningsgrunnlagHvisFinnes()
                .map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
                .map(newInput::medSkjæringstidspunktForBeregning)
                .orElse(newInput);
        return newInput;
    }


    /**
     * Overstyrer behandlingreferanse, eks for å få ny skjæringstidspunkt fra beregningsgrunnlag fra tidligere.
     */
    public BeregningsgrunnlagInput medBehandlingReferanse(KoblingReferanse ref) {
        var newInput = new BeregningsgrunnlagInput(this);
        newInput.koblingReferanse = Objects.requireNonNull(ref, "behandlingReferanse");
        return newInput;
    }

    private BeregningsgrunnlagInput medSkjæringstidspunktForBeregning(LocalDate skjæringstidspunkt) {
        var newInput = new BeregningsgrunnlagInput(this);
        var nyttSkjæringstidspunkt = Skjæringstidspunkt.builder(this.koblingReferanse.getSkjæringstidspunkt()).medSkjæringstidspunktBeregning(skjæringstidspunkt).build();
        newInput.koblingReferanse = this.koblingReferanse.medSkjæringstidspunkt(nyttSkjæringstidspunkt);
        return newInput;
    }

    @Override
    public String toString() {
        return "BeregningsgrunnlagInput{" +
                "behandlingReferanse=" + koblingReferanse +
                ", beregningsgrunnlagGrunnlag=" + beregningsgrunnlagGrunnlag +
                ", refusjonskravPerioder=" + kravPrArbeidsgiver +
                ", iayGrunnlag=" + iayGrunnlag +
                ", opptjeningAktiviteter=" + opptjeningAktiviteter +
                ", ytelsespesifiktGrunnlag=" + ytelsespesifiktGrunnlag +
                '}';
    }

}
