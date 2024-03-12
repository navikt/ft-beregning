package no.nav.folketrygdloven.kalkulator.steg;

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
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjenesteImpl;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.AvklaringsbehovUtlederFordelBeregning;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.OpprettBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse.VurderRefusjonBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.AvklaringsbehovUtlederFaktaOmBeregningFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.AvklaringsbehovUtlederFastsettBeregningsaktiviteterFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FastsettBeregningsperiodeTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FastsettSkjæringstidspunktOgStatuserFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.ForeslåBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.ForeslåSkjæringstidspunktTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FortsettForeslåBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FullføreBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.VilkårTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.VurderBeregningsgrunnlagTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

/**
 * Fasadetjeneste for å delegere alle kall fra steg
 */
public class BeregningsgrunnlagFRISINNTjeneste implements KalkulatorInterface {

    private final VilkårTjenesteFRISINN vilkårTjeneste = new VilkårTjenesteFRISINN();

    @Override
    public BeregningResultatAggregat fastsettBeregningsaktiviteter(FastsettBeregningsaktiviteterInput input) {
        validerFrisinn(input);
        var beregningsgrunnlagRegelResultat = new ForeslåSkjæringstidspunktTjenesteFRISINN().foreslåSkjæringstidspunkt(input);
        var tidligereAktivitetOverstyring = hentTidligereOverstyringer(input);
        var avklaringsbehov = new AvklaringsbehovUtlederFastsettBeregningsaktiviteterFRISINN()
                .utledAvklaringsbehov(beregningsgrunnlagRegelResultat);
        var vilkårResultat = vilkårTjeneste
                .lagVilkårResultatFastsettAktiviteter(input, beregningsgrunnlagRegelResultat.getVilkårsresultat());
        return Builder.fra(input)
                .medRegisterAktiviteter(beregningsgrunnlagRegelResultat.getRegisterAktiviteter())
                .medOverstyrteAktiviteter(vilkårResultat.isPresent() ? null : tidligereAktivitetOverstyring.orElse(null))
                .medBeregningsgrunnlag(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medVilkårResultat(vilkårResultat.orElse(null))
                .medAvklaringsbehov(avklaringsbehov)
                .build();
    }

    @Override
    public BeregningResultatAggregat fastsettBeregningsgrunnlag(StegProsesseringInput input) {
        validerFrisinn(input);
        var resultat = new FullføreBeregningsgrunnlagFRISINN().fullføreBeregningsgrunnlag(input);
        Builder resultatBuilder = Builder.fra(input)
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand());
        var vilkårResultat = vilkårTjeneste.lagVilkårResultatFullføre(input, resultat.getBeregningsgrunnlag());
        resultatBuilder.medVilkårResultat(vilkårResultat.orElse(null));
        return resultatBuilder.build();
    }

