package no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Ytelseandel {

    @JsonProperty(value = "aktivitetStatus")
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "inntektskategori")
    @Valid
    private Inntektskategori inntektskategori;

    @JsonProperty(value = "arbeidskategori")
    @Valid
    private Arbeidskategori arbeidskategori;

    @JsonProperty(value = "dagsats")
    @Valid
    private Long dagsats;

    public Ytelseandel(@Valid AktivitetStatus aktivitetStatus,
                       @Valid Inntektskategori inntektskategori,
                       @Valid Arbeidskategori arbeidskategori,
                       @Valid Long dagsats) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidskategori = arbeidskategori;
        this.dagsats = dagsats;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
