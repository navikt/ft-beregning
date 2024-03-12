package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;

/**
 * Tre mulige tilfeller avhengig av hvordan andelene i revurderingen ser ut:
 *
 * 1) Både aggregatandeler og spesifikke andeler:
 * Aggregatandeler og andeler uten matchende original andel må matches mot alle originale aggregatandeler og andeler som ikke matcher mot
 * noen andel i revurderingen. Alle andre andeler matches mot sin originale andel.
 *
 * 2) Bare spesifikke andeler:
 * For hver spesifikke andel må vi matche mot alle eksakt matchende (matcher arbeidsforhold referanse) originale andeler
 * Andeler som ikke kan matches på denne måten må mathces mot original aggregatandel om den finnes

 * 3) Bare aggregatandeler:
 *  Om vi bare har aggregat matcher vi bare mot alle originale andeler
 */

public class FinnAndelerMedØktRefusjonTjeneste {

    public static List<RefusjonAndel> finnAndelerPåSammeNøkkelMedØktRefusjon (List<RefusjonAndel> alleRevurderingAndeler, List<RefusjonAndel> alleOriginaleAndeler) {
        boolean finnesSpesifikkReferanse = alleRevurderingAndeler.stream().anyMatch(andel -> andel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold());
        boolean finnesGenerellReferanse = alleRevurderingAndeler.stream().anyMatch(andel -> !andel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold());

        if (finnesGenerellReferanse && finnesSpesifikkReferanse) {
            return håndterBlandingstilfelle(alleRevurderingAndeler, alleOriginaleAndeler);
        }

        if (finnesSpesifikkReferanse) {
            return håndterKunAndelerHarReferanse(alleRevurderingAndeler, alleOriginaleAndeler);
        }

        if (finnesGenerellReferanse) {
            return håndterKunAggregatandeler(alleRevurderingAndeler, alleOriginaleAndeler);
        }

        throw new IllegalStateException("Klarte ikke identifisere andeler med økt refusjon." +
                " Revurdering: " + alleRevurderingAndeler.toString() + " Original: " + alleOriginaleAndeler.toString());
    }

