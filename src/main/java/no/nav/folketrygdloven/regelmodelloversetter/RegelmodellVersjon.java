package no.nav.folketrygdloven.regelmodelloversetter;

import java.util.Properties;

import no.nav.fpsak.nare.evaluation.summary.EvaluationVersion;

public class RegelmodellVersjon {

    private RegelmodellVersjon() {
    }

    public static final EvaluationVersion REGELMODELL_VERSJON = readVersionPropertyFor("beregningsgrunnlag-regelmodell", "beregningsgrunnlag-regelmodell/beregningsgrunnlag-regelmodell-version.properties");

    public static EvaluationVersion readVersionPropertyFor(String projectName, String propertiesFile) {
        String version;
        try {
            final Properties properties = new Properties();
            properties.load(RegelmodellVersjon.class.getClassLoader().getResourceAsStream(propertiesFile));
            version = properties.getProperty("version");
        } catch (Exception e) {
	        throw new IllegalStateException("Klarte ikke finne versjon for " + projectName, e);
        }
        return new EvaluationVersion(projectName, version);
    }

}
