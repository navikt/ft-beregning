package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.EffektivÅrsinntektTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.AvslagsårsakPrPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.FrisinnGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.OpplystPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.SøknadsopplysningerDto;

public class YtelsespesifiktGrunnlagTjenesteFRISINN implements YtelsespesifiktGrunnlagTjeneste {

    @Override
    public Optional<YtelsespesifiktGrunnlagDto> map(BeregningsgrunnlagGUIInput input) {
        return Optional.of(mapFrisinngrunnlag(input));
    }

    private FrisinnGrunnlagDto mapFrisinngrunnlag(BeregningsgrunnlagGUIInput input) {
        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();

        FrisinnGrunnlag frisinngrunnlag = (FrisinnGrunnlag) ytelsespesifiktGrunnlag;

        FrisinnGrunnlagDto frisinnGrunnlagDto = new FrisinnGrunnlagDto();

        if (frisinngrunnlag.getSøkerYtelseForFrilans()) {
            frisinnGrunnlagDto.setOpplysningerFL(mapFrilansopplysninger(input));
        }

        if (frisinngrunnlag.getSøkerYtelseForNæring()) {
            frisinnGrunnlagDto.setOpplysningerSN(mapNæringsopplysninger(input));
        }

        List<OpplystPeriodeDto> søktePerioder = mapSøktePerider(input);
        frisinnGrunnlagDto.setPerioderSøktFor(søktePerioder);

        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        Optional<OppgittOpptjeningDto> oppgitOpptjening = input.getIayGrunnlag().getOppgittOpptjening();
        oppgitOpptjening.ifPresent(oppgittOpptjeningDto -> frisinnGrunnlagDto.setFrisinnPerioder(MapTilPerioderFRISINN.map(frisinnGrunnlag.getFrisinnPerioder(), oppgittOpptjeningDto)));

        frisinnGrunnlagDto.setAvslagsårsakPrPeriode(mapAvslagsårsakPerioder(input, frisinnGrunnlag, oppgitOpptjening));

        return frisinnGrunnlagDto;
    }

    private List<AvslagsårsakPrPeriodeDto> mapAvslagsårsakPerioder(BeregningsgrunnlagGUIInput input, FrisinnGrunnlag frisinnGrunnlag, Optional<OppgittOpptjeningDto> oppgitOpptjening) {
        return input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .map(periode -> new AvslagsårsakPrPeriodeDto(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(),
                        MapTilAvslagsårsakerFRISINN.finnForPeriode(periode, frisinnGrunnlag,
                                oppgitOpptjening,
                                input.getBeregningsgrunnlag().getGrunnbeløp(),
                                input.getSkjæringstidspunktForBeregning()).orElse(null)))
                .filter(a -> a.getAvslagsårsak() != null)
                .collect(Collectors.toList());
    }


    private List<OpplystPeriodeDto> mapSøktePerider(BeregningsgrunnlagGUIInput input) {
        List<OpplystPeriodeDto> søktePerioder = new ArrayList<>();
        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        FrisinnGrunnlag frisinnGrunnlag = (FrisinnGrunnlag) ytelsespesifiktGrunnlag;
        LocalDate stpBG = input.getBeregningsgrunnlag().getSkjæringstidspunkt();
        if (frisinnGrunnlag.getSøkerYtelseForNæring()) {
            søktePerioder.addAll(mapSøktePerioderForNæring(input, stpBG));
        }
        if (frisinnGrunnlag.getSøkerYtelseForFrilans()) {
            søktePerioder.addAll(mapSøktePerioderForFrilans(input, stpBG));
        }
        return søktePerioder;
    }

    private List<OpplystPeriodeDto> mapSøktePerioderForFrilans(BeregningsgrunnlagGUIInput input, LocalDate stpBG) {
        Optional<OppgittFrilansDto> oppgittFL = input.getIayGrunnlag().getOppgittOpptjening()
                .flatMap(OppgittOpptjeningDto::getFrilans);
        if (oppgittFL.isEmpty()) {
            return Collections.emptyList();
        }
        return oppgittFL.get().getOppgittFrilansInntekt().stream()
                .filter(inntekt -> !inntekt.getPeriode().getFomDato().isBefore(stpBG))
                .map(this::lagSøktPeriodeDtoForFrilans)
                .collect(Collectors.toList());
    }

