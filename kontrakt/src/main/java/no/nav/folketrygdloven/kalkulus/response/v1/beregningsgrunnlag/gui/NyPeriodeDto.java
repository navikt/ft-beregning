package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class NyPeriodeDto {

    @Valid
    @JsonProperty("erRefusjon")
    private boolean erRefusjon;

    @Valid
    @JsonProperty("erGradering")
    private boolean erGradering;

    @Valid
    @JsonProperty("erSøktYtelse")
    private boolean erSøktYtelse;

    @Valid
    @JsonProperty("fom")
    private LocalDate fom;

    @Valid
    @JsonProperty("tom")
    private LocalDate tom;

    public NyPeriodeDto() {
        // For Json serialisering
    }

    public NyPeriodeDto(boolean erRefusjon, boolean erGradering, boolean erSøktYtelse) {
        if ((erRefusjon && erGradering) || (!erRefusjon && !erGradering && !erSøktYtelse)) {
            throw new IllegalArgumentException("Må gjelde enten gradering, refusjon eller søkt ytelse");
        }
        this.erSøktYtelse = erSøktYtelse;
        this.erGradering = erGradering;
        this.erRefusjon = erRefusjon;
    }

    public boolean isErRefusjon() {
        return erRefusjon;
    }

    public void setErRefusjon(boolean erRefusjon) {
        this.erRefusjon = erRefusjon;
    }

    public boolean isErGradering() {
        return erGradering;
    }

    public void setErGradering(boolean erGradering) {
        this.erGradering = erGradering;
    }

    public boolean isErSøktYtelse() {
        return erSøktYtelse;
    }

    public void setErSøktYtelse(boolean erSøktYtelse) {
        this.erSøktYtelse = erSøktYtelse;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }
}
