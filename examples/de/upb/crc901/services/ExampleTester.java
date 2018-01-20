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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JOptionPane;

import org.junit.After;
import org.junit.Before;
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
import jaicore.basic.FileUtil;
import jaicore.basic.MathExt;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.SimpleInstanceImpl;
import jaicore.ml.core.SimpleInstancesImpl;
import org.junit.Assert;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;

public class ExampleTester {

	private final static int PORT = 8000;

	private HttpServiceServer server;
	private SequentialComposition composition;
	private SequentialCompositionSerializer sqs;
	private HttpServiceClient client;
	private final OntologicalTypeMarshallingSystem otms = new OntologicalTypeMarshallingSystem("testrsc/conf/types.conf");

	@Before
	public void init() throws Exception {

		/* start server */
		server = new HttpServiceServer(PORT, "testrsc/conf/operations.conf", "testrsc/conf/types.conf");

		/* read in composition */
		sqs = new SequentialCompositionSerializer();
		composition = sqs.readComposition(FileUtil.readFileAsList("testrsc/composition.txt"));

		client = new HttpServiceClient(otms);
	}

	@Test
	public void testClassifier() throws Exception {

		/* read instances */
		Instances wekaInstances = new Instances(
				new BufferedReader(new FileReader("../CrcTaskBasedConfigurator/testrsc" + File.separator + "polychotomous" + File.separator + "audiology.arff")));
		wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, new Random(0), .9f);

		/* create and train classifier service */
		Classifier c = new RandomTree();
		String serviceId = client.callServiceOperation("127.0.0.1:" + PORT + "/" + c.getClass().getName() + "::__construct").get("out").asText();
		client.callServiceOperation(serviceId + "::buildClassifier", split.get(0));

		/* eval instances on service */
		int mistakes = 0;
		for (Instance i : split.get(1)) {
			ServiceCompositionResult resource = client.callServiceOperation(serviceId + "::classifyInstance", i);
			double prediction = Double.parseDouble(resource.get("out").toString());
			if (prediction != i.classValue())
				mistakes++;
		}

		/* report score */
		System.out.println(mistakes + "/" + split.get(1).size());
		System.out.println("Accuracy: " + MathExt.round(1 - mistakes * 1f / split.get(1).size(), 2));
	}

	@Test
	public void testImageProcessor() throws Exception {

		/* create new classifier */
		System.out.println("Now running the following composition: ");
		System.out.println(sqs.serializeComposition(composition));
		File imageFile = new File("testrsc/FelixMohr.jpg");
		FastBitmap fb = new FastBitmap(imageFile.getAbsolutePath());
		JOptionPane.showMessageDialog(null, fb.toIcon(), "Result", JOptionPane.PLAIN_MESSAGE);
		ServiceCompositionResult resource = client.invokeServiceComposition(composition, fb);
		FastBitmap result = otms.jsonToObject(resource.get("fb3"), FastBitmap.class);
		JOptionPane.showMessageDialog(null, result.toIcon(), "Result", JOptionPane.PLAIN_MESSAGE);
	}
	
	@Test
	/**
	 * Tests compatibility with pase server. Note have run compserver.sh from Pase before running these tests.
	 * @throws Exception
	 */
	public void testPaseComposition1() throws Exception {
		List<String> composition_list = FileUtil.readFileAsList("testrsc/pase_composition1.txt");
        SequentialComposition pase_composition = sqs.readComposition(composition_list);

        // Parse inputs from 'testrsc/pase_composition1_data.json' to Instances and Instance
        // Until the marshalling system is implemented the client has to parse the data.
        byte[] encoded = Files.readAllBytes(Paths.get("testrsc/pase_composition1_data.json"));
        String data_string = new String(encoded, Charset.defaultCharset());
        encoded = null; 
        JsonNode data_dict = new ObjectMapper().readTree(data_string);
        JsonNode x_data = data_dict.get("x_data");
        JsonNode y_data = data_dict.get("y_data");
        JsonNode x_test = data_dict.get("x_test");
        jaicore.ml.interfaces.Instances parsed_x_data = new SimpleInstancesImpl(x_data);
        jaicore.ml.interfaces.Instance parsed_y_data = new SimpleInstanceImpl(y_data);
        jaicore.ml.interfaces.Instances parsed_x_test = new SimpleInstancesImpl(x_test);
        Map<String, Object> additionalInputs = new HashMap<>();
        additionalInputs.put("x_data", parsed_x_data);
        additionalInputs.put("y_data", parsed_y_data);
        additionalInputs.put("x_test", parsed_x_test);
        
        ServiceCompositionResult resource = client.invokeServiceComposition(pase_composition, additionalInputs);
        
        // check if predicitons are correct:
        double[] exptectedPredictions = {-0.236, 50.641, 110.041, 142.269, 179.063, 215.796, 216.136, 246.181, 343.532, 459.935}; // manually wrote this array down.
		// extract perdiciton array:
		List<Double> predictions = new ObjectMapper().readValue(resource.get("prediction").traverse(), new TypeReference<ArrayList<Double>>(){});
		// compare one by one
		for(int i = 0; i < exptectedPredictions.length; i++){
			
			Assert.assertEquals(predictions.get(i), exptectedPredictions[i], 0.01);
		}
	}
	
	@Test
	/**
	 * Tests compatibility with pase server. Note have run compserver.sh from Pase before running these tests.
	 * @throws Exception
	 */
	public void testPaseComposition2() throws Exception {
		List<String> composition_list = FileUtil.readFileAsList("testrsc/pase_composition2.txt");
        SequentialComposition pase_composition = sqs.readComposition(composition_list);
        ServiceCompositionResult resource = client.invokeServiceComposition(pase_composition);
        Assert.assertEquals(7, resource.get("f2").intValue());
        
	}
	
	@After
	public void suhtdown() {
		System.out.println("Shutting down ...");
		server.shutdown();
	}
}
