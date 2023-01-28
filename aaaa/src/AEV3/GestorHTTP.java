package AEV3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.text.StringEscapeUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GestorHTTP implements HttpHandler {

	MongoCollection<Document> coleccion=null;
	
	

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String requestParamValue = null;
		int caso = 0;
		if ("GET".equals(httpExchange.getRequestMethod())) {
			caso = asignarCaso(httpExchange);
			conexio();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			requestParamValue = handleGetRequest(httpExchange);
			handleGETResponse(httpExchange,requestParamValue);
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

	private void handleGETResponse(HttpExchange httpExchange,String requestParamValue) throws IOException {
		Bson query = Filters.eq("alias", requestParamValue);
		MongoCursor<Document> cursor = coleccion.find(query).iterator();
		String alias="Error";
		String nombre="Error";
		String nacionalidad="Error";
		String imag="Error";
		int any=0;
        while (cursor.hasNext()) {
            JSONObject obj = new JSONObject(cursor.next().toJson());
            alias=obj.getString("alias");
            System.out.println("Alias: :"+alias);
            nombre=obj.getString("nombreCompleto");
            any=obj.getInt("fechaNacimiento");
            nacionalidad=obj.getString("nacionalidad");
            imag=obj.getString("imagen");
        }
        
//		System.out.println("Recibida URI tipo GET: " + httpExchange.getRequestURI().toString());
//		OutputStream outputStream = httpExchange.getResponseBody();
//		String htmlResponse = "<html><body><h3>Alias: " + alias + "</h3><h3>Nombre: " + nombre + "</h3></body></html>";
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
				.append("<img src=\"data:image/png;base64, " + imag + "\" width=\"500\" height=\"600\"> ");
				
		// encode HTML content
		String htmlResponse = htmlBuilder.toString();
		// this line is a must
		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();
		System.out.println(imag);
	}
	
	public void conexio() {
		String ip="haitian:haitian@3.226.136.98";
		int port=27017;
		MongoClientURI uri= new MongoClientURI("mongodb://"+ip+":"+port);
		MongoClient mongoClient =new MongoClient(uri);
		MongoDatabase database = mongoClient.getDatabase("CNI");
		coleccion = database.getCollection("Criminales");
		if(coleccion!=null) {
			System.out.println("Conectado con exito");
		}else {
			System.out.println("Fallo al conectar");
		}
		

	}
	

	
	
	private String handlePostRequest(HttpExchange httpExchange) {
		System.out.println("Recibida URI tipo POST: " + httpExchange.getRequestBody().toString());
		InputStream is = httpExchange.getRequestBody();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	private void handlePostResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {

		System.out.println("El servidor pasa a procesar el body de la peticion POST: " + requestParamValue);

		// Opcion 1: si queremos que el servidor devuelva al cliente un HTML:
		// OutputStream outputStream = httpExchange.getResponseBody();
		// String htmlResponse = "Parametro/s POST: " + requestParamValue + " -> Se
		// procesara por parte del servidor";
		// outputStream.write(htmlResponse.getBytes());
		// outputStream.flush();
		// outputStream.close();
		// System.out.println("Devuelve respuesta HTML: " + htmlResponse);
		// httpExchange.sendResponseHeaders(200, htmlResponse.length());
		// System.out.println("Devuelve respuesta HTML: " + htmlResponse);

		// Opcion 2: el servidor devuelve al cliente un codigo de ok pero sin contenido
		// HTML
		httpExchange.sendResponseHeaders(204, -1);
		System.out.println("El servidor devuelve codigo 204");

		// TODO: a partir de aqui todas las operaciones que se quieran programar en el
		// servidor cuando recibe
		// una peticion POST (ejemplo: insertar en una base de datos lo que nos envia el
		// cliente en requestParamValue)

		// NOTA: se puede incluir tambien un punto de control antes de enviar el codigo
		// resultado de la
		// operacion en el header (httpExchange.sendResponseHeaders(CODIGOHTTP, {})).
		// Por ejemplo, si
		// hay un error se enviarian codigos del tipo 400, 401, 403, 404, etc.
		// https://developer.mozilla.org/es/docs/Web/HTTP/Status

	}
	
}
