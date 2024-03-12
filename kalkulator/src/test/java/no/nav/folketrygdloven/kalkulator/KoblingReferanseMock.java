package no.nav.folketrygdloven.kalkulator;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class KoblingReferanseMock extends KoblingReferanse {

    public static final AktørId AKTØR_ID = AktørId.dummy();
    private Skjæringstidspunkt skjæringstidspunkt;
    private FagsakYtelseType fagsakYtelseType = FagsakYtelseType.FORELDREPENGER;

    public KoblingReferanseMock() {
        super();
    }

    public KoblingReferanseMock(LocalDate skjæringstidspunktBeregning, LocalDate skjæringstidspunktOpptjening) {
        super();
        this.skjæringstidspunkt = Skjæringstidspunkt.builder()
                .medSkjæringstidspunktBeregning(skjæringstidspunktBeregning)
                .medSkjæringstidspunktOpptjening(skjæringstidspunktOpptjening)
                .medFørsteUttaksdato(skjæringstidspunktOpptjening.plusDays(1))
                .build();
    }

    public KoblingReferanseMock(LocalDate skjæringstidspunkt, FagsakYtelseType fagsakYtelseType) {
        super();
        this.fagsakYtelseType = fagsakYtelseType;
        this.skjæringstidspunkt = Skjæringstidspunkt.builder()
                .medSkjæringstidspunktBeregning(skjæringstidspunkt)
                .medSkjæringstidspunktOpptjening(skjæringstidspunkt)
                .medFørsteUttaksdato(skjæringstidspunkt.plusDays(1))
                .build();
    }

    public KoblingReferanseMock(LocalDate skjæringstidspunkt) {
        super();
        this.skjæringstidspunkt = Skjæringstidspunkt.builder()
            .medSkjæringstidspunktBeregning(skjæringstidspunkt)
            .medSkjæringstidspunktOpptjening(skjæringstidspunkt)
            .medFørsteUttaksdato(skjæringstidspunkt.plusDays(1))
            .build();
    }

    @Override
    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    @Override
    public Skjæringstidspunkt getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    @Override
    public LocalDate getSkjæringstidspunktBeregning() {
        return skjæringstidspunkt.getSkjæringstidspunktBeregning();
    }

    @Override
    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunkt.getSkjæringstidspunktOpptjening();
    }

    @Override
    public AktørId getAktørId() {
        return AKTØR_ID;
    }

    @Override
    public LocalDate getFørsteUttaksdato() {
        return skjæringstidspunkt.getFørsteUttaksdato();
    }

    @Override
    public Long getKoblingId() {
        return 1L;
    }
}
