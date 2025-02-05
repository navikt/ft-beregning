package no.nav.folketrygdloven.kalkulus.migrering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

public class BeregningRefusjonOverstyringMigreringDto extends BaseMigreringDto {

    @Valid
    private Arbeidsgiver arbeidsgiver;

    @Valid
    private LocalDate førsteMuligeRefusjonFom;

    @Valid
    private Boolean erFristUtvidet;

    @Valid
    @Size(max=100)
    private List<BeregningRefusjonPeriodeMigreringDto> refusjonPerioder = new ArrayList<>();

    public BeregningRefusjonOverstyringMigreringDto() {
    }

    public BeregningRefusjonOverstyringMigreringDto(Arbeidsgiver arbeidsgiver,
                                                    LocalDate førsteMuligeRefusjonFom,
                                                    Boolean erFristUtvidet,
                                                    List<BeregningRefusjonPeriodeMigreringDto> refusjonPerioder) {
        this.arbeidsgiver = arbeidsgiver;
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.erFristUtvidet = erFristUtvidet;
        this.refusjonPerioder = refusjonPerioder;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public LocalDate getFørsteMuligeRefusjonFom() {
        return førsteMuligeRefusjonFom;
    }

    public Boolean getErFristUtvidet() {
        return erFristUtvidet;
    }

    public List<BeregningRefusjonPeriodeMigreringDto> getRefusjonPerioder() {
        return refusjonPerioder;
    }
}
