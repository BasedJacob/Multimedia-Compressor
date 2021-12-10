//CMPT365 Project 2 - Jacob He
//Image compresser
//for image
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class ImageQuantatizer{
	
	public static void main(String args[]) {		
         JFrame frame = new JFrame("CMPT365 Project 2");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(300,300);
         JPanel panel = new JPanel(); 

        JButton button2 = new JButton("Image File");
        button2.addActionListener (new openImage()); 

        frame.getContentPane().add(button2);

        panel.add(button2);
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.setVisible(true);	
	}
	
	static class openImage implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			
			if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
				File myfile = chooser.getSelectedFile();
				String dir = myfile.getParent();
				BufferedImage img;
				try {					
					img = ImageIO.read(myfile);
					double OGMatrixY[][]= new double[img.getWidth()][img.getHeight()];
					double OGMatrixU[][]= new double[img.getWidth()][img.getHeight()];
					double OGMatrixV[][]= new double[img.getWidth()][img.getHeight()];
					//YUV image
					BufferedImage yuvImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
					for (int i =0; i < img.getWidth(); i++) {
						for (int j = 0; j < img.getHeight(); j++) {
							Color c = new Color(img.getRGB(i, j));
							double r = c.getRed();
							double g = c.getGreen();
							double b = c.getBlue();
							
							double y0 = (0.299*r) + (0.587*g) + (0.114*b);
							double u0 = (-0.1687*r) + (-0.3313*g) + (0.5*b);
							double v0 = (0.5*r) + (-0.4187*g) + (-0.0813*b);
							
							
							OGMatrixY[i][j] = y0;
							OGMatrixU[i][j] = u0;
							OGMatrixV[i][j] = v0;
							
						}
					}

					System.out.println("Printing the Original first 8x8 of Y matrix");
					for(int i =0; i< 8; i++){
						for(int j =0; j<8; j++){
							System.out.print((int)OGMatrixY[i][j] + "  ");
						}
						System.out.println(" ");
					}										
					//Make the DCT matrix
					///////////////////////////////////////////////////////
					double DCTmat[][]= new double[8][8];
					double DCTmatTranspose [][] = new double[8][8];
					int nElements = 8;
					//System.out.println("Transformation Matrix: ");
					for(int i =0; i< nElements; i++){
						for(int j =0; j<nElements; j++){
							double mysizeN = (double)nElements;
							double a = Math.sqrt(2.0/mysizeN);
							if(i==0){
								a = Math.sqrt(1.0/mysizeN);
							}
							double Cij = a*Math.cos( ( ( (2.0* (double)j) +1.0) * Math.PI*(double)i ) / (2.0*mysizeN) );
							DCTmat[i][j]=Cij;	
							//System.out.println(Cij);
						}
					}
					//Transposing the transformation matrix
					for(int i =0; i < nElements; i++){
						for (int k = 0; k < nElements; k++){
							DCTmatTranspose[i][k]=DCTmat[k][i];
						}
					}
					/////////////////////////////////////////////////////					
					//Now we do the DCT transformation for each 8x8 square
					
					int QuantitizedMatrix[][][]= new int[img.getWidth()][img.getHeight()][3];
					double FinalMatrix[][]= new double[8][8];
					double IntermediateMatrix [][] = new double[8][8];
					double Temp8x8Block [] [] = new double[8][8];
					//double QuantMat [] [] = new double[8][8];
					double QuantMat[][] = { 
							   { 1, 1, 2, 4, 8, 16, 32 ,64 },
		                       { 1, 1, 2, 4, 8, 16, 32 ,64  },
		                       { 2, 2, 2, 4, 8 ,16 ,32 ,64 },
		                       { 4, 4, 4, 4, 8, 16, 32 ,64 },
		                       {8, 8, 8, 8, 8, 16, 32, 64},
		                       {16, 16 ,16, 16 , 16, 16, 32 ,64},
		                       {32, 32, 32, 32, 32, 32, 32, 32},
		                       {64, 64, 64, 64, 64, 64, 64, 64}
		                       };
					
					
					int hIndex, vIndex;

					////////////////////////////////////////////////////////
					//This is for Y , y is the quantitizedmatrix[..][..][0]
					hIndex = 0;
					vIndex = 0;
					
					int times =0;
					while (hIndex < img.getWidth()) {
						while(vIndex < img.getHeight()) {
							
							for(int i = 0; i < 8; i++) {
								for(int j = 0; j < 8; j++) {
									Temp8x8Block[i][j]=OGMatrixY[i+hIndex][j+vIndex];
								}
							}
							
							
							//take the inner product of DCTMat * to make IntermediateMatrix
							int i, j, k;
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            IntermediateMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++)
						            	IntermediateMatrix[i][j] += DCTmat[i][k] * Temp8x8Block[k][j];
						        }
						    }
						   
						    
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            FinalMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++)
						            	FinalMatrix[i][j] += IntermediateMatrix[i][k] * DCTmatTranspose[k][j];
						        }
						    }
						    ////////
						    if(times == 0) {
						    	System.out.println("	First 8 x 8 POST DCT Transform");
						    	times++;
						    	for (i = 0; i < 8; i++) {
							        for (j = 0; j < 8; j++) {
							        	System.out.print((int)(FinalMatrix[i][j]) + "   ");
							        }
							        System.out.println(" ");
						    	}     
						    	System.out.println("DCT Transform over");
						    }
						    
						    //Since we have our DCT transformed matrix "finalmatrix" we can now Quantitize it.
						    for( i = 0; i < 8; i++) {
						    	for (j = 0; j < 8; j++) {
						    		QuantitizedMatrix[i+hIndex][j+vIndex][0]=(int)(FinalMatrix[i][j]/QuantMat[i][j]);
						    	}
						    }

							//Now move down an 8x8 block for the next loop
							vIndex+=8;
						}
						//Shift right a 8x8 block and move back to the top
						hIndex+=8;
						vIndex=0;
					}
					
