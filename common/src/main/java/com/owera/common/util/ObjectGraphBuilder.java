package com.owera.common.util;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * The purpose of ObjectGraphBuilder is to build a "Graph" or "Tree"
 * which represent the structure of an object. While it does so, it
 * also counts the memory usage for each of the objects and primitives
 * it process. I picture two ways of using this program:<ul>
 * <li>Measure the memory usage of an object 
 * <li>Inspect the object-tree/graph
 * </ul>I should be quick to say that the memory usage calculation is not
 * entirely accurate, but I sincerly hope that the calculation is so good
 * that it enables you to compare two objects and see which one is causing
 * the memory leakage (if you have one). 
 * <p>
 * Those of you who have used a memory-profiler will understand that my
 * little program does something like they do. The difference could be
 * summarized like this:<ul>
 * <li>The memory-profiler is much more advanced, have more graphics and so on.
 * <li>The memory-profiler cost a good deal of money
 * <li>The memory-profiler can be very resource-intensive if run in your
 * production-environment (because it is so advanced?)
 * <li>This program enables you pretty much full control over when and where
 * you want to check an object or two. I assume the cost of resources will
 * be so low that you could run this permanently in a production environment.
 * <li>You will be able to build further on this program, as long as you 
 * acknowledge my work in future modifications of this program.
 * </ul><p>
 * About the inaccurracy of this program:<br>
 * I have discovered that Strings are notoriously difficult to get a grip on.
 * The point is that when a String is created, the JVM checks its String-pool
 * first. I don't know exactly how that works, and my guess is that this
 * may very well differ from JVM to JVM. So, creating 1000 Strings could
 * demand 50 byte or 50000 byte of the memory. And we just don't know. There
 * fore I choose to take the latter approach, knowing that this could be
 * very wrong in some cases, but probably more right than wrong. However, my
 * main motivation for the decision was to be able to compare two objects
 * correctly, and counting each String object as unique will do just that.
 * <p>
 * Looking at the objectgraph you will soon realize that the objectgraph is
 * strictly hierarchial, that is an object which directly or indirectly refers
 * to each other, is not seen. We must do so, because we can't count
 * the same object twice if we will have correct memomry calculation, and
 * because it because more difficult to represent the objectgraph with our
 * toString()-method.
 * <p>
 * I have had my fair share of troubles trying to test OGB and one of the
 * problems has to do with static members. Keep in mind that my primary goal
 * is to compare two objects. Therefore, when I tried to compute an OG for
 * a Locale-object and failed because of a fairly complex static-structure
 * is this object, a chose to make certain adjustments:<ul>
 * <li>Ignore static primitives and primitive-wrappers alltogether
 * <li>Ignore all static members except those which are defined in a
 * 			class in a package defined in OGB (by using the addPacakgePrefix()).
 * 			These static members are added to a staticMap, so that these objects
 * 			are not counted twice in the process. A sound usecase would be to add
 * 			a packagename-prefix of a package you have made, to measure the effect
 * 			of the static structures there. However, any static structures in
 * 			a third-party library would be ignored. To cancel this behaviour, add 
 * 			prefix "", it will then match all package-names.
 * <li>Ignore a static member which is of the same type (class) as the class
 * 			it is a member of.  
 * </ul><p>
 * Warning:<br>
 * I have made a static staticMap. Every static object is put into here,
 * and it could cause this program itself to be the cause of a memory leak.
 * Use the method clearStaticMap() each time you want to start on a new fresh
 * 'profiling' or just want to clear the memory of its static-map.
 * 
 * <p>
 * Morten Simonsen, Oslo, October 20th 2004
 */

@SuppressWarnings({ "rawtypes" })
public class ObjectGraphBuilder {

	/* These constants are testet and found in the runtime of 
	 * Websphere 5.1 (that is an IBM JDK Runtime). To accuratly
	 * (or as accuratly as possible) measure the memory usage in
	 * other runtimes, you should measure these constants again.
	 * However, it is possibe that the constants are fairly 
	 * equal on all 32-bit plattforms.
	 * 
	 * A really good contribution to this program would be to compute
	 * this constants dynamically by running a large number of
	 * tests at the initialization of the ObjectGraphBuilder.
	 */

	private static int INT = 4;
	private static int LONG = 8;
	private static int FLOAT = 4;
	private static int DOUBLE = 8;
	private static int BOOLEAN = 1;
	private static int CHAR = 2;
	private static int SHORT = 2;
	private static int BYTE = 1;