    private List<OpplystPeriodeDto> mapSøktePerioderForNæring(BeregningsgrunnlagGUIInput input, LocalDate stpBG) {
        List<OppgittEgenNæringDto> oppgitteNæringer = input.getIayGrunnlag().getOppgittOpptjening()
                .map(OppgittOpptjeningDto::getEgenNæring)
                .orElse(Collections.emptyList());
        List<OppgittEgenNæringDto> næringerSøktFor = oppgitteNæringer.stream()
                .filter(næring -> !næring.getPeriode().getFomDato().isBefore(stpBG))
                .collect(Collectors.toList());
        return næringerSøktFor.stream()
                .map(this::lagSøktPeriodeDtoForNæring)
                .collect(Collectors.toList());
    }

    private OpplystPeriodeDto lagSøktPeriodeDtoForFrilans(OppgittFrilansInntektDto fl) {
        OpplystPeriodeDto dto = new OpplystPeriodeDto();
        dto.setFom(fl.getPeriode().getFomDato());
        dto.setTom(fl.getPeriode().getTomDato());
        dto.setStatusSøktFor(AktivitetStatus.FRILANSER);
        return dto;
    }

    private OpplystPeriodeDto lagSøktPeriodeDtoForNæring(OppgittEgenNæringDto næring) {
        OpplystPeriodeDto dto = new OpplystPeriodeDto();
        dto.setFom(næring.getPeriode().getFomDato());
        dto.setTom(næring.getPeriode().getTomDato());
        dto.setStatusSøktFor(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        return dto;
    }

    private SøknadsopplysningerDto mapNæringsopplysninger(BeregningsgrunnlagGUIInput input) {
        LocalDate stpBg = input.getSkjæringstidspunktForBeregning();
        List<OppgittEgenNæringDto> næringer = input.getIayGrunnlag().getOppgittOpptjening()
                .map(OppgittOpptjeningDto::getEgenNæring)
                .orElse(Collections.emptyList());

        boolean erNyoppstartetNæringsdrivende = næringer.stream().anyMatch(OppgittEgenNæringDto::getNyoppstartet);
        var oppgittLøpendeÅrsinntekt = næringer.stream()
                .filter(en -> !stpBg.isAfter(en.getTilOgMed()))
                .filter(en -> en.getBruttoInntekt() != null)
                .map(EffektivÅrsinntektTjenesteFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
                .reduce(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp::adder)
                .map(ModellTyperMapper::beløpTilDto)
                .orElse(Beløp.ZERO);
        var oppgittLøpendeInntekt = næringer.stream()
                .filter(en -> !stpBg.isAfter(en.getTilOgMed()))
                .map(OppgittEgenNæringDto::getBruttoInntekt)
                .filter(Objects::nonNull)
                .map(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp::verdi)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .map(Beløp::fra)
                .orElse(Beløp.ZERO);

        SøknadsopplysningerDto dto = new SøknadsopplysningerDto();
        dto.setErNyoppstartet(erNyoppstartetNæringsdrivende);
        dto.setOppgittÅrsinntekt(oppgittLøpendeÅrsinntekt);
        dto.setOppgittInntekt(oppgittLøpendeInntekt);
        return dto;
    }

    private SøknadsopplysningerDto mapFrilansopplysninger(BeregningsgrunnlagGUIInput input) {
        LocalDate stpBg = input.getSkjæringstidspunktForBeregning();
        Boolean erNyoppstartetFrilans = input.getIayGrunnlag().getOppgittOpptjening()
                .flatMap(OppgittOpptjeningDto::getFrilans)
                .map(OppgittFrilansDto::getErNyoppstartet)
                .orElse(false);

        List<OppgittFrilansInntektDto> oppgittFLInntekt = input.getIayGrunnlag().getOppgittOpptjening()
                .flatMap(OppgittOpptjeningDto::getFrilans)
                .map(OppgittFrilansDto::getOppgittFrilansInntekt)
                .orElse(Collections.emptyList());

        var oppgittLøpendeÅrsinntekt = oppgittFLInntekt.stream()
                .filter(oi -> !oi.getPeriode().getFomDato().isBefore(stpBg))
                .map(EffektivÅrsinntektTjenesteFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
                .reduce(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp::adder)
                .map(ModellTyperMapper::beløpTilDto)
                .orElse(Beløp.ZERO);
        var oppgittLøpendeInntekt = oppgittFLInntekt.stream()
                .filter(oi -> !oi.getPeriode().getFomDato().isBefore(stpBg))
                .map(OppgittFrilansInntektDto::getInntekt)
                .filter(Objects::nonNull)
                .map(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp::verdi)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .map(Beløp::fra)
                .orElse(Beløp.ZERO);
        SøknadsopplysningerDto dto = new SøknadsopplysningerDto();
        dto.setOppgittInntekt(oppgittLøpendeInntekt);
        dto.setOppgittÅrsinntekt(oppgittLøpendeÅrsinntekt);
        dto.setErNyoppstartet(erNyoppstartetFrilans);
        return dto;
    }
}
