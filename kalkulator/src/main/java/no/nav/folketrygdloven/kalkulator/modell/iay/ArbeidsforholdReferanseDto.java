package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public class ArbeidsforholdReferanseDto {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto internReferanse;
    private EksternArbeidsforholdRef eksternReferanse;

    public ArbeidsforholdReferanseDto(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internReferanse, EksternArbeidsforholdRef eksternReferanse) {
        this.arbeidsgiver = arbeidsgiver;
        this.internReferanse = internReferanse != null ? internReferanse : InternArbeidsforholdRefDto.nullRef();
        this.eksternReferanse = eksternReferanse;
    }

    ArbeidsforholdReferanseDto(ArbeidsforholdReferanseDto arbeidsforholdInformasjonEntitet) {
        this(arbeidsforholdInformasjonEntitet.arbeidsgiver, arbeidsforholdInformasjonEntitet.internReferanse, arbeidsforholdInformasjonEntitet.eksternReferanse);
    }

    public InternArbeidsforholdRefDto getInternReferanse() {
        return internReferanse;
    }

    public EksternArbeidsforholdRef getEksternReferanse() {
        return eksternReferanse;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof ArbeidsforholdReferanseDto))
            return false;
        ArbeidsforholdReferanseDto that = (ArbeidsforholdReferanseDto) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(internReferanse, that.internReferanse) &&
                Objects.equals(eksternReferanse, that.eksternReferanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, internReferanse, eksternReferanse);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdReferanseEntitet{" +
                "arbeidsgiver=" + arbeidsgiver +
                ", internReferanse=" + internReferanse +
                ", eksternReferanse=" + eksternReferanse +
                '}';
    }
}
