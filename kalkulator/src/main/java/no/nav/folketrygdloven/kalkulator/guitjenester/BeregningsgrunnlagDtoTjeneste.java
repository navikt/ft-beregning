package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.adapter.util.Dekningsgradtjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.BeregningsgrunnlagPrStatusOgAndelDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FaktaOmBeregningDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.inntektsgrunnlag.InntektsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.refusjon.VurderRefusjonDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag.YtelsespesifiktGrunnlagTjenesteFP;
import no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag.YtelsespesifiktGrunnlagTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag.YtelsespesifiktGrunnlagTjenesteOMP;
import no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag.YtelsespesifiktGrunnlagTjenesteSVP;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AvklaringsbehovDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.SammenligningsgrunnlagDto;

public class BeregningsgrunnlagDtoTjeneste {
    private final BeregningsgrunnlagPrStatusOgAndelDtoTjeneste beregningsgrunnlagPrStatusOgAndelDtoTjeneste =
            new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
    private final FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjeneste();

    public BeregningsgrunnlagDto lagBeregningsgrunnlagDto(BeregningsgrunnlagGUIInput input) {
        return lagDto(input);
    }

    private BeregningsgrunnlagDto lagDto(BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagDto dto = new BeregningsgrunnlagDto();
        mapAvklaringsbehov(input, dto);
        mapFaktaOmBeregning(input, dto);
        mapForlengelsePerioder(input, dto);
        if (input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes().isPresent()) {
            mapOverstyring(input, dto);
            mapSkjæringstidspunkt(input, dto);
            mapFaktaOmRefusjon(input, dto);
            mapFaktaOmFordeling(input, dto);
            mapSammenlingingsgrunnlagPrStatus(input, dto);
            mapBeregningsgrunnlagAktivitetStatus(input, dto);
            mapBeregningsgrunnlagPerioder(input, dto);
            mapBeløp(input, dto);
            mapAktivitetGradering(input, dto);
            mapDekningsgrad(input, dto);
            mapYtelsesspesifiktGrunnlag(input, dto);
            mapInntektsgrunnlag(input, dto);
        }
        return dto;
    }

    private static void mapForlengelsePerioder(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        if (input.getForlengelseperioder() != null) {
            dto.setForlengelseperioder(input.getForlengelseperioder().stream().map(p -> new Periode(p.getFomDato(), p.getTomDato())).toList());
        }
    }

    private void mapAvklaringsbehov(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        dto.setAvklaringsbehov(input.getAvklaringsbehov().stream()
                .filter(ab -> !ab.getStatus().equals(AvklaringsbehovStatus.AVBRUTT) && !ab.getErTrukket())
                .map(a -> new AvklaringsbehovDto(a.getDefinisjon(), a.getStatus(), kanLøses(a, input),
                        a.getErTrukket(), a.getBegrunnelse(), a.getVurdertAv(), a.getVurdertTidspunkt())).collect(Collectors.toList()));
    }

    private boolean kanLøses(no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov.AvklaringsbehovDto a, BeregningsgrunnlagGUIInput input) {
        if (!a.getDefinisjon().getStegFunnet().erFør(BeregningSteg.VURDER_REF_BERGRUNN)) {
            return true;
        }
        var forlengelseperioder = input.getForlengelseperioder();
        return forlengelseperioder == null || forlengelseperioder.isEmpty();
    }

    private void mapFaktaOmRefusjon(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        VurderRefusjonDtoTjeneste.lagDto(input).ifPresent(dto::setRefusjonTilVurdering);
    }

    private void mapInntektsgrunnlag(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        InntektsgrunnlagTjeneste.lagDto(input).ifPresent(dto::setInntektsgrunnlag);
    }

    private void mapOverstyring(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        boolean overstyrt = input.getBeregningsgrunnlag().isOverstyrt();
        dto.setErOverstyrtInntekt(overstyrt);
    }

    private void mapYtelsesspesifiktGrunnlag(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var tjeneste = switch (input.getFagsakYtelseType()) {
            case FORELDREPENGER -> new YtelsespesifiktGrunnlagTjenesteFP();
            case SVANGERSKAPSPENGER -> new YtelsespesifiktGrunnlagTjenesteSVP();
            case OMSORGSPENGER -> new YtelsespesifiktGrunnlagTjenesteOMP();
            case FRISINN -> new YtelsespesifiktGrunnlagTjenesteFRISINN();
            default -> null;
        };
        Optional.ofNullable(tjeneste).flatMap(t -> t.map(input)).ifPresent(dto::setYtelsesspesifiktGrunnlag);
    }

