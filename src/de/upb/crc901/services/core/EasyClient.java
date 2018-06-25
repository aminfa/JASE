package de.upb.crc901.services.core;

import jaicore.basic.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A client that makes requesting Server invocations 'easy'. The general idea is
 * to do all service calls via compositions.
 * 
 * EasyClient offers 'with...' methods that allow building a request through
 * chaining. E.g.:
 * 
 * new EasyClient().withBody(body).withHost("localhost:80")
 * 
 * After the desired request is built, use the 'dispatch' method to do a general
 * purpose request. For 'dispatch' a composition text has to be explicitly
 * created.
 * 
 * Alternatively EasyClient also offers methods that generate composition texts
 * before making a service request. E.g.: createService and
 * invokeOneLineOperation
 * 
 */
public final class EasyClient {
	
	private HttpBody body;
	private ServiceHandle innerHandle;

	private OntologicalTypeMarshallingSystem otms;

	public EasyClient() {
		this.innerHandle = new ServiceHandle();
		this.body = new HttpBody();
		this.otms = new OntologicalTypeMarshallingSystem();
	}

	public boolean checkInputs(final String... inputNamesToCheck) {
		for (String constructorArg : inputNamesToCheck) {
			if (!this.body.getState().containsField(constructorArg)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates a single servicehandle.
	 * 
	 * @param constructorArgNames
	 *            field names that are included as in the constructor as
	 *            arguments.
	 */
	public ServiceHandle createOneService(final String... constructorArgNames)
			throws IOException {
		// check if requirements met
		{
			if (!this.innerHandle.isRemote()) {
				throw new RuntimeException();
			}
			if (!this.innerHandle.isClassPathSpecified()) {
				throw new RuntimeException();
			}
			if (!this.checkInputs(constructorArgNames)) {
				throw new RuntimeException(
						"Field: " + Arrays.toString(constructorArgNames)
								+ " not available.");
			}
		}
		// create a new composition:
		String comp = "out = " + this.innerHandle.getHost() + "/"
				+ this.innerHandle.getClasspath() + "::__construct({";
		comp += this.getCompositionArgsFromStringInputs(constructorArgNames);
		comp += "})";
		this.withComposition(comp);
		ServiceCompositionResult result = this.dispatch();
		if (result.containsKey("out")) {
			JASEDataObject jdo = result.get("out");
			if (jdo.getData() instanceof ServiceHandle) {
				return (ServiceHandle) jdo.getData();
			}
		}
		throw new RuntimeException(
				"Error occurred during creation of service.");
	}

	/**
	 * Requirements for this method: innerHandle has a host A composition has
	 * been created.
	 */
	public ServiceCompositionResult dispatch() throws IOException {
		{
			if (!this.innerHandle.isRemote()) {
				throw new RuntimeException();
			}
			if (!this.body.containsComposition()) {
				throw new RuntimeException();
			}
		}
		HttpServiceClient client = new HttpServiceClient(this.otms);
		return client.sendCompositionRequest(this.innerHandle.getHost(),
				this.body);
	}

	public JASEDataObject invokeOneLineOperation(final String methodName,
			final String... methodArgNames) throws IOException {
		// check if requirements met
		{
			if (!this.innerHandle.isRemote()) {
				throw new RuntimeException();
			}
			if (!this.innerHandle.isSerialized()) {
				throw new RuntimeException();
			}
			if (!this.checkInputs(methodArgNames)) {
				throw new RuntimeException("Field: "
						+ Arrays.toString(methodArgNames) + " not available.");
			}
		}
		// create a new composition:
		String comp = "out = " + this.innerHandle.getServiceAddress() + "::"
				+ methodName + "({";
		comp += this.getCompositionArgsFromStringInputs(methodArgNames);
		comp += "})";
		this.withComposition(comp);
		return this.dispatch().get("out");
	}

	/**
	 * Appends a construct operation to the composition. Requires a host to be
	 * set before.
	 * 
	 * @param outFieldName
	 *            the field name that is on the left side of '='.
	 * @param classpath
	 *            classpath of the service to be created.
	 * @param constructorArgFieldNames
	 *            Parameter names that are used in the constructor.
	 */
	public EasyClient withAddedConstructOperation(final String outFieldName,
			final String classpath, final String... constructorArgFieldNames) {
		// check if requirements are met
		{
			if (!this.innerHandle.isRemote()) {
				throw new RuntimeException();
			}
		}
		String compositionLine = outFieldName + "=" + this.innerHandle.getHost()
				+ "/" + classpath + "::__construct({";
		compositionLine += this
				.getCompositionArgsFromStringInputs(constructorArgFieldNames);
		compositionLine += "});";

		body.addOpToComposition(compositionLine);
		return this;
	}

	/**
	 * Appends a method operation to the composition. Additionally, if the
	 * composition was empty before adding the created operation, the
	 * innerHandle will be set to the service retrieved with 'variableFieldName'
	 * from the keyword argument pool. If the service has not been added with
	 * 'withKeywordArgument' before, nothing will happen.
	 * 
	 * @param outFieldName
	 *            the field name that is on the left side of '='.
	 * @param variableFieldName
	 *            field name of the service variable on whome the operation is
	 *            executed.
	 * @param methodName
	 *            The name of the method to be executed.
	 * @param methodArgFieldNames
	 *            Parameter names that are used in the method invocation.
	 * 
	 */
	public EasyClient withAddedMethodOperation(String outFieldName,
			final String variableFieldName, final String methodName,
			final String... methodArgFieldNames) {
		String compositionLine = outFieldName + "=" + variableFieldName + "::"
				+ methodName + "({";
		compositionLine += this
				.getCompositionArgsFromStringInputs(methodArgFieldNames);
		compositionLine += "});";
		if (!body.containsComposition()) {
			// if the body doesn't contain a composition, this operation will be
			// the first one.
			// try to set the service handler:
			if (body.getState().containsField(variableFieldName)) {
				JASEDataObject jdo = body.getState()
						.retrieveField(variableFieldName);
				if (jdo.isofType(ServiceHandle.class.getSimpleName())) {
					withService((ServiceHandle) jdo.getData()); // replace the
																// inner handler
				}
			}
		}
		body.addOpToComposition(compositionLine);
		return this;
	}

	public EasyClient withBody(final HttpBody body) {
		this.body = Objects.requireNonNull(body);
		return this;
	}

	public EasyClient withInputs(final EnvironmentState envState) {
		this.body.getState().extendBy(envState);
		return this;
	}

	public EasyClient withClassPath(final String classpath) {
		Objects.requireNonNull(classpath);
		this.innerHandle = this.innerHandle.withClassPath(classpath);
		return this;
	}

	public EasyClient withComposition(final String compostionText) {
		this.body.setComposition(compostionText);
		return this;
	}

	public EasyClient withCompositionFile(final String filePath)
			throws IOException {
		String composition = FileUtil.readFileAsString(filePath);
		this.body.setComposition(composition);
		return this;
	}

	public EasyClient withCurrentIndex(final int currentIndex) {
		this.body.setCurrentIndex(currentIndex);
		return this;
	}

	public EasyClient withHost(String host) {
		Objects.requireNonNull(host);
		while (host.endsWith("/")) {
			host = host.substring(0, host.length() - 1);
		}
		this.innerHandle = this.innerHandle.withExternalHost(host);
		return this;
	}

	public EasyClient withHost(final String host, final int port) {
		Objects.requireNonNull(host);
		this.innerHandle = this.innerHandle.withExternalHost(host + ":" + port);
		return this;
	}

	public EasyClient withKeywordArgument(final String keyword,
			final Object data) {
		Objects.requireNonNull(keyword);
		Objects.requireNonNull(data);
		JASEDataObject parsedArgument = this.otms.allToSemantic(data, false);
		this.body.addKeyworkArgument(keyword, parsedArgument);
		return this;
	}
	
	public EasyClient withKeywordArgument_StringList(final String keyword,
			final String...argValues) {
		Objects.requireNonNull(argValues);
		List<String> stringlist = new ArrayList<String>(Arrays.asList(argValues));
		return withKeywordArgument(keyword, stringlist);
	}
	
	public EasyClient withPositionalArgument(final Object data) {
		Objects.requireNonNull(data);
		JASEDataObject parsedArgument = this.otms.allToSemantic(data, false);
		this.body.addPositionalArgument(parsedArgument);
		return this;
	}

	
	public EasyClient withPositionalArgument_StringList(final String...argValues) {
		Objects.requireNonNull(argValues);
		List<String> stringlist = Arrays.asList(argValues);
		return withPositionalArgument(stringlist);
	}

	public EasyClient withMaxIndex(final int currentIndex) {
		this.body.setMaxIndex(currentIndex);
		return this;
	}


	public EasyClient withService(final ServiceHandle serviceHandle) {
		this.innerHandle = Objects.requireNonNull(serviceHandle);
		return this;
	}

	public EasyClient withService(final String classpath, final String id) {
		this.innerHandle = this.innerHandle.withClassPath(classpath);
		this.innerHandle = this.innerHandle.withId(id);
		return this;
	}

	/**
	 * Sets the otms that is being used by this client. An otms object contains
	 * caches that help speedup serialization after the otms is used a bunch.
	 */
	public EasyClient withOTMS(OntologicalTypeMarshallingSystem otms) {
		if(otms != null) {
			this.otms = otms;
		}
		return this;
	}

	public String getCurrentCompositionText() {
		return body.getComposition();
	}

	// _____ UTILITY METHODS _____

	private String getCompositionArgsFromStringInputs(
			final String... argNames) {
		String comp = "";
		int index = 1;
		for (String constructorArg : argNames) {
			comp += ",i" + index + "=" + constructorArg;
			index++;
		}
		if (argNames.length == 0) {
			comp += ","; // scs bug
		} else {
			comp = comp.substring(1); // first comma is too much. Look at the
										// loop
		}
		return comp;
	}

}