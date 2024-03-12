package no.nav.folketrygdloven.kalkulator.modell.besteberegning;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class Ytelseandel {
    private AktivitetStatus aktivitetStatus;
    private Inntektskategori inntektskategori;
    private Arbeidskategori arbeidskategori;
    private Long dagsats;

    public Ytelseandel(AktivitetStatus aktivitetStatus,
                       Inntektskategori inntektskategori,
                       Long dagsats) {
        this.aktivitetStatus = Objects.requireNonNull(aktivitetStatus, "aktivitetstatus");
        this.inntektskategori = Objects.requireNonNull(inntektskategori, "inntektskategori");
        this.dagsats = dagsats;
    }

    public Ytelseandel(Arbeidskategori arbeidskategori,
                       Long dagsats) {
        this.arbeidskategori = Objects.requireNonNull(arbeidskategori, "arbeidskategori");
        this.dagsats = dagsats;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
