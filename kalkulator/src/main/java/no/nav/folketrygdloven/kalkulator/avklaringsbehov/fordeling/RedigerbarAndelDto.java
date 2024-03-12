package no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling;


import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class RedigerbarAndelDto {

    private Long andelsnr;
    private String arbeidsgiverId;
    private String arbeidsforholdId;
    private Boolean nyAndel;
    private AndelKilde kilde;

    protected RedigerbarAndelDto() { // NOSONAR
        // Jackson
    }

    public RedigerbarAndelDto(Long andelsnr,
                              String arbeidsgiverId,
                              String arbeidsforholdId,
                              Boolean nyAndel,
                              AndelKilde kilde) {
        this.andelsnr = andelsnr;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = arbeidsforholdId;
        this.nyAndel = nyAndel;
        this.kilde = kilde;
    }

    public RedigerbarAndelDto(Boolean nyAndel,
                              String arbeidsgiverId, String internArbeidsforholdId,
                              Long andelsnr,
                              AktivitetStatus aktivitetStatus, OpptjeningAktivitetType arbeidsforholdType, AndelKilde kilde) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
        Objects.requireNonNull(arbeidsforholdType, "arbeidsforholdType");
        this.nyAndel = nyAndel;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = internArbeidsforholdId;
        this.andelsnr = andelsnr;
        this.kilde = kilde;
    }

    public RedigerbarAndelDto(Boolean nyAndel,
                              String arbeidsgiverId, InternArbeidsforholdRefDto arbeidsforholdId,
                              Long andelsnr,
                              AktivitetStatus aktivitetStatus, OpptjeningAktivitetType arbeidsforholdType, AndelKilde kilde) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
        Objects.requireNonNull(arbeidsforholdType, "arbeidsforholdType");
        this.nyAndel = nyAndel;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = arbeidsforholdId == null ? null : arbeidsforholdId.getReferanse();
        this.andelsnr = andelsnr;
        this.kilde = kilde;
    }


    public RedigerbarAndelDto(Boolean nyAndel,
                              Long andelsnr,
                              AktivitetStatus aktivitetStatus, OpptjeningAktivitetType arbeidsforholdType, AndelKilde kilde) {
        this(nyAndel, null, (InternArbeidsforholdRefDto) null, andelsnr, aktivitetStatus, arbeidsforholdType, kilde);
    }


    /**
     * Returnerer andelsnr. Definerer andel som enten skal oppdateres eller kopieres for opprettelse av ny andel.
     *
     * @return Andelsnr
     */
    public Long getAndelsnr() {
        return andelsnr;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdId() {
        return InternArbeidsforholdRefDto.ref(arbeidsforholdId);
    }

    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }


    public Boolean erNyAndel() {
        return nyAndel;
    }

    public Boolean erLagtTilAvSaksbehandler() {
        return kilde.equals(AndelKilde.SAKSBEHANDLER_FORDELING) || kilde.equals(AndelKilde.SAKSBEHANDLER_KOFAKBER);
    }

    public AndelKilde getKilde() {
        return kilde;
    }

}
