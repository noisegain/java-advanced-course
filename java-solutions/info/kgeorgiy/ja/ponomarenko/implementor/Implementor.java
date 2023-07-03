package info.kgeorgiy.ja.ponomarenko.implementor;

import info.kgeorgiy.ja.ponomarenko.base.Utils;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Class for generating implementation of given class.
 * Can generate implementation of class and jar file with implementation.
 *
 * @author Ponomarenko Ilya
 *
 * @see info.kgeorgiy.java.advanced.implementor.JarImpler
 */
public class Implementor implements JarImpler {
    /**
     * Line separator for generated files.
     */
    private static final String SEP = System.lineSeparator();
    /**
     * Tabulation for generated files.
     */
    private static final String TAB = " ".repeat(4);
    /**
     * File separator for convert file path from package.
     */
    private static final char FILE_SEP = '/';
    public static final String TEMP_DIR = "temp";
    private static final String USAGE = "Implementor <class> <path>";

    /**
     * Default constructor.
     */
    public Implementor() {}

    /**
     * Entry point of {@link Implementor}.
     * Should have 2 args: class and path.
     * If you want to produce Jar, you can pass -jar as first argument and then class and path.
     *
     * @param args implementor arguments.
     */
    public static void main(String[] args) {
        if (!Utils.checkArgs(args, 2, USAGE)) {
            return;
        }
        try {
            int start = 0;
            if (args[0].equals("-jar")) {
                ++start;
            }
            var arg1 = Class.forName(args[start]);
            var arg2 = Path.of(args[start + 1]);
            var implementor = new Implementor();
            if (start == 0) {
                implementor.implement(arg1, arg2);
            } else {
                implementor.implementJar(arg1, arg2);
            }
        } catch (ImplerException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Implements class from given token and write it to given writer.
     *
     * @param token  given token.
     * @param writer given writer.
     * @throws IOException if writer exception happened.
     */
    private static void implementClass(Class<?> token, Writer writer) throws IOException {
        implementHead(token, writer);
        implementMethods(token, writer);
        writer.write("}" + SEP);
    }

    /**
     * Implements class head from given token and write it to given writer.
     *
     * @param token  given token.
     * @param writer given writer.
     * @throws IOException if writer exception happened.
     */
    private static void implementHead(Class<?> token, Writer writer) throws IOException {
        var pcg = token.getPackage();
        if (pcg != null) {
            writer.write("package " + pcg.getName() + ";" + SEP.repeat(2));
        }
        writer.write(String.format("public class %s implements %s {" + SEP, token.getSimpleName() + "Impl", token.getCanonicalName()));
    }

    /**
     * Implements public methods of given token and write it to given writer.
     *
     * @param token  given token.
     * @param writer given writer.
     * @throws IOException if writer exception happened.
     */
    private static void implementMethods(Class<?> token, Writer writer) throws IOException {
        for (Method method : token.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                implementMethod(method, writer);
            }
        }
    }

    /**
     * Implements given method and write it to given writer.
     *
     * @param method given method.
     * @param writer given writer.
     * @throws IOException if writer exception happened.
     */
    private static void implementMethod(Method method, Writer writer) throws IOException {
        writer.write(String.format(
                TAB + "public %s %s(%s) {" + SEP
                        + TAB.repeat(2) + "return %s;" + SEP
                        + TAB + "}" + SEP.repeat(2),
                method.getReturnType().getCanonicalName(),
                method.getName(),
                Arrays.stream(method.getParameters())
                        .map(parameter -> parameter.getType().getCanonicalName() + " " + parameter.getName())
                        .collect(Collectors.joining(", ")),
                getDefaultValue(method.getReturnType())));
    }

    /**
     * Returns default value for primitive types or null.
     *
     * @param token given token.
     * @return default value for given token.
     */
    private static String getDefaultValue(Class<?> token) {
        if (!token.isPrimitive()) {
            return "null";
        }
        if (token.equals(void.class)) {
            return "";
        }
        if (token.equals(boolean.class)) {
            return "false";
        }
        return "0";
    }

    /**
     * Compiles given token to java .class file.
     *
     * @param token     given token to compile.
     * @param path      where to compile token.
     * @param className java filename of token.
     * @throws ImplerException if compilation error or can't get uri of token code source.
     */
    private static void compile(Class<?> token, Path path, String className) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Path classPath;
        try {
            classPath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new ImplerException("URISyntaxException", e);
        }
        if (compiler == null) {
            throw new ImplerException("Can't find compiler");
        }
        int code = compiler.run(null, null, null, "-cp",
                classPath.toString(),
                path.resolve(className) + ".java",
                "-encoding", "UTF-8");
        if (code != 0) {
            throw new ImplerException("Can't compile");
        }
    }

    /**
     * Returns name for implemented token.
     *
     * @param token given token.
     * @return implementation name.
     */
    private String getImplName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Check if given token can be implemented.
     *
     * @param token given token.
     * @throws ImplerException if token is not interface or has private modifier.
     */
    private void assertCanImplement(Class<?> token) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Token should be (public / package private) interface");
        }
    }

    /**
     * Implements token in root directory.
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException if some exception happened.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        assertCanImplement(token);
        var packagePath = token.getPackageName().replace(".", File.separator);
        var pathToJavaFile = Path.of(root.toString(), packagePath, getImplName(token) + ".java");

        try {
            Files.createDirectories(Path.of(root.toString(), packagePath));
        } catch (IOException e) {
            throw new ImplerException("Can't create path to java file", e);
        }

        try (var writer = Files.newBufferedWriter(pathToJavaFile)) {
            implementClass(token, writer);
        } catch (IOException e) {
            throw new ImplerException("Exception while writing to file", e);
        }
    }

    /**
     * Implements token to given .jar file path.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException if some exception happened.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory(jarFile.getParent(), TEMP_DIR);
            implement(token, tmpDir);
            String classFilename = token.getPackage().getName().replace('.', FILE_SEP) + FILE_SEP + getImplName(token);
            compile(token, tmpDir, classFilename);
            try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile))) {
                writer.putNextEntry(new ZipEntry(classFilename + ".class"));
                Files.copy(Path.of(tmpDir.resolve(classFilename) + ".class"), writer);
            } catch (final IOException e) {
                throw new ImplerException("Compression to jar failed", e);
            }
        } catch (IOException e) {
            throw new ImplerException("Can't create temp folder", e);
        } finally {
            if (tmpDir != null) {
                tmpDir.toFile().deleteOnExit();
            }
        }
    }
}