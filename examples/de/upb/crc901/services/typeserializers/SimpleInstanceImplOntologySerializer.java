package de.upb.crc901.services.typeserializers;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.services.core.IOntologySerializer;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.SimpleInstanceImpl;
import jaicore.ml.core.WekaCompatibleInstancesImpl;
import weka.core.Instances;

public class SimpleInstanceImplOntologySerializer implements IOntologySerializer<SimpleInstanceImpl>  {
	public  SimpleInstanceImpl unserialize(final JsonNode json) {
		return new SimpleInstanceImpl(json);
	}

	public JsonNode serialize(final Instances instances) {
		try {
			if (instances.classIndex() < 0)
				return new ObjectMapper().readTree(WekaUtil.toJAICoreInstances(instances).toJson());
			else
				return new ObjectMapper().readTree(WekaUtil.toJAICoreLabeledInstances(instances).toJson());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JsonNode serialize(SimpleInstanceImpl object) {
		try {
			return new ObjectMapper().readTree(object.toJson());
		} catch (IOException e) { 
			throw new RuntimeException(e.getMessage(), e); // mask checked exception
		}
	}
}