    private static List<RefusjonAndel> håndterKunAndelerHarReferanse(List<RefusjonAndel> alleRevurderingAndeler, List<RefusjonAndel> alleOriginaleAndeler) {
        List<RefusjonAndel> andelerMedØktRefusjon = new ArrayList<>();

        List<RefusjonAndel> revurderingAndelerUtenOriginalMatch = finnForskjellIReferanseMellomLister(alleRevurderingAndeler, alleOriginaleAndeler);

        List<RefusjonAndel> revurderingAndelerMedOriginalMatch = alleRevurderingAndeler.stream()
                .filter(ra -> !revurderingAndelerUtenOriginalMatch.contains(ra))
                .collect(Collectors.toList());
        andelerMedØktRefusjon.addAll(matchSpesifikkeAndeler(revurderingAndelerMedOriginalMatch, alleOriginaleAndeler));

        List<RefusjonAndel> originaleAggregatandeler = alleOriginaleAndeler.stream()
                .filter(oa -> !oa.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .collect(Collectors.toList());
        if (harAndelerØktRefusjon(originaleAggregatandeler, revurderingAndelerUtenOriginalMatch)) {
            andelerMedØktRefusjon.addAll(filtrerUtAndelerUtenRefusjon(revurderingAndelerUtenOriginalMatch));
        }
        return andelerMedØktRefusjon;
    }

    private static List<RefusjonAndel> matchSpesifikkeAndeler(List<RefusjonAndel> alleSpesifikkeRevurderingAndeler, List<RefusjonAndel> alleOriginaleAndeler) {
        List<RefusjonAndel> andelerMedØktRefusjon = new ArrayList<>();
        alleSpesifikkeRevurderingAndeler.forEach(ra -> {
            List<RefusjonAndel> matchendeSpesifikkeOriginaleAndeler = alleOriginaleAndeler.stream()
                    .filter(oa -> oa.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()
                            && oa.getArbeidsforholdRef().gjelderFor(ra.getArbeidsforholdRef()))
                    .collect(Collectors.toList());
            if (harAndelerØktRefusjon(matchendeSpesifikkeOriginaleAndeler, Collections.singletonList(ra))) {
                andelerMedØktRefusjon.add(ra);
            }
        });
        return andelerMedØktRefusjon;
    }

    private static List<RefusjonAndel> håndterKunAggregatandeler(List<RefusjonAndel> alleRevurderingAndeler, List<RefusjonAndel> alleOriginaleAndeler) {
        List<RefusjonAndel> revurderingAggregatAndel = alleRevurderingAndeler.stream()
                .filter(revurdering -> !revurdering.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .collect(Collectors.toList());
        return harAndelerØktRefusjon(alleOriginaleAndeler, revurderingAggregatAndel) ? revurderingAggregatAndel : Collections.emptyList();
    }

    private static List<RefusjonAndel> håndterBlandingstilfelle(List<RefusjonAndel> alleRevurderingAndeler, List<RefusjonAndel> alleOriginaleAndeler) {
        List<RefusjonAndel> andelerSomMåVurderes = new ArrayList<>();

        // Lager liste med revurderingandeler som ikke har match eller er aggregatandeler
        List<RefusjonAndel> revurderingAggregatOgAndelerUtenMatchIOriginal = finnForskjellIReferanseMellomLister(alleRevurderingAndeler, alleOriginaleAndeler);
        revurderingAggregatOgAndelerUtenMatchIOriginal.addAll(finnAggregatAndeler(alleRevurderingAndeler));
        List<RefusjonAndel> originalAggregatOgAndelerUtenMatchIRevurdering = finnForskjellIReferanseMellomLister(alleOriginaleAndeler, alleRevurderingAndeler);
        originalAggregatOgAndelerUtenMatchIRevurdering.addAll(finnAggregatAndeler(alleOriginaleAndeler));

        if (harAndelerØktRefusjon(originalAggregatOgAndelerUtenMatchIRevurdering, revurderingAggregatOgAndelerUtenMatchIOriginal)) {
            andelerSomMåVurderes.addAll(filtrerUtAndelerUtenRefusjon(revurderingAggregatOgAndelerUtenMatchIOriginal));
        }

        // Alle andre andeler kan matches mot original liste
        List<RefusjonAndel> revurderingAndelerMedOriginalMatch = alleRevurderingAndeler.stream()
                .filter(ra -> !revurderingAggregatOgAndelerUtenMatchIOriginal.contains(ra))
                .collect(Collectors.toList());
        andelerSomMåVurderes.addAll(matchSpesifikkeAndeler(revurderingAndelerMedOriginalMatch, alleOriginaleAndeler));

        return andelerSomMåVurderes;
    }

    private static List<RefusjonAndel> finnAggregatAndeler(List<RefusjonAndel> andeler) {
        return andeler.stream()
                .filter(andel -> !andel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .collect(Collectors.toList());
    }

    private static List<RefusjonAndel> filtrerUtAndelerUtenRefusjon(List<RefusjonAndel> andeler) {
        return andeler.stream()
                .filter(andel -> andel.getRefusjon().compareTo(Beløp.ZERO) > 0)
                .collect(Collectors.toList());
    }

    private static List<RefusjonAndel> finnForskjellIReferanseMellomLister(List<RefusjonAndel> listeSomSkalSjekkes, List<RefusjonAndel> listeÅSjekkeMot) {
        List<InternArbeidsforholdRefDto> referanserISjekkliste = listeÅSjekkeMot.stream()
                .map(RefusjonAndel::getArbeidsforholdRef)
                .filter(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold)
                .collect(Collectors.toList());
        return listeSomSkalSjekkes.stream()
                .filter(andel -> andel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()
                        && !referanserISjekkliste.contains(andel.getArbeidsforholdRef()))
                .collect(Collectors.toList());
    }

    private static boolean harAndelerØktRefusjon(List<RefusjonAndel> originaleAndeler, List<RefusjonAndel> revurderingAndeler) {
        var originalRefusjon = totalRefusjon(originaleAndeler);
        var revurderingRefusjon = totalRefusjon(revurderingAndeler);
        return revurderingRefusjon.compareTo(originalRefusjon) > 0;
    }

    private static Beløp totalRefusjon(List<RefusjonAndel> andeler) {
        return andeler.stream()
                .map(RefusjonAndel::getRefusjon)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }
}
