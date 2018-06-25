package de.upb.crc901.services.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * EnvironmentState maps environment field names during an execution to their values. For example:
 * "a" : <Object of semantic type of Instances>, ...
 * 
 * @author aminfaez
 *
 */
@SuppressWarnings("serial")
public final class EnvironmentState implements Serializable {
	/**
	 * Contains field name that were part of the env state from the very beginning. 
	 * In a composition call startingFields is filled with the names of the inputs.
	 */
	private final Set<String> startingFields;
	
	/**
	 * The state of the environment.
	 * Variable names are mapped to their semantical objects.
	 */
	private final Map<String, JASEDataObject> envState;
	
	/**
	 * Counter that indicates how many positional arguments have been added to this instance.
	 */
	private int positionalArgumentCounter = 1;
	
	/**
	 * An empty env state.
	 */
	public EnvironmentState() {
		startingFields = new HashSet<>();
		envState = new HashMap<>();
	}
	
	/**
	 * An env state filled with the objects from starting state.
	 * @param startingState
	 */
	public EnvironmentState(Map<String, JASEDataObject> startingState) {
		startingFields = new HashSet<>(startingState.keySet()); // copy starting field names into a new set.
		envState = new HashMap<>(startingState); // copy the given map into a new map.
	}
	
	/**
	 * Adds all added fieldnames from current env state to the starting field.
	 * After this call addedFieldNames will return an iterable of 0 objects.
	 */
	public void resetStartingField() {
		for(String fieldName : addedFieldNames()) {
			startingFields.add(fieldName);
		}
	}
	
	/**
	 * Returns true if the given fieldname was included in the starting field.
	 */
	public boolean containedAtStart(String fieldName) {
		return startingFields.contains(fieldName);
	}
	
	/**
	 * Returns true if the given fieldname maps to a variable in this env state.
	 */
	public boolean containsField(String fieldName) {
		return envState.containsKey(fieldName);
	}
	
	/**
	 * Returns an iterable object of all field names that are among the starting fields.
	 */
	public Iterable<String> startingFieldNames() {
		return startingFields;
	}
	
	/**
	 * Returns an iterable object of all currently contained field names that map to a object of semantic type.
	 */
	public Iterable<String> currentFieldNames() {
		return envState.keySet();
	}
	
	
	/**
	 * Returns an iterable object of all field name that were added to this state after it's creation.
	 */
	public Iterable<String> addedFieldNames(){
		// return a anonymous sub type of Iterable that returns FileteredIterator in its 'iterator()' method.
		return () ->  
				new FilteredIterator<String>(
					currentFieldNames().iterator(),  	// iterator of all current fieldnames
					(s -> !containedAtStart(s))); 		// filter out every fieldname that was contained at start.
		
	}
	
	/**
	 * Returns an iterable object of all field name that are index fields, like : "$1", "$2", ...
	 */
	public Iterable<String> positionalFieldNames() {
		return () -> {
				return new FilteredIterator<String>(
					currentFieldNames().iterator(),  	// iterator of all current fieldnames
					(s -> isIndexField(s))); 			// filter out every fieldname that was is not a index field.
		};
	}

	/**
	 * Returns an iterable object of all field name that are not index fields, unlike : "$1", "$2", ...
	 */
	public Iterable<String> keywordFieldNames(){
		return () ->
				new FilteredIterator<String>(
					currentFieldNames().iterator(),  	// iterator of all current fieldnames
					(s -> !isIndexField(s))); 			// filter out every fieldname that was is a index field.
	}
	
	/**
	 * Returns an iterable object of all field name that are not index fields, unlike : "$1", "$2", ...
	 */
	public Iterable<String> serviceHandleFieldNames(){
		return () ->
				new FilteredIterator<String>(
					currentFieldNames().iterator(),  	// iterator of all current fieldnames
					(s -> s != null && retrieveField(s).getData() instanceof ServiceHandle )); 			// filter out every fieldname that was is a index field.
	}
	
	
	
	/**
	 * Adds the given JASEDataObject to the state with the given field name.
	 */
	public void addField(String newFieldName, JASEDataObject newVar) {
		if(newVar == null) {
			return;
		}
		if(isIndexField(newFieldName)) { // a positional field is trying to be added:
			int index = indexFromField(newFieldName);
			if(index<=0) {
				throw new RuntimeException("Index: " + index + " not allowed.");
			}
			setPositionalField(index, newVar);
		}else {
			envState.put(newFieldName, newVar);
		}
	}
	
	/**
	 * Returns the variable mapped by the envState with the given field name.
	 */
	public JASEDataObject retrieveField(String fieldName) {
		return envState.get(fieldName);
	}

	public void appendField(JASEDataObject newVar) {
		positionalArgumentCounter++; // one more argument that was added.
		setPositionalField(positionalArgumentCounter-1, newVar);
	}
	
	public void setPositionalField(int index, JASEDataObject newVar) {
		while(positionalArgumentCounter < index) {
			positionalArgumentCounter++;
		}
		if(newVar!=null) {
			envState.put(indexToField(index), newVar);
		}
	}
	/**
	 * This method contains the functional mapping from indices to fieldnames.
	 * TODO the offset of 1 between fieldname and fieldindex should be contained in this method.
	 */
	public static String indexToField(int index) {
		return "i" + index;
	}

	/**
	 * This method contains the functional mapping from fieldname to indices.
	 * TODO the offset of 1 between fieldname and fieldindex should be contained in this method.
	 */
	public static int indexFromField(String indexField) {
		if(isIndexField(indexField)) {
			return Integer.parseInt(indexField.substring(1));
		}else {
			throw new RuntimeException("Can't translate " + indexField + " to index.");
		}
	}
	/**
	 * Checks if the fieldname is a index field.
	 */
	public static boolean isIndexField(String field) {
		return field.matches("^i\\d+$");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((envState == null) ? 0 : envState.hashCode());
		result = prime * result + positionalArgumentCounter;
		result = prime * result + ((startingFields == null) ? 0 : startingFields.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EnvironmentState)) {
			return false;
		}
		EnvironmentState other = (EnvironmentState) obj;
		if (envState == null) {
			if (other.envState != null) {
				return false;
			}
		} else if (!envState.equals(other.envState)) {
			return false;
		}
		if (positionalArgumentCounter != other.positionalArgumentCounter) {
			return false;
		}
		if (startingFields == null) {
			if (other.startingFields != null) {
				return false;
			}
		} else if (!startingFields.equals(other.startingFields)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the pointer to the current map.
	 * @deprecated Will be removed at some point to prohibit access to the map itself. 
	 * That's because a environment by definition doesn't lose fields. 
	 * Exposing the pointer to the inner map may lead to bugs.
	 */
	public Map<String, JASEDataObject> getCurrentMap() {
		return envState;
	}

	/**
	 * Extends the fields of this map by the ones from the given state.
	 */
	public void extendBy(EnvironmentState additionalState) {
		this.extendBy(additionalState.getCurrentMap());
	}

	/**
	 * Extends the fields of this map by the ones from the given map.
	 */
	public void extendBy(Map<String, JASEDataObject> map) {
		for(String keyString : map.keySet()) {
			addField(keyString, map.get(keyString));
		}
	}
}
