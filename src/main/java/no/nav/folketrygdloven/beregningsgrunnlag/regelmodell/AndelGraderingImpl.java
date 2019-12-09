package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;

public class AndelGraderingImpl implements AndelGradering {
    private AktivitetStatusV2 aktivitetStatus;
    private List<Gradering> graderinger = new ArrayList<>();
    private Arbeidsforhold arbeidsforhold;
    private Long andelsnr;

    private AndelGraderingImpl() {
    }

    @Override
    public AktivitetStatusV2 getAktivitetStatus() {
        return aktivitetStatus;
    }

    @Override
    public List<Gradering> getGraderinger() {
        return graderinger;
    }

    @Override
    public boolean erNyAktivitet() {
        return andelsnr == null;
    }

    @Override
    public List<Refusjonskrav> getGyldigeRefusjonskrav() {
        return Collections.emptyList();
    }

    @Override
    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AndelGraderingImpl kladd;

        private Builder() {
            kladd = new AndelGraderingImpl();
        }

        public Builder medAktivitetStatus(AktivitetStatusV2 aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medGraderinger(List<Gradering> graderinger) {
            kladd.graderinger = graderinger;
            return this;
        }

        public Builder medAndelsnr(Long andelsnr) {
            kladd.andelsnr = andelsnr;
            return this;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public AndelGraderingImpl build() {
            return kladd;
        }
    }
}
