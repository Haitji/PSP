package TresEnRaya;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

public class ThreadJuego extends Thread {
	Socket serverClient;
	int clientNo;

	ThreadJuego(Socket inSocket) {
		serverClient = inSocket;
	}

	//Atributos
	String[] tablero = { "", "", "", "", "", "", "", "", "" };

	
	/**
	 * Este método recibe el par o impar del cliente y comprueba quien empieza 
	 * @return boolean Retorna true o false dependiendo de si el cliente ha acertado o no
	 * */
	private boolean inicio() {
		boolean par = false;
		int numeroAleatorio = (int) (Math.random() * 2 + 1);
		if (numeroAleatorio % 2 == 0) {
			par = true;
		}
		InputStream is;
		String parImpar = "";
		try {
			is = serverClient.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader bf = new BufferedReader(isr);
			parImpar = bf.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (parImpar.toUpperCase().equals("PAR") && par == true) {
			JOptionPane.showMessageDialog(null, "Acertastes");
			return true;
		} else if (parImpar.toUpperCase().equals("IMPAR") && par == false) {
			JOptionPane.showMessageDialog(null, "Acertastes");
			return true;
		} else {
			JOptionPane.showMessageDialog(null, "Fallastes");
			return false;
		}
	}

	/**
	 * Este método recibe la posicion y actualiza el array tablero
	 * @param pos Recibe la posición
	 * */
	private void recibirPosicion(int pos) {
		tablero[pos] = "X";
		comprobar();
	}

	
	/**
	 * Este método busca una casilla vacia
	 * @return int Retorna la casilla vacia
	 * */
	private int pintar() {
		int casilla = (int) (Math.random() * 8);
		while (tablero[casilla] != "") {
			casilla = (int) (Math.random() * 8);
		}
		if (tablero[casilla].equals("")) {
			tablero[casilla] = "O";
		}
		return casilla;
	}

	/**
	 * Este método comprueba si el array esta lleno o no 
	 * @return boolean Retorna true o false
	 * */
	private boolean comprobar() {
		List<String> list = Arrays.asList(tablero);
		if (list.contains("")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Aqui generamos el hilo
	 * */
	public void run() {
		try {
			InputStream is = serverClient.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader bf = new BufferedReader(isr);
			OutputStream os = serverClient.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			String clientTurn = "";
				if (inicio()) {//Comprueba quien empieza
					while (true) {//Siempre estara en funcionamiento, porque no hice un comprobar ganador en hilo
						//Bucle infinito de recibir y enviar numeros.
						clientTurn = bf.readLine();
						System.out.println("Posicion cliente" + clientTurn);
						recibirPosicion(Integer.valueOf(clientTurn));
						int pos = pintar();
						pw.write(String.valueOf(pos) + "\n");
						pw.flush();
					}
				} else {
					while (true) {
						int pos = pintar();
						pw.write(String.valueOf(pos) + "\n");
						pw.flush();
						clientTurn = bf.readLine();
						System.out.println("Posicion cliente" + clientTurn);
						recibirPosicion(Integer.valueOf(clientTurn));

					}
				}

			
//			serverClient.close();
		} catch (Exception ex) {
			System.out.println(ex);
		} finally {
			System.out.println("Client -" + clientNo + " exit!! ");
		}
		System.out.println(tablero);
	}
}
