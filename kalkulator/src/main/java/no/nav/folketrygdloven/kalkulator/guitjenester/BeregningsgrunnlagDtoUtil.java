package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Organisasjonstype;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagArbeidsforholdDto;
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
        FaktaOmBeregningAndelDto andelDto = settVerdierForAndel(andel, ytelsespesifiktGrunnlag, inntektArbeidYtelseGrunnlag, periode);
        andelDto.setAndelsnr(andel.getAndelsnr());
        return andelDto;
    }

    private static FaktaOmBeregningAndelDto settVerdierForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                BeregningsgrunnlagPeriodeDto periode) {
        FaktaOmBeregningAndelDto andelDto = new FaktaOmBeregningAndelDto();
        andelDto.setAktivitetStatus(andel.getAktivitetStatus());
        andelDto.setInntektskategori(andel.getGjeldendeInntektskategori());
        andelDto.setFastsattAvSaksbehandler(andel.getFastsattAvSaksbehandler());
        andelDto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        andelDto.setKilde(andel.getKilde());
        GUI_TJENESTE.finnArbeidsprosenterIPeriode(andel, ytelsespesifiktGrunnlag, periode.getPeriode())
                .forEach(andelDto::leggTilAndelIArbeid);
        lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag)
                .ifPresent(andelDto::setArbeidsforhold);
        return andelDto;
    }


    public static Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<InntektsmeldingDto> inntektsmeldingOptional, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        BeregningsgrunnlagArbeidsforholdDto dto = new BeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto, inntektsmeldingOptional, inntektArbeidYtelseGrunnlag);
    }

    public static Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdEndringDto(BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        BeregningsgrunnlagArbeidsforholdDto dto = new FordelBeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto, Optional.empty(), inntektArbeidYtelseGrunnlag);
    }

    private static Optional<BeregningsgrunnlagArbeidsforholdDto> lagBeregningsgrunnlagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                                        BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                                                                                        Optional<InntektsmeldingDto> inntektsmeldingOptional, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        if (skalIkkeOppretteArbeidsforhold(andel)) {
            return Optional.empty();
        }
        mapBgAndelArbeidsforhold(andel, arbeidsforhold, inntektsmeldingOptional, inntektArbeidYtelseGrunnlag);
        arbeidsforhold.setArbeidsforholdType(andel.getArbeidsforholdType());
        return Optional.of(arbeidsforhold);
    }

    private static boolean skalIkkeOppretteArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        boolean arbeidsforholdTypeErIkkeSatt = andel.getArbeidsforholdType() == null
                || OpptjeningAktivitetType.UDEFINERT.equals(andel.getArbeidsforholdType());
        return arbeidsforholdTypeErIkkeSatt && !andel.getBgAndelArbeidsforhold().isPresent();

    }

    private static void mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                 BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                                 Optional<InntektsmeldingDto> inntektsmelding, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        andel.getBgAndelArbeidsforhold().ifPresent(bga -> {
            Arbeidsgiver arbeidsgiver = bga.getArbeidsgiver();
            arbeidsforhold.setStartdato(bga.getArbeidsperiodeFom());
            arbeidsforhold.setOpphoersdato(finnKorrektOpphørsdato(andel));
            arbeidsforhold.setArbeidsforholdId(bga.getArbeidsforholdRef().getReferanse());
            arbeidsforhold.setRefusjonPrAar(ModellTyperMapper.beløpTilDto(bga.getGjeldendeRefusjonPrÅr()));
            arbeidsforhold.setNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().map(ModellTyperMapper::beløpTilDto).orElse(null));
            arbeidsforhold.setNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().map(ModellTyperMapper::beløpTilDto).orElse(null));
            inntektsmelding.ifPresent(im -> arbeidsforhold.setBelopFraInntektsmeldingPrMnd(ModellTyperMapper.beløpTilDto(im.getInntektBeløp())));
            mapArbeidsgiver(arbeidsforhold, arbeidsgiver);
            finnEksternArbeidsforholdId(andel, inntektArbeidYtelseGrunnlag).ifPresent(ref -> arbeidsforhold.setEksternArbeidsforholdId(ref.getReferanse()));
        });
    }

    private static Optional<EksternArbeidsforholdRef> finnEksternArbeidsforholdId(BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        Optional<Arbeidsgiver> agOpt = andel.getArbeidsgiver();
        Optional<InternArbeidsforholdRefDto> refOpt = andel.getArbeidsforholdRef();
        if (agOpt.isEmpty() || refOpt.isEmpty()) {
            return Optional.empty();
        }
        return iayGrunnlag.getArbeidsforholdInformasjon()
                .map(d -> d.finnEkstern(agOpt.get(), refOpt.get()));
    }

    private static void mapArbeidsgiver(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold, Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver != null) {
            arbeidsforhold.setArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
            if (OrgNummer.erKunstig(arbeidsgiver.getOrgnr())) {
                arbeidsforhold.setOrganisasjonstype(Organisasjonstype.KUNSTIG);
            }
        }
    }

    private static LocalDate finnKorrektOpphørsdato(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold()
                .flatMap(BGAndelArbeidsforholdDto::getArbeidsperiodeTom)
                .filter(tom -> !TIDENES_ENDE.equals(tom))
                .orElse(null);
    }

}
