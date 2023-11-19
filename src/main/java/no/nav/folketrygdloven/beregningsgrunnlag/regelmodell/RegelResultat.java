package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record RegelResultat(ResultatBeregningType beregningsresultat,
							String versjon,
                            RegelSporing sporing,
							List<RegelMerknad> merknader,
							RegelSporing sporingFinnGrenseverdi) {

    public RegelResultat(ResultatBeregningType beregningsresultat, String regelVersjon, String regelInput, String regelSporing) {
		this(beregningsresultat, regelVersjon, new RegelSporing(regelVersjon, regelInput, regelSporing), List.of(), null);
    }

	public static RegelResultat medRegelMerknad(RegelResultat regelResultat, RegelMerknad regelMerknad) {
		var merknader = new ArrayList<>(regelResultat.merknader());
		merknader.add(regelMerknad);
		return new RegelResultat(regelResultat.beregningsresultat(), regelResultat.versjon(), regelResultat.sporing(),
				merknader, regelResultat.sporingFinnGrenseverdi());
	}

	public static RegelResultat medRegelsporingFinnGrenseverdi(RegelResultat regelResultat, String input, String sporing) {
		return new RegelResultat(regelResultat.beregningsresultat(), regelResultat.versjon(), regelResultat.sporing(),
				regelResultat.merknader(), new RegelSporing(regelResultat.versjon(), input, sporing));
	}


	@Deprecated(forRemoval = true)
	public List<RegelMerknad> getMerknader() {
        return merknader;
    }

	@Deprecated(forRemoval = true)
    public ResultatBeregningType getBeregningsresultat() {
        return beregningsresultat;
    }

	@Deprecated(forRemoval = true)
    public RegelSporing getRegelSporing() {
        return sporing;
    }

	@Deprecated(forRemoval = true)
    public Optional<RegelSporing> getRegelSporingFinnGrenseverdi() {
        return Optional.ofNullable(sporingFinnGrenseverdi);
    }
}
