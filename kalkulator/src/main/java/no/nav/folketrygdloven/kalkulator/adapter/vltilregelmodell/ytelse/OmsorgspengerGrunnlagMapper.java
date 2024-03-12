package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse;

import java.util.Collection;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;


public class OmsorgspengerGrunnlagMapper {

    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlagDto, BeregningsgrunnlagInput input) {
        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        if (ytelsespesifiktGrunnlag instanceof no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag omsorspengegrunnlag) {
            List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = omsorspengegrunnlag.getUtbetalingsgradPrAktivitet();
            var finnesArbeidsandelIkkeSøktOm = finnesArbeidsandelIkkeSøktOm(utbetalingsgradPrAktivitet, beregningsgrunnlagDto);
            if (omsorspengegrunnlag.getBrukerSøkerPerioder().isPresent()) {
                return new OmsorgspengerGrunnlag(finnesArbeidsandelIkkeSøktOm, !omsorspengegrunnlag.getBrukerSøkerPerioder().get().isEmpty());
            } else {
                boolean harSøktFLEllerSN = utbetalingsgradPrAktivitet.stream()
                        .filter(this::erFrilansEllerNæring)
                        .anyMatch(this::harUtbetaling);
                return new OmsorgspengerGrunnlag(finnesArbeidsandelIkkeSøktOm,
                        harSøktFLEllerSN || !harAlleInntektsmeldingerFulltRefusjonskrav(input.getInntektsmeldinger())
                );
            }
        }

        throw new IllegalStateException("Forventer OmsorgspengerGrunnlag for OMP");
    }

    private boolean harAlleInntektsmeldingerFulltRefusjonskrav(Collection<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream().allMatch(im -> im.getRefusjonBeløpPerMnd() != null &&
                im.getRefusjonBeløpPerMnd().equals(im.getInntektBeløp()));
    }

    private boolean harUtbetaling(UtbetalingsgradPrAktivitetDto aktivitet) {
        return aktivitet.getPeriodeMedUtbetalingsgrad().stream()
                .anyMatch(p -> p.getUtbetalingsgrad().compareTo(Utbetalingsgrad.ZERO) > 0);
    }

    private boolean erFrilansEllerNæring(UtbetalingsgradPrAktivitetDto aktivitet) {
        return aktivitet.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.FRILANS)
                || aktivitet.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    private boolean finnesArbeidsandelIkkeSøktOm(List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader, BeregningsgrunnlagDto beregningsgrunnlagDto) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        return andeler.stream()
                .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
                .anyMatch(andel -> !erSøktOm(utbetalingsgrader, andel));
    }

    private boolean erSøktOm(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return utbetalingsgradPrAktivitet.stream()
                .filter(utb -> utb.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.ORDINÆRT_ARBEID))
                .filter(utb -> utb.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().equals(andel.getArbeidsgiver()))
                .anyMatch(utb -> utb.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(andel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef())));
    }

}
