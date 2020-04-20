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
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettStatusOgAndelPrPeriodeFRISINN.ID)
public class FastsettStatusOgAndelPrPeriodeFRISINN extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR_19_2";
    static final String BESKRIVELSE = "Fastsett status per andel og periode";

    FastsettStatusOgAndelPrPeriodeFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        Map<String, Object> resultater = new HashMap<>();
        opprettAktivitetStatuser(regelmodell);
        regelmodell.getAktivitetStatuser()
            .forEach(as -> resultater.put("Aktivitetstatus." + as.name(), as.getBeskrivelse()));
        regelmodell.getBeregningsgrunnlagPrStatusListe()
            .forEach(bgps -> resultater.put("BeregningsgrunnlagPrStatus." + bgps.getAktivitetStatus().name(), bgps.getAktivitetStatus().getBeskrivelse()));
        return beregnet(resultater);
    }

    private void opprettAktivitetStatuser(AktivitetStatusModell regelmodell) {
        LocalDate skjæringtidspktForBeregning = regelmodell.getSkjæringstidspunktForBeregning();
        List<AktivPeriode> aktivePerioder = regelmodell.getAktivePerioder();
        List<AktivPeriode> aktivePerioderVedStp = hentAktivePerioderVedSkjæringtidspunkt(skjæringtidspktForBeregning, aktivePerioder);
        for (AktivPeriode ap : aktivePerioderVedStp) {
            AktivitetStatus aktivitetStatus = mapAktivitetTilStatus(ap.getAktivitet());
            regelmodell.leggTilAktivitetStatus(aktivitetStatus);
            var arbeidsforhold = AktivitetStatus.ATFL.equals(aktivitetStatus) ? ap.getArbeidsforhold() : null;
            regelmodell.leggTilBeregningsgrunnlagPrStatus(new BeregningsgrunnlagPrStatus(aktivitetStatus, arbeidsforhold));
        }
    }

    private List<AktivPeriode> hentAktivePerioderVedSkjæringtidspunkt(LocalDate dato, List<AktivPeriode> aktivePerioder) {
        return aktivePerioder.stream().filter(ap -> ap.inneholder(dato.minusDays(1))).collect(Collectors.toList());
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

}
