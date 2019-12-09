package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

public class FastsettPeriodeRegel {
    private FastsettPeriodeRegel() {
        // skjul public constructor
    }

    public static List<SplittetPeriode> fastsett(PeriodeModell input) {
        IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker = IdentifiserPeriodeÅrsaker.identifiser(input);
        return periodiserBeregningsgrunnlag(input, identifisertePeriodeÅrsaker);
    }

    private static List<SplittetPeriode> periodiserBeregningsgrunnlag(PeriodeModell input, IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker) {
        // lag alle periodene, med riktige andeler
        Map<LocalDate, Set<PeriodeSplittData>> periodeMap = identifisertePeriodeÅrsaker.getPeriodeMap();


        List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries = new ArrayList<>(periodeMap.entrySet());

        ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator = entries.listIterator();


        List<SplittetPeriode> list = new ArrayList<>();
        while (listIterator.hasNext()) {
            Map.Entry<LocalDate, Set<PeriodeSplittData>> entry = listIterator.next();
            LocalDate periodeFom = entry.getKey();
            Set<PeriodeSplittData> periodeSplittData = entry.getValue();

            List<BeregningsgrunnlagPrArbeidsforhold> førstePeriodeAndeler = input.getArbeidsforholdOgInntektsmeldinger().stream()
                .filter(im -> !im.erNyAktivitet())
                .map(im -> mapToArbeidsforhold(im, periodeFom))
                .collect(Collectors.toList());

            List<SplittetAndel> nyeAndeler = input.getArbeidsforholdOgInntektsmeldinger().stream()
                .filter(ArbeidsforholdOgInntektsmelding::erNyAktivitet)
                .filter(im -> !im.slutterFørSkjæringstidspunkt(input.getSkjæringstidspunkt()))
                .filter(im -> harRefusjonIPeriode(im, periodeFom)
                    || harGraderingIPeriode(im, periodeFom))
                .map(im -> mapSplittetAndel(im, periodeFom))
                .collect(Collectors.toList());

            nyeAndeler.addAll(input.getAndelGraderinger().stream()
                .filter(AndelGradering::erNyAktivitet)
                .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatusV2.SN) || andel.getAktivitetStatus().equals(AktivitetStatusV2.FL))
                .filter(andel -> harGraderingIPeriode(andel, periodeFom))
                .map(FastsettPeriodeRegel::mapSplittetAndelFLSN)
                .collect(Collectors.toList()));

            LocalDate tom = utledPeriodeTom(entries, listIterator);
            Periode periode = new Periode(periodeFom, tom);
            SplittetPeriode splittetPeriode = SplittetPeriode.builder()
                .medPeriode(periode)
                .medPeriodeÅrsaker(getPeriodeÅrsaker(periodeSplittData, input.getSkjæringstidspunkt(), periodeFom))
                .medFørstePeriodeAndeler(førstePeriodeAndeler)
                .medNyeAndeler(nyeAndeler)
                .build();
            list.add(splittetPeriode);
        }
        return list;
    }

    private static LocalDate utledPeriodeTom(List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries, ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator) {
        return listIterator.hasNext() ?
            entries.get(listIterator.nextIndex()).getKey().minusDays(1) :
            null;
    }

    private static boolean harRefusjonIPeriode(ArbeidsforholdOgInntektsmelding im, LocalDate periodeFom) {
        return im.getGyldigeRefusjonskrav().stream()
            .filter(refusjonskrav -> refusjonskrav.getPeriode().inneholder(periodeFom))
            .anyMatch(refusjonskrav -> refusjonskrav.getMånedsbeløp().compareTo(BigDecimal.ZERO) > 0);
    }

    private static boolean harGraderingIPeriode(AndelGradering im, LocalDate periodeFom) {
        return im.getGraderinger().stream()
            .anyMatch(gradering -> gradering.getPeriode().inneholder(periodeFom));
    }

    private static SplittetAndel mapSplittetAndelFLSN(AndelGradering im) {
        return SplittetAndel.builder()
            .medAktivitetstatus(im.getAktivitetStatus())
            .build();
    }

    private static SplittetAndel mapSplittetAndel(ArbeidsforholdOgInntektsmelding im, LocalDate periodeFom) {
        BigDecimal refusjonPrÅr = im.getGyldigeRefusjonskrav().stream()
            .filter(refusjonskrav -> refusjonskrav.getPeriode().inneholder(periodeFom))
            .map(refusjonskrav -> refusjonskrav.getMånedsbeløp().multiply(BigDecimal.valueOf(12)))
            .findFirst().orElse(BigDecimal.ZERO);

        Periode ansettelsesPeriode = im.getAnsettelsesperiode();

        return SplittetAndel.builder()
            .medAktivitetstatus(im.getAktivitetStatus())
            .medArbeidsforhold(im.getArbeidsforhold())
            .medRefusjonskravPrÅr(refusjonPrÅr)
            .medArbeidsperiodeFom(ansettelsesPeriode.getFom())
            .medArbeidsperiodeTom(ansettelsesPeriode.getTom())
            .build();
    }

    private static BeregningsgrunnlagPrArbeidsforhold mapToArbeidsforhold(ArbeidsforholdOgInntektsmelding im, LocalDate fom) {
        Optional<BigDecimal> refusjonskravPrÅr = im.getGyldigeRefusjonskrav().stream()
            .filter(refusjon -> refusjon.getPeriode().inneholder(fom))
            .findFirst()
            .map(refusjonskrav -> refusjonskrav.getMånedsbeløp().multiply(BigDecimal.valueOf(12)));
        Optional<BigDecimal> naturalytelseBortfaltPrÅr = im.getNaturalYtelser().stream()
            .filter(naturalYtelse -> naturalYtelse.getFom().isEqual(DateUtil.TIDENES_BEGYNNELSE))
            .filter(naturalYtelse -> naturalYtelse.getTom().isBefore(fom))
            .map(NaturalYtelse::getBeløp)
            .reduce(BigDecimal::add);
        Optional<BigDecimal> naturalytelseTilkommer = im.getNaturalYtelser().stream()
            .filter(naturalYtelse -> naturalYtelse.getTom().isEqual(DateUtil.TIDENES_ENDE))
            .filter(naturalYtelse -> naturalYtelse.getFom().isBefore(fom))
            .map(NaturalYtelse::getBeløp)
            .reduce(BigDecimal::add);
        return BeregningsgrunnlagPrArbeidsforhold.builder()
            .medAndelNr(im.getAndelsnr())
            .medRefusjonskravPrÅr(refusjonskravPrÅr.orElse(null))
            .medNaturalytelseTilkommetPrÅr(naturalytelseTilkommer.orElse(null))
            .medNaturalytelseBortfaltPrÅr(naturalytelseBortfaltPrÅr.orElse(null))
            .medArbeidsforhold(im.getArbeidsforhold())
            .build();
    }

    private static List<PeriodeÅrsak> getPeriodeÅrsaker(Set<PeriodeSplittData> periodeSplittData, LocalDate skjæringstidspunkt, LocalDate periodeFom) {
        return periodeSplittData.stream()
            .map(PeriodeSplittData::getPeriodeÅrsak)
            .filter(periodeÅrsak -> !PeriodeÅrsak.UDEFINERT.equals(periodeÅrsak))
            .filter(periodeÅrsak -> !(Set.of(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR).contains(periodeÅrsak)
                && skjæringstidspunkt.equals(periodeFom)))
            .collect(Collectors.toList());
    }
}
