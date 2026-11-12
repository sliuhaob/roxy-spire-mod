import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.expr.ExprEditor;

/**
 * Test-only stand-in for ModTheSpire's patcher.
 *
 * It applies the mod's instrument patches and adds its SpireFields to copies of the game
 * classes, using ModTheSpire's own javassist. The mechanics test loads those copies ahead of
 * the game jar, so it runs patched vanilla code, and a replacement string that does not
 * compile fails here instead of at game launch. Patch kinds this does not emulate fail loudly,
 * so a new patch is never silently left untested.
 */
public class PatchEmulator {
    private static final Set<String> PATCH_METHOD_NAMES =
        new HashSet<>(Arrays.asList("Prefix", "Postfix", "Insert", "Replace", "Raw"));

    public static String fieldName(Field spireField) {
        return "roxyTest_" + spireField.getDeclaringClass().getName().replaceAll("[^A-Za-z0-9]", "_")
            + "_" + spireField.getName();
    }

    public static List<Class<?>> patchClasses(File classesDir) throws Exception {
        Path root = classesDir.toPath();
        List<String> names;
        try (Stream<Path> files = Files.walk(root.resolve("roxymod"))) {
            names = files.map(root::relativize).map(Path::toString)
                .filter(name -> name.endsWith(".class"))
                .map(name -> name.substring(0, name.length() - 6).replace(File.separatorChar, '.'))
                .sorted().collect(Collectors.toList());
        }
        List<Class<?>> patches = new ArrayList<>();
        for (String name : names) {
            Class<?> type = Class.forName(name, false, PatchEmulator.class.getClassLoader());
            for (Annotation annotation : type.getDeclaredAnnotations()) {
                String kind = annotation.annotationType().getSimpleName();
                if (kind.startsWith("SpirePatch") && !kind.equals("SpirePatch")) {
                    throw unsupported(type, "@" + kind);
                }
            }
            if (type.isAnnotationPresent(SpirePatch.class)) {
                patches.add(type);
            }
        }
        return patches;
    }

    /** Points every SpireField at the field this tool added to its target class. */
    public static int initializeFields(File classesDir) throws Exception {
        int count = 0;
        for (Class<?> patch : patchClasses(classesDir)) {
            SpirePatch info = patch.getAnnotation(SpirePatch.class);
            if (!SpirePatch.CLASS.equals(info.method())) {
                continue;
            }
            for (Field field : spireFields(patch)) {
                ((SpireField<?>) field.get(null)).initialize(info.clz(), fieldName(field));
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) throws Exception {
        File classesDir = new File(args[0]);
        File outDir = new File(args[1]);
        ClassPool pool = new ClassPool(true);
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            pool.appendClassPath(entry);
        }

        Map<String, CtClass> targets = new TreeMap<>();
        int instruments = 0;
        int fields = 0;
        for (Class<?> patch : patchClasses(classesDir)) {
            SpirePatch info = patch.getAnnotation(SpirePatch.class);
            if (info.clz() == void.class) {
                throw unsupported(patch, "a target named by string (cls)");
            }
            boolean defaultParams = info.paramtypez().length == 1 && info.paramtypez()[0] == void.class
                && info.paramtypes().length == 1 && info.paramtypes()[0].equals("DEFAULT");
            if (!defaultParams) {
                throw unsupported(patch, "paramtypez/paramtypes");
            }
            String targetName = info.clz().getName();
            if (!targets.containsKey(targetName)) {
                targets.put(targetName, pool.get(targetName));
            }
            CtClass target = targets.get(targetName);

            if (SpirePatch.CLASS.equals(info.method())) {
                for (Field field : spireFields(patch)) {
                    CtField added = new CtField(pool.get("java.lang.Object"), fieldName(field), target);
                    added.setModifiers(javassist.Modifier.PUBLIC);
                    target.addField(added);
                    fields++;
                }
                continue;
            }
            if (SpirePatch.CONSTRUCTOR.equals(info.method()) || SpirePatch.STATICINITIALIZER.equals(info.method())) {
                throw unsupported(patch, "a constructor or static initializer target");
            }

            CtBehavior[] behaviors = target.getDeclaredMethods(info.method());
            if (behaviors.length == 0) {
                throw new IllegalStateException(patch.getName() + ": " + targetName + " has no method " + info.method());
            }
            for (Method method : patch.getDeclaredMethods()) {
                boolean instrument = method.isAnnotationPresent(SpireInstrumentPatch.class) || method.getName().equals("Instrument");
                if (instrument) {
                    for (CtBehavior behavior : behaviors) {
                        behavior.instrument((ExprEditor) method.invoke(null));
                    }
                    instruments++;
                    continue;
                }
                if (PATCH_METHOD_NAMES.contains(method.getName())) {
                    throw unsupported(patch, "a " + method.getName() + " method");
                }
                for (Annotation annotation : method.getDeclaredAnnotations()) {
                    String kind = annotation.annotationType().getSimpleName();
                    if (kind.startsWith("Spire") && kind.endsWith("Patch")) {
                        throw unsupported(patch, "@" + kind);
                    }
                }
            }
        }

        for (CtClass target : targets.values()) {
            target.writeFile(outDir.getPath());
        }
        System.out.println("Emulated " + instruments + " instrument patches and " + fields
            + " SpireFields on " + targets.keySet());
    }

    private static List<Field> spireFields(Class<?> patch) {
        List<Field> result = new ArrayList<>();
        for (Field field : patch.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && SpireField.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                result.add(field);
            }
        }
        result.sort(Comparator.comparing(Field::getName));
        return result;
    }

    private static IllegalStateException unsupported(Class<?> patch, String what) {
        return new IllegalStateException(patch.getName() + " uses " + what
            + ", which tools/tests/PatchEmulator.java does not emulate yet. Extend it before relying on -Verify.");
    }
}
