package AEV3;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GestorHTTP implements HttpHandler {

	// He tenido que crear 2 clases aparte para poder gestionar el json
	List<Criminales> lista = new ArrayList<Criminales>();

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String requestParamValue = null;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		guardarLog(httpExchange, timestamp);
		try {
			lista = rellenarListaCriminal();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		int caso = 0;
		if ("GET".equalsIgnoreCase(httpExchange.getRequestMethod())) {
			caso = asignarCaso(httpExchange);// Asigno los casos
			if (caso == 1) {
				handleGETResponseBuscarTodos(httpExchange);
			}
			if (caso == 2) {
				requestParamValue = handleGetRequest(httpExchange);
				handleGETResponseBuscarUno(httpExchange, requestParamValue);
			}
			if (caso == 3) {
				handleGETResponseError(httpExchange);
			}

		} else if ("POST".equalsIgnoreCase(httpExchange.getRequestMethod())) {
			if (correct(httpExchange)) {// Si la url contiene nuevo se ejecutara y si no enviara mensaje de error
				JSONObject request = null;
				try {
					request = handlePostRequest(httpExchange);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
				try {
					handlePostResponse(httpExchange, request);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				handlePOSTResponseError(httpExchange);
			}
		} else {
			System.out.println("DESCONOCIDA");
		}
	}

	/**
	 * Metodo que splitea despues del =
	 */
	private String handleGetRequest(HttpExchange httpExchange) {

		return httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
	}

	/**
	 * Metodo que asigna caso para gestionar que caso de get ejecutar
	 * 
	 * @autor Haitian
	 * @param httpExchange
	 * @return int retorna el caso
	 */
	private int asignarCaso(HttpExchange httpExchange) {
		String caso = httpExchange.getRequestURI().toString().split("/servidor/")[1];
		if (caso.contains("mostrarTodos")) {// Uso un contain aunque no es lo mas adecuado.Recomiendo usar el equal y
											// luego hacer split en mostarUno
			return 1;
		} else if (caso.contains("mostrarUno")) {
			return 2;
		} else {
			return 3;
		}
	}

	/**
	 * Metodo que rellena la lista de criminales
	 * 
	 * @return lista de criminales
	 */
	private List<Criminales> rellenarListaCriminal() throws IOException, ParseException {
		List<Criminales> a = new ArrayList<Criminales>();
		JSONParser parser = new JSONParser();
		FileReader reader = new FileReader("lista.json");
		Object obj = parser.parse(reader);
		JSONObject pJsonObj = (JSONObject) obj;
		JSONArray array = (JSONArray) pJsonObj.get("criminales");
		for (int i = 0; i < array.size(); i++) {
			JSONObject criminal = (JSONObject) array.get(i);

			String alias = (String) criminal.get("alias");
			String nombre = (String) criminal.get("nombreCompleto");
			String fecha = (String) criminal.get("fechaNacimiento");
			String nacional = (String) criminal.get("nacionalidad");
			String img = (String) criminal.get("imagen");

			a.add(new Criminales(alias, nombre, fecha, nacional, img));
		}
		return a;
	}

	/**
	 * Este metodo busca el criminal mendiante el alias y le envia la informacion al
	 * cliente
	 * 
	 * @author Haitian
	 * @param httpExchange
	 * @param requestParamValue
	 */
	private void handleGETResponseBuscarUno(HttpExchange httpExchange, String requestParamValue) throws IOException {
		System.out.println("Recibida URI tipo GET: " + httpExchange.getRequestURI().toString());
		String a = buscarCriminal(requestParamValue);
		String[] b = a.split("%");
		if (!"vacio".equals(a)) {
			OutputStream outputStream = httpExchange.getResponseBody();
			StringBuilder htmlBuilder = new StringBuilder();
			htmlBuilder.append("<html>").append("<body>").append("<h1>").append("Criminales de CNI ").append("</h1>")
					.append("<h3> Alias: " + b[0]).append("</h3>").append("<h3> Nombre: " + b[1]).append("</h3>")
					.append("<h3> Fecha: " + b[2]).append("</h3>").append("<h3> Nacionalidad: " + b[3]).append("</h3>")
					.append("<img src=\"data:image/png;base64, " + b[4] + "\" width=\"500\" height=\"600\"> ");
			String htmlResponse = htmlBuilder.toString();
			httpExchange.sendResponseHeaders(200, htmlResponse.length());

			outputStream.write(htmlResponse.getBytes());
			outputStream.flush();
			outputStream.close();
		} else {
			OutputStream outputStream = httpExchange.getResponseBody();
			String htmlResponse = "<html><body><h1>criminal no registrado</h1></body></html>";
			httpExchange.sendResponseHeaders(200, htmlResponse.length());
			outputStream.write(htmlResponse.getBytes());
			outputStream.flush();
			outputStream.close();
		}

	}

	/**
	 * Busca en el json y retorna todops los alia
	 * 
	 * @param httpExchange
	 */
	private void handleGETResponseBuscarTodos(HttpExchange httpExchange) throws IOException {
		System.out.println("Recibida URI tipo GET: " + httpExchange.getRequestURI().toString());
		OutputStream outputStream = httpExchange.getResponseBody();
		String htmlResponse = "<html><body><h1>Lista criminales</h1><ul>";
		for (int i = 0; i < lista.size(); i++) {
			htmlResponse += "<li>" + lista.get(i).getAlias() + "</li>";
		}
		htmlResponse += "</ul></body></html>";
		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();
	}

	/**
	 * Envia un mensaje de error al cliente
	 * 
	 * @param httpExchange
	 */
	private void handleGETResponseError(HttpExchange httpExchange) throws IOException {
		OutputStream outputStream = httpExchange.getResponseBody();
		String htmlResponse = "<html><body><h1>Ruta incorrecta</h1></body></html>";
		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();
	}

	/**
	 * Este metodo busca el criminal apartir del alias
	 * 
	 * @return retorna el string con toda la informacion
	 */
	private String buscarCriminal(String alias) {
		String a = "";
		for (int i = 0; i < lista.size(); i++) {
			if (lista.get(i).getAlias().equals(alias)) {// Uso separadores % porque hay caracteres especiales que da
														// problemas
				a = lista.get(i).getAlias() + "%" + lista.get(i).getNombreCompleto() + "%"
						+ lista.get(i).getFechaNacimiento() + "%" + lista.get(i).getNacionalidad() + "%"
						+ lista.get(i).getImagen();
			}
		}
		if (a != "") {
			//System.out.println(a);
			return a;
		} else {
			System.out.println("vacio");
			return "vacio";
		}

	}

	/**
	 * Busca si la URI contiene nuevo
	 * 
	 * @return retorna true o false dependiendo de la URI
	 */
	private boolean correct(HttpExchange httpExchange) {
		String caso = httpExchange.getRequestURI().toString().split("/servidor/")[1];
		if (caso.contains("nuevo")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Recibe y request body y lo transforma en un objeto json
	 * 
	 * @return devuelve el objeto json
	 */
	private JSONObject handlePostRequest(HttpExchange httpExchange)
			throws UnsupportedEncodingException, IOException, ParseException {
		System.out.println("Recibida URI tipo POST: " + httpExchange.getRequestBody().toString());

		InputStream inputStream = httpExchange.getRequestBody();
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		System.out.println(jsonObject);
		return jsonObject;
	}

	/**
	 * Este metodo sobre escribe el fichero json con los nuevos criminales + los
	 * antiguos Y envia al cliente un mensaje de insertado con exito
	 * El envio de email te pide los parametros por teclado
	 * @author Haitian
	 * @param httpExchange
	 * @param requestParamValue recibe un parametro Jsonobject
	 * @throws MessagingException 
	 */
	private void handlePostResponse(HttpExchange httpExchange, JSONObject requestParamValue) throws IOException, MessagingException {

		OutputStream outputStream = httpExchange.getResponseBody();
		String htmlResponse = "<html><body><h3>Insertado con exito</h3></body></html>";
		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();
		System.out.println("El servidor devuelve codigo 200");
		String alias = (String) requestParamValue.get("alias");
		String nombre = (String) requestParamValue.get("nombreCompleto");
		// System.out.println("nombreCompleto: "+nombre);
		String fecha = (String) requestParamValue.get("fechaNacimiento");
		// System.out.println("Edad: "+edad);
		// System.out.println("Tipo de dato:
		// "+((Object)edad).getClass().getSimpleName());
		String nacionalidad = (String) requestParamValue.get("nacionalidad");
		String imagen = (String) requestParamValue.get("imagen");
		Criminales te = new Criminales(alias, nombre, fecha, nacionalidad, imagen);
		lista.add(te);
		Gson gson = new Gson();
		GestorJson Gjson = new GestorJson(lista);
		String json = gson.toJson(Gjson);

		System.out.println(json);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter("lista.json"))) {
			bw.write(json);
			System.out.println("Fichero creado");
		} catch (IOException ex) {
			System.out.println("Error al insertar");
		}
		
		//El envio de email te lo pide por teclado
		Scanner sc = new Scanner(System.in);
		System.out.print("Introduce el tipo de host:");
		String host=sc.nextLine();
		System.out.print("Introduce remitente:");
		String remi=sc.nextLine();
		System.out.print("Introduce la contraseña:");
		String cont=sc.nextLine();
		System.out.print("Introduce el email destinatario:");
		String dest=sc.nextLine();
		String asunto="Registro criminal";
		String mensaje=" Alias: "+alias+"\n Nombre: "+nombre+"\n Fecha Nacimiento: "+fecha+"\n Nacionalidad: "+nacionalidad+"\n Imagen en base 64:"+imagen;
		envioMail(host,remi,cont,mensaje,asunto,dest);
	}

	/**
	 * Este metodo solo envia al cliente el mensaje d que se equivoco de ruta
	 */
	private void handlePOSTResponseError(HttpExchange httpExchange) throws IOException {
		OutputStream outputStream = httpExchange.getResponseBody();
		String htmlResponse = "<html><body><h1>Ruta incorrecta</h1></body></html>";
		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();
	}

	/**
	 * Este metodo escribe un fichero log.txt con el ip y el timestamp del cliente
	 * 
	 * @author Haitian
	 * @param httpExchange
	 * @param fecha
	 */
	public void guardarLog(HttpExchange httpExchange, Timestamp fecha) throws IOException {
		File fichero = new File("logs.txt");
		InetSocketAddress address = httpExchange.getRemoteAddress();
		System.out.println("log: " + address);
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (fichero.createNewFile()) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fichero));
			bw.write("Ip: " + address + "   Timestamp: " + fecha + "\n");
			bw.close();
		} else {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fichero, true));
			bw.write("Ip: " + address + "   Timestamp: " + sdf3.format(fecha) + "\n");
			bw.close();
		}
	}

	/**
	 * Metodo para enviar correo
	 * @param host tipo de host
	 * @param remitente
	 * @param contraseña
	 * @param mensaje
	 * @param asunto
	 * @param destinatario
	 * 
	 * */
	public static void envioMail(String host,String remitente,String contrasena,String mensaje, String asunto, String email_destino)
			throws UnsupportedEncodingException, MessagingException, FileNotFoundException {
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", remitente);
		props.put("mail.smtp.clave", contrasena);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", 587);
		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(remitente));
		message.addRecipients(Message.RecipientType.TO, email_destino);
		message.setSubject(asunto);
		BodyPart messageBodyPart1 = new MimeBodyPart();
		messageBodyPart1.setText(mensaje);
		BodyPart messageBodyPart2 = new MimeBodyPart();
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart1);
		multipart.addBodyPart(messageBodyPart2);
		message.setContent(multipart);
		Transport transport = session.getTransport("smtp");
		transport.connect(host, remitente, contrasena);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}
}
