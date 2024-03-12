package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.felles.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class FastsettFaktaOmBeregningVerdierTjeneste {

    private FastsettFaktaOmBeregningVerdierTjeneste() {
        // skjul
    }

    /**
     * Setter inntekt/inntektskategori verdier for fakta om beregning
     *
     * @param andel                  Informasjon om andel
     * @param fastsatteVerdier       Verdier som er fastsatt
     * @param periode                Beregningsgrunnlagperiode
     * @param periodeForrigeGrunnlag Periode fra forrige grunnlag
     */
    public static void fastsettVerdierForAndel(RedigerbarAndelFaktaOmBeregningDto andel,
                                               FastsatteVerdierDto fastsatteVerdier,
                                               BeregningsgrunnlagPeriodeDto periode,
                                               Optional<BeregningsgrunnlagPeriodeDto> periodeForrigeGrunnlag) {
        validerAtPåkrevdeVerdierErSatt(andel);
        if (andel.getNyAndel() || andel.getLagtTilAvSaksbehandler()) {
            if (andel.getAktivitetStatus().isPresent()) {
                fastsettBeløpForNyAndelMedAktivitetstatus(periode, andel.getAktivitetStatus().get(), fastsatteVerdier);
            } else {
                fastsettBeløpForAndelLagtTilAvSaksbehandlerFraAndelsreferanse(andel, periode, periodeForrigeGrunnlag, fastsatteVerdier);
            }
        } else {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder korrektAndel = getKorrektAndel(periode, periodeForrigeGrunnlag, andel);
            settInntektskategoriOgFastsattBeløp(andel, fastsatteVerdier, korrektAndel, periode);
        }
    }

    private static void validerAtPåkrevdeVerdierErSatt(RedigerbarAndelFaktaOmBeregningDto andel) {
        if (andel.getAndelsnr().isEmpty() && andel.getAktivitetStatus().isEmpty()) {
            throw new IllegalArgumentException("Enten andelsnr eller aktivitetstatus må vere satt.");
        }
        if (!andel.getLagtTilAvSaksbehandler() && !andel.getNyAndel() && andel.getAndelsnr().isEmpty()) {
            throw new IllegalArgumentException("Eksisterende andeler som ikkje er lagt til av saksbehandler må ha andelsnr.");
        }
    }

    private static void fastsettBeløpForAndelLagtTilAvSaksbehandlerFraAndelsreferanse(RedigerbarAndelFaktaOmBeregningDto andel,
                                                                                      BeregningsgrunnlagPeriodeDto periode,
                                                                                      Optional<BeregningsgrunnlagPeriodeDto> periodeForrigeGrunnlag,
                                                                                      FastsatteVerdierDto fastsatteVerdier) {
        if (andel.getAndelsnr().isEmpty()) {
            throw new IllegalStateException("Må ha andelsnr for å fastsette beløp fra andelsnr");
        }
        Long andelsnr = andel.getAndelsnr().get();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder korrektAndel;
        if (!andel.getNyAndel() && periodeForrigeGrunnlag.isPresent()) {
            korrektAndel = BeregningsgrunnlagPrStatusOgAndelDto.kopier(MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriodePåAndelsnr(periodeForrigeGrunnlag.get(), andelsnr));
        } else {
            korrektAndel = BeregningsgrunnlagPrStatusOgAndelDto.kopier(MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriodePåAndelsnr(periode, andelsnr));
        }
        settInntektskategoriOgFastsattBeløp(andel, fastsatteVerdier, korrektAndel, periode);
    }

    private static void settInntektskategoriOgFastsattBeløp(RedigerbarAndelFaktaOmBeregningDto andel,
                                                            FastsatteVerdierDto fastsatteVerdier,
                                                            BeregningsgrunnlagPrStatusOgAndelDto.Builder korrektAndel,
                                                            BeregningsgrunnlagPeriodeDto korrektPeriode) {
        Inntektskategori nyInntektskategori = fastsatteVerdier.getInntektskategori();
        if (nyInntektskategori != null) {
            korrektAndel.medInntektskategori(nyInntektskategori);
        }
        korrektAndel
                .medBeregnetPrÅr(fastsatteVerdier.finnEllerUtregnFastsattBeløpPrÅr())
                .medBesteberegningPrÅr(Boolean.TRUE.equals(fastsatteVerdier.getSkalHaBesteberegning()) ? fastsatteVerdier.finnEllerUtregnFastsattBeløpPrÅr() : null)
                .medFastsattAvSaksbehandler(true);
        if (andel.getNyAndel() || andel.getLagtTilAvSaksbehandler()) {
            korrektAndel.nyttAndelsnr(korrektPeriode)
                    .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                    .build(korrektPeriode);
        }
    }


    private static void fastsettBeløpForNyAndelMedAktivitetstatus(BeregningsgrunnlagPeriodeDto periode,
                                                                  AktivitetStatus aktivitetStatus,
                                                                  FastsatteVerdierDto fastsatteVerdier) {
        var fastsatt = fastsatteVerdier.finnEllerUtregnFastsattBeløpPrÅr();// NOSONAR
        Inntektskategori nyInntektskategori = fastsatteVerdier.getInntektskategori();
        if (nyInntektskategori == null) {
            throw new IllegalStateException("Kan ikke sette inntektskategori lik null på ny andel.");
        }
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(aktivitetStatus)
                .medInntektskategori(nyInntektskategori)
                .medBeregnetPrÅr(fastsatt)
                .medBesteberegningPrÅr(Boolean.TRUE.equals(fastsatteVerdier.getSkalHaBesteberegning()) ? fastsatt : null)
                .medFastsattAvSaksbehandler(true)
                .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                .build(periode);
    }


    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder getKorrektAndel(BeregningsgrunnlagPeriodeDto periode,
                                                                                Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode,
                                                                                RedigerbarAndelFaktaOmBeregningDto andel) {
        if (andel.getAndelsnr().isEmpty()) {
            throw new IllegalArgumentException("Har ikke andelsnr når man burde ha hatt det.");
        }
        Long andelsnr = andel.getAndelsnr().get();
        if (andel.getLagtTilAvSaksbehandler() && !andel.getNyAndel() && forrigePeriode.isPresent()) {
            return BeregningsgrunnlagPrStatusOgAndelDto.kopier(MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriodePåAndelsnr(forrigePeriode.get(), andelsnr));
        }
        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriodePåAndelsnr(periode, andelsnr));
    }

}
