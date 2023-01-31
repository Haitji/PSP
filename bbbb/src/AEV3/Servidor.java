package AEV3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;



public class Servidor {
	public static void main(String[] args) throws IOException {
		String host = "localhost"; //127.0.0.1
		int puerto = 5001;
		InetSocketAddress direccionTCPIP = new InetSocketAddress(host,puerto);
		int backlog = 0;
		HttpServer servidor = HttpServer.create(direccionTCPIP, backlog);
		GestorHTTP gestorHTTP = new GestorHTTP();
		String mostrarTodos = "/servidor";
		HttpContext ctxTodo=servidor.createContext(mostrarTodos);
		ctxTodo.setHandler(gestorHTTP);
		
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
		servidor.setExecutor(threadPoolExecutor);
		servidor.start();
		System.out.println("Servidor HTTP arranca en el puerto " + puerto);
		

	}
}
