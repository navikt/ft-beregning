package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettStatusOgAndelPrPeriodeFRISINN.ID)
public class FastsettStatusOgAndelPrPeriodeFRISINN extends LeafSpecification<AktivitetStatusModellFRISINN> {

    static final String ID = "FP_BR_19_2";
    static final String BESKRIVELSE = "Fastsett status per andel og periode";

    FastsettStatusOgAndelPrPeriodeFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModellFRISINN regelmodell) {
        Map<String, Object> resultater = new HashMap<>();
        List<Periode> beregningsperioden = finnBeregningsperioder(regelmodell, resultater);
        regelmodell.setBeregningsperioder(beregningsperioden);
        beregningsperioden.forEach(bp -> resultater.put("Periode " + bp.getFom() + " - " + bp.getTom(), "Beregningsperiode"));
        opprettAktivitetStatuserForAllePerioder(beregningsperioden, regelmodell);
        regelmodell.getAktivitetStatuser()
            .forEach(as -> resultater.put("Aktivitetstatus." + as.name(), as.getBeskrivelse()));
        regelmodell.getBeregningsgrunnlagPrStatusListe()
            .forEach(bgps -> resultater.put("BeregningsgrunnlagPrStatus." + bgps.getAktivitetStatus().name(), bgps.getAktivitetStatus().getBeskrivelse()));
        return beregnet(resultater);
    }

    private void opprettAktivitetStatuserForAllePerioder(List<Periode> beregningsperioden, AktivitetStatusModellFRISINN regelmodell) {
        List<AktivPeriode> aktivePerioder = regelmodell.getAktivePerioder();
        List<AktivPeriode> aktivePerioderVedStp = hentAktivePerioderIListeMedPerioder(beregningsperioden, aktivePerioder);
        for (AktivPeriode ap : aktivePerioderVedStp) {
            AktivitetStatus aktivitetStatus = mapAktivitetTilStatus(ap.getAktivitet());
            regelmodell.leggTilAktivitetStatus(aktivitetStatus);
            var arbeidsforhold = AktivitetStatus.ATFL.equals(aktivitetStatus) ? ap.getArbeidsforhold() : null;
            regelmodell.leggTilBeregningsgrunnlagPrStatus(new BeregningsgrunnlagPrStatus(aktivitetStatus, arbeidsforhold));
        }
        if (harIkkeOpprettetFrilans(regelmodell)) {
            regelmodell.leggTilBeregningsgrunnlagPrStatus(new BeregningsgrunnlagPrStatus(AktivitetStatus.ATFL, Arbeidsforhold.frilansArbeidsforhold()));
        }
        if (harIkkeOpprettetNæring(regelmodell)) {
            regelmodell.leggTilBeregningsgrunnlagPrStatus(new BeregningsgrunnlagPrStatus(AktivitetStatus.SN));
        }
    }

    private boolean harIkkeOpprettetFrilans(AktivitetStatusModellFRISINN regelmodell) {
        return regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .noneMatch(st -> st.getAktivitetStatus().equals(AktivitetStatus.ATFL)
                && st.getArbeidsforholdList().stream().anyMatch(arb -> arb.getAktivitet().equals(Aktivitet.FRILANSINNTEKT)));
    }

    private boolean harIkkeOpprettetNæring(AktivitetStatusModellFRISINN regelmodell) {
        return regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .noneMatch(st -> st.getAktivitetStatus().equals(AktivitetStatus.SN));
    }

    private List<Periode> finnBeregningsperioder(AktivitetStatusModellFRISINN regelmodell, Map<String, Object> resultater) {
        Inntektsgrunnlag inntektsgrunnlag = regelmodell.getInntektsgrunnlag();
        LocalDate skjæringstidspunktForBeregning = regelmodell.getSkjæringstidspunktForBeregning();
        return FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, skjæringstidspunktForBeregning, resultater);
    }

    private AktivitetStatus mapAktivitetTilStatus(Aktivitet aktivitet) {
        List<Aktivitet> arbeistaker = Arrays.asList(Aktivitet.ARBEIDSTAKERINNTEKT, Aktivitet.FRILANSINNTEKT);
        AktivitetStatus aktivitetStatus;
        if (Aktivitet.NÆRINGSINNTEKT.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.SN;
        } else if (arbeistaker.contains(aktivitet) && aktivitet.harOrgnr()) {
            aktivitetStatus = AktivitetStatus.ATFL;
        } else {
            aktivitetStatus = AktivitetStatus.UDEFINERT;
        }
        return aktivitetStatus;
    }

    private List<AktivPeriode> hentAktivePerioderIListeMedPerioder(List<Periode> beregningsperioder, List<AktivPeriode> aktivePerioder) {
        return aktivePerioder.stream()
            .filter(ap -> beregningsperioder.stream().anyMatch(p -> ap.getPeriode().overlapper(p)))
            .collect(Collectors.toList());
    }

}
