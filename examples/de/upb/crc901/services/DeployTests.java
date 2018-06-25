package de.upb.crc901.services;

import de.upb.crc901.configurationsetting.operation.SequentialComposition;
import de.upb.crc901.configurationsetting.serialization.SequentialCompositionSerializer;
import de.upb.crc901.services.core.HttpServiceClient;
import de.upb.crc901.services.core.HttpServiceServer;
import de.upb.crc901.services.core.JASEDataObject;
import de.upb.crc901.services.core.OntologicalTypeMarshallingSystem;
import de.upb.crc901.services.core.ServiceCompositionResult;
import de.upb.crc901.services.core.ServiceHandle;
import de.upb.crc901.services.core.TimeLogger;
import de.upb.crc901.services.wrappers.WekaClassifierWrapper;

import jaicore.basic.FileUtil;
import jaicore.ml.WekaUtil;
import jaicore.ml.interfaces.LabeledInstances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;

/**
 * Contains a deployment test case for presentation purposes.
 *
 * @author aminfaez
 *
 */
public class DeployTests {
  private final static int PORT = 8000;

  private static HttpServiceServer server;

  private static HttpServiceClient client;
  private static final OntologicalTypeMarshallingSystem otms = new OntologicalTypeMarshallingSystem();

  private static Instances wekaInstances;
  private static Instances wekaInstancesSmall;
  private static Instances wekaInstancesLarge;
  private static Instances wekaInstancesXLarge;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    /* start server */
    server = HttpServiceServer.TEST_SERVER();

    client = new HttpServiceClient(otms);
    wekaInstances = new Instances(new BufferedReader(new FileReader("../CrcTaskBasedConfigurator/testrsc" + File.separator + "polychotomous" + File.separator + "audiology.arff")));
    // File.separator + "mnist" +
    // File.separator + "test.arff")));

    wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);

    // wekaInstancesSmall = new Instances(
    // new BufferedReader(new FileReader(
    // "../CrcTaskBasedConfigurator/testrsc" +
    // File.separator + "secom" +
    // File.separator + "train.arff")));
    // wekaInstancesSmall.setClassIndex(wekaInstancesSmall.numAttributes() - 1);
    //
    // wekaInstancesLarge = new Instances(
    // new BufferedReader(new FileReader(
    // "../CrcTaskBasedConfigurator/testrsc" +
    // File.separator + "mnist" +
    // File.separator + "train.arff")));
    // wekaInstancesLarge.setClassIndex(wekaInstancesLarge.numAttributes() - 1);
    //
    // wekaInstancesXLarge = new Instances(
    // new BufferedReader(new FileReader(
    // "../CrcTaskBasedConfigurator/testrsc" +
    // File.separator + "mnist" +
    // File.separator + "test.arff")));
    // wekaInstancesXLarge.setClassIndex(wekaInstancesXLarge.numAttributes() - 1);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    server.shutdown();
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test_preproPase_classifyPase() throws FileNotFoundException, IOException {
    List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, new Random(0), .1f, .8f);
    ServiceCompositionResult result = this.executeComposition("testrsc/deployPase.txt", split.get(0), split.get(1), split.get(2));
    System.out.println("Prediction accuracy Scikit RandomForestClassifier: " + result.get("Accuracy").getData());
  }

  @Test
  public void test_preproPase_classifyJase() throws FileNotFoundException, IOException {
    List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, new Random(0), .1f, .8f);
    ServiceCompositionResult result = this.executeComposition("testrsc/deployJase.txt", split.get(0), split.get(1), split.get(2));
    System.out.println("Prediction accuracy Weka RandomForest: " + result.get("Accuracy").getData());
    Assert.assertEquals("localhost:8000", ((ServiceHandle) result.get("forestmodel").getData()).getHost());
  }

  @Test
  public void test_nn_tensorflow() throws IOException {
    List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, new Random(0), .8f, .14f, 0.05f);
    ServiceCompositionResult result = this.executeComposition("testrsc/nn_tf.txt", split.get(0), split.get(1), split.get(2));
    System.out.println("Prediction accuracy MLP TF: " + result.get("Accuracy").getData());
    List<Double> predictions = (List<Double>) result.get("Predictions").getData();
    // System.out.println("Predictions by NN TF: " + predictions);

  }

   @Test
  public void test_nn_weka() throws IOException {
    List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, new Random(0), .8f, .14f, 0.05f);
    ServiceCompositionResult result = this.executeComposition("testrsc/nn_weka.txt", split.get(0), split.get(1), split.get(2));
    System.out.println("Prediction accuracy MLP Weka: " + result.get("Accuracy").getData());
//    List<Double> predictions = (List<Double>) result.get("Predictions").getData();
    // System.out.println("Predictions by NN TF: " + predictions);
  }

   @Test
  public void test_nn_scikit() throws IOException {
    List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, new Random(0), .1f, .7f, 0.2f);
    ServiceCompositionResult result = this.executeComposition("testrsc/nn_sk.txt", split.get(0), split.get(1), split.get(2));
    System.out.println("Prediction accuracy MLP SK: " + result.get("Accuracy").getData());
  }
   
  @Test
  public void timeoutTest() throws IOException, InterruptedException {
	  List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, new Random(0), .8f, .14f, 0.05f);
	  // test java interrupt
	  Thread clientThread = new Thread( () -> {
		  	try {
				this.executeComposition("testrsc/nn_weka.txt", split.get(0), split.get(1), split.get(2));
			} catch (Exception e) {
				e.printStackTrace();
			} 
			try {
				this.executeComposition("testrsc/nn_weka.txt", split.get(0), split.get(1), split.get(2));
			} catch (Exception e) {
				e.printStackTrace();
			} 
	  });
	  clientThread.start();
	  Thread.sleep(2000);
	  clientThread.stop();
	  clientThread.join();
  }

  private ServiceCompositionResult executeComposition(final String filePath, final Object... inputs) throws IOException {
    /* load composition */
    List<String> compositionList = FileUtil.readFileAsList(filePath);
    SequentialCompositionSerializer sqs = new SequentialCompositionSerializer();
    SequentialComposition paseComposition = sqs.readComposition(compositionList);
    /* send composition with data to server */
    ServiceCompositionResult result = client.invokeServiceComposition(paseComposition, inputs);
    /* return the result */
    return result;
  }
  
  

