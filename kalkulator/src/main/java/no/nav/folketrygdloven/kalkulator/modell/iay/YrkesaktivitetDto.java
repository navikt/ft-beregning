package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

public class YrkesaktivitetDto {

    private Set<AktivitetsAvtaleDto> aktivitetsAvtale = new LinkedHashSet<>();
    private Set<PermisjonDto> permisjoner = new LinkedHashSet<>();
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private ArbeidType arbeidType;

    public YrkesaktivitetDto() {
        // hibernate
    }

    public YrkesaktivitetDto(YrkesaktivitetDto yrkesaktivitet) {
        var kopierFra = yrkesaktivitet; // NOSONAR
        this.arbeidType = kopierFra.getArbeidType();
        this.arbeidsgiver = kopierFra.getArbeidsgiver();
        this.arbeidsforholdRef = kopierFra.arbeidsforholdRef;

        // NB må aksessere felt her heller en getter siden getter filtrerer
        this.aktivitetsAvtale = kopierFra.aktivitetsAvtale.stream().map(aa -> {
            var aktivitetsAvtaleEntitet = new AktivitetsAvtaleDto(aa);
            return aktivitetsAvtaleEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));

        this.permisjoner = kopierFra.permisjoner.stream().map(p -> {
            var permisjon = new PermisjonDto(p);
            return permisjon;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Kategorisering av aktivitet som er enten pensjonsgivende inntekt eller likestilt med pensjonsgivende inntekt
     * <p>
     * Fra aa-reg
     * <ul>
     * <li>{@link ArbeidType#ORDINÆRT_ARBEIDSFORHOLD}</li>
     * <li>{@link ArbeidType#MARITIMT_ARBEIDSFORHOLD}</li>
     * <li>{@link ArbeidType#FORENKLET_OPPGJØRSORDNING}</li>
     * <li>{@link ArbeidType#FRILANSER_OPPDRAGSTAKER}</li>
     * </ul>
     * <p>
     * De resterende kommer fra søknaden
     *
     * @return {@link ArbeidType}
     */
    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    void setArbeidType(ArbeidType arbeidType) {
        this.arbeidType = arbeidType;
    }

    /**
     * Unik identifikator for arbeidsforholdet til aktøren i bedriften. Selve nøkkelen er ikke unik, men er unik for arbeidstaker hos arbeidsgiver.
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return {@code ArbeidsforholdRef.ref(null)} hvis ikke tilstede
     */
    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
    }

    void setArbeidsforholdId(InternArbeidsforholdRefDto arbeidsforholdId) {
        this.arbeidsforholdRef = arbeidsforholdId != null && !InternArbeidsforholdRefDto.nullRef().equals(arbeidsforholdId) ? arbeidsforholdId : null;
    }

    /**
     * Identifiser om yrkesaktiviteten gjelder for arbeidsgiver og arbeidsforholdRef.
     *
     * @param arbeidsgiver      en {@link Arbeidsgiver}
     * @param arbeidsforholdRef et {@link InternArbeidsforholdRefDto}
     * @return true hvis arbeidsgiver og arbeidsforholdRef macther
     */
    public boolean gjelderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        boolean gjelderForArbeidsgiver = Objects.equals(getArbeidsgiver(), arbeidsgiver);
        boolean gjelderFor = gjelderForArbeidsgiver && getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
        return gjelderFor;
    }

    public boolean gjelderFor(InntektsmeldingDto im) {
        return gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef());
    }

    public Collection<AktivitetsAvtaleDto> getAlleAktivitetsAvtaler() {
        return Collections.unmodifiableSet(aktivitetsAvtale);
    }

    public Collection<AktivitetsAvtaleDto> getAlleAnsettelsesperioder() {
        return aktivitetsAvtale.stream().filter(AktivitetsAvtaleDto::erAnsettelsesPeriode).collect(Collectors.toUnmodifiableSet());
    }


    void leggTilAktivitetsAvtale(AktivitetsAvtaleDto aktivitetsAvtale) {
        this.aktivitetsAvtale.add(aktivitetsAvtale);
    }

    public Set<PermisjonDto> getPermisjoner() {
        return permisjoner;
    }


    public Set<PermisjonDto> getFullPermisjon() {
        return permisjoner.stream().filter(p -> p.getProsentsats() != null && p.getProsentsats().compareTo(Stillingsprosent.HUNDRED) >= 0).collect(Collectors.toSet());
    }


    void leggTilPermisjon(PermisjonDto permisjon) {
        this.permisjoner.add(permisjon);
    }


    /**
     * Arbeidsgiver
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return {@link Arbeidsgiver}
     */
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    boolean erArbeidsforhold() {
        return ArbeidType.AA_REGISTER_TYPER.contains(arbeidType);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof YrkesaktivitetDto)) {
            return false;
        }
        YrkesaktivitetDto other = (YrkesaktivitetDto) obj;
        return Objects.equals(this.getArbeidsforholdRef(), other.getArbeidsforholdRef()) &&
                Objects.equals(this.getArbeidType(), other.getArbeidType()) &&
                Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getArbeidsforholdRef(), getArbeidType(), getArbeidsgiver());
    }

    @Override
    public String toString() {
        return "YrkesaktivitetDto{" +
                "aktivitetsAvtale=" + aktivitetsAvtale +
                ", permisjoner=" + permisjoner +
                ", arbeidsgiver=" + arbeidsgiver +
                ", arbeidsforholdRef=" + arbeidsforholdRef +
                ", arbeidType=" + arbeidType +
                '}';
    }

}
