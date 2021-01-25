package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnPrArbeidsforholdFraAOrdningen.ID)
class BeregnPrArbeidsforholdFraAOrdningen extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FB_BR 14.3";
    static final String BESKRIVELSE = "Rapportert inntekt = snitt av mnd-inntekter i beregningsperioden * 12";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    BeregnPrArbeidsforholdFraAOrdningen(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        Objects.requireNonNull(arbeidsforhold, "arbeidsforhold");
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Periode bp = arbeidsforhold.getBeregningsperiode();
        int beregningsPeriodeLengdeIMnd = 3;
        if (bp == null) {
            throw new IllegalStateException("Beregningsperiode mangler, kan ikke fastsette beregningsgrunnlag for arbeidsforhold");
        }

        List<BigDecimal> inntekter = grunnlag.getInntektsgrunnlag().getPeriodeinntekter(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, bp.getTom(), beregningsPeriodeLengdeIMnd);
        BigDecimal sum = inntekter.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal snittFraBeregningsperiodenPrÅr = sum.multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(beregningsPeriodeLengdeIMnd), 10, RoundingMode.HALF_EVEN);
        BigDecimal andelFraAOrdningenPrÅr = finnAndelAvBeregnet(snittFraBeregningsperiodenPrÅr, arbeidsforhold, grunnlag);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medBeregnetPrÅr(andelFraAOrdningenPrÅr)
            .build();
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("beregnetPrÅr", arbeidsforhold.getBeregnetPrÅr());
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        return beregnet(resultater);
    }

    /**
     * I tilfelle der det er omsorgspenger/pleiepenger og det er kun mottatt inntektsmelding for enkelt arbeidsforhold ho en arbeidsgiver
     * skal resterende arbeidsforhold fordele restbeløpet fra a-ordningen mellom seg (https://jira.adeo.no/browse/TSF-1153).
     *
     * I alle andre caser gis det fulle snittbeløpet til dette arbeidsforholdet.
     *
     * @param beregnetPrÅr Snitt de siste 3 månedene fra a-ordningen
     * @param arbeidsforhold Arbeidsforhold
     * @param periode Periode
     * @return Andel av snitt fra a-ordningen
     */
    private BigDecimal finnAndelAvBeregnet(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        var ytelsesSpesifiktGrunnlag = periode.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlagHvisFinnes();
        if (!arbeidsforhold.erFrilanser() && arbeidsforhold.getArbeidsgiverId() != null && ytelsesSpesifiktGrunnlag.map(YtelsesSpesifiktGrunnlag::erKap9Ytelse).orElse(false)) {
            return fordelRestinntektFraAOrdningen(beregnetPrÅr, arbeidsforhold, periode);
        }
        return beregnetPrÅr;
    }

    private BigDecimal fordelRestinntektFraAOrdningen(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        Inntektsgrunnlag inntektsgrunnlag = periode.getInntektsgrunnlag();
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdISammeOrg = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getArbeidsforholdIkkeFrilans()
            .stream()
            .filter(a -> a.getArbeidsgiverId() != null)
            .filter(a -> a.getArbeidsgiverId().equals(arbeidsforhold.getArbeidsgiverId()))
            .collect(Collectors.toList());
        BigDecimal beløpFraInntektsmeldingPrÅr = arbeidsforholdISammeOrg.stream()
            .map(inntektsgrunnlag::getInntektFraInntektsmelding)
            .map(beløp -> beløp.multiply(BigDecimal.valueOf(12)))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        if (beløpFraInntektsmeldingPrÅr.compareTo(beregnetPrÅr) > 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal restFraAOrdningen = beregnetPrÅr.subtract(beløpFraInntektsmeldingPrÅr);

        long antallArbeidsforholdUtenInntektsmelding = arbeidsforholdISammeOrg.stream()
            .filter(a -> inntektsgrunnlag.getInntektFraInntektsmelding(a) == null || inntektsgrunnlag.getInntektFraInntektsmelding(a).compareTo(BigDecimal.ZERO) == 0)
            .count();

        if (antallArbeidsforholdUtenInntektsmelding == 0) {
            throw new IllegalStateException("Kan ikke beregne andel fra aordningen når alle andeler for arbeidsgiver med orgnr " + arbeidsforhold.getArbeidsgiverId() +
                " har inntektsmelding");
        }

        return restFraAOrdningen.divide(BigDecimal.valueOf(antallArbeidsforholdUtenInntektsmelding), RoundingMode.HALF_EVEN);
    }
}
