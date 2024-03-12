package no.nav.folketrygdloven.kalkulator.felles.inntektgradering;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnAktivitetsgradForAndel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.TilkommetInntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.util.BeregningsgrunnlagUtil;
import no.nav.folketrygdloven.kalkulator.adapter.util.Dekningsgradtjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.PleiepengerGrunnlagMapperFastsett;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn.FrisinnGrunnlagMapperFastsett;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class MapBeregningsgrunnlagFraVLTilRegel {

    public no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag map(BeregningsgrunnlagInput input,
                                                                                                 BeregningsgrunnlagDto beregningsgrunnlag) {
        var ref = input.getKoblingReferanse();
        Objects.requireNonNull(ref, "BehandlingReferanse kan ikke være null!");
        Objects.requireNonNull(beregningsgrunnlag, "Beregningsgrunnlag kan ikke være null!");

        List<AktivitetStatusMedHjemmel> aktivitetStatuser = beregningsgrunnlag.getAktivitetStatuser().stream()
                .map(MapBeregningsgrunnlagFraVLTilRegel::mapVLAktivitetStatusMedHjemmel)
                .sorted()
                .collect(Collectors.toList());

        List<BeregningsgrunnlagPeriode> perioder = mapBeregningsgrunnlagPerioder(beregningsgrunnlag, input);

        var builder = no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag.builder();

        return builder
                .medAktivitetStatuser(aktivitetStatuser)
                .medBeregningsgrunnlagPerioder(perioder)
                .medGrunnbeløp(beregningsgrunnlag.getGrunnbeløp().verdi())
                .medYtelsesdagerIEtÅr(KonfigTjeneste.getYtelsesdagerIÅr())
                .medYtelsesSpesifiktGrunnlag(mapYtelsesSpesifiktGrunnlag(input))
                .medAntallGØvreGrenseverdi(KonfigTjeneste.getAntallGØvreGrenseverdi())
                .medMidlertidigInaktivType(mapMidlertidigInaktivType(input))
                .leggTilToggle("GRADERING_MOT_INNTEKT", KonfigurasjonVerdi.instance().get("GRADERING_MOT_INNTEKT", false))
                .build();
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

    private YtelsesSpesifiktGrunnlag mapYtelsesSpesifiktGrunnlag(BeregningsgrunnlagInput input) {
        return switch (input.getFagsakYtelseType()) {
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE -> new PleiepengerGrunnlagMapperFastsett().map(input);
            case FRISINN -> new FrisinnGrunnlagMapperFastsett().map(input);
            default -> null;
        };
    }

    private static AktivitetStatusMedHjemmel mapVLAktivitetStatusMedHjemmel(final BeregningsgrunnlagAktivitetStatusDto vlBGAktivitetStatus) {
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

    private static AktivitetStatus mapVLAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus vlBGAktivitetStatus) {
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

    private static List<BeregningsgrunnlagPeriode> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                                                 BeregningsgrunnlagInput input) {
        var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(input.getForlengelseperioder(), vlBeregningsgrunnlag);
        var perioder = vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> perioderTilVurderingTjeneste.erTilVurdering(p.getPeriode()))
                .map(vlBGPeriode -> mapPeriode(vlBGPeriode, input)).toList();

        return perioder;
    }

    private static BeregningsgrunnlagPeriode mapPeriode(BeregningsgrunnlagPeriodeDto vlBGPeriode, BeregningsgrunnlagInput input) {
        Dekningsgrad dekningsgradPeriode = input.getYtelsespesifiktGrunnlag() == null ? null : Dekningsgradtjeneste.mapTilDekningsgradRegel(input.getYtelsespesifiktGrunnlag(), Optional.of(vlBGPeriode.getBeregningsgrunnlagPeriodeFom()));
        final BeregningsgrunnlagPeriode.Builder regelBGPeriode = BeregningsgrunnlagPeriode.builder()
                .medPeriode(Periode.of(vlBGPeriode.getBeregningsgrunnlagPeriodeFom(), vlBGPeriode.getBeregningsgrunnlagPeriodeTom()))
                .medDekningsgrad(dekningsgradPeriode)
                .leggTilTilkommetInntektsforhold(mapTilkomneInntekter(vlBGPeriode));

        List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = mapVLBGPrStatus(vlBGPeriode, input);
        beregningsgrunnlagPrStatus.forEach(regelBGPeriode::medBeregningsgrunnlagPrStatus);
        return regelBGPeriode.build();
    }

    private static List<TilkommetInntekt> mapTilkomneInntekter(BeregningsgrunnlagPeriodeDto vlBGPeriode) {
        return vlBGPeriode.getTilkomneInntekter().stream().filter(TilkommetInntektDto::skalRedusereUtbetaling).map(MapBeregningsgrunnlagFraVLTilRegel::mapTilkommetInntekt).toList();
    }

    private static TilkommetInntekt mapTilkommetInntekt(TilkommetInntektDto ti) {
        return new TilkommetInntekt(AktivitetStatus.valueOf(ti.getAktivitetStatus().getKode()),
                MapArbeidsforholdFraVLTilRegel.arbeidsforholdFor(ti.getAktivitetStatus(), ti.getArbeidsgiver(), OpptjeningAktivitetType.UDEFINERT, ti.getArbeidsforholdRef().getReferanse()),
                Beløp.safeVerdi(ti.getTilkommetInntektPrÅr()));
    }

    private static List<BeregningsgrunnlagPrStatus> mapVLBGPrStatus(BeregningsgrunnlagPeriodeDto vlBGPeriode, BeregningsgrunnlagInput input) {
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
                BeregningsgrunnlagPrStatus bgps = mapVLBGPStatusForAlleAktivitetStatuser(vlBGPStatus, input);
                liste.add(bgps);
            }
        }

        return liste;
    }

    // Ikke ATFL, de har separat mapping
    private static BeregningsgrunnlagPrStatus mapVLBGPStatusForAlleAktivitetStatuser(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus, BeregningsgrunnlagInput input) {
        final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
        var aktivitetsgrad = finnAktivitetsgradForAndel(vlBGPStatus, vlBGPStatus.getBeregningsgrunnlagPeriode().getPeriode(), input.getYtelsespesifiktGrunnlag(), false);
        var builder = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(regelAktivitetStatus)
                .medBruttoPrÅr(Beløp.safeVerdi(vlBGPStatus.getBruttoPrÅr()))
                .medInntektsgrunnlagPrÅr(Beløp.safeVerdi(vlBGPStatus.getGrunnlagPrÅr().getBruttoUtenFordelt()) != null ? vlBGPStatus.getGrunnlagPrÅr().getBruttoUtenFordelt().verdi() : BigDecimal.ZERO)
                .medAndelNr(vlBGPStatus.getAndelsnr())
                .medUtbetalingsprosent(BigDecimal.valueOf(100));
        aktivitetsgrad.ifPresent(ag -> builder.medAktivitetsgrad(ag.verdi()));
        return builder.build();
    }

    // Felles mapping av alle statuser som mapper til ATFL
    private static BeregningsgrunnlagPrStatus mapVLBGPStatusForATFL(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                                    AktivitetStatus regelAktivitetStatus, BeregningsgrunnlagInput input) {

        BeregningsgrunnlagPrStatus.Builder regelBGPStatusATFL = BeregningsgrunnlagPrStatus.builder().medAktivitetStatus(regelAktivitetStatus);

        for (BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            if (regelAktivitetStatus.equals(mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus()))) {
                BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold = byggAndel(vlBGPStatus, input);
                regelBGPStatusATFL.medArbeidsforhold(regelArbeidsforhold);
            }
        }
        return regelBGPStatusATFL.build();
    }

    private static BeregningsgrunnlagPrArbeidsforhold byggAndel(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus, BeregningsgrunnlagInput input) {
        BeregningsgrunnlagPrArbeidsforhold.Builder builder = BeregningsgrunnlagPrArbeidsforhold.builder();
        var aktivitetsgrad = finnAktivitetsgradForAndel(vlBGPStatus, vlBGPStatus.getBeregningsgrunnlagPeriode().getPeriode(), input.getYtelsespesifiktGrunnlag(), false);
        builder
                .medBruttoPrÅr(Beløp.safeVerdi(vlBGPStatus.getBruttoPrÅr()))
                .medInntektsgrunnlagPrÅr(Beløp.safeVerdi(vlBGPStatus.getGrunnlagPrÅr().getBruttoUtenFordelt()) != null ? vlBGPStatus.getGrunnlagPrÅr().getBruttoUtenFordelt().verdi() : BigDecimal.ZERO)
                .medAndelNr(vlBGPStatus.getAndelsnr())
                .medArbeidsforhold(MapArbeidsforholdFraVLTilRegel.arbeidsforholdFor(vlBGPStatus))
                .medUtbetalingsprosent(BigDecimal.valueOf(100));
        aktivitetsgrad.ifPresentOrElse(ag -> builder.medAktivitetsgrad(ag.verdi()), () -> builder.medAktivitetsgrad(BigDecimal.ZERO));
        vlBGPStatus.getBgAndelArbeidsforhold().ifPresent(bga ->
                builder
                        .medNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().map(Beløp::verdi).orElse(null))
                        .medNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().map(Beløp::verdi).orElse(null))
                        .medRefusjonPrÅr(Beløp.safeVerdi(bga.getGjeldendeRefusjonPrÅr())));

        return builder.build();
    }
}
