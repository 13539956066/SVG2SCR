package com.gelakinetic.SVG2SCR;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/*
 * This only works on a specific kind of SVG. Here's how to produce it
 * 
 * 1) Set Inkscape to output absolute coordinates
 *      (Shift+Ctrl+P >> Input/Output >> SVG Output >> Path string format: Absolute)
 * 2) Turn both objects and strokes to paths
 *      (Ctrl+Alt+A) >> (Shift+Ctrl+C) >> (Ctrl+Alt+C)
 * 3) Approximate curved paths with many small straight ones
 *      (Ctrl+Alt+A)
 *      (Extensions >> Modify Path >> Add Nodes)
 *      (Extensions >> Modify Path >> Flatten Beziers)
 * 4) Polygons can't have holes in them, so they need to be split in parts.
 *      Draw a rectangle dividing the hole, like this: () => ([)]
 *      Select the rectangle and the polygon
 *      Use the Division command to split the polygon (Ctrl+/)
 * 5) Jiggle all objects to make sure they're saved with absolute coordinates
 *      (Ctrl+Alt+A) >> move everything left then right using the arrow keys
 * 6) Break apart paths
 *      (Ctrl+Shift+K)
 * 7) Ungroup all objects
 *      (Ctrl+Alt+A) >> (Shift+Ctrl+G) repeatedly, until there are no changes
 * 8) Save As >> Plain SVG
 * 
 * When you run the SCR file, 1px in the SVG will be equivalent to 1 of whatever
 * unit your EAGLE display is set to (mic, mm, mil, inch) 
 */

public class SVG2SCR {

	/**
	 * This class converts a list of SVG files into an EAGLE SCR file
	 * 
	 * @param args
	 *            A list of SVG files to convert into a single EAGLE script
	 *            (SCR). The filenames must follow the form LAYER_COMMAND.svg,
	 *            where LAYER is an EAGLE layer like "tPlace" or "top" and
	 *            COMMAND is an EAGLE drawing command like "poly" or "wire"
	 */
	public static void main(String[] args) {

		BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter(new File("svg.scr")));
			for (String arg : args) {
				String argParts[] = arg.split("[_\\.]");
				svg2scr(argParts[1], argParts[0], new File(arg), output);
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given an input SVG file, generate an SCR file to draw that image in EAGLE
	 * 
	 * @param command
	 *            The EAGLE command to invoke, usually POLY or WIRE
	 * @param layer
	 *            The EAGLE layer to draw the vector in
	 * @param inputFile
	 *            The SVG file
	 * @param output
	 *            Where to write the output SCR to
	 * @throws IOException
	 *             If something goes terribly wrong
	 */
	private static void svg2scr(String command, String layer, File inputFile, BufferedWriter output)
			throws IOException {

		/* Write the initial EAGLE commands to the SCR */
		output.write("set wire_bend 2;\n");
		output.write("set width 0.1;\n");
		output.write("change layer " + layer + ";\n");

		try {
			/* Parse the SVG (actually XML) file */
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			/* Pull the height out of the SVG so we can flip the Y axis */
			double height = 0;
			NodeList nListSvg = doc.getElementsByTagName("svg");
			for (int temp = 0; temp < nListSvg.getLength(); temp++) {
				Node nNode = nListSvg.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					height = Double.parseDouble(((Element) nNode).getAttribute("height"));
				}
			}
			Transform flipY = new Transform("matrix(1,0,0,-1,0," + height + ")");

			/* Get a list of paths from the SVG */
			NodeList nListPaths = doc.getElementsByTagName("path");
			for (int temp = 0; temp < nListPaths.getLength(); temp++) {
				Node nNode = nListPaths.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					/* Write the EAGLE command before all the points */
					output.write(command + " ");

					/* Get the actual path data from the "d" attribute */
					String d = ((Element) nNode).getAttribute("d");

					/* State variables for following SVG instructions */
					Segment initialSeg = null;
					Segment lastSeg = null;
					char instruction = 'M';

					/*
					 * Step through the actual path data and follow the SVG
					 * commands
					 */
					for (String part : d.split("\\s+")) {
						if (part.length() == 1) {
							instruction = part.charAt(0);
							if ('Z' == instruction || 'z' == instruction) {
								/* Close the path */
								output.write(initialSeg.applyTransform(flipY).toString());
								initialSeg = null;
							}
						} else if (part.length() > 0) {
							Segment seg = null;
							switch (instruction) {
								case 'M':
								case 'm': {
									/* Starting coordinate */
									String coords[] = part.split(",");
									seg = new Segment(instruction, Double.parseDouble(coords[0]),
											Double.parseDouble(coords[1]));
									break;
								}
								case 'L':
								case 'l': {
									/* Line to this coordinate */
									String coords[] = part.split(",");
									seg = new Segment(instruction, Double.parseDouble(coords[0]),
											Double.parseDouble(coords[1]));
									break;
								}
								case 'H':
								case 'h': {
									/* Horizontal line to this coordinate */
									seg = new Segment(instruction, Double.parseDouble(part), lastSeg.y);
									break;
								}
								case 'V':
								case 'v': {
									/* Vertical line to this coordinate */
									seg = new Segment(instruction, lastSeg.x, Double.parseDouble(part));
									break;
								}
							}
							/* Save the initial point for the Z command */
							if (initialSeg == null) {
								initialSeg = seg;
							}
							/* Save the current point for H and L commands */
							lastSeg = seg;
							/*
							 * Write the point to the SCR file, flipped over the
							 * Y axis
							 */
							output.write(seg.applyTransform(flipY).toString());
						}
					}
					/* Cap this path */
					output.write(";\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
