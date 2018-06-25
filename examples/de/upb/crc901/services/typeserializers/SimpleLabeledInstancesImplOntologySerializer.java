package de.upb.crc901.services.typeserializers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.services.core.IOntologySerializer;
import de.upb.crc901.services.core.JASEDataObject;
import jaicore.ml.core.SimpleLabeledInstancesImpl;
import jaicore.ml.interfaces.LabeledInstances;

public class SimpleLabeledInstancesImplOntologySerializer implements IOntologySerializer<LabeledInstances<String>>  {

	private static final List<String> supportedTypes = Arrays.asList(new String[] {"LabeledInstances"});
	
	public LabeledInstances<String> unserialize(final JASEDataObject jdo) {
		return (LabeledInstances<String>) jdo.getData();
	}

	public JASEDataObject serialize(final LabeledInstances<String> linstances) {
		String type = "LabeledInstances";
		JASEDataObject jdo = new JASEDataObject(type, linstances);
		return jdo;
	}
	
	public Collection<String> getSupportedSemanticTypes(){
		return supportedTypes;
	}
}
