package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

public class OppgittEgenNæringDto implements OppgittPeriodeInntekt {

    private Intervall periode;
    private OrgNummer virksomhetOrgnr;
    private VirksomhetType virksomhetType = VirksomhetType.UDEFINERT;
    private String regnskapsførerNavn;
    private String regnskapsførerTlf;
    private LocalDate endringDato;
    private String begrunnelse;
    private Beløp bruttoInntekt;

    private boolean nyoppstartet;
    private boolean varigEndring;
    private boolean nyIArbeidslivet;

    OppgittEgenNæringDto() {
    }

    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    @Override
    public Intervall getPeriode() {
        return periode;
    }

    @Override
    public Beløp getInntekt() {
        return bruttoInntekt;
    }

    public VirksomhetType getVirksomhetType() {
        return virksomhetType;
    }

    void setVirksomhetType(VirksomhetType virksomhetType) {
        this.virksomhetType = virksomhetType;
    }

    /** Samme som {@link #getVirksomhetOrgnr()} men returnerer string.*/
    public String getOrgnr() {
        return virksomhetOrgnr == null ? null : virksomhetOrgnr.getId();
    }

    public OrgNummer getVirksomhetOrgnr() {
        return virksomhetOrgnr;
    }

    void setVirksomhetOrgnr(OrgNummer orgNr) {
        this.virksomhetOrgnr = orgNr;
    }

    public String getRegnskapsførerNavn() {
        return regnskapsførerNavn;
    }

    void setRegnskapsførerNavn(String regnskapsførerNavn) {
        this.regnskapsførerNavn = regnskapsførerNavn;
    }

    public String getRegnskapsførerTlf() {
        return regnskapsførerTlf;
    }

    void setRegnskapsførerTlf(String regnskapsførerTlf) {
        this.regnskapsførerTlf = regnskapsførerTlf;
    }

    public LocalDate getEndringDato() {
        return endringDato;
    }

    void setEndringDato(LocalDate endringDato) {
        this.endringDato = endringDato;
    }

    public Beløp getBruttoInntekt() {
        return bruttoInntekt;
    }

    void setBruttoInntekt(Beløp bruttoInntekt) {
        this.bruttoInntekt = bruttoInntekt;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public boolean getNyoppstartet() {
        return nyoppstartet;
    }

    void setNyoppstartet(boolean nyoppstartet) {
        this.nyoppstartet = nyoppstartet;
    }

    void setNyIArbeidslivet(boolean nyIArbeidslivet) {
        this.nyIArbeidslivet = nyIArbeidslivet;
    }

    public boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    public boolean getVarigEndring() {
        return varigEndring;
    }

    void setVarigEndring(boolean varigEndring) {
        this.varigEndring = varigEndring;
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof OppgittEgenNæringDto))
            return false;
        OppgittEgenNæringDto that = (OppgittEgenNæringDto) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(virksomhetOrgnr, that.virksomhetOrgnr) &&
            Objects.equals(nyoppstartet, that.nyoppstartet) &&
            Objects.equals(virksomhetType, that.virksomhetType) &&
            Objects.equals(regnskapsførerNavn, that.regnskapsførerNavn) &&
            Objects.equals(regnskapsførerTlf, that.regnskapsførerTlf) &&
            Objects.equals(endringDato, that.endringDato) &&
            Objects.equals(begrunnelse, that.begrunnelse) &&
            Objects.equals(bruttoInntekt, that.bruttoInntekt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, virksomhetOrgnr, virksomhetType, nyoppstartet, regnskapsførerNavn, regnskapsførerTlf, endringDato, begrunnelse,
            bruttoInntekt);
    }

    @Override
    public String toString() {
        return "EgenNæringEntitet{" +
            "periode=" + periode +
            ", virksomhet=" + virksomhetOrgnr +
            ", nyoppstartet=" + nyoppstartet +
            ", virksomhetType=" + virksomhetType +
            ", regnskapsførerNavn='" + regnskapsførerNavn + '\'' +
            ", regnskapsførerTlf='" + regnskapsførerTlf + '\'' +
            ", endringDato=" + endringDato +
            ", begrunnelse='" + begrunnelse + '\'' +
            ", bruttoInntekt=" + bruttoInntekt +
            '}';
    }
}
