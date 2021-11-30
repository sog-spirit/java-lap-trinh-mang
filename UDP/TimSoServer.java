package timSoUDP;

import java.awt.Point;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TimSoServer {
	static List<Point> hasPlayed = new ArrayList<Point>();
	static int gameBoardSize = 5;
	static int gameBoardPosition[][] = new int[gameBoardSize][gameBoardSize];
	static int gameBoardPlayer[][] = new int[gameBoardSize][gameBoardSize];
	Random rand = new Random();
	int numberOfPlayer = 0;
	
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
//		Arrays.fill(gameBoardPlayer, 1);
		initGameBoard();
		
		try {
			DatagramSocket socket = new DatagramSocket(6996);
			
			byte receiveData[] = new byte[1000];
			byte sendData[] = new byte[1000];
			
			while(true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				socket.receive(receivePacket);
				String str = new String(receivePacket.getData()).substring(0, receivePacket.getLength());
				if(str.contains("want to join")) {
					if(numberOfPlayer < 4) {
						numberOfPlayer++;
						String message = "you are a player " + numberOfPlayer;
						DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), receivePacket.getAddress(), receivePacket.getPort());
						socket.send(sendPacket);
						continue;
					}
					else {
						String message = "you are a viewer";
						DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), receivePacket.getAddress(), receivePacket.getPort());
						socket.send(sendPacket);
						continue;
					}
				}
				
				if(str.contains("create board")) {
					String boardPosition = "";
					for(int i = 0; i < gameBoardSize; i++) {
						for(int j = 0; j < gameBoardSize; j++) {
							if(gameBoardPosition[i][j] < 10) {
								String temp = "0" + gameBoardPosition[i][j];
								boardPosition += temp;
							}
							else {
								boardPosition += gameBoardPosition[i][j];
							}
						}
					}
					DatagramPacket sendPacket = new DatagramPacket(boardPosition.getBytes(), boardPosition.length(), receivePacket.getAddress(), receivePacket.getPort());
					socket.send(sendPacket);
					continue;
				}
				
				if(str.contains("newpos")) {
					System.out.println(str);
					String ID = str.substring(6, 6+1);
					String xCord = str.substring(7, 7+1);
					String yCord = str.substring(8, 8+1);
					
					gameBoardPlayer[Integer.parseInt(xCord)][Integer.parseInt(yCord)] = Integer.parseInt(ID);
					hasPlayed.add(new Point(Integer.parseInt(xCord), Integer.parseInt(yCord)));
				}
				if(str.contains("update")) {
					String message = "";
					
					if(numberOfPlayer < 4) {
						message = "not enough";
						DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), receivePacket.getAddress(), receivePacket.getPort());
						socket.send(sendPacket);
						continue;
					}
//					
					for(Point p : hasPlayed) {
						message += p.x + p.y + gameBoardPlayer[p.x][p.y];
					}
					DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), receivePacket.getAddress(), receivePacket.getPort());
					socket.send(sendPacket);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
