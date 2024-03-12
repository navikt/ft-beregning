package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapInntektskategoriRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;

public class MapFraFordelingsmodell {

    public static BeregningsgrunnlagDto map(List<FordelPeriodeModell> regelperioder,
                                            List<RegelResultat> regelResultater,
                                            BeregningsgrunnlagDto eksisterendeBG) {
        if (regelResultater.size() != regelperioder.size()) {
            throw new IllegalArgumentException("Antall regelperioder ("
                    + regelperioder.size()
                    + ") må være samme som antall regelresultater ("
                    + regelResultater.size() + ")");
        }
        Objects.requireNonNull(regelperioder, "regelperioder");
        Objects.requireNonNull(eksisterendeBG, "eksisterendeBg");
        return mapGrunnlag(regelperioder, eksisterendeBG);
    }

    private static BeregningsgrunnlagDto mapGrunnlag(List<FordelPeriodeModell> regelperioder, BeregningsgrunnlagDto eksisterendeBG) {
        var nyttBG = BeregningsgrunnlagDto.builder(eksisterendeBG).build();
        var mappedePerioder = regelperioder.stream()
                .map(p -> mapPeriode(p, nyttBG.getBeregningsgrunnlagPerioder())).collect(Collectors.toList());
        mappedePerioder.forEach(p -> fastsettAgreggerteVerdier(p, nyttBG));
        return nyttBG;
    }

    private static void fastsettAgreggerteVerdier(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        var bruttoPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder);
        BeregningsgrunnlagPeriodeDto.oppdater(periode)
                .medBruttoPrÅr(bruttoPrÅr.orElse(null))
                .build(eksisterendeVLGrunnlag);
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(FordelPeriodeModell regelPeriode, List<BeregningsgrunnlagPeriodeDto> bgPerioder) {
        var bgPeriode = finnBgPeriode(regelPeriode, bgPerioder).orElseThrow();
        var bgPeriodeBuilder = new BeregningsgrunnlagPeriodeDto.Builder(bgPeriode, true);
        regelPeriode.getAndeler().forEach(regelAndel -> {
            if (regelAndel.erNytt()) {
                var builder = mapNyAndel(bgPeriode, regelAndel);
                bgPeriodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(builder);
            } else {
                oppdaterEksisterendeAndel(bgPeriodeBuilder, regelAndel);
            }
        });
        return bgPeriodeBuilder.build();
    }

    private static void oppdaterEksisterendeAndel(BeregningsgrunnlagPeriodeDto.Builder eksisterendePeriode, FordelAndelModell regelAndel) {
        var andelBuilder = eksisterendePeriode.getBuilderForAndel(regelAndel.getAndelNr(), true)
                .orElseThrow(() -> new IllegalStateException("Finner ikke builder for eksisterende andel med andelsnr " + regelAndel.getAndelNr()));
        settFelterFraRegelPåBuilder(regelAndel, andelBuilder);
    }

    private static void settFelterFraRegelPåBuilder(FordelAndelModell regelAndel,
                                                    BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder) {
        andelBuilder.medInntektskategoriAutomatiskFordeling(MapInntektskategoriRegelTilVL.map(regelAndel.getInntektskategori()));
        regelAndel.getFordeltPrÅr().map(Beløp::fra).ifPresent(andelBuilder::medFordeltPrÅr);
        settFelterPåArbeidsforhold(regelAndel, andelBuilder).ifPresent(andelBuilder::medBGAndelArbeidsforhold);
    }

    private static Optional<BGAndelArbeidsforholdDto.Builder> settFelterPåArbeidsforhold(FordelAndelModell regelAndel,
                                                                                         BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder) {
        if (regelAndel.getFordeltRefusjonPrÅr().isEmpty() && regelAndel.getNaturalytelseBortfaltPrÅr().isEmpty()) {
            return Optional.empty();
        }
        var arbforBuilder = andelBuilder.getBgAndelArbeidsforholdDtoBuilder();
        regelAndel.getFordeltRefusjonPrÅr().map(Beløp::fra).ifPresent(fordeltRef -> arbforBuilder.medFordeltRefusjonPrÅr(verifiserIkkeNegativtBeløp(fordeltRef)));
        regelAndel.getNaturalytelseBortfaltPrÅr().map(Beløp::fra).ifPresent(bortfaltNat -> arbforBuilder.medNaturalytelseBortfaltPrÅr(verifiserIkkeNegativtBeløp(bortfaltNat)));
        return Optional.of(arbforBuilder);
    }