	private static int INTEGERW = 16;
	private static int LONGW = 24;
	private static int FLOATW = 16;
	private static int DOUBLEW = 24;
	private static int BOOLEANW = 16;
	private static int CHARACTERW = 16;
	private static int SHORTW = 16;
	private static int BYTEW = 16;

	public static int OBJECTREF = 4;
	public static int OBJECTSIZE = 16;

	private static Map<Object, String> staticMap = new HashMap<Object, String>();
	private static List<String> packageList = new ArrayList<String>();
	private static Map<Class, ObjectGraphPredefinedClass> classMap = new HashMap<Class, ObjectGraphPredefinedClass>();
	private static boolean initRun = false;

	/**
	 * Here you could put code for initializing packagePrefix-list and
	 * the class-map. The code below is an example of how it is done in
	 * the Owera company.
	 */
	public static void init() {
		initRun = true;
		ObjectGraphBuilder.addPackagePrefix("com.owera");
		ObjectGraphBuilder.addPredefinedClass(SimpleDateFormat.class, 367264, 1795);
	}

	public static void addPredefinedClass(Class c, long initalMemUsage, long additionalMemUsage) {
		ObjectGraphPredefinedClass ogpc = new ObjectGraphPredefinedClass(c, initalMemUsage, additionalMemUsage);
		classMap.put(c, ogpc);
	}

	public static void addPackagePrefix(String packagePrefix) {
		packageList.add(packagePrefix);
	}

	public static void clearPackagePrefixes() {
		packageList = new ArrayList<String>();
	}

	/**
	 * This method decide whether a static object should be processed further
	 * or not. The rules is mentioned in the beginning of this class, but
	 * here is a repetition:<ul>
	 * <li>If the fields class == the class where the field is defined, then
	 * 		skip processing.
	 * <li>If the static object is processed before (checking the 'staticMap')
	 * 		then we skip processing.
	 * <li>If the prefix of the packagename of a field is not defined in 
	 * 		'packageList', then we skip processing further. If packageList 
	 * 		contains as first element "", then all static members are valid in
	 * 		this test.  
	 * </ul>
	 */
	private static boolean processStatic(Class containerClass, Field field, Object fieldObject) {

		Class fieldClass = field.getType();
		if (fieldClass.equals(containerClass))
			return false;
		if (staticMap.get(fieldObject) != null)
			return false;

		staticMap.put(fieldObject, "PROCESSED");

		Package pack = containerClass.getPackage();
		String packName = null;
		if (pack == null)
			packName = "";
		else {
			packName = pack.getName();
		}
		for (int i = 0; i < packageList.size(); i++) {
			String packPrefix = (String) packageList.get(i);
			if (packName.startsWith(packPrefix) || packPrefix.equals("")) {
				return true;
			}
		}
		return false;
	}

	private static ObjectGraph objectArrayObjectGraph(String objectName, String className, ObjectGraph og, Object o, Stack<Object> s) {
		Map<String, Object> objectMap = og.getObjectMap();
		ObjectGraph newOg = new ObjectGraph();
		Object[] array = (Object[]) o;
		newOg.setInstanceCount(array.length);
		newOg.setMemUsage(OBJECTREF + OBJECTSIZE);
		newOg.setObjectName(objectName);
		newOg.setClassName(className);
		for (int i = 0; i < array.length; i++) {
			Object oneArrayObject = array[i];
			if (oneArrayObject == null)
				nullObjectGraph(objectName + "[" + i + "]", className, newOg);
			else {
				s.push(o);
				newOg = computeOG(oneArrayObject, objectName + "[" + i + "]", s, newOg);
				s.pop();
			}
		}
		og.addMemUsage(newOg.getMemUsage());
		objectMap.put(objectName, newOg);
		return og;
	}