//////////////////////////////////////////////////////////////////////////
					//This is for U , u is the quantitizedmatrix[..][..][1]
					hIndex = 0;
					vIndex = 0;
					while (hIndex < img.getWidth()) {
						while(vIndex < img.getHeight()) {
							
							for(int i = 0; i < 8; i++) {
								for(int j = 0; j < 8; j++) {
									Temp8x8Block[i][j]=OGMatrixU[i+hIndex][j+vIndex];
								}
							}	
							//take the inner product of DCTMat * to make IntermediateMatrix
							int i, j, k;
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            IntermediateMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++)
						            	IntermediateMatrix[i][j] += DCTmat[i][k] * Temp8x8Block[k][j];
						        }
						    }
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            FinalMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++)
						            	FinalMatrix[i][j] += IntermediateMatrix[i][k] * DCTmatTranspose[k][j];
						        }
						    }
						    
						    //Since we have our DCT transformed matrix "finalmatrix"
						    //we can now Quantitize it.
						    for( i = 0; i < 8; i++) {
						    	for (j = 0; j < 8; j++) {
						    		QuantitizedMatrix[i+hIndex][j+vIndex][1]=(int)(FinalMatrix[i][j]/QuantMat[i][j]);
						    	}
						    }
							//Now move down an 8x8 block for the next loop
							vIndex+=8;
						}
						//Shift right a 8x8 block and move back to the top
						hIndex+=8;
						vIndex=0;
					}
