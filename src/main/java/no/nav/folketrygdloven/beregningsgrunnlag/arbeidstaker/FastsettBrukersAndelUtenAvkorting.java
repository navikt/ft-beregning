package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBrukersAndelUtenAvkorting.ID)
class FastsettBrukersAndelUtenAvkorting extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 29.6.1";
    static final String BESKRIVELSE = "Fastsett Brukers Andel og ev arbeidsgivers andel av Brutto beregningsgrunnlag";

    FastsettBrukersAndelUtenAvkorting() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (bgps == null) {
            return ja(); // Andre statuser har alltid brukers andel lik brutto/avkortet/redusert BG
        }

		// Kva skjer her?
        for (BeregningsgrunnlagPrArbeidsforhold arbeidsforhold : bgps.getArbeidsforholdSomSkalBrukes()) {
            BigDecimal arbeidsgiversAndel = arbeidsforhold.getMaksimalRefusjonPrÅr();

            BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
                .medMaksimalRefusjonPrÅr(arbeidsgiversAndel);
        }

        return ja();

    }

}
