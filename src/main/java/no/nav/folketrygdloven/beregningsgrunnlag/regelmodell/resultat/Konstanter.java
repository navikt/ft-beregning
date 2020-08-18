package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;

/**
 * Samling av ulike konstanter og toggler som brukes i reglene
 */
public class Konstanter {

    // Konstanter

    private int antallGMilitærHarKravPå = 3;
    private BigDecimal antallGØvreGrenseverdi = BigDecimal.valueOf(6);
    private BigDecimal antallGMinstekravVilkår = BigDecimal.valueOf(0.5);
    private BigDecimal ytelsedagerIPrÅr = BigDecimal.valueOf(260);
    private BigDecimal avviksgrenseProsent = BigDecimal.valueOf(25);
    private List<Grunnbeløp> grunnbeløpSatser = new ArrayList<>();

    // Toggler

    private boolean splitteATFLToggleErPå = false;

    void setAntallGMilitærHarKravPå(int antallGMilitærHarKravPå) {
        this.antallGMilitærHarKravPå = antallGMilitærHarKravPå;
    }

    void setAntallGØvreGrenseverdi(BigDecimal antallGØvreGrenseverdi) {
        this.antallGØvreGrenseverdi = antallGØvreGrenseverdi;
    }

    void setAntallGMinstekravVilkår(BigDecimal antallGMinstekravVilkår) {
        this.antallGMinstekravVilkår = antallGMinstekravVilkår;
    }

    void setYtelsedagerIPrÅr(BigDecimal ytelsedagerIPrÅr) {
        this.ytelsedagerIPrÅr = ytelsedagerIPrÅr;
    }

    void setAvviksgrenseProsent(BigDecimal avviksgrenseProsent) {
        this.avviksgrenseProsent = avviksgrenseProsent;
    }

    void setSplitteATFLToggleErPå(boolean splitteATFLToggleErPå) {
        this.splitteATFLToggleErPå = splitteATFLToggleErPå;
    }

    void setGrunnbeløpSatser(List<Grunnbeløp> grunnbeløpSatser) {
        this.grunnbeløpSatser = grunnbeløpSatser;
    }

    public int getAntallGMilitærHarKravPå() {
        return antallGMilitærHarKravPå;
    }

    public BigDecimal getAntallGØvreGrenseverdi() {
        return antallGØvreGrenseverdi;
    }

    public BigDecimal getAntallGMinstekravVilkår() {
        return antallGMinstekravVilkår;
    }

    public BigDecimal getYtelsedagerIPrÅr() {
        return ytelsedagerIPrÅr;
    }

    public BigDecimal getAvviksgrenseProsent() {
        return avviksgrenseProsent;
    }

    public boolean isSplitteATFLToggleErPå() {
        return splitteATFLToggleErPå;
    }

    public List<Grunnbeløp> getGrunnbeløpSatser() {
        return grunnbeløpSatser;
    }

}
