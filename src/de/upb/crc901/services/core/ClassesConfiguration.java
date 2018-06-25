package de.upb.crc901.services.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jaicore.basic.FileUtil;
import weka.core.json.JSONNode;

/***
 * Encapsulated configuration about classes, like wrappers names.
 * @author aminfaez
 *
 */
public class ClassesConfiguration extends HashMap<String, JsonNode> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private Map<String, JsonNode> rawConfiguration = new HashMap<>();
	
	private static final Logger log = LoggerFactory.getLogger(HttpServiceServer.class);

	/**
	 * Reads the cofiguration files from configPaths and appends them into itself..
	 */
	public ClassesConfiguration(String... configPaths) throws IOException {
		this();
		appendConfigFromFiles(configPaths);
	}
	
	public ClassesConfiguration() {
		super();
	}

	/**
	 * Appends class configurations from files to the existing ones.
	 */
	public void appendConfigFromFiles(String... configFiles) throws IOException  {
		if(configFiles.length == 0) {
			return;
		}
		List<String> jsonStringList = new ArrayList<>(configFiles.length);
		for(String filePath : configFiles) {
			try {
				String jsonString = FileUtil.readFileAsString(filePath);
				jsonStringList.add(jsonString);
			} catch( IOException e) {
				log.error("Exception when reading file {}: {}", filePath, e.getMessage());;
			}
		}
		String[]  jsonStringArr = jsonStringList.toArray(new String[jsonStringList.size()]);
		this.appendConfigFromJsonStrings(jsonStringArr);
	}
	
	/**
	 * Appends class configurations from json-strings to the existing ones.
	 */
	public void appendConfigFromJsonStrings(String... jsonStrings) throws IOException {
		if(jsonStrings.length == 0) {
			return;
		}
		TypeReference<HashMap<String,JsonNode>> typeRef = new TypeReference<HashMap<String,JsonNode>>() {};
        ObjectMapper mapper = new ObjectMapper(); 
        for(String jsonString : jsonStrings) {
        		// iterate over configs and append them to the existing configuration
            HashMap<String,JsonNode> loadedConfig = mapper.readValue(jsonString, typeRef);
            rawConfiguration.putAll(loadedConfig);
        }
        // reset this configuration, in order to call resolveInheritances on a un extended configurations
		this.clear(); // clear all configurations.
        this.putAll(rawConfiguration);
        this.resolveInheritances();
		
	}
	
	/**
	 * Resolves the 'extends' attribute from classes.
	 * A class can define an 'extends' array filled with other classes from the configuration.
	 * The class will inherit all the attributes from the other classes. 
	 */
	private void resolveInheritances() {
		Set<String> resolvedClasses = new HashSet<>();
		Set<String> unresolvedClasses = new HashSet<>();
		for(String classpath : this.keySet()) {
			if(resolvedClasses.contains(classpath)) {
				// this was resolved by a previous iteration. 
				continue;
			}
			else {
				// Resolve the one class.
				JsonNode classconfig = getClassConfiguration(classpath);
				unresolvedClasses.add(classpath);
				resolveInheritance(classconfig, unresolvedClasses, resolvedClasses);
				unresolvedClasses.remove(classpath);
				resolvedClasses.add(classpath);
			}
		}
	}
	
	/** 
	 * Resolves the inheritance of the given class-config by recursively resolving the class-configs which are extended by the given class-config.
	 * By the end of the method all the extended classes will be contained in the resolved set. 
	 * 
	 * Some examples: 
	 * 
	 * If A extends B, because B doesn't inherit anything, all of the attributes from B are immediately added to A. Now A and B are resolved.
	 * 
	 * If A extends B and B extends C, first B needs to be resolved. 
	 * So 'A' will be added to 'unresolved' and resolveInheritance(B, {A}, {})  will be called.
	 * Afterwards 'B' will be added to resolved and it's attributes will be added to A.
	 * 
	 * If A extends B and B extends C and C extends A, there is a circled dependency.
	 * After one recursion steps, resolveInheritance(C, {A, B}, {}) will be called. Then because 'C' extends a class from the unresolved set a runtime exception will be thrown.
	 * 
	 * @inheritSet Set of classes that are inheriting from this classes configuration. 
	 * If the given classconfig inherits from one of the classes in this set a RuntimeException will be thrown to indicate that a circled dependency has occurred.
	 */
	private void resolveInheritance(JsonNode classconfig, Set<String> unresolved, Set<String> resolved) {
		if(classconfig.has("extends")) {
			// Go once through the array to check for circled dependency.
			for(JsonNode superConfig : classconfig.get("extends")) {
				if(unresolved.contains(superConfig.asText())) {
					throw new RuntimeException("Circled dependency in Inheritance in ClassesConfig:"
							+ " The given class and class: " + superConfig.asText() + " extend from each other.");
				}
			}
			for(JsonNode superConfigArrayEntry : classconfig.get("extends")) {
				String superClasspath = superConfigArrayEntry.asText();
				JsonNode superConfig = getClassConfiguration(superClasspath);
				if(!resolved.contains(superClasspath)) {
					// first resolve the base config:
					unresolved.add(superClasspath);
					resolveInheritance(superConfig, unresolved, resolved);
					resolved.add(superClasspath);
					unresolved.remove(superClasspath);
				}
				// now add everything from superConfig to classconfig
				Iterator<String> fieldNames = superConfig.fieldNames();
				while(fieldNames.hasNext()) {
					String attributeName = fieldNames.next();
					if(attributeName.equals("extends")) {
						// don't want to change inheritance or else funny things happen when calling resolveInheritances twice. 
						continue;
					}
					JsonNode superAttribute = superConfig.get(attributeName);
					boolean extended = false; // flag that indicates that attribute has been extended.
					if(classconfig.has(attributeName)) {
						// first try to add the value to the array or dictionary
						JsonNode baseAttribute = classconfig.get(attributeName);
						if(baseAttribute.isArray() && superAttribute.isArray()) {
							((ArrayNode) baseAttribute).addAll((ArrayNode) superAttribute);
						} 
						else if(baseAttribute.isObject() && baseAttribute.isObject()) {
							((ObjectNode)baseAttribute).setAll((ObjectNode) superAttribute);
						}
						else {
							System.err.println("CONFIG: Attribute " + attributeName + " is being replaced by an incompatible type from: " + superClasspath);
						}
					}
					if(!extended) {
						// couldnt add attribute, so just replace it
						((ObjectNode)classconfig).set(attributeName, superAttribute);
					}
				}
			}
		}
	}
	
	/**
	 * Recursively checks if the classpath is set to extend the given baseClasspath.
	 * Deep first search in the inheritance tree.
	 */
	public boolean extendsClassConfig(String classpath, String baseClasspath) {
		JsonNode classconfig = getClassConfiguration(classpath);
		if(classconfig.has("extends")) {
			for(JsonNode extension : classconfig.get("extends")) {
				if(baseClasspath.equals(extension.asText())) {
					// add this class to the bases so all sub classes of this will return true aswell.
					return true;
				}
				// if the extended classpath extends the base classpath also return true:
				else if (extendsClassConfig(extension.asText(), baseClasspath)){
					return true;
				}
			}
		}
		// none found
		return false;
	}
	
	/**
	 * Returns true if the configuration has the given classpath entry.
	 */
	public boolean classknown(String classpath) {
		return this.containsKey(classpath);
	}
	
	/**
	 * Returns the JsonNode for the classpath in as it is defined in the configuration.
	 */
	private JsonNode getClassConfiguration(String classpath) {
		return this.get(classpath);
	}
	
	/**
	 * Returns true if in the configuration the given classpath entry has an wrapper entry.
	 */
	public boolean isWrapped(String classpath) {
		return classknown(classpath) && getClassConfiguration(classpath).has("wrapper");
	}
	
	/**
	 * Returns the classpath of wrapper which the given classpath was assigned onto.
	 */
	public String getWrapperClasspath(String classpath) {
		return this.get(classpath).get("wrapper").asText();
	}
	
	/**
	 * Returns true if the given method is defined within the given class scope in the classes.json configuration.
	 * @param classpath fully qualified class name.
	 * @param methodName method name to check
	 * @return true if the configuration defines the method unter the classpath.
	 */
	public boolean methodKnown(String classpath, String methodName) {
		if(!classknown(classpath)) {
			return false;
		}
		if("__construct".equals(methodName)) {
			// constructor is known.
			return true;
		}
		if(isWrapped(classpath)) {
			// first look at the wrapper
			String wrapperPath = getWrapperClasspath(classpath);
			if(methodKnown(wrapperPath, methodName)) {
				// method overrides it
				return true;
			}
		}
		JsonNode classConfig = getClassConfiguration(classpath);
		if (classConfig.has("methods")) {
			// if the 'methods' fields is defined:
			JsonNode methods = classConfig.get("methods");
			if(methods.isArray()) {
				// traverse array
				for(JsonNode method : methods) {
					// array of methods
					if(method.isTextual()) {
						// simple string entry for method
						if(method.asText().equals(methodName)) {
							return true;
						}
					}
				}
				// method definition not found
				return false;
			}else {
				// object node. use the has method
				return methods.has(methodName);
			}
		}
		else {
			// the 'methods' fields is not defined. 
			// So return true, because if 'methods' is not defined, all methods of the given class are white-listed.
			return true;
		}
	}
	
	/**
	 * The method result-map is used after method invocation to map returning keywords to values, like arguments and return value.
	 * 
	 * Example:
	 * 		The map returned for method with the given methodName for an instance of the class with the given classpath contains entries:
	 * 		-"a":"return"
	 * 			After the method is invoked, 'a' will be mapped to the return value of the invocation.
	 * 		-"b":"i2"
	 * 			After the method is invoked, 'b' will be mapped to the second argument which was passed to the method. 
	 * 			Notice that some methods may apply changes to passed arguments instead of returning values.
	 * 			This way problems with call-by-reference can be resolved.
	 */
	public Map<String, String> getMethodResultMap(String classpath, String methodName) {
		if(!methodKnown(classpath, methodName)) {
			throw new RuntimeException("The classpath: " + classpath + " and method: " + methodName + " are not known.");
		}
		if("__construct".equals(methodName)) {
			return getStandardResultMap();
		}
		if(isWrapped(classpath)) {
			// if the class is wrapped first look if the wrapper config is defining the result map of this method.
			String wrapperClasspath = getWrapperClasspath(classpath);
			if(methodKnown(wrapperClasspath, methodName)) {
				return getMethodResultMap(wrapperClasspath, methodName);
			}
		}
		JsonNode classConfig = getClassConfiguration(classpath);
		if (classConfig.has("methods")) {
			JsonNode methods = classConfig.get("methods");
			if(!methods.isArray()) { // if its not an array it's method may define mapping
				JsonNode method = classConfig.get("methods").get(methodName);
				if(method == null) {
					return null;
				}
				if(!method.isTextual() && method.has("resultmap")){
					// result map was defined
					TypeReference<HashMap<String,String>> typeRef = new TypeReference<HashMap<String,String>>() {};
					ObjectMapper mapper = new ObjectMapper();
					Map<String, String> resultMap = mapper.convertValue(method.get("resultmap"), typeRef);
					return resultMap;	
				}
			}
			
			// although the method is defined, no result-map was defined in the classes.json config. opt out to standard result-map.
		}
		// if no map was returned up to this point, it means that the class white-lists all methods. 
		// return standard resultMap:
		return getStandardResultMap();
		
	}
	
	/**
	 * The standard result map which maps the returned value of the method invocation to the 'out' keyword.
	 * @return standard result map
	 */
	private Map<String, String> getStandardResultMap(){
		Map<String, String> resultMap = new HashMap<>();
		resultMap.put("out", "return");
		return resultMap;
	}
	
	/**
	 * Returns an Iterable of all the subconfigurations of the given baseConfig.
	 * For every classpath in the iterable it holds that it extends baseConfing.
	 * @param baseConfig base configuration
	 * @return iterable off all configuration that extend the given baseconfig.
	 */
	public Iterable<String> allSubconfigs(String baseConfig) {
		// this filter returns true if the provided string extends from one of the classes in bases.
		Function<String, Boolean> inheritanceFilter = classpath -> {
			// classpaths starting with $ aren't actually classpaths
			return !classpath.startsWith("$") && extendsClassConfig(classpath, baseConfig);
		};
		// return a new filtered iterator wrapped in a iterable.
		return () -> new FilteredIterator<>(keySet().iterator(), inheritanceFilter);
	}
	
}