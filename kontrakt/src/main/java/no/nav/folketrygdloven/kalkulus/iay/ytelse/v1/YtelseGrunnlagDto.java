package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class YtelseGrunnlagDto {

    @JsonProperty(value = "arbeidskategori")
    private Arbeidskategori arbeidskategori;

    @JsonProperty(value = "fordeling")
    @Size
    @Valid
    private List<YtelseFordelingDto> fordeling;

    public YtelseGrunnlagDto() {
    }

    public YtelseGrunnlagDto(Arbeidskategori arbeidskategori,
                             List<YtelseFordelingDto> fordeling) {
        this.arbeidskategori = arbeidskategori;
        this.fordeling = fordeling;
    }


    public List<YtelseFordelingDto> getFordeling() {
        return fordeling;
    }

    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }

}
