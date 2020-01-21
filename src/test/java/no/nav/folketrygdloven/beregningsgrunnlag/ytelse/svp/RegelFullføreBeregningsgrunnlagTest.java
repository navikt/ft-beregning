package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RegelFullføreBeregningsgrunnlagTest {
    public static final String ORGNR = "910";
    private static final String ORGNR_2 = "974760673";
    private static final String ORGNR_3 = "976967631";

    @Test
    public void to_arbeidsforhold_hel_og_halv_utbetaling_kun_penger_til_refusjon() {
        //Arrange
        double bruttoEn = 624_000;
        double refusjonEn = 600_000;

        double bruttoTo = 576_000;
        double refusjonTo = 480_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(99_858));

        leggTilArbeidsforhold(periode, 1L, ORGNR, bruttoEn, refusjonEn, 50);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, bruttoTo, refusjonTo, 100);

        //Act
        kjørRegelFinnGrenseverdi(periode);
        kjørRegelFullførBeregningsgrunnlag(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(443369.52));
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();

        verifiserArbfor(arbeidsforhold, ORGNR, 0, 887);
        verifiserArbfor(arbeidsforhold, ORGNR_2, 0, 819);
    }

    @Test
    public void to_arbeidsforhold_hel_og_halv_utbetaling_penger_til_bruker_og_refusjon() {
        //Arrange
        double bruttoEn = 624_000;
        double refusjonEn = 300_000;

        double bruttoTo = 576_000;
        double refusjonTo = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(99_858));

        leggTilArbeidsforhold(periode, 1L, ORGNR, bruttoEn, refusjonEn, 50);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, bruttoTo, refusjonTo, 100);

        //Act
        kjørRegelFinnGrenseverdi(periode);
        kjørRegelFullførBeregningsgrunnlag(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(443369.52));
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();

        verifiserArbfor(arbeidsforhold, ORGNR, 22, 577);
        verifiserArbfor(arbeidsforhold, ORGNR_2, 337, 769);
    }

    @Test
    public void tre_arbeidsforhold_halv_og_halv_og_ingen_utbetaling_penger_til_bruker_og_refusjon() {
        //Arrange
        double bruttoEn = 600_000;
        double refusjonEn = 560_000.04;

        double bruttoTo = 750_000;
        double refusjonTo = 333_333.36;

        double bruttoTre = 250_000;
        double refusjonTre = 0;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(99_858));

        leggTilArbeidsforhold(periode, 1L, ORGNR, bruttoEn, refusjonEn, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, bruttoTo, refusjonTo, 60);
        leggTilArbeidsforhold(periode, 3L, ORGNR_3, bruttoTre, refusjonTre, 0);

        //Act
        kjørRegelFinnGrenseverdi(periode);
        kjørRegelFullførBeregningsgrunnlag(periode);
        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(393190.875));
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();

        verifiserArbfor(arbeidsforhold, ORGNR, 0, 743);
        verifiserArbfor(arbeidsforhold, ORGNR_2, 0, 769);
        verifiserArbfor(arbeidsforhold, ORGNR_3, 0, 0);
    }

    private void verifiserArbfor(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold, String orgnr, int forventetDagsatsBrukersAndel, int forventetDagsatsRefusjon) {
        Optional<BeregningsgrunnlagPrArbeidsforhold> arbforOpt = arbeidsforhold.stream().filter(a -> a.getArbeidsgiverId().equals(orgnr)).findFirst();
        assertThat(arbforOpt).isPresent();
        BeregningsgrunnlagPrArbeidsforhold arbfor = arbforOpt.get();
        assertThat(arbfor.getDagsatsArbeidsgiver()).isEqualTo(forventetDagsatsRefusjon);
        assertThat(arbfor.getDagsatsBruker()).isEqualTo(forventetDagsatsBrukersAndel);
        assertThat(arbfor.getDagsats()).isEqualTo(forventetDagsatsBrukersAndel + forventetDagsatsRefusjon);
    }

    private void leggTilArbeidsforhold(BeregningsgrunnlagPeriode periode,
                                       long andelsnr,
                                       String orgnr,
                                       double beregnetPrÅr,
                                       double refusjonPrÅr,
                                       double utbetalingsgrad) {
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
        BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        if (atfl == null) {
            BeregningsgrunnlagPeriode.builder(periode)
                .medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
                    .builder()
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, refusjonPrÅr, utbetalingsgrad, arbeidsforhold))
                    .build());
        } else {
            BeregningsgrunnlagPrStatus.builder(atfl)
                .medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, refusjonPrÅr, utbetalingsgrad, arbeidsforhold))
                .build();
        }
    }

    private BeregningsgrunnlagPrArbeidsforhold lagBeregningsgrunnlagPrArbeidsforhold(long andelsnr,
                                                                                     double beregnetPrÅr,
                                                                                     double refusjonskrav,
                                                                                     double utbetalingsgrad,
                                                                                     Arbeidsforhold arbeidsforhold) {
        BeregningsgrunnlagPrArbeidsforhold arb = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medAndelNr(andelsnr)
            .medArbeidsforhold(arbeidsforhold)
            .medBeregnetPrÅr(BigDecimal.valueOf(beregnetPrÅr))
            .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonskrav))
            .medUtbetalingsprosentSVP(BigDecimal.valueOf(utbetalingsgrad))
            .build();
        arb.setErSøktYtelseFor(utbetalingsgrad > 0);
        return arb;
    }


    private RegelResultat kjørRegelFinnGrenseverdi(BeregningsgrunnlagPeriode grunnlag) {
        RegelFinnGrenseverdi regel = new RegelFinnGrenseverdi(grunnlag);
        Evaluation evaluation = regel.evaluer(grunnlag);
        return RegelmodellOversetter.getRegelResultat(evaluation, "input");
    }

    private RegelResultat kjørRegelFullførBeregningsgrunnlag(BeregningsgrunnlagPeriode grunnlag) {
        RegelFullføreBeregningsgrunnlag regel = new RegelFullføreBeregningsgrunnlag(grunnlag);
        Evaluation evaluation = regel.evaluer(grunnlag);
        return RegelmodellOversetter.getRegelResultat(evaluation, "input");
    }

}
