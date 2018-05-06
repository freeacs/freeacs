package com.owera.common.util;

import java.lang.reflect.Array;

@SuppressWarnings("rawtypes")
public class ArrayUtils {
	/**
	 * The method can cast an array from one type to another. The
	 * typical case is from Object[] to String[]. (Casting the other
	 * way is standard and straight-forward.) OBS! However, if you try
	 * to convert to/from a primitive array, the method will fail. The
	 * method will also fail if you convert between inconvertible
	 * object-types.
	 */
	public static Object cast(Object array, Class type) {
		if (array == null)
			return null;
		else {
			int arrayLength = getArrayLength(array);
			Object noA = Array.newInstance(type, arrayLength);

			for (int i = 0; i < arrayLength; i++) {
				Array.set(noA, i, Array.get(array, i));
			}
			return noA;
		}
	}

	/**
	 * @param array
	 * @return
	 */
	private static int getArrayLength(Object array) {
		Class arrayClass = array.getClass().getComponentType();
		int arrayLength = 0;
		if (arrayClass.isPrimitive())
			arrayLength = primitiveArrayLength(array);
		else
			arrayLength = ((Object[]) array).length;
		return arrayLength;
	}

	/**
	 * @param o
	 * @return
	 */
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

}
