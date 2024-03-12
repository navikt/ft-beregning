package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FordelBeregningsgrunnlagArbeidsforholdDto extends BeregningsgrunnlagArbeidsforholdDto {

    @Valid
    @JsonProperty(value = "perioderMedGraderingEllerRefusjon")
    @Size(max=100)
    @NotNull
    private List<NyPeriodeDto> perioderMedGraderingEllerRefusjon = new ArrayList<>();

    @Valid
    @JsonProperty(value = "permisjon")
    private PermisjonDto permisjon;

    public void leggTilPeriodeMedGraderingEllerRefusjon(NyPeriodeDto periodeMedGraderingEllerRefusjon) {
        this.perioderMedGraderingEllerRefusjon.add(periodeMedGraderingEllerRefusjon);
    }

    public List<NyPeriodeDto> getPerioderMedGraderingEllerRefusjon() {
        return perioderMedGraderingEllerRefusjon;
    }

    public void setPerioderMedGraderingEllerRefusjon(List<NyPeriodeDto> perioderMedGraderingEllerRefusjon) {
        this.perioderMedGraderingEllerRefusjon = perioderMedGraderingEllerRefusjon;
    }

    public PermisjonDto getPermisjon() {
        return permisjon;
    }

    public void setPermisjon(PermisjonDto permisjon) {
        this.permisjon = permisjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FordelBeregningsgrunnlagArbeidsforholdDto that = (FordelBeregningsgrunnlagArbeidsforholdDto) o;
        return Objects.equals(perioderMedGraderingEllerRefusjon, that.perioderMedGraderingEllerRefusjon)
            && Objects.equals(permisjon, that.permisjon);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), perioderMedGraderingEllerRefusjon);
    }
}
