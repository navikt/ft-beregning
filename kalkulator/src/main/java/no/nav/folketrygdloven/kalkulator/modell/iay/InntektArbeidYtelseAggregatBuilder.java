package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;

/**
 * Builder for å håndtere en gitt versjon {@link VersjonTypeDto} av grunnlaget.
 * <p>
 * Holder styr på om det er en oppdatering av eksisterende informasjon, om det gjelder før eller etter skjæringstidspunktet
 * og om det er registerdata eller saksbehandlers beslutninger.
 * <p>
 * NB! Viktig at denne builderen hentes fra repository for å sikre at den er rett tilstand ved oppdatering. Hvis ikke kan data gå tapt.
 */
public class InntektArbeidYtelseAggregatBuilder {

    private final InntektArbeidYtelseAggregatDto kladd;
    private final VersjonTypeDto versjon;
    private final List<ArbeidsforholdReferanseDto> nyeInternArbeidsforholdReferanser = new ArrayList<>();

    private InntektArbeidYtelseAggregatBuilder(InntektArbeidYtelseAggregatDto kladd, VersjonTypeDto versjon) {
        this.kladd = kladd;
        this.versjon = versjon;
    }

    public static InntektArbeidYtelseAggregatBuilder oppdatere(Optional<InntektArbeidYtelseAggregatDto> oppdatere, VersjonTypeDto versjon) {
        return builderFor(oppdatere, versjon);
    }

    private static InntektArbeidYtelseAggregatBuilder builderFor(Optional<InntektArbeidYtelseAggregatDto> kopierDataFra,
                                                                 VersjonTypeDto versjon) {
        return kopierDataFra
                .map(kopier -> new InntektArbeidYtelseAggregatBuilder(new InntektArbeidYtelseAggregatDto(kopier), versjon))
                .orElseGet(() -> new InntektArbeidYtelseAggregatBuilder(new InntektArbeidYtelseAggregatDto(), versjon));
    }

    /**
     * Legger til inntekter hvis det ikke er en oppdatering av eksisterende.
     *
     * @param aktørInntekt {@link AktørInntektBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørInntekt(AktørInntektBuilder aktørInntekt) {
        if (!aktørInntekt.getErOppdatering()) {
            this.kladd.setAktørInntekt(aktørInntekt.build());
        }
        return this;
    }

    /**
     * Legger til aktiviteter hvis det ikke er en oppdatering av eksisterende.
     *
     * @param aktørArbeid {@link AktørArbeidBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørArbeid(AktørArbeidBuilder aktørArbeid) {
        if (!aktørArbeid.getErOppdatering()) {
            this.kladd.setAktørArbeid(aktørArbeid.build());
        }
        return this;
    }

    /**
     * Legger til tilstøtende ytelser hvis det ikke er en oppdatering av eksisterende.
     *
     * @param aktørYtelse {@link AktørYtelseBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørYtelse(AktørYtelseBuilder aktørYtelse) {
        if (!aktørYtelse.getErOppdatering() && aktørYtelse.harVerdi()) {
            this.kladd.setAktørYtelse(aktørYtelse.build());
        }
        return this;
    }

    /**
     * Oppretter builder for aktiviteter for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @return builder {@link AktørArbeidBuilder}
     */
    public AktørArbeidBuilder getAktørArbeidBuilder() {
        return AktørArbeidBuilder.oppdatere(Optional.ofNullable(kladd.getAktørArbeid()));
    }

    /**
     * Oppretter builder for inntekter for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @return builder {@link AktørInntektBuilder}
     */
    public AktørInntektBuilder getAktørInntektBuilder() {
        return AktørInntektBuilder.oppdatere(Optional.ofNullable(kladd.getAktørInntekt()));
    }

    /**
     * Oppretter builder for tilstøtende ytelser for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @return builder {@link AktørYtelseBuilder}
     */
    public AktørYtelseBuilder getAktørYtelseBuilder() {
        return AktørYtelseBuilder.oppdatere(Optional.ofNullable(kladd.getAktørYtelse()));
    }

    public InntektArbeidYtelseAggregatDto build() {
        return this.kladd;
    }

    VersjonTypeDto getVersjon() {
        return versjon;
    }


    public InternArbeidsforholdRefDto medNyInternArbeidsforholdRef(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef eksternReferanse) {
        if (eksternReferanse == null || eksternReferanse.getReferanse() == null) {
            return InternArbeidsforholdRefDto.nullRef();
        }
        InternArbeidsforholdRefDto nyRef = InternArbeidsforholdRefDto.nyRef();
        nyeInternArbeidsforholdReferanser.add(new ArbeidsforholdReferanseDto(arbeidsgiver, nyRef, eksternReferanse));
        return nyRef;
    }

