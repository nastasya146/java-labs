package ru.ifmo.rain.demyanenko.implementor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;

import info.kgeorgiy.java.advanced.implementor.ImplerException;


/**
 * It serves to implement class.
 * 
 * @author demyanenko
 * @version 1.0
 */
public class ClassImplementator {

	/**
	 * It serves to implement class.
	 * 
	 * @param classToken {@link java.lang.Class}
	 * @return {@link java.lang.String}
	 * @throws ImplerException 
	 */	
	public static String implement(Class<?> classToken) throws ImplerException {
		String aPackage = BaseImplementator.implementPackge(classToken);
		String header = implementHeader(classToken);
		String constructor = implementConstructor(classToken);
		String implementedMethods = "";
		HashSet<String> methodSet = new HashSet<String>();
		String name;
		if (Modifier.isAbstract(classToken.getModifiers()))
		{
			List<Class> parents = new ArrayList<>(Collections.singletonList(classToken));

			while (!parents.isEmpty()) {
				Class<?> parent = parents.get(0);

				for (Method method : parent.getDeclaredMethods()){
					name = getMethodUnicName(method);
					if (!methodSet.contains(name))
					{
						try {
							methodSet.add(name);
							String implementedClassMethod = BaseImplementator.implementMethod(method);
							
							if (implementedClassMethod.isEmpty()) {
								continue;
							}

							implementedMethods += "\n\n" + implementedClassMethod;
						} catch (UnsupportedOperationException e)
						{
							System.out.println(parent.getTypeName() + " " + method.getParameterTypes());
						}
					}
				}

				parents.addAll(Arrays.asList(parent.getInterfaces()));
				if (parent.getSuperclass()!=null && Modifier.isAbstract(parent.getSuperclass().getModifiers()))
					parents.add(parent.getSuperclass());
				parents.remove(0);

			}
		}
		
		return String.format("%s\n%s\n%s\n%s\n}", aPackage, header, constructor, implementedMethods);
	}

	private static String getMethodUnicName(Method method) {
		String name = method.getName() + method.getReturnType();
		for (Class<?> paramType : method.getParameterTypes()) {
			name += paramType.getTypeName();
		}
		return name;
	}
	
	/**
	 * It serves to implement a class constructor
	 * 
	 * @param classToken {@link java.lang.Class}
	 * @return {@link java.lang.String}
	 * @throws ImplerException 
	 */
	private static String implementConstructor(Class<?> classToken) throws ImplerException {
		boolean allConstructorsIsPrivate = true;
		boolean hasDefaultConstructor = false;
		
		for (Constructor<?> constructor : classToken.getDeclaredConstructors()) {
			if (constructor.getParameterTypes().length == 0) {
				hasDefaultConstructor = true;
			}
			allConstructorsIsPrivate &= Modifier.isPrivate(constructor.getModifiers());
		}
		
		if (allConstructorsIsPrivate) {
			throw new ImplerException();
		}

		if (classToken.equals(Enum.class)) {
			throw new ImplerException();
		}

		if (hasDefaultConstructor) {
			return "";
		}
		
		String result = String.format("\t%sImpl() throws Exception {\n\t\tsuper(", classToken.getSimpleName());
		for (Class<?> parameter : classToken.getDeclaredConstructors()[0].getParameterTypes()) {
			result += String.format("(%s) %s, ", parameter.getTypeName(), BaseImplementator.getDefaultValue(parameter));
		}
		result = result.substring(0, result.length() - 2);
		result += ");\n\t}";
		return result;
	}

	/**
	 * It serves to implement header line that contains class attributes.
	 * 
	 * @param classToken {@link java.lang.Class}
	 * @return {@link java.lang.String}
	 */
	private static String implementHeader(Class<?> classToken) {
		int mod = classToken.getModifiers();
		String modString = "public";
		if (Modifier.isAbstract(mod)) {
			//modString += " abstract";
		}
		return String.format("%s class %sImpl extends %s {\n", modString, classToken.getSimpleName(), classToken.getName());
	}

}
