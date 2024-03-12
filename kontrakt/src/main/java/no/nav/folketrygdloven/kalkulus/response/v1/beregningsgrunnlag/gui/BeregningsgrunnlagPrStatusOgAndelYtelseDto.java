package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;


@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE, setterVisibility= JsonAutoDetect.Visibility.NONE, fieldVisibility= JsonAutoDetect.Visibility.ANY)
public class BeregningsgrunnlagPrStatusOgAndelYtelseDto extends BeregningsgrunnlagPrStatusOgAndelDto {
    @Valid
    @JsonProperty("belopFraMeldekortPrMnd")
    private Beløp belopFraMeldekortPrMnd;

    @Valid
    @JsonProperty("belopFraMeldekortPrAar")
    private Beløp belopFraMeldekortPrAar;

    @Valid
    @JsonProperty("oppjustertGrunnlag")
    private Beløp oppjustertGrunnlag;

    public BeregningsgrunnlagPrStatusOgAndelYtelseDto() {
        super();
    }

    public Beløp getBelopFraMeldekortPrMnd() {
        return belopFraMeldekortPrMnd;
    }

    public Beløp getBelopFraMeldekortPrAar() {
        return belopFraMeldekortPrAar;
    }

    public void setBelopFraMeldekortPrMnd(Beløp belopFraMeldekortPrMnd) {
        this.belopFraMeldekortPrMnd = belopFraMeldekortPrMnd;
    }

    public void setBelopFraMeldekortPrAar(Beløp belopFraMeldekortPrAar) {
        this.belopFraMeldekortPrAar = belopFraMeldekortPrAar;
    }

    public Beløp getOppjustertGrunnlag() {
        return oppjustertGrunnlag;
    }

    public void setOppjustertGrunnlag(Beløp oppjustertGrunnlag) {
        this.oppjustertGrunnlag = oppjustertGrunnlag;
    }
}
