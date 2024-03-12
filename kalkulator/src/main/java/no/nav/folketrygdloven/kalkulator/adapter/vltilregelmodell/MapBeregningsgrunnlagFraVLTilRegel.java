package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.psb.PleiepengerGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.svp.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.util.BeregningsgrunnlagUtil;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapInntektskategoriFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapPeriodeÅrsakFraVlTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse.FrisinnGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse.OmsorgspengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FullføreBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulator.input.VurderBeregningsgrunnlagvilkårInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeÅrsakDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;

public class MapBeregningsgrunnlagFraVLTilRegel {

    public Beregningsgrunnlag map(BeregningsgrunnlagInput input,
                                  BeregningsgrunnlagGrunnlagDto oppdatertGrunnlag) {
        var ref = input.getKoblingReferanse();
        Objects.requireNonNull(ref, "BehandlingReferanse kan ikke være null!");
        Objects.requireNonNull(oppdatertGrunnlag, "BeregningsgrunnlagGrunnlag kan ikke være null");
        BeregningsgrunnlagDto beregningsgrunnlag = oppdatertGrunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "Beregningsgrunnlag kan ikke være null!");

        List<AktivitetStatusMedHjemmel> aktivitetStatuser = beregningsgrunnlag.getAktivitetStatuser().stream()
                .map(this::mapVLAktivitetStatusMedHjemmel)
                .sorted()
                .collect(Collectors.toList());

        var inntektsgrunnlag = finnMapper(ref.getFagsakYtelseType()).map(input, beregningsgrunnlag.getSkjæringstidspunkt());
        List<BeregningsgrunnlagPeriode> perioder = mapBeregningsgrunnlagPerioder(beregningsgrunnlag, input);
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();

        var builder = Beregningsgrunnlag.builder();

