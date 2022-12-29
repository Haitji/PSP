package TresEnRaya;

import java.net.ServerSocket;
import java.net.Socket;


public class Servidor {

	/**
	 * Aqui se lanza los hilos cada vez que un cliente se conecta
	 * */
	public static void main(String[] args) {

		try {
			ServerSocket server = new ServerSocket(8888);
			int counter = 0;
			System.out.println("Server Started ....");
			while (true) {//Bucle infinito de lanzar hilos
				counter++;
				Socket serverClient = server.accept(); 
				System.out.println(" >> " + "Client No:" + counter + " started!");
				ThreadJuego sct = new ThreadJuego(serverClient); 																						
				sct.start();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
