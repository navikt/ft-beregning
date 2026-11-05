package no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon.FastsettRefusjonsgrunnlagRegel.RefusjonsgrunnlagProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagResultat.Refusjonsandel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagResultat.Refusjonsandel.Refusjonsperiode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonKilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettRefusjonsgrunnlag.ID)
public class FastsettRefusjonsgrunnlag extends LeafSpecification<RefusjonsgrunnlagProsesstruktur> {

    static final String ID = FastsettRefusjonsgrunnlagRegel.ID + ".1";

    public FastsettRefusjonsgrunnlag() {
        super(ID, "Fastsett refusjonsandeler fra periodiserte refusjonskrav");
    }

    @Override
    public Evaluation evaluate(RefusjonsgrunnlagProsesstruktur prosesstruktur) {
        var refusjonsandeler = fastsettRefusjonsandeler(prosesstruktur.input());
        prosesstruktur.refusjonsandeler().addAll(refusjonsandeler);
        var resultat = ja();
        resultat.setEvaluationProperty("refusjonsandeler", refusjonsandeler);
        return resultat;
    }

    private static List<Refusjonsandel> fastsettRefusjonsandeler(RefusjonsgrunnlagModell input) {
        return input.refusjonskravListe().stream()
            .map(FastsettRefusjonsgrunnlag::lagAndel)
            .toList();
    }

    private static Refusjonsandel lagAndel(Refusjonskrav krav) {
        var journalpostId = Objects.requireNonNull(krav.journalpostId(), "Journalpost er påkrevd for refusjonskrav fra inntektsmelding");
        var perioder = krav.kravperioder().stream()
            .sorted(Comparator.comparing(p -> p.periode().getFom()))
            .map(p -> new Refusjonsperiode(p.periode(), p.refusjonPerMnd(), Utfall.GODKJENT, journalpostId))
            .toList();
        return new Refusjonsandel(krav.arbeidsforholdsinformasjon().arbeidsgiver(),
            krav.arbeidsforholdsinformasjon().arbeidsforholdId(), journalpostId, RefusjonKilde.INNTEKTSMELDING, perioder);
    }
}