	/**
	 * Ok, here we go:<br><ul>
	 * <li>The input of this method is *the* object which we want to build the objectgraph from. 
	 * In addition we must have a name of the object, we use that as a key in the objectgraph.
	 * Next we must have a empty Stack to start with. The stack is populated each time we have
	 * to call this method again (recursivly) with an object which is a 'member' of *the* object.
	 * When the recursive call is finished, the Stack is popped. That point is, that if object
	 * A refers to B and B refers to C and C refers to A, then the stack would be populated with
	 * A,B before processing C. Then, when we try to process A, we check with the Stack, and 
	 * skip the processing of A for the second time. The last argument is an empty ObjectGraph.
	 * This graph will we populated in the method and at last returned.
	 * 
	 * <li>Check to see if the object processed before (by using the Stack and *the* object)
	 * 
	 * <li>If *the* object is a primitive-wrapper, make an ObjectGraph for this type of object,
	 * based on the constants defined. 
	 *  
	 * <li>If *the* object is a String, make an ObjectGraph for the String, we don't dive
	 * further into the String-object.
	 *
	 * <li>If *the* object is a Reference, return **, because a reference cannot cause any
	 * memory-problems.
	 * 
	 * <li>If *the* object is an array, we have two options:<ul>
	 * 	<li>If the array is a primitive-array, we can directly compute its ObjectGraph
	 * 	<li>If the array is a object-array, we must process each object in the array. First
	 * we create an objectgraph representing the array itself, then we iterate through the
	 * array and create objectgraphs for each object, adding these to the array-object-
	 * graph. To do so we must call this method recursively.
	 * </ul>
	 * <li>At this point we have to conclude that this object is a regular one. Read 
	 * the comment on that particular method for further explanation.
	 */

	private static ObjectGraph computeOG(Object o, String objectName, Stack<Object> s, ObjectGraph og) {
		/* DEBUGING ONLY  
		 int stackDepth = 0;
		 StringBuffer sb = new StringBuffer();
		 if (s != null)
		 stackDepth = s.size();
		 for (int i = 0; i < stackDepth; i++)
		 sb.append("\t");
		 System.out.println(sb.toString() + "Process " + objectName + " " + o + " [Stack-depth is " + stackDepth + "]");
		 DEBUGING ONLY */
		// 2
		if (processedBefore(o, s)) {
			return og;
		}
		// 3
		else if (isPrimitiveWrapper(o.getClass()))
			return primitiveWrapperObjectGraph(objectName, o.getClass().getName(), og, o.getClass());
		// 4
		else if (o.getClass() == String.class)
			return stringObjectGraph(objectName, o.getClass().getName(), og, (String) o);
		else if (o instanceof Reference)
			return og;

		// 5
		else if (o.getClass().isArray()) {
			// 5.1.
			if (o.getClass().getComponentType().isPrimitive())
				return primitiveArrayObjectGraph(objectName, o.getClass().getName(), og, o);
			// 5.2.
			else
				return objectArrayObjectGraph(objectName, o.getClass().getName(), og, o, s);
		}
		// 6		
		else
			return regularObjectObjectGraph(objectName, og, o, s);
	}

	private static void nullObjectGraph(String objectName, String className, ObjectGraph og) {
		ObjectGraph newOg = new ObjectGraph();
		newOg.setInstanceCount(0);
		newOg.setMemUsage(OBJECTREF);
		newOg.setObjectName(objectName);
		newOg.setClassName(className);
		og.addMemUsage(newOg.getMemUsage());
		og.getObjectMap().put(objectName, newOg);
	}

	private static int primitiveArrayLength(Object o) {
		if (o.getClass().getComponentType() == Integer.TYPE) {
			return ((int[]) o).length;
		}
		if (o.getClass().getComponentType() == Long.TYPE) {
			return ((long[]) o).length;
		}
		if (o.getClass().getComponentType() == Float.TYPE) {
			return ((float[]) o).length;
		}
		if (o.getClass().getComponentType() == Double.TYPE) {
			return ((double[]) o).length;
		}
		if (o.getClass().getComponentType() == Short.TYPE) {
			return ((short[]) o).length;
		}
		if (o.getClass().getComponentType() == Byte.TYPE) {
			return ((byte[]) o).length;
		}
		if (o.getClass().getComponentType() == Character.TYPE) {
			return ((char[]) o).length;
		}
		if (o.getClass().getComponentType() == Boolean.TYPE) {
			return ((boolean[]) o).length;
		}
		return 0;
	}

	public static void clearStaticMap() {
		staticMap = new HashMap<Object, String>();
	}

	/* 
	 * Because the method getDecalredFields() only returns the Fields defined in the
	 * particular Class, we have to iterate through all the superclasses to find all
	 * the fields defined in one object.
	 */
	private static Field[] allFields(Class c) {
		Field[] fields = c.getDeclaredFields();
		// 2.			
		Class c1 = c;
		while (c1.getSuperclass() != null && c1.getSuperclass() != Object.class) {
			Field[] tmp = fields;
			c1 = c1.getSuperclass();
			Field[] superFields = c1.getDeclaredFields();
			fields = new Field[fields.length + superFields.length];
			System.arraycopy(tmp, 0, fields, 0, tmp.length);
			System.arraycopy(superFields, 0, fields, tmp.length, superFields.length);
		}
		return fields;
	}

