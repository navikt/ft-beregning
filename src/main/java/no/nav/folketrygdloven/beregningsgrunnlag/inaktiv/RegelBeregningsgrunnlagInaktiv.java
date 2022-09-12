package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelBeregnBruttoPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Beregner bruker som er midlertidig utenfor inntektsgivende arbeid etter §8-47
 *
 * Normal beregning er snittinntekt fra de tre siste ferdiglignede år med mindre annen informasjon er tilgjengelig.
 *
 * Om det er mottatt inntektsmelding og bruker har et registrert arbeidsforhold på skjæringstidspunktet (8-47 b),
 * skal inntekt fra inntektsmeldingen brukes i fastsettelse av beregningsgrunnlaget.
 *
 * Beregning ved kombinasjon av inntekt fra tre siste ferdiglignede år og informasjon fra inntektsmelding er ikke støttet enda.
 *
 * Bruker skal også kunne oppgi inntekt/varige endringer selv i søknad, men dette er enda ikke støttet.
 *
 */
public class RegelBeregningsgrunnlagInaktiv implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_14-15-27-28";
    private final BeregningsgrunnlagPeriode regelmodell;

    public RegelBeregningsgrunnlagInaktiv(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = getArbeidsforhold();
        Specification<BeregningsgrunnlagPeriode> beregningsgrunnlagATFL = arbeidsforhold.isEmpty() ? new Beregnet() :
                rs.beregningsForeachThenRegel("FP_BR 14.X", "Fastsett beregningsgrunnlag pr arbeidsforhold",
                    new RegelBeregnBruttoPrArbeidsforhold().getSpecification(), "arbeidsforhold", arbeidsforhold, new Beregnet());

        Specification<BeregningsgrunnlagPeriode> sjekkOmMottattInntektsmelding =
                rs.beregningHvisRegel(new SjekkHarArbeidsforholdMedIM(),
		                beregningsgrunnlagATFL,
		                new RegelBeregningsgrunnlagInaktivUtenIM().getSpecification());

	    Specification<BeregningsgrunnlagPeriode> settHjemmel =
			    rs.beregningsRegel(SettHjemmelInaktiv.ID, SettHjemmelInaktiv.BESKRIVELSE, new SettHjemmelInaktiv(), sjekkOmMottattInntektsmelding);

        return settHjemmel;
    }

	private List<BeregningsgrunnlagPrArbeidsforhold> getArbeidsforhold() {
		BeregningsgrunnlagPrStatus atflStatus = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
		return atflStatus == null ? Collections.emptyList() : atflStatus.getArbeidsforhold();
	}
}