///////////////////////////////////////////////////////////////////////////
					
					//This is for U , u is the quantitizedmatrix[..][..][1]
					hIndex = 0;
					vIndex = 0;
					while (hIndex < img.getWidth()) {
						while(vIndex < img.getHeight()) {
							
							for(int i = 0; i < 8; i++) {
								for(int j = 0; j < 8; j++) {
									Temp8x8Block[i][j]=OGMatrixV[i+hIndex][j+vIndex];
								}
							}
							
							//take the inner product of DCTMat * to make IntermediateMatrix
							int i, j, k;
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            IntermediateMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++)
						            	IntermediateMatrix[i][j] += DCTmat[i][k] * Temp8x8Block[k][j];
						        }
						    }
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            FinalMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++)
						            	FinalMatrix[i][j] += IntermediateMatrix[i][k] * DCTmatTranspose[k][j];
						        }
						    }
						    
						    //Since we have our DCT transformed matrix "finalmatrix"
						    //we can now Quantitize it.
						    for( i = 0; i < 8; i++) {
						    	for (j = 0; j < 8; j++) {
						    		QuantitizedMatrix[i+hIndex][j+vIndex][2]=(int)(FinalMatrix[i][j]/QuantMat[i][j]);
						    	}
						    }
							//Now move down an 8x8 block for the next loop
							vIndex+=8;
						}
						//Shift right a 8x8 block and move back to the top
						hIndex+=8;
						vIndex=0;
					}
//////////////////////////////////////////////////////////////////////////
					//We finished Quantizing the values. 
					//Lets print the first 8x8 to check!.
					System.out.println("	First 8x8 Quantatized Matrix");
					for(int i = 0; i < 8; i++) {
						for(int j = 0; j < 8; j++) {
							System.out.print(QuantitizedMatrix[i][j][0] + " - ");
						}
						System.out.println("");
					}
					System.out.println("Quantization over");
					
					//Write the Quantitized values into a text document.
					FileWriter writer = new FileWriter(dir+"\\Quantitized.txt");
					System.out.println("\nQuantizated File Saved As: "+ dir+"\\Quantitized.txt");
				//////////////////////////////////////////////////////
					//for Y, then for U, then for V
					writer.write("FOR Y:\n");
						for(int i =0; i < img.getWidth(); i++) {
							writer.write("\nRow: " + i + "\n");
							for(int j =0; j < img.getHeight(); j++) {
								writer.write("(");							
								writer.write(QuantitizedMatrix[i][j][0] + "");
								writer.write(") ");
							}
						}
						writer.write("\n\nFOR U:        \n ");
						for(int i =0; i < img.getWidth(); i++) {
							writer.write("\nRow: " + i + "\n");
							for(int j =0; j < img.getHeight(); j++) {
								writer.write("(");							
								writer.write(QuantitizedMatrix[i][j][0] + "");
								writer.write(") ");
							}
						}
						writer.write("\n\nFOR V:        \n ");
						for(int i =0; i < img.getWidth(); i++) {
							writer.write("\nRow: " + i + "\n");
							for(int j =0; j < img.getHeight(); j++) {
								writer.write("(");							
								writer.write(QuantitizedMatrix[i][j][0] + "");
								writer.write(") ");
							}
						}
						/////////////////////////////////////////////////////
						writer.close();
	
				//Invert the TransformatioinMatrix and TransformationMatrixTranspose.
						//but since it's unitary, TMatrix^T = Tmatrix^-1 , and Tmatrix^T^-1 = TMatrix
						
						
				//Now dequantitize the matrix, put the converted values in DeQuantitizedMatrix.
				int DeQuantitizedMatrix[][][] = new int[8][8][3];
				double DecompressedImage[][][] = new double[img.getWidth()][img.getHeight()][3];
				hIndex =0;
				vIndex = 0;
				//int ctr = 0;
				while (hIndex < img.getWidth()) {
					while(vIndex < img.getHeight()) {
						//Dequantitize one 8x8 block fpr Y+U+V
						for (int i =0; i < 8; i++) {
							for (int j = 0; j < 8; j++) {
								for (int k = 0; k < 3; k++) {
									DeQuantitizedMatrix[i][j][k]=QuantitizedMatrix[i+hIndex][j+vIndex][k]*(int)QuantMat[i][j];
								}
							}
						}				
							//take the inner product for Y
							int i, j, k;
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            IntermediateMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++)
						            	IntermediateMatrix[i][j] += DCTmatTranspose[i][k] * DeQuantitizedMatrix[k][j][0];
						        }
						    }
						   		    
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            FinalMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++) {
						            	FinalMatrix[i][j] += IntermediateMatrix[i][k] * DCTmat[k][j];
						            }
						            	DecompressedImage[i+hIndex][j+vIndex][0]=FinalMatrix[i][j];
						            
						        }
						    }
						    
