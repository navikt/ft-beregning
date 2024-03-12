package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.time.LocalDate;

public class VurderRefusjonAndelBeregningsgrunnlagDto {

    private String arbeidsgiverOrgnr;
    private String arbeidsgiverAktørId;
    private String internArbeidsforholdRef;
    private LocalDate fastsattRefusjonFom;
    private Integer delvisRefusjonBeløpPrMnd;


    public VurderRefusjonAndelBeregningsgrunnlagDto(String arbeidsgiverOrgnr,
                                                    String arbeidsgiverAktørId,
                                                    String internArbeidsforholdRef,
                                                    LocalDate fastsattRefusjonFom,
                                                    Integer delvisRefusjonBeløpPrMnd) {
        if (arbeidsgiverAktørId == null && arbeidsgiverOrgnr == null) {
            throw new IllegalStateException("Både orgnr og aktørId er null, udyldig tilstand");
        }
        if (arbeidsgiverAktørId != null && arbeidsgiverOrgnr != null) {
            throw new IllegalStateException("Både orgnr og aktørId er satt, udyldig tilstand");
        }
        this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
        this.internArbeidsforholdRef = internArbeidsforholdRef;
        this.fastsattRefusjonFom = fastsattRefusjonFom;
        this.delvisRefusjonBeløpPrMnd = delvisRefusjonBeløpPrMnd;
    }

    public String getArbeidsgiverOrgnr() {
        return arbeidsgiverOrgnr;
    }

    public String getArbeidsgiverAktørId() {
        return arbeidsgiverAktørId;
    }

    public String getInternArbeidsforholdRef() {
        return internArbeidsforholdRef;
    }

    public LocalDate getFastsattRefusjonFom() {
        return fastsattRefusjonFom;
    }

    public Integer getDelvisRefusjonBeløpPrMnd() {
        return delvisRefusjonBeløpPrMnd;
    }

    @Override
    public String toString() {
        return "VurderRefusjonAndelBeregningsgrunnlagDto{" +
                "arbeidsgiverOrgnr='" + sisteTreSifferOrgnr() + '\'' +
                ", arbeidsgiverAktørId='" + arbeidsgiverAktørId + '\'' +
                ", internArbeidsforholdRef='" + internArbeidsforholdRef + '\'' +
                ", fastsattRefusjonFom=" + fastsattRefusjonFom +
                ", delvisRefusjonBeløpPrMnd=" + delvisRefusjonBeløpPrMnd +
                '}';
    }

    private String sisteTreSifferOrgnr() {
        if (arbeidsgiverOrgnr == null) {
            return null;
        }
        int length = arbeidsgiverOrgnr.length();
        if (length <= 3) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 3) + arbeidsgiverOrgnr.substring(length - 3);
    }

}
