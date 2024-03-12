package no.nav.folketrygdloven.kalkulator.steg;

import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat.Builder;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningResultat;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.ForeslåBesteberegning;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AvklaringsbehovUtlederFastsettBeregningsaktiviteter;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ForeslåSkjæringstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjenesteImpl;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.AvklaringsbehovUtlederFordelBeregning;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VilkårTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.fortsettForeslå.FortsettForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlagTjenesteVelger;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.AvklaringsbehovUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.OpprettBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.VurderRefusjonBeregningsgrunnlagFelles;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse.VurderRefusjonBeregningsgrunnlagPleiepenger;
import no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt.BeregningsperiodeFastsetter;
import no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt.SkjæringstidspunktFastsetter;
import no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt.AvklaringsbehovUtlederTilkommetInntekt;
import no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt.TilkommetInntektTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

/**
 * Fasadetjeneste for å delegere alle kall fra steg
 */
public class BeregningsgrunnlagTjeneste implements KalkulatorInterface {

    private final VilkårTjeneste vilkårTjeneste = new VilkårTjeneste();

    @Override
    public BeregningResultatAggregat fastsettBeregningsaktiviteter(FastsettBeregningsaktiviteterInput input) {
        validerIkkeFrisinn(input);
        var beregningsgrunnlagRegelResultat = new ForeslåSkjæringstidspunktTjeneste().foreslåSkjæringstidspunkt(input);
        var tidligereAktivitetOverstyring = hentTidligereOverstyringer(input);
        var avklaringsbehov = AvklaringsbehovUtlederFastsettBeregningsaktiviteter.utledTjeneste(input.getFagsakYtelseType())
                .utledAvklaringsbehov(beregningsgrunnlagRegelResultat, input, tidligereAktivitetOverstyring.isPresent());
        var vilkårResultat = vilkårTjeneste
                .lagVilkårResultatFastsettAktiviteter(input, beregningsgrunnlagRegelResultat.getVilkårsresultat());
        return BeregningResultatAggregat.Builder.fra(input)
                .medRegisterAktiviteter(beregningsgrunnlagRegelResultat.getRegisterAktiviteter())
                .medOverstyrteAktiviteter(vilkårResultat.isPresent() ? null : tidligereAktivitetOverstyring.orElse(null))
                .medBeregningsgrunnlag(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medVilkårResultat(vilkårResultat.orElse(null))
                .medAvklaringsbehov(avklaringsbehov)
                .build();
    }

    @Override
    public BeregningResultatAggregat fastsettBeregningsgrunnlag(StegProsesseringInput input) {
        validerIkkeFrisinn(input);
        var resultat = FullføreBeregningsgrunnlagTjenesteVelger.utledTjeneste(input.getFagsakYtelseType())
                .fullføreBeregningsgrunnlag(input);
        Builder resultatBuilder = Builder.fra(input)
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand());
        var vilkårResultat = vilkårTjeneste.lagVilkårResultatFullføre(input, resultat.getBeregningsgrunnlag());
        resultatBuilder.medVilkårResultat(vilkårResultat.orElse(null));
        return resultatBuilder.build();
    }

