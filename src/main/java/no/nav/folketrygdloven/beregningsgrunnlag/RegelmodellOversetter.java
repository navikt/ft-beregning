package no.nav.folketrygdloven.beregningsgrunnlag;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.evaluation.summary.EvaluationVersion;

public class RegelmodellOversetter {

	private static final EvaluationVersion VERSJON = hentVersjonFraPomProperties("META-INF/maven/no.nav.folketrygdloven/beregningsgrunnlag-regelmodell/pom.properties");
	private static final RegelmodellOversetterImpl OVERSETTER = new RegelmodellOversetterImpl(VERSJON);

	private RegelmodellOversetter() {
	}

	public static RegelResultat getRegelResultat(Evaluation evaluation, String regelInput) {
		return OVERSETTER.getRegelResultat(evaluation, regelInput);
	}

	public static String getSporing(Evaluation evaluation) {
		return OVERSETTER.getSporing(evaluation);
	}

	private static EvaluationVersion hentVersjonFraPomProperties(String path) {
		try (InputStream fil = EvaluationSerializer.class.getClassLoader().getResourceAsStream(path)) {
			Properties properties = new Properties();
			properties.load(fil);
			String versjon = properties.getProperty("version");
			String hva = properties.getProperty("groupId") + ":" + properties.getProperty("artifactId");
			return new EvaluationVersion(hva, versjon);
		} catch (IOException e) {
			throw new IllegalStateException("Klarte ikke finne versjon fra " + path, e);
		}
	}


}
