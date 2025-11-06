package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.time.LocalDate;
import java.util.Optional;

public class VurderRefusjonAndelBeregningsgrunnlagDto {

    private String arbeidsgiverOrgnr;
    private String arbeidsgiverAktørId;
    private String internArbeidsforholdRef;
    private LocalDate fastsattRefusjonFom;
    private Integer delvisRefusjonBeløpPrMnd;
    private Boolean erFristUtvidet;

    public VurderRefusjonAndelBeregningsgrunnlagDto() {
        // for builder
    }

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

    public VurderRefusjonAndelBeregningsgrunnlagDto(String arbeidsgiverOrgnr,
                                                    String arbeidsgiverAktørId,
                                                    String internArbeidsforholdRef,
                                                    LocalDate fastsattRefusjonFom,
                                                    Integer delvisRefusjonBeløpPrMnd,
                                                    Boolean erFristUtvidet) {
        this(arbeidsgiverOrgnr, arbeidsgiverAktørId, internArbeidsforholdRef, fastsattRefusjonFom, delvisRefusjonBeløpPrMnd);
        this.erFristUtvidet = erFristUtvidet;
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

    public Optional<Boolean> getErFristUtvidet() {
        return Optional.ofNullable(erFristUtvidet);
    }

    @Override
    public String toString() {
        return "VurderRefusjonAndelBeregningsgrunnlagDto{" +
                "arbeidsgiverOrgnr='" + sisteTreSifferOrgnr() + '\'' +
                ", arbeidsgiverAktørId='" + arbeidsgiverAktørId + '\'' +
                ", internArbeidsforholdRef='" + internArbeidsforholdRef + '\'' +
                ", fastsattRefusjonFom=" + fastsattRefusjonFom +
                ", delvisRefusjonBeløpPrMnd=" + delvisRefusjonBeløpPrMnd +
                ", erFristUtvidet=" + erFristUtvidet +
                '}';
    }

    private String sisteTreSifferOrgnr() {
        if (arbeidsgiverOrgnr == null) {
            return null;
        }
        var length = arbeidsgiverOrgnr.length();
        if (length <= 3) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 3) + arbeidsgiverOrgnr.substring(length - 3);
    }
}
