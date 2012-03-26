/* 
 * License: source-license.txt
 * If this code is used independently, copy the license here.
 */

package wombat.scheme.libraries;

import java.awt.FileDialog;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import java.io.*;
import javax.imageio.*;

import wombat.scheme.libraries.gui.ImageFrame;
import wombat.scheme.libraries.types.ImageData;

/**
 * Helper class to load, save, and display images using Java. 
 */
public class ImageAPI {
	private ImageAPI() {}
	
	/**
	 * Show a dialog to allow the user to choose an image, then read it into a byte stream.
	 * @return A stream of encoded data. The first two values are rows then columns, then the sequence is [r,g,b,a] across rows then down.
	 * @throws IOException If we cannot read the file.
	 */
	public static ImageData readImage() throws IOException {
		FileDialog fc = new FileDialog((java.awt.Frame) null, "read-image", FileDialog.LOAD);
        fc.setVisible(true);
        if (fc.getFile() == null)
        	throw new IllegalArgumentException("Error in read-image: no image chosen.");

        File file = new File(fc.getDirectory(), fc.getFile());
        
		return readImage(file.getCanonicalPath());
	}
	
	/**
	 * Read an image into a bytestream.
	 * @param filename The file to read.
	 * @return A stream of encoded data. The first two values are rows then columns, then the sequence is [r,g,b,a] across rows then down.
	 * @throws IOException If we cannot read the image.
	 */
	public static ImageData readImage(String filename) throws IOException {
		BufferedImage bi = ImageIO.read(new File(filename));
		
		int[] data = new int[bi.getWidth() * bi.getHeight()];
		bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), data, 0, bi.getWidth());
		
		return new ImageData(bi.getWidth(), bi.getHeight(), data);
	}
	
	/**
	 * Write the given [r,g,b,a] buffer to an image. Display a dialog for the filename.
	 * @param data The image to write. The first two values are the width and height.
	 * @throws IOException If we cannot write the image.
	 */
	public static void writeImage(ImageData img) throws IOException {
		FileDialog fc = new FileDialog((java.awt.Frame) null, "write-image", FileDialog.SAVE);
        fc.setVisible(true);

        if (fc.getFile() == null)
        	throw new IllegalArgumentException("Error in read-image: no image chosen.");

        File file = new File(fc.getDirectory(), fc.getFile());
		writeImage(img, file.getCanonicalPath());
	}
	
	/**
	 * Write the given [r,g,b,a] buffer to an image.
	 * @param data The image to write. The first two values are the width and height.
	 * @param filename The file to write to.
	 * @throws IOException If we cannot write the image.
	 */
	public static void writeImage(ImageData img, String filename) throws IOException {
		RenderedImage ri = new BufferedImage(img.Width, img.Height, BufferedImage.TYPE_4BYTE_ABGR);
		((BufferedImage) ri).setRGB(0, 0, img.Width, img.Height, img.Data, 0, img.Width);
		
		File file = new File(filename);
		String[] parts = file.getName().split("\\.");
		ImageIO.write(ri, parts[parts.length - 1], file);
	}
	
	/**
	 * Display the given [r,g,b,a] buffer as an image.
	 * @param data The image to write. The first two values are the width and height.
	 * @throws IOException If we cannot write the image.
	 */
	public static void displayImage(ImageData img) {
		final RenderedImage ri = new BufferedImage(img.Width, img.Height, BufferedImage.TYPE_4BYTE_ABGR);
		((BufferedImage) ri).setRGB(0, 0, img.Width, img.Height, img.Data, 0, img.Width);
		
		// Create the basic frame.
		new ImageFrame((Image) ri).setVisible(true);
	}
}


