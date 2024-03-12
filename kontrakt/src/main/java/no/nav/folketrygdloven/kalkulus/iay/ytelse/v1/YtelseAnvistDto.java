package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.IayProsent;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelseAnvistDto {

    @JsonProperty("anvistPeriode")
    @Valid
    private Periode anvistPeriode;

    @JsonProperty("beløp")
    @Valid
    private Beløp beløp;

    @JsonProperty("dagsats")
    @Valid
    private Beløp dagsats;

    @JsonProperty("utbetalingsgradProsent")
    @Valid
    private IayProsent utbetalingsgradProsent;

    @JsonProperty("anvisteAndeler")
    @Size()
    @Valid
    private List<AnvistAndel> anvisteAndeler;


    public YtelseAnvistDto() {
        // default ctor
    }

    public YtelseAnvistDto(Periode anvistPeriode,
                           Beløp beløp,
                           Beløp dagsats,
                           IayProsent utbetalingsgradProsent,
                           List<AnvistAndel> anvisteAndeler) {
        this.anvistPeriode = anvistPeriode;
        this.beløp = beløp;
        this.dagsats = dagsats;
        this.utbetalingsgradProsent = utbetalingsgradProsent;
        this.anvisteAndeler = anvisteAndeler;
    }

    public Periode getAnvistPeriode() {
        return anvistPeriode;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public Beløp getDagsats() {
        return dagsats;
    }

    public IayProsent getUtbetalingsgradProsent() {
        return utbetalingsgradProsent;
    }

    public List<AnvistAndel> getAnvisteAndeler() {
        return anvisteAndeler;
    }
}
