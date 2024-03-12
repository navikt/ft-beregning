package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.besteberegning.modell.output.AktivitetNøkkel;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetAndel;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegningOutput;
import no.nav.folketrygdloven.besteberegning.modell.output.Inntekt;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapOpptjeningAktivitetFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class MapBesteberegningFraRegelTilVL {
    private static final List<Aktivitet> YTELSER_FRA_SAMMENLIGNINGSFILTERET = Arrays.asList(Aktivitet.SYKEPENGER_MOTTAKER, Aktivitet.FORELDREPENGER_MOTTAKER,
            Aktivitet.SVANGERSKAPSPENGER_MOTTAKER);

    public static BeregningsgrunnlagDto mapTilBeregningsgrunnlag(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                           BesteberegningOutput output) {
        BeregningsgrunnlagDto gammeltGrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
        var nyttGrunnlag = new BeregningsgrunnlagDto(gammeltGrunnlag);
        validerIngenBesteberegningSatt(nyttGrunnlag);
        if  (output.getSkalBeregnesEtterSeksBesteMåneder()) {
            oppdaterBeregningForAndelerIBesteberegnetGrunnlag(nyttGrunnlag, output);
            settBesteberegningTilNullForAndreAndeler(nyttGrunnlag);
        }
        return nyttGrunnlag;
    }


    private static void validerIngenBesteberegningSatt(BeregningsgrunnlagDto nyttGrunnlag) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedBesteberegningSatt = nyttGrunnlag.getBeregningsgrunnlagPerioder().stream()
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .filter(a -> a.getBesteberegningPrÅr() != null)
                .collect(Collectors.toList());
        if (!andelerMedBesteberegningSatt.isEmpty()) {
            throw new IllegalStateException("Feil ved mapping fra regelmodell til domenemodell i besteberegning:" +
                    " Det finnes andeler med besteberegnet satt, ugyldig tilstand. Andeler: " + andelerMedBesteberegningSatt);
        }
    }

    private static void oppdaterBeregningForAndelerIBesteberegnetGrunnlag(BeregningsgrunnlagDto nyttGrunnlag, BesteberegningOutput output) {
        List<BesteberegnetAndel> andelListe = output.getBesteberegnetGrunnlag().getBesteberegnetAndelList();
        nyttGrunnlag.getBeregningsgrunnlagPerioder()
                .forEach(p -> andelListe
                        .forEach(a -> oppdaterAndelerMedBesteberegnetInntekt(p, a)));
    }

    private static void settBesteberegningTilNullForAndreAndeler(BeregningsgrunnlagDto nyttGrunnlag) {
        nyttGrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream()).forEach(andel -> {
            if (andel.getBesteberegningPrÅr() == null) {
                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel).medBesteberegningPrÅr(Beløp.ZERO);
            }
        });
    }

    private static void oppdaterAndelerMedBesteberegnetInntekt(BeregningsgrunnlagPeriodeDto periode, BesteberegnetAndel a) {
        AktivitetNøkkel aktivitetNøkkel = a.getAktivitetNøkkel();
        if (harArbeidsgiver(a.getAktivitetNøkkel())) {
            fordelTilArbeidsandeler(periode, a);
        } else {
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = finnEnesteMatchendeAndelIPeriode(periode, aktivitetNøkkel);
            if (matchendeAndel.isPresent()) {
                oppdaterBesteberegningForAndel(a.getBesteberegnetPrÅr(), matchendeAndel.get());
            } else {
                leggPåDagpenger(periode, a);
            }
        }
    }

    private static void fordelTilArbeidsandeler(BeregningsgrunnlagPeriodeDto periode, BesteberegnetAndel a) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndeler = finnArbeidstakerAndel(periode, a.getAktivitetNøkkel());
        if (matchendeAndeler.isEmpty()) {
            leggPåDagpenger(periode, a);
        } else {
            fordelLiktTilAlleMatchendeAndeler(matchendeAndeler, a);
        }
    }

    private static void fordelLiktTilAlleMatchendeAndeler(List<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndeler, BesteberegnetAndel a) {
        var antallAndeler = BigDecimal.valueOf(matchendeAndeler.size());
        // antallAndeler skal aldri være 0 her, sjekkes utenfor
        var besteberegnetPrAndel = a.getBesteberegnetPrÅr().divide(antallAndeler, 10, RoundingMode.HALF_EVEN);
        matchendeAndeler.forEach(andel -> oppdaterBesteberegningForAndel(besteberegnetPrAndel, andel));
    }

    private static boolean harArbeidsgiver(AktivitetNøkkel aktivitetNøkkel) {
        return aktivitetNøkkel.getOrgnr() != null || aktivitetNøkkel.getAktørId() != null;
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnEnesteMatchendeAndelIPeriode(BeregningsgrunnlagPeriodeDto periode, AktivitetNøkkel aktivitetNøkkel) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = Optional.empty();
        if (aktivitetNøkkel.getType().equals(Aktivitet.FRILANSINNTEKT)) {
            matchendeAndel = finnFørsteAndel(periode, AktivitetStatus.FRILANSER);
        } else if (aktivitetNøkkel.getType().equals(Aktivitet.NÆRINGSINNTEKT)) {
            matchendeAndel = finnFørsteAndel(periode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        } else if (YTELSER_FRA_SAMMENLIGNINGSFILTERET.contains(aktivitetNøkkel.getType())) {
            matchendeAndel = finnAndelForYtelse(periode, aktivitetNøkkel);
        }
        return matchendeAndel;
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelForYtelse(BeregningsgrunnlagPeriodeDto periode,
                                                                                     AktivitetNøkkel aktivitetNøkkel) {
        return switch (aktivitetNøkkel.getYtelseGrunnlagType()) {
            case YTELSE_FOR_ARBEID -> finnFørsteAndel(periode, AktivitetStatus.ARBEIDSTAKER);
            case YTELSE_FOR_FRILANS -> finnFørsteAndel(periode, AktivitetStatus.FRILANSER);
            case YTELSE_FOR_NÆRING -> finnFørsteAndel(periode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
            case YTELSE_FOR_ARBEIDSAVKLARINGSPENGER, YTELSE_FOR_DAGPENGER -> finnFørsteAndel(periode, AktivitetStatus.DAGPENGER);
        };
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> finnArbeidstakerAndel(BeregningsgrunnlagPeriodeDto periode, AktivitetNøkkel aktivitetNøkkel) {
        String identifikator = aktivitetNøkkel.getOrgnr() != null ? aktivitetNøkkel.getOrgnr() : aktivitetNøkkel.getAktørId();
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(bgAndel -> bgAndel.getArbeidsgiver().isPresent())
                .filter(bgAndel -> bgAndel.getArbeidsgiver().get().getIdentifikator().equals(identifikator))
                .filter(bgAndel -> bgAndel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().gjelderFor(InternArbeidsforholdRefDto.ref(aktivitetNøkkel.getArbeidsforholdId())))
                .collect(Collectors.toList());
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnFørsteAndel(BeregningsgrunnlagPeriodeDto periode, AktivitetStatus status) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream().filter(bgAndel -> bgAndel.getAktivitetStatus().equals(status))
                .findFirst();
    }

    private static void oppdaterBesteberegningForAndel(BigDecimal besteberegnetBeløp, BeregningsgrunnlagPrStatusOgAndelDto matchendeAndel) {
        var besteberegnet = matchendeAndel.getBesteberegningPrÅr();
        var beregnet = Beløp.safeVerdi(besteberegnet) == null ? Beløp.fra(besteberegnetBeløp) : besteberegnet.adder(Beløp.fra(besteberegnetBeløp));
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(matchendeAndel)
                .medBesteberegningPrÅr(beregnet);
    }

    private static void leggPåDagpenger(BeregningsgrunnlagPeriodeDto periode, BesteberegnetAndel a) {
        var dagpengeAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER))
                .findFirst();
        if (dagpengeAndel.isPresent()) {
            oppdaterBesteberegningForAndel(a.getBesteberegnetPrÅr(), dagpengeAndel.get());
        } else {
            BeregningsgrunnlagPrStatusOgAndelDto.ny().medKilde(AndelKilde.PROSESS_BESTEBEREGNING)
                    .medBesteberegningPrÅr(Beløp.fra(a.getBesteberegnetPrÅr()))
                    .medAktivitetStatus(AktivitetStatus.DAGPENGER)
                    .medInntektskategori(Inntektskategori.DAGPENGER)
                    .build(periode);
        }
    }

    public static List<BesteberegningMånedGrunnlag> mapSeksBesteMåneder(BesteberegningOutput output) {
        return output.getBesteMåneder().stream()
                .map(beregnetMånedsgrunnlag -> new BesteberegningMånedGrunnlag(
                        beregnetMånedsgrunnlag.getInntekter().stream().map(MapBesteberegningFraRegelTilVL::mapInntekt).collect(Collectors.toList()),
                        beregnetMånedsgrunnlag.getMåned()
                )).collect(Collectors.toList());

    }

    private static no.nav.folketrygdloven.kalkulator.steg.besteberegning.Inntekt mapInntekt(Inntekt inntekt) {
        if (inntekt.getAktivitetNøkkel().getType().equals(Aktivitet.ARBEIDSTAKERINNTEKT)) {
            return new no.nav.folketrygdloven.kalkulator.steg.besteberegning.Inntekt(
                    mapArbeidsgiver(inntekt.getAktivitetNøkkel()),
                    mapArbeidsforholdRef(inntekt.getAktivitetNøkkel()),
                    Beløp.fra(inntekt.getInntektPrMåned()));
        }
        return new no.nav.folketrygdloven.kalkulator.steg.besteberegning.Inntekt(mapAktivitetStatus(inntekt.getAktivitetNøkkel().getType()), Beløp.fra(inntekt.getInntektPrMåned()));
    }

    private static OpptjeningAktivitetType mapAktivitetStatus(Aktivitet type) {
        return MapOpptjeningAktivitetFraRegelTilVL.map(type);
    }

    private static InternArbeidsforholdRefDto mapArbeidsforholdRef(AktivitetNøkkel aktivitetNøkkel) {
        return aktivitetNøkkel.getArbeidsforholdId() != null ? InternArbeidsforholdRefDto.ref(aktivitetNøkkel.getArbeidsforholdId()) : InternArbeidsforholdRefDto.nullRef();
    }

    private static Arbeidsgiver mapArbeidsgiver(AktivitetNøkkel aktivitetNøkkel) {
        if (aktivitetNøkkel.getOrgnr() != null) {
            return Arbeidsgiver.virksomhet(aktivitetNøkkel.getOrgnr());
        } else if (aktivitetNøkkel.getAktørId() != null) {
            return Arbeidsgiver.person(new AktørId(aktivitetNøkkel.getAktørId()));
        }
        throw new IllegalArgumentException("Kan ikke mappe arbeidsgiver uten orgnr eller aktørid");
    }
}
