package no.nav.folketrygdloven.kalkulator.modell.iay;

import static no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE;
import static no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING;
import static no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType.INNTEKT_IKKE_MED_I_BG;
import static no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType.LAGT_TIL_AV_SAKSBEHANDLER;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;

public class ArbeidsforholdOverstyringDto {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private InternArbeidsforholdRefDto nyArbeidsforholdRef;
    private ArbeidsforholdHandlingType handling = ArbeidsforholdHandlingType.UDEFINERT;
    private Stillingsprosent stillingsprosent;
    private List<ArbeidsforholdOverstyrtePerioderDto> arbeidsforholdOverstyrtePerioder = new ArrayList<>();

    ArbeidsforholdOverstyringDto() {
    }

    ArbeidsforholdOverstyringDto(ArbeidsforholdOverstyringDto arbeidsforholdOverstyringEntitet) {
        this.arbeidsgiver = arbeidsforholdOverstyringEntitet.getArbeidsgiver();
        this.arbeidsforholdRef = arbeidsforholdOverstyringEntitet.arbeidsforholdRef;
        this.handling = arbeidsforholdOverstyringEntitet.getHandling();
        this.nyArbeidsforholdRef = arbeidsforholdOverstyringEntitet.nyArbeidsforholdRef;
        this.stillingsprosent = arbeidsforholdOverstyringEntitet.getStillingsprosent();
        leggTilOverstyrtePerioder(arbeidsforholdOverstyringEntitet);
    }

    private void leggTilOverstyrtePerioder(ArbeidsforholdOverstyringDto arbeidsforholdOverstyringEntitet) {
        for (ArbeidsforholdOverstyrtePerioderDto overstyrtePerioderEntitet : arbeidsforholdOverstyringEntitet.getArbeidsforholdOverstyrtePerioder()) {
            ArbeidsforholdOverstyrtePerioderDto perioderEntitet = new ArbeidsforholdOverstyrtePerioderDto(overstyrtePerioderEntitet);
            this.arbeidsforholdOverstyrtePerioder.add(perioderEntitet);
        }
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRefDto.nullRef();
    }

    void setArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
        this.arbeidsforholdRef = arbeidsforholdRef;
    }

    public ArbeidsforholdHandlingType getHandling() {
        return handling;
    }

    void setHandling(ArbeidsforholdHandlingType handling) {
        this.handling = handling;
    }

    void leggTilOverstyrtPeriode(LocalDate fom, LocalDate tom) {
        ArbeidsforholdOverstyrtePerioderDto overstyrtPeriode = new ArbeidsforholdOverstyrtePerioderDto();
        overstyrtPeriode.setPeriode(Intervall.fraOgMedTilOgMed(fom, tom));
        arbeidsforholdOverstyrtePerioder.add(overstyrtPeriode);
    }

    public List<ArbeidsforholdOverstyrtePerioderDto> getArbeidsforholdOverstyrtePerioder() {
        return arbeidsforholdOverstyrtePerioder;
    }

    void setNyArbeidsforholdRef(InternArbeidsforholdRefDto nyArbeidsforholdRef) {
        this.nyArbeidsforholdRef = nyArbeidsforholdRef != null && !InternArbeidsforholdRefDto.nullRef().equals(nyArbeidsforholdRef) ? nyArbeidsforholdRef : null;
    }

    public boolean kreverIkkeInntektsmelding() {
        return Set.of(LAGT_TIL_AV_SAKSBEHANDLER, BRUK_UTEN_INNTEKTSMELDING,
                BRUK_MED_OVERSTYRT_PERIODE, INNTEKT_IKKE_MED_I_BG).contains(handling);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ArbeidsforholdOverstyringDto)) return false;
        ArbeidsforholdOverstyringDto that = (ArbeidsforholdOverstyringDto) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdOverstyringEntitet{" +
                "arbeidsgiver=" + arbeidsgiver +
                ", arbeidsforholdRef=" + arbeidsforholdRef +
                ", handling=" + handling +
                '}';
    }

    public Stillingsprosent getStillingsprosent() {
        return stillingsprosent;
    }

    void setStillingsprosent(Stillingsprosent stillingsprosent) {
        this.stillingsprosent = stillingsprosent;
    }
}
