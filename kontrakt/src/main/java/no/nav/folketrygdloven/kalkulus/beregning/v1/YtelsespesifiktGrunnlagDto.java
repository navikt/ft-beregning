package no.nav.folketrygdloven.kalkulus.beregning.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.folketrygdloven.kalkulus.kodeverk.KodeKonstanter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "ytelseType", defaultImpl = Void.class)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PleiepengerNærståendeGrunnlag.class, name = KodeKonstanter.YT_PLEIEPENGER_NÆRSTÅENDE),
        @JsonSubTypes.Type(value = PleiepengerSyktBarnGrunnlag.class, name = KodeKonstanter.YT_PLEIEPENGER_SYKT_BARN),
        @JsonSubTypes.Type(value = OmsorgspengerGrunnlag.class, name = KodeKonstanter.YT_OMSORGSPENGER),
        @JsonSubTypes.Type(value = OpplæringspengerGrunnlag.class, name = KodeKonstanter.YT_OPPLÆRINGSPENGER),
        @JsonSubTypes.Type(value = ForeldrepengerGrunnlag.class, name = KodeKonstanter.YT_FORELDREPENGER),
        @JsonSubTypes.Type(value = SvangerskapspengerGrunnlag.class, name = KodeKonstanter.YT_SVANGERSKAPSPENGER),
        @JsonSubTypes.Type(value = FrisinnGrunnlag.class, name = KodeKonstanter.YT_FRISINN),
})
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public abstract class YtelsespesifiktGrunnlagDto {}
