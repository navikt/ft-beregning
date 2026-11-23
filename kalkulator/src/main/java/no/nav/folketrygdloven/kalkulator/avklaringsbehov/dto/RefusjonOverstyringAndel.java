package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

public record RefusjonOverstyringAndel(String arbeidsgiverIdent,
                                       String arbeidsforholdId,
                                       RefusjonKilde kilde,
                                       List<RefusjonOverstyringPeriode> refusjonsperioder) {

    public record RefusjonOverstyringPeriode(String fom,
                                             String tom,
                                             BigDecimal refusjonPrMnd,
                                             RefusjonUtfallÅrsak utfallÅrsak) {
    }
}
