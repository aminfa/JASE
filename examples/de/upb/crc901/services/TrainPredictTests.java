package de.upb.crc901.services;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.upb.crc901.services.core.EasyClient;
import de.upb.crc901.services.core.HttpServiceClient;
import de.upb.crc901.services.core.HttpServiceServer;
import de.upb.crc901.services.core.OntologicalTypeMarshallingSystem;
import de.upb.crc901.services.core.ServiceHandle;
import weka.core.Instances;

public class TrainPredictTests {

	
	private final static int PORT = 8000;


	  private static final OntologicalTypeMarshallingSystem otms = new OntologicalTypeMarshallingSystem();

	  private static Instances wekaInstances;

	  @BeforeClass
	  public static void setUpBeforeClass() throws Exception {
	    /* start server */

	    wekaInstances = new Instances(new BufferedReader(new FileReader("../CrcTaskBasedConfigurator/testrsc" 
	    + File.separator + "polychotomous" +
	    		File.separator + "weather.arff")));
	    

	    wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);

	  }


	
	@Test
	public void create() throws IOException {
		EasyClient ec =  new EasyClient();
		ec.withHost("localhost:5000").withAddedConstructOperation("out",
				"sklearn.ensemble.RandomForestClassifier");
		ServiceHandle sh = (ServiceHandle) ec.dispatch().get("out").getData();
		ec = new EasyClient();
		ec.withService(sh) 	.withKeywordArgument("s1", sh)
							.withPositionalArgument(wekaInstances)
						 	.withAddedMethodOperation("Empty", "s1", "train", "i1").dispatch();
		ec = new EasyClient();
		ec.withService(sh)	.withKeywordArgument("s1", sh)
							.withPositionalArgument(wekaInstances)
							.withAddedMethodOperation("predictions", "s1", "predict", "i1");
		List<String> predictions = (List<String>) ec.dispatch().get("predictions").getData();
		System.out.println(predictions);
	}

}
