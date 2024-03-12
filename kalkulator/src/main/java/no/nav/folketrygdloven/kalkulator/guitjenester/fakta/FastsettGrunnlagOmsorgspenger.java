package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnGradertBruttoForAndel;
import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

public class FastsettGrunnlagOmsorgspenger extends FastsettGrunnlagGenerell {

    @Override
    public boolean skalGrunnlagFastsettes(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (erBlittFastsattFør(andel)) {
            return true;
        }
        if (!harForeslåttBeregning(input.getBeregningsgrunnlagGrunnlag())) {
            return false;
        }
        if (erBrukerKunArbeidstaker(input) && finnesKunFullRefusjon(input)) {
            return false;
        }
        return super.skalGrunnlagFastsettes(input, andel);
    }

    public static boolean finnesKunFullRefusjon(BeregningsgrunnlagGUIInput input) {
        OmsorgspengerGrunnlag yg = input.getYtelsespesifiktGrunnlag();
        boolean finnesAtAndelIkkeSøktOm = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> AktivitetStatus.ARBEIDSTAKER.equals(a.getAktivitetStatus()))
                .anyMatch(a -> !erSøktOm(a, yg.getUtbetalingsgradPrAktivitet()));
        if (finnesAtAndelIkkeSøktOm) {
            return false;
        }
        if (harIngenKrav(input)) {
            return false;
        }
        return !girDirekteUtbetalingTilBruker(input, input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0));
    }

    private static boolean harIngenKrav(BeregningsgrunnlagGUIInput input) {
        return input.getInntektsmeldinger().stream().noneMatch(im -> im.getRefusjonBeløpPerMnd() != null && !im.getRefusjonBeløpPerMnd().erNullEller0());
    }

    private boolean erBlittFastsattFør(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getOverstyrtPrÅr() != null;
    }

    private static boolean girDirekteUtbetalingTilBruker(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagPeriodeDto periode) {
        if (!harForeslåttBeregning(input.getBeregningsgrunnlagGrunnlag())) {
            return false;
        }
        var grenseverdi6G = input.getBeregningsgrunnlag().getGrunnbeløp().multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());
        var gradertRefusjonVedSkjæringstidspunkt = finnGradertRefusjonskravPåSkjæringstidspunktet(input.getInntektsmeldinger(), input.getSkjæringstidspunktForBeregning(), input.getYtelsespesifiktGrunnlag());
        var lavesteGrenseRefusjon = grenseverdi6G.min(gradertRefusjonVedSkjæringstidspunkt);
        var totaltBeregningsgrunnlag = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .map(a -> finnGradertBruttoForAndel(a, periode.getPeriode(), input.getYtelsespesifiktGrunnlag()))
                .reduce(Beløp.ZERO, Beløp::adder);
        var avkortetTotaltGrunnlag = grenseverdi6G.min(totaltBeregningsgrunnlag);

        return lavesteGrenseRefusjon.compareTo(avkortetTotaltGrunnlag) < 0;
    }

    private static boolean erBrukerKunArbeidstaker(BeregningsgrunnlagGUIInput input) {
        return input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .allMatch(a -> AktivitetStatus.ARBEIDSTAKER.equals(a.getAktivitetStatus()));
    }

    private static boolean harForeslåttBeregning(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto) {
        return !beregningsgrunnlagGrunnlagDto.getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private static boolean erSøktOm(BeregningsgrunnlagPrStatusOgAndelDto andel, List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        return utbetalingsgradPrAktivitet.stream()
                .filter(utb -> utb.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.ORDINÆRT_ARBEID))
                .filter(utb -> utb.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().equals(andel.getArbeidsgiver()))
                .anyMatch(utb -> utb.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(andel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef())));
    }


}
