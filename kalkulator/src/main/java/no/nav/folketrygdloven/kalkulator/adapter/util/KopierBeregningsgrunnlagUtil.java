package no.nav.folketrygdloven.kalkulator.adapter.util;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

public final class KopierBeregningsgrunnlagUtil {

    private KopierBeregningsgrunnlagUtil() {
    }

    public static void kopierBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagPeriode kopi) {
        if (!kopi.getBeregningsgrunnlagPrStatus().isEmpty()) {
            throw new IllegalStateException("Kan ikke kopiere beregningsgrunnlagperiode når beregningsgrunnlag pr status allerede finnes");
        }
        for (BeregningsgrunnlagPrStatus forrigeStatus : grunnlag.getBeregningsgrunnlagPrStatus()) {
            if (forrigeStatus.erArbeidstakerEllerFrilanser()) {
                BeregningsgrunnlagPrStatus ny = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(forrigeStatus.getAktivitetStatus())
                    .medBeregningsgrunnlagPeriode(kopi)
                    .build();
                kopierArbeidsforhold(forrigeStatus.getArbeidsforhold(), ny, grunnlag.getYtelsedagerPrÅr());
            } else {
                BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(forrigeStatus.getAktivitetStatus())
                    .medBeregnetPrÅr(forrigeStatus.getBeregnetPrÅr())
                    .medBeregningsgrunnlagPeriode(kopi)
                    .medBesteberegningPrÅr(forrigeStatus.getBesteberegningPrÅr())
                    .medErNyIArbeidslivet(forrigeStatus.getNyIArbeidslivet())
                    .medAndelNr(forrigeStatus.getAndelNr())
                    .medInntektskategori(forrigeStatus.getInntektskategori())
                    .build();
            }
        }
    }

    private static void kopierArbeidsforhold(List<BeregningsgrunnlagPrArbeidsforhold> kopierFraArbeidsforholdListe,
                                             BeregningsgrunnlagPrStatus ny, BigDecimal ytelsedagerPrÅr) {
        for (BeregningsgrunnlagPrArbeidsforhold kopierFraArbeidsforhold : kopierFraArbeidsforholdListe) {
            BeregningsgrunnlagPrArbeidsforhold kopiertArbeidsforhold = kopierArbeidsforhold(kopierFraArbeidsforhold, ytelsedagerPrÅr);
            BeregningsgrunnlagPrStatus.builder(ny).medArbeidsforhold(kopiertArbeidsforhold);
        }
    }

    private static BeregningsgrunnlagPrArbeidsforhold kopierArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold kopierFraArbeidsforhold, BigDecimal ytelsedagerPrÅr) {
        BeregningsgrunnlagPrArbeidsforhold.Builder bgPrArbeidsforholdBuilder = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(kopierFraArbeidsforhold.getArbeidsforhold())
            .medInntektskategori(kopierFraArbeidsforhold.getInntektskategori())
            .medAndelNr(kopierFraArbeidsforhold.getAndelNr())
            .medBeregnetPrÅr(kopierFraArbeidsforhold.getBeregnetPrÅr())
            .medNaturalytelseBortfaltPrÅr(kopierFraArbeidsforhold.getNaturalytelseBortfaltPrÅr().orElse(null))
            .medNaturalytelseTilkommetPrÅr(kopierFraArbeidsforhold.getNaturalytelseTilkommetPrÅr().orElse(null))
            .medGjeldendeRefusjonPrÅr(kopierFraArbeidsforhold.getGjeldendeRefusjonPrÅr().orElse(null))
            .medFordeltRefusjonPrÅr(kopierFraArbeidsforhold.getFordeltRefusjonPrÅr())
            .medMaksimalRefusjonPrÅr(kopierFraArbeidsforhold.getMaksimalRefusjonPrÅr())
            .medAvkortetRefusjonPrÅr(kopierFraArbeidsforhold.getAvkortetRefusjonPrÅr())
            .medRedusertRefusjonPrÅr(kopierFraArbeidsforhold.getRedusertRefusjonPrÅr(), ytelsedagerPrÅr)
            .medAvkortetBrukersAndelPrÅr(kopierFraArbeidsforhold.getAvkortetBrukersAndelPrÅr())
            .medRedusertBrukersAndelPrÅr(kopierFraArbeidsforhold.getRedusertBrukersAndelPrÅr(), ytelsedagerPrÅr)
            .medErTidsbegrensetArbeidsforhold(kopierFraArbeidsforhold.getTidsbegrensetArbeidsforhold())
            .medFastsattAvSaksbehandler(kopierFraArbeidsforhold.getFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(kopierFraArbeidsforhold.getLagtTilAvSaksbehandler())
            .medBeregningsperiode(kopierFraArbeidsforhold.getBeregningsperiode());

        return bgPrArbeidsforholdBuilder.build();
    }


}
