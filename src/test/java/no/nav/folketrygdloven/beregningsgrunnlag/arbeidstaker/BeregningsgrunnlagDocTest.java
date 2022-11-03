package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.avkorting.RegelFastsettUtbetalingsbeløpTilBruker;
import no.nav.folketrygdloven.beregningsgrunnlag.dok.DokumentasjonRegelBeregnBruttoPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.dok.DokumentasjonRegelBeregningsgrunnlagATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.dok.DokumentasjonRegelFastsettAvkortetBGOver6GNårRefusjonUnder6G;
import no.nav.folketrygdloven.beregningsgrunnlag.dok.DokumentasjonRegelFastsettAvkortetVedRefusjonOver6G;
import no.nav.folketrygdloven.beregningsgrunnlag.dok.DokumentasjonRegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.dok.DokumentasjonRegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.over6g.RegelBeregnRefusjonPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap.RegelFastsettBeregningsgrunnlagDPellerAAP;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.specification.Specification;

public class BeregningsgrunnlagDocTest {

    @Test
    public void testRegelFastsettBeregningsgrunnlagDPellerAAP() {
        Specification<BeregningsgrunnlagPeriode> beregning = new RegelFastsettBeregningsgrunnlagDPellerAAP().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();
	    assertThat(json).isNotBlank();

    }

    @Test
    public void testRegelFastsettAvkortetVedRefusjonOver6G() {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelFastsettAvkortetVedRefusjonOver6G().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();
	    assertThat(json).isNotBlank();

    }

    @Test
    public void testRegelFastsettAvkortetBGOver6GNårRefusjonUnder6G() {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelFastsettAvkortetBGOver6GNårRefusjonUnder6G().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();
	    assertThat(json).isNotBlank();

    }

    @Test
    public void testRegelBeregnRefusjonPrArbeidsforhold() {
        Specification<BeregningsgrunnlagPeriode> beregning = new RegelBeregnRefusjonPrArbeidsforhold().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();
	    assertThat(json).isNotBlank();

    }

    @Test
    public void testRegelBeregningsgrunnlagATFL() {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelBeregningsgrunnlagATFL().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();
	    assertThat(json).isNotBlank();

    }

    @Test
    public void testRegelBeregnBruttoPrArbeidsforhold() {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelBeregnBruttoPrArbeidsforhold().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();
	    assertThat(json).isNotBlank();

    }

    @Test
    public void testRegelFastsettUtbetalingsbeløpTilBruker() {
        Specification<BeregningsgrunnlagPeriode> beregning = new RegelFastsettUtbetalingsbeløpTilBruker().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();
	    assertThat(json).isNotBlank();

    }

    @Test
    public void testRegelForeslåBeregningsgrunnlag() {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelForeslåBeregningsgrunnlag().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();
	    assertThat(json).isNotBlank();

    }

    @Test
    public void testRegelFullføreBeregningsgrunnlag() {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelFullføreBeregningsgrunnlag().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();
	    assertThat(json).isNotBlank();

    }




}
