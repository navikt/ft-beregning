package no.nav.folketrygdloven.kalkulus.mappers;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ValiderKontraktDtoer {


    @SuppressWarnings("rawtypes")
    private static final Map<Class, List<List<Class<? extends Annotation>>>> VALIDERINGSALTERNATIVER = new HashMap<Class, List<List<Class<? extends Annotation>>>>() {
        {
            put(String.class, asList(
                    asList(Pattern.class, Size.class),
                    asList(Pattern.class),
                    singletonList(Digits.class)));
            put(Long.class, asList(
                    asList(Min.class, Max.class),
                    asList(Digits.class)));
            put(long.class, asList(
                    asList(Min.class, Max.class),
                    asList(Digits.class)));
            put(Integer.class, singletonList(
                    asList(Min.class, Max.class)));
            put(int.class, singletonList(
                    asList(Min.class, Max.class)));
            put(BigDecimal.class, asList(
                    asList(Min.class, Max.class, Digits.class),
                    asList(DecimalMin.class, DecimalMax.class, Digits.class)));
        }
    };

    public static void validerAlleDtoerIKontraken() throws IOException, ClassNotFoundException {
        Class<?>[] classes = getClasses("no.nav.folketrygdloven.kalkulus");
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
    }

    private static void validerRiktigAnnotert(Field field) {
        var alternativer = getVurderingsalternativer(field);
        if (alternativer == null) {
            alternativer = List.of(List.of(Valid.class));
        }
        for (var alternativ : alternativer) {
            boolean harAlleAnnoteringerForAlternativet = true;
            for (Class<? extends Annotation> annotering : alternativ) {
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
        Class<?> type = field.getType();
        if (field.getType().isEnum()) {
            return Collections.singletonList(Collections.singletonList(Valid.class));
        } else if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            return singletonList(List.of(Valid.class, Size.class));
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

    private static Class<?>[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                List<Class<?>> classes1 = findClasses(file, packageName + "." + file.getName());
                for (var aClass : classes1) {
                    if (aClass.getName().endsWith("Dto")) {
                        classes.add(aClass);
                    }
                }
            } else if (file.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (clazz.getName().endsWith("Dto")) {
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }
}
