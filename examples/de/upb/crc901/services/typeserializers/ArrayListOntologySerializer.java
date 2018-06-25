package de.upb.crc901.services.typeserializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import de.upb.crc901.services.core.IOntologySerializer;
import de.upb.crc901.services.core.JASEDataObject;
import weka.core.DenseInstance;
import weka.core.json.JSONNode;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ArrayListOntologySerializer implements IOntologySerializer<ArrayList<String>> {

	private static final List<String> supportedTypes = Arrays.asList(new String[] {"StringList"});
	
	@Override
	public ArrayList<String> unserialize(JASEDataObject jdo) {
		if(jdo.getData() instanceof List<?>) {
			// iterate over array elements and add them into an array:
			ArrayList<String> returnedList = new ArrayList<>();
			returnedList.addAll((List<String>) jdo.getData());
			return returnedList;
		}
		else {
			typeMismatch(jdo);
			return null;
		}
		
	}

	@Override
	public JASEDataObject serialize(ArrayList<String> list) {
		JASEDataObject jdo = new JASEDataObject("StringList", list);
		return jdo;
	}

	@Override
	public Collection<String> getSupportedSemanticTypes() {
		return supportedTypes;
	}

}
