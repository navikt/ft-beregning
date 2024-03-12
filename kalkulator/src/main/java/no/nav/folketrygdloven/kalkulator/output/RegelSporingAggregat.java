package no.nav.folketrygdloven.kalkulator.output;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public record RegelSporingAggregat(List<RegelSporingGrunnlag> regelsporingerGrunnlag,
                                   List<RegelSporingPeriode> regelsporingPerioder) {

    public RegelSporingAggregat(List<RegelSporingPeriode> regelsporingPerioder) {
        this(List.of(), regelsporingPerioder);
    }

    public RegelSporingAggregat(RegelSporingGrunnlag... regelsporingerGrunnlag) {
        this(Arrays.asList(regelsporingerGrunnlag), List.of());
    }

    public static RegelSporingAggregat konkatiner(RegelSporingAggregat sporing1, RegelSporingAggregat sporing2) {
        if (sporing1 == null) {
            return sporing2;
        }
        if (sporing2 == null) {
            return sporing1;
        }
        return new RegelSporingAggregat(
                Stream.concat(
                        sporing1.regelsporingerGrunnlag().stream(),
                        sporing2.regelsporingerGrunnlag().stream()).toList(),
                Stream.concat(
                        sporing1.regelsporingPerioder().stream(),
                        sporing2.regelsporingPerioder().stream()).toList()
        );
    }

}
