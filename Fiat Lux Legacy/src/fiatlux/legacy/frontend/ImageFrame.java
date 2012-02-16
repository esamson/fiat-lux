package fiatlux.legacy.frontend;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JFrame;

public class ImageFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public ImageFrame(String title) {
		super(title);
	}

	public void setImage(Image i) {
		this.image = i;
	}

	public void setStatus(String s) {
		this.status = s;
	}

	public void paint(Graphics g) {
		// call the super to maintain proper vison
		super.paint(g);

		// calculate coordinates to center image
		int centerX = this.getWidth() / 2;
		int imageX = centerX - image.getWidth(this) / 2;
		int centerY = this.getHeight() / 2;
		int imageY = centerY - image.getHeight(this) / 2;
		g.drawImage(image, imageX, imageY, this);

		// calculate coordinates to center text
		int textY = imageY + image.getHeight(this) + 10;
		FontMetrics metrics = g.getFontMetrics();
		int textWidth = metrics.stringWidth(status);
		int textX = centerX - (textWidth / 2);
		g.drawString(status, textX, textY);
	}

	private Image image;
	private String status;
}
