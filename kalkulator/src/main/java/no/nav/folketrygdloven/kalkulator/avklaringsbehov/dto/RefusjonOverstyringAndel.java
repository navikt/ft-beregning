package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

public record RefusjonOverstyringAndel(Arbeidsgiver arbeidsgiverIdent,
                                       InternArbeidsforholdRefDto arbeidsforholdId,
                                       RefusjonKilde kilde,
                                       List<RefusjonOverstyringPeriode> refusjonsperioder) {

    public record RefusjonOverstyringPeriode(LocalDate fom,
                                             LocalDate tom,
                                             BigDecimal refusjonPrMnd,
                                             RefusjonUtfall utfall,
                                             RefusjonUtfallÅrsak utfallÅrsak) {
    }
}
