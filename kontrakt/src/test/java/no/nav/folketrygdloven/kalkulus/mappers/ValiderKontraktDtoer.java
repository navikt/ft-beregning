package no.nav.folketrygdloven.kalkulus.mappers;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import no.nav.folketrygdloven.kalkulus.annoteringer.Fritekst;

public class ValiderKontraktDtoer {

    @SuppressWarnings("rawtypes")
    private static final Set<Class> UNNTATT_FRA_VALIDERING = Set.of(boolean.class, Boolean.class, UUID.class, LocalDate.class, LocalDateTime.class);

    @SuppressWarnings("rawtypes")
    private static final Map<Class, List<List<Class<? extends Annotation>>>> VALIDERINGSALTERNATIVER = Map.of(
        String.class, List.of(List.of(Digits.class),
            List.of(Pattern.class, Size.class), List.of(Pattern.class),
            List.of(Fritekst.class, Size.class), List.of(Fritekst.class)),
        Long.class, List.of(List.of(Min.class, Max.class), List.of(Digits.class)),
        long.class, List.of(List.of(Min.class, Max.class), List.of(Digits.class)),
        Integer.class, List.of(List.of(Min.class, Max.class)), int.class, List.of(List.of(Min.class, Max.class)),
        BigDecimal.class, List.of(List.of(Min.class, Max.class, Digits.class),
            List.of(DecimalMin.class, DecimalMax.class, Digits.class))
    );

    public static boolean validerAlleDtoerIKontraken() throws IOException, ClassNotFoundException {
        var classes = getClasses("no.nav.folketrygdloven.kalkulus");
        /* TODO: Fjerne begrensning på Dto i klassenavn + bruk getAllClasses() - som vil traversere ut fra request.
        var k2 = getAllClasses();
        var diff = classes.stream().filter(c -> !k2.contains(c))
            .filter(c -> !c.getPackageName().contains(".response."))
            .filter(c -> !c.getPackageName().contains(".kodeverk"))
            .collect(Collectors.toList());

         */
        for (var aClass : classes) {
            for (var field : getRelevantFields(aClass)) {
                if (field.getAnnotation(JsonIgnore.class) != null) {
                    continue; // feltet blir hverken serialisert elle deserialisert, unntas fra sjekk
                }
                if (field.getType().isEnum()) {
                    continue; // enum er OK
                }
                if (field.getType().isPrimitive()) {
                    continue; // primitiv OK
                }
                if (Collection.class.isAssignableFrom(field.getType()) || Map.class.isAssignableFrom(field.getType())) {
                    // Ikke Valid før Collection/Map - men inne i generics-delen
                    if (field.getAnnotatedType().getAnnotation(Valid.class) != null) {
                        throw new AssertionError("Flytt annotering @Valid inn i List/Set/Collection/Map for feltet " + field + ".");
                    }
                    if (field.getAnnotatedType() instanceof AnnotatedParameterizedType annotatedParameterizedType) {
                        var annotert = annotatedParameterizedType.getAnnotatedActualTypeArguments();
                        for (var ann : annotert) {
                            var kreverAnnoteringer = Optional.ofNullable(getVurderingsalternativer(ann.getType())).orElse(List.of(List.of(Valid.class)));
                            validerRiktigAnnotert(field, ann, kreverAnnoteringer);
                        }
                        continue;
                    }
                    throw new AssertionError("Feltet " + field + " har ikke påkrevde annoteringer.");
                }
                validerRiktigAnnotert(field);
            }
        }
		return true;
    }

    private static void validerRiktigAnnotert(Field field) {
        var alternativer = getVurderingsalternativer(field.getType());
        if (alternativer == null) {
            alternativer = List.of(List.of(Valid.class));
        }
        for (var alternativ : alternativer) {
            if (alternativ.isEmpty() || alternativ.stream().allMatch(field::isAnnotationPresent)) {
                return;
            }
        }
        throw new IllegalArgumentException("Feltet " + field + " har ikke påkrevde annoteringer: " + alternativer);
    }

    private static void validerRiktigAnnotert(Field field, AnnotatedType ann, List<List<Class<? extends Annotation>>> alternativer) {
        for (var alternativ : alternativer) {
            if (alternativ.isEmpty() || alternativ.stream().allMatch(ann::isAnnotationPresent)) {
                return;
            }
        }
        throw new AssertionError("Feltet " + field + " Type " + ann + " har ikke påkrevde annoteringer: " + alternativer);
    }

