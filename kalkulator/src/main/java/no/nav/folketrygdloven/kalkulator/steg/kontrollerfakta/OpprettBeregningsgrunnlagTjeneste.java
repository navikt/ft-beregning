package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;


import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.inntektskategori.FastsettInntektskategoriTjeneste.fastsettInntektskategori;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagVerifiserer;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaTjenesteK14;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaTjenesteOMP;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaTjenestePleiepenger;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering.FastsettNaturalytelsePerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt.BeregningsperiodeFastsetter;
import no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt.SkjæringstidspunktFastsetter;


public class OpprettBeregningsgrunnlagTjeneste {

    private final FastsettNaturalytelsePerioderTjeneste fastsettNaturalytelsePerioderTjeneste = new FastsettNaturalytelsePerioderTjeneste();

    /**
     * Henter inn grunnlagsdata om nødvendig
     * Oppretter og bygger beregningsgrunnlag for behandlingen
     * Oppretter perioder og andeler på beregningsgrunnlag
     * Setter inntektskategori på andeler
     * Splitter perioder basert på naturalytelse.
     *
     * @param input                        en {@link BeregningsgrunnlagInput}
     * @param skjæringstidspunktFastsetter
     * @param beregningsperiodeFastsetter
     */
    public BeregningsgrunnlagRegelResultat opprettOgLagreBeregningsgrunnlag(FaktaOmBeregningInput input,
                                                                            SkjæringstidspunktFastsetter skjæringstidspunktFastsetter,
                                                                            BeregningsperiodeFastsetter beregningsperiodeFastsetter) {
        var ref = input.getKoblingReferanse();
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        BeregningAktivitetAggregatDto beregningAktiviteter = grunnlag.getGjeldendeAktiviteter();

        // Fastsetter andeler, status og endelig skjæringstidpsunkt
        var resultatMedAndeler = skjæringstidspunktFastsetter
                .fastsettSkjæringstidspunktOgStatuser(input, beregningAktiviteter, GrunnbeløpMapper.mapGrunnbeløpInput(input.getGrunnbeløpInput()));

        // Oppdaterer koblinginformasjon for videre prosessering
        KoblingReferanse refMedSkjæringstidspunkt = ref
                .medSkjæringstidspunkt(oppdaterSkjæringstidspunktForBeregning(beregningAktiviteter, resultatMedAndeler.getBeregningsgrunnlag()));
        BeregningsgrunnlagInput newInput = input.medBehandlingReferanse(refMedSkjæringstidspunkt);

        // Fastsett inntektskategorier
        var medFastsattInntektskategori = fastsettInntektskategori(resultatMedAndeler.getBeregningsgrunnlag(), input.getIayGrunnlag());

        // Fastsett beregningsperiode
        var medFastsattBeregningsperiode = beregningsperiodeFastsetter
                .fastsettBeregningsperiode(medFastsattInntektskategori, input.getIayGrunnlag(), input.getInntektsmeldinger());

        // Fastsett fakta
        Optional<FaktaAggregatDto> faktaAggregatDto = lagFaktaAggregat(input, medFastsattBeregningsperiode);

        // Oppretter perioder for endring i naturalytelse (tilkommet/bortfalt)
        var resultatMedNaturalytelse = fastsettNaturalytelsePerioderTjeneste.fastsettPerioderForNaturalytelse(newInput, medFastsattBeregningsperiode);

        BeregningsgrunnlagVerifiserer.verifiserOppdatertBeregningsgrunnlag(resultatMedNaturalytelse.getBeregningsgrunnlag(), new PerioderTilVurderingTjeneste(null, resultatMedNaturalytelse.getBeregningsgrunnlag()));
        return new BeregningsgrunnlagRegelResultat(resultatMedNaturalytelse.getBeregningsgrunnlag(),
                faktaAggregatDto.orElse(null),
                RegelSporingAggregat.konkatiner(resultatMedAndeler.getRegelsporinger().orElse(null),
                        resultatMedNaturalytelse.getRegelsporinger().orElse(null)));
    }

    private Skjæringstidspunkt oppdaterSkjæringstidspunktForBeregning(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                      BeregningsgrunnlagDto beregningsgrunnlag) {
        return Skjæringstidspunkt.builder()
                .medSkjæringstidspunktOpptjening(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening())
                .medSkjæringstidspunktBeregning(beregningsgrunnlag.getSkjæringstidspunkt()).build();
    }

    private Optional<FaktaAggregatDto> lagFaktaAggregat(FaktaOmBeregningInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        return switch (input.getFagsakYtelseType()) {
            case FORELDREPENGER, SVANGERSKAPSPENGER -> new FastsettFaktaTjenesteK14().fastsettFakta(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
            case OPPLÆRINGSPENGER, PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE ->
                    new FastsettFaktaTjenestePleiepenger().fastsettFakta(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
            case OMSORGSPENGER -> new FastsettFaktaTjenesteOMP().fastsettFakta(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
            default -> Optional.empty();
        };
    }

}
