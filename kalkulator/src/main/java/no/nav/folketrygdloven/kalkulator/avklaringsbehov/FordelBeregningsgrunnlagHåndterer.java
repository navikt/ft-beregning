package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KalkulatorException;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelFastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class FordelBeregningsgrunnlagHåndterer {

    private FordelBeregningsgrunnlagHåndterer() {
        // skjul
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(FordelBeregningsgrunnlagDto dto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagPeriodeDto> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (FordelBeregningsgrunnlagPeriodeDto endretPeriode : dto.getEndretBeregningsgrunnlagPerioder()) {
            fastsettVerdierForPeriode(input, perioder, endretPeriode);
        }



        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FASTSATT_INN);
    }


    private static void fastsettVerdierForPeriode(HåndterBeregningsgrunnlagInput input,
                                                  List<BeregningsgrunnlagPeriodeDto> perioder,
                                                  FordelBeregningsgrunnlagPeriodeDto endretPeriode) {
        BeregningsgrunnlagPeriodeDto korrektPeriode = getKorrektPeriode(input, perioder, endretPeriode);
        var refusjonMap = FordelRefusjonTjeneste.getRefusjonPrÅrMap(endretPeriode, korrektPeriode);
        BeregningsgrunnlagPeriodeDto.Builder perioderBuilder = BeregningsgrunnlagPeriodeDto.oppdater(korrektPeriode);
        // Må sortere med eksisterende først for å sette andelsnr på disse først
        List<FordelBeregningsgrunnlagAndelDto> sorted = sorterMedNyesteSist(endretPeriode);

        var andelerSomErLagtTilTidligere = korrektPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getKilde().equals(AndelKilde.SAKSBEHANDLER_FORDELING))
                .collect(Collectors.toList());

        var fordelteAndelsnr = new HashSet<Long>();
        for (FordelBeregningsgrunnlagAndelDto endretAndel : sorted) {
            var builderFraAktivtGrunnlag = perioderBuilder.getBuilderForAndel(endretAndel.getAndelsnr(), !endretAndel.erNyAndel());
            if (builderFraAktivtGrunnlag.isPresent()) {
                fordel(refusjonMap, perioderBuilder, endretAndel, builderFraAktivtGrunnlag.get(), endretAndel.erNyAndel());
                fordelteAndelsnr.add(endretAndel.getAndelsnr());
            } else {
                var builderFraForrigeGrunnlag = lagBuilderFraForrigeGrunnlagEllerNy(input.getForrigeGrunnlagFraHåndteringTilstand(), endretPeriode.getFom(), endretAndel);
                var nyAndel = fordel(refusjonMap, perioderBuilder, endretAndel, builderFraForrigeGrunnlag, true);
                fordelteAndelsnr.add(nyAndel.getAndelsnr());
            }
        }

        var andelerSomSkalFjernes = andelerSomErLagtTilTidligere.stream()
                .filter(a -> !fordelteAndelsnr.contains(a.getAndelsnr()))
                .collect(Collectors.toList());
        andelerSomSkalFjernes.forEach(a -> BeregningsgrunnlagPeriodeDto.oppdater(korrektPeriode).fjernBeregningsgrunnlagPrStatusOgAndel(a));
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto fordel(Map<FordelBeregningsgrunnlagAndelDto, Beløp> refusjonMap,
                                                               BeregningsgrunnlagPeriodeDto.Builder perioderBuilder,
                                                               FordelBeregningsgrunnlagAndelDto endretAndel,
                                                               BeregningsgrunnlagPrStatusOgAndelDto.Builder builderForAndel, boolean skalLeggeTilAndel) {
        fastsettVerdierForAndel(builderForAndel, refusjonMap, endretAndel);
        if (skalLeggeTilAndel) {
            builderForAndel.medAndelsnr(null);
            builderForAndel.medKilde(AndelKilde.SAKSBEHANDLER_FORDELING);
            builderForAndel.medBeregnetPrÅr(null);
            builderForAndel.medOverstyrtPrÅr(null);
            builderForAndel.medBesteberegningPrÅr(null);
            builderForAndel.medFordeltPrÅr(null);
            builderForAndel.medInntektskategoriAutomatiskFordeling(null);
            perioderBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(builderForAndel);
        }
        return builderForAndel.build();
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder lagBuilderFraForrigeGrunnlagEllerNy(Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraHåndteringTilstand,
                                                                                                    LocalDate fom, FordelBeregningsgrunnlagAndelDto endretAndel) {
        return forrigeGrunnlagFraHåndteringTilstand.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes)
                .stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(p -> p.getPeriode().getFomDato().equals(fom))
                .findFirst()
                .stream()
                .flatMap(a -> a.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .filter(a -> a.getAndelsnr().equals(endretAndel.getAndelsnr()))
                .findFirst()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::kopier)
                .orElseThrow();
    }

    private static void fastsettVerdierForAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder builderForAndel,
                                                Map<FordelBeregningsgrunnlagAndelDto, Beløp> refusjonMap,
                                                FordelBeregningsgrunnlagAndelDto endretAndel) {
        FordelFastsatteVerdierDto fastsatteVerdier = endretAndel.getFastsatteVerdier();
        FordelFastsatteVerdierDto verdierMedJustertRefusjon = lagVerdierMedFordeltRefusjon(refusjonMap, endretAndel, fastsatteVerdier);
        settVerdierPåBuilder(builderForAndel, verdierMedJustertRefusjon);
    }

    private static void settVerdierPåBuilder(BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder,
                                             FordelFastsatteVerdierDto verdierMedJustertRefusjon) {
        andelBuilder.medInntektskategoriManuellFordeling(verdierMedJustertRefusjon.getInntektskategori())
                .medManueltFordeltPrÅr(verdierMedJustertRefusjon.finnEllerUtregnFastsattBeløpPrÅr())
                .medFastsattAvSaksbehandler(true);
        andelBuilder.oppdaterArbeidsforholdHvisFinnes()
                .ifPresent(b -> b.medNaturalytelseBortfaltPrÅr(null) // Ved omfordeling må vi nulle ut naturalytelse og inkludere dette i det fordelt beløp
                        .medNaturalytelseTilkommetPrÅr(null)
                        .medManueltFordeltRefusjonPrÅr(Beløp.fra(verdierMedJustertRefusjon.getRefusjonPrÅr())));
    }

    private static FordelFastsatteVerdierDto lagVerdierMedFordeltRefusjon(Map<FordelBeregningsgrunnlagAndelDto, Beløp> refusjonMap,
                                                                          FordelBeregningsgrunnlagAndelDto endretAndel,
                                                                          FordelFastsatteVerdierDto fastsatteVerdier) {
        return FordelFastsatteVerdierDto.Builder.oppdater(fastsatteVerdier)
                .medRefusjonPrÅr(Beløp.safeVerdi(refusjonMap.get(endretAndel)) != null ? refusjonMap.get(endretAndel).intValue() : null)
                .build();
    }

    private static BeregningsgrunnlagPeriodeDto getKorrektPeriode(BeregningsgrunnlagInput input,
                                                                  List<BeregningsgrunnlagPeriodeDto> perioder,
                                                                  FordelBeregningsgrunnlagPeriodeDto endretPeriode) {
        return perioder.stream()
                .filter(periode -> periode.getBeregningsgrunnlagPeriodeFom().equals(endretPeriode.getFom()))
                .findFirst()
                .orElseThrow(() -> new KalkulatorException("FT-401647", String.format("Finner ikke periode for eksisterende grunnlag. Behandling  %s", input.getKoblingReferanse().getKoblingId())));
    }

    private static List<FordelBeregningsgrunnlagAndelDto> sorterMedNyesteSist(FordelBeregningsgrunnlagPeriodeDto endretPeriode) {
        Comparator<FordelBeregningsgrunnlagAndelDto> FordelBeregningsgrunnlagAndelDtoComparator = (a1, a2) -> {
            if (a1.erNyAndel()) {
                return 1;
            }
            if (a2.erNyAndel()) {
                return -1;
            }
            return 0;
        };
        return endretPeriode.getAndeler().stream().sorted(FordelBeregningsgrunnlagAndelDtoComparator).collect(Collectors.toList());
    }

}
