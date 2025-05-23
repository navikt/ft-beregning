package no.nav.folketrygdloven.kalkulus.migrering;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

public class BGAndelArbeidsforholdMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private Arbeidsgiver arbeidsgiver;

    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @Valid
    private Beløp refusjonskravPrÅr;

    @Valid
    private Beløp saksbehandletRefusjonPrÅr;

    @Valid
    private Beløp fordeltRefusjonPrÅr;

    @Valid
    private Beløp manueltFordeltRefusjonPrÅr;

    @Valid
    private Hjemmel hjemmelForRefusjonskravfrist;

    @Valid
    private Utfall refusjonskravFristUtfall;

    @Valid
    private Beløp naturalytelseBortfaltPrÅr;

    @Valid
    private Beløp naturalytelseTilkommetPrÅr;

    @Valid
    private LocalDate arbeidsperiodeFom;

    @Valid
    private LocalDate arbeidsperiodeTom;

    public BGAndelArbeidsforholdMigreringDto() {
        // Bruker heller settere her siden det er så mange like felter
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public void setArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
        this.arbeidsforholdRef = arbeidsforholdRef;
    }

    public Beløp getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
    }

    public void setRefusjonskravPrÅr(Beløp refusjonskravPrÅr) {
        this.refusjonskravPrÅr = refusjonskravPrÅr;
    }

    public Beløp getSaksbehandletRefusjonPrÅr() {
        return saksbehandletRefusjonPrÅr;
    }

    public void setSaksbehandletRefusjonPrÅr(Beløp saksbehandletRefusjonPrÅr) {
        this.saksbehandletRefusjonPrÅr = saksbehandletRefusjonPrÅr;
    }

    public Beløp getFordeltRefusjonPrÅr() {
        return fordeltRefusjonPrÅr;
    }

    public void setFordeltRefusjonPrÅr(Beløp fordeltRefusjonPrÅr) {
        this.fordeltRefusjonPrÅr = fordeltRefusjonPrÅr;
    }

    public Beløp getManueltFordeltRefusjonPrÅr() {
        return manueltFordeltRefusjonPrÅr;
    }

    public void setManueltFordeltRefusjonPrÅr(Beløp manueltFordeltRefusjonPrÅr) {
        this.manueltFordeltRefusjonPrÅr = manueltFordeltRefusjonPrÅr;
    }

    public Hjemmel getHjemmelForRefusjonskravfrist() {
        return hjemmelForRefusjonskravfrist;
    }

    public void setHjemmelForRefusjonskravfrist(Hjemmel hjemmelForRefusjonskravfrist) {
        this.hjemmelForRefusjonskravfrist = hjemmelForRefusjonskravfrist;
    }

    public Utfall getRefusjonskravFristUtfall() {
        return refusjonskravFristUtfall;
    }

    public void setRefusjonskravFristUtfall(Utfall refusjonskravFristUtfall) {
        this.refusjonskravFristUtfall = refusjonskravFristUtfall;
    }

    public Beløp getNaturalytelseBortfaltPrÅr() {
        return naturalytelseBortfaltPrÅr;
    }

    public void setNaturalytelseBortfaltPrÅr(Beløp naturalytelseBortfaltPrÅr) {
        this.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
    }

    public Beløp getNaturalytelseTilkommetPrÅr() {
        return naturalytelseTilkommetPrÅr;
    }

    public void setNaturalytelseTilkommetPrÅr(Beløp naturalytelseTilkommetPrÅr) {
        this.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
    }

    public LocalDate getArbeidsperiodeFom() {
        return arbeidsperiodeFom;
    }

    public void setArbeidsperiodeFom(LocalDate arbeidsperiodeFom) {
        this.arbeidsperiodeFom = arbeidsperiodeFom;
    }

    public LocalDate getArbeidsperiodeTom() {
        return arbeidsperiodeTom;
    }

    public void setArbeidsperiodeTom(LocalDate arbeidsperiodeTom) {
        this.arbeidsperiodeTom = arbeidsperiodeTom;
    }
}
