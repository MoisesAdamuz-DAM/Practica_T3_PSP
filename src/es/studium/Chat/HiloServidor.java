package es.studium.Chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HiloServidor extends Thread {
	//Recibe informacion
	DataInputStream fentrada;
	Socket socket;
	//Sirve para ver cuando tenemos que acabar
	boolean fin = false;

	public HiloServidor(Socket socket) {
		this.socket = socket;
		try {
			fentrada = new DataInputStream(socket.getInputStream());
		}

		catch (IOException e) {
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}

// En el m�todo run() lo primero que hacemos
// es enviar todos los mensajes actuales al cliente que se
// acaba de incorporar
	public synchronized void run() {
		ServidorChat.mensaje.setText("N�mero de conexiones actuales: " + ServidorChat.ACTUALES);
		String texto = ServidorChat.textarea.getText();
		EnviarMensajes(texto);
// Seguidamente, se crea un bucle en el que se recibe lo que el cliente escribe en el chat.
// Cuando un cliente finaliza con el bot�n Salir, se env�a un * al servidor del Chat,
// entonces se sale del bucle while, ya que termina el proceso del cliente,
// de esta manera se controlan las conexiones actuales
		while (!fin) {
			String cadena = "";
			try {
				cadena = fentrada.readUTF();
				if (cadena.trim().equals("*")) {
				//Actualiza cuando el cliente se ha ido 
					ServidorChat.ACTUALES--;
					ServidorChat.mensaje.setText("N�mero de conexiones actuales: " + ServidorChat.ACTUALES);
					fin = true;	
				}
// El texto que el cliente escribe en el chat,
// se a�ade al textarea del servidor y se reenv�a a todos los clientes
//				else {
//					ServidorChat.textarea.append(cadena + "\n");
//					texto = ServidorChat.textarea.getText();
//					EnviarMensajes(texto);
//				}
				else
				{
					if(cadena.contains("."))
					{
						ServidorChat.textarea.append(cadena + "\n");
					}
					else
					{
						String[] arrayJuego = cadena.split("> ");
						String nombre = arrayJuego[0];
						String numero = arrayJuego[1];
						if(Integer.parseInt(numero) < ServidorChat.random)
						{
							ServidorChat.textarea.append("> " + nombre + " piensa que el n�mero es el " + numero + ", pero el n�mero es MAYOR. \n");
						}
						else if(Integer.parseInt(numero) > ServidorChat.random)
						{
							ServidorChat.textarea.append("> " + nombre + " piensa que el n�mero es el " + numero + ", pero el n�mero es MENOR. \n");	
						}
						else if(Integer.parseInt(numero) == ServidorChat.random)
						{
							ServidorChat.textarea.append("> " + nombre + " piensa que el n�mero es el " + numero + ", y ha ACERTADOOOO!!!! \n" + "El ganad@r ha sido: " + nombre + ". \n" + "El juego ha finalizado.");	
							texto = ServidorChat.textarea.getText();
							EnviarMensajes(texto);
							Thread.sleep(10000);
							//System.exit(0);
						}
					}
					texto = ServidorChat.textarea.getText();
					EnviarMensajes(texto);
				}
			}
			 catch (Exception ex) {
				ex.printStackTrace();
				fin = true;
			}
		}
	}

// El m�todo EnviarMensajes() env�a el texto del textarea a
// todos los sockets que est�n en la tabla de sockets,
// de esta forma todos ven la conversaci�n.
// El programa abre un stream de salida para escribir el texto en el socket
	//Enviamos el mensaje al resto de personas
	private void EnviarMensajes(String texto) {
		for (int i = 0; i < ServidorChat.CONEXIONES; i++) {
			Socket socket = ServidorChat.tabla[i];
			try {
				//Crea un flujo de salida por cada cliente
				DataOutputStream fsalida = new DataOutputStream(socket.getOutputStream());
				fsalida.writeUTF(texto);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
