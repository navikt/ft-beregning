package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class SvangerskapspengerGrunnlag extends YtelsespesifiktGrunnlagDto {

    @JsonProperty(value = "utbetalingsgradPrAktivitet", required = true)
    @Size(min = 1)
    @Valid
    private List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;

    @JsonProperty(value = "tilkommetInntektHensyntasFom")
    @Valid
    private LocalDate tilkommetInntektHensyntasFom;


    protected SvangerskapspengerGrunnlag() {
        // default ctor
    }

    public SvangerskapspengerGrunnlag(@NotNull @Valid List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, LocalDate tilkommetInntektHensyntasFom) {
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
        this.tilkommetInntektHensyntasFom = tilkommetInntektHensyntasFom;
    }


    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        return utbetalingsgradPrAktivitet;
    }

    public LocalDate getTilkommetInntektHensyntasFom() {
        return tilkommetInntektHensyntasFom;
    }

    @Override
    public String toString() {
        return "SvangerskapspengerGrunnlag{" +
                "utbetalingsgradPrAktivitet=" + utbetalingsgradPrAktivitet +
                ", tilkommetInntektHensyntasFom=" + tilkommetInntektHensyntasFom +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SvangerskapspengerGrunnlag that = (SvangerskapspengerGrunnlag) o;
        return Objects.equals(utbetalingsgradPrAktivitet, that.utbetalingsgradPrAktivitet) && Objects.equals(tilkommetInntektHensyntasFom, that.tilkommetInntektHensyntasFom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), utbetalingsgradPrAktivitet, tilkommetInntektHensyntasFom);
    }
}
