package no.nav.folketrygdloven.skjæringstidspunkt.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettKombinasjoner.ID)
public class FastsettStatusOgAndelPrPeriode extends LeafSpecification<AktivitetStatusModell> {

    public static final String ID = "FP_BR_19_2";
    public static final String BESKRIVELSE = "Fastsett status per andel og periode";

    protected FastsettStatusOgAndelPrPeriode() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        opprettAktivitetStatuser(regelmodell);

        Map<String, Object> resultater = new HashMap<>();
        regelmodell.getAktivitetStatuser()
            .forEach(as -> resultater.put("Aktivitetstatus." + as.name(), as.getBeskrivelse()));
        regelmodell.getBeregningsgrunnlagPrStatusListe()
            .forEach(bgps -> resultater.put("BeregningsgrunnlagPrStatus." + bgps.getAktivitetStatus().name(), bgps.getAktivitetStatus().getBeskrivelse()));
        return beregnet(resultater);
    }

    private void opprettAktivitetStatuser(AktivitetStatusModell regelmodell) {
        LocalDate skjæringtidspktForBeregning = regelmodell.getSkjæringstidspunktForBeregning();
        List<AktivPeriode> aktivePerioder = regelmodell.getAktivePerioder();
        List<AktivPeriode> aktivePerioderVedStp = hentAktivePerioderForBeregning(skjæringtidspktForBeregning, aktivePerioder);
        if (harKunYtelsePåSkjæringstidspunkt(aktivePerioderVedStp)) {
            regelmodell.leggTilAktivitetStatus(AktivitetStatus.KUN_YTELSE);
            BeregningsgrunnlagPrStatus bgPrStatus = new BeregningsgrunnlagPrStatus(AktivitetStatus.BA);
            regelmodell.leggTilBeregningsgrunnlagPrStatus(bgPrStatus);
        } else {
            opprettStatusForAktiviteter(regelmodell, aktivePerioderVedStp);
        }
    }

    private void opprettStatusForAktiviteter(AktivitetStatusModell regelmodell, List<AktivPeriode> aktivePerioderVedStp) {
        for (AktivPeriode ap : aktivePerioderVedStp) {
            AktivitetStatus aktivitetStatus = mapAktivitetTilStatus(ap.getAktivitet());
            if (!AktivitetStatus.KUN_YTELSE.equals(aktivitetStatus) && ikkeMilitærMedAndreAktiviteterPåStp(aktivePerioderVedStp, aktivitetStatus)) {
                regelmodell.leggTilAktivitetStatus(aktivitetStatus);
                var arbeidsforhold = AktivitetStatus.ATFL.equals(aktivitetStatus) ? ap.getArbeidsforhold() : null;
                regelmodell.leggTilBeregningsgrunnlagPrStatus(new BeregningsgrunnlagPrStatus(aktivitetStatus, arbeidsforhold));
            }
        }
    }

    private boolean ikkeMilitærMedAndreAktiviteterPåStp(List<AktivPeriode> aktivPerioderVedSkjæringtidspunkt, AktivitetStatus aktivitetStatus) {
        return !(AktivitetStatus.MS.equals(aktivitetStatus) && aktivPerioderVedSkjæringtidspunkt.size() > 1);
    }

    private boolean harKunYtelsePåSkjæringstidspunkt(List<AktivPeriode> aktivPerioderVedSkjæringtidspunkt) {
        return !aktivPerioderVedSkjæringtidspunkt.isEmpty() && aktivPerioderVedSkjæringtidspunkt.stream()
            .allMatch(ap -> mapAktivitetTilStatus(ap.getAktivitet()).equals(AktivitetStatus.KUN_YTELSE));
    }

    private AktivitetStatus mapAktivitetTilStatus(Aktivitet aktivitet) {
        List<Aktivitet> arbeistaker = Arrays.asList(Aktivitet.ARBEIDSTAKERINNTEKT, Aktivitet.FRILANSINNTEKT,
            Aktivitet.VENTELØNN_VARTPENGER, Aktivitet.ETTERLØNN_SLUTTPAKKE, Aktivitet.VIDERE_ETTERUTDANNING,
            Aktivitet.UTDANNINGSPERMISJON);
        List<Aktivitet> tilstøtendeYtelse = Arrays.asList(Aktivitet.SYKEPENGER_MOTTAKER, Aktivitet.FORELDREPENGER_MOTTAKER,
            Aktivitet.PLEIEPENGER_MOTTAKER, Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Aktivitet.OPPLÆRINGSPENGER, Aktivitet.FRISINN_MOTTAKER,
            Aktivitet.OMSORGSPENGER);
        AktivitetStatus aktivitetStatus;

        if (Aktivitet.NÆRINGSINNTEKT.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.SN;
        } else if (Aktivitet.DAGPENGEMOTTAKER.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.DP;
        } else if (Aktivitet.AAP_MOTTAKER.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.AAP;
        } else if (Aktivitet.MILITÆR_ELLER_SIVILTJENESTE.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.MS;
        } else if (tilstøtendeYtelse.contains(aktivitet)) {
            aktivitetStatus = AktivitetStatus.KUN_YTELSE;
        } else if (arbeistaker.contains(aktivitet)) {
            aktivitetStatus = AktivitetStatus.ATFL;
        } else {
            aktivitetStatus = AktivitetStatus.UDEFINERT;
        }
        return aktivitetStatus;
    }

    private List<AktivPeriode> hentAktivePerioderForBeregning(LocalDate skjæringstidspunkt, List<AktivPeriode> aktivePerioder) {
        return aktivePerioder.stream().filter(ap -> ap.inneholder(finnDatogrenseForInkluderteAktiviteter(skjæringstidspunkt))).collect(Collectors.toList());
    }

    protected LocalDate finnDatogrenseForInkluderteAktiviteter(LocalDate dato) {
        return dato.minusDays(1);
    }

}
