package no.nav.folketrygdloven.kalkulator.modell.iay;

import static no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType.IKKE_BRUK;
import static no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InntektsmeldingAggregatDto {

    private List<InntektsmeldingDto> inntektsmeldinger = new ArrayList<>();

    private ArbeidsforholdInformasjonDto arbeidsforholdInformasjon;

    public InntektsmeldingAggregatDto() {
    }

    InntektsmeldingAggregatDto(InntektsmeldingAggregatDto inntektsmeldingAggregat) {
        this(inntektsmeldingAggregat.getAlleInntektsmeldinger());
    }

    public InntektsmeldingAggregatDto(Collection<InntektsmeldingDto> inntektsmeldinger) {
        this.inntektsmeldinger.addAll(inntektsmeldinger.stream()
                .map(InntektsmeldingDto::new)
                .collect(Collectors.toList()));
    }

    /**
     * Alle gjeldende inntektsmeldinger i behandlingen (de som skal brukes)
     * @return Liste med {@link InntektsmeldingDto}
     *
     *Merk denne filtrerer inntektsmeldinger ifht hva som skal brukes. */
    public List<InntektsmeldingDto> getInntektsmeldingerSomSkalBrukes() {
        return inntektsmeldinger.stream().filter(this::skalBrukes).collect(Collectors.toUnmodifiableList());
    }

    /** Get alle inntetksmeldinger (både de som skal brukes og ikke brukes). */
    public List<InntektsmeldingDto> getAlleInntektsmeldinger() {
        return List.copyOf(inntektsmeldinger);
    }

    private boolean skalBrukes(InntektsmeldingDto im) {
        return arbeidsforholdInformasjon == null || arbeidsforholdInformasjon.getOverstyringer()
            .stream()
            .noneMatch(ov -> erFjernet(im, ov));
    }

    private boolean erFjernet(InntektsmeldingDto im, ArbeidsforholdOverstyringDto ov) {
        return (ov.getArbeidsforholdRef().equals(im.getArbeidsforholdRef()))
            && ov.getArbeidsgiver().equals(im.getArbeidsgiver())
            && (Objects.equals(IKKE_BRUK, ov.getHandling())
            || Objects.equals(SLÅTT_SAMMEN_MED_ANNET, ov.getHandling())
            || ov.kreverIkkeInntektsmelding());
    }

    void taHensynTilBetraktninger(ArbeidsforholdInformasjonDto arbeidsforholdInformasjon) {
        this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof InntektsmeldingAggregatDto)) return false;
        InntektsmeldingAggregatDto that = (InntektsmeldingAggregatDto) o;
        return Objects.equals(inntektsmeldinger, that.inntektsmeldinger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektsmeldinger);
    }

    public static class InntektsmeldingAggregatDtoBuilder {
        private InntektsmeldingAggregatDto kladd;

        InntektsmeldingAggregatDtoBuilder(InntektsmeldingAggregatDto kladd) {
            this.kladd = kladd;
        }

        public static InntektsmeldingAggregatDtoBuilder ny() {
            return new InntektsmeldingAggregatDtoBuilder(new InntektsmeldingAggregatDto());
        }

        public InntektsmeldingAggregatDtoBuilder medArbeidsforholdInformasjonDto(ArbeidsforholdInformasjonDto arbeidsforholdInformasjonDto) {
            this.kladd.arbeidsforholdInformasjon = arbeidsforholdInformasjonDto;
            return this;
        }

        public InntektsmeldingAggregatDtoBuilder leggTil(InntektsmeldingDto inntektsmeldingDto) {
            this.kladd.inntektsmeldinger.add(inntektsmeldingDto);
            return this;
        }

        public InntektsmeldingAggregatDto build() {
            return kladd;
        }
    }
}