        return builder
                .medInntektsgrunnlag(inntektsgrunnlag)
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medAktivitetStatuser(aktivitetStatuser)
                .medBeregningsgrunnlagPerioder(perioder)
                .medGrunnbeløp(beregningsgrunnlag.getGrunnbeløp().verdi())
                .medYtelsesdagerIEtÅr(KonfigTjeneste.getYtelsesdagerIÅr())
                .medAvviksgrenseProsent(KonfigTjeneste.getAvviksgrenseProsent())
                .medYtelsesSpesifiktGrunnlag(mapYtelsesSpesifiktGrunnlag(input, beregningsgrunnlag))
                // Verdier som kun brukes av FORESLÅ (skille ut i egen mapping?)
                .medAntallGMilitærHarKravPå(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).antallGMilitærHarKravPå().intValue())
                .medAntallGØvreGrenseverdi(KonfigTjeneste.getAntallGØvreGrenseverdi())
                .medUregulertGrunnbeløp(Beløp.safeVerdi(mapUregulertGrunnbeløp(input, beregningsgrunnlag)))
                .medMidlertidigInaktivType(mapMidlertidigInaktivType(input))
                .medGrunnbeløpSatser(grunnbeløpSatser(input))
                .medFomDatoForIndividuellSammenligningATFLSN(LocalDate.of(2023,1,1))
                .build();
    }

    private MapInntektsgrunnlagVLTilRegel finnMapper(FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OMSORGSPENGER, OPPLÆRINGSPENGER, FORELDREPENGER, SVANGERSKAPSPENGER -> new MapInntektsgrunnlagVLTilRegelFelles();
            case FRISINN -> new MapInntektsgrunnlagVLTilRegelFRISINN();
            default -> throw new IllegalArgumentException("Finner ikke MapInntektsgrunnlagVLTilRegel implementasjon for ytelse " + ytelseType);
        };
    }

    private List<Grunnbeløp> grunnbeløpSatser(BeregningsgrunnlagInput input) {
        if (input instanceof ForeslåBeregningsgrunnlagInput foreslåBeregningsgrunnlagInput) {
            return GrunnbeløpMapper.mapGrunnbeløpInput(foreslåBeregningsgrunnlagInput.getGrunnbeløpInput());
        } else if (input instanceof FortsettForeslåBeregningsgrunnlagInput fortsettForeslåBeregningsgrunnlagInput) {
            return GrunnbeløpMapper.mapGrunnbeløpInput(fortsettForeslåBeregningsgrunnlagInput.getGrunnbeløpInput());
        }
        return Collections.emptyList();
    }

    private MidlertidigInaktivType mapMidlertidigInaktivType(BeregningsgrunnlagInput input) {
        if (input.getOpptjeningAktiviteter() == null) {
            return null;
        }
        var midlertidigInaktivType = input.getOpptjeningAktiviteter().getMidlertidigInaktivType();
        return midlertidigInaktivType != null ?
                MidlertidigInaktivType.valueOf(midlertidigInaktivType.name()) :
                null;
    }

    private Beløp mapUregulertGrunnbeløp(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        if (input instanceof VurderBeregningsgrunnlagvilkårInput vurderBeregningsgrunnlagvilkårInput) {
            return vurderBeregningsgrunnlagvilkårInput.getUregulertGrunnbeløp().orElse(beregningsgrunnlag.getGrunnbeløp());
        }
        if (input instanceof VurderRefusjonBeregningsgrunnlagInput vurderRefusjonBeregningsgrunnlagInput) {
            return vurderRefusjonBeregningsgrunnlagInput.getUregulertGrunnbeløp().orElse(beregningsgrunnlag.getGrunnbeløp());
        }
        if (input instanceof FordelBeregningsgrunnlagInput fordelBeregningsgrunnlagInput) {
            return fordelBeregningsgrunnlagInput.getUregulertGrunnbeløp().orElse(beregningsgrunnlag.getGrunnbeløp());
        }
        if (input instanceof FullføreBeregningsgrunnlagInput fullføreBeregningsgrunnlagInput) {
            return fullføreBeregningsgrunnlagInput.getUregulertGrunnbeløp().orElse(beregningsgrunnlag.getGrunnbeløp());
        }
        return null;
    }

    private YtelsesSpesifiktGrunnlag mapYtelsesSpesifiktGrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        return switch (input.getFagsakYtelseType()) {
            case FORELDREPENGER ->  new ForeldrepengerGrunnlagMapper().map(beregningsgrunnlag);
            case SVANGERSKAPSPENGER -> new SvangerskapspengerGrunnlag();
            case OMSORGSPENGER -> new OmsorgspengerGrunnlagMapper().map(beregningsgrunnlag, input);
            case OPPLÆRINGSPENGER, PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE -> new PleiepengerGrunnlag(input.getFagsakYtelseType().getKode());
            case FRISINN -> new FrisinnGrunnlagMapper().map(input);
            default -> null;
        };
    }

    private List<PeriodeÅrsak> mapPeriodeÅrsak(List<BeregningsgrunnlagPeriodeÅrsakDto> beregningsgrunnlagPeriodeÅrsaker) {
        return beregningsgrunnlagPeriodeÅrsaker.stream().map(BeregningsgrunnlagPeriodeÅrsakDto::getPeriodeÅrsak).map(MapPeriodeÅrsakFraVlTilRegel::map).collect(Collectors.toList());
    }

    private AktivitetStatusMedHjemmel mapVLAktivitetStatusMedHjemmel(final BeregningsgrunnlagAktivitetStatusDto vlBGAktivitetStatus) {
        BeregningsgrunnlagHjemmel hjemmel = null;
        if (!Hjemmel.UDEFINERT.equals(vlBGAktivitetStatus.getHjemmel())) {
            try {
                hjemmel = BeregningsgrunnlagHjemmel.valueOf(vlBGAktivitetStatus.getHjemmel().getKode());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Ukjent Hjemmel: (" + vlBGAktivitetStatus.getHjemmel().getKode() + ").", e);
            }
        }
        AktivitetStatus as = mapVLAktivitetStatus(vlBGAktivitetStatus.getAktivitetStatus());
        return new AktivitetStatusMedHjemmel(as, hjemmel);
    }

    private AktivitetStatus mapVLAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus vlBGAktivitetStatus) {
        if (BeregningsgrunnlagUtil.erATFL(vlBGAktivitetStatus)) {
            return AktivitetStatus.ATFL;
        }

        try {
            return AktivitetStatus.valueOf(vlBGAktivitetStatus.getKode());
        } catch (IllegalArgumentException e) {
            if (BeregningsgrunnlagUtil.erATFL_SN(vlBGAktivitetStatus)) {
                return AktivitetStatus.ATFL_SN;
            }
            throw new IllegalStateException("Ukjent AktivitetStatus: (" + vlBGAktivitetStatus.getKode() + ").", e);
        }
    }

    private List<BeregningsgrunnlagPeriode> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                                          BeregningsgrunnlagInput input) {
        List<BeregningsgrunnlagPeriode> perioder = new ArrayList<>();
        vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(vlBGPeriode -> {
            final BeregningsgrunnlagPeriode.Builder regelBGPeriode = BeregningsgrunnlagPeriode.builder()
                    .medPeriode(Periode.of(vlBGPeriode.getBeregningsgrunnlagPeriodeFom(), vlBGPeriode.getBeregningsgrunnlagPeriodeTom()))
                    .leggTilPeriodeÅrsaker(mapPeriodeÅrsak(vlBGPeriode.getBeregningsgrunnlagPeriodeÅrsaker()));

            List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = mapVLBGPrStatus(vlBGPeriode, input);
            beregningsgrunnlagPrStatus.forEach(regelBGPeriode::medBeregningsgrunnlagPrStatus);
            perioder.add(regelBGPeriode.build());
        });

        return perioder;
    }

    private List<BeregningsgrunnlagPrStatus> mapVLBGPrStatus(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                             BeregningsgrunnlagInput input) {
        List<BeregningsgrunnlagPrStatus> liste = new ArrayList<>();
        BeregningsgrunnlagPrStatus bgpsATFL = null;

        for (BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
            if (AktivitetStatus.ATFL.equals(regelAktivitetStatus) || AktivitetStatus.AT.equals(regelAktivitetStatus)) {
                if (bgpsATFL == null) {  // Alle ATFL håndteres samtidig her
                    bgpsATFL = mapVLBGPStatusForATFL(vlBGPeriode, regelAktivitetStatus, input);
                    liste.add(bgpsATFL);
                }
            } else {
                BeregningsgrunnlagPrStatus bgps = mapVLBGPStatusForAlleAktivietetStatuser(vlBGPStatus, input);
                liste.add(bgps);
            }
        }
        return liste;
    }

    // Ikke ATFL og TY, de har separat mapping
    private BeregningsgrunnlagPrStatus mapVLBGPStatusForAlleAktivietetStatuser(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus,
                                                                               BeregningsgrunnlagInput input) {
        final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
        List<BigDecimal> pgi = (vlBGPStatus.getPgiSnitt() == null ? new ArrayList<>() :
                Arrays.asList(Beløp.safeVerdi(vlBGPStatus.getPgi1()), Beløp.safeVerdi(vlBGPStatus.getPgi2()), Beløp.safeVerdi(vlBGPStatus.getPgi3())));
        Optional<FaktaAktørDto> faktaAktørDto = input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat().flatMap(FaktaAggregatDto::getFaktaAktør);
        return BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(regelAktivitetStatus)
                .medBeregningsperiode(beregningsperiodeFor(vlBGPStatus))
                .medBeregnetPrÅr(Beløp.safeVerdi(vlBGPStatus.getBeregnetPrÅr()))
                .medOverstyrtPrÅr(Beløp.safeVerdi(vlBGPStatus.getOverstyrtPrÅr()))
                .medFordeltPrÅr(Optional.ofNullable(Beløp.safeVerdi(vlBGPStatus.getManueltFordeltPrÅr())).orElseGet(() -> Beløp.safeVerdi(vlBGPStatus.getFordeltPrÅr())) )// Midlertidig løsning til vi lager egen mapping for fastsett vl til regel
                .medBesteberegningPrÅr(Beløp.safeVerdi(vlBGPStatus.getBesteberegningPrÅr()))
                .medGjennomsnittligPGI(Beløp.safeVerdi(vlBGPStatus.getPgiSnitt()))
                .medPGI(pgi)
                .medÅrsbeløpFraTilstøtendeYtelse(Beløp.safeVerdi(vlBGPStatus.getÅrsbeløpFraTilstøtendeYtelseVerdi()))
                .medErNyIArbeidslivet(faktaAktørDto.map(FaktaAktørDto::getErNyIArbeidslivetSNVurdering).orElse(null))
                .medAndelNr(vlBGPStatus.getAndelsnr())
                .medInntektskategori(MapInntektskategoriFraVLTilRegel.map(vlBGPStatus.getGjeldendeInntektskategori()))
                .medFastsattAvSaksbehandler(vlBGPStatus.getFastsattAvSaksbehandler())
                .medBesteberegningPrÅr(Beløp.safeVerdi(vlBGPStatus.getBesteberegningPrÅr()))
                .medOrginalDagsatsFraTilstøtendeYtelse(vlBGPStatus.getOrginalDagsatsFraTilstøtendeYtelse())
                .medUtbetalingsprosent(finnUtbetalingsgradForAndel(vlBGPStatus, vlBGPStatus.getBeregningsgrunnlagPeriode().getPeriode(), input.getYtelsespesifiktGrunnlag(), false).verdi())
                .build();
    }


    private Periode beregningsperiodeFor(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        if (vlBGPStatus.getBeregningsperiodeFom() == null && vlBGPStatus.getBeregningsperiodeTom() == null) {
            return null;
        }
        return Periode.of(vlBGPStatus.getBeregningsperiodeFom(), vlBGPStatus.getBeregningsperiodeTom());
    }

    // Felles mapping av alle statuser som mapper til ATFL
    private BeregningsgrunnlagPrStatus mapVLBGPStatusForATFL(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                             AktivitetStatus regelAktivitetStatus,
                                                             BeregningsgrunnlagInput input) {

        BeregningsgrunnlagPrStatus.Builder regelBGPStatusATFL = BeregningsgrunnlagPrStatus.builder().medAktivitetStatus(regelAktivitetStatus);

        for (BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            if (regelAktivitetStatus.equals(mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus()))) {
                BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold = byggAndel(vlBGPStatus, input);
                regelBGPStatusATFL.medArbeidsforhold(regelArbeidsforhold);
            }
        }
        return regelBGPStatusATFL.build();
    }

    private BeregningsgrunnlagPrArbeidsforhold byggAndel(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus, BeregningsgrunnlagInput input) {
        BeregningsgrunnlagPrArbeidsforhold.Builder builder = BeregningsgrunnlagPrArbeidsforhold.builder();
        builder
                .medInntektskategori(MapInntektskategoriFraVLTilRegel.map(vlBGPStatus.getGjeldendeInntektskategori()))
                .medBeregnetPrÅr(Beløp.safeVerdi(vlBGPStatus.getBeregnetPrÅr()))
                .medBeregningsperiode(beregningsperiodeFor(vlBGPStatus))
                .medFastsattAvSaksbehandler(vlBGPStatus.getFastsattAvSaksbehandler())
                .medLagtTilAvSaksbehandler(vlBGPStatus.erLagtTilAvSaksbehandler())
                .medAndelNr(vlBGPStatus.getAndelsnr())
                .medOverstyrtPrÅr(Beløp.safeVerdi(vlBGPStatus.getOverstyrtPrÅr()))
                .medFordeltPrÅr(Optional.ofNullable(Beløp.safeVerdi(vlBGPStatus.getManueltFordeltPrÅr())).orElseGet(() -> Beløp.safeVerdi(vlBGPStatus.getFordeltPrÅr()))) // Midlertidig løsning til vi lager egen mapping for fastsettsteget vl til regel
                .medBesteberegningPrÅr(Beløp.safeVerdi(vlBGPStatus.getBesteberegningPrÅr()))
                .medArbeidsforhold(MapArbeidsforholdFraVLTilRegel.arbeidsforholdForMedStartdato(vlBGPStatus, input.getIayGrunnlag(), input.getSkjæringstidspunktForBeregning()))
                .medUtbetalingsprosentSVP(finnUtbetalingsgradForAndel(vlBGPStatus, vlBGPStatus.getBeregningsgrunnlagPeriode().getPeriode(), input.getYtelsespesifiktGrunnlag(), false).verdi());
        Optional<Boolean> erTidsbegrenset = input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat().flatMap(fa -> fa.getFaktaArbeidsforhold(vlBGPStatus))
                .map(FaktaArbeidsforholdDto::getErTidsbegrensetVurdering);
        vlBGPStatus.getBgAndelArbeidsforhold().ifPresent(bga ->
                builder
                        .medNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().map(Beløp::verdi).orElse(null))
                        .medNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().map(Beløp::verdi).orElse(null))
                        .medErTidsbegrensetArbeidsforhold(erTidsbegrenset.orElse(null))
                        .medGjeldendeRefusjonPrÅr(Beløp.safeVerdi(bga.getGjeldendeRefusjonPrÅr())));

        return builder.build();
    }
}