    public static class AktørArbeidBuilder {
        private final AktørArbeidDto kladd;
        private final boolean oppdatering;

        private AktørArbeidBuilder(AktørArbeidDto aktørArbeid, boolean oppdatering) {
            this.kladd = aktørArbeid;
            this.oppdatering = oppdatering;
        }

        static AktørArbeidBuilder ny() {
            return new AktørArbeidBuilder(new AktørArbeidDto(), false);
        }

        static AktørArbeidBuilder oppdatere(AktørArbeidDto oppdatere) {
            return new AktørArbeidBuilder(oppdatere, true);
        }

        public static AktørArbeidBuilder oppdatere(Optional<AktørArbeidDto> oppdatere) {
            return oppdatere.map(AktørArbeidBuilder::oppdatere).orElseGet(AktørArbeidBuilder::ny);
        }

        public YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForNøkkelAvType(OpptjeningsnøkkelDto nøkkel, ArbeidType arbeidType) {
            return kladd.getYrkesaktivitetBuilderForNøkkel(nøkkel, arbeidType);
        }

        public YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForType(ArbeidType type) {
            return kladd.getYrkesaktivitetBuilderForType(type);
        }

        public AktørArbeidBuilder leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder builder) {
            YrkesaktivitetDto yrkesaktivitet = builder.build();
            if (!builder.getErOppdatering()) {
                kladd.leggTilYrkesaktivitet(yrkesaktivitet);
            }
            return this;
        }

        public AktørArbeidBuilder leggTilYrkesaktivitet(YrkesaktivitetDto yrkesaktivitet) {
            kladd.leggTilYrkesaktivitet(yrkesaktivitet);
            return this;
        }

        public AktørArbeidDto build() {
            if (kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException();
        }

        boolean getErOppdatering() {
            return oppdatering;
        }
    }

    public static class AktørInntektBuilder {
        private final AktørInntektDto kladd;
        private final boolean oppdatering;

        private AktørInntektBuilder(AktørInntektDto aktørInntekt, boolean oppdatering) {
            this.kladd = aktørInntekt;
            this.oppdatering = oppdatering;
        }

        static AktørInntektBuilder ny() {
            return new AktørInntektBuilder(new AktørInntektDto(), false);
        }

        static AktørInntektBuilder oppdatere(AktørInntektDto oppdatere) {
            return new AktørInntektBuilder(oppdatere, true);
        }

        public static AktørInntektBuilder oppdatere(Optional<AktørInntektDto> oppdatere) {
            return oppdatere.map(AktørInntektBuilder::oppdatere).orElseGet(AktørInntektBuilder::ny);
        }

        public InntektDtoBuilder getInntektBuilder(InntektskildeType inntektsKilde, OpptjeningsnøkkelDto opptjeningsnøkkel) {
            return kladd.getInntektBuilder(inntektsKilde, opptjeningsnøkkel);
        }

        public AktørInntektBuilder leggTilInntekt(InntektDtoBuilder builder) {
            if (!builder.getErOppdatering()) {
                kladd.leggTilInntekt(builder.build());
            }
            return this;
        }

        public AktørInntektDto build() {
            if (kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException();
        }

        boolean getErOppdatering() {
            return oppdatering;
        }

    }

    public static class AktørYtelseBuilder {
        private final AktørYtelseDto kladd;
        private final boolean oppdatering;

        private AktørYtelseBuilder(AktørYtelseDto aktørYtelse, boolean oppdatering) {
            this.kladd = aktørYtelse;
            this.oppdatering = oppdatering;
        }

        static AktørYtelseBuilder ny() {
            return new AktørYtelseBuilder(new AktørYtelseDto(), false);
        }

        static AktørYtelseBuilder oppdatere(AktørYtelseDto oppdatere) {
            return new AktørYtelseBuilder(oppdatere, true);
        }

        public static AktørYtelseBuilder oppdatere(Optional<AktørYtelseDto> oppdatere) {
            return oppdatere.map(AktørYtelseBuilder::oppdatere).orElseGet(AktørYtelseBuilder::ny);
        }

        boolean getErOppdatering() {
            return oppdatering;
        }

        public AktørYtelseBuilder leggTilYtelse(YtelseDtoBuilder builder) {
            YtelseDto ytelse = builder.build();
            if (!builder.getErOppdatering()) {
                this.kladd.leggTilYtelse(ytelse);
            }
            return this;
        }

        boolean harVerdi() {
            return kladd.hasValues();
        }

        public AktørYtelseDto build() {
            if (this.kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException("Har ikke innhold");
        }
    }

}