	private static ObjectGraph primitiveArrayObjectGraph(String objectName, String className, ObjectGraph og, Object o) {
		ObjectGraph newOg = new ObjectGraph();
		int lengthOfArray = primitiveArrayLength(o);
		newOg.setInstanceCount(lengthOfArray);
		newOg.setMemUsage(primitiveSize(o.getClass().getComponentType()) * lengthOfArray + OBJECTREF + OBJECTSIZE);
		og.addMemUsage(newOg.getMemUsage());
		newOg.setObjectName(objectName);
		newOg.setClassName(className);
		og.getObjectMap().put(objectName, newOg);
		return og;
	}

	private static void primitiveObjectGraph(ObjectGraph og, Field field) {
		ObjectGraph fieldOg = new ObjectGraph();
		fieldOg.setInstanceCount(1);
		fieldOg.setMemUsage(primitiveSize(field.getType()));
		fieldOg.setObjectName(field.getName());
		fieldOg.setClassName(field.getType().getName());
		og.addMemUsage(fieldOg.getMemUsage());
		og.getObjectMap().put(field.getName(), fieldOg);
	}

	private static ObjectGraph primitiveWrapperObjectGraph(String objectName, String className, ObjectGraph og, Class c) {
		ObjectGraph newOg = new ObjectGraph();
		newOg.setInstanceCount(1);
		newOg.setMemUsage(primitiveWrapperSize(c) + OBJECTREF);
		og.addMemUsage(newOg.getMemUsage());
		newOg.setObjectName(objectName);
		newOg.setClassName(className);
		og.getObjectMap().put(objectName, newOg);
		return og;
	}

	/*
	 * The processing of a regular object is separated in several steps. First an explanation: 
	 * A Field is a member variable of a Class. We can only find the fields of one particular class
	 * at the time. Now the steps:
	 * 1. Find all the fields to the objects class (including those in the superclasses)
	 * 2. Create an objectgraph representing *the* object. Then iterate throgh all the fields of the
	 * object. Now three things can happen:
	 * 2.1. The field is a static one and a primitive-wrapper/primitive. We ignore the field.
	 * 2.2. The field is a primitve. We make an objectgraph for that primitive.
	 * 2.3. The field is an object. We extract the object from the field.
	 * 2.3.1. If the object turns out to be null, we compute a null-objectgraph.
	 * 2.3.2. Check to see if the object is a static one. If this is the case, then
	 * 				we check to see if the object is created in a package listed in the packagePrefixList.
	 * 				If this is also the case, we check to see if we have processed this
	 * 				object before. If this is not the case, then the method signals "true" and
	 * 				the object is processed in the next step.
	 * 2.3.3.  Process the field-object recursively
	 * 
	 * We have to have a try/catch around each field-loop, because the method
	 * field.get(o) may throw an IllegalAccessException. I guess that is connected
	 * to SecurityManager, but the easiest thing to do is just ignore the field
	 * if anything wrong happens.
	 */

