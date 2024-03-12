package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class AnvistAndel {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdId;
    private Beløp beløp;
    private Beløp dagsats;
    private Stillingsprosent refusjonsgrad;
    private Inntektskategori inntektskategori;

    public AnvistAndel(Arbeidsgiver arbeidsgiver, Beløp beløp, Stillingsprosent refusjonsgrad, Inntektskategori inntektskategori) {
        this.arbeidsgiver = arbeidsgiver;
        this.beløp = beløp;
        this.refusjonsgrad = refusjonsgrad;
        this.inntektskategori = inntektskategori;
    }

    public AnvistAndel(Arbeidsgiver arbeidsgiver,
                       InternArbeidsforholdRefDto arbeidsforholdId, Beløp beløp,
                       Beløp dagsats, Stillingsprosent refusjonsgrad, Inntektskategori inntektskategori) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdId = arbeidsforholdId;
        this.beløp = beløp;
        this.dagsats = dagsats;
        this.refusjonsgrad = refusjonsgrad;
        this.inntektskategori = inntektskategori;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public Stillingsprosent getRefusjonsgrad() {
        return refusjonsgrad;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Beløp getDagsats() {
        return dagsats;
    }
}
