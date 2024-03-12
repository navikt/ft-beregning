package no.nav.folketrygdloven.kalkulator.guitjenester.inntektsgrunnlag;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PGIType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagInntektDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagMånedDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.PGIGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.PGIPrÅrDto;

public class InntektsgrunnlagMapper {
    private final Optional<Intervall> sammenligningsperiode;
    private final List<Arbeidsgiver> frilansArbeidsgivere;

    public InntektsgrunnlagMapper(Optional<Intervall> sammenligningsperiode,
                                  List<Arbeidsgiver> frilansArbeidsgivere) {
        this.sammenligningsperiode = sammenligningsperiode;
        this.frilansArbeidsgivere = frilansArbeidsgivere;
    }

    public Optional<InntektsgrunnlagDto> map(Collection<InntektDto> alleInntekter) {

        if (alleInntekter.isEmpty()) {
            return Optional.empty();
        }
        List<InntektsgrunnlagMånedDto> sammenligningsgrunnlagInntekter = mapSammenligningsgrunnlagInntekter(alleInntekter);
        List<PGIPrÅrDto> pgiGrunnlagInntekter  = mapPGIGrunnlagInntekter(alleInntekter);

        return Optional.of(new InntektsgrunnlagDto(sammenligningsgrunnlagInntekter, pgiGrunnlagInntekter));
    }

    private List<PGIPrÅrDto> mapPGIGrunnlagInntekter(Collection<InntektDto> alleInntekter) {
        Optional<InntektDto> sigrunInntekt = alleInntekter.stream()
                .filter(innt -> innt.getInntektsKilde().equals(InntektskildeType.SIGRUN))
                .findFirst();
        if (sigrunInntekt.isEmpty()) {
            return Collections.emptyList();
        }
        var pgiGrunnlagÅr = sigrunInntekt.get().getAlleInntektsposter().stream()
                .map(post -> post.getPeriode().getFomDato().getYear())
                .collect(Collectors.toUnmodifiableSet());
        return pgiGrunnlagÅr.stream()
                .map(år -> mapSkattegrunnlag(år, sigrunInntekt.get().getAlleInntektsposter()))
                .toList();
    }

    private PGIPrÅrDto mapSkattegrunnlag(Integer år, Collection<InntektspostDto> alleInntektsposter) {
        var beløpPrType = alleInntektsposter.stream()
                .filter(ipost -> ipost.getPeriode().getFomDato().getYear() == år)
                .collect(Collectors.toMap(
                        it -> finnPGIType(it.getInntektspostType()),
                        InntektspostDto::getBeløp,
                        Beløp::adder));
        var grunnlagPrType = beløpPrType.entrySet().stream().map(e -> new PGIGrunnlagDto(e.getKey(), ModellTyperMapper.beløpTilDto(e.getValue()))).toList();
        return new PGIPrÅrDto(år, grunnlagPrType);
    }

    private PGIType finnPGIType(InntektspostType inntektspostType) {
        return switch (inntektspostType) {
            case LØNN -> PGIType.LØNN;
            case SELVSTENDIG_NÆRINGSDRIVENDE, NÆRING_FISKE_FANGST_FAMBARNEHAGE -> PGIType.NÆRING;
            // Bør ikke forekomme på disse dataene, men vanskelig å forutse hva som kommer fra sigrun
            case UDEFINERT, VANLIG, YTELSE -> PGIType.UDEFINERT;
        };
    }

    private List<InntektsgrunnlagMånedDto> mapSammenligningsgrunnlagInntekter(Collection<InntektDto> alleInntekter) {
        List<InntektDtoMedMåned> alleInntektsposter = alleInntekter.stream()
                .filter(i -> i.getInntektsKilde().equals(InntektskildeType.INNTEKT_SAMMENLIGNING))
                .map(this::mapInntektATFLYtelse)
                .flatMap(Collection::stream)
                .toList();
        Map<LocalDate, List<InntektDtoMedMåned>> dateMap = alleInntektsposter.stream().collect(Collectors.groupingBy(intp -> intp.månedFom));
        if (dateMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<InntektsgrunnlagMånedDto> måneder = new ArrayList<>();
        dateMap.forEach((månedFom, poster) -> {
            List<InntektsgrunnlagInntektDto> inntekDtoer = poster.stream()
                    .map(post -> new InntektsgrunnlagInntektDto(post.inntektAktivitetType, ModellTyperMapper.beløpTilDto(post.beløp)))
                    .toList();
            LocalDate tom = månedFom.with(TemporalAdjusters.lastDayOfMonth());
            måneder.add(new InntektsgrunnlagMånedDto(månedFom, tom, inntekDtoer));
        });
        return måneder;
    }

    private List<InntektDtoMedMåned> mapInntektATFLYtelse(InntektDto inn) {
        if (!inn.getInntektsKilde().equals(InntektskildeType.INNTEKT_SAMMENLIGNING) || sammenligningsperiode.isEmpty()) {
            return Collections.emptyList();
        }
        return inn.getAlleInntektsposter().stream()
                .filter(intp -> sammenligningsperiode.get().inkluderer(intp.getPeriode().getFomDato().withDayOfMonth(1)))
                .map(intp -> new InntektDtoMedMåned(finnInntektType(inn.getArbeidsgiver(), intp.getInntektspostType()),
                        Beløp.safeVerdi(intp.getBeløp()) != null ? intp.getBeløp() : Beløp.ZERO,
                        intp.getPeriode().getFomDato().withDayOfMonth(1)))
                .collect(Collectors.toList());

    }

    private InntektAktivitetType finnInntektType(Arbeidsgiver arbeidsgiver, InntektspostType inntektspostType) {
        if (InntektspostType.YTELSE.equals(inntektspostType)) {
            return InntektAktivitetType.YTELSEINNTEKT;
        }
        if (arbeidsgiver == null) {
            return InntektAktivitetType.UDEFINERT;
        }
        return frilansArbeidsgivere.contains(arbeidsgiver) ? InntektAktivitetType.FRILANSINNTEKT : InntektAktivitetType.ARBEIDSTAKERINNTEKT;
    }

    record InntektDtoMedMåned(InntektAktivitetType inntektAktivitetType,
                              Beløp beløp, LocalDate månedFom) {
    }
}