//  @Test
  public void test_runtime_weka() throws Exception {
    System.out.println("DeployTests:test_runtime_weka() starts");

    Instances[] datasets = { wekaInstancesSmall, wekaInstancesLarge, wekaInstancesXLarge };

    for (Instances dataset : datasets) {
      if (dataset == null) {
        System.out.println("Data set is null.");
        continue;
      }
      List<Instances> nominalSplit = WekaUtil.getStratifiedSplit(dataset, new Random(1), .8f);
      Filter nominalToBin = new NominalToBinary();
      nominalToBin.setInputFormat(dataset);
      Instances binData = Filter.useFilter(dataset, nominalToBin);
      List<Instances> binSplit = WekaUtil.getStratifiedSplit(dataset, new Random(1), .8f);

      Instances trainSet = nominalSplit.get(0);
      Instances testSet = nominalSplit.get(1);

      System.out.println("\n\tStart to test time for trainset size: " + trainSet.size() + " and testset size: " + testSet.size() + "\n");

      Classifier localService1 = new RandomTree();
      Classifier localService2 = new RandomTree();

      // local service
      long startTime = System.currentTimeMillis();
      List<String> predictionsLocalOriginal = this.runLocalClassifier(localService1, trainSet, testSet);
      System.out.println("Total Time for local classifier, original dataset: " + (System.currentTimeMillis() - startTime) + " ms");
      localService1 = null;
      // local service for filtered data
      startTime = System.currentTimeMillis();
      List<String> predictionsLocalBinary = this.runLocalClassifier(localService2, binSplit.get(0), binSplit.get(1));
      System.out.println("Total Time for local classifier, dataset with binary attributes: " + (System.currentTimeMillis() - startTime) + " ms");
      localService2 = null;
      // local wrapper:
      // parse data
      startTime = System.currentTimeMillis();
      LabeledInstances<String> parsedTrainSet = (LabeledInstances<String>) otms.objectToSemantic(trainSet).getData();
      LabeledInstances<String> parsedTestSet = (LabeledInstances<String>) otms.objectToSemantic(testSet).getData();
      long parsingTime = (System.currentTimeMillis() - startTime);
      System.out.println("Total Time for parsing data, weka -> JAICore: " + parsingTime + " ms");

      startTime = System.currentTimeMillis();
      WekaClassifierWrapper localWrapper = new WekaClassifierWrapper(RandomTree.class.getConstructor(), new JASEDataObject[0]);
      localWrapper.train(parsedTrainSet);
      List<String> predictionsLocalWrapper = localWrapper.predict(parsedTestSet);
      long localWrapperTime = (System.currentTimeMillis() - startTime);
      System.out.println("Total Time for wrapped classifier: " + localWrapperTime + " ms");
      System.out.println("Total Time for wrapped classifier, plus parsing: " + (localWrapperTime + parsingTime) + " ms");
      parsedTrainSet = null;
      parsedTestSet = null;
      localWrapper = null;

      // service composition:
      startTime = System.currentTimeMillis();
      ServiceCompositionResult result = this.executeComposition("testrsc/weka_randomTree.txt", trainSet, testSet);
      TimeLogger.STOP_TIME("Predictions received");
      System.out.println("Total Time for http service classifier: " + (System.currentTimeMillis() - startTime) + " ms");
      List<String> predicitonsService = (List<String>) result.get("Predictions").getData();

      assert predictionsLocalOriginal.size() == testSet.size();
      assert predictionsLocalBinary.size() == testSet.size();
      assert predictionsLocalWrapper.size() == testSet.size();
      assert predicitonsService.size() == testSet.size();

      // now compare results:
      int mismatch1 = 0, mismatch2 = 0, mismatch3 = 0;

      for (int index = 0, size = testSet.size(); index < size; index++) {

        if (!predictionsLocalOriginal.get(index).equals(predictionsLocalBinary.get(index))) {
          mismatch1++;
        }
        if (!predictionsLocalOriginal.get(index).equals(predictionsLocalWrapper.get(index))) {
          mismatch2++;
        }
        if (!predictionsLocalOriginal.get(index).equals(predicitonsService.get(index))) {
          mismatch3++;
        }
      }
      System.out.println("Amount of mismatched predicitons when filtering with NominalToBonary: " + mismatch1);
      System.out.println("Amount of mismatched predicitons when parsing to JAIcore semantic types: " + mismatch1);
      System.out.println("Amount of mismatched predicitons when using a http service: " + mismatch1);

    }

  }

  public List<String> runLocalClassifier(final Classifier classifier, final Instances trainData, final Instances testData) throws Exception {
    Classifier localService = new RandomTree();
    // train local service
    localService.buildClassifier(trainData);
    // score local service
    List<String> predictedLabels = new ArrayList<>();
    for (Instance instance : testData) {
      double result = localService.classifyInstance(instance);
      String predictedLabel = testData.classAttribute().value((int) result);
      predictedLabels.add(predictedLabel);
    }
    return predictedLabels;
  }
}
