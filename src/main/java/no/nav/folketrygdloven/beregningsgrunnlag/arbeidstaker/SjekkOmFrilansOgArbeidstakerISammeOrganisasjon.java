package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFrilansOgArbeidstakerISammeOrganisasjon.ID)
class SjekkOmFrilansOgArbeidstakerISammeOrganisasjon extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 27.9";
    static final String BESKRIVELSE = "Er bruker frilans og arbeidstaker i samme organisasjon";


    SjekkOmFrilansOgArbeidstakerISammeOrganisasjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).erFlOgAtISammeOrganisasjon() ? ja() : nei();
    }
}
