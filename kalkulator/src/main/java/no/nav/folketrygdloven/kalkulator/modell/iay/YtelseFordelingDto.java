package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.RoundingMode;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektPeriodeType;

public class YtelseFordelingDto {

    private Beløp beløp;
    private InntektPeriodeType inntektPeriodeType;
    private Arbeidsgiver arbeidsgiver;
    private Boolean erRefusjon;

    protected YtelseFordelingDto() {
    }

    public YtelseFordelingDto(Arbeidsgiver arbeidsgiver, InntektPeriodeType inntektPeriodeType, int beløp, Boolean erRefusjon) {
        this(arbeidsgiver, inntektPeriodeType, Beløp.fra(beløp), erRefusjon);
    }

    public YtelseFordelingDto(Arbeidsgiver arbeidsgiver, InntektPeriodeType inntektPeriodeType, Beløp beløp, Boolean erRefusjon) {
        this.arbeidsgiver = arbeidsgiver;
        this.inntektPeriodeType = inntektPeriodeType;
        this.beløp = Optional.ofNullable(Beløp.safeVerdi(beløp)).map(b -> b.setScale(2, RoundingMode.HALF_UP)).map(Beløp::fra).orElse(null);
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public InntektPeriodeType getHyppighet() {
        return inntektPeriodeType;
    }

    public Boolean getErRefusjon() {
        return erRefusjon;
    }
}
