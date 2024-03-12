package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FrisinnBehandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;

public class VilkårTjenesteFRISINN  {

    // Samme som VilkårTjeneste
    public Optional<BeregningVilkårResultat> lagVilkårResultatFastsettAktiviteter(BeregningsgrunnlagInput input, List<BeregningVilkårResultat> beregningVilkårResultatListe) {
        boolean erAvslått = beregningVilkårResultatListe.stream().anyMatch(vp -> !vp.getErVilkårOppfylt());
        Intervall vilkårsperiode = Intervall.fraOgMedTilOgMed(input.getSkjæringstidspunktOpptjening(), TIDENES_ENDE);
        if (erAvslått) {
            Optional<BeregningVilkårResultat> avslåttVilkår = beregningVilkårResultatListe.stream().filter(vr -> !vr.getErVilkårOppfylt()).findFirst();
            return Optional.of(avslåttVilkår.map(beregningVilkårResultat -> lagAvslag(vilkårsperiode, beregningVilkårResultat))
                    .orElseThrow(() -> new IllegalStateException("Forventer å finne vilkår med avslag.")));
        }
        return Optional.empty();
    }

    public BeregningVilkårResultat lagVilkårResultatFordel(BeregningsgrunnlagInput input, List<BeregningVilkårResultat> beregningVilkårResultatListe) {
        boolean erAvslått = erBeregningsgrunnlagVilkåretAvslått(input, beregningVilkårResultatListe);
        Intervall vilkårsperiode = Intervall.fraOgMedTilOgMed(input.getSkjæringstidspunktForBeregning(), TIDENES_ENDE);
        if (erAvslått) {
            Optional<BeregningVilkårResultat> avslåttVilkår = beregningVilkårResultatListe.stream().filter(vr -> !vr.getErVilkårOppfylt()).findFirst();
            return avslåttVilkår
                    .map(beregningVilkårResultat -> new BeregningVilkårResultat(false, beregningVilkårResultat.getVilkårsavslagsårsak(), vilkårsperiode))
                    .orElseGet(() -> new BeregningVilkårResultat(true, vilkårsperiode));
        } else {
            return new BeregningVilkårResultat(true, vilkårsperiode);
        }
    }