    private static List<List<Class<? extends Annotation>>> getVurderingsalternativer(Type type) {
        if (type.getClass().isEnum()) {
            return List.of(List.of(Valid.class));
        } else if (Collection.class.isAssignableFrom(type.getClass()) || Map.class.isAssignableFrom(type.getClass())) {
            return List.of(List.of(Size.class));
        }
        if (UNNTATT_FRA_VALIDERING.contains(type)) {
            return List.of(List.of());
        }
        return VALIDERINGSALTERNATIVER.get(type);
    }

    private static Set<Field> getRelevantFields(Class<?> klasse) {
        Set<Field> fields = new LinkedHashSet<>();
        while (!klasse.isPrimitive() && !klasse.getName().startsWith("java") && !klasse.isInterface() && !klasse.getSimpleName().toLowerCase().contains("builder")) {
            fields.addAll(fjernStaticFields(List.of(klasse.getDeclaredFields())));
            klasse = klasse.getSuperclass();
        }
        return fields;
    }

    private static Collection<Field> fjernStaticFields(List<Field> fields) {
        return fields.stream().filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    private static Set<Class<?>> getAllClasses() throws ClassNotFoundException, IOException {
        Set<Class<?>> alleKlasser = new TreeSet<>(Comparator.comparing(Class::getName));
        alleKlasser.addAll(getClasses("no.nav.folketrygdloven.kalkulus.request"));
        var initielleKlasser = new ArrayList<>(alleKlasser);
        for (var klasse : initielleKlasser) {
            traverserAlleFelterForKlasse(klasse, alleKlasser);
            traverserKlasserRekursivt(klasse, alleKlasser);
        }
        return alleKlasser;
    }


    private static void traverserAlleFelterForKlasse(Class<?> klasse, Set<Class<?>> alleKlasser) {
        for (var felt : getRelevantFields(klasse)) {
            if (felt.getType().isEnum()) {
                continue;
            }
            traverserKlasserRekursivt(felt.getType(), alleKlasser);
            if (felt.getGenericType() instanceof ParameterizedType parameterizedType) {
                for (var klazz : genericTypes(parameterizedType)) {
                    traverserAlleFelterForKlasse(klazz, alleKlasser);
                    traverserKlasserRekursivt(klazz, alleKlasser);
                }
            }
        }
    }

    private static void traverserKlasserRekursivt(Class<?> klasse, Set<Class<?>> alleKlasser) {
        if (klasse.getName().startsWith("java") || alleKlasser.contains(klasse)) {
            return;
        }
        alleKlasser.add(klasse);
        traverserAlleFelterForKlasse(klasse, alleKlasser);
        if (klasse.getSuperclass() != null) {
            traverserAlleFelterForKlasse(klasse.getSuperclass(), alleKlasser);
            traverserKlasserRekursivt(klasse.getSuperclass(), alleKlasser);
        }
        if (klasse.isAnnotationPresent(JsonSubTypes.class)) {
            var jsonSubTypes = klasse.getAnnotation(JsonSubTypes.class);
            for (var subtype : jsonSubTypes.value()) {
                traverserAlleFelterForKlasse(subtype.value(), alleKlasser);
                traverserKlasserRekursivt(subtype.value(), alleKlasser);
            }
        }
    }

    private static Set<Class<?>> genericTypes(ParameterizedType parameterizedType) {
        return Arrays.stream(parameterizedType.getActualTypeArguments()).map(a -> (Class<?>) a).collect(Collectors.toSet());
    }

    private static List<Class<?>> getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        var classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        var path = packageName.replace('.', '/');
        var resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            var resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        var classes = new ArrayList<Class<?>>();
        for (var directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException, IOException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        var files = directory.listFiles();
        for (var file : files) {
            if (file.isDirectory()) {
                if (!file.getCanonicalPath().contains("/classes/") && !file.getCanonicalPath().contains("/generated-sources/")) {
                    continue;
                }
                assert !file.getName().contains(".");
                var classes1 = findClasses(file, packageName + "." + file.getName());
                for (var aClass : classes1) {
                    if (aClass.getName().endsWith("Dto")) { // TODO: Fjerne denne!!!!
                        classes.add(aClass);
                    }
                }
            } else if (file.getName().endsWith(".class")) {
                var clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (clazz.getName().endsWith("Dto")) { // TODO: Fjerne denne!!!!
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }
}