    private static boolean gjelderSammeArbeidsforhold(FordelAndelModell regelAndel, BeregningsgrunnlagPrStatusOgAndelDto kalkulusAndel) {
        var kalkulusRef = kalkulusAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).orElse(InternArbeidsforholdRefDto.nullRef());
        var regelRef = InternArbeidsforholdRefDto.ref(regelAndel.getArbeidsforhold().map(Arbeidsforhold::getArbeidsforholdId).orElse(null));
        return Objects.equals(kalkulusRef, regelRef);
    }

    private static boolean matcherArbeidsgiver(FordelAndelModell regelAndel, BeregningsgrunnlagPrStatusOgAndelDto kalkulusAndel) {
        var arbeidsgiverIdRegel = regelAndel.getArbeidsforhold().map(Arbeidsforhold::getArbeidsgiverId).orElse(null);
        var arbeidsgiverIdKalkulus = kalkulusAndel.getArbeidsgiver().map(Arbeidsgiver::getIdentifikator).orElse(null);
        return Objects.equals(arbeidsgiverIdKalkulus, arbeidsgiverIdRegel);
    }

    private static Optional<BeregningsgrunnlagPeriodeDto> finnBgPeriode(FordelPeriodeModell regelPeriode, List<BeregningsgrunnlagPeriodeDto> bgPerioder) {
        return bgPerioder.stream()
                .filter(bgp -> Objects.equals(bgp.getBeregningsgrunnlagPeriodeFom(), regelPeriode.getBgPeriode().getFom())
                        && Objects.equals(bgp.getBeregningsgrunnlagPeriodeTom(), regelPeriode.getBgPeriode().getTom()))
                .findFirst();
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder mapNyAndel(BeregningsgrunnlagPeriodeDto kalkulusPeriode, FordelAndelModell regelAndel) {
        if (regelAndel.getAndelNr() != null) {
            throw new IllegalStateException("Andelsnr er satt, men regelAndel er markert som ny. Ugyldig tilstand for andel" + regelAndel);
        }
        var kalkulusAndel = kalkulusPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> matcherAktivitetstatus(regelAndel, bgpsa)
                        && (!bgpsa.getAktivitetStatus().erArbeidstaker() || matcherArbeidsinformasjon(regelAndel, bgpsa)))
                .filter(bgpsa -> matcherArbeidsgiver(regelAndel, bgpsa))
                .filter(bgpsa -> gjelderSammeArbeidsforhold(regelAndel, bgpsa))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Har opprettet ny andel som ikke matcher noen eksiterende arbeidsforhold under automatisk fordeling"));
        var andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medKilde(AndelKilde.PROSESS_OMFORDELING)
                .medArbforholdType(kalkulusAndel.getArbeidsforholdType())
                .medAktivitetStatus(kalkulusAndel.getAktivitetStatus());
        if (kalkulusAndel.getBeregningsperiode() != null) {
            andelBuilder.medBeregningsperiode(kalkulusAndel.getBeregningsperiodeFom(), kalkulusAndel.getBeregningsperiodeTom());
        }
        kalkulusAndel.getBgAndelArbeidsforhold().ifPresent(bga -> andelBuilder.medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder(bga)));
        settFelterFraRegelPåBuilder(regelAndel, andelBuilder);
        return andelBuilder;
    }

    private static boolean matcherAktivitetstatus(FordelAndelModell regelAndel, BeregningsgrunnlagPrStatusOgAndelDto bgpsa) {
        return bgpsa.getAktivitetStatus().getKode().equals(regelAndel.getAktivitetStatus().name());
    }

    private static boolean matcherArbeidsinformasjon(FordelAndelModell regelAndel, BeregningsgrunnlagPrStatusOgAndelDto bgpsa) {
        return bgpsa.getBgAndelArbeidsforhold().isPresent() &&
                matcherArbeidsgiver(regelAndel, bgpsa) &&
                gjelderSammeArbeidsforhold(regelAndel, bgpsa);
    }

    private static Beløp verifiserIkkeNegativtBeløp(Beløp beløp) {
        if (beløp.compareTo(Beløp.ZERO) < 0) {
            throw new IllegalStateException("Beløp er negativ, ugyldig tilstand. Beløp var:  " + beløp);
        }
        return beløp;
    }
}
