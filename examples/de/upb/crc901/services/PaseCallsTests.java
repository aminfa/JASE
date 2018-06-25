/**
 * ExampleTester.java
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
package de.upb.crc901.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JOptionPane;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Catalano.Imaging.FastBitmap;
import de.upb.crc901.configurationsetting.operation.SequentialComposition;
import de.upb.crc901.configurationsetting.serialization.SequentialCompositionSerializer;
import de.upb.crc901.services.core.HttpServiceClient;
import de.upb.crc901.services.core.HttpServiceServer;
import de.upb.crc901.services.core.OntologicalTypeMarshallingSystem;
import de.upb.crc901.services.core.ServiceCompositionResult;
import de.upb.crc901.services.core.ServiceHandle;
import jaicore.basic.FileUtil;
import jaicore.basic.MathExt;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.SimpleInstanceImpl;
import jaicore.ml.core.SimpleInstancesImpl;
import jaicore.ml.core.SimpleLabeledInstancesImpl;

import org.junit.Assert;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;

public class PaseCallsTests {


	private HttpServiceClient client;
	private final OntologicalTypeMarshallingSystem otms = new OntologicalTypeMarshallingSystem();

	@Before
	public void init() throws Exception {
		/* read in composition */
		client = new HttpServiceClient(otms);
	}
	@Test
	/**
	 * Tests compatibility with pase server. Note have run compserver.sh from Pase before running these tests.
	 * @throws Exception
	 */
	public void testPaseComposition_DecisionTree() throws Exception {
		List<String> composition_list = FileUtil.readFileAsList("testrsc/pase_composition1.txt");
		SequentialCompositionSerializer sqs = new SequentialCompositionSerializer();
        SequentialComposition pase_composition = sqs.readComposition(composition_list);

        // Parse inputs from 'testrsc/pase_composition1_data.json' to Instances and Instance
        // Until the marshalling system is implemented the client has to parse the data.
        String data_string = FileUtil.readFileAsString("testrsc/pase_composition1_data.json");
        JsonNode data_dict = new ObjectMapper().readTree(data_string);
        JsonNode training_set = data_dict.get("training_set");
        JsonNode prediction_set = data_dict.get("prediction_set");
        jaicore.ml.interfaces.LabeledInstances<String> training_data = new SimpleLabeledInstancesImpl(training_set);
        jaicore.ml.interfaces.Instances prediction_data = new SimpleInstancesImpl(prediction_set);
        Map<String, Object> additionalInputs = new HashMap<>();
        additionalInputs.put("training_data", training_data);
        additionalInputs.put("prediction_data", prediction_data);
        
        ServiceCompositionResult resource = client.invokeServiceComposition(pase_composition, additionalInputs);
        
        // check if predicitons are correct:
        // manualy extracted data from composition.
        List<String> expectedLabels = Arrays.asList("A", "B", "C", "D");
        int expectedPredictionCount = 20;
        
		// extract perdiciton array:
		List<String> predictions = (List<String>) resource.get("prediction").getData();
		Assert.assertEquals(expectedPredictionCount, predictions.size());
		// see if the predicted label is contained in the list  of available one.
		for(int i = 0; i < predictions.size(); i++){

			System.out.print("\"" + predictions.get(i) + "\",");
			if(! expectedLabels.contains(predictions.get(i))) {
				Assert.fail(predictions.get(i) + " was predicted at " + i + " but isn't contained in " + expectedLabels.toString());
			}
		}
	}
	
	@Test
	/**
	 * Tests compatibility with pase server. Note have run compserver.sh from Pase before running these tests.
	 * @throws Exception
	 */
	public void testPaseCompositionPlainLib() throws Exception {
        List<String> composition_list = FileUtil.readFileAsList("testrsc/pase_composition2.txt");
		SequentialCompositionSerializer sqs = new SequentialCompositionSerializer();
        SequentialComposition pase_composition = sqs.readComposition(composition_list);
        ServiceCompositionResult resource = client.invokeServiceComposition(pase_composition);
        Assert.assertEquals(5, resource.get("f2").getData());
        Assert.assertEquals(5, resource.get("f5").getData());
	}
	
	@Test
	public void testPaseServiceOperation() throws Exception{
		ServiceCompositionResult result = client.callServiceOperation("localhost:5000/plainlib.package1.b.B::__construct", 1,2);
//		Assert.assertEquals("plainlib.package1.b.B", result.get("class").asText());
		ServiceHandle sh = (ServiceHandle) result.get("out").getData();
		Assert.assertTrue(sh.isRemote());
	}
	
}
