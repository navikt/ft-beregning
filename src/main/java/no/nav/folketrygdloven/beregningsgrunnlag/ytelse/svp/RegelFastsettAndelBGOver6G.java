package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsettAndelBGOver6G implements RuleService<BeregningsgrunnlagPeriode> {
    public static final String ID = "FP_BR_29.8";
    public static final String BESKRIVELSE = "Fastsett avkortet BG over 6G når refusjon under 6G";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelFastsettAndelBGOver6G(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        BeregningsgrunnlagPrStatus bgpsa = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        //FP_BR_29.8.10 For alle beregningsgrunnlagsandeler som gjelder arbeidsforhold, fastsett Brukers Andel
        //FP_BR_29.8.4 Avkort alle beregningsgrunnlagsander som ikke gjelder arbeidsforhold andelsmessig
        Specification<BeregningsgrunnlagPeriode> avkortAndelerAndelsmessigOgFastsettBrukersAndel = rs.beregningsRegel(
            AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig.ID,
            AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig.BESKRIVELSE,
            new AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig(),
            new FastsettAndelForBGAndelerSomGjelderArbeidsforhold());

        Specification<BeregningsgrunnlagPeriode> avkortAndelerSomIkkegjelderAFtil0 = new Beregnet();

        if (bgpsa != null) {
            Specification<BeregningsgrunnlagPeriode> fastsettAndelerForArbeidsforhold = rs.beregningsRegel(
                FastsettAndelForArbeidsforhold.ID,
                FastsettAndelForArbeidsforhold.BESKRIVELSE,
                new FastsettAndelForArbeidsforhold(),
                new Beregnet());

            //FP_BR_29.8.3 Avkort alle beregningsgrunnlagsandeler som ikke gjelder arbeidsforhold til 0
            avkortAndelerSomIkkegjelderAFtil0 = rs.beregningsRegel(
                AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0.ID,
                AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0.BESKRIVELSE,
                new AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0(),
                fastsettAndelerForArbeidsforhold);
        }

        //FP_BR_29.8.2 Er totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold > 6G?
        Specification<BeregningsgrunnlagPeriode> erTotaltBGFraArbeidforholdStørreEnn6G = rs.beregningHvisRegel(new SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdi(),
            avkortAndelerSomIkkegjelderAFtil0, avkortAndelerAndelsmessigOgFastsettBrukersAndel);


        return erTotaltBGFraArbeidforholdStørreEnn6G;
    }

}
