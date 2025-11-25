package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.ArrayList;
import java.util.List;

public class FastsettBeregningsgrunnlagATFLDto {

    private List<InntektPrAndelDto> inntektPrAndelList;
    private Integer inntektFrilanser;
    private List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder;

    FastsettBeregningsgrunnlagATFLDto() {
        // For Jackson
    }

    public FastsettBeregningsgrunnlagATFLDto(List<InntektPrAndelDto> inntektPrAndelList, Integer inntektFrilanser) { // NOSONAR
        this.inntektPrAndelList = new ArrayList<>(inntektPrAndelList);
        this.inntektFrilanser = inntektFrilanser;
    }

    public FastsettBeregningsgrunnlagATFLDto(Integer inntektFrilanser) { // NOSONAR
        this.inntektFrilanser = inntektFrilanser;
    }

    public Integer getInntektFrilanser() {
        return inntektFrilanser;
    }

    public List<InntektPrAndelDto> getInntektPrAndelList() {
        return inntektPrAndelList;
    }

    public List<FastsattePerioderTidsbegrensetDto> getFastsatteTidsbegrensedePerioder() {
        return fastsatteTidsbegrensedePerioder;
    }

    public boolean tidsbegrensetInntektErFastsatt() {
        return fastsatteTidsbegrensedePerioder != null && !fastsatteTidsbegrensedePerioder.isEmpty();
    }

}
