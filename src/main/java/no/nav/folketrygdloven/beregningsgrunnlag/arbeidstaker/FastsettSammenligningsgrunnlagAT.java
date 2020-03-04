package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.BevegeligeHelligdagerUtil;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSammenligningsgrunnlagAT.ID)
class FastsettSammenligningsgrunnlagAT extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 28.6";
    static final String BESKRIVELSE = "Sammenligningsgrunnlag for AT er sum av inntektene i sammenligningsperioden";

    FastsettSammenligningsgrunnlagAT() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if (grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT) == null) {
            Periode sammenligningsPeriode = lagSammenligningsPeriode(grunnlag.getInntektsgrunnlag(), grunnlag.getSkjæringstidspunkt(), grunnlag);
            BigDecimal sammenligningsgrunnlagInntekt = grunnlag.getInntektsgrunnlag().getSamletInntektISammenligningsperiodeAT(sammenligningsPeriode);
            SammenligningsGrunnlag sg = SammenligningsGrunnlag.builder()
                .medSammenligningsperiode(sammenligningsPeriode)
                .medRapportertPrÅr(sammenligningsgrunnlagInntekt)
                .build();
            Beregningsgrunnlag.builder(grunnlag.getBeregningsgrunnlag()).medSammenligningsgrunnlagPrStatus(AktivitetStatus.AT, sg).build();
        }
        Map<String, Object> resultater = new HashMap<>();
        SammenligningsGrunnlag sammenligningsGrunnlag = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT);
        resultater.put("sammenligningsperiode", sammenligningsGrunnlag.getSammenligningsperiode());
        resultater.put("sammenligningsgrunnlagPrÅr", sammenligningsGrunnlag.getRapportertPrÅr());
        return beregnet(resultater);
    }

    private Periode lagSammenligningsPeriode(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunkt, BeregningsgrunnlagPeriode grunnlag) {
        LocalDate behandlingsTidspunkt = DateUtil.iDag();
        LocalDate gjeldendeTidspunkt = behandlingsTidspunkt.isBefore(skjæringstidspunkt) ? behandlingsTidspunkt : skjæringstidspunkt;
        LocalDate sisteØnskedeInntektMåned = gjeldendeTidspunkt.minusMonths(1).withDayOfMonth(1);
        if (erEtterRapporteringsFrist(inntektsgrunnlag.getInntektRapporteringFristDag(), gjeldendeTidspunkt, behandlingsTidspunkt)) {
            return lag12MånedersPeriodeTilOgMed(sisteØnskedeInntektMåned);
        }
        return lag12MånedersPeriodeTilOgMed(sisteØnskedeInntektMåned.minusMonths(1));
    }

    private static boolean erEtterRapporteringsFrist(int inntektRapporteringFristDag, LocalDate gjeldendeTidspunkt, LocalDate nåtid) {
        LocalDate fristUtenHelligdager = gjeldendeTidspunkt.withDayOfMonth(1).minusDays(1).plusDays(inntektRapporteringFristDag);
        LocalDate fristMedHelligdager = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(fristUtenHelligdager);
        return nåtid.isAfter(fristMedHelligdager);
    }

    private static Periode lag12MånedersPeriodeTilOgMed(LocalDate periodeTom) {
        LocalDate tom = periodeTom.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate fom = tom.minusYears(1).plusMonths(1).withDayOfMonth(1);
        return Periode.of(fom, tom);
    }
}
