import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class CommunicationSpeed {
	
	private final int port = 8080;
	
	class JavaClassHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange t) throws IOException {
			Map<String, Object> post = parsePostParameters(t);
//			System.out.println(post.get);
			
			String response = "";
			
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	@Test
	public void test() throws IOException {
		
		/* create server */
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/", new JavaClassHandler());
		server.start();
		
		/* create large object */
		String base = "";
		Random r = new Random();
		for (int i = 0; i < 20; i++) {
			base += r.nextLong() + "\n";
			base += base;
		}
		List<String> sb = new ArrayList<>();
		for (int i = 0; i < 20; i++)
			sb.add(base);
		
		/* send http request to server over 127.0.0.1 sending */
		URL url = new URL("http://127.0.0.1:" + port);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");

		/* send data */
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes("object=");
		for (String s : sb)
			wr.writeBytes(URLEncoder.encode(s, "UTF-8"));
		wr.flush();
		wr.close();
		
		/* read and return answer */
		InputStream in = con.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String curline;
		StringBuilder content = new StringBuilder();
		while ((curline = br.readLine()) != null) {
			content.append(curline + '\n');
		}
		br.close();
		con.disconnect();
	}

	private Map<String, Object> parsePostParameters(HttpExchange exchange) throws IOException {
		if ((!"post".equalsIgnoreCase(exchange.getRequestMethod())))
			throw new UnsupportedEncodingException("No post request");
		Map<String, Object> parameters = new HashMap<String, Object>();
		BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "utf-8"));
		String query = br.readLine();
		System.out.println(query);
		parseQuery(query, parameters);
		return parameters;
	}
	

	@SuppressWarnings("unchecked")
	public void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

		if (query != null) {
			String pairs[] = query.split("[&]");
			for (String pair : pairs) {
				String param[] = pair.split("[=]");
				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
				}

				if (param.length > 1) {
					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
				}
				
				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if (obj instanceof List<?>) {
						List<String> values = (List<String>) obj;
						values.add(value);

					} else if (obj instanceof String) {
						List<String> values = new ArrayList<String>();
						values.add((String) obj);
						values.add(value);
						parameters.put(key, values);
					}
				} else {
					parameters.put(key, value);
				}
			}
		}
	}


}
