package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;

public class ArbeidsforholdInformasjonDto {

    private Set<ArbeidsforholdReferanseDto> referanser = new LinkedHashSet<>();
    private List<ArbeidsforholdOverstyringDto> overstyringer = new ArrayList<>();

    public ArbeidsforholdInformasjonDto() {
    }

    public ArbeidsforholdInformasjonDto(ArbeidsforholdInformasjonDto arbeidsforholdInformasjon) {
        for (ArbeidsforholdReferanseDto arbeidsforholdReferanse : arbeidsforholdInformasjon.referanser) {
            final ArbeidsforholdReferanseDto referanseEntitet = new ArbeidsforholdReferanseDto(arbeidsforholdReferanse);
            this.referanser.add(referanseEntitet);
        }
        for (ArbeidsforholdOverstyringDto arbeidsforholdOverstyringEntitet : arbeidsforholdInformasjon.overstyringer) {
            final ArbeidsforholdOverstyringDto overstyringEntitet = new ArbeidsforholdOverstyringDto(arbeidsforholdOverstyringEntitet);
            this.overstyringer.add(overstyringEntitet);
        }
    }

    public List<ArbeidsforholdOverstyringDto> getOverstyringer() {
        return Collections.unmodifiableList(this.overstyringer);
    }

    public EksternArbeidsforholdRef finnEkstern(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internReferanse) {
        if (internReferanse.getReferanse() == null) return EksternArbeidsforholdRef.nullRef();

        return referanser.stream()
            .filter(this::erIkkeMerget)
            .filter(r -> Objects.equals(r.getInternReferanse(), internReferanse) && Objects.equals(r.getArbeidsgiver(), arbeidsgiver))
            .findFirst()
            .map(ArbeidsforholdReferanseDto::getEksternReferanse)
            .orElseThrow(
                () -> new IllegalStateException("Mangler eksternReferanse for internReferanse: " + internReferanse + ", arbeidsgiver: " + arbeidsgiver));
    }

    private boolean erIkkeMerget(ArbeidsforholdReferanseDto arbeidsforholdReferanseEntitet) {
        return overstyringer.stream().noneMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
            && ov.getArbeidsgiver().equals(arbeidsforholdReferanseEntitet.getArbeidsgiver())
            && ov.getArbeidsforholdRef().equals(arbeidsforholdReferanseEntitet.getInternReferanse()));
    }

    void leggTilNyReferanse(ArbeidsforholdReferanseDto arbeidsforholdReferanse) {
        referanser.add(arbeidsforholdReferanse);
    }

    ArbeidsforholdOverstyringDtoBuilder getOverstyringBuilderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto ref) {
        return ArbeidsforholdOverstyringDtoBuilder.oppdatere(this.overstyringer
            .stream()
            .filter(ov -> ov.getArbeidsgiver().equals(arbeidsgiver)
                && ov.getArbeidsforholdRef().gjelderFor(ref))
            .findFirst())
            .medArbeidsforholdRef(ref)
            .medArbeidsgiver(arbeidsgiver);
    }

    void leggTilOverstyring(ArbeidsforholdOverstyringDto build) {
        this.overstyringer.add(build);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof ArbeidsforholdInformasjonDto))
            return false;
        ArbeidsforholdInformasjonDto that = (ArbeidsforholdInformasjonDto) o;
        return Objects.equals(referanser, that.referanser) &&
            Objects.equals(overstyringer, that.overstyringer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referanser, overstyringer);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdInformasjonEntitet{" +
            "referanser=" + referanser +
            ", overstyringer=" + overstyringer +
            '}';
    }

}
