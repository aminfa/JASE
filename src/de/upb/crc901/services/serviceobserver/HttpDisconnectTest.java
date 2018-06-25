package de.upb.crc901.services.serviceobserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpDisconnectTest implements HttpHandler {
	

	@Override
	public void handle(HttpExchange t) throws IOException {
		int i = 10;
		while( i > 0) {
			i--;
			try {
				Thread.sleep(300);
				System.out.println("Still calculating");
			} catch (InterruptedException e) {
			 	e.printStackTrace();
			} 
		}
		System.out.println("Done calculating:");
		t.sendResponseHeaders(200, "done".getBytes().length);
		System.out.println("Wrote headers");
		OutputStream stream = t.getResponseBody();
		t.getResponseBody().write("done".getBytes());
		System.out.println("Wrote body");
	}
	static HttpServer server;
	static void StartServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(8000), 100);
		

        // Set an Executor for the multi-threading
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(16));
        
		server.createContext("/", new HttpDisconnectTest());
		server.start();
		System.out.println("Server is up ...");
	}
	static void CloseServer() {
		System.exit(0);
	}
	
	// CLIENT
	public static void main(String[]a) throws IOException, InterruptedException {
//		URL url = new URL("http://localhost:5000/test");
		
		URL url = new URL("http://localhost:8000/test");
		StartServer();
		
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setConnectTimeout(2000);
		con.setChunkedStreamingMode(1<<20); // 1 MByte buffer
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		System.out.println("Sending data started");
		/* send data */
		OutputStream out = con.getOutputStream();
		System.out.println("Sending data concluded");
		for(byte i = 1; i <= 10; i++) {
			out.write(i);
			Thread.sleep(50);
		}
		
		out.close();
		System.out.println("Sent data; now start sleeping.");
		Thread.sleep(500);
//		out.close();
		System.out.println("Woken up; now disconnect.");
		con.disconnect();
//		out.close();
		System.out.println("Disconnected; now reading return stream.");
		try {
			int responseCode = con.getResponseCode();
			if(responseCode == 200) {
				try (InputStream in = con.getInputStream()){
					String returnString = IOUtils.toString(in, Charset.defaultCharset()); 
					System.out.println("Server Returned: " + returnString);
				}catch(IOException ex) {
					ex.printStackTrace();
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		} finally {
			CloseServer();
		}
	}
}
