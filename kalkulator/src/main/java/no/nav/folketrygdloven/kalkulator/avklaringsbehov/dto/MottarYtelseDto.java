package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.List;


public class MottarYtelseDto {

    private Boolean frilansMottarYtelse;
    private List<ArbeidstakerandelUtenIMMottarYtelseDto> arbeidstakerUtenIMMottarYtelse;

    public MottarYtelseDto(Boolean frilansMottarYtelse, List<ArbeidstakerandelUtenIMMottarYtelseDto> arbeidstakerUtenIMMottarYtelse) {
        this.frilansMottarYtelse = frilansMottarYtelse;
        this.arbeidstakerUtenIMMottarYtelse = arbeidstakerUtenIMMottarYtelse;
    }

    public Boolean getFrilansMottarYtelse() {
        return frilansMottarYtelse;
    }

    public List<ArbeidstakerandelUtenIMMottarYtelseDto> getArbeidstakerUtenIMMottarYtelse() {
        return arbeidstakerUtenIMMottarYtelse;
    }

    public void setFrilansMottarYtelse(Boolean frilansMottarYtelse) {
        this.frilansMottarYtelse = frilansMottarYtelse;
    }

    public void setArbeidstakerUtenIMMottarYtelse(List<ArbeidstakerandelUtenIMMottarYtelseDto> arbeidstakerUtenIMMottarYtelse) {
        this.arbeidstakerUtenIMMottarYtelse = arbeidstakerUtenIMMottarYtelse;
    }
}
