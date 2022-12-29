package TresEnRaya;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Cliente extends JFrame {
	
	//Atributos 
	JFrame frame;
	JPanel panel;
	JLabel lbl1;
	JLabel lbl2;
	JLabel lbl3;
	JLabel lbl4;
	JLabel lbl5;
	JLabel lbl6;
	JLabel lbl7;
	JLabel lbl8;
	JLabel lbl9;
	String turno = "X";
	JLabel lbs[] = new JLabel[9];
	boolean ganador = false;
	int va[][] = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }, { 1, 4, 7 }, { 2, 5, 8 }, { 3, 6, 9 }, { 1, 5, 9 },
			{ 3, 5, 7 } };//Estados ganadores

	
	/**
	 * Constructor
	 * @param socket Recibimos el socket sobre el cual actual
	 * */
	public Cliente(Socket socket) {
		initial();
		lbs[0] = lbl1;
		lbs[1] = lbl2;
		lbs[2] = lbl3;
		lbs[3] = lbl4;
		lbs[4] = lbl5;
		lbs[5] = lbl6;
		lbs[6] = lbl7;
		lbs[7] = lbl8;
		lbs[8] = lbl9;
		InitEventHandler(socket);
	}


//	private boolean lleno() {
//		List<JLabel> list = Arrays.asList(lbs);
//		if (list.contains("")) {
//			ganador=false;
//		} else {
//			ganador=true;
//		}
//		return true;
//	}

	/**
	 * Este método pinta la casilla y envia el numero al servidor
	 * @param socket Recibe el socket sobre el cual actual
	 * @param casilla Recibe la casilla que debe pintar
	 * */
	private void presionar(Socket socket, int casilla) {
		if (!ganador) {
			if (lbs[casilla].getText().equals("")) {
				lbs[casilla].setText(turno);
				enviarNumero(socket, String.valueOf(casilla));
				comprobarGanador();//Compruebas si alguien ha ganado
				cambiarTurno();//Cambias de turno
			}
		}
	}

	/**
	 * Sobrecarga del método anterior que solo pinta 
	 * @param socket Recibe el socket sobre el cual actual
	 * @param casilla Recibe la casilla que debe pintar
	 * */
	private void presionar(int casilla) {
		if (!ganador) {
			if (lbs[casilla].getText().equals("")) {
				lbs[casilla].setText(turno);
				comprobarGanador();
				cambiarTurno();
			}
		}
	}

	/**
	 * Este método cambia de turno, de X y O 
	 * */
	private void cambiarTurno() {
		if (turno.equals("X")) {
			turno = "O";
		} else {
			turno = "X";
		}
	}

	/**
	 * Este método contiene los mouseListeners sobre el cual ejecuta presionar
	 * @param socket Recibe un socket para pasarselo a presionar
	 * */
	public void InitEventHandler(Socket socket) {
		lbl1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				presionar(socket, 0);
			}
		});
		lbl2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				presionar(socket, 1);
			}
		});
		lbl3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				presionar(socket, 2);
			}
		});
		lbl4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				presionar(socket, 3);
			}
		});
		lbl5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				presionar(socket, 4);
			}
		});
		lbl6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				presionar(socket, 5);
			}
		});
		lbl7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				presionar(socket, 6);
			}
		});
		lbl8.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				presionar(socket, 7);
			}
		});
		lbl9.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				presionar(socket, 8);
			}
		});
	}

	
	/**
	 * Este método recbe el numero del servidor y activa presionar
	 * @param socket Recibe un socket sobre el que actual
	 * */
	public void recibirNumero(Socket socket) {
		InputStream is;
		try {
			is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader bf = new BufferedReader(isr);
			String turnoServidor = bf.readLine();
			if(!ganador) {//Si hay ganador no hace nada
				presionar(Integer.valueOf(turnoServidor));//Ejecuta presionar con el numero recibido del servidor
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Este método ejecuta presionar y envia el numero al servidor
	 * @param socket Recibe el socket sobre el cual actual
	 * @param num Recibe el numero que enviar al servidor
	 * */
	public void enviarNumero(Socket socket, String num) {
		OutputStream os;
		try {
			os = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			pw.write(String.valueOf(num) + "\n");
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Este método envia par o impar al servidor
	 * @param socket Recibe el socket sobre el cual actual
	 * */
	public void enviarParImpar(Socket socket) {
		String[] opciones = { "PAR", "IMPAR" };
		String tabla = (String) JOptionPane.showInputDialog(null, "Quien empieza", "Par o Impar",
				JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);//Si no selecionas nada o le das a cancelar se envia un 0
		OutputStream os;
		try {
			os = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			pw.write(String.valueOf(tabla) + "\n");
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Este método recorre la matriz y compara con el estado ganador para saber si aguien ha ganado
	 * @return boolean Retorna true o false si ha ganado alguien
	 */
	private boolean comprobarGanador() {
		for (int i = 0; i < va.length; i++) {
			if (lbs[va[i][0] - 1].getText().equals("X") && lbs[va[i][1] - 1].getText().equals("X")
					&& lbs[va[i][2] - 1].getText().equals("X")) {
				lbs[va[i][0] - 1].setBackground(Color.green);
				lbs[va[i][1] - 1].setBackground(Color.green);
				lbs[va[i][2] - 1].setBackground(Color.green);
				ganador = true;
				JOptionPane.showMessageDialog(null, "Gana X");
				return true;
			}
			if (lbs[va[i][0] - 1].getText().equals("O") && lbs[va[i][1] - 1].getText().equals("O")
					&& lbs[va[i][2] - 1].getText().equals("O")) {
				lbs[va[i][0] - 1].setBackground(Color.green);
				lbs[va[i][1] - 1].setBackground(Color.green);
				lbs[va[i][2] - 1].setBackground(Color.green);
				ganador = true;
				JOptionPane.showMessageDialog(null, "Gana O");
				return true;
			}
		}
		return false;
	}

	/**
	 * Este metodo es la interfaz
	 */

	public void initial() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 422, 436);
		frame.getContentPane().setLayout(null);

		panel = new JPanel();
		panel.setForeground(new Color(255, 255, 255));
		panel.setBackground(Color.BLUE);
		panel.setBounds(90, 113, 200, 200);
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		lbl1 = new JLabel("");
		lbl1.setHorizontalAlignment(SwingConstants.CENTER);
		lbl1.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.BOLD, 48));
		lbl1.setOpaque(true);
		lbl1.setBackground(new Color(255, 255, 255));
		lbl1.setBounds(0, 0, 60, 60);
		panel.add(lbl1);

		lbl2 = new JLabel("");
		lbl2.setOpaque(true);
		lbl2.setHorizontalAlignment(SwingConstants.CENTER);
		lbl2.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.BOLD, 48));
		lbl2.setBackground(Color.WHITE);
		lbl2.setBounds(70, 0, 60, 60);
		panel.add(lbl2);

		lbl3 = new JLabel("");
		lbl3.setOpaque(true);
		lbl3.setHorizontalAlignment(SwingConstants.CENTER);
		lbl3.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.BOLD, 48));
		lbl3.setBackground(Color.WHITE);
		lbl3.setBounds(140, 0, 60, 60);
		panel.add(lbl3);

		lbl4 = new JLabel("");
		lbl4.setOpaque(true);
		lbl4.setHorizontalAlignment(SwingConstants.CENTER);
		lbl4.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.BOLD, 48));
		lbl4.setBackground(Color.WHITE);
		lbl4.setBounds(0, 70, 60, 60);
		panel.add(lbl4);

		lbl5 = new JLabel("");
		lbl5.setOpaque(true);
		lbl5.setHorizontalAlignment(SwingConstants.CENTER);
		lbl5.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.BOLD, 48));
		lbl5.setBackground(Color.WHITE);
		lbl5.setBounds(70, 70, 60, 60);
		panel.add(lbl5);

		lbl6 = new JLabel("");
		lbl6.setOpaque(true);
		lbl6.setHorizontalAlignment(SwingConstants.CENTER);
		lbl6.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.BOLD, 48));
		lbl6.setBackground(Color.WHITE);
		lbl6.setBounds(140, 70, 60, 60);
		panel.add(lbl6);

		lbl7 = new JLabel("");
		lbl7.setOpaque(true);
		lbl7.setHorizontalAlignment(SwingConstants.CENTER);
		lbl7.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.BOLD, 48));
		lbl7.setBackground(Color.WHITE);
		lbl7.setBounds(0, 140, 60, 60);
		panel.add(lbl7);

		lbl8 = new JLabel("");
		lbl8.setOpaque(true);
		lbl8.setHorizontalAlignment(SwingConstants.CENTER);
		lbl8.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.BOLD, 48));
		lbl8.setBackground(Color.WHITE);
		lbl8.setBounds(70, 140, 60, 60);
		panel.add(lbl8);

		lbl9 = new JLabel("");
		lbl9.setOpaque(true);
		lbl9.setHorizontalAlignment(SwingConstants.CENTER);
		lbl9.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.BOLD, 48));
		lbl9.setBackground(Color.WHITE);
		lbl9.setBounds(140, 140, 60, 60);
		panel.add(lbl9);
		this.frame.setVisible(true);
	}

	public static void main(String[] args) {
		Cliente t; 
		try {
			Socket socket = new Socket("127.0.0.1", 8888);
			t = new Cliente(socket);
			t.enviarParImpar(socket);
			while (!t.ganador) {//Bucle de recibir y enviar numeros
				t.recibirNumero(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