    public Optional<BeregningVilkårResultat> lagVilkårResultatFullføre(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlagDto) {
        boolean harAvkortetHeleSistePeriode = harAvslagGrunnetAvkorting(beregningsgrunnlagDto, input.getYtelsespesifiktGrunnlag());
        Intervall vilkårsperiode = Intervall.fraOgMedTilOgMed(input.getSkjæringstidspunktForBeregning(), TIDENES_ENDE);
        return harAvkortetHeleSistePeriode ? Optional.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.AVKORTET_GRUNNET_ANNEN_INNTEKT, vilkårsperiode)) :
                Optional.empty();
    }

    private BeregningVilkårResultat lagAvslag(Intervall vilkårsperiode, BeregningVilkårResultat beregningVilkårResultat) {
        return new BeregningVilkårResultat(false, beregningVilkårResultat.getVilkårsavslagsårsak(), vilkårsperiode);
    }

    private Boolean erBeregningsgrunnlagVilkåretAvslått(BeregningsgrunnlagInput input, List<BeregningVilkårResultat> beregningVilkårResultatListe) {
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        if (frisinnGrunnlag.getFrisinnBehandlingType().equals(FrisinnBehandlingType.REVURDERING)) {
            return erAlleSøknadperioderAvslått(input, beregningVilkårResultatListe);
        }
        return erSisteSøknadsperiodeAvslått(input, beregningVilkårResultatListe);
    }

    private Boolean erAlleSøknadperioderAvslått(BeregningsgrunnlagInput input, List<BeregningVilkårResultat> beregningVilkårResultatListe) {
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        var søktePerioder = frisinnGrunnlag.getFrisinnPerioder().stream().map(FrisinnPeriode::getPeriode).collect(Collectors.toList());
        return beregningVilkårResultatListe.stream()
                .filter(vp -> søktePerioder.stream().anyMatch(p -> vp.getPeriode().overlapper(p)))
                .noneMatch(BeregningVilkårResultat::getErVilkårOppfylt);
    }

    private boolean harAvslagGrunnetAvkorting(BeregningsgrunnlagDto beregningsgrunnlagDto, FrisinnGrunnlag frisinnGrunnlag) {
        if (frisinnGrunnlag.getFrisinnBehandlingType().equals(FrisinnBehandlingType.REVURDERING)) {
            return erAllePerioderAvkortetTilNull(beregningsgrunnlagDto, frisinnGrunnlag);
        }
        return erSistePeriodeAvkortetTilNull(beregningsgrunnlagDto, frisinnGrunnlag);
    }

    private boolean erAllePerioderAvkortetTilNull(BeregningsgrunnlagDto beregningsgrunnlagDto, FrisinnGrunnlag frisinnGrunnlag) {
        var søktePerioder = frisinnGrunnlag.getFrisinnPerioder().stream().map(FrisinnPeriode::getPeriode).collect(Collectors.toList());
        return beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().stream()
                .filter(bgPeriode -> søktePerioder.stream().anyMatch(søktPeriode -> bgPeriode.getPeriode().overlapper(søktPeriode)))
                .allMatch(p -> harAvkortetGrunnetAnnenInntekt(frisinnGrunnlag, p));
    }

    private boolean erSistePeriodeAvkortetTilNull(BeregningsgrunnlagDto beregningsgrunnlagDto, FrisinnGrunnlag frisinnGrunnlag) {
        Intervall sisteSøknadsperiode = FinnSøknadsperioder.finnSisteSøknadsperiode(frisinnGrunnlag);
        return beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getPeriode().overlapper(sisteSøknadsperiode))
                .allMatch(p -> harAvkortetGrunnetAnnenInntekt(frisinnGrunnlag, p));
    }

    private Boolean erSisteSøknadsperiodeAvslått(BeregningsgrunnlagInput input, List<BeregningVilkårResultat> beregningVilkårResultatListe) {
        Intervall sisteSøknadsperiode = FinnSøknadsperioder.finnSisteSøknadsperiode(input.getYtelsespesifiktGrunnlag());
        return beregningVilkårResultatListe.stream()
                .filter(vp -> vp.getPeriode().overlapper(sisteSøknadsperiode))
                .noneMatch(BeregningVilkårResultat::getErVilkårOppfylt);
    }

    private boolean harAvkortetGrunnetAnnenInntekt(FrisinnGrunnlag frisinnGrunnlag, BeregningsgrunnlagPeriodeDto p) {
        LocalDate fom = p.getBeregningsgrunnlagPeriodeFom();
        boolean harSøktFrilans = frisinnGrunnlag.getSøkerYtelseForFrilans(fom);
        boolean harSøktNæring = frisinnGrunnlag.getSøkerYtelseForNæring(fom);
        boolean harAvkortetSøktNæring = harAvkortetSøktNæring(p, harSøktNæring);
        boolean harAvkortetSøktFrilans = harAvkortetSøktFrilans(harSøktFrilans, p);
        if ((harSøktFrilans && harAvkortetSøktFrilans) && !harSøktNæring) {
            return true;
        }
        if ((harSøktNæring && harAvkortetSøktNæring) && !harSøktFrilans) {
            return true;
        }
        if (harAvkortetSøktFrilans && harAvkortetSøktNæring) {
            return true;
        }
        return false;
    }

    private boolean harAvkortetSøktNæring(BeregningsgrunnlagPeriodeDto periode, boolean harSøktNæring) {
        return harSøktNæring && harIkkeUtbetalingForNæring(periode);
    }

    private boolean harAvkortetSøktFrilans(boolean harSøktFrilans, BeregningsgrunnlagPeriodeDto periode) {
        return harSøktFrilans && harIkkeUtbetalingForFrilans(periode);
    }

    private Boolean harIkkeUtbetalingForFrilans(BeregningsgrunnlagPeriodeDto p) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erFrilanser())
                .findFirst()
                .map(a -> a.getDagsats().equals(0L))
                .orElse(true);
    }

    private Boolean harIkkeUtbetalingForNæring(BeregningsgrunnlagPeriodeDto p) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                .findFirst()
                .map(a -> a.getDagsats().equals(0L))
                .orElse(true);
    }

}
