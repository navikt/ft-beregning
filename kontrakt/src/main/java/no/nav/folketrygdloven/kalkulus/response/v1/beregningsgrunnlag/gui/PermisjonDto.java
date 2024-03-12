package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class PermisjonDto {

    @Valid
    @JsonProperty(value = "permisjonFom")
    @NotNull
    private LocalDate permisjonFom;

    @Valid
    @JsonProperty(value = "permisjonTom")
    @NotNull
    private LocalDate permisjonTom;


    PermisjonDto(){
        // Skjul default constructor
    }



    public PermisjonDto(LocalDate permisjonFom, LocalDate permisjonTom) {
        this.permisjonFom = permisjonFom;
        this.permisjonTom = permisjonTom;
    }

    public PermisjonDto(PermisjonDto p) {
        this.permisjonFom = p.permisjonFom;
        this.permisjonTom = p.permisjonTom;
    }

    public LocalDate getPermisjonFom() {
        return permisjonFom;
    }

    public void setPermisjonFom(LocalDate permisjonFom) {
        this.permisjonFom = permisjonFom;
    }

    public LocalDate getPermisjonTom() {
        return permisjonTom;
    }

    public void setPermisjonTom(LocalDate permisjonTom) {
        this.permisjonTom = permisjonTom;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermisjonDto that = (PermisjonDto) o;
        return Objects.equals(permisjonFom, that.permisjonFom)
            && Objects.equals(permisjonTom, that.permisjonTom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permisjonFom, permisjonTom);
    }

}
