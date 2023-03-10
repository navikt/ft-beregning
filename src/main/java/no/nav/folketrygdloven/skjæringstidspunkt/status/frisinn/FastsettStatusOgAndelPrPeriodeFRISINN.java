package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
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
    public static final int MND_FØR_STP_MED_FL_INNTEKT = 12;

    FastsettStatusOgAndelPrPeriodeFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        Map<String, Object> resultater = new HashMap<>();
        opprettAktivitetStatuser((AktivitetStatusModellFRISINN) regelmodell);
        regelmodell.getAktivitetStatuser()
            .forEach(as -> resultater.put("Aktivitetstatus." + as.name(), as.getBeskrivelse()));
        regelmodell.getBeregningsgrunnlagPrStatusListe()
            .forEach(bgps -> resultater.put("BeregningsgrunnlagPrStatus." + bgps.getAktivitetStatus().name(), bgps.getAktivitetStatus().getBeskrivelse()));
        return beregnet(resultater);
    }

    private void opprettAktivitetStatuser(AktivitetStatusModellFRISINN regelmodell) {
        LocalDate skjæringtidspktForBeregning = regelmodell.getSkjæringstidspunktForBeregning();
        List<AktivPeriode> aktivePerioder = regelmodell.getAktivePerioder();
        List<AktivPeriode> aktivePerioderVedStp = hentAktivePerioderPåSkjæringtidspunkt(skjæringtidspktForBeregning, aktivePerioder);
        Inntektsgrunnlag inntektsgrunnlag = regelmodell.getInntektsgrunnlag();
        boolean harPerioderUtenYtelse = !FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, regelmodell.getSkjæringstidspunktForOpptjening()).isEmpty();
        boolean skalIkkeLeggeTilFL = skalIkkeLeggeTilFrilans(regelmodell.getSkjæringstidspunktForOpptjening(), regelmodell);
        for (AktivPeriode ap : aktivePerioderVedStp) {
            AktivitetStatus aktivitetStatus = mapAktivitetTilStatus(ap.getAktivitet(), harPerioderUtenYtelse);
            if (skalIkkeLeggesTil(skalIkkeLeggeTilFL, ap, aktivitetStatus)) {
                continue;
            }
            regelmodell.leggTilAktivitetStatus(aktivitetStatus);
            var arbeidsforhold = AktivitetStatus.ATFL.equals(aktivitetStatus) ? ap.getArbeidsforhold() : null;
            regelmodell.leggTilBeregningsgrunnlagPrStatus(new BeregningsgrunnlagPrStatus(aktivitetStatus, arbeidsforhold));
        }
    }

    private boolean skalIkkeLeggeTilFrilans(LocalDate stpOpptjening, AktivitetStatusModellFRISINN regelModellFrisinn) {
        Inntektsgrunnlag inntektsgrunnlag = regelModellFrisinn.getInntektsgrunnlag();
        boolean harIngenPerioderMedKunFrilans = regelModellFrisinn.getFrisinnPerioder().stream()
            .noneMatch(fp -> (fp.getSøkerYtelseFrilans() && !fp.getSøkerYtelseNæring()));
        boolean ingenPerioderHarLøpendeInntekt = regelModellFrisinn.getFrisinnPerioder().stream()
            .allMatch(fp -> {
                List<Periodeinntekt> inntektspostFraSøknadForStatusIPeriode = inntektsgrunnlag.getInntektspostFraSøknadForStatusIPeriode(AktivitetStatus.FL, fp.getPeriode());
                BigDecimal inntektIPeriode = inntektspostFraSøknadForStatusIPeriode.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                return inntektIPeriode.compareTo(BigDecimal.ZERO) == 0;
            });
        List<BigDecimal> flInntekterFraRegister = inntektsgrunnlag.getFrilansPeriodeinntekter(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, stpOpptjening, MND_FØR_STP_MED_FL_INNTEKT);
        return (flInntekterFraRegister.isEmpty() && harIngenPerioderMedKunFrilans) && ingenPerioderHarLøpendeInntekt;
    }

    private boolean skalIkkeLeggesTil(boolean skalIkkeLeggeTilFL, AktivPeriode ap, AktivitetStatus aktivitetStatus) {
        return (AktivitetStatus.ATFL.equals(aktivitetStatus) && Aktivitet.FRILANSINNTEKT.equals(ap.getArbeidsforhold().getAktivitet()) && skalIkkeLeggeTilFL)
            || AktivitetStatus.UDEFINERT.equals(aktivitetStatus);
    }

    private List<AktivPeriode> hentAktivePerioderPåSkjæringtidspunkt(LocalDate dato, List<AktivPeriode> aktivePerioder) {
        return aktivePerioder.stream()
            .filter(ap -> ap.getPeriode().getFom().isBefore(dato))
            .filter(ap -> ap.inneholder(dato)).toList();
    }


    private AktivitetStatus mapAktivitetTilStatus(Aktivitet aktivitet, boolean harPerioderUtenYtelse) {
        List<Aktivitet> arbeistaker = Arrays.asList(Aktivitet.ARBEIDSTAKERINNTEKT, Aktivitet.FRILANSINNTEKT);
        AktivitetStatus aktivitetStatus;

        if (Aktivitet.NÆRINGSINNTEKT.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.SN;
        } else if (Aktivitet.DAGPENGEMOTTAKER.equals(aktivitet) && !harPerioderUtenYtelse) {
            aktivitetStatus = AktivitetStatus.DP;
        } else if (Aktivitet.AAP_MOTTAKER.equals(aktivitet) && !harPerioderUtenYtelse) {
            aktivitetStatus = AktivitetStatus.AAP;
        } else if (arbeistaker.contains(aktivitet)) {
            aktivitetStatus = AktivitetStatus.ATFL;
        } else {
            aktivitetStatus = AktivitetStatus.UDEFINERT;
        }
        return aktivitetStatus;
    }


}
