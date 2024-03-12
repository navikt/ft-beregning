package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class InntektArbeidYtelseGrunnlagDtoBuilder {

    private InntektArbeidYtelseGrunnlagDto kladd;

    protected InntektArbeidYtelseGrunnlagDtoBuilder(InntektArbeidYtelseGrunnlagDto kladd) {
        this.kladd = kladd;
    }

    public static InntektArbeidYtelseGrunnlagDtoBuilder nytt() {
        return new InntektArbeidYtelseGrunnlagDtoBuilder(new InntektArbeidYtelseGrunnlagDto());
    }

    public static InntektArbeidYtelseGrunnlagDtoBuilder oppdatere(InntektArbeidYtelseGrunnlagDto kladd) {
        return new InntektArbeidYtelseGrunnlagDtoBuilder(new InntektArbeidYtelseGrunnlagDto(kladd));
    }

    public static InntektArbeidYtelseGrunnlagDtoBuilder oppdatere(Optional<InntektArbeidYtelseGrunnlagDto> kladd) {
        return kladd.map(InntektArbeidYtelseGrunnlagDtoBuilder::oppdatere).orElseGet(InntektArbeidYtelseGrunnlagDtoBuilder::nytt);
    }

    // FIXME: Bør ikke være public, bryter encapsulation
    public InntektArbeidYtelseGrunnlagDto getKladd() {
        return kladd;
    }

    public InntektsmeldingAggregatDto getInntektsmeldinger() {
        final Optional<InntektsmeldingAggregatDto> inntektsmeldinger = kladd.getInntektsmeldinger();
        return inntektsmeldinger.map(InntektsmeldingAggregatDto::new).orElseGet(InntektsmeldingAggregatDto::new);
    }

    public void setInntektsmeldinger(InntektsmeldingAggregatDto inntektsmeldinger) {
        kladd.setInntektsmeldinger(inntektsmeldinger);
    }

    public ArbeidsforholdInformasjonDto getInformasjon() {
        var informasjon = kladd.getArbeidsforholdInformasjon();

        var informasjonEntitet = informasjon.orElseGet(ArbeidsforholdInformasjonDto::new);
        kladd.setInformasjon(informasjonEntitet);
        return informasjonEntitet;
    }

    public InntektArbeidYtelseGrunnlagDtoBuilder medInformasjon(ArbeidsforholdInformasjonDto informasjon) {
        kladd.setInformasjon(informasjon);
        return this;
    }

    private void medSaksbehandlet(InntektArbeidYtelseAggregatBuilder builder) {
        if (builder != null) {
            kladd.setSaksbehandlet(builder.build());
        }
    }

    private void medRegister(InntektArbeidYtelseAggregatBuilder builder) {
        if (builder != null) {
            kladd.setRegister(builder.build());
        }
    }

    public InntektArbeidYtelseGrunnlagDtoBuilder medOppgittOpptjening(OppgittOpptjeningDtoBuilder builder) {
        if (builder != null) {
            if (kladd.getOppgittOpptjening().isPresent()) {
                throw new IllegalStateException("Utviklerfeil: Er ikke lov å endre oppgitt opptjening!");
            }
            kladd.setOppgittOpptjening(builder.build());
        }
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto build() {
        var k = kladd;
        if (kladd.getArbeidsforholdInformasjon().isPresent()) {
            k.taHensynTilBetraktninger();
        }
        kladd = null; // må ikke finne på å gjenbruke buildere her, tar heller straffen i en NPE ved første feilkall
        return k;
    }

    public InntektArbeidYtelseGrunnlagDtoBuilder medData(InntektArbeidYtelseAggregatBuilder builder) {
        VersjonTypeDto versjon = builder.getVersjon();

        if (versjon == VersjonTypeDto.REGISTER) {
            medRegister(builder);
        } else if (versjon == VersjonTypeDto.SAKSBEHANDLET) {
            medSaksbehandlet(builder);
        }
        return this;
    }

    public Optional<ArbeidsforholdInformasjonDto> getArbeidsforholdInformasjon() {
        return kladd.getArbeidsforholdInformasjon();
    }

    public InntektArbeidYtelseGrunnlagDtoBuilder medErAktivtGrunnlag(boolean erAktivt) {
        kladd.setAktivt(erAktivt);
        return this;
    }

    public InntektArbeidYtelseGrunnlagDtoBuilder medInntektsmeldinger(Collection<InntektsmeldingDto> inntektsmeldinger) {
        setInntektsmeldinger(new InntektsmeldingAggregatDto(inntektsmeldinger));
        return this;
    }

    public InntektArbeidYtelseGrunnlagDtoBuilder medInntektsmeldinger(InntektsmeldingDto... inntektsmeldinger) {
        return medInntektsmeldinger(Arrays.asList(inntektsmeldinger));
    }
}