    @Override
    public BeregningResultatAggregat fordelBeregningsgrunnlag(StegProsesseringInput input) {
        validerFrisinn(input);
        var fordelResultat = new FordelBeregningsgrunnlagTjenesteImpl()
                .omfordelBeregningsgrunnlag(input);
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
        validerFrisinn(input);
        var vilkårVurderingResultat = new VurderBeregningsgrunnlagTjenesteFRISINN()
                .vurderBeregningsgrunnlag(input, input.getBeregningsgrunnlagGrunnlag());
        BeregningsgrunnlagDto vurdertBeregningsgrunnlag = vilkårVurderingResultat.getBeregningsgrunnlag();
        var vilkårResultat = vilkårTjeneste
                .lagVilkårResultatFordel(input, vilkårVurderingResultat.getVilkårsresultat());
        return Builder.fra(input)
                .medAvklaringsbehov(vilkårVurderingResultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(vurdertBeregningsgrunnlag, input.getStegTilstand())
                .medVilkårResultat(vilkårResultat)
                .medRegelSporingAggregat(vilkårVurderingResultat.getRegelsporinger().orElse(null))
                .build();
    }

    @Override
    public BeregningResultatAggregat vurderTilkommetInntekt(StegProsesseringInput input) {
        validerFrisinn(input);
        throw new IllegalStateException("Utviklerfeil: Frisinn skal ikke vurdere tilkommet inntekt");
    }


    /**
     * Vurderer peridoder med refusjon
     *
     * @param input Input til steget
     * @return Resultat av vurdering av refusjonskrav
     */
    @Override
    public BeregningResultatAggregat vurderRefusjonskravForBeregninggrunnlag(VurderRefusjonBeregningsgrunnlagInput input) {
        validerFrisinn(input);
        validerSynkronisertUttak(input);
        var vurderRefusjonResultat = new VurderRefusjonBeregningsgrunnlagFRISINN().vurderRefusjon(input);
        return Builder.fra(input)
                .medAvklaringsbehov(vurderRefusjonResultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(vurderRefusjonResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(vurderRefusjonResultat.getRegelsporinger().orElse(null))
                .build();
    }

    private void validerSynkronisertUttak(VurderRefusjonBeregningsgrunnlagInput input) {
        validerFrisinn(input);
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
        validerFrisinn(input);
        throw new IllegalStateException("Utviklerfeil: Frisinn skal ikke besteberegnes");
    }

    @Override
    public BeregningResultatAggregat foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input) {
        validerFrisinn(input);
        var resultat = new ForeslåBeregningsgrunnlagFRISINN()
                .foreslåBeregningsgrunnlag(input);
        return Builder.fra(input)
                .medAvklaringsbehov(resultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    @Override
    public BeregningResultatAggregat fortsettForeslåBeregningsgrunnlag(FortsettForeslåBeregningsgrunnlagInput input) {
        validerFrisinn(input);
        var resultat = new FortsettForeslåBeregningsgrunnlagFRISINN()
                .fortsettForeslåBeregningsgrunnlag(input);
        return Builder.fra(input)
                .medAvklaringsbehov(resultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    @Override
    public BeregningResultatAggregat kontrollerFaktaBeregningsgrunnlag(FaktaOmBeregningInput input) {
        validerFrisinn(input);
        var resultat = new OpprettBeregningsgrunnlagTjeneste().opprettOgLagreBeregningsgrunnlag(input,
                FastsettSkjæringstidspunktOgStatuserFRISINN::fastsett,
                FastsettBeregningsperiodeTjenesteFRISINN::fastsettBeregningsperiode);

        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(input.getStegTilstand());
        var avklaringsbehovresultat = new AvklaringsbehovUtlederFaktaOmBeregningFRISINN()
                .utledAvklaringsbehovFor(input, nyttGrunnlag);

        BeregningsgrunnlagDto grunnlagMedTilfeller = BeregningsgrunnlagDto.builder(beregningsgrunnlag)
                .leggTilFaktaOmBeregningTilfeller(avklaringsbehovresultat.getFaktaOmBeregningTilfeller())
                .build();

        return Builder.fra(input)
                .medBeregningsgrunnlag(grunnlagMedTilfeller, input.getStegTilstand())
                .medFaktaAggregat(resultat.getFaktaAggregatDto(), input.getStegTilstand())
                .medAvklaringsbehov(avklaringsbehovresultat.getBeregningAvklaringsbehovResultatList())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    private Optional<BeregningAktivitetOverstyringerDto> hentTidligereOverstyringer(FastsettBeregningsaktiviteterInput input) {
        Optional<BeregningsgrunnlagGrunnlagDto> overstyrtGrunnlag = input.getForrigeGrunnlagFraStegUt();
        return overstyrtGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getOverstyring);
    }

    private void validerFrisinn(StegProsesseringInput input) {
        if (!FagsakYtelseType.FRISINN.equals(input.getFagsakYtelseType())) {
            throw new IllegalStateException("Utviklerfeil: Feil ytelse til FRISINN-beregning " + input.getFagsakYtelseType().getKode());
        }
    }

}
