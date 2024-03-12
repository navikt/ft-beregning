package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class TilkommetInntektDto implements IndexKey {

    private final AktivitetStatus aktivitetStatus;
    private final Arbeidsgiver arbeidsgiver;
    private final InternArbeidsforholdRefDto arbeidsforholdRef;
    @SjekkVedKopiering
    private Beløp bruttoInntektPrÅr;
    @SjekkVedKopiering
    private Beløp tilkommetInntektPrÅr;
    @SjekkVedKopiering
    private Boolean skalRedusereUtbetaling;


    public TilkommetInntektDto(TilkommetInntektDto tilkommetInntektDto) {
        this.aktivitetStatus = tilkommetInntektDto.aktivitetStatus;
        this.arbeidsgiver = tilkommetInntektDto.arbeidsgiver;
        this.arbeidsforholdRef = tilkommetInntektDto.arbeidsforholdRef;
        this.bruttoInntektPrÅr = tilkommetInntektDto.bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntektDto.tilkommetInntektPrÅr;
        this.skalRedusereUtbetaling = tilkommetInntektDto.skalRedusereUtbetaling;
    }


    public TilkommetInntektDto(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
    }

    public TilkommetInntektDto(AktivitetStatus aktivitetStatus,
                               Arbeidsgiver arbeidsgiver,
                               InternArbeidsforholdRefDto arbeidsforholdRef,
                               Beløp bruttoInntektPrÅr,
                               Beløp tilkommetInntektPrÅr,
                               Boolean skalRedusereUtbetaling) {
        if (skalRedusereUtbetaling != null && !skalRedusereUtbetaling && tilkommetInntektPrÅr != null) {
            throw new IllegalStateException("Skal ikke sette tilkommet inntekt når ikke redusert utbetaling");
        }
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntektPrÅr;
        this.skalRedusereUtbetaling = skalRedusereUtbetaling;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
    }

    public Beløp getBruttoInntektPrÅr() {
        return bruttoInntektPrÅr;
    }

    public Beløp getTilkommetInntektPrÅr() {
        return tilkommetInntektPrÅr;
    }

    public Boolean skalRedusereUtbetaling() {
        return skalRedusereUtbetaling;
    }


    public boolean matcher(TilkommetInntektDto annet) {
        return this.aktivitetStatus.equals(annet.getAktivitetStatus()) &&
                Objects.equals(this.arbeidsgiver, annet.arbeidsgiver) &
                        Objects.equals(this.arbeidsforholdRef, annet.arbeidsforholdRef);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TilkommetInntektDto that = (TilkommetInntektDto) o;
        return skalRedusereUtbetaling == that.skalRedusereUtbetaling &&
                aktivitetStatus == that.aktivitetStatus &&
                Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef) &&
                Objects.equals(bruttoInntektPrÅr, that.bruttoInntektPrÅr) &&
                Objects.equals(tilkommetInntektPrÅr, that.tilkommetInntektPrÅr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver, arbeidsforholdRef, bruttoInntektPrÅr, tilkommetInntektPrÅr, skalRedusereUtbetaling);
    }

    @Override
    public String toString() {
        return "TilkommetInntektDto{" +
                "aktivitetStatus=" + aktivitetStatus +
                ", arbeidsgiver=" + arbeidsgiver +
                ", arbeidsforholdRef=" + arbeidsforholdRef +
                ", bruttoInntektPrÅr=" + bruttoInntektPrÅr +
                ", tilkommetInntektPrÅr=" + tilkommetInntektPrÅr +
                ", skalRedusereUtbetaling=" + skalRedusereUtbetaling +
                '}';
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(aktivitetStatus, arbeidsgiver, arbeidsforholdRef);
    }
}
