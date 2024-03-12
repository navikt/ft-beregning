package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad extends MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL {

    private static final Map<AktivitetStatusV2, AktivitetStatus> statusMap = new EnumMap<>(AktivitetStatusV2.class);
    private static final Map<AktivitetStatus, OpptjeningAktivitetType> aktivitetTypeMap = new HashMap<>();

    static {
        statusMap.put(AktivitetStatusV2.SN, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        statusMap.put(AktivitetStatusV2.FL, AktivitetStatus.FRILANSER);
        statusMap.put(AktivitetStatusV2.DP, AktivitetStatus.DAGPENGER);
        aktivitetTypeMap.put(AktivitetStatus.FRILANSER, OpptjeningAktivitetType.FRILANS);
        aktivitetTypeMap.put(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, OpptjeningAktivitetType.NÆRING);
    }

    @Override
    protected void mapAndeler(SplittetPeriode splittetPeriode,
                              List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        andelListe.forEach(eksisterendeAndel -> leggTilEksisterende(beregningsgrunnlagPeriode, eksisterendeAndel));
        try {
            splittetPeriode.getNyeAndeler().forEach(nyAndel -> mapNyAndel(beregningsgrunnlagPeriode, nyAndel));
        } catch (Exception e) {
            var error = String.format("Klarte ikke mappe nye andeler %s. Fullstendig feilmelding: %s", splittetPeriode.getNyeAndeler(), e);
            throw new IllegalStateException(error);
        }
    }

    private void mapNyAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, SplittetAndel nyAndel) {
        // Antar at vi ikkje får nye andeler for ytelse FRISINN
        if (nyAndelErSNFlDP(nyAndel)) {
            AktivitetStatus aktivitetStatus = mapAktivitetStatus(nyAndel.getAktivitetStatus());
            if (aktivitetStatus == null) {
                throw new IllegalStateException("Klarte ikke identifisere aktivitetstatus under periodesplitt. Status var " + nyAndel.getAktivitetStatus());
            }
            boolean eksisterende = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .anyMatch(a -> a.getAktivitetStatus().equals(aktivitetStatus) && a.getArbeidsforholdType().equals(aktivitetTypeMap.get(aktivitetStatus)));
            if (!eksisterende) {
                BeregningsgrunnlagPrStatusOgAndelDto.ny()
                        .medKilde(AndelKilde.PROSESS_PERIODISERING)
                        .medAktivitetStatus(aktivitetStatus)
                        .medArbforholdType(aktivitetTypeMap.get(aktivitetStatus))
                        .build(beregningsgrunnlagPeriode);
            }
        } else {
            Arbeidsgiver arbeidsgiver = MapArbeidsforholdFraRegelTilVL.map(nyAndel.getArbeidsforhold().getReferanseType(), nyAndel.getArbeidsforhold().getOrgnr(), nyAndel.getArbeidsforhold().getAktørId());
            InternArbeidsforholdRefDto iaRef = InternArbeidsforholdRefDto.ref(nyAndel.getArbeidsforhold().getArbeidsforholdId());
            BGAndelArbeidsforholdDto.Builder andelArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(arbeidsgiver)
                    .medArbeidsforholdRef(iaRef)
                    .medArbeidsperiodeFom(nyAndel.getArbeidsperiodeFom())
                    .medArbeidsperiodeTom(nyAndel.getArbeidsperiodeTom());
            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medKilde(AndelKilde.PROSESS_PERIODISERING)
                    .medBGAndelArbeidsforhold(andelArbeidsforholdBuilder)
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                    .build(beregningsgrunnlagPeriode);
        }
    }

    private AktivitetStatus mapAktivitetStatus(AktivitetStatusV2 aktivitetStatusV2) {
        if (aktivitetStatusV2 == null) {
            return null;
        }
        var status = statusMap.get(aktivitetStatusV2);
        if (status == null) {
            throw new IllegalStateException(
                    "Mangler mapping til " + AktivitetStatus.class.getName() + " fra " + AktivitetStatusV2.class.getName() + "." + aktivitetStatusV2);
        }
        return status;
    }

    private boolean nyAndelErSNFlDP(SplittetAndel nyAndel) {
        return nyAndel.getAktivitetStatus() != null
                && (nyAndel.getAktivitetStatus().equals(AktivitetStatusV2.SN)
                || nyAndel.getAktivitetStatus().equals(AktivitetStatusV2.FL)
                || nyAndel.getAktivitetStatus().equals(AktivitetStatusV2.DP));
    }

    private void leggTilEksisterende(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                     BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(eksisterendeAndel);
        andelBuilder.build(beregningsgrunnlagPeriode);
    }

}
