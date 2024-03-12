package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.ArrayList;
import java.util.List;

public class FastsettBgKunYtelseDto {

    private List<FastsattBrukersAndel> andeler;
    private Boolean skalBrukeBesteberegning;

    public FastsettBgKunYtelseDto(List<FastsattBrukersAndel> andeler, Boolean skalBrukeBesteberegning) { // NOSONAR
        this.andeler = new ArrayList<>(andeler);
        this.skalBrukeBesteberegning = skalBrukeBesteberegning;
    }

    public List<FastsattBrukersAndel> getAndeler() {
        return andeler;
    }

    public void setAndeler(List<FastsattBrukersAndel> andeler) {
        this.andeler = andeler;
    }

    public Boolean getSkalBrukeBesteberegning() {
        return skalBrukeBesteberegning;
    }

    public void setSkalBrukeBesteberegning(Boolean skalBrukeBesteberegning) {
        this.skalBrukeBesteberegning = skalBrukeBesteberegning;
    }
}
