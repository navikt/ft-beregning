//package no.nav.folketrygdloven.beregningsgrunnlag.kontrollerfakta.utledere;
//
//
//import no.nav.folketrygdloven.beregningsgrunnlag.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
//import no.nav.folketrygdloven.beregningsgrunnlag.gradering.AktivitetGradering;
//import no.nav.folketrygdloven.beregningsgrunnlag.input.BeregningsgrunnlagInput;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.behandling.BehandlingReferanse;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.behandling.FagsakYtelseType;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.behandling.RelasjonsRolleType;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.beregningsgrunnlag.BeregningAktivitetAggregat;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.beregningsgrunnlag.Beregningsgrunnlag;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlag;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
//import no.nav.folketrygdloven.beregningsgrunnlag.modell.opptjening.OpptjeningAktivitetType;
//import no.nav.folketrygdloven.beregningsgrunnlag.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
//import no.nav.folketrygdloven.beregningsgrunnlag.KLASSER_MED_AVHENGIGHETER.fp.VurderBesteberegningTilfelleUtleder;
//
//import no.nav.vedtak.felles.jpa.tid.AbstractLocalDateInterval;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//public class VurderBesteberegningTilfelleUtlederTest {
//
//    public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, 1, 1);
//    private VurderBesteberegningTilfelleUtleder vurderBesteberegningTilfelleUtleder;
//    private BehandlingReferanse behandlingReferanse = mock(BehandlingReferanse.class);
//
//    @BeforeEach
//    public void setUp() {
//        when(behandlingReferanse.getFagsakYtelseType()).thenReturn(FagsakYtelseType.FORELDREPENGER);
//        vurderBesteberegningTilfelleUtleder = new VurderBesteberegningTilfelleUtleder();
//    }
//
//    @Test
//    public void skal_returnere_empty_om_bg_har_status_dagpenger() {
//        // Arrange
//        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
//            .medAktivitetStatus(AktivitetStatus.DAGPENGER))
//            .medSkjæringstidspunkt(LocalDate.of(2019, 1, 1))
//            .build();
//
//        BeregningAktivitetAggregat beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT,Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10),
//            AbstractLocalDateInterval.TIDENES_ENDE), OpptjeningAktivitetType.DAGPENGER);
//
//        // Act
//        Optional<FaktaOmBeregningTilfelle> tilfelleOpt = act(lagInput(), bg, beregningAktivitetAggregat);
//
//        // Assert
//        assertThat(tilfelleOpt).isNotPresent();
//    }
//
//    @Test
//    public void skal_returnere_empty_om_bg_har_status_kun_ytelse() {
//        // Arrange
//        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
//            .medAktivitetStatus(AktivitetStatus.KUN_YTELSE))
//            .medSkjæringstidspunkt(LocalDate.of(2019, 1, 1))
//            .build();
//        BeregningAktivitetAggregat beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT,Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10),
//            AbstractLocalDateInterval.TIDENES_ENDE), OpptjeningAktivitetType.SYKEPENGER);
//
//        // Act
//        Optional<FaktaOmBeregningTilfelle> tilfelleOpt = act(lagInput(), bg, beregningAktivitetAggregat);
//
//        // Assert
//        assertThat(tilfelleOpt).isNotPresent();
//    }
//
//    @Test
//    public void skal_returnere_empty_om_søker_er_far() {
//        // Arrange
//        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
//            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
//            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
//            .build();
//
//        when(behandlingReferanse.getRelasjonsRolleType()).thenReturn(RelasjonsRolleType.FARA);
//
//        BeregningAktivitetAggregat beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT,
//            Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10),
//            AbstractLocalDateInterval.TIDENES_ENDE), OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetType.DAGPENGER);
//
//        // Act
//        Optional<FaktaOmBeregningTilfelle> tilfelleOpt = act(lagInput(), bg, beregningAktivitetAggregat);
//
//        // Assert
//        assertThat(tilfelleOpt).isNotPresent();
//    }
//
//    @Test
//    public void skal_returnere_empty_om_gjelder_adopsjon() {
//        // Arrange
//        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
//            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
//            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
//            .build();
//
//        when(behandlingReferanse.getRelasjonsRolleType()).thenReturn(RelasjonsRolleType.MORA);
//        BeregningAktivitetAggregat beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT,Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10),
//            AbstractLocalDateInterval.TIDENES_ENDE), OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetType.DAGPENGER);
//
//        // Act
//        Optional<FaktaOmBeregningTilfelle> tilfelleOpt = act(lagInput(FamilieHendelseType.ADOPSJON), bg, beregningAktivitetAggregat);
//
//        // Assert
//        assertThat(tilfelleOpt).isNotPresent();
//    }
//
//    @Test
//    public void skal_returnere_empty_om_gjelder_ikkje_dagpenger_i_opptjeningsaktiviteter() {
//        // Arrange
//        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
//            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
//            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
//            .build();
//
//        when(behandlingReferanse.getRelasjonsRolleType()).thenReturn(RelasjonsRolleType.MORA);
//
//        BeregningAktivitetAggregat beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT,Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10),
//            AbstractLocalDateInterval.TIDENES_ENDE), OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetType.NÆRING);
//
//        // Act
//        Optional<FaktaOmBeregningTilfelle> tilfelleOpt = act(lagInput(), bg, beregningAktivitetAggregat);
//
//        // Assert
//        assertThat(tilfelleOpt).isNotPresent();
//    }
//
//    @Test
//    public void skal_tilfelle_om_dagpenger_i_opptjeningsaktiviteter() {
//        // Arrange
//        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
//            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
//            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
//            .build();
//
//        when(behandlingReferanse.getRelasjonsRolleType()).thenReturn(RelasjonsRolleType.MORA);
//
//        BeregningAktivitetAggregat beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT, Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10),
//            AbstractLocalDateInterval.TIDENES_ENDE), OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetType.DAGPENGER);
//
//        // Act
//        Optional<FaktaOmBeregningTilfelle> tilfelleOpt = act(lagInput(), bg, beregningAktivitetAggregat);
//
//        // Assert
//        assertThat(tilfelleOpt).hasValue(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING);
//    }
//
//    @Test
//    public void skal_returnere_empty_om_behandling_er_for_svangerskapspenger() {
//        // Arrange
//        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
//            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
//            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
//            .build();
//        when(behandlingReferanse.getFagsakYtelseType()).thenReturn(FagsakYtelseType.SVANGERSKAPSPENGER);
//
//        // Act
//        Optional<FaktaOmBeregningTilfelle> tilfelleOpt = act(lagInput(), bg, null);
//
//        // Assert
//        assertThat(tilfelleOpt).isNotPresent();
//    }
//
//
//    private BeregningsgrunnlagInput lagInput(boolean skalVurdereBesteberegning) {
//        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(100, skalVurdereBesteberegning);
//        return new BeregningsgrunnlagInput(behandlingReferanse, null, null, AktivitetGradering.INGEN_GRADERING, foreldrepengerGrunnlag);
//    }
//
//    private BeregningsgrunnlagInput lagInput() {
//        return lagInput(true);
//    }
//
//    private Optional<FaktaOmBeregningTilfelle> act(BeregningsgrunnlagInput input, Beregningsgrunnlag bg, BeregningAktivitetAggregat beregningAktivitetAggregat) {
//        BeregningsgrunnlagGrunnlag grunnlag = BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
//            .medRegisterAktiviteter(beregningAktivitetAggregat)
//            .medBeregningsgrunnlag(bg)
//            .build(behandlingReferanse.getId(), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
//
//        return vurderBesteberegningTilfelleUtleder.utled(input, grunnlag);
//    }
//}
