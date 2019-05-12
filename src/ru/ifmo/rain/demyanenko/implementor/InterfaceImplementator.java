package ru.ifmo.rain.demyanenko.implementor;

import java.lang.reflect.Method;
/**
 * It serves to implement interface.
 * 
 * @author demyanenko
 * @version 1.0
 */

public class InterfaceImplementator{
	/**
	 * It serves to implement interface
	 * 
	 * @param classToken {@link java.lang.Class}
	 * @return {@link java.lang.String}
	 */
	public static String implement(Class<?> classToken) {
		String aPackage = BaseImplementator.implementPackge(classToken);
		String header = implementHeader(classToken);
		String implementedMethods = "";
		for (Method method : classToken.getMethods()) {
			String implementedClassMethod = BaseImplementator.implementMethod(method);

			if (implementedClassMethod.isEmpty()) {
				continue;
			}

			implementedMethods += "\n\n" + implementedClassMethod;
		}

		return String.format("%s\n%s\n%s\n}", aPackage, header, implementedMethods);
	}

	/**
	 * It serves to implement header line that contains class attributes.
	 * 
	 * @param classToken {@link java.lang.Class}
	 * @return {@link java.lang.String}
	 */
	public static String implementHeader(Class<?> classToken) {
		return String.format("public class %sImpl implements %s {\n", classToken.getSimpleName(), classToken.getName());
	}
}
