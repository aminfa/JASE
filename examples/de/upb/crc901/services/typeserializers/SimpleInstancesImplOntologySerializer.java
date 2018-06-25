package de.upb.crc901.services.typeserializers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.services.core.IOntologySerializer;
import de.upb.crc901.services.core.JASEDataObject;
import jaicore.ml.core.SimpleInstancesImpl;
import jaicore.ml.interfaces.Instances;

public class SimpleInstancesImplOntologySerializer implements IOntologySerializer<Instances>  {
	
	private static final List<String> supportedTypes = Arrays.asList(new String[] {"Instances"});
	
	public  Instances unserialize(final JASEDataObject jdo) {
		return (Instances) jdo.getData();
	}

	@Override
	public JASEDataObject serialize(Instances object) {
		String type = "Instances";
		JASEDataObject jdo = new JASEDataObject(type, object);
		return jdo;
	}

	@Override
	public Collection<String> getSupportedSemanticTypes() {
		return supportedTypes;
	}
}
