package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.util.Collection;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.felles.ytelseovergang.DirekteOvergangTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;

public class VurderMottarYtelseTjeneste {

    private VurderMottarYtelseTjeneste() {
        // Skjul
    }

    public static boolean skalVurdereMottattYtelse(BeregningsgrunnlagDto beregningsgrunnlag,
                                                   InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                   Collection<InntektsmeldingDto> inntektsmeldinger) {
        boolean erFrilanser = erFrilanser(beregningsgrunnlag);

        InntektFilterDto filter = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister());
        var ytelsefilter = new YtelseFilterDto(iayGrunnlag.getAktørYtelseFraRegister());
        filter = filter.filterSammenligningsgrunnlag();

        if (erFrilanser) {
            return mottarYtelseIBeregningsperiode(beregningsgrunnlag, filter, AktivitetStatus.FRILANSER) && harYtelseBasertPåFrilans(beregningsgrunnlag, ytelsefilter);
        }
        var arbeidstakerSomManglerInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
                .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, inntektsmeldinger);
        if (!arbeidstakerSomManglerInntektsmelding.isEmpty()) {
            return mottarYtelseIBeregningsperiode(beregningsgrunnlag, filter, AktivitetStatus.ARBEIDSTAKER) && harArbeidstakerMottattYtelseForArbeidsforholdUtenInntektsmelding(beregningsgrunnlag, ytelsefilter, arbeidstakerSomManglerInntektsmelding);
        }
        return false;
    }

    private static boolean harArbeidstakerMottattYtelseForArbeidsforholdUtenInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag, YtelseFilterDto ytelsefilter, Collection<BeregningsgrunnlagPrStatusOgAndelDto> arbeidstakerSomManglerInntektsmelding) {
        return arbeidstakerSomManglerInntektsmelding.stream().anyMatch(a -> harDirekteMottattYtelseForArbeidsgiver(beregningsgrunnlag, ytelsefilter, a.getArbeidsgiver().orElseThrow(() -> new IllegalStateException("Forventer å finne arbeidsgiver"))));
    }

    private static boolean mottarYtelseIBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag, InntektFilterDto filter, AktivitetStatus aktivitetsStatus) {
        Intervall beregningsPeriodeForStatus = finnBeregningsperiodeForAktivitetStatus(beregningsgrunnlag, aktivitetsStatus);
        return filter.getFiltrertInntektsposter().stream().anyMatch(inntektspostDto -> {
            boolean overlapperYtelseMedBeregningsgrunnlaget = beregningsPeriodeForStatus.overlapper(inntektspostDto.getPeriode());
            return InntektspostType.YTELSE.equals(inntektspostDto.getInntektspostType()) && overlapperYtelseMedBeregningsgrunnlaget;
        });
    }

    private static boolean harYtelseBasertPåFrilans(BeregningsgrunnlagDto beregningsgrunnlag,
                                                    YtelseFilterDto ytelseFilterDto) {
        if (!KonfigurasjonVerdi.instance().get("VURDER_MOTTAR_YTELSE_FL_FILTRERING", false)) {
            return true;
        }
        Intervall beregningsPeriodeForStatus = finnBeregningsperiodeForAktivitetStatus(beregningsgrunnlag, AktivitetStatus.FRILANSER);
        return ytelseFilterDto.getFiltrertYtelser().stream()
                .flatMap(y -> y.getYtelseAnvist().stream())
                .filter(ya -> ya.getAnvistPeriode().overlapper(beregningsPeriodeForStatus))
                .anyMatch(ya -> ya.getAnvisteAndeler().isEmpty() || ya.getAnvisteAndeler().stream().anyMatch(a -> a.getInntektskategori().equals(Inntektskategori.FRILANSER)));
    }

    private static boolean harDirekteMottattYtelseForArbeidsgiver(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                  YtelseFilterDto ytelseFilterDto, Arbeidsgiver arbeidsgiver) {
        if (!KonfigurasjonVerdi.instance().get("VURDER_MOTTAR_YTELSE_AT_FILTRERING", false)) {
            return true;
        }
        Intervall beregningsPeriodeForStatus = finnBeregningsperiodeForAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        return DirekteOvergangTjeneste.harDirekteMottattYtelseForArbeidsgiver(beregningsPeriodeForStatus, arbeidsgiver, ytelseFilterDto.getFiltrertYtelser());
    }

    public static boolean erFrilanser(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser());
    }

    public static Intervall finnBeregningsperiodeForAktivitetStatus(BeregningsgrunnlagDto beregningsgrunnlag, AktivitetStatus aktivitetsStatus) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> aktivitetsStatus.equals(andel.getAktivitetStatus())).map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregningsperiode).findFirst().orElseThrow(() -> new IllegalStateException("Fant ingen beregningsperiode for " + aktivitetsStatus.name()));
    }


}
