package de.upb.crc901.services.typeserializers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.services.core.IOntologySerializer;
import de.upb.crc901.services.core.JASEDataObject;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.SimpleLabeledInstanceImpl;
import jaicore.ml.interfaces.LabeledInstance;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class DenseInstanceOntologySerializer implements IOntologySerializer<DenseInstance> {

	private static final List<String> supportedTypes = Arrays.asList(new String[] {"Instance", "LabeledInstance"});
	
	public DenseInstance unserialize(final JASEDataObject jdo) {
		if(jdo.getData() instanceof LabeledInstance<?>) {
			// Parse the inner JsonNode into LabeledInstance
			LabeledInstance<String> data = (SimpleLabeledInstanceImpl) jdo.getData();
			DenseInstance inst = new DenseInstance(WekaUtil.fromJAICoreInstance(data));
			assert inst.dataset() != null : "No dataset assigned to instance " + inst;
			return inst;
		}
		else {
			typeMismatch(jdo);
			return null;
		}
	}

	public JASEDataObject serialize(final DenseInstance instance) {
		Object object;
		String type;
		if (instance.classIndex() < 0) {
			object = WekaUtil.toJAICoreInstance(instance);
			type = "Instance";
		}
		else {
			Instances instances = new Instances(instance.dataset(), 1);
			instances.add(instance);
			
			Instance wekaInstance = instance;
			if(WekaUtil.needsBinarization(instances, true)) {
				weka.filters.unsupervised.attribute.NominalToBinary toBinFilter = new weka.filters.unsupervised.attribute.NominalToBinary();
				Instance filteredInstace = null;
				try {
					toBinFilter.setInputFormat(instances);
					instances = Filter.useFilter(instances, toBinFilter);
					wekaInstance = instances.get(0);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			
			object = WekaUtil.toJAICoreLabeledInstance(wekaInstance);
			type = "LabeledInstance";
		}
		JASEDataObject jdo = new JASEDataObject(type, object);
		return jdo;
	}

	@Override
	public Collection<String> getSupportedSemanticTypes() {
		return supportedTypes;
	}
}
