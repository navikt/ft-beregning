package no.nav.folketrygdloven.kalkulator.modell.iay;


import java.util.Objects;

public class InntektArbeidYtelseAggregatDto {

    private AktørInntektDto aktørInntekt;
    private AktørArbeidDto aktørArbeid;
    private AktørYtelseDto aktørYtelse;

    InntektArbeidYtelseAggregatDto() {
        // hibernate
    }

    /** copy constructor */
    InntektArbeidYtelseAggregatDto(InntektArbeidYtelseAggregatDto kopierFra) {
        this.setAktørInntekt(kopierFra.getAktørInntekt() == null ? null : new AktørInntektDto(kopierFra.getAktørInntekt()));
        this.setAktørArbeid(kopierFra.getAktørArbeid() == null ? null : new AktørArbeidDto(kopierFra.getAktørArbeid()));
        this.setAktørYtelse(kopierFra.getAktørYtelse() == null ? null : new AktørYtelseDto(kopierFra.getAktørYtelse()));
    }

    public AktørInntektDto getAktørInntekt() {
        return aktørInntekt;
    }

    void setAktørInntekt(AktørInntektDto aktørInntekt) {
        this.aktørInntekt = aktørInntekt;
    }

    public AktørArbeidDto getAktørArbeid() {
        return aktørArbeid;
    }

    void setAktørArbeid(AktørArbeidDto aktørArbeid) {
        this.aktørArbeid = aktørArbeid;
    }

    AktørYtelseDto getAktørYtelse() {
        return aktørYtelse;
    }

    void setAktørYtelse(AktørYtelseDto aktørYtelse) {
        this.aktørYtelse = aktørYtelse;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof InntektArbeidYtelseAggregatDto)) {
            return false;
        }
        InntektArbeidYtelseAggregatDto other = (InntektArbeidYtelseAggregatDto) obj;
        return Objects.equals(this.getAktørInntekt(), other.getAktørInntekt())
            && Objects.equals(this.getAktørArbeid(), other.getAktørArbeid())
            && Objects.equals(this.getAktørYtelse(), other.getAktørYtelse());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørInntekt, aktørArbeid, aktørYtelse);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "aktørInntekt=" + aktørInntekt +
            ", aktørArbeid=" + aktørArbeid +
            ", aktørYtelse=" + aktørYtelse +
            '>';
    }

}
