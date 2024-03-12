package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.LønnsinntektBeskrivelse;
import no.nav.folketrygdloven.kalkulus.kodeverk.SkatteOgAvgiftsregelType;

public class InntektspostDto {

    private InntektspostType inntektspostType;
    private SkatteOgAvgiftsregelType skatteOgAvgiftsregelType = SkatteOgAvgiftsregelType.UDEFINERT;
    private InntektDto inntekt;
    private LønnsinntektBeskrivelse lønnsinnntektBeskrivelse = LønnsinntektBeskrivelse.UDEFINERT;
    private Intervall periode;
    private Beløp beløp;
    private InntektYtelseType inntektYtelseType;

    public InntektspostDto() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    InntektspostDto(InntektspostDto inntektspost) {
        this.inntektspostType = inntektspost.getInntektspostType();
        this.skatteOgAvgiftsregelType = inntektspost.getSkatteOgAvgiftsregelType();
        this.periode = inntektspost.getPeriode();
        this.beløp = inntektspost.getBeløp();
        this.inntektYtelseType = inntektspost.getInntektYtelseType();
        this.lønnsinnntektBeskrivelse = inntektspost.getLønnsinnntektBeskrivelse();
    }

    /**
     * Underkategori av utbetaling
     * <p>
     * F.eks
     * <ul>
     * <li>Lønn</li>
     * <li>Ytelse</li>
     * <li>Næringsinntekt</li>
     * </ul>
     *
     * @return {@link InntektspostType}
     */
    public InntektspostType getInntektspostType() {
        return inntektspostType;
    }

    void setInntektspostType(InntektspostType inntektspostType) {
        this.inntektspostType = inntektspostType;
    }

    /**
     * En kodeverksverdi som angir særskilt beskatningsregel.
     * Den er ikke alltid satt, og kommer fra inntektskomponenten
     *
     * @return {@link SkatteOgAvgiftsregelType}
     */
    public SkatteOgAvgiftsregelType getSkatteOgAvgiftsregelType() {
        return skatteOgAvgiftsregelType;
    }

    void setSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType skatteOgAvgiftsregelType) {
        this.skatteOgAvgiftsregelType = skatteOgAvgiftsregelType;
    }

    public LønnsinntektBeskrivelse getLønnsinnntektBeskrivelse() {
        return lønnsinnntektBeskrivelse;
    }


    public void setLønnsinnntektBeskrivelse(LønnsinntektBeskrivelse lønnsinnntektBeskrivelse) {
        this.lønnsinnntektBeskrivelse = lønnsinnntektBeskrivelse;
    }

    void setPeriode(LocalDate fom, LocalDate tom) {
        this.periode = Intervall.fraOgMedTilOgMed(fom, tom);
    }

    public Intervall getPeriode() {
        return periode;
    }

    /**
     * Beløpet som har blitt utbetalt i perioden
     *
     * @return Beløpet
     */
    public Beløp getBeløp() {
        return beløp;
    }

    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    public InntektDto getInntekt() {
        return inntekt;
    }

    void setInntekt(InntektDto inntekt) {
        this.inntekt = inntekt;
    }

    public InntektYtelseType getInntektYtelseType() {
        return inntektYtelseType;
    }

    public void setInntektYtelseType(InntektYtelseType inntektYtelseType) {
        this.inntektYtelseType = inntektYtelseType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof InntektspostDto)) {
            return false;
        }
        InntektspostDto other = (InntektspostDto) obj;
        return Objects.equals(this.getInntektspostType(), other.getInntektspostType())
                && Objects.equals(this.getInntektYtelseType(), other.getInntektYtelseType())
                && Objects.equals(this.getSkatteOgAvgiftsregelType(), other.getSkatteOgAvgiftsregelType())
                && Objects.equals(this.getPeriode().getFomDato(), other.getPeriode().getFomDato())
                && Objects.equals(this.getPeriode().getTomDato(), other.getPeriode().getTomDato());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInntektspostType(), getInntektYtelseType(), getSkatteOgAvgiftsregelType(), getPeriode().getFomDato(), getPeriode().getTomDato());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
                "inntektYtelseType=" + inntektYtelseType +
                "inntektspostType=" + inntektspostType +
                "skatteOgAvgiftsregelType=" + skatteOgAvgiftsregelType +
                ", fraOgMed=" + periode.getFomDato() +
                ", tilOgMed=" + periode.getTomDato() +
                ", beløp=" + beløp +
                '>';
    }

    public boolean hasValues() {
        return inntektspostType != null || periode.getFomDato() != null || periode.getTomDato() != null || beløp != null;
    }

}
