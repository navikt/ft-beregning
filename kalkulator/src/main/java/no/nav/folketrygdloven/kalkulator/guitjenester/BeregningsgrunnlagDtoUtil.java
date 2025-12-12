package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Organisasjonstype;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.StillingsprosentDto;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

public class BeregningsgrunnlagDtoUtil {

    private static final KalkulatorGuiInterface GUI_TJENESTE = new BeregningsgrunnlagGuiTjeneste();

    private BeregningsgrunnlagDtoUtil() {
        // Skjul
    }

    static FaktaOmBeregningAndelDto lagFaktaOmBeregningAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                             YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                             InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                             BeregningsgrunnlagPeriodeDto periode) {
        var andelDto = settVerdierForAndel(andel, ytelsespesifiktGrunnlag, inntektArbeidYtelseGrunnlag, periode);
        andelDto.setAndelsnr(andel.getAndelsnr());
        return andelDto;
    }

    private static FaktaOmBeregningAndelDto settVerdierForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                BeregningsgrunnlagPeriodeDto periode) {
        var andelDto = new FaktaOmBeregningAndelDto();
        andelDto.setAktivitetStatus(andel.getAktivitetStatus());
        andelDto.setInntektskategori(andel.getGjeldendeInntektskategori());
        andelDto.setFastsattAvSaksbehandler(andel.getFastsattAvSaksbehandler());
        andelDto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        andelDto.setKilde(andel.getKilde());
        GUI_TJENESTE.finnArbeidsprosenterIPeriode(andel, ytelsespesifiktGrunnlag, periode.getPeriode()).forEach(andelDto::leggTilAndelIArbeid);
        lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag).ifPresent(andelDto::setArbeidsforhold);
        return andelDto;
    }

    public static Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                     Optional<InntektsmeldingDto> inntektsmeldingOptional,
                                                                                     InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        var dto = new BeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto, inntektsmeldingOptional, inntektArbeidYtelseGrunnlag, null);
    }

    public static Optional<BeregningsgrunnlagArbeidsforholdDto> lagUtvidetArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                            Optional<InntektsmeldingDto> inntektsmeldingOptional,
                                                                                            InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                                            Skjæringstidspunkt skjæringstidspunkt) {
        var dto = new BeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto, inntektsmeldingOptional, inntektArbeidYtelseGrunnlag, skjæringstidspunkt);
    }

    public static Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdEndringDto(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                            InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        BeregningsgrunnlagArbeidsforholdDto dto = new FordelBeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto, Optional.empty(), inntektArbeidYtelseGrunnlag, null);
    }

    private static Optional<BeregningsgrunnlagArbeidsforholdDto> lagBeregningsgrunnlagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                                        BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                                                                                        Optional<InntektsmeldingDto> inntektsmeldingOptional,
                                                                                                        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                                                        Skjæringstidspunkt skjæringstidspunkt) {
        if (skalIkkeOppretteArbeidsforhold(andel)) {
            return Optional.empty();
        }
        mapBgAndelArbeidsforhold(andel, arbeidsforhold, inntektsmeldingOptional, inntektArbeidYtelseGrunnlag, skjæringstidspunkt);
        arbeidsforhold.setArbeidsforholdType(andel.getArbeidsforholdType());
        return Optional.of(arbeidsforhold);
    }

    private static boolean skalIkkeOppretteArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        var arbeidsforholdTypeErIkkeSatt =
            andel.getArbeidsforholdType() == null || OpptjeningAktivitetType.UDEFINERT.equals(andel.getArbeidsforholdType());
        return arbeidsforholdTypeErIkkeSatt && !andel.getBgAndelArbeidsforhold().isPresent();

    }

    private static void mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                 BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                                 Optional<InntektsmeldingDto> inntektsmelding,
                                                 InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                 Skjæringstidspunkt skjæringstidspunkt) {
        andel.getBgAndelArbeidsforhold().ifPresent(bga -> {
            var arbeidsgiver = bga.getArbeidsgiver();
            arbeidsforhold.setStartdato(bga.getArbeidsperiodeFom());
            arbeidsforhold.setOpphoersdato(finnKorrektOpphørsdato(andel));
            arbeidsforhold.setArbeidsforholdId(bga.getArbeidsforholdRef().getReferanse());
            arbeidsforhold.setRefusjonPrAar(ModellTyperMapper.beløpTilDto(bga.getGjeldendeRefusjonPrÅr()));
            arbeidsforhold.setNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().map(ModellTyperMapper::beløpTilDto).orElse(null));
            arbeidsforhold.setNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().map(ModellTyperMapper::beløpTilDto).orElse(null));
            inntektsmelding.ifPresent(im -> arbeidsforhold.setBelopFraInntektsmeldingPrMnd(ModellTyperMapper.beløpTilDto(im.getInntektBeløp())));
            mapArbeidsgiver(arbeidsforhold, arbeidsgiver);
            finnEksternArbeidsforholdId(andel, inntektArbeidYtelseGrunnlag).ifPresent(
                ref -> arbeidsforhold.setEksternArbeidsforholdId(ref.getReferanse()));
            if (skjæringstidspunkt != null) {
                mapStillingsprosenterOgSisteLønnsendringsdato(arbeidsforhold, inntektArbeidYtelseGrunnlag, skjæringstidspunkt, arbeidsgiver);
            }
        });
    }

    private static Optional<EksternArbeidsforholdRef> finnEksternArbeidsforholdId(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var agOpt = andel.getArbeidsgiver();
        var refOpt = andel.getArbeidsforholdRef();
        if (agOpt.isEmpty() || refOpt.isEmpty()) {
            return Optional.empty();
        }
        return iayGrunnlag.getArbeidsforholdInformasjon().map(d -> d.finnEkstern(agOpt.get(), refOpt.get()));
    }

    private static void mapArbeidsgiver(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold, Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver != null) {
            arbeidsforhold.setArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
            if (OrgNummer.erKunstig(arbeidsgiver.getOrgnr())) {
                arbeidsforhold.setOrganisasjonstype(Organisasjonstype.KUNSTIG);
            }
        }
    }

    private static void mapStillingsprosenterOgSisteLønnsendringsdato(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                                                      InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                      Skjæringstidspunkt skjæringstidspunkt,
                                                                      Arbeidsgiver arbeidsgiver) {
        var aktørArbeid = inntektArbeidYtelseGrunnlag.getAktørArbeidFraRegister().orElse(null);
        var intervall = getIntervallSisteÅrFørStp(skjæringstidspunkt);
        if (aktørArbeid != null && intervall != null) {
            aktørArbeid.hentAlleYrkesaktiviteter()
                .stream()
                .filter(ya -> ya.getArbeidsgiver().equals(arbeidsgiver) && ya.getArbeidsforholdRef()
                    .getReferanse()
                    .equals(arbeidsforhold.getArbeidsforholdId()))
                .findFirst()
                .ifPresent(ya -> {
                    arbeidsforhold.setStillingsprosenter(getStillingsprosenter(ya.getAlleAktivitetsAvtaler(), intervall));
                    arbeidsforhold.setSisteLønnsendringsdato(getSisteLønnsendringsdato(ya.getAlleAktivitetsAvtaler(), intervall));
                });
        }
    }

    private static List<StillingsprosentDto> getStillingsprosenter(Collection<AktivitetsAvtaleDto> avtaler, Intervall intervallSisteÅret) {
        return avtaler.stream()
            .filter(avtale -> avtale.getStillingsprosent() != null && erAvtaleIPeriode(avtale, intervallSisteÅret))
            .map(avtale -> new StillingsprosentDto(avtale.getStillingsprosent().verdi(), avtale.getPeriode().getFomDato(),
                avtale.getPeriode().getTomDato()))
            .toList();
    }

    private static LocalDate getSisteLønnsendringsdato(Collection<AktivitetsAvtaleDto> avtaler, Intervall intervallSisteÅret) {
        return avtaler.stream()
            .filter(avtale -> avtale.getSisteLønnsendringsdato() != null && erAvtaleIPeriode(avtale, intervallSisteÅret))
            .map(AktivitetsAvtaleDto::getSisteLønnsendringsdato)
            .max(LocalDate::compareTo)
            .orElse(null);
    }

    private static Intervall getIntervallSisteÅrFørStp(Skjæringstidspunkt skjæringstidspunkt) {
        return Optional.ofNullable(skjæringstidspunkt)
            .map(stp -> Optional.ofNullable(stp.getSkjæringstidspunktBeregning()).orElse(stp.getSkjæringstidspunktOpptjening()))
            .map(stp -> Intervall.fraOgMedTilOgMed(stp.minusMonths(12).plusDays(1), stp))
            .orElse(null);
    }

    private static boolean erAvtaleIPeriode(AktivitetsAvtaleDto avtale, Intervall periode) {
        return !avtale.erAnsettelsesPeriode() && periode.overlapper(avtale.getPeriode());
    }

    private static LocalDate finnKorrektOpphørsdato(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold()
            .flatMap(BGAndelArbeidsforholdDto::getArbeidsperiodeTom)
            .filter(tom -> !TIDENES_ENDE.equals(tom))
            .orElse(null);
    }
}