    @Override
    public BeregningResultatAggregat fordelBeregningsgrunnlag(StegProsesseringInput input) {
        validerIkkeFrisinn(input);
        var fordelResultat = new FordelBeregningsgrunnlagTjenesteImpl().omfordelBeregningsgrunnlag(input);
        var nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                .medBeregningsgrunnlag(fordelResultat.getBeregningsgrunnlag())
                .build(input.getStegTilstand());
        var avklaringsbehov = AvklaringsbehovUtlederFordelBeregning.utledAvklaringsbehovFor(
                input.getKoblingReferanse(),
                nyttGrunnlag,
                input.getYtelsespesifiktGrunnlag(),
                input.getIayGrunnlag(),
                input.getForlengelseperioder());
        return Builder.fra(input)
                .medAvklaringsbehov(avklaringsbehov)
                .medBeregningsgrunnlag(fordelResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(new RegelSporingAggregat(
                        fordelResultat.getRegelsporinger().map(RegelSporingAggregat::regelsporingerGrunnlag).orElse(Collections.emptyList()),
                        fordelResultat.getRegelsporinger().map(RegelSporingAggregat::regelsporingPerioder).stream().flatMap(Collection::stream)
                                .collect(Collectors.toList())))
                .build();
    }

    /**
     * Vurderer beregningsgrunnlagsvilkåret
     *
     * @param input Input til vurdering av vilkåret
     * @return Resultat av vilkårsvurdering
     */
    @Override
    public BeregningResultatAggregat vurderBeregningsgrunnlagvilkår(StegProsesseringInput input) {
        validerIkkeFrisinn(input);
        var vilkårVurderingResultat = new VurderBeregningsgrunnlagTjeneste()
                .vurderBeregningsgrunnlag(input, input.getBeregningsgrunnlagGrunnlag());
        BeregningsgrunnlagDto vurdertBeregningsgrunnlag = vilkårVurderingResultat.getBeregningsgrunnlag();
        var vilkårResultat = vilkårTjeneste
                .lagVilkårResultatFordel(input, vilkårVurderingResultat.getVilkårsresultat());
        return BeregningResultatAggregat.Builder.fra(input)
                .medAvklaringsbehov(vilkårVurderingResultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(vurdertBeregningsgrunnlag, input.getStegTilstand())
                .medVilkårResultat(vilkårResultat)
                .medRegelSporingAggregat(vilkårVurderingResultat.getRegelsporinger().orElse(null))
                .build();
    }


    /**
     * Vurderer tilkommet inntekt
     *
     * @param input Standard steginput
     * @return Resultat av vurdering av tilkommet inntekt: nytt bg og eventuelt aksjonspunkt
     */
    @Override
    public BeregningResultatAggregat vurderTilkommetInntekt(StegProsesseringInput input) {
        validerIkkeFrisinn(input);
        var bg = new TilkommetInntektTjeneste().vurderTilkommetInntekt(input);
        var avklaringsbehov = AvklaringsbehovUtlederTilkommetInntekt.utledAvklaringsbehovFor(
                BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag()).medBeregningsgrunnlag(bg).build(VURDERT_TILKOMMET_INNTEKT),
                input.getYtelsespesifiktGrunnlag(),
                input.getForlengelseperioder());
        return BeregningResultatAggregat.Builder.fra(input)
                .medAvklaringsbehov(avklaringsbehov)
                .medBeregningsgrunnlag(bg, input.getStegTilstand())
                .build();
    }


    /**
     * Vurderer peridoder med refusjon
     *
     * @param input Input til steget
     * @return Resultat av vurdering av refusjonskrav
     */
    @Override
    public BeregningResultatAggregat vurderRefusjonskravForBeregninggrunnlag(VurderRefusjonBeregningsgrunnlagInput input) {
        validerIkkeFrisinn(input);
        validerSynkronisertUttak(input);
        var vurderRefusjonResultat = FagsakYtelseType.PLEIEPENGER_SYKT_BARN.equals(input.getFagsakYtelseType())
                || FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE.equals(input.getFagsakYtelseType()) ?
                new VurderRefusjonBeregningsgrunnlagPleiepenger().vurderRefusjon(input) :
                new VurderRefusjonBeregningsgrunnlagFelles().vurderRefusjon(input);
        return Builder.fra(input)
                .medAvklaringsbehov(vurderRefusjonResultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(vurderRefusjonResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(vurderRefusjonResultat.getRegelsporinger().orElse(null))
                .build();
    }

    private void validerSynkronisertUttak(VurderRefusjonBeregningsgrunnlagInput input) {
        if (input.getYtelsespesifiktGrunnlag() instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            var harUttakForBrukersAndel = utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet()
                    .stream().anyMatch(a -> a.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.BRUKERS_ANDEL));
            var bgHarIkkeBrukersAndel = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .noneMatch(a -> a.getAktivitetStatus().equals(AktivitetStatus.BRUKERS_ANDEL));

            if (harUttakForBrukersAndel && bgHarIkkeBrukersAndel) {
                throw new IllegalStateException("Uttak og beregning i ugyldig tilstand. Saken må flippes til manuell revurdering og flyttes til start.");
            }
        }
    }

    /**
     * Foreslår besteberegning
     *
     * @param input Input til foreslå besteberegning
     * @return resultat av foreslått besteberegning
     */
    @Override
    public BesteberegningResultat foreslåBesteberegning(ForeslåBesteberegningInput input) {
        validerIkkeFrisinn(input);
        if (input.getBeregningsgrunnlag().isOverstyrt()) {
            // Skal ikkje gjere noko i steget om overstyrt inntekt
            return BesteberegningResultat.Builder.fra(input)
                    .medBeregningsgrunnlag(new BeregningsgrunnlagDto(input.getBeregningsgrunnlag()))
                    .build();
        }
        BesteberegningRegelResultat resultat = new ForeslåBesteberegning().foreslåBesteberegning(input);
        BeregningsgrunnlagVerifiserer.verifiserBesteberegnetBeregningsgrunnlag(resultat.getBeregningsgrunnlag());
        return BesteberegningResultat.Builder.fra(input)
                .medVurderingsgrunnlag(resultat.getBesteberegningVurderingGrunnlag())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    @Override
    public BeregningResultatAggregat foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input) {
        validerIkkeFrisinn(input);
        var resultat = new ForeslåBeregningsgrunnlag()
                .foreslåBeregningsgrunnlag(input);
        return BeregningResultatAggregat.Builder.fra(input)
                .medAvklaringsbehov(resultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    @Override
    public BeregningResultatAggregat fortsettForeslåBeregningsgrunnlag(FortsettForeslåBeregningsgrunnlagInput input) {
        validerIkkeFrisinn(input);
        var resultat = new FortsettForeslåBeregningsgrunnlag()
                .fortsettForeslåBeregningsgrunnlag(input);
        return BeregningResultatAggregat.Builder.fra(input)
                .medAvklaringsbehov(resultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    @Override
    public BeregningResultatAggregat kontrollerFaktaBeregningsgrunnlag(FaktaOmBeregningInput input) {
        validerIkkeFrisinn(input);
        var resultat = new OpprettBeregningsgrunnlagTjeneste().opprettOgLagreBeregningsgrunnlag(input,
                SkjæringstidspunktFastsetter.utledFastsettSkjæringstidspunktTjeneste(input.getFagsakYtelseType()),
                BeregningsperiodeFastsetter.utledFastsettBeregningsperiodeTjeneste(input.getFagsakYtelseType()));

        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(input.getStegTilstand());
        var avklaringsbehovresultat = new AvklaringsbehovUtlederFaktaOmBeregning()
                .utledAvklaringsbehovFor(input, nyttGrunnlag, harOverstyrtBergningsgrunnlag(input));

        BeregningsgrunnlagDto grunnlagMedTilfeller = BeregningsgrunnlagDto.builder(beregningsgrunnlag)
                .leggTilFaktaOmBeregningTilfeller(avklaringsbehovresultat.getFaktaOmBeregningTilfeller())
                .build();

        return BeregningResultatAggregat.Builder.fra(input)
                .medBeregningsgrunnlag(grunnlagMedTilfeller, input.getStegTilstand())
                .medFaktaAggregat(resultat.getFaktaAggregatDto(), input.getStegTilstand())
                .medAvklaringsbehov(avklaringsbehovresultat.getBeregningAvklaringsbehovResultatList())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    private boolean harOverstyrtBergningsgrunnlag(StegProsesseringInput input) {
        return input.getForrigeGrunnlagFraStegUt()
                .flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes)
                .stream()
                .anyMatch(BeregningsgrunnlagDto::isOverstyrt);
    }

    private Optional<BeregningAktivitetOverstyringerDto> hentTidligereOverstyringer(FastsettBeregningsaktiviteterInput input) {
        Optional<BeregningsgrunnlagGrunnlagDto> overstyrtGrunnlag = input.getForrigeGrunnlagFraStegUt();
        return overstyrtGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getOverstyring);
    }

    private void validerIkkeFrisinn(StegProsesseringInput input) {
        if (FagsakYtelseType.FRISINN.equals(input.getFagsakYtelseType())) {
            throw new IllegalStateException("Utviklerfeil: FRISINN til beregning");
        }
    }

}