	@SuppressWarnings("unchecked")
	private static ObjectGraph regularObjectObjectGraph(String objectName, ObjectGraph og, Object o, Stack s) {

		Class c = o.getClass();
		if (classMap.get(c) != null) {
			ObjectGraphPredefinedClass ogpc = (ObjectGraphPredefinedClass) classMap.get(c);
			ogpc.makeObjectGraph(objectName, s, og);
			return og;
		}
		// 1.
		Field[] fields = allFields(c);
		// 2.
		Map objectMap = og.getObjectMap();
		ObjectGraph newOg = new ObjectGraph();
		newOg.setInstanceCount(1);
		newOg.setMemUsage(OBJECTSIZE + OBJECTREF);
		newOg.setObjectName(objectName);
		newOg.setClassName(c.getName());
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			try {
				// 2.1.
				if (Modifier.isStatic(field.getModifiers()) && (field.getType().isPrimitive() || isPrimitiveWrapper(field.getType())))
					continue;
				// 2.2.
				if (field.getType().isPrimitive())
					primitiveObjectGraph(newOg, field);
				// 2.3.
				else {
					field.setAccessible(true);
					Object fieldObject = field.get(o);
					// 2.3.1.
					if (fieldObject == null) {
						if (Modifier.isStatic(field.getModifiers()))
							continue;
						else
							nullObjectGraph(field.getName(), field.getType().getName(), newOg);
					} else {
						// 2.3.2.
						if (Modifier.isStatic(field.getModifiers()) && fieldObject != null) {
							boolean doProcess = processStatic(c, field, fieldObject);
							if (!doProcess)
								continue;

						} // 2.3.3.
						s.push(o);
						newOg = computeOG(fieldObject, field.getName(), s, newOg);
						s.pop();
					}
				}
			} catch (Throwable t) {
				System.err.println("Error occurred proccesing field: " + field.getName() + " Exception: " + t.toString());
			}
		}
		og.addMemUsage(newOg.getMemUsage());
		objectMap.put(objectName, newOg); // objectMap ligger i og
		return og;
	}

	private static ObjectGraph stringObjectGraph(String objectName, String className, ObjectGraph og, String s) {
		ObjectGraph newOg = new ObjectGraph();
		newOg.setInstanceCount(1);
		newOg.setMemUsage(stringSize(s));
		og.addMemUsage(newOg.getMemUsage());
		newOg.setObjectName(objectName);
		newOg.setClassName(className);
		og.getObjectMap().put(objectName, newOg);
		// objectMap ligger i og
		return og;
	}

	/* 
	 * Just some piece of code to check whether the class represents a primitive
	 * wrapper.
	 */
	private static boolean isPrimitiveWrapper(Class c) {
		if (c == Integer.class || c == Long.class || c == Float.class || c == Double.class)
			return true;
		else if (c == Short.class || c == Byte.class || c == Boolean.class || c == Character.class || c == Void.class)
			return true;
		return false;
	}

	/* 
	 * I am unsure of the void-primitive, is it ever instantiated? Save for that
	 * the method just returns the size of a primitive.
	 */
	private static int primitiveSize(Class c) {
		if (c == Integer.TYPE)
			return INT;
		else if (c == Long.TYPE)
			return LONG;
		else if (c == Float.TYPE)
			return FLOAT;
		else if (c == Double.TYPE)
			return DOUBLE;
		else if (c == Boolean.TYPE)
			return BOOLEAN;
		else if (c == Character.TYPE)
			return CHAR;
		else if (c == Short.TYPE)
			return SHORT;
		else if (c == Byte.TYPE)
			return BYTE;
		else
			// (c == Void.TYPE)
			return 0;
	} /* 
	 * I am unsure of the Void-class, is it ever instantiated? Save for that
	 * the method just returns the size of a primitive-wrapper-object.
	 */

	private static int primitiveWrapperSize(Class c) {
		if (c == Integer.class)
			return INTEGERW;
		else if (c == Long.class)
			return LONGW;
		else if (c == Float.class)
			return FLOATW;
		else if (c == Double.class)
			return DOUBLEW;
		else if (c == Short.class)
			return SHORTW;
		else if (c == Byte.class)
			return BYTEW;
		else if (c == Boolean.class)
			return BOOLEANW;
		else if (c == Character.class)
			return CHARACTERW;
		else
			// (c == Void.class)
			return 0;
	}

	private static boolean processedBefore(Object o, Stack<Object> s) {

		Enumeration<Object> enumr = s.elements();
		while (enumr.hasMoreElements()) {
			Object previousO = enumr.nextElement();
			if (previousO == o)
				return true;
			else if (o.getClass() == String.class && previousO.getClass() == String.class && previousO.equals(o))
				return true;
		}
		return false;
	}

	private static int stringSize(String s) {
		if (s == null)
			return 4;
		else
			return s.length() * 2 + 2 * (OBJECTREF + OBJECTSIZE) + 12;
	}

	/*
	 * Because the first level in the objectgraph is just overhead, we use this method
	 * to just remove that part of the objectgraph. In addition we make sure that the
	 * memory-usage is rounded to the nearest 8-byte.
	 */
	public static ObjectGraph compute(Object o, String objectName) {
		if (o == null)
			return null;
		ObjectGraph og = computeOG(o, objectName, new Stack<Object>(), new ObjectGraph());
		og = (ObjectGraph) og.getObjectMap().get(objectName);
		og.addMemUsage(-8);
		if (og.getMemUsage() % 8 > 0)
			og.addMemUsage(og.getMemUsage() % 8);
		return og;
	}

	public static Map<Object, String> getStaticMap() {
		return staticMap;
	}

	public static boolean isInitRun() {
		return initRun;
	}

}
