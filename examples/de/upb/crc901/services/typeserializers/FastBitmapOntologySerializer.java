package de.upb.crc901.services.typeserializers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Catalano.Imaging.FastBitmap;
import de.upb.crc901.services.CatalanoWrapper;
import de.upb.crc901.services.core.IOntologySerializer;
import de.upb.crc901.services.core.JASEDataObject;
import jaicore.ml.core.SimpleInstanceImpl;
import jaicore.ml.interfaces.Instance;

public class FastBitmapOntologySerializer implements IOntologySerializer<FastBitmap> {
	
	private static final List<String> supportedTypes = Arrays.asList(new String[] {"Instance"});
	
	public FastBitmap unserialize(final JASEDataObject jdo) {
		if(jdo.getData() instanceof Instance) {
			Instance instance = (Instance) jdo.getData();
			return new FastBitmap(CatalanoWrapper.instance2FastBitmap(instance));
		}
		else {
			typeMismatch(jdo);
			return null;
		}
	}

	public JASEDataObject serialize(final FastBitmap fb) {
		Instance data = CatalanoWrapper.fastBitmap2Instance(fb);
		String type = "Instance";
		JASEDataObject jdo = new JASEDataObject(type, data);
		return jdo;
	}

	@Override
	public Collection<String> getSupportedSemanticTypes() {
		return supportedTypes;
	}
}
