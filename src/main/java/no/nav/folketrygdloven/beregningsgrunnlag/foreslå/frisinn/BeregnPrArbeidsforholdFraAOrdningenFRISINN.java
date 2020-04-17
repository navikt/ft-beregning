package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn.FinnPerioderUtenYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RuleDocumentation(BeregnPrArbeidsforholdFraAOrdningenFRISINN.ID)
class BeregnPrArbeidsforholdFraAOrdningenFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final BigDecimal ANTALL_MÅNEDER_I_ÅR = BigDecimal.valueOf(12);
    static final String ID = "FRISINN 2.3";
    static final String BESKRIVELSE = "Rapportert inntekt = snitt av mnd-inntekter i beregningsperioden * 12";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    BeregnPrArbeidsforholdFraAOrdningenFRISINN(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        Objects.requireNonNull(arbeidsforhold, "arbeidsforhold");
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
        LocalDate skjæringstidspunkt = grunnlag.getSkjæringstidspunkt();
        List<Periode> perioderSomSkalBrukesForInntekter = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, skjæringstidspunkt, resultater);

        BigDecimal totalSumForArbeidsforhold = BigDecimal.ZERO;
        for (Periode periode : perioderSomSkalBrukesForInntekter) {
            List<Periodeinntekt> inntekterHosAgForPeriode = inntektsgrunnlag.getInntektForArbeidsforholdIPeriode(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, periode);
            BigDecimal sumForPeriode = inntekterHosAgForPeriode.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            totalSumForArbeidsforhold = totalSumForArbeidsforhold.add(sumForPeriode);
            resultater.put("sumForPeriode" + periode.toString(), sumForPeriode);
        }

        BigDecimal antallPerioder = BigDecimal.valueOf(perioderSomSkalBrukesForInntekter.size());
        BigDecimal snittlønnPrMnd = totalSumForArbeidsforhold.divide(antallPerioder, 10, RoundingMode.HALF_EVEN);
        BigDecimal årslønn = snittlønnPrMnd.multiply(ANTALL_MÅNEDER_I_ÅR);

        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medBeregnetPrÅr(årslønn)
            .build();

        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        resultater.put("sumForArbeidsforhold", totalSumForArbeidsforhold);
        resultater.put("antallPerioder", antallPerioder);
        resultater.put("beregnetPrÅr", årslønn);
        return beregnet(resultater);
    }
}
