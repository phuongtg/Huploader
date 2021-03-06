package com.preprocessor;

import java.io.File;
import java.util.Hashtable;

import com.preprocessor.bixi.BixiTransformer;
import com.preprocessor.cosmo.CosmoTransformer;
import com.util.XConstants;
import com.util.XParser;


/**
 * Get the correct transform function depending on the different file format input
 * @author dan
 *
 */
public abstract class Transformer {
	
	// input: input directory, file format, template file, output directory
	// output: the file is transformed to csv files 
	public static void main(String[] args){
		
		if(args.length < 4)
			System.out.println("the argements should be input directory, file format, template file, output directory");
		
		String inDir = args[0];
		int format = Integer.valueOf(args[1]);
		String template = args[2];
		String outDir = args[3];
		
		Transformer transformer = null;
		
		if(format == XConstants.XML_FORMAT){
			
			transformer = new BixiTransformer();
			transformer.execute(inDir, ".xml", template, outDir);
			
		}else if(format == XConstants.BINARY_FORMAT){
			
			transformer = new CosmoTransformer();
			transformer.execute(inDir, ".bin", template, outDir);
		}else{
			System.out.println("Bad File Format");
		}
		
		
	}
	
	
	protected abstract void parse(String inFile,String outFile);
	
	// This represents the attributes in the raw file
	protected String[] item_set = null;
	
	public Transformer(){
	}
	
	/**
	 * 
	 * @param inputDir
	 * @param format
	 * @param template
	 * @param outDir
	 */
	public void execute(String inputDir, String fileSuffix, String csv_desc_file, String outDir){
		
		this.item_set = XParser.getTemplateDescAsArray(csv_desc_file);
		
		File in_directory = new File(inputDir);
		File out_directory = new File(outDir);
		try{
			if(!out_directory.exists())
				out_directory.createNewFile();
			if(in_directory.exists()){			
				File[] files = in_directory.listFiles();
				System.out.println("the total number: "+files.length);
				for(int i = 0;i<files.length;i++){
					String name = files[i].getName();
					if(name.contains(fileSuffix)){						
						System.out.println("start transform file Number: "+i);
						this.parse(in_directory.getAbsolutePath()+"/"+name,out_directory.getAbsolutePath()+"/"+name+".csv");						
					}									
				}
			}	
		}catch(Exception e){
			e.printStackTrace();
		}				
	}		
	
	
	
	
	
	
	
}
