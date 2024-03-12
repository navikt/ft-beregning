package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public abstract class UtbetalingsgradGrunnlag {

    private final List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;
    private LocalDate tilkommetInntektHensyntasFom;

    public UtbetalingsgradGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
    }

    public UtbetalingsgradGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, LocalDate tilkommetInntektHensyntasFom) {
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
        this.tilkommetInntektHensyntasFom = tilkommetInntektHensyntasFom;
    }

    public Optional<LocalDate> getTilkommetInntektHensyntasFom() {
        return Optional.ofNullable(tilkommetInntektHensyntasFom);
    }

    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        return utbetalingsgradPrAktivitet;
    }

    public List<PeriodeMedUtbetalingsgradDto> finnUtbetalingsgraderForArbeid(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto) {
        return getUtbetalingsgradPrAktivitet()
                .stream()
                .filter(akt -> matchArbeidsgiver(arbeidsgiver, akt) && matcherArbeidsforholdReferanse(arbeidsforholdRefDto, akt))
                .flatMap(akt -> akt.getPeriodeMedUtbetalingsgrad().stream())
                .collect(Collectors.toList());
    }

    private static Boolean matchArbeidsgiver(Arbeidsgiver arbeidsgiver, UtbetalingsgradPrAktivitetDto akt) {
        return akt.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().map(Arbeidsgiver::getIdentifikator)
                .map(id -> id.equals(arbeidsgiver.getIdentifikator())).orElse(false);
    }


    private static boolean matcherArbeidsforholdReferanse(InternArbeidsforholdRefDto arbeidsforholdRefDto, UtbetalingsgradPrAktivitetDto utbGrad) {
        return utbGrad.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(arbeidsforholdRefDto);
    }

}
