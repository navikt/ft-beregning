package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.time.Period;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InntektPeriodeType implements Kodeverdi, KontraktKode {

    DAGLIG("DAGLG", Period.ofDays(1)),
    UKENTLIG("UKNLG", Period.ofWeeks(1)),
    BIUKENTLIG("14DLG", Period.ofWeeks(2)),
    MÅNEDLIG("MNDLG", Period.ofMonths(1)),
    ÅRLIG("AARLG", Period.ofYears(1)),
    FASTSATT25PAVVIK("INNFS", Period.ofYears(1)),
    PREMIEGRUNNLAG("PREMGR", Period.ofYears(1)),
    UDEFINERT(KodeKonstanter.UDEFINERT, null),
    ;


    @JsonValue
    private final String kode;
    @JsonIgnore
    private final Period periode;

    InntektPeriodeType(String kode, Period periode) {
        this.kode = kode;
        this.periode = periode;
    }


    @Override
    public String getKode() {
        return kode;
    }

    public Period getPeriode() {
        return periode;
    }


}
