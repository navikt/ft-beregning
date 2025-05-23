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

class EtterlønnSluttpakkeTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);

    @Test
    void skalGiTilfelleDersomSøkerHarAndelMedEtterlønnSluttpakke() {
        //Arrange
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER, Collections.singletonList(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE));

        //Act
        var brukerHarEtterlønnSluttpakke = act(beregningsgrunnlag);

        //Assert
        assertThat(brukerHarEtterlønnSluttpakke).isTrue();
    }

    @Test
    void skalIkkeGiTilfelleDersomSøkerIkkeHarAndelMedEtterlønnSluttpakke() {
        //Arrange
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER, Collections.singletonList(OpptjeningAktivitetType.ARBEID));

        //Act
        var brukerHarEtterlønnSluttpakke = act(beregningsgrunnlag);

        //Assert
        assertThat(brukerHarEtterlønnSluttpakke).isFalse();
    }

    @Test
    void skalIkkeGiTilfelleDersomSøkerIkkeErArbeidstaker() {
        //Arrange
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Collections.singletonList(OpptjeningAktivitetType.NÆRING));

        //Act
        var brukerHarEtterlønnSluttpakke = act(beregningsgrunnlag);

        //Assert
        assertThat(brukerHarEtterlønnSluttpakke).isFalse();
    }

    @Test
    void skalGiTilfelleDersomSøkerHarAndreAndelerMenOgsåEtterlønnSluttpakke() {
        //Arrange
        var beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER, List.of(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE, OpptjeningAktivitetType.VENTELØNN_VARTPENGER));

        //Act
        var brukerHarEtterlønnSluttpakke = act(beregningsgrunnlag);

        //Assert
        assertThat(brukerHarEtterlønnSluttpakke).isTrue();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(AktivitetStatus aktivitetStatus, List<OpptjeningAktivitetType> opptjeningAktivitetTypes) {
        var asb = BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(aktivitetStatus);
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(Beløp.fra(93000))
            .leggTilAktivitetStatus(asb)
            .build();
        var periodeBuilder = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), null);
        var periode = periodeBuilder.build(beregningsgrunnlag);
        for (var type : opptjeningAktivitetTypes) {
            var builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
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
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        return EtterlønnSluttpakkeTjeneste.skalVurdereOmBrukerHarEtterlønnSluttpakke(grunnlag);
    }
}
