package timSoUDP;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

public class TimSoClient extends JFrame implements MouseListener, Runnable {
	int gameBoardSize = 5;
	int tileSize = 100;
	int offSet = 50;
	int gameBoardPosition[][] = new int[5][5];
	int gameBoardPlayer[][] = new int[gameBoardSize][gameBoardSize];
	int playerID;
	boolean isPlayer = true;
	boolean isPlayable = false;
	List<Point> hasPlayed = new ArrayList<Point>();
	
	public static void main(String[] args) {
		new Thread(new TimSoClient()).start();
	}
	public TimSoClient() {
//		Arrays.fill(gameBoardPlayer, 0);
		
		this.setTitle("Tro choi tim so");
		this.setSize(gameBoardSize*tileSize + 2*offSet, gameBoardSize*tileSize + 2*offSet);
		this.setDefaultCloseOperation(3);
		this.addMouseListener(this);
		
		this.setVisible(true);
	}
	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket();
			byte receiveData[] = new byte[1000];
			
			String message = "want to join";
			DatagramPacket invitePacket = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("localhost"), 6996);
			socket.send(invitePacket);
			DatagramPacket respondPacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(respondPacket);
			String restr = new String(respondPacket.getData()).substring(0, respondPacket.getLength());
			
			if(restr.contains("you are a player")) {
				String ID = restr.substring(restr.length()-1);
				playerID = Integer.parseInt(ID);
				//System.out.println(ID);
			}
			if(restr.contains("you are a viewer")) {
				isPlayer = false;
			}
			
			message = "create board";
			invitePacket = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("localhost"), 6996);
			socket.send(invitePacket);
			socket.receive(respondPacket);
			restr = new String(respondPacket.getData()).substring(0, respondPacket.getLength());
			//System.out.println(restr);
			for(int i = 0; i < gameBoardSize; i++) {
				for(int j = 0; j < gameBoardSize; j++) {
					String boardPos = restr.substring((i*5+j)*2, (i*5+j)*2 + 2);
					//System.out.println(boardPos);
					gameBoardPosition[i][j] = Integer.parseInt(boardPos);
				}
			}
			this.repaint();
			
			while(true) {
				message = "update";
				invitePacket = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("localhost"), 6996);
				socket.send(invitePacket);
				socket.receive(respondPacket);
				restr = new String(respondPacket.getData()).substring(0, respondPacket.getLength());
				
				if(restr.contains("not enough" )) {
					isPlayable = false;
					continue;
				}
				isPlayable = true;
				
//				hasPlayed.clear();
				for(int i = 0; i < restr.length()/3; i++) {
					String ID = restr.substring(i*3, i*3);
					String x = restr.substring(i*3+1, i*3+1);
					String y = restr.substring(i*3+2, i*3+2);
					
					if(hasPlayed.contains(new Point(Integer.parseInt(x), Integer.parseInt(y))))
						continue;
					hasPlayed.add(new Point(Integer.parseInt(x), Integer.parseInt(y)));
					gameBoardPosition[Integer.parseInt(x)][Integer.parseInt(y)] = Integer.parseInt(ID);
				}
				this.repaint();
			}
		}
		catch (Exception e) {
			
		}
	}
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		for(int i = 0; i < hasPlayed.size(); i++) {
			int xBoard = hasPlayed.get(i).x;
			int yBoard = hasPlayed.get(i).y;
			
			int x = offSet + xBoard*tileSize;
			int y = offSet + yBoard*tileSize;
			playerID = gameBoardPlayer[xBoard][yBoard];
			
			if(playerID == 1)
				g.setColor(Color.CYAN);
			if(playerID == 2)
				g.setColor(Color.RED);
			if(playerID == 3)
				g.setColor(Color.MAGENTA);
			if(playerID == 4)
				g.setColor(Color.YELLOW);
			g.fillRect(x, y, 2*offSet, 2*offSet);
		}
		
		g.setColor(Color.BLACK);
		for(int i = 0; i <= gameBoardSize; i++) {
			//ke duong ngang
			g.drawLine(offSet, offSet+i*tileSize, offSet+gameBoardSize*tileSize, offSet+i*tileSize);
			//ke duong doc
			g.drawLine(offSet+i*tileSize, offSet, offSet+i*tileSize, offSet+gameBoardSize*tileSize);
		}
		
		g.setFont(new Font("arial", Font.BOLD, tileSize/3));
		for(int i = 0; i < gameBoardSize; i++) {
			for(int j = 0; j < gameBoardSize; j++) {
				String string = gameBoardPosition[i][j] + "";
				if(gameBoardPosition[i][j] < 10)
					string = "00" + string;
				else //if(gameBoardPosition[i][j] < 100)
					string = "0" + string;
				
				int x = offSet + i*tileSize + tileSize/2 - tileSize/4;
				int y = offSet + j*tileSize + tileSize/2 + tileSize/4 - tileSize/8;
				g.drawString(string, x, y);
			}
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if(isPlayer && isPlayable) {
			int x = e.getX();
			int y = e.getY();
			
			if(x < offSet || x >= offSet + tileSize*gameBoardSize)
				return;
			if( y < offSet || y >= offSet + gameBoardSize*tileSize)
				return;
			
			int xCord = (x - offSet) / tileSize;
			int yCord = (y - offSet) / tileSize;
			
			for(Point p : hasPlayed) {
				if(xCord == p.x && yCord == p.y)
					return;
			}
//			for(int i = 0; i < gameBoardSize; i++) {
//				for(int j = 0; j < gameBoardSize; j++) {
//					if(gameBoardPosition[i][j] == 0)
//						return;
//				}
//			}
			if(gameBoardPosition[xCord][yCord] != hasPlayed.size() + 1)
				return;
//			hasPlayed.add(new Point(xCord, yCord));
			String message = "newpos" + playerID + xCord + yCord;
			try {
//				System.out.println(message);
				DatagramSocket socket = new DatagramSocket();
				DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("localhost"), 6996);
				socket.send(sendPacket);
			}
			catch (Exception e1) {
				System.out.println(e1);
			}
		}
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
