package no.nav.folketrygdloven.skjæringstidspunkt.status.fp;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.skjæringstidspunkt.status.FastsettKombinasjoner;
import no.nav.folketrygdloven.skjæringstidspunkt.status.FastsettStatusOgAndelPrPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettStatusOgAndelPrPeriode.ID)
public class FastsettStatusOgAndelPrPeriodeFP extends FastsettStatusOgAndelPrPeriode {

    public FastsettStatusOgAndelPrPeriodeFP() {
        super();
    }

    @Override
    protected LocalDate finnDatogrenseForInkluderteAktiviteter(LocalDate skjæringstidspunkt) {
        return skjæringstidspunkt.minusDays(1);
    }

}
