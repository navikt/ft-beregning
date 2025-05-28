package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.nare.evaluation.Evaluation;

class OmfordelFraArbeid extends OmfordelFraATFL {

    public static final String ID = "FP_BR 22.3.8";
    public static final String BESKRIVELSE = "Flytt beregnignsgrunnlag fra andre arbeidsforhold.";

    OmfordelFraArbeid(FordelAndelModell arbeidsforhold) {
        super(arbeidsforhold, ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell modell) {
        var resultater = omfordelFraAktivitetOmMulig(modell);
        var resultater2 = omfordelNaturalytelseFraAktivitetOmMulig(modell);
        resultater.putAll(resultater2);
        return beregnet(resultater);
    }

    protected Map<String, Object> omfordelNaturalytelseFraAktivitetOmMulig(FordelModell modell) {
        var harAktivitetMedOmfordelbartGrunnlag = finnAktivitetMedOmfordelbarNaturalYtelse(modell.getInput()).isPresent();
        if (!harAktivitetMedOmfordelbartGrunnlag) {
            return new HashMap<>();
        }
        var aktivitet = finnArbeidsforholdMedRiktigInntektskategori(modell.getInput());
        return new OmfordelNaturalytelseForArbeidsforhold(modell).omfordelForArbeidsforhold(aktivitet, this::finnAktivitetMedOmfordelbarNaturalYtelse);
    }

    protected Optional<FordelAndelModell> finnAktivitetMedOmfordelbarNaturalYtelse(FordelPeriodeModell beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getAlleAndelerForStatus(AktivitetStatus.AT)
            .stream()
            .filter(a -> a.getGradertNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
            .filter(this::harRefusjonskravLavereEnnBg)
            .findFirst();
    }


    @Override
    protected Inntektskategori finnInntektskategori() {
        return Inntektskategori.ARBEIDSTAKER;
    }

    @Override
    protected Optional<FordelAndelModell> finnAktivitetMedOmfordelbartBg(FordelPeriodeModell beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getAlleAndelerForStatus(AktivitetStatus.AT)
            .stream()
            .filter(this::harBgSomKanFlyttes)
            .filter(this::harRefusjonskravLavereEnnBg)
            .findFirst();
    }

    private boolean harRefusjonskravLavereEnnBg(FordelAndelModell arbeidsforhold) {
        var refusjonskrav = arbeidsforhold.getGradertRefusjonPrÅr().orElse(BigDecimal.ZERO);
        return refusjonskrav.compareTo(arbeidsforhold.getGradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO)) < 0;
    }

    private boolean harBgSomKanFlyttes(FordelAndelModell beregningsgrunnlagPrArbeidsforhold) {
        return beregningsgrunnlagPrArbeidsforhold.getGradertBruttoPrÅr().orElse(BigDecimal.ZERO)
            .subtract(beregningsgrunnlagPrArbeidsforhold.getGradertNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO)).compareTo(BigDecimal.ZERO) > 0
            && (beregningsgrunnlagPrArbeidsforhold.getGradertFordeltPrÅr().isEmpty() || beregningsgrunnlagPrArbeidsforhold.getGradertFordeltPrÅr().orElseThrow().compareTo(BigDecimal.ZERO) > 0);
    }

}
