package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.iay.IayProsent;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

/**
 * Angir størrelse for ytelse.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class AnvistAndel {

    /**
     * Kan være null.
     */
    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty("arbeidsforholdId")
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdId;

    @JsonProperty(value = "dagsats", required = true)
    @NotNull
    private Beløp dagsats;

    @JsonProperty("utbetalingsgrad")
    private IayProsent utbetalingsgrad;

    // Andel av dagsats som utbetales til arbeidsgiver
    @JsonProperty("refusjonsgrad")
    private IayProsent refusjonsgrad;

    @JsonProperty(value = "inntektskategori")
    @Valid
    private Inntektskategori inntektskategori;

    protected AnvistAndel() {
    }

    public AnvistAndel(Aktør arbeidsgiver, int beløp, int utbetalingsgrad, int refusjonsgrad, Inntektskategori inntektskategori, String arbeidsforholdId) {
        this(arbeidsgiver,
                new InternArbeidsforholdRefDto(arbeidsforholdId),
                Beløp.fra(beløp),
                IayProsent.fra(utbetalingsgrad),
                IayProsent.fra(refusjonsgrad),
                inntektskategori);
    }

    public AnvistAndel(Aktør arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdId,
                       Beløp beløp, IayProsent utbetalingsgrad, IayProsent refusjonsgrad, Inntektskategori inntektskategori) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdId = arbeidsforholdId;
        this.dagsats = beløp;
        this.utbetalingsgrad = utbetalingsgrad;
        this.refusjonsgrad = refusjonsgrad;
        this.inntektskategori = inntektskategori;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public Beløp getDagsats() {
        return dagsats;
    }

    public IayProsent getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public IayProsent getRefusjonsgrad() {
        return refusjonsgrad;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
