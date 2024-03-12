package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov.AvklaringsbehovDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdReferanseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class BeregningsgrunnlagGUIInput {

    /** Data som referer behandlingen beregningsgrunnlag inngår i. */
    private KoblingReferanse koblingReferanse;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag;

    /** Avklaringsbehov som finnes på grunnlaget */
    private List<AvklaringsbehovDto> avklaringsbehov;

    /** Grunnlag fra fordelsteget. Brukes i visning av automatisk fordeling og utledning av andeler som skal redigeres */
    private BeregningsgrunnlagGrunnlagDto fordelBeregningsgrunnlagGrunnlag;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen i original behandling. Settes på av modulen. */
    private List<BeregningsgrunnlagGrunnlagDto> beregningsgrunnlagGrunnlagFraForrigeBehandling = new ArrayList<>();

    /** Datoer for innsending og oppstart av refusjon for alle arbeidsgivere */
    private List<KravperioderPrArbeidsforholdDto> kravperioderPrArbeidsgiver;

    /** IAY grunnlag benyttet av beregningsgrunnlag. Merk kan bli modifisert av innhenting av inntekter for beregning, sammenligning. */
    private InntektArbeidYtelseGrunnlagDto iayGrunnlag;


    private final YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag;

    private List<Intervall> forlengelseperioder;

    private Map<String, Boolean> toggles = new HashMap<>();

    public BeregningsgrunnlagGUIInput(KoblingReferanse koblingReferanse,
                                      InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                      List<KravperioderPrArbeidsforholdDto> kravperioderPrArbeidsgiver, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        this.koblingReferanse = Objects.requireNonNull(koblingReferanse, "behandlingReferanse");
        this.iayGrunnlag = iayGrunnlag;
        this.kravperioderPrArbeidsgiver = kravperioderPrArbeidsgiver;
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
    }

    private BeregningsgrunnlagGUIInput(BeregningsgrunnlagGUIInput input) {
        this(input.getKoblingReferanse(), input.getIayGrunnlag(),
                input.getKravperioderPrArbeidsgiver(), input.getYtelsespesifiktGrunnlag());
        this.beregningsgrunnlagGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        this.fordelBeregningsgrunnlagGrunnlag = input.fordelBeregningsgrunnlagGrunnlag;
        this.beregningsgrunnlagGrunnlagFraForrigeBehandling = input.beregningsgrunnlagGrunnlagFraForrigeBehandling;
        this.toggles = input.getToggles();
        this.forlengelseperioder = input.getForlengelseperioder();
    }

    public List<Intervall> getForlengelseperioder() {
        return forlengelseperioder;
    }

    public void setForlengelseperioder(List<Intervall> forlengelseperioder) {
        this.forlengelseperioder = forlengelseperioder;
    }

    public List<AvklaringsbehovDto> getAvklaringsbehov() {
        return avklaringsbehov == null ? Collections.emptyList() : avklaringsbehov;
    }

    public KoblingReferanse getKoblingReferanse() {
        return koblingReferanse;
    }

    public BeregningsgrunnlagGrunnlagDto getBeregningsgrunnlagGrunnlag() {
        return beregningsgrunnlagGrunnlag;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlagGrunnlag == null ? null : beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
    }

    public List<BeregningsgrunnlagGrunnlagDto> getBeregningsgrunnlagGrunnlagFraForrigeBehandling() {
        return beregningsgrunnlagGrunnlagFraForrigeBehandling;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return koblingReferanse.getFagsakYtelseType();
    }

    public InntektArbeidYtelseGrunnlagDto getIayGrunnlag() {
        return iayGrunnlag;
    }

    public Collection<InntektsmeldingDto> getInntektsmeldinger() {
        LocalDate skjæringstidspunktOpptjening = getSkjæringstidspunktOpptjening();
        if(skjæringstidspunktOpptjening == null) return Collections.emptyList();
        return new InntektsmeldingFilter(iayGrunnlag).hentInntektsmeldingerBeregning(skjæringstidspunktOpptjening);
    }

    public Optional<FaktaAggregatDto> getFaktaAggregat() {
        return getBeregningsgrunnlagGrunnlag().getFaktaAggregat();
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

    public List<KravperioderPrArbeidsforholdDto> getKravperioderPrArbeidsgiver() {
        return kravperioderPrArbeidsgiver;
    }

    /** Sjekk fagsakytelsetype før denne kalles. */
    @SuppressWarnings("unchecked")
    public <V extends YtelsespesifiktGrunnlag> V getYtelsespesifiktGrunnlag() {
        return (V) ytelsespesifiktGrunnlag;
    }

    /**
     * Oppdaterer iaygrunnlag med informasjon om arbeidsgiver og referanser for visning
     *
     * @param referanser Referanser
     */
    public void oppdaterArbeidsgiverinformasjon(Set<ArbeidsforholdReferanseDto> referanser) {
        InntektArbeidYtelseGrunnlagDtoBuilder oppdatere = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(iayGrunnlag);
        ArbeidsforholdInformasjonDtoBuilder arbeidsforholdInformasjonDtoBuilder = ArbeidsforholdInformasjonDtoBuilder.builder(Optional.ofNullable(oppdatere.getInformasjon()));
        referanser.forEach(arbeidsforholdInformasjonDtoBuilder::leggTilNyReferanse);
        oppdatere.medInformasjon(arbeidsforholdInformasjonDtoBuilder.build());
        this.iayGrunnlag = oppdatere.build();
    }

    public BeregningsgrunnlagGUIInput medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new BeregningsgrunnlagGUIInput(this);
        newInput.beregningsgrunnlagGrunnlag = grunnlag;
        newInput = grunnlag.getBeregningsgrunnlagHvisFinnes()
            .map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
            .map(newInput::medSkjæringstidspunktForBeregning)
            .orElse(newInput);
        return newInput;
    }

    public BeregningsgrunnlagGUIInput medAvklaringsbehov(List<AvklaringsbehovDto> avklaringsbehov) {
        if (this.avklaringsbehov != null) {
            throw new IllegalStateException("Listen med avklaringsbehov kan ikke endres.");
        }
        this.avklaringsbehov = Collections.unmodifiableList(avklaringsbehov);
        return this;
    }

    public void leggTilToggle(String feature, Boolean isEnabled) {
        toggles.put(feature, isEnabled);
    }

    public Map<String, Boolean> getToggles() {
        return toggles;
    }

    public boolean isEnabled(String feature, boolean defaultValue) {
        return toggles.getOrDefault(feature, defaultValue);
    }

    public BeregningsgrunnlagGUIInput medBeregningsgrunnlagGrunnlagFraForrigeBehandling(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new BeregningsgrunnlagGUIInput(this);
        newInput.beregningsgrunnlagGrunnlagFraForrigeBehandling = new ArrayList<>(List.of(grunnlag));
        return newInput;
    }

    public BeregningsgrunnlagGUIInput medBeregningsgrunnlagGrunnlagFraForrigeBehandling(List<BeregningsgrunnlagGrunnlagDto> grunnlag) {
        var newInput = new BeregningsgrunnlagGUIInput(this);
        newInput.beregningsgrunnlagGrunnlagFraForrigeBehandling = grunnlag;
        return newInput;
    }

    private BeregningsgrunnlagGUIInput medSkjæringstidspunktForBeregning(LocalDate skjæringstidspunkt) {
        var newInput = new BeregningsgrunnlagGUIInput(this);
        var nyttSkjæringstidspunkt = Skjæringstidspunkt.builder(this.koblingReferanse.getSkjæringstidspunkt()).medSkjæringstidspunktBeregning(skjæringstidspunkt).build();
        newInput.koblingReferanse = this.koblingReferanse.medSkjæringstidspunkt(nyttSkjæringstidspunkt);
        return newInput;
    }

}
