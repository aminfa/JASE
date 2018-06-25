package de.upb.crc901.services.serviceobserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpServiceObserver implements HttpHandler {

	public final static String Cancel_Request = "Cancel";
	public final static String Java_Worker_Started = "Java_Worker_Started";
	public final static String Python_Worker_Started = "Python_Worker_Started";
	
	private final static Pattern messageRegex = Pattern.compile(
													"^("
													+ Cancel_Request + "|"
													+ Java_Worker_Started + "|"
													+ Python_Worker_Started + ""
													+ "):(\\d+)" // client id
													+ "(?:_(\\d+))?" // request time millisec
													+ "(?:_(\\d+))?$"	 // python process id
															);
	
	final static Logger logger = LoggerFactory.getLogger(HttpServiceObserver.class);

	private static Map<Long, Long> activeRequests = new HashMap<>();
	
	private static Map<Long, List<Thread>> javaThreads = new HashMap<>();
	private static Map<Long, List<Long>> pythonPIds = new HashMap<>();

	@Override
	public void handle(HttpExchange t) throws IOException {
		String message = IOUtils.toString(t.getRequestBody(), Charset.defaultCharset());
		logger.debug("Observer recevied a new message: {}", message);
		try {
			notice(message, null);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		t.sendResponseHeaders(200,0);
		t.getResponseBody().close();
	}
	
	public static synchronized void javaServerRequestNotice(String requestId) throws IOException {
		notice(Java_Worker_Started + ":" + requestId, Thread.currentThread() );
	}
	
	public static synchronized void notice(String message, Thread javaServerThread) throws IOException {
		logger.info("Observer received message: {}", message); 
		Matcher matcher =messageRegex.matcher(message);
		 if(!matcher.matches()) {
			 logger.error("The message={} has a wrong syntax");
			 return;
		 }
		 String messageFlag = matcher.group(1);
		 long clientId = Long.parseLong(matcher.group(2));
		 
		 if(messageFlag.equals(Cancel_Request)) {
			 // cancels java processes and threads that are running this task.
			 cancelTasks(clientId);
			 
		 } else {

			 long requestTime = Long.parseLong(matcher.group(3));
			 if(!activeRequests.containsKey(clientId)) {
				 // client has not made a request before
				 activeRequests.put(clientId, requestTime);
			 }
			 Long activeRequestTime = activeRequests.get(clientId);
			 if(activeRequestTime < requestTime) {
//				 cancelTasks(clientId);
				 if(pythonPIds.get(clientId) != null) {
					 pythonPIds.put(clientId, null);
				 }
				 if(javaThreads.get(clientId) != null) {
					 javaThreads.put(clientId, null);
				 }
				 activeRequests.put(clientId, requestTime);
			 }
			 

			 if(messageFlag.equals(Java_Worker_Started)) {
				 if(javaServerThread == null) { 
					 logger.error("The Java server has to use the static method to notify the httpServiceObserver.");
					 // cannot yet handle java side http cancellation
					 // because there is no way to kill a java thread from outside of the process. (I think)
				 }
				 
				 if(javaThreads.get(clientId) == null) {
					 List<Thread> javaThreadList = new LinkedList<>();
					 javaThreads.put(clientId, javaThreadList);
					 
				 }
				 javaThreads.get(clientId).add(javaServerThread);
				 
			 } else if(messageFlag.equals(Python_Worker_Started)) {
				 long pythonPId = Long.parseLong(matcher.group(4));
				 if(pythonPIds.get(clientId) == null) {
					 List<Long> pythonProcesses = new LinkedList<>();
					 pythonPIds.put(clientId, pythonProcesses);
				 }
				 pythonPIds.get(clientId).add(pythonPId);
				 
			 }
		 }
	}
	
	private static void cancelTasks(Long clientId) throws IOException {
		if(javaThreads.containsKey(clientId)) {
			for(Thread javaServerThread : javaThreads.get(clientId)) {
				if(javaServerThread.isAlive()) {
					System.out.println("Interrupting " + javaServerThread);
					javaServerThread.interrupt();
			
				}
			}
			javaThreads.remove(clientId);
		}
		if(pythonPIds.containsKey(clientId)) {
			for(Long pythonPId : pythonPIds.get(clientId)) {
				// kill python prrocess
				try {
					Runtime.getRuntime().exec("kill -9 " + pythonPId);
					System.out.println("killed python process: "  + pythonPId);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("Can't kill python process: {} ", pythonPId);
				}
			}
			pythonPIds.remove(clientId);
		}
	}
	
	static HttpServer server;
	public static void StartServer(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 1000);
		server.createContext("/", new HttpServiceObserver());
		server.start();
		System.out.println("Server is up ...");
	}
	public static void CloseServer() {
		server.stop(0);
	}

	
}
