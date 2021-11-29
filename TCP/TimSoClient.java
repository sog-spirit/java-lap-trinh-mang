package backup;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class TimSoClient extends JFrame implements MouseListener, Runnable {
	int gameBoardSize = 5;
	int tileSize = 100;
	int offSet = 50;
	int gameBoardPosition[][] = new int[5][5];
	int gameBoardPlayer[][] = new int[gameBoardSize][gameBoardSize];
	int playerID;
	List<Point> hasPlayed = new ArrayList<Point>();
	DataOutputStream dataOut;
	DataInputStream dataIn;
	
	public static void main(String[] args) {
		new Thread(new TimSoClient()).start();
	}
	public TimSoClient() {
		this.setTitle("Tro choi tim so");
		this.setSize(gameBoardSize*tileSize + 2*offSet, gameBoardSize*tileSize + 2*offSet);
		this.setDefaultCloseOperation(3);
		this.addMouseListener(this);
		
		try {
			Socket socket = new Socket("localhost", 6996);
			dataOut = new DataOutputStream(socket.getOutputStream());
			dataIn = new DataInputStream(socket.getInputStream());
		}
		catch(Exception e) {
			
		}
		this.setVisible(true);
	}
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
//		if(playerID == 1)
//			g.setColor(Color.CYAN);
//		if(playerID == 2)
//			g.setColor(Color.RED);
//		if(playerID == 3)
//			g.setColor(Color.MAGENTA);
//		if(playerID == 4)
//			g.setColor(Color.YELLOW);
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
				else if(gameBoardPosition[i][j] < 100)
					string = "0" + string;
				
				int x = offSet + i*tileSize + tileSize/2 - tileSize/4;
				int y = offSet + j*tileSize + tileSize/2 + tileSize/4 - tileSize/8;
				g.drawString(string, x, y);
			}
		}
	}
	@Override
	public void run() {
		try {
			for(int i = 0; i < gameBoardSize; i++) {
				for(int j = 0; j < gameBoardSize; j++) {
					gameBoardPosition[i][j] = Integer.parseInt(dataIn.readUTF());
				}
			}
			
			while (true) {
				int x = Integer.parseInt(dataIn.readUTF());
				int y = Integer.parseInt(dataIn.readUTF());
				hasPlayed.add(new Point(x, y));
				int playerID = Integer.parseInt(dataIn.readUTF());
				gameBoardPlayer[x][y] = playerID;
				this.playerID = playerID;
				this.repaint();
			}
		}
		catch (Exception e) {
			
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
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
		if(gameBoardPosition[xCord][yCord] != hasPlayed.size() + 1)
			return;
		try {
			dataOut.writeUTF(xCord + "");
			dataOut.writeUTF(yCord + "");
		}
		catch(Exception e1) {
			
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

