import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Neural {
	JFrame jframe;
	JLabel background;
	BufferedImage maze0;
	BufferedImage maze1;
	Graphics2D g0;
	Graphics2D g1;
	Timer timer;
	Robot robot;
	int tx = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	int ty = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	int size = 1;
	int time = 1;
	double strength = 0.2;
	int txs = tx / size;
	int tys = ty / size;
	double speed = 3;
	Point2D[] neuronsPos = new Point2D[100];
	Point2D[] neuronsVel = new Point2D[100];
	boolean mouse1 = false;
	int radius = 128;
	boolean circle = true;
	int buffer = 0;

	public Graphics2D getDrawBufferGraphics() {
		return buffer == 0 ? g0 : g1;
	}

	public BufferedImage getDrawBuffer() {
		return buffer == 0 ? maze0 : maze1;
	}
	
	public BufferedImage getDisplayBuffer() {
		return buffer == 0 ? maze1 : maze0;
	}

	public void swapBuffers() {
		buffer = 1 - buffer;
	}

	public void display() {
		if (size == 1) {
			background.setIcon(new ImageIcon(getDisplayBuffer()));
		} else {
			background.setIcon(new ImageIcon(getDisplayBuffer().getScaledInstance(tx, ty, BufferedImage.SCALE_FAST)));
		}
	}

	public void gen() {
		Point mouseLoc = null;
		mouseLoc = MouseInfo.getPointerInfo().getLocation();
		mouseLoc = new Point((mouseLoc.x / size), (mouseLoc.y / size));
		if (circle) {
			getDrawBufferGraphics().setColor(Color.magenta);
			getDrawBufferGraphics().drawOval(mouseLoc.x - radius * size, mouseLoc.y - radius * size, radius * 2 * size, radius * 2 * size);
		}
		for (int i = 0; i < neuronsVel.length; i++) {
			if (!mouse1 && neuronsPos[i].distance(mouseLoc) < radius) {
				double actualSpeed = speed * neuronsVel[i].distance(0, 0);
				double xSpeed = (mouseLoc.x - neuronsPos[i].getX()) / neuronsPos[i].distance(mouseLoc);
				double ySpeed = (mouseLoc.y - neuronsPos[i].getY()) / neuronsPos[i].distance(mouseLoc);
				neuronsPos[i].setLocation(neuronsPos[i].getX() + actualSpeed * xSpeed,
						neuronsPos[i].getY() + actualSpeed * ySpeed);
			} else {
				neuronsPos[i].setLocation(neuronsPos[i].getX() + speed * neuronsVel[i].getX(),
						neuronsPos[i].getY() + speed * neuronsVel[i].getY());
			}
		}

		getDrawBufferGraphics().setColor(Color.cyan);
		for (int i = 0; i < neuronsPos.length; i++) {
			boolean draw = true;
			if (neuronsPos[i].getX() < 0 || neuronsPos[i].getX() > txs) {
				neuronsVel[i].setLocation(-neuronsVel[i].getX(), neuronsVel[i].getY());
				draw = false;
			}
			if (neuronsPos[i].getY() < 0 || neuronsPos[i].getY() > tys) {
				neuronsVel[i].setLocation(neuronsVel[i].getX(), -neuronsVel[i].getY());
				draw = false;
			}
			if (draw) {
				getDrawBufferGraphics().fillOval((int) neuronsPos[i].getX() - (tys / 400), (int) neuronsPos[i].getY() - (tys / 400),
						(tys / 200), (tys / 200));
			}
		}
		for (int i = 0; i < neuronsPos.length - 1; i++) {
			for (int j = i + 1; j < neuronsPos.length; j++) {
				double dist = neuronsPos[i].distance(neuronsPos[j]) / (tys * strength);
				float brightness = (float) (Math.cos(dist * Math.PI / 2));
				if (dist < 1) {
					getDrawBufferGraphics().setColor(new Color(0.8f, 0.8f, 0.8f, brightness));
					getDrawBufferGraphics().drawLine((int) neuronsPos[i].getX(), (int) neuronsPos[i].getY(), (int) neuronsPos[j].getX(),
							(int) neuronsPos[j].getY());
				}
			}
		}
	}

	public void init() {

		jframe = new JFrame("Neural");
		jframe.setUndecorated(true);
		maze0 = new BufferedImage(txs, tys, BufferedImage.TYPE_3BYTE_BGR);
		maze1 = new BufferedImage(txs, tys, BufferedImage.TYPE_3BYTE_BGR);
		g0 = maze0.createGraphics();
		g0.clearRect(0, 0, txs, tys);
		g1 = maze1.createGraphics();
		g1.clearRect(0, 0, txs, tys);

		for (int i = 0; i < neuronsPos.length; i++) {
			neuronsPos[i] = new Point2D.Float();
			neuronsPos[i].setLocation(Math.random() * txs, Math.random() * tys);
			neuronsVel[i] = new Point2D.Float();
			neuronsVel[i].setLocation((Math.random() * 2) - 1, (Math.random() * 2) - 1);
		}
		background = new JLabel();
		background.setBounds(0, 0, tx, ty);
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				gen();
				swapBuffers();
			}
		}, 0, time);
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				display();
			}
		}, 0, time);
		try {
			robot = new Robot();
		} catch (AWTException e2) {
			e2.printStackTrace();
		}
		timer.schedule(new TimerTask() {
			public void run() {
				robot.keyPress(KeyEvent.VK_F23);
				robot.keyRelease(KeyEvent.VK_F23);
			}
		}, 60000, 60000);
		jframe.add(background);
		jframe.setBounds(0, 0, tx, ty);
		jframe.setLayout(null);
		jframe.setResizable(false);
		jframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
		jframe.setAlwaysOnTop(true);
		jframe.setVisible(true);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		jframe.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					jframe.dispose();
					timer.cancel();
				}
			}

			public void keyPressed(KeyEvent e) {

			}

		});
		jframe.addMouseListener(new MouseListener() {

			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouse1 = false;
				}
			}

			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouse1 = true;
				}
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON2) {
					circle = !circle;
				}
			}
		});
		jframe.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				speed -= e.getWheelRotation();
				if (speed < 0) {
					speed = 0;
				}
				if (speed > 15) {
					speed = 15;
				}
			}
		});
	}

	public static void main(String[] args) {
		Neural n = new Neural();
		n.init();
	}
}