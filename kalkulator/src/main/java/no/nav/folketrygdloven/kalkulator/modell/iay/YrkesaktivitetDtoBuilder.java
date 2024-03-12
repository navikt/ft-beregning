package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

public class YrkesaktivitetDtoBuilder {
    private final YrkesaktivitetDto kladd;
    private boolean oppdaterer;

    private YrkesaktivitetDtoBuilder(YrkesaktivitetDto kladd, boolean oppdaterer) {
        this.kladd = kladd;
        this.oppdaterer = oppdaterer;
    }

    static YrkesaktivitetDtoBuilder ny() {
        return new YrkesaktivitetDtoBuilder(new YrkesaktivitetDto(), false);
    }

    static YrkesaktivitetDtoBuilder oppdatere(YrkesaktivitetDto oppdatere) {
        return new YrkesaktivitetDtoBuilder(oppdatere, true);
    }

    public static YrkesaktivitetDtoBuilder oppdatere(Optional<YrkesaktivitetDto> oppdatere) {
        return oppdatere.map(YrkesaktivitetDtoBuilder::oppdatere).orElseGet(YrkesaktivitetDtoBuilder::ny);
    }

    public YrkesaktivitetDtoBuilder medArbeidType(ArbeidType arbeidType) {
        kladd.setArbeidType(arbeidType);
        return this;
    }

    public YrkesaktivitetDtoBuilder medArbeidsforholdId(InternArbeidsforholdRefDto arbeidsforholdId) {
        kladd.setArbeidsforholdId(arbeidsforholdId);
        return this;
    }

    public YrkesaktivitetDtoBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public AktivitetsAvtaleDtoBuilder getAktivitetsAvtaleBuilder() {
        return nyAktivitetsAvtaleBuilder();
    }

    public YrkesaktivitetDtoBuilder leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder builder) {
        if(!builder.isOppdatering()) {
            AktivitetsAvtaleDto aktivitetsAvtale = builder.build();
            kladd.leggTilAktivitetsAvtale(aktivitetsAvtale);
        }
        return this;
    }

    public YrkesaktivitetDtoBuilder leggTilPermisjon(PermisjonDtoBuilder builder) {
        PermisjonDto permisjonDto = builder.build();
        kladd.leggTilPermisjon(permisjonDto);
        return this;
    }

    boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public YrkesaktivitetDto build() {
        return kladd;
    }

    public static AktivitetsAvtaleDtoBuilder nyAktivitetsAvtaleBuilder() {
        return AktivitetsAvtaleDtoBuilder.ny();
    }

    public AktivitetsAvtaleDtoBuilder getAktivitetsAvtaleBuilder(Intervall periode, boolean erAnsettelsesperioden) {
        AktivitetsAvtaleDtoBuilder oppdater = AktivitetsAvtaleDtoBuilder.oppdater(kladd.getAlleAktivitetsAvtaler()
            .stream()
            .filter(aa -> aa.matcherPeriode(periode)
                && (!kladd.erArbeidsforhold() || aa.erAnsettelsesPeriode() == erAnsettelsesperioden)).findFirst());
        oppdater.medPeriode(periode);
        return oppdater;
    }
}