//					    if( ctr ==0 ) {
//					    	ctr++;
//					    	System.out.print("First 8x8 De-Quantitized & Inverse DCT'd Matrix of Y"+"\n");
//					    	for (i = 0; i < 8; i++) {
//						        for (j = 0; j < 8; j++) {
//						        	System.out.print((int)FinalMatrix[i][j]+  "  ");
//						        	
//						        }
//						        System.out.print("\n");
//						   }
//					    }
						    
						  //take the inner product for U
						    i=0; 
						    j =0;
						    k=0;
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            IntermediateMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++)
						            	IntermediateMatrix[i][j] += DCTmatTranspose[i][k] * DeQuantitizedMatrix[k][j][1];
						        }
						    }
						   		    
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            FinalMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++) {
						            	FinalMatrix[i][j] += IntermediateMatrix[i][k] * DCTmat[k][j];
						            }
						            	DecompressedImage[i+hIndex][j+vIndex][1]=FinalMatrix[i][j];
						            
						        }
						    }
						    
						  //take the inner product for V
						    i=0; 
						    j =0;
						    k=0;
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            IntermediateMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++)
						            	IntermediateMatrix[i][j] += DCTmatTranspose[i][k] * DeQuantitizedMatrix[k][j][2];
						        }
						    }
						   		    
						    for (i = 0; i < 8; i++) {
						        for (j = 0; j < 8; j++) {
						            FinalMatrix[i][j] = 0;
						            for (k = 0; k < 8; k++) {
						            	FinalMatrix[i][j] += IntermediateMatrix[i][k] * DCTmat[k][j];
						            }
						            	DecompressedImage[i+hIndex][j+vIndex][2]=FinalMatrix[i][j];
						            
						        }
						    }
						


						
						vIndex+=8;
					}
					vIndex =0;
					hIndex+=8;
				}
				
				//Convert to RGB + place RGB value in new image, limit bounds to 0 and 255
				for (int i = 0; i < img.getWidth(); i++) {
			        for (int j = 0; j < img.getHeight(); j++) {
			        	double Yf = DecompressedImage[i][j][0];
			        	double Uf = DecompressedImage[i][j][1];
			        	double Vf = DecompressedImage[i][j][2];
			        	
			        	 int Rf = (int)(Yf + 1.140*Vf);
			        	 int Gf = (int)(Yf - 0.395*Uf - 0.581*Vf);
			        	int Bf = (int)(Yf + 2.032*Uf);
			        	if(Bf>255) 
			        		Bf=255;
			        	if(Gf>255) 
			        		Gf=255;
			        	if(Rf>255) 
			        		Rf=255;
			        	if(Bf<0) 
			        		Bf=0;
			        	if(Gf<0) 
			        		Gf=0;
			        	if(Rf<0) 
			        		Rf=0;
			        	
			        	Color finalcolor = new Color(Rf, Gf, Bf);
			            //Setting new Color object to the image
						yuvImage.setRGB(i, j, finalcolor.getRGB() );
			        }
			    }
					//Save the image to a file.
					
					
					File outputfile = new File(dir+"NewSample.bmp");
					System.out.println("\nDecompressed File Saved As: " + dir+"NewSample.bmp");
					ImageIO.write(yuvImage, "bmp", outputfile);	
					//Now show the images!
					JFrame frame = new JFrame("Original Image");
			        JFrame frame2 = new JFrame("Decompressed Image");
					JLabel label = new JLabel(new ImageIcon(img));
					JLabel label2 = new JLabel(new ImageIcon(yuvImage));
					frame.getContentPane().add(label);
			        frame2.getContentPane().add(label2);
			        frame.pack();
			        frame2.pack();
					frame.setLocation(50,50);
					frame.setVisible(true);
					frame2.setLocation(50+img.getWidth(),50);
					frame2.setVisible(true);
			        
					
				} 
				catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}			
	}
}

}