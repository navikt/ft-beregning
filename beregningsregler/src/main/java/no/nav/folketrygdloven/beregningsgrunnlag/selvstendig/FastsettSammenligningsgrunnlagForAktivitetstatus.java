package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSammenligningsgrunnlagForAktivitetstatus.ID)
public class FastsettSammenligningsgrunnlagForAktivitetstatus extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.4";
    static final String BESKRIVELSE = "Fastsett sammenligningsgrunnlag og beregn avvik i henhold til § 8-35 tredje ledd";

	private final AktivitetStatus aktivitetStatus;

    public FastsettSammenligningsgrunnlagForAktivitetstatus(AktivitetStatus aktivitetStatus) {
        super(ID, BESKRIVELSE);
	    if (!AktivitetStatus.SN.equals(aktivitetStatus) && !AktivitetStatus.BA.equals(aktivitetStatus)) {
		    throw new IllegalArgumentException("Kan ikke beregne avvik for aktivitetstatus " + aktivitetStatus);
	    }
		this.aktivitetStatus = aktivitetStatus;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var oppgittInntekt = FinnRapporterteInntekter.finnImplementasjonForStatus(aktivitetStatus).finnRapportertInntekt(grunnlag)
		        .orElseThrow(() -> new IllegalStateException("Fant ikke oppgitt månedsinntekt ved varig endret inntekt"));

        var sammenligningsGrunnlag = opprettSammenligningsgrunnlag(grunnlag, oppgittInntekt);
	    beregnOgFastsettAvvik(grunnlag, sammenligningsGrunnlag);
	    Beregningsgrunnlag.builder(grunnlag.getBeregningsgrunnlag()).leggTilSammenligningsgrunnlagPrStatus(sammenligningsGrunnlag).build();

        var resultater = gjørRegelsporing(grunnlag, sammenligningsGrunnlag, oppgittInntekt);
        return beregnet(resultater);
    }

    private void beregnOgFastsettAvvik(BeregningsgrunnlagPeriode grunnlag, SammenligningsGrunnlag sammenligningsGrunnlag) {
        var pgiSnitt = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus).getGjennomsnittligPGI();
        var sammenligning = sammenligningsGrunnlag.getRapportertPrÅr();
        var diff = pgiSnitt.subtract(sammenligning).abs();

        var avvikProsent = pgiSnitt.compareTo(BigDecimal.ZERO) != 0
            ? diff.divide(pgiSnitt, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.valueOf(100);

        sammenligningsGrunnlag.setAvvikProsent(avvikProsent);
    }

    private SammenligningsGrunnlag opprettSammenligningsgrunnlag(BeregningsgrunnlagPeriode grunnlag, Periodeinntekt oppgittInntekt) {
        var bgATFL = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        var aapStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
	    var dpStatus = grunnlag.getBeregningsgrunnlagFraDagpenger();

        var bruttoAAP = aapStatus == null ? BigDecimal.ZERO : aapStatus.getBeregnetPrÅr();
        var bruttoDP = dpStatus.map(BeregningsgrunnlagPrStatus::getBeregnetPrÅr).orElse(BigDecimal.ZERO);

        var bruttoATFL = bgATFL != null ? bgATFL.getBruttoInkludertNaturalytelsePrÅr() : BigDecimal.ZERO;
        var antallPerioderPrÅr = oppgittInntekt.getInntektPeriodeType().getAntallPrÅr();
        var oppgittÅrsInntekt = oppgittInntekt.getInntekt().multiply(antallPerioderPrÅr);

        var sammenligningInntekt = oppgittÅrsInntekt.add(bruttoATFL).add(bruttoAAP).add(bruttoDP);
        var sammenligningsperiode = Periode.of(oppgittInntekt.getFom(), oppgittInntekt.getFom().plusMonths(1).minusDays(1));

        return SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(sammenligningsperiode)
            .medRapportertPrÅr(sammenligningInntekt)
	        .medSammenligningstype(aktivitetStatus.erSelvstendigNæringsdrivende() ? SammenligningGrunnlagType.SN : SammenligningGrunnlagType.MIDLERTIDIG_INAKTIV)
            .build();
    }

    private Map<String, Object> gjørRegelsporing(BeregningsgrunnlagPeriode grunnlag, SammenligningsGrunnlag sammenligningsGrunnlag, Periodeinntekt oppgittInntekt) {
        Map<String, Object> resultater = new LinkedHashMap<>();
        var bgAAP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        var bgDP = grunnlag.getBeregningsgrunnlagFraDagpenger();
        var bgATFL = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        var bgForStatus = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);

        var bruttoString = "brutto";
        if (bgATFL != null) {
            var status = "ATFL";
            resultater.put(bruttoString + status, bgATFL.getBeregnetPrÅr());
        }
        if (bgAAP != null) {
            var status = "AAP";
            resultater.put(bruttoString + status, bgAAP.getBeregnetPrÅr());
        }
        if (bgDP.isPresent()) {
            var status = "DP";
            resultater.put(bruttoString + status, bgDP.get().getBeregnetPrÅr());
        }
        resultater.put("gjennomsnittligPGI", bgForStatus.getGjennomsnittligPGI());
        resultater.put("inntektEtterVarigEndringPrÅr", oppgittInntekt.getInntekt());
        resultater.put("sammenligningsperiode", sammenligningsGrunnlag.getSammenligningsperiode());
        resultater.put("sammenligningsgrunnlagRapportertPrÅr", sammenligningsGrunnlag.getRapportertPrÅr());
        resultater.put("avvikProsent", sammenligningsGrunnlag.getAvvikProsent());

        return resultater;
    }
}
