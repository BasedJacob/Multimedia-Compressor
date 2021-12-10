
//CMPT365 Project 2 - Jacob He
//Image compresser

//for image
import java.io.File;

import java.io.FileWriter;

import javax.swing.JFileChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//for audio 
import java.util.zip.Deflater;

public class AudioCompresser {
	
	public static void main(String args[]) {		
         JFrame frame = new JFrame("CMPT365 Project 2");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(300,300);
         JPanel panel = new JPanel(); 
        JButton button1 = new JButton("Audio File");
        button1.addActionListener (new openAudio()); 
        //JButton button2 = new JButton("Image File");
        //button2.addActionListener (new openImage()); 
        frame.getContentPane().add(button1);
        //frame.getContentPane().add(button2);
        panel.add(button1);
        //panel.add(button2);
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.setVisible(true);	
	}
	

	static class openAudio implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
				File myfile = chooser.getSelectedFile();
				String dir = myfile.getParent();
				//System.out.println("dir = " + dir);
				try
			      {
			         // Open the wav file specified as the first argument
			         WavFile wavFile = WavFile.openWavFile(myfile);

			         // Display information about the wav file
			         wavFile.display();

			         // Get the number of audio channels in the wav file
			         int numChannels = wavFile.getNumChannels();

			         // Create a buffer of 100 frames
			         int[] buffer = new int[100 * numChannels];

			         int framesRead;
			         int totalframes = (int)wavFile.getNumFrames();
			         //System.out.println("Total Frames: " + totalframes);
			      
			         int lastvalue =0;
			         int difference =0;
			         int currentvalue=0;
			         int linearprediction1[] = new int[totalframes];
			         String longstring ="";   

			         //FOR MONO CASE 
			         if(numChannels==1) {
			        	 do
				         {
				            framesRead = wavFile.readFrames(buffer, 100);
				            // Linear Prediction 
				            for (int s=0 ; s<framesRead * numChannels ; s++)
				            {
				            	currentvalue = (buffer[s]);
				            	difference = currentvalue-lastvalue;
				            	lastvalue = currentvalue;
				            	linearprediction1[s]=difference;
				            	longstring += String.valueOf(difference);
				            	longstring += ":";
				            }
				         }
				         while (framesRead != 0);
	 
			        	 //Compress the data with entropy coding using util.zip
//			        	 FileWriter lstring = new FileWriter("C:\\Users\\Sheldon\\Documents\\LinPredMono");
//			        	 lstring.write(longstring);
//			        	 lstring.close();
			        	 //String linpredString = Arrays.toString(linearprediction1);
			        	 System.out.print("Lin-linpredString: " + longstring );
			        	 byte[] input = longstring.getBytes("UTF-8");
			             byte[] output = new byte[(int)myfile.length()];
			             Deflater compresser = new Deflater();
			             compresser.setInput(input);
			             compresser.finish();
			             int compressedDataLength = compresser.deflate(output);
			             
			             //System.out.println("LinearPrediction-CompressedDataLength: " + compressedDataLength );
			             compresser.end();
			        	 //Write the data to a file.

			        	 FileWriter writer = new FileWriter(dir+"\\CompressedMonoAudio");
			        	 for(int i =0; i < compressedDataLength; i++) {
			        		 writer.write(output[i]);
			        	 }
			        	 writer.close();
				         wavFile.close();
				         System.out.println("\nOriginal Length = " + myfile.length() +" Bytes");
				         System.out.println("Compressed Length = " + compressedDataLength + " Bytes");
				         System.out.println("Compression Ratio = " + (double)myfile.length()/(double)compressedDataLength );
				         System.out.println("\nCompressed File Saved As: " + dir+ "\\CompressedMonoAudio");
				         
				         
				         JFrame frame = new JFrame("Computation");
				         frame.setSize(100,100);
							JLabel label = new JLabel("Compression Ratio = " + myfile.length() +" / "+ compressedDataLength+ " = "+ (double)myfile.length()/(double)compressedDataLength);
							
							frame.getContentPane().add(label);
					        frame.pack();
							frame.setLocation(50,50);
							frame.setVisible(true);
			         }
			         
			       //FOR STEREO CASE 
			         if(numChannels==2) {
			        	 int midchannel =0;
			        	 int sidechannel =0;
			        	 currentvalue =0;
			        	 int nextvalue =0;
			        	 do
				         {
				            // Read frames into buffer
				            framesRead = wavFile.readFrames(buffer, 100);
				            // Coupling
				            for (int s=0 ; s<framesRead * numChannels ; s+=2)
				            {
				            	currentvalue = (buffer[s+1]);
				            	nextvalue = (buffer[s+1]);
				            	midchannel = (currentvalue+nextvalue)/2;
				            	sidechannel = (currentvalue-nextvalue)/2;
				            	longstring += String.valueOf(midchannel);
				            	longstring += ":";
				            	longstring += String.valueOf(sidechannel);
				            	longstring += " ";
				            }
				         }
				         while (framesRead != 0);
	 
			        	 //Compress the data with entropy coding using util.zip
//			        	 FileWriter lstring = new FileWriter("C:\\Users\\Sheldon\\Documents\\CouplingStereo");
//			        	 lstring.write(longstring);
//			        	 lstring.close();
			        	 
			        	 byte[] input = longstring.getBytes("UTF-8");
			             byte[] output = new byte[(int)myfile.length()];
			             Deflater compresser = new Deflater();
			             compresser.setInput(input);
			             compresser.finish();
			             int compressedDataLength = compresser.deflate(output);
			             
			             //System.out.println("LinearPrediction-CompressedDataLength: " + compressedDataLength );
			             compresser.end();
			        	 //Write the data to a file.
			        	 
			        	 FileWriter writer = new FileWriter(dir+ "\\CompressedStereoAudio");
			        	 for(int i =0; i < compressedDataLength; i++) {
			        		 writer.write(output[i]);
			        	 }
			        	 writer.close();
				         wavFile.close();
				         
				         //print original filesize vs new filesize
				         System.out.println("\nOriginal Length = " + myfile.length() +" Bytes");
				         System.out.println("Compressed Length = " + compressedDataLength + " Bytes");
				         System.out.println("Compression Ratio = " + (double)myfile.length()/(double)compressedDataLength );
				         System.out.println("\nCompressed File Saved As: " + dir+ "\\CompressedStereoAudio");
				         
				         
				         JFrame frame = new JFrame("Computation");
				         frame.setSize(100,100);
							JLabel label = new JLabel("Compression Ratio = " + myfile.length() +" / "+ compressedDataLength+ " = "+ (double)myfile.length()/(double)compressedDataLength);
							
							frame.getContentPane().add(label);
					        frame.pack();
							frame.setLocation(50,50);
							frame.setVisible(true);
			         }
			      }
			      catch (Exception ei)
			      {
			         System.err.println(ei);
			      }				
			}						
	}
}
}