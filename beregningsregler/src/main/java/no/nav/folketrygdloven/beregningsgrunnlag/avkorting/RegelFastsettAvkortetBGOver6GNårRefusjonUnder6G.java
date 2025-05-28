package no.nav.folketrygdloven.beregningsgrunnlag.avkorting;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Fastsatt;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G implements RuleService<BeregningsgrunnlagPeriode> {
    public static final String ID = "FP_BR_29.8";
    public static final String BESKRIVELSE = "Fastsett avkortet BG over 6G når refusjon under 6G";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var bgpsa = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        //FP_BR_29.8.10 For alle beregningsgrunnlagsandeler som gjelder arbeidsforhold, fastsett Brukers Andel
        //FP_BR_29.8.4 Avkort alle beregningsgrunnlagsander som ikke gjelder arbeidsforhold andelsmessig
        var avkortAndelerAndelsmessigOgFastsettBrukersAndel = rs.beregningsRegel(AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig.ID,
                AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig.BESKRIVELSE,
		        new FastsettBrukersAndelForBGAndelerSomGjelderArbeidsforhold(),
                new AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig());

        Specification<BeregningsgrunnlagPeriode> avkortAndelerSomIkkegjelderAFtil0 = new Fastsatt();

        if (bgpsa != null) {

        //FP_BR_29.8.6-9 Fastsett andel til fordeling - Itereres over like mange ganger som antall arbeidsforhold.
            var antallKjøringer = bgpsa.getArbeidsforhold().size();
            if (antallKjøringer > 0) {
                List<Specification<BeregningsgrunnlagPeriode>> prArbeidsforhold = new ArrayList<>();
                bgpsa.getArbeidsforhold().forEach(af -> prArbeidsforhold.add(opprettRegelFastsettUtbetalingsbeløpTilBruker()));
                var fastsettUtbetalingsbeløpTilBrukerChain = rs.beregningsRegel(ID, BESKRIVELSE, prArbeidsforhold, new Fastsatt());

                //FP_BR_29.8.3 Avkort alle beregningsgrunnlagsandeler som ikke gjelder arbeidsforhold til 0
                avkortAndelerSomIkkegjelderAFtil0 = rs.beregningsRegel(AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0.ID, AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0.BESKRIVELSE,
                    new AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0(), fastsettUtbetalingsbeløpTilBrukerChain);
            }
        }

        //FP_BR_29.8.2 Er totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold > 6G?
        var erTotaltBGFraArbeidforholdStørreEnn6G = rs.beregningHvisRegel(new SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdi(),
            avkortAndelerSomIkkegjelderAFtil0, avkortAndelerAndelsmessigOgFastsettBrukersAndel);

        //FP_BR_29.8.1 For alle beregningsgrunnlagsandeler som gjelder arbeidsforhold, fastsett avkortet refusjon pr andel
        var fastsettAvkortetBGOver6GNårRefusjonUnder6G =
            rs.beregningsRegel(ID, BESKRIVELSE, new FastsettAvkortetRefusjonPrAndel(), erTotaltBGFraArbeidforholdStørreEnn6G);


        return fastsettAvkortetBGOver6GNårRefusjonUnder6G;
    }

    private Specification<BeregningsgrunnlagPeriode> opprettRegelFastsettUtbetalingsbeløpTilBruker() {
        return new RegelFastsettUtbetalingsbeløpTilBruker().getSpecification();
    }
}
