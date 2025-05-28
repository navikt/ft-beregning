package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig.ID)
public class AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig extends LeafSpecification<BeregningsgrunnlagPeriode> {
    public static final String ID = "FP_BR 29.8.4";
    public static final String BESKRIVELSE = "Avkort alle beregningsgrunnlagsandeler som ikke gjelder arbeidsforhold andelsmessig.";

    AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
	    var atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
	    var sumBeregningsgrunnlagArbeidsforhold = atfl == null ? BigDecimal.ZERO : atfl.getArbeidsforholdIkkeFrilans()
            .stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
	    var grenseverdi = grunnlag.getGrenseverdi();
        resultater.put("grenseverdi", grenseverdi);
	    var bGUtenArbeidsforholdTilFordeling = grenseverdi.subtract(sumBeregningsgrunnlagArbeidsforhold);

        // inntekt knyttet til frilans må fordeles først
        if (atfl != null) {
	        var frilansArbeidsforholdOpt = atfl.getFrilansArbeidsforhold();
            if (frilansArbeidsforholdOpt.isPresent()) {
	            var af = frilansArbeidsforholdOpt.get();
	            var bruttoBeregningsgrunnlagForAndelen = af.getBruttoInkludertNaturalytelsePrÅr()
                    .orElseThrow(() -> new IllegalStateException("Brutto er ikke satt for arbeidsforhold " + af.toString()));
                BigDecimal avkortetBrukersAndel;
                if (bruttoBeregningsgrunnlagForAndelen.compareTo(bGUtenArbeidsforholdTilFordeling) >= 0) {
                    avkortetBrukersAndel = bGUtenArbeidsforholdTilFordeling;
                    lagResultaterFrilanser(af, avkortetBrukersAndel, resultater);
                    grunnlag.getBeregningsgrunnlagPrStatus().stream()
                        .filter(bgps -> !bgps.erArbeidstakerEllerFrilanser())
                        .forEach(bgp -> lagResultaterUtenArbeidsforhold(bgp, BigDecimal.ZERO, resultater));
                    return beregnet(resultater);
                } else {
                    avkortetBrukersAndel = bruttoBeregningsgrunnlagForAndelen;
                    bGUtenArbeidsforholdTilFordeling = bGUtenArbeidsforholdTilFordeling.subtract(avkortetBrukersAndel);
                    lagResultaterFrilanser(af, avkortetBrukersAndel, resultater);
                }
            }
        }

        // sortere etter avkorting prioritet for beregningsgrunnlag uten arbeidsforhold
	    var bgpsSorted = finnAlleBGUtenArbeidsForholdSorterte(grunnlag);
	    var bgpsIter = bgpsSorted.iterator();
        while (bgpsIter.hasNext()) {
	        var bgps = bgpsIter.next();
	        var bruttoBeregningsgrunnlagForAndelen = bgps.getBruttoInkludertNaturalytelsePrÅr();
            BigDecimal avkortetBrukersAndel;
            if (bruttoBeregningsgrunnlagForAndelen.compareTo(bGUtenArbeidsforholdTilFordeling) >= 0) {
                avkortetBrukersAndel = bGUtenArbeidsforholdTilFordeling;
                lagResultaterUtenArbeidsforhold(bgps, avkortetBrukersAndel, resultater);
                bgpsIter.forEachRemaining(bgp -> lagResultaterUtenArbeidsforhold(bgp, BigDecimal.ZERO, resultater));
                return beregnet(resultater);
            } else {
                avkortetBrukersAndel = bruttoBeregningsgrunnlagForAndelen;
                bGUtenArbeidsforholdTilFordeling = bGUtenArbeidsforholdTilFordeling.subtract(avkortetBrukersAndel);
                lagResultaterUtenArbeidsforhold(bgps, avkortetBrukersAndel, resultater);
            }
        }
        return beregnet(resultater);
    }

    private List<BeregningsgrunnlagPrStatus> finnAlleBGUtenArbeidsForholdSorterte(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .filter(bgps -> !bgps.erArbeidstakerEllerFrilanser())
            .sorted(Comparator.comparingInt(bgps -> bgps.getAktivitetStatus().getAvkortingPrioritet()))
            .toList();
    }

    private void lagResultaterFrilanser(BeregningsgrunnlagPrArbeidsforhold af, BigDecimal avkortetBrukersAndel, Map<String, Object> resultater) {
        BeregningsgrunnlagPrArbeidsforhold.builder(af).medAndelsmessigFørGraderingPrAar(avkortetBrukersAndel).build();
        resultater.put("avkortetPrÅr.arbeidsforhold." + af.getArbeidsgiverId(), af.getAvkortetBrukersAndelPrÅr());
    }

    private void lagResultaterUtenArbeidsforhold(BeregningsgrunnlagPrStatus bgps, BigDecimal avkortetBrukersAndel, Map<String, Object> resultater) {
        BeregningsgrunnlagPrStatus.builder(bgps).medAndelsmessigFørGraderingPrAar(avkortetBrukersAndel).build();
        resultater.put("avkortetPrÅr.status." + bgps.getAktivitetStatus().name(), bgps.getAvkortetPrÅr());
    }
}
