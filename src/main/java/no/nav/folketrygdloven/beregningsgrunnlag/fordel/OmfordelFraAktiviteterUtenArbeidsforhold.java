package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class OmfordelFraAktiviteterUtenArbeidsforhold extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final String ID = "FP_BR 22.3.6";
    private static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra aktivitet uten arbeidsforhold";
    private final Comparator<BeregningsgrunnlagPrStatus> AVKORTING_COMPARATOR = Comparator.comparingInt(a -> a.getAktivitetStatus().getAvkortingPrioritet());

    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    OmfordelFraAktiviteterUtenArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        Map<String, Object> resultater = omfordelFraBgPrStatusUtenArbeidsforholdIPrioritertRekkefølge(beregningsgrunnlagPeriode);
        return beregnet(resultater);
    }

    private Map<String, Object> omfordelFraBgPrStatusUtenArbeidsforholdIPrioritertRekkefølge(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        BigDecimal restbeløpSomSkalFlyttesTilArbeidsforhold = finnRestbeløpSomMåOmfordeles();
        Map<String, Object> resultater = new HashMap<>();
        Optional<BeregningsgrunnlagPrStatus> bgPrStatusMedBeløpSomKanFlyttes = finnStatusMedDisponibeltBeløpOgHøyestAvkortingPrioritet(beregningsgrunnlagPeriode);
        while (harMerÅFlytte(restbeløpSomSkalFlyttesTilArbeidsforhold) && bgPrStatusMedBeløpSomKanFlyttes.isPresent()) {
            BeregningsgrunnlagPrStatus bgPrStatus = bgPrStatusMedBeløpSomKanFlyttes.get();
            BigDecimal maksimaltBeløpForOmfordelingPrStatus = finnFlyttbartGrunnlagForStatus(bgPrStatus);
            if (skalFlytteHeleGrunnlagetFraStatus(restbeløpSomSkalFlyttesTilArbeidsforhold, maksimaltBeløpForOmfordelingPrStatus)) {
                settFordeltForStatusTilNull(bgPrStatus);
                adderBeløpTilBgForArbeidsforhold(maksimaltBeløpForOmfordelingPrStatus);
                restbeløpSomSkalFlyttesTilArbeidsforhold = reduserRestbeløpSomSkalOmfordeles(restbeløpSomSkalFlyttesTilArbeidsforhold, maksimaltBeløpForOmfordelingPrStatus);
            } else {
                reduserFordeltForStatus(restbeløpSomSkalFlyttesTilArbeidsforhold, bgPrStatus);
                adderBeløpTilBgForArbeidsforhold(restbeløpSomSkalFlyttesTilArbeidsforhold);
                restbeløpSomSkalFlyttesTilArbeidsforhold = BigDecimal.ZERO;
            }
            resultater.put("fordeltPrÅr", bgPrStatus.getGradertFordeltPrÅr());
            resultater.put("aktivitetstatus", bgPrStatus.getAktivitetStatus());
            bgPrStatusMedBeløpSomKanFlyttes = finnStatusMedDisponibeltBeløpOgHøyestAvkortingPrioritet(beregningsgrunnlagPeriode);
        }
        return resultater;
    }

    private BigDecimal reduserRestbeløpSomSkalOmfordeles(BigDecimal restbeløpSomSkalOmfordelesTilAktivitet, BigDecimal maksimaltBeløpForOmfordelingPrStatus) {
        restbeløpSomSkalOmfordelesTilAktivitet = restbeløpSomSkalOmfordelesTilAktivitet.subtract(maksimaltBeløpForOmfordelingPrStatus);
        return restbeløpSomSkalOmfordelesTilAktivitet;
    }

    private void settFordeltForStatusTilNull(BeregningsgrunnlagPrStatus bgPrStatus) {
        BeregningsgrunnlagPrStatus.builder(bgPrStatus)
            .medFordeltPrÅr(BigDecimal.ZERO);
    }

    private void reduserFordeltForStatus(BigDecimal restbeløpSomSkalOmfordelesTilAktivitet, BeregningsgrunnlagPrStatus bgPrStatus) {
        BigDecimal fordelt = bgPrStatus.getGradertBruttoPrÅr().subtract(restbeløpSomSkalOmfordelesTilAktivitet);
        if (fordelt.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Kan ikke fordele et negativt beløp til " + bgPrStatus.getAktivitetStatus());
        }
        BeregningsgrunnlagPrStatus.builder(bgPrStatus)
            .medFordeltPrÅr(fordelt);
    }

    private BigDecimal finnRestbeløpSomMåOmfordeles() {
        BigDecimal refusjonskravPrÅr = arbeidsforhold.getGradertRefusjonskravPrÅr().orElse(BigDecimal.ZERO);
        BigDecimal bruttoBgForArbeidsforhold = arbeidsforhold.getGradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO);
        if (refusjonskravPrÅr.compareTo(bruttoBgForArbeidsforhold) <= 0) {
            throw new IllegalStateException("Skal ikke flytte beregningsgrunnlag til arbeidsforhold der refusjon ikke overstiger beregningsgrunnlag som allerede er satt.");
        }
        return refusjonskravPrÅr.subtract(bruttoBgForArbeidsforhold);
    }

    private Optional<BeregningsgrunnlagPrStatus> finnStatusMedDisponibeltBeløpOgHøyestAvkortingPrioritet(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusSomSkalBrukes()
            .stream()
            .filter(this::harBgSomKanFlyttes)
            .filter(bgprStatus -> bgprStatus.getArbeidsforhold().isEmpty())
            .max(AVKORTING_COMPARATOR);
    }

    private BigDecimal finnFlyttbartGrunnlagForStatus(BeregningsgrunnlagPrStatus bgPrStatus) {
        return bgPrStatus.getGradertBruttoPrÅr();
    }

    private boolean skalFlytteHeleGrunnlagetFraStatus(BigDecimal restBeløpSomMåFlyttes, BigDecimal flyttbartBeløp) {
        return restBeløpSomMåFlyttes.compareTo(flyttbartBeløp) >= 0;
    }

    private boolean harMerÅFlytte(BigDecimal skalFlyttesTilAktivitet) {
        return skalFlyttesTilAktivitet.compareTo(BigDecimal.ZERO) > 0;
    }

    private void adderBeløpTilBgForArbeidsforhold(BigDecimal beløpSomSkalOmfordelesTilArbeidsforhold) {
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medFordeltPrÅr(arbeidsforhold.getGradertBruttoPrÅr().add(beløpSomSkalOmfordelesTilArbeidsforhold));
    }

    private boolean harBgSomKanFlyttes(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
        return beregningsgrunnlagPrStatus.getGradertBruttoPrÅr().compareTo(BigDecimal.ZERO) > 0;
    }
}
