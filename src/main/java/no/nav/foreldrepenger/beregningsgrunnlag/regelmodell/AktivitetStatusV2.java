package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

/**
 * Intern representasjon av aktivitetstatusene som brukes i beregningskomponenten.
 * Pga avhengigheter mellom beregninger av noen aktivitetstatuser (f. eks AAP/DP alltid før SN/ATFL_SN) er det viktig at
 * rekkefølgen de ulike statusene beregenes overholdes. Denne rekkefølgen blir overholdt av 'prioritet'-feltet. Statuser
 * med høyere prioritet-verdi blir beregnet etter statuser med lavere prioritet-verdi.
 */
public enum AktivitetStatusV2 {
    AT("Arbeidstaker", 1, 1),
    FL("Frilanser", 1, 2),
    KUN_YTELSE("Mottaker av tilstøtende ytelse"),
    DP("Dagpenger", 1, 3),
    AAP("Mottaker av arbeidsavklaringspenger", 1, 4),
    MS("Militær/Sivil"),
    SN("Selvstendig næringsdrivende", 2, 5); //Skal ligge sist av AktivitetStatusene

    private final String beskrivelse;
    //Lavere verdi -> høyere prioritet.
    private final int beregningPrioritet;
    private final int avkortingPrioritet;

    AktivitetStatusV2(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        beregningPrioritet = 1;
        avkortingPrioritet = 9;
    }

    AktivitetStatusV2(String beskrivelse, int beregningPrioritet, int avkortingPrioritet) {
        this.beskrivelse = beskrivelse;
        this.beregningPrioritet = beregningPrioritet;
        this.avkortingPrioritet = avkortingPrioritet;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public int getBeregningPrioritet() {
        return beregningPrioritet;
    }

    public int getAvkortingPrioritet(){
        return avkortingPrioritet;
    }
}
