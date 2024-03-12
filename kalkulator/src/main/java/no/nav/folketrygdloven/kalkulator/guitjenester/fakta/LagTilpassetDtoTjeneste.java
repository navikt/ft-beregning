package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelATDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelDtoFelles;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelFLDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelSNDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelYtelseDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.EgenNæringDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.PgiDto;

class LagTilpassetDtoTjeneste {

    private static final BigDecimal DAGPENGER_FAKTOR = BigDecimal.valueOf(62.4);
    private static final BigDecimal AAP_FAKTOR = BigDecimal.valueOf(66);
    private static final BigDecimal HUNDRE = BigDecimal.valueOf(100);

    private LagTilpassetDtoTjeneste() {
    }

    static BeregningsgrunnlagPrStatusOgAndelDto opprettTilpassetDTO(KoblingReferanse ref,
                                                                    no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                    InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                    Optional<FaktaAggregatDto> faktaAggregat) {
        if (AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(andel.getAktivitetStatus())  || AktivitetStatus.BRUKERS_ANDEL.equals(andel.getAktivitetStatus())) {
            return opprettSNDto(andel, inntektArbeidYtelseGrunnlag, ref.getSkjæringstidspunktBeregning());
        } else if (AktivitetStatus.ARBEIDSTAKER.equals(andel.getAktivitetStatus())
                && andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr).isPresent()) {
            return opprettATDto(andel);
        } else if (AktivitetStatus.FRILANSER.equals(andel.getAktivitetStatus())) {
            return opprettFLDto(faktaAggregat.flatMap(FaktaAggregatDto::getFaktaAktør));
        } else if (AktivitetStatus.DAGPENGER.equals(andel.getAktivitetStatus()) || AktivitetStatus.ARBEIDSAVKLARINGSPENGER.equals(andel.getAktivitetStatus())) {
            return opprettYtelseDto(ref, inntektArbeidYtelseGrunnlag, andel);
        } else {
            return new BeregningsgrunnlagPrStatusOgAndelDtoFelles();
        }
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto opprettSNDto(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                     InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                     LocalDate stpBG) {
        //Merk, PGI verdier ligger i kronologisk synkende rekkefølge og er pgi fra årene i beregningsperioden
        BeregningsgrunnlagPrStatusOgAndelSNDto dtoSN = new BeregningsgrunnlagPrStatusOgAndelSNDto();

        List<OppgittEgenNæringDto> egneNæringer = inntektArbeidYtelseGrunnlag.getOppgittOpptjening()
                .map(OppgittOpptjeningDto::getEgenNæring)
                .orElse(Collections.emptyList());

        dtoSN.setPgiSnitt(ModellTyperMapper.beløpTilDto(andel.getPgiSnitt()));


        // Næringer som startet etter skjæringstidspunktet for beregning er ikke relevante
        List<EgenNæringDto> næringer = egneNæringer.stream()
                .filter(en -> en.getPeriode().getFomDato() != null && en.getPeriode().getFomDato().isBefore(stpBG))
                .map(EgenNæringMapper::map)
                .collect(Collectors.toList());

        dtoSN.setNæringer(næringer);

        List<PgiDto> pgiDtoer = lagPgiDto(andel);
        dtoSN.setPgiVerdier(pgiDtoer);

        return dtoSN;
    }

    private static List<PgiDto> lagPgiDto(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        LocalDate beregningsperiodeTom = andel.getBeregningsperiodeTom();
        if (beregningsperiodeTom == null) {
            return Collections.emptyList();
        }
        List<PgiDto> liste = new ArrayList<>();
        liste.add(new PgiDto(ModellTyperMapper.beløpTilDto(andel.getPgi1()), beregningsperiodeTom.getYear()));
        liste.add(new PgiDto(ModellTyperMapper.beløpTilDto(andel.getPgi2()), beregningsperiodeTom.minusYears(1).getYear()));
        liste.add(new PgiDto(ModellTyperMapper.beløpTilDto(andel.getPgi3()), beregningsperiodeTom.minusYears(2).getYear()));
        return liste;
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto opprettATDto(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        BeregningsgrunnlagPrStatusOgAndelATDto dtoAT = new BeregningsgrunnlagPrStatusOgAndelATDto();
        dtoAT.setBortfaltNaturalytelse(andel.getBgAndelArbeidsforhold().orElseThrow().getNaturalytelseBortfaltPrÅr().map(ModellTyperMapper::beløpTilDto).orElseThrow());
        return dtoAT;
    }


    private static BeregningsgrunnlagPrStatusOgAndelFLDto opprettFLDto(Optional<FaktaAktørDto> faktaAktør) {
        BeregningsgrunnlagPrStatusOgAndelFLDto dtoFL = new BeregningsgrunnlagPrStatusOgAndelFLDto();
        dtoFL.setErNyoppstartet(faktaAktør.map(FaktaAktørDto::getErNyoppstartetFLVurdering).orElse(null));
        return dtoFL;
    }

    private static BeregningsgrunnlagPrStatusOgAndelYtelseDto opprettYtelseDto(KoblingReferanse ref,
                                                                               InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                               no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        BeregningsgrunnlagPrStatusOgAndelYtelseDto dtoYtelse = new BeregningsgrunnlagPrStatusOgAndelYtelseDto();
        YtelseFilterDto ytelseFilter = new YtelseFilterDto(inntektArbeidYtelseGrunnlag.getAktørYtelseFraRegister()).før(ref.getSkjæringstidspunktBeregning());
        var årsbeløpFraMeldekort = FinnInntektFraYtelse.finnÅrbeløpFraMeldekortForAndel(ref, andel, ytelseFilter);
        årsbeløpFraMeldekort.ifPresent(beløp -> {
            dtoYtelse.setBelopFraMeldekortPrAar(ModellTyperMapper.beløpTilDto(beløp));
            dtoYtelse.setBelopFraMeldekortPrMnd(ModellTyperMapper.beløpTilDto(beløp.divider(KonfigTjeneste.getMånederIÅr(), 10, RoundingMode.HALF_UP)));
            if (Beløp.safeVerdi(andel.getBruttoPrÅr()) != null) {
                dtoYtelse.setOppjustertGrunnlag(ModellTyperMapper.beløpTilDto(finnVisningstallForOppjustertGrunnlag(andel.getAktivitetStatus(), andel.getBruttoPrÅr())));
            }
        });

        return dtoYtelse;
    }

    private static Beløp finnVisningstallForOppjustertGrunnlag(AktivitetStatus aktivitetStatus, Beløp bruttoPrÅr) {
        if (AktivitetStatus.DAGPENGER.equals(aktivitetStatus)) {
            return oppjustertDagpengesats(bruttoPrÅr);
        } else {
            return oppjustertAAPSats(bruttoPrÅr);
        }
    }

    private static Beløp oppjustertAAPSats(Beløp bruttoPrÅr) {
        var mellomregning = bruttoPrÅr.divider(AAP_FAKTOR, 0, RoundingMode.HALF_EVEN);
        return mellomregning.multipliser(HUNDRE);
    }

    private static Beløp oppjustertDagpengesats(Beløp bruttoPrÅr) {
        var mellomregning = bruttoPrÅr.divider(DAGPENGER_FAKTOR, 0, RoundingMode.HALF_EVEN);
        return mellomregning.multipliser(HUNDRE);
    }
}
