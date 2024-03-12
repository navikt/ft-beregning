package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonAndelTilVurderingDto {

    @Valid
    @JsonProperty("aktivitetStatus")
    @NotNull
    private AktivitetStatus aktivitetStatus;

    @Valid
    @JsonProperty("tidligereUtbetalinger")
    @Size(min = 1)
    private List<TidligereUtbetalingDto> tidligereUtbetalinger;

    @Valid
    @JsonProperty("nyttRefusjonskravFom")
    private LocalDate nyttRefusjonskravFom;

    @Valid
    @JsonProperty("fastsattNyttRefusjonskravFom")
    private LocalDate fastsattNyttRefusjonskravFom;

    @Valid
    @JsonProperty("tidligsteMuligeRefusjonsdato")
    private LocalDate tidligsteMuligeRefusjonsdato;

    @Valid
    @JsonProperty("arbeidsgiver")
    private Arbeidsgiver arbeidsgiver;

    @JsonProperty(value = "internArbeidsforholdRef")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String internArbeidsforholdRef;

    @JsonProperty(value = "eksternArbeidsforholdRef")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String eksternArbeidsforholdRef;

    @Valid
    @JsonProperty("skalKunneFastsetteDelvisRefusjon")
    private boolean skalKunneFastsetteDelvisRefusjon;

    @Valid
    @JsonProperty("fastsattDelvisRefusjonPrMnd")
    private Beløp fastsattDelvisRefusjonPrMnd;

    @Valid
    @JsonProperty("maksTillattDelvisRefusjonPrMnd")
    private Beløp maksTillattDelvisRefusjonPrMnd;

    public RefusjonAndelTilVurderingDto() {
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public List<TidligereUtbetalingDto> getTidligereUtbetalinger() {
        return tidligereUtbetalinger;
    }

    public void setTidligereUtbetalinger(List<TidligereUtbetalingDto> tidligereUtbetalinger) {
        this.tidligereUtbetalinger = tidligereUtbetalinger;
    }

    public LocalDate getNyttRefusjonskravFom() {
        return nyttRefusjonskravFom;
    }

    public void setNyttRefusjonskravFom(LocalDate nyttRefusjonskravFom) {
        this.nyttRefusjonskravFom = nyttRefusjonskravFom;
    }

    public LocalDate getFastsattNyttRefusjonskravFom() {
        return fastsattNyttRefusjonskravFom;
    }

    public void setFastsattNyttRefusjonskravFom(LocalDate fastsattNyttRefusjonskravFom) {
        this.fastsattNyttRefusjonskravFom = fastsattNyttRefusjonskravFom;
    }

    public String getInternArbeidsforholdRef() {
        return internArbeidsforholdRef;
    }

    public void setInternArbeidsforholdRef(String internArbeidsforholdRef) {
        this.internArbeidsforholdRef = internArbeidsforholdRef;
    }

    public String getEksternArbeidsforholdRef() {
        return eksternArbeidsforholdRef;
    }

    public void setEksternArbeidsforholdRef(String eksternArbeidsforholdRef) {
        this.eksternArbeidsforholdRef = eksternArbeidsforholdRef;
    }

    public LocalDate getTidligsteMuligeRefusjonsdato() {
        return tidligsteMuligeRefusjonsdato;
    }

    public void setTidligsteMuligeRefusjonsdato(LocalDate tidligsteMuligeRefusjonsdato) {
        this.tidligsteMuligeRefusjonsdato = tidligsteMuligeRefusjonsdato;
    }

    public boolean getSkalKunneFastsetteDelvisRefusjon() {
        return skalKunneFastsetteDelvisRefusjon;
    }

    public void setSkalKunneFastsetteDelvisRefusjon(boolean skalKunneFastsetteDelvisRefusjon) {
        this.skalKunneFastsetteDelvisRefusjon = skalKunneFastsetteDelvisRefusjon;
    }

    public Beløp getFastsattDelvisRefusjonPrMnd() {
        return fastsattDelvisRefusjonPrMnd;
    }

    public void setFastsattDelvisRefusjonPrMnd(Beløp fastsattDelvisRefusjonPrMnd) {
        this.fastsattDelvisRefusjonPrMnd = fastsattDelvisRefusjonPrMnd;
    }

    public Beløp getMaksTillattDelvisRefusjonPrMnd() {
        return maksTillattDelvisRefusjonPrMnd;
    }

    public void setMaksTillattDelvisRefusjonPrMnd(Beløp maksTillattDelvisRefusjonPrMnd) {
        this.maksTillattDelvisRefusjonPrMnd = maksTillattDelvisRefusjonPrMnd;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonAndelTilVurderingDto that = (RefusjonAndelTilVurderingDto) o;
        return Objects.equals(aktivitetStatus, that.aktivitetStatus) &&
                Objects.equals(tidligereUtbetalinger, that.tidligereUtbetalinger) &&
                Objects.equals(nyttRefusjonskravFom, that.nyttRefusjonskravFom) &&
                Objects.equals(fastsattNyttRefusjonskravFom, that.fastsattNyttRefusjonskravFom) &&
                Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(skalKunneFastsetteDelvisRefusjon, that.skalKunneFastsetteDelvisRefusjon) &&
                Objects.equals(tidligsteMuligeRefusjonsdato, that.tidligsteMuligeRefusjonsdato) &&
                Objects.equals(internArbeidsforholdRef, that.internArbeidsforholdRef) &&
                Objects.equals(eksternArbeidsforholdRef, that.eksternArbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus,
                tidligereUtbetalinger,
                nyttRefusjonskravFom,
                fastsattNyttRefusjonskravFom,
                arbeidsgiver,
                skalKunneFastsetteDelvisRefusjon,
                internArbeidsforholdRef,
                eksternArbeidsforholdRef,
                tidligsteMuligeRefusjonsdato);
    }
}
