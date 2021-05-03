package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Aktivitet {
    AAP_MOTTAKER,
    ARBEIDSTAKERINNTEKT,
    DAGPENGEMOTTAKER,
    ETTERLØNN_SLUTTPAKKE,
    FORELDREPENGER_MOTTAKER,
    FRILANSINNTEKT,
    FRISINN_MOTTAKER,
    MILITÆR_ELLER_SIVILTJENESTE,
    NÆRINGSINNTEKT,
    OMSORGSPENGER,
    OPPLÆRINGSPENGER,
    PLEIEPENGER_MOTTAKER,
    SVANGERSKAPSPENGER_MOTTAKER,
    SYKEPENGER_MOTTAKER,
    VENTELØNN_VARTPENGER,
    VIDERE_ETTERUTDANNING,
    UTDANNINGSPERMISJON,
    UDEFINERT;

    private static final Set<Aktivitet> AKTIVITETER_MED_ORGNR = new HashSet<>(Arrays.asList(ARBEIDSTAKERINNTEKT, FRILANSINNTEKT));

	private static final Set<Aktivitet> YTELSER_I_SAMMENLIGNINGSGRUNNLAGET = new HashSet<>(Arrays.asList(FORELDREPENGER_MOTTAKER,
			OMSORGSPENGER, OPPLÆRINGSPENGER, PLEIEPENGER_MOTTAKER, SVANGERSKAPSPENGER_MOTTAKER, SYKEPENGER_MOTTAKER));

    public boolean harOrgnr() {
        return AKTIVITETER_MED_ORGNR.contains(this);
    }

	public boolean erYtelseFraSammenligningsfilter() {
		return YTELSER_I_SAMMENLIGNINGSGRUNNLAGET.contains(this);
	}

}
