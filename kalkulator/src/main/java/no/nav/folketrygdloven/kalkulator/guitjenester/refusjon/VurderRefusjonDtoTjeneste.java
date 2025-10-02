package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AndelerMedØktRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravSomKommerForSentDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;

public final class VurderRefusjonDtoTjeneste {

    private VurderRefusjonDtoTjeneste() {
        // Skjuler default
    }

    public static Optional<RefusjonTilVurderingDto> lagRefusjonTilVurderingDto(BeregningsgrunnlagGUIInput input) {
        var beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes();
        var originaleGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().stream()
                .flatMap(gr -> gr.getBeregningsgrunnlagHvisFinnes().stream()).toList();
        if (originaleGrunnlag.isEmpty() || beregningsgrunnlag.isEmpty() || beregningsgrunnlag.get().getGrunnbeløp() == null) {
            return Optional.empty();
        }
        var grenseverdi = beregningsgrunnlag.get().getGrunnbeløp().multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());

        var andelerMedØktRefusjon = originaleGrunnlag.stream()
                .flatMap(originaltBg -> AndelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(beregningsgrunnlag.get(), originaltBg, grenseverdi, input.getYtelsespesifiktGrunnlag()).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, unikeElementer()));
        if (!andelerMedØktRefusjon.isEmpty()) {
            return LagVurderRefusjonDto.lagDto(andelerMedØktRefusjon, input);
        }

        return lagDtoBasertPåTidligereAvklaringer(input);
    }

    public static List<RefusjonskravSomKommerForSentDto> lagRefusjonskravSomKommerForSentListe(BeregningsgrunnlagGUIInput input) {
        var refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag()
            .getRefusjonOverstyringer()
            .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
            .orElse(Collections.emptyList());

        var arbeidsgivere = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgivereSomHarSøktRefusjonForSent(input.getIayGrunnlag(), input.getBeregningsgrunnlagGrunnlag(), input.getKravperioderPrArbeidsgiver(), input.getFagsakYtelseType());
        return arbeidsgivere.stream().map(arbeidsgiver -> {
            var dto = new RefusjonskravSomKommerForSentDto();
            dto.setArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
            sjekkStatusPåRefusjon(arbeidsgiver.getIdentifikator(), refusjonOverstyringer, input.getSkjæringstidspunktForBeregning()).ifPresent(
                dto::setErRefusjonskravGyldig);
            return dto;
        }).toList();
    }

    private static BinaryOperator<List<RefusjonAndel>> unikeElementer() {
        return (andeler1, andeler2) -> {
            var nyListe = new ArrayList<RefusjonAndel>();
            nyListe.addAll(andeler1);
            nyListe.addAll(andeler2);
            return nyListe.stream().distinct().toList();
        };
    }

    // Metode for å støtte visning av saker som tidligere er løst men som av ulike grunner ikke lenger gir samme resultat i avklaringsbehovutledning
    private static Optional<RefusjonTilVurderingDto> lagDtoBasertPåTidligereAvklaringer(BeregningsgrunnlagGUIInput input) {
        var hardkodetIntervall = Intervall.fraOgMed(input.getSkjæringstidspunktForBeregning()); // Bruker hele perioden det kan kreves refusjon for
        List<RefusjonAndel> andeler = new ArrayList<>();
        var refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList())
                .stream()
                .filter(pa -> !pa.getRefusjonPerioder().isEmpty())
                .toList();
        refusjonOverstyringer.forEach(avkalring -> {
            var tidligereAvklaringerPåAG = avkalring.getRefusjonPerioder().stream()
                    .map(refusjonPeriode -> new RefusjonAndel(AktivitetStatus.ARBEIDSTAKER, avkalring.getArbeidsgiver(), refusjonPeriode.getArbeidsforholdRef(),
                            Beløp.ZERO, Beløp.ZERO)) // De to siste parameterne brukes ikke for å lage dto så kan settes til dummy-verdier
                    .toList();
            andeler.addAll(tidligereAvklaringerPåAG);
        });
        if (andeler.isEmpty()) {
            return Optional.empty();
        }
        Map<Intervall, List<RefusjonAndel>> avklaringMap = new HashMap<>();
        avklaringMap.put(hardkodetIntervall, andeler);
        return LagVurderRefusjonDto.lagDto(avklaringMap, input);
    }

    private static Optional<Boolean> sjekkStatusPåRefusjon(String identifikator,
                                                           List<BeregningRefusjonOverstyringDto> refusjonOverstyringer,
                                                           LocalDate skjæringstidspunktForBeregning) {
        var statusOpt = refusjonOverstyringer.stream()
            .filter(refusjonOverstyring -> refusjonOverstyring.getArbeidsgiver().getIdentifikator().equals(identifikator))
            .findFirst();

        if (statusOpt.isEmpty() && refusjonOverstyringer.isEmpty()) {
            return Optional.empty();
        }

        return getErFristUtvidet(statusOpt, skjæringstidspunktForBeregning);
    }

    private static Optional<Boolean> getErFristUtvidet(Optional<BeregningRefusjonOverstyringDto> statusOpt,
                                                       LocalDate skjæringstidspunktForBeregning) {
        return statusOpt.flatMap(o -> {
            if (o.getFørsteMuligeRefusjonFom().isPresent()) {
                return Optional.of(skjæringstidspunktForBeregning.isEqual(o.getFørsteMuligeRefusjonFom().get()));
            }
            return o.getErFristUtvidet();
        });
    }
}
