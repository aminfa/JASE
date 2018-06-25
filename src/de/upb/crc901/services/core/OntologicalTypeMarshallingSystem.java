/**
 * HttpServiceServer.java
 * Copyright (C) 2017 Paderborn University, Germany
 * 
 * @author: Felix Mohr (mail@felixmohr.de)
 */

/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.upb.crc901.services.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of casting object of arbitrary type to/from its semantic type.
 * 
 * Semantic instances are considered to be boxed into JASEDataObject objects.
 * 
 * The object may be primitive. In this case use the primitveToSemantic or primtiveFromSemantic methods.
 * 
 * If the object isn't primitive use objectToSemantic or objectFromSemantic methods. 
 * For a given object of simple classname: x these methods tries to access the class: 'xOntologySerializer' in the package: 'de.upb.crc901.services.typeserializers'.
 * These classes musst implement IOntologySerializer with generic type: x.
 * 
 * For example if you invoke objectToSemantic with a Catalano.Imaging.FastBitmap object, the method assumes that the class 'de.upb.crc901.services.typeserializers.FastBitmapOntologySerializer' is accessible. This class must implement 'IOntologySerializer\<FastBitmap\>'.
 * 
 * @author aminfaez
 *
 */
public class OntologicalTypeMarshallingSystem {

	private static final Logger logger = LoggerFactory.getLogger(OntologicalTypeMarshallingSystem.class);

	private static final String BOOLEAN_TYPE = Boolean.class.getSimpleName();
	private static final String NUMBER_TYPE = Number.class.getSimpleName();
	private static final String STRING_TYPE = String.class.getSimpleName();
	
	// a cache that maps the link between semantic and pojo objects to a flag that indicates if serialization between them is possible. used by is Link Implemented
	private final static Map<Link, Boolean> linkExistsCache = new HashMap<>();
	
	// a cache that maps each Serializer Class Name to it's serializer. if a class is mapped to null it means that the serializer doesn't exist.
	private final static Map<String, IOntologySerializer> serializerCache = new HashMap<>();
	
	/**
	 * Returns true if the given object can be cast to the given semantic type.
	 */
	public boolean isLinkImplemented(String semanticType, Class<?> clazz) {
		if(semanticType.equals(clazz.getSimpleName())) {
			// no conversion requiered
			return true;
		}
		if(semanticType.equals(NUMBER_TYPE)) {
			if(Float.class.isAssignableFrom(clazz) || clazz.getName().equals("float")) {
				return true;
			}else if(Double.class.isAssignableFrom(clazz) || clazz.getName().equals("double")) {
				return true;
			}else if(Integer.class.isAssignableFrom(clazz) || clazz.getName().equals("int")) {
				return true;
			}else if(Byte.class.isAssignableFrom(clazz) || clazz.getName().equals("byte")) {
				return true;
			}else if(Short.class.isAssignableFrom(clazz) || clazz.getName().equals("short")) {
				return true;
			}else if(Long.class.isAssignableFrom(clazz) || clazz.getName().equals("long")) {
				return true;
			}
			
			return Number.class.isAssignableFrom(clazz); // maybe it can be casted
		}
		Link link = new Link(semanticType, clazz);
		if(linkExistsCache.containsKey(link)) {
			return linkExistsCache.get(link);
		}
		try {
			
			/* check whether serializer exists and wheter it implements the IOntologySerializer interface */
			Class<?> serializerClass = Class.forName("de.upb.crc901.services.typeserializers." + clazz.getSimpleName() + "OntologySerializer");
			Object serializer = serializerClass.newInstance();
			if (!(serializer instanceof IOntologySerializer<?>))
				return false;
			
			/* if the serializer does not support the semantic type, return false */
			if (!((IOntologySerializer<?>)serializer).getSupportedSemanticTypes().contains(semanticType))
				return false;
			linkExistsCache.put(link, true);
			return true;
			
		} catch (Exception e) {
			linkExistsCache.put(link, false);
			return false;
		}
	}

