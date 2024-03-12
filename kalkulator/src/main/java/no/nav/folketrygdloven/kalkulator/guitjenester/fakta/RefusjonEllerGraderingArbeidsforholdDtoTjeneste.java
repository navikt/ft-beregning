package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.NyAktivitetMedSøktYtelseFordeling.lagPerioderForNyAktivitetMedSøktYtelse;
import static no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandlingForPeriode;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelBeregningsgrunnlagTilfelleInput;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelingGraderingTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.NyPeriodeDto;

public class RefusjonEllerGraderingArbeidsforholdDtoTjeneste {

    private static final LocalDate START_TIDSREGNING = LocalDate.of(2000, Month.JANUARY, 1);

    private RefusjonEllerGraderingArbeidsforholdDtoTjeneste() {
        // Skjul
    }

    public static List<FordelBeregningsgrunnlagArbeidsforholdDto> lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(BeregningsgrunnlagGUIInput input, LocalDate skjæringstidspunktForBeregning) {
        List<BeregningsgrunnlagPeriodeDto> perioder = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = FordelBeregningsgrunnlagTilfelleInput.fraBeregningsgrunnlagRestInput(input);
        var tilfelleMap = finnFordelingTilfelleMap(input.getBeregningsgrunnlag(), fordelingInput);
        return tilfelleMap.entrySet().stream()
                .map(tilfelleEntry -> mapTilEndretArbeidsforholdDto(input, tilfelleEntry, skjæringstidspunktForBeregning, perioder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(af -> !af.getPerioderMedGraderingEllerRefusjon().isEmpty())
                .collect(Collectors.toList());
    }

    private static Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> finnFordelingTilfelleMap(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                         FordelBeregningsgrunnlagTilfelleInput fordelingInput) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> tilfelleMap = new HashMap<>();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .forEach(periode -> {
                    var andelTilfelleMap = vurderManuellBehandlingForPeriode(periode, fordelingInput);
                    tilfelleMap.putAll(andelTilfelleMap.entrySet().stream()
                            .filter(e -> Boolean.FALSE.equals(e.getKey().erLagtTilAvSaksbehandler()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                });
        return tilfelleMap;
    }

    private static Optional<FordelBeregningsgrunnlagArbeidsforholdDto> mapTilEndretArbeidsforholdDto(BeregningsgrunnlagGUIInput input,
                                                                                                     Map.Entry<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> tilfelleEntry,
                                                                                                     LocalDate stp, List<BeregningsgrunnlagPeriodeDto> perioder) {
        BeregningsgrunnlagPrStatusOgAndelDto andel = tilfelleEntry.getKey();
        return BeregningsgrunnlagDtoUtil.lagArbeidsforholdEndringDto(andel, input.getIayGrunnlag())
                .map(af -> {
                    FordelBeregningsgrunnlagArbeidsforholdDto endringAf = (FordelBeregningsgrunnlagArbeidsforholdDto) af;
                    settEndretArbeidsforholdForNyttRefusjonskrav(andel, endringAf, perioder);
                    settEndretArbeidsforholdForSøktGradering(andel, endringAf, finnGradering(input));
                    lagPerioderForNyAktivitetMedSøktYtelse(input.getYtelsespesifiktGrunnlag(), tilfelleEntry.getValue(), andel, endringAf)
                            .forEach(endringAf::leggTilPeriodeMedGraderingEllerRefusjon);
                    andel.getBgAndelArbeidsforhold().flatMap(bga ->
                            UtledPermisjonTilDto.utled(input.getIayGrunnlag(), stp, bga)
                    ).ifPresent(endringAf::setPermisjon);
                    return endringAf;
                });
    }

    private static AktivitetGradering finnGradering(BeregningsgrunnlagGUIInput input) {
        return input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag ?
                ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).getAktivitetGradering() : AktivitetGradering.INGEN_GRADERING;
    }

    private static void settEndretArbeidsforholdForNyttRefusjonskrav(BeregningsgrunnlagPrStatusOgAndelDto distinctAndel,
                                                                     FordelBeregningsgrunnlagArbeidsforholdDto endretArbeidsforhold, List<BeregningsgrunnlagPeriodeDto> perioder) {
        LocalDate sluttDatoRefusjon = TIDENES_BEGYNNELSE;
        for (int i = 0; i < perioder.size(); i++) {
            BeregningsgrunnlagPeriodeDto periode = perioder.get(i);
            LocalDate tomDatoPeriode = periode.getBeregningsgrunnlagPeriodeTom() == null ?
                    TIDENES_ENDE : periode.getBeregningsgrunnlagPeriodeTom();
            if (sluttDatoRefusjon.isBefore(tomDatoPeriode)) {
                var refusjonBeløpOpt = finnRefusjonsbeløpForAndelIPeriode(distinctAndel, periode);
                if (refusjonBeløpOpt.isPresent()) {
                    LocalDate startdatoRefusjon = periode.getBeregningsgrunnlagPeriodeFom();
                    sluttDatoRefusjon = finnSluttdato(distinctAndel, perioder, i, refusjonBeløpOpt.get());
                    endretArbeidsforhold.leggTilPeriodeMedGraderingEllerRefusjon(lagNyPeriodeDtoForRefusjon(startdatoRefusjon, sluttDatoRefusjon));
                }
            }
        }
    }

    private static NyPeriodeDto lagNyPeriodeDtoForRefusjon(LocalDate fom, LocalDate tom) {
        if (fom != null && tom != null && tom.isBefore(fom)) {
            throw new IllegalStateException("Periode: tom kan ikke være før fom: " + fom + " " + tom);
        }
        var dto = new NyPeriodeDto(true, false, false);
        dto.setFom(fom == null || !fom.isAfter(START_TIDSREGNING) ? null : fom);
        dto.setTom(tom == null || TIDENES_ENDE.minusDays(2).isBefore(tom) ? null : tom);
        return dto;
    }

    private static LocalDate finnSluttdato(BeregningsgrunnlagPrStatusOgAndelDto distinctAndel, List<BeregningsgrunnlagPeriodeDto> perioder, int i, Beløp refusjonBeløp) {
        LocalDate sluttDatoRefusjon = TIDENES_ENDE;
        if (i == perioder.size() - 1) {
            return sluttDatoRefusjon;
        }
        for (int k = i + 1; k < perioder.size(); k++) {
            BeregningsgrunnlagPeriodeDto nestePeriode = perioder.get(k);
            var refusjonINestePeriode = finnRefusjonsbeløpForAndelIPeriode(distinctAndel, nestePeriode).orElse(Beløp.ZERO);
            if (refusjonINestePeriode.compareTo(refusjonBeløp) != 0) {
                return perioder.get(k - 1).getBeregningsgrunnlagPeriodeTom();
            }
        }
        return sluttDatoRefusjon;
    }

    private static Optional<Beløp> finnRefusjonsbeløpForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto distinctAndel,
                                                                           BeregningsgrunnlagPeriodeDto periode) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> !andel.erLagtTilAvSaksbehandler())
                .filter(andel -> andel.gjelderSammeArbeidsforhold(distinctAndel))
                .filter(andel -> andel.getBgAndelArbeidsforhold()
                        .map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(Beløp.ZERO).compareTo(Beløp.ZERO) != 0)
                .findFirst();
        return matchendeAndel
                .flatMap(andel -> andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr));
    }

    private static void settEndretArbeidsforholdForSøktGradering(BeregningsgrunnlagPrStatusOgAndelDto distinctAndel,
                                                                 FordelBeregningsgrunnlagArbeidsforholdDto endretArbeidsforhold,
                                                                 AktivitetGradering aktivitetGradering) {
        List<Gradering> graderingerForArbeidsforhold = FordelingGraderingTjeneste.hentGraderingerForAndel(distinctAndel, aktivitetGradering);
        graderingerForArbeidsforhold.forEach(gradering -> {
            NyPeriodeDto graderingDto = new NyPeriodeDto(false, true, false);
            Intervall periode = gradering.getPeriode();
            graderingDto.setFom(periode.getFomDato());
            graderingDto.setTom(periode.getTomDato().isBefore(TIDENES_ENDE) ? periode.getTomDato() : null);
            endretArbeidsforhold.leggTilPeriodeMedGraderingEllerRefusjon(graderingDto);
        });
    }

}
