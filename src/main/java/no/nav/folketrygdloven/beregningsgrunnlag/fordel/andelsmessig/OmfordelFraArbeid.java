package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
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
        Map<String, Object> resultater = omfordelFraAktivitetOmMulig(modell);
        Map<String, Object> resultater2 = omfordelNaturalytelseFraAktivitetOmMulig(modell);
        resultater.putAll(resultater2);
        return beregnet(resultater);
    }

    protected Map<String, Object> omfordelNaturalytelseFraAktivitetOmMulig(FordelModell modell) {
        boolean harAktivitetMedOmfordelbartGrunnlag = finnAktivitetMedOmfordelbarNaturalYtelse(modell.getInput()).isPresent();
        if (!harAktivitetMedOmfordelbartGrunnlag) {
            return new HashMap<>();
        }
        var aktivitet = finnArbeidsforholdMedRiktigInntektskategori(modell.getInput());
        return new OmfordelNaturalytelseForArbeidsforhold(modell).omfordelForArbeidsforhold(aktivitet, this::finnAktivitetMedOmfordelbarNaturalYtelse);
    }

    protected Optional<FordelAndelModell> finnAktivitetMedOmfordelbarNaturalYtelse(FordelPeriodeModell beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getAlleAndelerForStatus(AktivitetStatus.AT)
            .stream()
            .filter(a -> a.getNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
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
        BigDecimal refusjonskrav = arbeidsforhold.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO);
        return refusjonskrav.compareTo(arbeidsforhold.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO)) < 0;
    }

    private boolean harBgSomKanFlyttes(FordelAndelModell beregningsgrunnlagPrArbeidsforhold) {
        return beregningsgrunnlagPrArbeidsforhold.getBruttoPrÅr().orElse(BigDecimal.ZERO)
            .subtract(beregningsgrunnlagPrArbeidsforhold.getNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO)).compareTo(BigDecimal.ZERO) > 0
            && (beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr().isEmpty() || beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr().orElseThrow().compareTo(BigDecimal.ZERO) > 0);
    }

}
