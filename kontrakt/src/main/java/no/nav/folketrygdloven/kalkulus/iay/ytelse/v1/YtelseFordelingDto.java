package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektPeriodeType;

/** Angir hyppighet og størrelse for ytelse. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class YtelseFordelingDto {

    /** Tillater kun positive verdier. Max verdi håndteres av mottager. */
    @JsonProperty(value = "beløp", required = true)
    @Valid
    @NotNull
    private Beløp beløp;

    /** Angir hvilken periode beløp gjelder for. */
    @JsonProperty(value = "inntektPeriodeType", required = true)
    @NotNull
    private InntektPeriodeType inntektPeriodeType;

    /** Kan være null. */
    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    /** Kan være null. */
    @JsonProperty(value = "erRefusjon")
    @Valid
    private Boolean erRefusjon;

    protected YtelseFordelingDto() {
    }

    public YtelseFordelingDto(Aktør arbeidsgiver, InntektPeriodeType inntektPeriodeType, BigDecimal beløp, Boolean erRefusjon) {
        this(arbeidsgiver, inntektPeriodeType, beløp == null ? null : Beløp.fra(beløp.setScale(2, RoundingMode.HALF_UP)), erRefusjon);
    }

    public YtelseFordelingDto(Aktør arbeidsgiver, InntektPeriodeType inntektPeriodeType, Beløp beløp, Boolean erRefusjon) {
        this.arbeidsgiver = arbeidsgiver;
        this.inntektPeriodeType = inntektPeriodeType;
        this.beløp = beløp;
        this.erRefusjon = erRefusjon;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public InntektPeriodeType getHyppighet() {
        return inntektPeriodeType;
    }

    public Boolean getErRefusjon() {
        return erRefusjon;
    }
}
