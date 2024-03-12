package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class FrisinnPeriode  {
    private Intervall periode;
    private boolean søkerFrilans;
    private boolean søkerNæring;

    public FrisinnPeriode(Intervall periode, boolean søkerFrilans, boolean søkerNæring) {
        this.periode = periode;
        this.søkerFrilans = søkerFrilans;
        this.søkerNæring = søkerNæring;
    }

    public Intervall getPeriode() {
        return periode;
    }

    public boolean getSøkerFrilans() {
        return søkerFrilans;
    }

    public boolean getSøkerNæring() {
        return søkerNæring;
    }
}
