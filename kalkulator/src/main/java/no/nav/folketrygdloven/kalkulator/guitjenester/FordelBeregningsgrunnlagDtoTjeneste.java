package no.nav.folketrygdloven.kalkulator.guitjenester;

import static java.util.stream.Collectors.toList;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.util.Comparator;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.ManuellBehandlingRefusjonGraderingDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.RefusjonDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.RefusjonEllerGraderingArbeidsforholdDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelingDto;

public class FordelBeregningsgrunnlagDtoTjeneste {

    private FordelBeregningsgrunnlagDtoTjeneste() {
        // Skjul
    }

    public static void lagDto(BeregningsgrunnlagGUIInput input,
                              FordelingDto dto) {
        boolean harUtførtSteg = !input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        if (!harUtførtSteg) {
            return;
        }
        FordelBeregningsgrunnlagDto bgDto = new FordelBeregningsgrunnlagDto();
        settEndretArbeidsforholdDto(input, bgDto);
        bgDto.setFordelBeregningsgrunnlagPerioder(lagPerioder(input));
        dto.setFordelBeregningsgrunnlag(bgDto);
    }

    private static List<FordelBeregningsgrunnlagPeriodeDto> lagPerioder(BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<BeregningsgrunnlagPeriodeDto> bgPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        List<FordelBeregningsgrunnlagPeriodeDto> fordelPerioder = bgPerioder.stream()
                .map(periode -> mapTilPeriodeDto(input, periode, beregningsgrunnlag.getGrunnbeløp()))
                .sorted(Comparator.comparing(FordelBeregningsgrunnlagPeriodeDto::getFom)).collect(toList());
        return fordelPerioder;
    }

    private static FordelBeregningsgrunnlagPeriodeDto mapTilPeriodeDto(BeregningsgrunnlagGUIInput input,
                                                                       BeregningsgrunnlagPeriodeDto periode,
                                                                       Beløp grunnbeløp) {
        FordelBeregningsgrunnlagPeriodeDto fordelBGPeriode = new FordelBeregningsgrunnlagPeriodeDto();
        fordelBGPeriode.setFom(periode.getBeregningsgrunnlagPeriodeFom());
        fordelBGPeriode.setTom(periode.getBeregningsgrunnlagPeriodeTom() == TIDENES_ENDE ? null : periode.getBeregningsgrunnlagPeriodeTom());
        List<FordelBeregningsgrunnlagAndelDto> fordelAndeler = lagFordelBGAndeler(fordelBGPeriode, input, periode, grunnbeløp);
        fordelBGPeriode.setFordelBeregningsgrunnlagAndeler(fordelAndeler);
        return fordelBGPeriode;
    }

    private static void settEndretArbeidsforholdDto(BeregningsgrunnlagGUIInput input, FordelBeregningsgrunnlagDto bgDto) {
        RefusjonEllerGraderingArbeidsforholdDtoTjeneste
                .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning())
                .forEach(bgDto::leggTilArbeidsforholdTilFordeling);
    }

    private static List<FordelBeregningsgrunnlagAndelDto> lagFordelBGAndeler(FordelBeregningsgrunnlagPeriodeDto fordelBGPeriode,
                                                                             BeregningsgrunnlagGUIInput input,
                                                                             BeregningsgrunnlagPeriodeDto periode,
                                                                             Beløp grunnbeløp) {
        var aktivitetGradering = input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag ?
                ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).getAktivitetGradering() : AktivitetGradering.INGEN_GRADERING;
        List<FordelBeregningsgrunnlagAndelDto> fordelAndeler = FordelBeregningsgrunnlagAndelDtoTjeneste.lagEndretBgAndelListe(input, periode);

        fordelBGPeriode.setHarPeriodeAarsakGraderingEllerRefusjon(ManuellBehandlingRefusjonGraderingDtoTjeneste
                .skalSaksbehandlerRedigereInntekt(
                        input.getBeregningsgrunnlagGrunnlag(),
                        aktivitetGradering,
                        periode,
                        input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder(),
                        input.getInntektsmeldinger(),
                        input.getForlengelseperioder()));
        fordelBGPeriode.setSkalRedigereInntekt(ManuellBehandlingRefusjonGraderingDtoTjeneste
                .skalSaksbehandlerRedigereInntekt(
                        input.getBeregningsgrunnlagGrunnlag(),
                        aktivitetGradering,
                        periode,
                        input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder(),
                        input.getInntektsmeldinger(),
                        input.getForlengelseperioder()));
        fordelBGPeriode.setSkalPreutfyllesMedBeregningsgrunnlag(ManuellBehandlingRefusjonGraderingDtoTjeneste
                .skalRedigereGrunnetTidligerePerioder(
                        input.getBeregningsgrunnlagGrunnlag(),
                        aktivitetGradering,
                        periode,
                        input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder(),
                        input.getInntektsmeldinger(),
                        input.getForlengelseperioder()));
        fordelBGPeriode.setSkalKunneEndreRefusjon(ManuellBehandlingRefusjonGraderingDtoTjeneste
                .skalSaksbehandlerRedigereRefusjon(
                        input.getBeregningsgrunnlagGrunnlag(),
                        aktivitetGradering,
                        periode,
                        input.getInntektsmeldinger(),
                        grunnbeløp,
                        input.getForlengelseperioder()));
        RefusjonDtoTjeneste.slåSammenRefusjonForAndelerISammeArbeidsforhold(fordelAndeler);
        return fordelAndeler;
    }

}
