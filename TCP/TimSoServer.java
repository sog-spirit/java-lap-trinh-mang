package backup;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class TimSoServer {
	static List<Point> hasPlayed = new ArrayList<Point>();
	static int gameBoardSize = 5;
	static int gameBoardPosition[][] = new int[gameBoardSize][gameBoardSize];
	static Vector<ClientProcessHandler> clientProcesses = new Vector<ClientProcessHandler>();
	Random rand = new Random();
	
	public static void main(String[] args) {
		new TimSoServer();
	}
	
	void initGameBoard() {
		for(int i = 0; i < gameBoardSize; i++) {
			for(int j = 0; j < gameBoardSize; j++) {
				gameBoardPosition[i][j] = i*gameBoardSize + j + 1;
			}
		}
		
		for(int r = 0; r < gameBoardSize*gameBoardSize; r++) {
			int i1 = rand.nextInt(gameBoardSize);
			int j1 = rand.nextInt(gameBoardSize);
			int i2 = rand.nextInt(gameBoardSize);
			int j2 = rand.nextInt(gameBoardSize);
			int tmp = gameBoardPosition[i1][j1];
			gameBoardPosition[i1][j1] = gameBoardPosition[i2][j2];
			gameBoardPosition[i2][j2] = tmp;
		}	
	}
	
	public TimSoServer() {
		initGameBoard();
		try {
			ServerSocket server = new ServerSocket(6996);
			lll: while(true) {
				Socket socket = server.accept();
				System.out.println(socket.getInetAddress() + "Join!!");
				ClientProcessHandler clientProcess = new ClientProcessHandler(socket);
				
				if(clientProcesses.size() <= 4) {
					clientProcesses.add(clientProcess);
					clientProcess.start();
				}
				else {
					for(int i = 0; i < 4; i++) {
						ClientProcessHandler clientProcess1 = clientProcesses.get(i);
						if(!clientProcess1.isConnecting && clientProcess1.socket.getInetAddress().equals(socket.getInetAddress())) {
							clientProcesses.remove(i);
							clientProcesses.add(i, clientProcess);
							clientProcess.start();
							continue lll;
						}
					}
					clientProcesses.add(clientProcess);
//					if(!isFull) {
//						for(int i = 0; i < 4; i++) {
//							for(ClientProcessHandler process : clientProcesses) {
//								process.start();
//							}
//						}
//						isFull = true;
//					}
				}
			}
		}
		catch(Exception e) {
			
		}
	}
}

class ClientProcessHandler extends Thread {
	Socket socket;
	DataOutputStream dataOut;
	DataInputStream dataIn;
	boolean isConnecting = true;
	boolean hasBoardGameGenerated = false;
	
	public ClientProcessHandler(Socket socket) {
		try {
			this.socket = socket;
			dataOut = new DataOutputStream(socket.getOutputStream());
			dataIn = new DataInputStream(socket.getInputStream());

			for(Point point : TimSoServer.hasPlayed) {
				dataOut.writeUTF(point.x + "");
				dataOut.writeUTF(point.y + "");
			}
			
			if(!hasBoardGameGenerated) {
				for(int i = 0; i < TimSoServer.gameBoardSize; i++) {
					for(int j = 0; j < TimSoServer.gameBoardSize; j++) {
						dataOut.writeUTF(TimSoServer.gameBoardPosition[i][j] + "");
					}
				}
				hasBoardGameGenerated = true;
			}
		}
		catch(Exception e) {
			isConnecting = false;
		}
	}
	
	public void run() {
		try {
			loop: while(true) {
				int playerID = 0;
				int incomingX = Integer.parseInt(dataIn.readUTF());
				int incomingY = Integer.parseInt(dataIn.readUTF());
				
				if(TimSoServer.clientProcesses.size() < 4)
					continue;
				
				System.out.println(socket.getInetAddress());
				
				if(incomingX < 0 || incomingX >= TimSoServer.gameBoardSize || incomingY < 0 || incomingY >= TimSoServer.gameBoardSize)
					continue;
				
				for(Point p : TimSoServer.hasPlayed) {
					if(p.x == incomingX && p.y == incomingY)
						continue loop;
				}
				if (this == TimSoServer.clientProcesses.get(0))
					playerID = 1;
				if (this == TimSoServer.clientProcesses.get(1))
					playerID = 2;
				if (this == TimSoServer.clientProcesses.get(2))
					playerID = 3;
				if (this == TimSoServer.clientProcesses.get(3))
					playerID = 4;
				for(ClientProcessHandler process : TimSoServer.clientProcesses) {
					try {
						process.dataOut.writeUTF(incomingX + "");
						process.dataOut.writeUTF(incomingY + "");
						process.dataOut.writeUTF(playerID + "");
					}
					catch(Exception e) {
						process.isConnecting = false;
					}
				}
			}
		}
		catch(Exception e) {
			isConnecting = false;
		}
	}
}