	/**
	 * Returns true if the given object is primitive.
	 * 
	 */
	public boolean isPrimitive(Object o) {
		if(o instanceof JASEDataObject) {
			return isPrimitiveType(((JASEDataObject)o).getType());
		}
		if(o instanceof Number) {
			return true;
		}
		if(o instanceof String) {
			return true;
		}
		if(o instanceof Boolean) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the given type is of primitive.
	 * 
	 */
	public boolean isPrimitiveType(String type) {
		Objects.requireNonNull(type);
		if(type.equals(NUMBER_TYPE)) {
			return true;
		}
		if(type.equals(STRING_TYPE)) {
			return true;
		}
		if(type.equals(BOOLEAN_TYPE)) {
			return true;
		}
		return false;
	}
	
	

	public JASEDataObject primitiveToSemantic(Object o) {
		if(o instanceof JASEDataObject) {
			return (JASEDataObject) o;
		} else if(o instanceof Number) {
			return new JASEDataObject(NUMBER_TYPE, o);
		} else if(o instanceof String) {
			return new JASEDataObject(STRING_TYPE, o);
		} else if(o instanceof Boolean) {
			return new JASEDataObject(BOOLEAN_TYPE, o);
		} else {
			throw new RuntimeException(o.getClass().getName() + " is not of primitive type.");
		}
	}
	
	public Object primitiveFromSemantic(JASEDataObject jdo) {
		String type = jdo.getType();
		if(type.equals(NUMBER_TYPE)) {
			return NumberUtils.createNumber(jdo.getData().toString());
		}
		if(type.equals(STRING_TYPE)) {
			return jdo.getData();
		}
		if(type.equals(BOOLEAN_TYPE)) {
			return "true".equalsIgnoreCase((String) jdo.getData());
		}
		else {
			throw new RuntimeException(type + " is not of primitive type.");
		}
	}
	
	public JASEDataObject primitiveToSemanticAsString(String semanticString) {
		if(isPrimitiveNumber(semanticString)) {
			// if it is NumberUtils.isCreatable then it is of number type:
			Number number = NumberUtils.createNumber(semanticString);
			return primitiveToSemantic(number);
		} else if(isPrimitiveBoolean(semanticString)){
			// if the string is 'true' or 'false the type must be boolean:
			return primitiveToSemantic(BooleanUtils.toBooleanObject(semanticString));
		} else {
			// if none of the above match just assume it's type was meant to be string:
			return primitiveToSemantic(semanticString);
		}
	}
	
	public boolean isPrimitiveNumber(String stringvalue) {
		return NumberUtils.isCreatable(stringvalue);
	}
	
	public boolean isPrimitiveBoolean(String stringvalue) {
		return ("true".equalsIgnoreCase(stringvalue) ||
				 "false".equalsIgnoreCase(stringvalue));
	}

	public JASEDataObject allToSemantic(Object o, boolean primitiveAsString) {
		if(o instanceof JASEDataObject) {
			return (JASEDataObject) o;
		}
		if(isPrimitive(o)) {
			JASEDataObject jdo;
			if(primitiveAsString) {
				jdo = primitiveToSemanticAsString((String)o);
			} else {
				jdo = primitiveToSemantic(o);
			}
			return jdo;
		}else {
			JASEDataObject jdo = objectToSemantic(o);
			return jdo;
		}
	}
	

	public <T> T allFromSemantic(JASEDataObject jdo, Class<T> clazz) {
		if(clazz.getSimpleName().equals(jdo.getType())){
			return (T) jdo.getData();
		}
		if(isPrimitive(jdo)) {
			Object o = primitiveFromSemantic(jdo);
			if(jdo.getType().equals(NUMBER_TYPE)) {
				Number numbervalue = (Number)o;
				if(Float.class.isAssignableFrom(clazz) || clazz.getName().equals("float")) {
					return (T) new Double(numbervalue.floatValue());
				}else if(Double.class.isAssignableFrom(clazz) || clazz.getName().equals("double")) {
					return (T) new Double(numbervalue.doubleValue());
				}else if(Integer.class.isAssignableFrom(clazz) || clazz.getName().equals("int")) {
					return (T) new Integer(numbervalue.intValue());
				}else if(Byte.class.isAssignableFrom(clazz) || clazz.getName().equals("byte")) {
					return (T) new Integer(numbervalue.byteValue());
				}else if(Short.class.isAssignableFrom(clazz) || clazz.getName().equals("short")) {
					return (T) new Integer(numbervalue.shortValue());
				}else if(Long.class.isAssignableFrom(clazz) || clazz.getName().equals("long")) {
					return (T) new Long(numbervalue.longValue());
				} 
				else {
					throw new RuntimeException("Can't parse " + numbervalue + " to class:" + clazz);
				}
			}
			else {
				return (T) o;
			}
		} else {
			return objectFromSemantic(jdo, clazz);
		}
	}

	public JASEDataObject objectToSemantic(Object o) {
		if (o == null) {
			throw new IllegalArgumentException("Cannot serialize null-objects.");
		}
		String classname = o.getClass().getSimpleName();
		String serializerClassName = "de.upb.crc901.services.typeserializers." + classname + "OntologySerializer";
		IOntologySerializer serializer;
		if(!serializerCache.containsKey(serializerClassName)) {
//				Method method = MethodUtils.getMatchingAccessibleMethod(Class.forName(serializerClassName), "serialize", o.getClass());
//				assert method != null : "Could not find method \"serialize(" + o.getClass() + ")\" in serializer class " + serializerClassName;
			try {
				serializer = (IOntologySerializer<?>) Class.forName(serializerClassName).getConstructor().newInstance();
			} catch (Exception e) {
				// cache the info that the serializer couldnt be found:
				serializerCache.put(serializerClassName, null);
				serializer = null;
			}
		}
		else { // this serializer was cached by a previous invocation:
			serializer = serializerCache.get(serializerClassName);
		}
		if(serializer == null) {
			// couldnt be found:
			throw new UnsupportedOperationException("Cannot convert objects of type " + classname
					+ " to JSON. The necessary serializer class \""+serializerClassName+"\" was not found.");
		
		}
		try {
			
			TimeLogger.STOP_TIME("Serializing " + classname + " started");
			JASEDataObject serialization = (JASEDataObject) serializer.serialize(o);
			// method.invoke(serializer, o);
			TimeLogger.STOP_TIME("Serializing " + classname + " concluded");
			
			return serialization;
		} catch (Exception e) {
			throw new UnsupportedOperationException(
					"Cannot convert objects of type " + o.getClass().getName() + " to JSON objects. The necessary serializer class \"de.upb.crc901.services.typeserializers."
							+ o.getClass().getSimpleName() + "OntologySerializer\" throws an exception.",e);
		}
	}
	
	public  <T> T objectFromSemantic(JASEDataObject jdo, Class<T> clazz) {
		
		String type = jdo.getType();
		/* determine serializer */
		String classname = clazz.getSimpleName();
		String serializerClasspath = "de.upb.crc901.services.typeserializers." + classname + "OntologySerializer";
		IOntologySerializer serializer;
		if(!serializerCache.containsKey(serializerClasspath)) {
			try {
				serializer = (IOntologySerializer) Class.forName(serializerClasspath).getConstructor().newInstance();
			}	catch (Exception e) {
				// cache the info that the serializer couldnt be found:
				serializerCache.put(serializerClasspath, null);
				serializer = null;
			}
		} else {
			serializer = serializerCache.get(serializerClasspath);
		}
		if(serializer == null) { // serializer not found:
			throw new UnsupportedOperationException("Cannot convert objects of type " + type + " to a Java object of class " + clazz.getName()
			+ ". The necessary serializer class \"de.upb.crc901.services.typeserializers." + clazz.getSimpleName() + "OntologySerializer\" was not found.");
		}
			// TODO remove this block
//			Method method = MethodUtils.getAccessibleMethod(serializerClass, "unserialize", JASEDataObject.class);
//			if (method == null)
//				throw new UnsupportedOperationException("Cannot convert objects of type " + type + " to a Java object of class " + clazz.getName()
//						+ ". The serializer class \"de.upb.crc901.services.typeserializers." + clazz.getSimpleName()
//						+ "OntologySerializer\" has no method \"unserialize(JsonNode)\".");
//
//			Object rawSerializer = serializerClass.getConstructor().newInstance();
//			if (!(IOntologySerializer.class.isInstance(rawSerializer)))
//				throw new ClassCastException("The ontological serializer for " + clazz.getSimpleName() + " does not implement the IOntologySerializer interface!");
			

		try {
			TimeLogger.STOP_TIME("Deserializing " + classname + " started");
			/* unserialize the semantic object to an actualy required Java object */
			T returnValue = (T) serializer.unserialize(jdo);
			TimeLogger.STOP_TIME("Deserializing " + classname + " concluded");
			return returnValue;
		}  catch (Exception e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("Cannot convert objects of type " + type + " to a Java object of class " + classname
					+ ". The necessary serializer class \""+serializerClasspath+"\" throws an exception.");
		} 
	}
	
	public Object[] objectArrayFromSemantic(Class<?>[] requiredType, List<JASEDataObject> jdoList) {
		Object[] parsedObjects = new Object[requiredType.length];
		int index = 0;
		for(JASEDataObject jdo : jdoList) {
			if(index >= requiredType.length) {
				break;
			}
			parsedObjects[index] = allFromSemantic(jdo, requiredType[index]);
			index++;
		}
		return parsedObjects;
	}
	
	// tupel class for caching:
	private class Link{
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result;
			result = prime * result + ((clazz == null) ? 0 : clazz.getName().hashCode());
			result = prime * result + ((semanticType == null) ? 0 : semanticType.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Link other = (Link) obj;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			if (semanticType == null) {
				if (other.semanticType != null)
					return false;
			} else if (!semanticType.equals(other.semanticType))
				return false;
			return true;
		}
		final String semanticType;
		final Class<?> clazz;
		Link(String semanticType, Class<?> clazz) {
			super();
			this.semanticType = semanticType;
			this.clazz = clazz;
		}
	}
}
