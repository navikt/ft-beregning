package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KalkulatorException;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattBrukersAndel;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBgKunYtelseDto;
import no.nav.folketrygdloven.kalkulator.felles.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class FastsettBgKunYtelseOppdaterer {

    private FastsettBgKunYtelseOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto,
                                Optional<BeregningsgrunnlagDto> forrigeBg,
                                BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        FastsettBgKunYtelseDto kunYtelseDto = dto.getKunYtelseFordeling();
        BeregningsgrunnlagPeriodeDto periode = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        List<FastsattBrukersAndel> andeler = kunYtelseDto.getAndeler();
        fjernAndeler(periode, andeler.stream().map(FastsattBrukersAndel::getAndelsnr).collect(Collectors.toList()));
        Boolean skalBrukeBesteberegning = kunYtelseDto.getSkalBrukeBesteberegning();
        for (FastsattBrukersAndel andel : andeler) {
            if (andel.getNyAndel()) {
                fastsettBeløpForNyAndel(periode, andel, kunYtelseDto.getSkalBrukeBesteberegning());
            } else {
                BeregningsgrunnlagPrStatusOgAndelDto korrektAndel = getKorrektAndel(periode, andel, forrigeBg);
                settInntektskategoriOgFastsattBeløp(andel, korrektAndel, periode, skalBrukeBesteberegning);
            }
        }

        // Setter fakta aggregat
        if (skalBrukeBesteberegning != null) {
            FaktaAggregatDto.Builder faktaBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
            FaktaAktørDto.Builder faktaAktørBuilder = faktaBuilder.getFaktaAktørBuilder();
            faktaAktørBuilder.medSkalBesteberegnesFastsattAvSaksbehandler(skalBrukeBesteberegning);
            faktaBuilder.medFaktaAktør(faktaAktørBuilder.build());
            grunnlagBuilder.medFaktaAggregat(faktaBuilder.build());
        }
    }


    private static void fjernAndeler(BeregningsgrunnlagPeriodeDto periode, List<Long> andelsnrListe) {
        BeregningsgrunnlagPeriodeDto.kopier(periode).fjernBeregningsgrunnlagPrStatusOgAndelerSomIkkeLiggerIListeAvAndelsnr(andelsnrListe);
    }


    private static void settInntektskategoriOgFastsattBeløp(FastsattBrukersAndel andel, BeregningsgrunnlagPrStatusOgAndelDto korrektAndel,
                                                     BeregningsgrunnlagPeriodeDto periode, Boolean skalBrukeBesteberegning) {
        Inntektskategori inntektskategori = andel.getInntektskategori();
        var fastsattBeløp = Beløp.fra(andel.getFastsattBeløp()).multipliser(KonfigTjeneste.getMånederIÅr());
        if (andel.getNyAndel()) {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(korrektAndel)
                    .medBeregnetPrÅr(fastsattBeløp)
                    .medBesteberegningPrÅr(Boolean.TRUE.equals(skalBrukeBesteberegning) ? fastsattBeløp : null)
                    .medInntektskategori(inntektskategori)
                    .medFastsattAvSaksbehandler(true)
                    .nyttAndelsnr(periode)
                    .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER).build(periode);
        } else {
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchetAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream().filter(bgAndel -> bgAndel.equals(korrektAndel)).findFirst();
            matchetAndel.ifPresentOrElse(endreEksisterende(skalBrukeBesteberegning, inntektskategori, fastsattBeløp),
                    leggTilFraForrige(korrektAndel, periode, skalBrukeBesteberegning, inntektskategori, fastsattBeløp)
            );
        }
    }

    private static Runnable leggTilFraForrige(BeregningsgrunnlagPrStatusOgAndelDto korrektAndel, BeregningsgrunnlagPeriodeDto periode, Boolean skalBrukeBesteberegning, Inntektskategori inntektskategori, Beløp fastsattBeløp) {
        return () -> BeregningsgrunnlagPrStatusOgAndelDto.kopier(korrektAndel)
                .medBeregnetPrÅr(fastsattBeløp)
                .medBesteberegningPrÅr(Boolean.TRUE.equals(skalBrukeBesteberegning) ? fastsattBeløp : null)
                .medInntektskategori(inntektskategori)
                .medFastsattAvSaksbehandler(true).build(periode);
    }

    private static Consumer<BeregningsgrunnlagPrStatusOgAndelDto> endreEksisterende(Boolean skalBrukeBesteberegning, Inntektskategori inntektskategori, Beløp fastsattBeløp) {
        return match -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(match)
                .medBeregnetPrÅr(fastsattBeløp)
                .medBesteberegningPrÅr(Boolean.TRUE.equals(skalBrukeBesteberegning) ? fastsattBeløp : null)
                .medInntektskategori(inntektskategori)
                .medFastsattAvSaksbehandler(true);
    }


    private static void fastsettBeløpForNyAndel(BeregningsgrunnlagPeriodeDto periode,
                                         FastsattBrukersAndel andel, Boolean skalBrukeBesteberegning) {
        var fastsatt = Beløp.fra(andel.getFastsattBeløp()).multipliser(KonfigTjeneste.getMånederIÅr());
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
                .medInntektskategori(andel.getInntektskategori())
                .medBeregnetPrÅr(fastsatt)
                .medBesteberegningPrÅr(Boolean.TRUE.equals(skalBrukeBesteberegning) ? fastsatt : null)
                .medFastsattAvSaksbehandler(true)
                .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                .build(periode);
    }


    private static BeregningsgrunnlagPrStatusOgAndelDto getKorrektAndel(BeregningsgrunnlagPeriodeDto periode, FastsattBrukersAndel andel, Optional<BeregningsgrunnlagDto> forrigeBg) {
        if (andel.getLagtTilAvSaksbehandler() && !andel.getNyAndel()) {
            return finnAndelFraForrigeGrunnlag(periode, andel, forrigeBg);
        }
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAndelsnr().equals(andel.getAndelsnr()))
                .findFirst()
                .orElseThrow(() -> new KalkulatorException("FT-401646", "Finner ikke andelen for eksisterende grunnlag."));
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto finnAndelFraForrigeGrunnlag(BeregningsgrunnlagPeriodeDto periode, FastsattBrukersAndel andel, Optional<BeregningsgrunnlagDto> forrigeBg) {
        List<BeregningsgrunnlagPeriodeDto> matchendePerioder = forrigeBg.stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(periodeIGjeldendeGrunnlag -> periodeIGjeldendeGrunnlag.getPeriode().overlapper(periode.getPeriode())).collect(Collectors.toList());
        if (matchendePerioder.size() != 1) {
            throw MatchBeregningsgrunnlagTjeneste.fantFlereEnn1Periode();
        }
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelIForrigeGrunnlag = matchendePerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAndelsnr().equals(andel.getAndelsnr()))
                .findFirst();
        return andelIForrigeGrunnlag
                .orElseGet(() -> MatchBeregningsgrunnlagTjeneste
                        .matchMedAndelFraPeriode(periode, andel.getAndelsnr(), null));
    }
}
