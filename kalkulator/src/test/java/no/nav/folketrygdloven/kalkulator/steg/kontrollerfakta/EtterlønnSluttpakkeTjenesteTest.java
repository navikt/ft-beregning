package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class EtterlønnSluttpakkeTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);

    public EtterlønnSluttpakkeTjenesteTest() {
    }

    @Test
    public void skalGiTilfelleDersomSøkerHarAndelMedEtterlønnSluttpakke() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER, Collections.singletonList(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE));

        //Act
        boolean brukerHarEtterlønnSluttpakke = act(beregningsgrunnlag);

        //Assert
        assertThat(brukerHarEtterlønnSluttpakke).isTrue();
    }

    @Test
    public void skalIkkeGiTilfelleDersomSøkerIkkeHarAndelMedEtterlønnSluttpakke() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER, Collections.singletonList(OpptjeningAktivitetType.ARBEID));

        //Act
        boolean brukerHarEtterlønnSluttpakke = act(beregningsgrunnlag);

        //Assert
        assertThat(brukerHarEtterlønnSluttpakke).isFalse();
    }

    @Test
    public void skalIkkeGiTilfelleDersomSøkerIkkeErArbeidstaker() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Collections.singletonList(OpptjeningAktivitetType.NÆRING));

        //Act
        boolean brukerHarEtterlønnSluttpakke = act(beregningsgrunnlag);

        //Assert
        assertThat(brukerHarEtterlønnSluttpakke).isFalse();
    }

    @Test
    public void skalGiTilfelleDersomSøkerHarAndreAndelerMenOgsåEtterlønnSluttpakke() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER, List.of(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE, OpptjeningAktivitetType.VENTELØNN_VARTPENGER));

        //Act
        boolean brukerHarEtterlønnSluttpakke = act(beregningsgrunnlag);

        //Assert
        assertThat(brukerHarEtterlønnSluttpakke).isTrue();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(AktivitetStatus aktivitetStatus, List<OpptjeningAktivitetType> opptjeningAktivitetTypes) {
        BeregningsgrunnlagAktivitetStatusDto.Builder asb = BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(aktivitetStatus);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(Beløp.fra(93000))
            .leggTilAktivitetStatus(asb)
            .build();
        BeregningsgrunnlagPeriodeDto.Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), null);
        BeregningsgrunnlagPeriodeDto periode = periodeBuilder.build(beregningsgrunnlag);
        for (OpptjeningAktivitetType type : opptjeningAktivitetTypes) {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(aktivitetStatus)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregnetPrÅr(null)
                .medArbforholdType(type);
            if (aktivitetStatus.erArbeidstaker()) {
                builder.medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.fra(AktørId.dummy())));
            }
            builder
                .build(periode);
        }
        return beregningsgrunnlag;
    }

    private boolean act(BeregningsgrunnlagDto beregningsgrunnlag) {
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        return EtterlønnSluttpakkeTjeneste.skalVurdereOmBrukerHarEtterlønnSluttpakke(grunnlag);
    }
}
