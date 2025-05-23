package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBeregnetPrÅr.ID)
class FastsettBeregnetPrÅr extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.5";
    static final String BESKRIVELSE = "Beregnet årsinntekt er sum av alle rapporterte inntekter";

    FastsettBeregnetPrÅr() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        var hjemmel = settHjemmelForATFL(grunnlag.getBeregningsgrunnlag(), bgps);

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("beregnetPrÅr", bgps.getBeregnetPrÅr());
        resultater.put("samletNaturalytelseBortfaltMinusTilkommetPrÅr", bgps.samletNaturalytelseBortfaltMinusTilkommetPrÅr());
        resultater.put("hjemmel", hjemmel);
        return beregnet(resultater);
    }

    private BeregningsgrunnlagHjemmel settHjemmelForATFL(Beregningsgrunnlag grunnlag, BeregningsgrunnlagPrStatus bgps) {
        var status = grunnlag.getAktivitetStatus(AktivitetStatus.ATFL);
        var kombinasjon = status.getAktivitetStatus().equals(AktivitetStatus.ATFL_SN);
        var arbeidstaker = !bgps.getArbeidsforholdIkkeFrilans().isEmpty();
        var frilans = bgps.getFrilansArbeidsforhold().isPresent();
        BeregningsgrunnlagHjemmel hjemmel;
        if (arbeidstaker) {
            hjemmel = finnHjemmelForArbeidstaker(grunnlag.getYtelsesSpesifiktGrunnlag(), kombinasjon, frilans);
        } else if (frilans) {
            hjemmel = settHjemmelForFrilansIkkeArbeid(grunnlag.getYtelsesSpesifiktGrunnlag(), kombinasjon);
        } else {
            throw new IllegalStateException("ATFL-andel mangler både arbeidsforhold og frilansaktivitet");
        }
        status.setHjemmel(hjemmel);
        return hjemmel;
    }

    private BeregningsgrunnlagHjemmel settHjemmelForFrilansIkkeArbeid(YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag,
                                                                      boolean kombinasjon) {
        if (ytelsesSpesifiktGrunnlag.erKap9Ytelse()) {
            return finnHjemmelForFrilansK9(kombinasjon);
        }
        return finnHjemmelForFrilansK14(kombinasjon);
    }

    private BeregningsgrunnlagHjemmel finnHjemmelForFrilansK14(boolean kombinasjon) {
        if (kombinasjon) {
            return BeregningsgrunnlagHjemmel.K14_HJEMMEL_FRILANSER_OG_SELVSTENDIG;
        }
        return BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER;
    }

    private BeregningsgrunnlagHjemmel finnHjemmelForArbeidstaker(YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag,
                                                                 boolean kombinasjon,
                                                                 boolean frilans) {
        if (ytelsesSpesifiktGrunnlag.erKap9Ytelse()) {
            return finnHjemmelForArbeidstakerK9(ytelsesSpesifiktGrunnlag, kombinasjon, frilans);
        }
        return finnHjemmelForArbeidstakerK14(kombinasjon, frilans);
    }

    private BeregningsgrunnlagHjemmel finnHjemmelForArbeidstakerK14(boolean kombinasjon, boolean frilans) {
        if (kombinasjon) {
            return frilans ? BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER_OG_SELVSTENDIG : BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG;
        }
        return frilans ? BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER : BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER;
    }

    private BeregningsgrunnlagHjemmel finnHjemmelForArbeidstakerK9(YtelsesSpesifiktGrunnlag grunnlag,
                                                                   boolean kombinasjon,
                                                                   boolean frilans) {
        if (kombinasjon) {
            return (frilans ? BeregningsgrunnlagHjemmel.K9_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER_OG_SELVSTENDIG : BeregningsgrunnlagHjemmel.K9_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG);
        }
        if (frilans) {
            return BeregningsgrunnlagHjemmel.K9_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER;
        }
        if (grunnlag instanceof  OmsorgspengerGrunnlag ompGrunnlag && !ompGrunnlag.omfattesAvKap9Paragraf9()) {
            return BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_REFUSJON;
        }
        return BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_MED_AVVIKSVURDERING;
    }

    private BeregningsgrunnlagHjemmel finnHjemmelForFrilansK9(boolean kombinasjon) {
        if (kombinasjon) {
            return BeregningsgrunnlagHjemmel.K9_HJEMMEL_FRILANSER_OG_SELVSTENDIG;
        }
        return BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_FRILANSER;
    }

}
