package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;


public class BeregningRefusjonOverstyringDto {

    private Arbeidsgiver arbeidsgiver;
    private LocalDate førsteMuligeRefusjonFom;
    private Boolean erFristUtvidet;
    private BeregningRefusjonOverstyringerDto refusjonOverstyringer;
    private List<BeregningRefusjonPeriodeDto> refusjonPerioder = new ArrayList<>();

    BeregningRefusjonOverstyringDto() {
        // Hibernate
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver,
                                           boolean erFristUtvidet) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        this.erFristUtvidet = erFristUtvidet;
        this.arbeidsgiver = arbeidsgiver;
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver,
                                           LocalDate førsteMuligeRefusjonFom, boolean erFristUtvidet) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        this.erFristUtvidet = erFristUtvidet;
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.arbeidsgiver = arbeidsgiver;
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver, LocalDate førsteMuligeRefusjonFom,
                                           List<BeregningRefusjonPeriodeDto> refusjonPerioder,
                                           Boolean erFristUtvidet) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        this.refusjonPerioder = refusjonPerioder;
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.arbeidsgiver = arbeidsgiver;
        this.erFristUtvidet = erFristUtvidet;
    }

    void setRefusjonOverstyringerEntitet(BeregningRefusjonOverstyringerDto refusjonOverstyringer) {
        this.refusjonOverstyringer = refusjonOverstyringer;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Optional<LocalDate> getFørsteMuligeRefusjonFom() {
        return Optional.ofNullable(førsteMuligeRefusjonFom);
    }

    public Optional<Boolean> getErFristUtvidet() {
        return Optional.ofNullable(erFristUtvidet);
    }

    public List<BeregningRefusjonPeriodeDto> getRefusjonPerioder() {
        return Collections.unmodifiableList(refusjonPerioder);
    }
}
