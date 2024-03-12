package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktivitetGraderingDto {

    @JsonProperty(value = "andelGraderingDto")
    @Valid
    @Size(min = 1)
    private List<AndelGraderingDto> andelGraderingDto;

    protected AktivitetGraderingDto() {
        // default ctor
    }

    public AktivitetGraderingDto(@Valid @NotEmpty List<AndelGraderingDto> andelGraderingDto) {
        this.andelGraderingDto = andelGraderingDto;
    }

    public List<AndelGraderingDto> getAndelGraderingDto() {
        return andelGraderingDto;
    }

    @Override
    public String toString() {
        return "AktivitetGraderingDto{" +
                "andelGraderingDto=" + andelGraderingDto +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AktivitetGraderingDto that = (AktivitetGraderingDto) o;
        return Objects.equals(andelGraderingDto, that.andelGraderingDto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(andelGraderingDto);
    }
}
