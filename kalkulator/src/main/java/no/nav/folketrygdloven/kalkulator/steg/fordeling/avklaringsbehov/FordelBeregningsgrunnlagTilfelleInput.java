package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * Input for å utlede tilfelle for fordel beregningsgrunnlag
 */
public class FordelBeregningsgrunnlagTilfelleInput {

    private final List<Intervall> forlengelseperioder;
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private AktivitetGradering aktivitetGradering;
    private Collection<InntektsmeldingDto> inntektsmeldinger;
    private FagsakYtelseType fagsakYtelseType;


    public FordelBeregningsgrunnlagTilfelleInput(BeregningsgrunnlagDto beregningsgrunnlag,
                                                 AktivitetGradering aktivitetGradering,
                                                 Collection<InntektsmeldingDto> inntektsmeldinger, List<Intervall> forlengelseperioder,
                                                 FagsakYtelseType fagsakYtelseType) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.aktivitetGradering = aktivitetGradering;
        this.inntektsmeldinger = inntektsmeldinger;
        this.forlengelseperioder = forlengelseperioder;
        this.fagsakYtelseType = fagsakYtelseType;
        verifyStateForBuild();
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public AktivitetGradering getAktivitetGradering() {
        return aktivitetGradering;
    }

    public Collection<InntektsmeldingDto> getInntektsmeldinger() {
        return Collections.unmodifiableCollection(inntektsmeldinger);
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public List<Intervall> getForlengelseperioder() {
        return forlengelseperioder;
    }

    @Override
    public String toString() {
        return "FordelBeregningsgrunnlagTilfelleInput{" +
                "beregningsgrunnlag=" + beregningsgrunnlag +
                ", aktivitetGradering=" + aktivitetGradering +
                ", inntektsmeldinger=" + inntektsmeldinger +
                ", fagsakYtelseType=" + fagsakYtelseType +
            '}';
    }

    public static FordelBeregningsgrunnlagTilfelleInput fraBeregningsgrunnlagRestInput(BeregningsgrunnlagGUIInput input) {
        var aktivitetGradering = input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag ?
                ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).getAktivitetGradering() : AktivitetGradering.INGEN_GRADERING;
        var inntektsmeldinger = input.getInntektsmeldinger();
        return new FordelBeregningsgrunnlagTilfelleInput(input.getBeregningsgrunnlag(), aktivitetGradering, inntektsmeldinger,
            input.getForlengelseperioder(), input.getFagsakYtelseType());
    }

    private void verifyStateForBuild() {
        Objects.requireNonNull(beregningsgrunnlag, "Beregningsgrunnlag");
    }

}
