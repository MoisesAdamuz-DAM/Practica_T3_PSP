package es.studium.Chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClienteChar extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	Socket socket;
	DataInputStream fentrada;
	DataOutputStream fsalida;
	String nombre;
	static JTextField mensaje = new JTextField();
	private JScrollPane scrollpane;
	static JTextArea textarea;
	JButton boton = new JButton("Enviar");
	JButton desconectar = new JButton("Salir");
	boolean repetir = true;

public ClienteChar(Socket socket, String nombre)
{
	// Prepara la pantalla. Se recibe el socket creado y el nombre del cliente
	super(" Conexi�n del cliente chat: " + nombre);
	setLayout(null);
	mensaje.setBounds(10, 10, 400, 30);
	add(mensaje);
	textarea = new JTextArea();
	scrollpane = new JScrollPane(textarea);
	scrollpane.setBounds(10, 50, 400, 300);
	add(scrollpane);
	boton.setBounds(420, 10, 100, 30);
	add(boton);
	desconectar.setBounds(420, 50, 100, 30);
	add(desconectar);
	textarea.setEditable(false);
	boton.addActionListener(this);
	this.getRootPane().setDefaultButton(boton);
	desconectar.addActionListener(this);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.socket = socket;
	this.nombre = nombre;
	// Se crean los flujos de entrada y salida.
	// En el flujo de salida se escribe un mensaje
	// indicando que el cliente se ha unido al Chat.
	// El HiloServidor recibe este mensaje y
	// lo reenv�a a todos los clientes conectados
	try
	{
	fentrada = new DataInputStream(socket.getInputStream());
	fsalida = new DataOutputStream(socket.getOutputStream());
	//Env�a mensaje de conexci�n al cliente
	String texto = "SERVIDOR> Entra en el chat... " + nombre;
	fsalida.writeUTF(texto);
	}
	catch (IOException ex)
	{
	System.out.println("Error de E/S");
	ex.printStackTrace();
	System.exit(0);
	}
	}

//El m�todo main es el que lanza el cliente,
//para ello en primer lugar se solicita el nombre o nick del
//cliente, una vez especificado el nombre
//se crea la conexi�n al servidor y se crear la pantalla del Chat(ClientChat)
//lanzando su ejecuci�n (ejecutar()).
	public static void main(String[] args) throws Exception {
		int puerto = 44444;
		String nombre = JOptionPane.showInputDialog("Introduce tu nombre o nick:");
		Socket socket = null;
		try {
			
			socket = new Socket("127.0.0.1", puerto);
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(),
					"<<Mensaje de Error:1>>", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if (!nombre.trim().equals("")) {
			ClienteChar cliente = new ClienteChar(socket, nombre);
			cliente.setBounds(0, 0, 540, 400);
			cliente.setVisible(true);
			cliente.ejecutar();
		} else {
			System.out.println("El nombre est� vac�o...");
		}
	}

//Cuando se pulsa el bot�n Enviar,
//el mensaje introducido se env�a al servidor por el flujo de salida
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == boton) {
			String texto = nombre + "> " + mensaje.getText();
			try {
				mensaje.setText("");
				fsalida.writeUTF(texto);
				//Bloqueamos el bot�n una vez pulsado 
				boton.setEnabled(false);
				//El bot�n permanecer� bloqueado 3 segundos
				Thread.sleep(3000);
				//Pasado esos 3 segundos, el bot�n se desbloquear�
				boton.setEnabled(true);
			} catch (IOException  | InterruptedException ex) {
				ex.printStackTrace();
			}
		}
// Si se pulsa el bot�n Salir,
// se env�a un mensaje indicando que el cliente abandona el chat
// y tambi�n se env�a un * para indicar
// al servidor que el cliente se ha cerrado
		else if (e.getSource() == desconectar) {
			String texto = "SERVIDOR> Abandona el chat... " + nombre;
			try {
				fsalida.writeUTF(texto);
				fsalida.writeUTF("*");
				repetir = false;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

// Dentro del m�todo ejecutar(), el cliente lee lo que el
// hilo le manda (mensajes del Chat) y lo muestra en el textarea.
// Esto se ejecuta en un bucle del que solo se sale
// en el momento que el cliente pulse el bot�n Salir
// y se modifique la variable repetir
	public void ejecutar() {
		String texto = "";
		while (repetir) {
			try {
				texto = fentrada.readUTF();
				textarea.setText(texto);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Imposible conectar co el servidor \n" + ex.getMessage(),
						"<<Mensaje de Error:2>>", JOptionPane.ERROR_MESSAGE);
				repetir = false;
			}
		}
		try {
			socket.close();
			System.exit(0);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

