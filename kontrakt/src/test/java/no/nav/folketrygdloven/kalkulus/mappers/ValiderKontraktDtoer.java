package no.nav.folketrygdloven.kalkulus.mappers;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import no.nav.folketrygdloven.kalkulus.opptjening.v1.Fritekst;

public class ValiderKontraktDtoer {


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
                validerRiktigAnnotert(field);
            }
        }
		return true;
    }

    private static void validerRiktigAnnotert(Field field) {
        var alternativer = getVurderingsalternativer(field);
        if (alternativer == null) {
            alternativer = List.of(List.of(Valid.class));
        }
        for (var alternativ : alternativer) {
            var harAlleAnnoteringerForAlternativet = true;
            for (var annotering : alternativ) {
                if (field.getAnnotation(annotering) == null) {
                    harAlleAnnoteringerForAlternativet = false;
                }
            }
            if (harAlleAnnoteringerForAlternativet) {
                return;
            }
        }
        throw new IllegalArgumentException("Feltet " + field + " har ikke påkrevde annoteringer: " + alternativer);
    }

    private static List<List<Class<? extends Annotation>>> getVurderingsalternativer(Field field) {
        var type = field.getType();
        if (field.getType().isEnum()) {
            return List.of(List.of(Valid.class));
        } else if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            return List.of(List.of(Valid.class, Size.class));
        }
        return VALIDERINGSALTERNATIVER.get(type);
    }

    private static Set<Field> getRelevantFields(Class<?> klasse) {
        Set<Field> fields = new LinkedHashSet<>();
        while (!klasse.isPrimitive() && !klasse.getName().startsWith("java") && klasse.getName().endsWith("Dto")) {
            fields.addAll(fjernStaticFields(List.of(klasse.getDeclaredFields())));
            klasse = klasse.getSuperclass();
        }
        return fields;
    }

    private static Collection<Field> fjernStaticFields(List<Field> fields) {
        return fields.stream().filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList());
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

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        var files = directory.listFiles();
        for (var file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                var classes1 = findClasses(file, packageName + "." + file.getName());
                for (var aClass : classes1) {
                    if (aClass.getName().endsWith("Dto")) {
                        classes.add(aClass);
                    }
                }
            } else if (file.getName().endsWith(".class")) {
                var clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (clazz.getName().endsWith("Dto")) {
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }
}
