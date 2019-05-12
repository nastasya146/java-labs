package ru.ifmo.rain.demyanenko.implementor;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.StandardJavaFileManager;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.JarImpler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

/**
 * It serves to implement interface.
 * 
 * @author demyanenko
 * @version 1.0
 */

public class Implementator implements Impler, JarImpler{
	/**
	 * 
	 * 
	 * @param args
	 *            command line arguments {@link java.lang.String}
	 * @throws IOException {@link java.io.IOException}
	 * @throws ImplerException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ImplerException, ClassNotFoundException {
		if (args.length != 1 && args.length != 3) {
			System.out.println("You should write only full class name or use -jar key");
			return;
		}
		if (args.length == 1) {
			Class<?> classToken = Class.forName(args[0]);
			Implementator impl = new Implementator();
			impl.implement(classToken, Paths.get("."));
		}

		if (args.length == 3) {
			if (!args[0].equals("-jar")) {
				System.out.println("Wrong input parameters");
				return;
			}
			Class<?> classToken = Class.forName(args[1]);
			Implementator impl = new Implementator();
			impl.implementJar(classToken, Paths.get(args[2]));
		}

	}
	
	/**
	 * This method returns java file name
	 * @see java.lang.String
	 * @param fullClassName {@link java.lang.String}
	 * @return {@link java.lang.String}
	 */
	private static String getJavaFileName(String fullClassName) {
		return String.format("%sImpl.java", fullClassName.substring(fullClassName.lastIndexOf(".") + 1, fullClassName.length()));
	}
	
	/**
	 * This method returns class file name
	 * @param fullClassName {@link java.lang.String}
	 * @return {@link java.lang.String}
	 */
	private static String getClassFileName(String fullClassName) {
		return String.format("%sImpl.class", fullClassName.substring(fullClassName.lastIndexOf(".") + 1, fullClassName.length()));
	}
	
	/**
	 * This method returns file path
	 * @param fullClassName {@link java.lang.String}
	 * @param filePath {@link java.io.File}
	 * @return {@link java.lang.String}
	 */
	private static String getFilePath(String fullClassName, Path filePath) {
		return String.format("%s\\%s", filePath.toAbsolutePath().toString(), fullClassName.substring(0, fullClassName.lastIndexOf(".")).replace('.',  '\\'));
	}
	
	private static void setFileIntoJar(JarOutputStream jarOuputStream, String filePath) throws IOException {
		JarEntry jarEntry = new JarEntry(filePath);
		jarOuputStream.putNextEntry(jarEntry);

		byte[] buffer = new byte[4086];
		int bufferFillingPart = 0;
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(filePath));) {
			while ((bufferFillingPart = inputStream.read(buffer, 0, buffer.length)) != -1) {
				jarOuputStream.write(buffer, 0, bufferFillingPart);
			}
		}
		jarOuputStream.closeEntry();
	}

	/**
	 * This method creates manifest with version and author
	 * @param version {@link java.lang.String}
	 * @param author {@link java.lang.String}
	 * @return {@link java.util.jar.Manifest}
	 */
	private static Manifest makeManifest(String version, String author) {
		Manifest manifest = new Manifest();
		Attributes attributes = manifest.getMainAttributes();
		attributes.put(Attributes.Name.MANIFEST_VERSION, version);
		attributes.put(new Attributes.Name("Author"), author);
		return manifest;
	}	

	/**
	 * This method implements class token into string form. Class token must be
	 * not primitive.
	 * 
	 * @param classToken {@link java.lang.Class}
	 * @return {@link java.lang.String}
	 * @throws ImplerException 
	 */
	private String implement(Class<?> classToken) throws ImplerException {
		if (classToken.isInterface()) {
			return InterfaceImplementator.implement(classToken);
		}

		return ClassImplementator.implement(classToken);
	}
	
	/**
	 * Realize impler interface. This method implements class into java file
	 * 
	 * @param classToken {@link java.lang.Class} type token to create implementation for.
     * @param path root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     * generated.
     */
	@Override
	public void implement(Class<?> classToken, Path path) throws ImplerException {
		if (Modifier.isFinal(classToken.getModifiers())) {
			throw new ImplerException();
		}
		
		String outputClassName = getJavaFileName(classToken.getName());
		String outputFilePath = getFilePath(classToken.getName(), path);
		String outputFullClassName = String.format("%s\\%s", outputFilePath, outputClassName);
		
		try 
		{
			Files.createDirectories(Paths.get(outputFilePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFullClassName), StandardCharsets.UTF_8));) {
			Implementator implementor = new Implementator();
			writer.write(implementor.implement(classToken));
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * * @param classToken {@link java.lang.Class} type token to create implementation for.
     * @param filePath target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    public void implementJar(Class<?> classToken, Path filePath) throws ImplerException {
		System.setProperty("file.encoding","UTF-8");
    	Path curr = Paths.get("");
		this.implement(classToken, curr);
    	String implementedJavaFileName = getJavaFileName(new String(classToken.getName().getBytes(), StandardCharsets.UTF_8));
    	String implementedFilePath = getFilePath(classToken.getName(), curr);
    	String implementedJavaFullName = String.format("%s\\%s", implementedFilePath, implementedJavaFileName);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager =
		compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
		List<String> optionList = new ArrayList<>(Arrays.asList("-cp", "./info.kgeorgiy.java.advanced.implementor.jar"));
		compiler.getTask(null,
				null,
				null,
				optionList,
				null,
				fileManager.getJavaFileObjects(implementedJavaFullName))
				.call();
		try {
			fileManager.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Manifest manifest = makeManifest("1.0", "demyanenko");
		try {
			Files.createDirectories(filePath.getParent());
			filePath.toFile().createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(filePath.toAbsolutePath().toString()), manifest)) {
            String implementedClassFullName = String.format("%s\\%s", implementedFilePath, getClassFileName(classToken.getName()));
            JarEntry jarEntry = new JarEntry(Paths.get("").toUri().relativize(Paths.get(implementedClassFullName).toUri()).toString());
            jarOutputStream.putNextEntry(jarEntry);
			byte[] buffer = new byte[4086];
			int bufferFillingPart = 0;
			try (InputStream inputStream = new BufferedInputStream(new FileInputStream(implementedClassFullName))) {
				while ((bufferFillingPart = inputStream.read(buffer, 0, buffer.length)) != -1) {
					jarOutputStream.write(buffer, 0, bufferFillingPart);
				}
			}

			jarOutputStream.closeEntry();
        } catch (IOException e) {
			e.printStackTrace();
		}
    }
}