    private void mapDekningsgrad(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var dekningsgradProsentverdi = Dekningsgradtjeneste.finnDekningsgradProsentverdi(input.getYtelsespesifiktGrunnlag(), Optional.of(dto.getSkjæringstidspunkt()));
        dto.setDekningsgrad(dekningsgradProsentverdi);
    }

    private void mapFaktaOmFordeling(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        FaktaOmFordelingDtoTjeneste.lagDto(input).ifPresent(dto::setFaktaOmFordeling);
    }

    private void mapFaktaOmBeregning(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        faktaOmBeregningDtoTjeneste.lagDto(input).ifPresent(dto::setFaktaOmBeregning);
    }

    private void mapSammenlingingsgrunnlagPrStatus(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        dto.setSammenligningsgrunnlagPrStatus(lagSammenligningsgrunnlagDtoPrStatus(beregningsgrunnlag));
    }

    private void mapBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        dto.setAktivitetStatus(lagAktivitetStatusListe(beregningsgrunnlag));
    }

    private void mapBeregningsgrunnlagPerioder(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlagPerioder = lagBeregningsgrunnlagPeriodeRestDto(input);
        dto.setBeregningsgrunnlagPeriode(beregningsgrunnlagPerioder);
    }

    private void mapSkjæringstidspunkt(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var skjæringstidspunktForBeregning = input.getSkjæringstidspunktForBeregning();
        dto.setSkjaeringstidspunktBeregning(skjæringstidspunktForBeregning);
        dto.setSkjæringstidspunkt(skjæringstidspunktForBeregning);
    }

    private void mapBeløp(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var grunnbeløp = Optional.ofNullable(beregningsgrunnlag.getGrunnbeløp()).orElse(Beløp.ZERO);
        var halvG = grunnbeløp.map(g -> g.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
        dto.setHalvG(ModellTyperMapper.beløpTilDto(halvG));
        dto.setGrunnbeløp(ModellTyperMapper.beløpTilDto(grunnbeløp));
        dto.setHjemmel(beregningsgrunnlag.getHjemmel());
    }

    private void mapAktivitetGradering(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var aktivitetGradering = input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag ?
                ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).getAktivitetGradering() : AktivitetGradering.INGEN_GRADERING;
        var andelerMedGraderingUtenBG = GraderingUtenBeregningsgrunnlagTjeneste.finnAndelerMedGraderingUtenBG(beregningsgrunnlag,
                aktivitetGradering);

        if (!andelerMedGraderingUtenBG.isEmpty()) {
            dto.setAndelerMedGraderingUtenBG(beregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input, andelerMedGraderingUtenBG));
        }
    }

    private List<SammenligningsgrunnlagDto> lagSammenligningsgrunnlagDtoPrStatus(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        List<SammenligningsgrunnlagDto> sammenligningsgrunnlagDtos = new ArrayList<>();
        beregningsgrunnlag.getSammenligningsgrunnlagPrStatusListe().forEach(s -> {
            SammenligningsgrunnlagDto dto = new SammenligningsgrunnlagDto();
            dto.setSammenligningsgrunnlagFom(s.getSammenligningsperiodeFom());
            dto.setSammenligningsgrunnlagTom(s.getSammenligningsperiodeTom());
            dto.setRapportertPrAar(ModellTyperMapper.beløpTilDto(s.getRapportertPrÅr()));
            dto.setAvvikPromille(s.getAvvikPromilleNy());
            BigDecimal avvikProsent = s.getAvvikPromilleNy() == null ? null : s.getAvvikPromilleNy().scaleByPowerOfTen(-1);
            dto.setAvvikProsent(avvikProsent);
            dto.setSammenligningsgrunnlagType(s.getSammenligningsgrunnlagType());
            dto.setDifferanseBeregnet(ModellTyperMapper.beløpTilDto(finnDifferanseBeregnet(beregningsgrunnlag, s)));
            sammenligningsgrunnlagDtos.add(dto);
        });
        return sammenligningsgrunnlagDtos;

    }

    private Beløp finnBeregnetGammeltSammenliningsgrunnlag(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        if (finnesAndelMedSN(beregningsgrunnlag)) {
            return hentBeregnetPGI(beregningsgrunnlag, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        } else {
            return hentBeregnetGrunnlag(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER);
        }
    }

    private Beløp finnDifferanseBeregnet(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag,
                                              SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagPrStatus) {
        var beregnet = switch(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType()) {
            case SAMMENLIGNING_AT ->  hentBeregnetGrunnlag(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
            case SAMMENLIGNING_FL -> hentBeregnetGrunnlag(beregningsgrunnlag, AktivitetStatus.FRILANSER);
            case SAMMENLIGNING_AT_FL -> hentBeregnetGrunnlag(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER);
            case SAMMENLIGNING_SN -> hentBeregnetPGI(beregningsgrunnlag, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
            case SAMMENLIGNING_MIDL_INAKTIV -> hentBeregnetPGI(beregningsgrunnlag, AktivitetStatus.BRUKERS_ANDEL);
            case SAMMENLIGNING_ATFL_SN -> finnBeregnetGammeltSammenliningsgrunnlag(beregningsgrunnlag);
        };
        return beregnet.subtraher(sammenligningsgrunnlagPrStatus.getRapportertPrÅr());
    }

    private Beløp hentBeregnetGrunnlag(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag, AktivitetStatus... statuser) {
        var statuserSomSkalAdderes = Arrays.asList(statuser);
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(b -> statuserSomSkalAdderes.contains(b.getAktivitetStatus()))
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private Beløp hentBeregnetPGI(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag, AktivitetStatus status) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(b -> b.getAktivitetStatus().equals(status))
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getPgiSnitt)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private boolean finnesAndelMedSN(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .anyMatch(b -> b.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
    }

    private List<BeregningsgrunnlagPeriodeDto> lagBeregningsgrunnlagPeriodeRestDto(BeregningsgrunnlagGUIInput input) {
        List<BeregningsgrunnlagPeriodeDto> dtoList = new ArrayList<>();
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto periode : beregningsgrunnlagPerioder) {
            BeregningsgrunnlagPeriodeDto dto = lagBeregningsgrunnlagPeriode(input, periode);
            dtoList.add(dto);
        }
        return dtoList;
    }

    private BeregningsgrunnlagPeriodeDto lagBeregningsgrunnlagPeriode(BeregningsgrunnlagGUIInput input,
                                                                      no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto periode) {
        BeregningsgrunnlagPeriodeDto dto = new BeregningsgrunnlagPeriodeDto();
        dto.setBeregningsgrunnlagPeriodeFom(periode.getBeregningsgrunnlagPeriodeFom());
        dto.setBeregningsgrunnlagPeriodeTom(periode.getBeregningsgrunnlagPeriodeTom() == TIDENES_ENDE ? null : periode.getBeregningsgrunnlagPeriodeTom());
        dto.setBeregnetPrAar(ModellTyperMapper.beløpTilDto(periode.getBeregnetPrÅr()));
        dto.setBruttoPrAar(ModellTyperMapper.beløpTilDto(periode.getBruttoPrÅr()));
        var bruttoInkludertBortfaltNaturalytelsePrAar = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoInkludertNaturalYtelser)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(null);
        dto.setBruttoInkludertBortfaltNaturalytelsePrAar(ModellTyperMapper.beløpTilDto(bruttoInkludertBortfaltNaturalytelsePrAar));
        dto.setAvkortetPrAar(periode.getAvkortetPrÅr() == null ? null : ModellTyperMapper.beløpTilDto(finnAvkortetUtenGraderingPrÅr(bruttoInkludertBortfaltNaturalytelsePrAar, input.getBeregningsgrunnlag().getGrunnbeløp())));
        dto.setRedusertPrAar(ModellTyperMapper.beløpTilDto(periode.getRedusertPrÅr()));
        dto.setDagsats(periode.getDagsats());
        dto.leggTilPeriodeAarsaker(periode.getPeriodeÅrsaker());
        dto.setAndeler(
                beregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input, periode.getBeregningsgrunnlagPrStatusOgAndelList()));
        return dto;
    }

    private Beløp finnAvkortetUtenGraderingPrÅr(Beløp bruttoInkludertBortfaltNaturalytelsePrAar, Beløp grunnbeløp) {
        if (bruttoInkludertBortfaltNaturalytelsePrAar == null) {
            return null;
        }
        var seksG = grunnbeløp.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());
        return bruttoInkludertBortfaltNaturalytelsePrAar.compareTo(seksG) > 0 ? seksG : bruttoInkludertBortfaltNaturalytelsePrAar;
    }

    private List<no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus> lagAktivitetStatusListe(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        ArrayList<no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus> statusListe = new ArrayList<>();
        for (var status : beregningsgrunnlag.getAktivitetStatuser()) {
            statusListe.add(status.getAktivitetStatus());
        }
        return statusListe;
    }

}
