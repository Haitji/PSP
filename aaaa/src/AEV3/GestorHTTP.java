package AEV3;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.text.StringEscapeUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GestorHTTP implements HttpHandler {

	 String alias;
	 String nombre;
	 int any;
	 String nacionalidad;
	 String imagen;
	MongoCollection<Document> coleccion=null;
	
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String requestParamValue = null;
		int caso = 0;
		if ("GET".equals(httpExchange.getRequestMethod())) {
			caso = asignarCaso(httpExchange);
			conexio();
			requestParamValue = handleGetRequest(httpExchange);
			buscarCriminal(requestParamValue);
			handleGETResponse(httpExchange);
		} else if ("POST".equals(httpExchange.getRequestMethod())) {
			// requestParamValue = handlePostRequest(httpExchange);
			// handlePOSTResponse(httpExchange, requestParamValue);
		} else {
			System.out.println("DESCONOCIDA");
		}
	}

	private String handleGetRequest(HttpExchange httpExchange) {
		System.out.println(httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1]);
		return httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
	}

	private int asignarCaso(HttpExchange httpExchange) {
		int caso = httpExchange.getRequestURI().toString().split("/").length;
		System.out.println("Caso=============" + caso);
		return caso;
	}

	private void handleGETResponse(HttpExchange httpExchange) throws IOException {
//		System.out.println("Recibida URI tipo GET: " + httpExchange.getRequestURI().toString());
//		OutputStream outputStream = httpExchange.getResponseBody();
//		String htmlResponse = "<html><body>Hola a a" + requestParamValue + "</body></html>";
//		httpExchange.sendResponseHeaders(200, htmlResponse.length());
//		outputStream.write(htmlResponse.getBytes());
//		outputStream.flush();
//		outputStream.close();
		

		OutputStream outputStream = httpExchange.getResponseBody();
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<html>")
				.append("<body>")
				.append("<h1>")
				.append("Criminales de CNI ")
				.append("</h1>")
				.append("<h3> Alias: "+alias)
				.append("</h3>")
				.append("<h3> Nombre: "+nombre)
				.append("</h3>")
				.append("<h3> Fecha: "+any)
				.append("</h3>")
				.append("<h3> Nacionalidad: "+nacionalidad)
				.append("</h3>")
				.append("</body>")
				.append("</html>");
		// encode HTML content
		String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
		// this line is a must
		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();
	}
	
	public void conexio() {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase database = mongoClient.getDatabase("CNI");
		coleccion = database.getCollection("Criminales");
		if(coleccion!=null) {
			System.out.println("Conectado con exito");
		}else {
			System.out.println("Fallo al conectar");
		}
	}
	
	public void buscarCriminal(String a) {
		Bson query = Filters.eq("alias", a);
		MongoCursor<Document> cursor = coleccion.find(query).iterator();
        while (cursor.hasNext()) {
            JSONObject obj = new JSONObject(cursor.next().toJson());
            alias=obj.getString("alias");
            System.out.println("Alias: :"+alias);
            nombre=obj.getString("nombreCompleto");
            System.out.println("Alias: :"+nombre);
       	 	any=obj.getInt("fechaNaciemiento");
       	 System.out.println("Alias: :"+any);
       	 	nacionalidad=obj.getString("nacionalidad");
       	 System.out.println("Alias: :"+nacionalidad);
        }
        //System.out.println("Buscar a :"+a);
	}
}
