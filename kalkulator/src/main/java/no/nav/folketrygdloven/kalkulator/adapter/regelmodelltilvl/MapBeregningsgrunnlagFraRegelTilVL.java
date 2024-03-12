package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;


import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapAktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapInntektskategoriRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapOpptjeningAktivitetFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapPeriodeÅrsakFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

public class MapBeregningsgrunnlagFraRegelTilVL {

    public BeregningsgrunnlagDto mapForeslåBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatGrunnlag,
                                                              BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        return map(resultatGrunnlag, eksisterendeVLGrunnlag);
    }

    private BeregningsgrunnlagDto map(Beregningsgrunnlag resultatGrunnlag, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        var builder = BeregningsgrunnlagDto.builder(eksisterendeVLGrunnlag);
        var sgPrStatus = resultatGrunnlag.getSammenligningsgrunnlagPrStatus().stream()
                .map(this::mapSammenligningsgrunnlagPrStatus)
                // Har vi allerede et sammenligningsgrunnlag av denne typen trenger vi ikke flere
                .filter(sg -> eksisterendeVLGrunnlag.getSammenligningsgrunnlagForStatus(sg.getSammenligningsgrunnlagType()).isEmpty())
                .toList();
        sgPrStatus.forEach(builder::leggTilSammenligningsgrunnlag);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = builder.build();

        Objects.requireNonNull(resultatGrunnlag, "resultatGrunnlag");
        MapAktivitetStatusMedHjemmel.mapAktivitetStatusMedHjemmel(resultatGrunnlag.getAktivitetStatuser(), nyttBeregningsgrunnlag, resultatGrunnlag.getBeregningsgrunnlagPerioder().getFirst());

        mapPerioder(nyttBeregningsgrunnlag, resultatGrunnlag.getBeregningsgrunnlagPerioder());

        return nyttBeregningsgrunnlag;
    }

    private SammenligningsgrunnlagPrStatusDto mapSammenligningsgrunnlagPrStatus(SammenligningsGrunnlag sgRegel) {
        return SammenligningsgrunnlagPrStatusDto.builder()
                .medSammenligningsgrunnlagType(mapSammenligningstype(sgRegel.getSammenligningstype()))
                .medSammenligningsperiode(sgRegel.getSammenligningsperiode().getFom(), sgRegel.getSammenligningsperiode().getTom())
                .medRapportertPrÅr(Beløp.fra(sgRegel.getRapportertPrÅr()))
                .medAvvikPromilleNy(sgRegel.getAvvikPromilleUtenAvrunding())
                .build();
    }

    private SammenligningsgrunnlagType mapSammenligningstype(SammenligningGrunnlagType sammenligningstype) {
        return switch (sammenligningstype) {
            case AT_FL -> SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL;
            case SN -> SammenligningsgrunnlagType.SAMMENLIGNING_SN;
            case MIDLERTIDIG_INAKTIV -> SammenligningsgrunnlagType.SAMMENLIGNING_MIDL_INAKTIV;
        };
    }

    private void mapPerioder(BeregningsgrunnlagDto eksisterendeVLGrunnlag,
                             List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder) {

        int vlBGnummer = 0;
        for (var resultatBGPeriode : beregningsgrunnlagPerioder) {

            BeregningsgrunnlagPeriodeDto eksisterendePeriode = (vlBGnummer < eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().size())
                    ? eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().get(vlBGnummer)
                    : null;
            BeregningsgrunnlagPeriodeDto mappetPeriode = mapBeregningsgrunnlagPeriode(resultatBGPeriode, eksisterendePeriode, eksisterendeVLGrunnlag);
            for (BeregningsgrunnlagPrStatus regelAndel : resultatBGPeriode.getBeregningsgrunnlagPrStatus()) {
                if (regelAndel.getAndelNr() == null) {
                    mapAndelMedArbeidsforhold(mappetPeriode, regelAndel);
                } else {
                    mapAndel(mappetPeriode, regelAndel);
                }
            }
            vlBGnummer++;
            fastsettAgreggerteVerdier(mappetPeriode, eksisterendeVLGrunnlag);
        }
    }

    private static void mapAndel(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel) {
        mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> regelAndel.getAndelNr().equals(bgpsa.getAndelsnr()))
                .forEach(resultatAndel -> mapBeregningsgrunnlagPrStatus(mappetPeriode, regelAndel, resultatAndel));
    }

    private void mapAndelMedArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel) {
        for (BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold : regelAndel.getArbeidsforhold()) {
            mapEksisterendeAndelForArbeidsforhold(mappetPeriode, regelAndel, regelAndelForArbeidsforhold);
        }
    }

    private void mapEksisterendeAndelForArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel, BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelOpt = mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> regelAndelForArbeidsforhold.getAndelNr().equals(bgpsa.getAndelsnr()))
                .findFirst();
        if (andelOpt.isPresent()) {
            BeregningsgrunnlagPrStatusOgAndelDto resultatAndel = andelOpt.get();
            mapBeregningsgrunnlagPrStatusForATKombinert(mappetPeriode, regelAndel, resultatAndel);
        }
    }

    private static void fastsettAgreggerteVerdier(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        var bruttoPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder);
        var avkortetPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getAvkortetPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder);
        var redusertPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getRedusertPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder);
        BeregningsgrunnlagPeriodeDto.oppdater(periode)
                .medBruttoPrÅr(bruttoPrÅr.orElse(null))
                .medAvkortetPrÅr(avkortetPrÅr.orElse(null))
                .medRedusertPrÅr(redusertPrÅr.orElse(null))
                .build(eksisterendeVLGrunnlag);
    }

    private void mapBeregningsgrunnlagPrStatusForATKombinert(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                                    BeregningsgrunnlagPrStatus resultatBGPStatus,
                                                                    BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel) {
        for (BeregningsgrunnlagPrArbeidsforhold arbeidsforhold : resultatBGPStatus.getArbeidsforhold()) {
            if (gjelderSammeAndel(vlBGPAndel, arbeidsforhold)) {
                BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(vlBGPAndel));
                andelBuilder = settFasteVerdier(andelBuilder, arbeidsforhold, Optional.of(vlBGPAndel));
                if (skalByggeBGArbeidsforhold(arbeidsforhold, vlBGPAndel)) {
                    BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforhold = mapArbeidsforhold(vlBGPAndel, arbeidsforhold);
                    andelBuilder.medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
                }
                andelBuilder
                        .build(vlBGPeriode);
                return;
            }
        }
    }

    private BGAndelArbeidsforholdDto.Builder mapArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel,
                                                                      BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return BGAndelArbeidsforholdDto.Builder.oppdater(vlBGPAndel.getBgAndelArbeidsforhold())
                .medNaturalytelseBortfaltPrÅr(Beløp.fra(arbeidsforhold.getNaturalytelseBortfaltPrÅr().orElse(null)))
                .medNaturalytelseTilkommetPrÅr(Beløp.fra(arbeidsforhold.getNaturalytelseTilkommetPrÅr().orElse(null)));
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder settFasteVerdier(BeregningsgrunnlagPrStatusOgAndelDto.Builder builder,
                                                                                   BeregningsgrunnlagPrArbeidsforhold arbeidsforhold,
                                                                                   Optional<BeregningsgrunnlagPrStatusOgAndelDto> eksisterendeAndel) {
        if (arbeidsforhold.getBeregningsperiode() != null && arbeidsforhold.getBeregningsperiode().getFom() != null) {
            builder.medBeregningsperiode(arbeidsforhold.getBeregningsperiode().getFom(), arbeidsforhold.getBeregningsperiode().getTom());
        }
        builder
                .medBeregnetPrÅr(verifisertBeløp(arbeidsforhold.getBeregnetPrÅr()))
                .medOverstyrtPrÅr(verifisertBeløp(arbeidsforhold.getOverstyrtPrÅr()))
                .medFordeltPrÅr(verifisertBeløp(arbeidsforhold.getFordeltPrÅr()))
                .medAvkortetPrÅr(verifisertBeløp(arbeidsforhold.getAvkortetPrÅr()))
                .medRedusertPrÅr(verifisertBeløp(arbeidsforhold.getRedusertPrÅr()))
                .medMaksimalRefusjonPrÅr(Beløp.fra(arbeidsforhold.getMaksimalRefusjonPrÅr()))
                .medAvkortetRefusjonPrÅr(Beløp.fra(arbeidsforhold.getAvkortetRefusjonPrÅr()))
                .medRedusertRefusjonPrÅr(Beløp.fra(arbeidsforhold.getRedusertRefusjonPrÅr()))
                .medAvkortetBrukersAndelPrÅr(verifisertBeløp(arbeidsforhold.getAvkortetBrukersAndelPrÅr()))
                .medRedusertBrukersAndelPrÅr(verifisertBeløp(arbeidsforhold.getRedusertBrukersAndelPrÅr()))
                .medFastsattAvSaksbehandler(arbeidsforhold.getFastsattAvSaksbehandler())
                .medArbforholdType(MapOpptjeningAktivitetFraRegelTilVL.map(arbeidsforhold.getArbeidsforhold().getAktivitet()))
                .medAvkortetFørGraderingPrÅr(verifisertBeløp(arbeidsforhold.getAndelsmessigFørGraderingPrAar()));
        mapInntektskategoriOmEndret(builder, arbeidsforhold, eksisterendeAndel);
        return builder;
    }

    private static void mapInntektskategoriOmEndret(BeregningsgrunnlagPrStatusOgAndelDto.Builder builder, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, Optional<BeregningsgrunnlagPrStatusOgAndelDto> eksisterendeAndel) {
        Inntektskategori inntektskategoriFraRegel = MapInntektskategoriRegelTilVL.map(arbeidsforhold.getInntektskategori());
        if (eksisterendeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getGjeldendeInntektskategori).map(i -> !i.equals(inntektskategoriFraRegel)).orElse(true)) {
            builder.medInntektskategoriAutomatiskFordeling(inntektskategoriFraRegel);
        }
    }

    private static boolean skalByggeBGArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel) {
        return vlBGPAndel.getBgAndelArbeidsforhold().isPresent() &&
                (arbeidsforhold.getNaturalytelseBortfaltPrÅr().isPresent()
                        || arbeidsforhold.getNaturalytelseTilkommetPrÅr().isPresent()
                        || arbeidsforhold.getGjeldendeRefusjonPrÅr().isPresent());
    }

    private static Beløp verifisertBeløp(BigDecimal beløp) {
        return beløp == null ? null : Beløp.fra(beløp.max(BigDecimal.ZERO));
    }

    private static boolean gjelderSammeAndel(BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        if (vlBGPAndel.getAndelsnr() != null && arbeidsforhold.getAndelNr() != null) {
            return vlBGPAndel.getAndelsnr().equals(arbeidsforhold.getAndelNr());
        }
        if (vlBGPAndel.getAktivitetStatus().erFrilanser()) {
            return arbeidsforhold.erFrilanser();
        }
        if (arbeidsforhold.erFrilanser()) {
            return false;
        }
        if (!vlBGPAndel.getGjeldendeInntektskategori().equals(MapInntektskategoriRegelTilVL.map(arbeidsforhold.getInntektskategori()))) {
            return false;
        }
        if (!matcherArbeidsgivere(vlBGPAndel, arbeidsforhold)) {
            return false;
        }
        if (!matcherOpptjeningsaktivitet(vlBGPAndel, arbeidsforhold)) {
            return false;
        }
        return vlBGPAndel.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .filter(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold)
                .map(ref -> Objects.equals(ref, InternArbeidsforholdRefDto.ref(arbeidsforhold.getArbeidsforhold().getArbeidsforholdId())))
                .orElse(arbeidsforhold.getArbeidsforhold().getArbeidsforholdId() == null);
    }

    private static boolean matcherOpptjeningsaktivitet(BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        if (arbeidsforhold.getArbeidsforhold() != null) {
            return Objects.equals(vlBGPAndel.getArbeidsforholdType(), MapOpptjeningAktivitetFraRegelTilVL.map(arbeidsforhold.getArbeidsforhold().getAktivitet()));
        }
        return vlBGPAndel.getArbeidsforholdType() == null;
    }

    private static boolean matcherArbeidsgivere(BeregningsgrunnlagPrStatusOgAndelDto andel, BeregningsgrunnlagPrArbeidsforhold forhold) {
        Arbeidsgiver arbeidsgiver = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver).orElse(null);
        if (forhold.getArbeidsgiverId() == null) {
            return arbeidsgiver == null;
        } else
            return arbeidsgiver != null && Objects.equals(forhold.getArbeidsgiverId(), arbeidsgiver.getIdentifikator());
    }

    private static void mapBeregningsgrunnlagPrStatus(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                      BeregningsgrunnlagPrStatus resultatBGPStatus,
                                                      BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatusOgAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(vlBGPStatusOgAndel));
        if (resultatBGPStatus.getBeregningsperiode() != null && resultatBGPStatus.getBeregningsperiode().getFom() != null) {
            builder.medBeregningsperiode(resultatBGPStatus.getBeregningsperiode().getFom(), resultatBGPStatus.getBeregningsperiode().getTom());
        }

        builder
                .medBeregnetPrÅr(verifisertBeløp(resultatBGPStatus.getBeregnetPrÅr()))
                .medOverstyrtPrÅr(verifisertBeløp(resultatBGPStatus.getOverstyrtPrÅr()))
                .medFordeltPrÅr(verifisertBeløp(resultatBGPStatus.getFordeltPrÅr()))
                .medAvkortetPrÅr(verifisertBeløp(resultatBGPStatus.getAvkortetPrÅr()))
                .medRedusertPrÅr(verifisertBeløp(resultatBGPStatus.getRedusertPrÅr()))
                .medPgi(Beløp.fra(resultatBGPStatus.getGjennomsnittligPGI()), resultatBGPStatus.getPgiListe().stream().map(Beløp::fra).collect(Collectors.toList()))
                .medÅrsbeløpFraTilstøtendeYtelse(Beløp.fra(resultatBGPStatus.getÅrsbeløpFraTilstøtendeYtelse()))
                .medInntektskategori(MapInntektskategoriRegelTilVL.map(resultatBGPStatus.getInntektskategori()))
                .medFastsattAvSaksbehandler(resultatBGPStatus.erFastsattAvSaksbehandler())
                .medBesteberegningPrÅr(Beløp.fra(resultatBGPStatus.getBesteberegningPrÅr()))
                .medOrginalDagsatsFraTilstøtendeYtelse(resultatBGPStatus.getOrginalDagsatsFraTilstøtendeYtelse())
                .build(vlBGPeriode);
    }

    private static BeregningsgrunnlagPeriodeDto mapBeregningsgrunnlagPeriode(final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatGrunnlagPeriode,
                                                                             final BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                                             BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        if (vlBGPeriode == null) {
            BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.ny()
                    .medBeregningsgrunnlagPeriode(
                            resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getFom(),
                            resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getTomOrNull()
                    )
                    .leggTilPeriodeÅrsaker(mapPeriodeÅrsaker(resultatGrunnlagPeriode.getPeriodeÅrsaker()));
            BeregningsgrunnlagPeriodeDto periode = builder
                    .build(eksisterendeVLGrunnlag);
            // Vi kopierer alle andeler fra første periode (med tilhørende andelsnr)
            var førstePeriode = eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().get(0);
            opprettBeregningsgrunnlagPrStatusOgAndel(førstePeriode, periode);
            return periode;
        }
        BeregningsgrunnlagPeriodeDto.Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.oppdater(vlBGPeriode)
                .medBeregningsgrunnlagPeriode(
                        resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getFom(),
                        resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getTomOrNull()
                )
                .fjernPeriodeårsaker()
                .leggTilPeriodeÅrsaker(mapPeriodeÅrsaker(resultatGrunnlagPeriode.getPeriodeÅrsaker()));
        periodeBuilder.build(eksisterendeVLGrunnlag);
        return vlBGPeriode;
    }


    private static void opprettBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPeriodeDto kopierFra, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        kopierFra.getBeregningsgrunnlagPrStatusOgAndelList().forEach(bgpsa -> {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medAndelsnr(bgpsa.getAndelsnr())
                    .medArbforholdType(bgpsa.getArbeidsforholdType())
                    .medAktivitetStatus(bgpsa.getAktivitetStatus())
                    .medInntektskategori(bgpsa.getGjeldendeInntektskategori());
            Optional<Arbeidsgiver> arbeidsgiver = bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver);
            Optional<InternArbeidsforholdRefDto> arbeidsforholdRef = bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef);
            if (arbeidsgiver.isPresent() || arbeidsforholdRef.isPresent()) {
                BGAndelArbeidsforholdDto arbeidsforhold = bgpsa.getBgAndelArbeidsforhold().get();
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforhold = BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(arbeidsgiver.orElse(null))
                        .medArbeidsforholdRef(arbeidsforholdRef.orElse(null))
                        .medArbeidsperiodeFom(arbeidsforhold.getArbeidsperiodeFom())
                        .medArbeidsperiodeTom(arbeidsforhold.getArbeidsperiodeTom().orElse(null));
                andelBuilder
                        .medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
            }
            andelBuilder.build(beregningsgrunnlagPeriode);
        });
    }

    private static List<PeriodeÅrsak> mapPeriodeÅrsaker(List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak> periodeÅrsaker) {
        return periodeÅrsaker.stream()
                .map(MapPeriodeÅrsakFraRegelTilVL::map)
                .collect(Collectors.toList());
    }
}
