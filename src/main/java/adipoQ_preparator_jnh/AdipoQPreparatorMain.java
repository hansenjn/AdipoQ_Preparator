package adipoQ_preparator_jnh;
/** ===============================================================================
* AdipoQ Preparator Version 0.2.0
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*  
* See the GNU General Public License for more details.
*  
* Copyright (C) Jan Niklas Hansen
* Date: October 13, 2020 (This Version: May 28, 2022)
*   
* For any questions please feel free to contact me (jan.hansen@uni-bonn.de).
* =============================================================================== */

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.text.*;

import javax.swing.UIManager;

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.*;
import ij.plugin.*;
import ij.process.LUT;
import ij.text.*;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import ij.process.AutoThresholder.Method;

public class AdipoQPreparatorMain implements PlugIn, Measurements {
	//Name variables
	static final String PLUGINNAME = "AdipoQ Preparator";
	static final String PLUGINVERSION = "0.2.0";
	
	//Fix fonts
	static final Font SuperHeadingFont = new Font("Sansserif", Font.BOLD, 16);
	static final Font HeadingFont = new Font("Sansserif", Font.BOLD, 14);
	static final Font SubHeadingFont = new Font("Sansserif", Font.BOLD, 12);
	static final Font TextFont = new Font("Sansserif", Font.PLAIN, 12);
	static final Font InstructionsFont = new Font("Sansserif", 2, 12);
	static final Font RoiFont = new Font("Sansserif", Font.PLAIN, 12);
	
	DecimalFormat df6 = new DecimalFormat("#0.000000");
	DecimalFormat df3 = new DecimalFormat("#0.000");
	DecimalFormat df0 = new DecimalFormat("#0");
	DecimalFormat dfDialog = new DecimalFormat("#0.000000");
		
	static SimpleDateFormat NameDateFormatter = new SimpleDateFormat("yyMMdd_HHmmss");
	static SimpleDateFormat FullDateFormatter = new SimpleDateFormat("yyyy-MM-dd	HH:mm:ss");
	static SimpleDateFormat FullDateFormatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
	//Fix LUTs
	final static double STARDISTLUTNUMBERS [][] = new double [][] {{0.0,0.0,0.0},{175.0,153.0,230.0},{246.0,248.0,253.0},{79.0,178.0,215.0},
		{101.0,176.0,142.0},{70.0,82.0,165.0},{41.0,168.0,120.0},{252.0,236.0,247.0},{231.0,178.0,190.0},{210.0,234.0,217.0},
		{157.0,121.0,169.0},{152.0,218.0,231.0},{89.0,116.0,135.0},{210.0,143.0,173.0},{253.0,251.0,250.0},{250.0,237.0,222.0},
		{179.0,134.0,128.0},{252.0,247.0,252.0},{146.0,112.0,163.0},{250.0,244.0,249.0},{251.0,241.0,242.0},{231.0,213.0,236.0},
		{180.0,217.0,208.0},{238.0,231.0,241.0},{206.0,172.0,88.0},{218.0,222.0,246.0},{232.0,222.0,161.0},{184.0,24.0,77.0},
		{135.0,170.0,175.0},{51.0,198.0,123.0},{252.0,254.0,251.0},{188.0,131.0,219.0},{118.0,238.0,206.0},{188.0,202.0,211.0},
		{218.0,112.0,99.0},{38.0,88.0,208.0},{214.0,223.0,242.0},{81.0,111.0,181.0},{231.0,130.0,164.0},{111.0,106.0,167.0},
		{63.0,171.0,80.0},{223.0,245.0,237.0},{249.0,248.0,253.0},{251.0,249.0,247.0},{236.0,236.0,246.0},{206.0,205.0,236.0},
		{181.0,212.0,94.0},{198.0,160.0,31.0},{124.0,216.0,112.0},{105.0,170.0,117.0},{26.0,137.0,219.0},{31.0,190.0,131.0},
		{234.0,192.0,195.0},{182.0,121.0,26.0},{229.0,238.0,201.0},{218.0,212.0,31.0},{34.0,47.0,194.0},{112.0,206.0,25.0},
		{167.0,243.0,227.0},{141.0,165.0,114.0},{193.0,191.0,139.0},{217.0,197.0,158.0},{217.0,219.0,250.0},{229.0,224.0,197.0},
		{165.0,176.0,110.0},{33.0,211.0,71.0},{155.0,59.0,162.0},{228.0,161.0,69.0},{176.0,109.0,174.0},{221.0,179.0,122.0},
		{229.0,184.0,190.0},{197.0,220.0,216.0},{213.0,78.0,97.0},{127.0,152.0,193.0},{126.0,102.0,158.0},{230.0,219.0,215.0},
		{100.0,137.0,85.0},{243.0,224.0,174.0},{209.0,243.0,199.0},{246.0,232.0,196.0},{86.0,210.0,73.0},{110.0,209.0,158.0},
		{216.0,186.0,168.0},{82.0,55.0,233.0},{119.0,167.0,199.0},{121.0,191.0,73.0},{137.0,198.0,208.0},{195.0,120.0,23.0},
		{215.0,225.0,234.0},{159.0,69.0,107.0},{145.0,226.0,137.0},{124.0,124.0,174.0},{227.0,212.0,154.0},{248.0,246.0,252.0},
		{191.0,221.0,181.0},{137.0,144.0,71.0},{124.0,166.0,212.0},{212.0,156.0,148.0},{216.0,150.0,218.0},{226.0,188.0,187.0},
		{70.0,58.0,231.0},{114.0,156.0,88.0},{158.0,138.0,186.0},{251.0,246.0,247.0},{132.0,200.0,62.0},{232.0,241.0,249.0},
		{155.0,182.0,216.0},{105.0,181.0,238.0},{239.0,244.0,230.0},{225.0,120.0,149.0},{188.0,237.0,222.0},{226.0,100.0,216.0},
		{233.0,229.0,252.0},{188.0,242.0,173.0},{232.0,179.0,239.0},{88.0,216.0,137.0},{233.0,203.0,224.0},{183.0,200.0,215.0},
		{166.0,111.0,150.0},{96.0,83.0,170.0},{220.0,210.0,238.0},{246.0,252.0,252.0},{227.0,117.0,146.0},{144.0,157.0,241.0},
		{227.0,239.0,234.0},{150.0,166.0,194.0},{252.0,244.0,244.0},{164.0,232.0,148.0},{208.0,210.0,247.0},{236.0,252.0,230.0},
		{204.0,217.0,249.0},{71.0,182.0,134.0},{223.0,213.0,174.0},{148.0,190.0,136.0},{43.0,123.0,180.0},{115.0,162.0,219.0},
		{60.0,150.0,223.0},{226.0,227.0,238.0},{54.0,63.0,161.0},{245.0,252.0,249.0},{190.0,122.0,165.0},{127.0,186.0,139.0},
		{30.0,179.0,122.0},{160.0,101.0,139.0},{193.0,119.0,208.0},{243.0,241.0,247.0},{173.0,129.0,62.0},{253.0,240.0,246.0},
		{231.0,226.0,244.0},{231.0,112.0,113.0},{180.0,174.0,124.0},{180.0,95.0,162.0},{222.0,221.0,170.0},{60.0,89.0,154.0},
		{167.0,135.0,41.0},{203.0,132.0,197.0},{161.0,42.0,183.0},{74.0,152.0,207.0},{83.0,188.0,130.0},{193.0,132.0,88.0},
		{78.0,53.0,191.0},{79.0,129.0,115.0},{92.0,37.0,203.0},{231.0,162.0,217.0},{254.0,249.0,249.0},{254.0,253.0,254.0},
		{189.0,144.0,141.0},{71.0,183.0,89.0},{194.0,175.0,224.0},{206.0,208.0,146.0},{253.0,254.0,254.0},{184.0,87.0,40.0},
		{227.0,231.0,213.0},{208.0,98.0,85.0},{177.0,67.0,211.0},{213.0,233.0,173.0},{50.0,229.0,63.0},{217.0,190.0,202.0},
		{152.0,128.0,234.0},{186.0,155.0,148.0},{222.0,222.0,151.0},{98.0,124.0,194.0},{187.0,212.0,233.0},{171.0,203.0,128.0},
		{188.0,126.0,151.0},{223.0,227.0,234.0},{238.0,247.0,249.0},{32.0,103.0,186.0},{126.0,88.0,187.0},{131.0,189.0,122.0},
		{220.0,234.0,225.0},{253.0,253.0,252.0},{251.0,252.0,249.0},{244.0,237.0,239.0},{143.0,109.0,186.0},{253.0,254.0,254.0},
		{148.0,168.0,113.0},{114.0,203.0,34.0},{250.0,247.0,245.0},{75.0,201.0,151.0},{184.0,239.0,176.0},{77.0,58.0,163.0},
		{202.0,226.0,208.0},{242.0,243.0,231.0},{195.0,108.0,93.0},{209.0,151.0,111.0},{126.0,120.0,198.0},{191.0,242.0,228.0},
		{63.0,181.0,214.0},{224.0,32.0,151.0},{189.0,152.0,155.0},{180.0,227.0,70.0},{220.0,220.0,241.0},{125.0,223.0,54.0},
		{226.0,144.0,132.0},{247.0,239.0,253.0},{168.0,236.0,162.0},{226.0,243.0,231.0},{241.0,246.0,250.0},{233.0,201.0,233.0},
		{190.0,198.0,228.0},{252.0,248.0,251.0},{254.0,255.0,254.0},{201.0,120.0,222.0},{145.0,154.0,71.0},{202.0,91.0,123.0},
		{100.0,91.0,159.0},{177.0,192.0,140.0},{150.0,94.0,112.0},{184.0,147.0,242.0},{160.0,180.0,141.0},{241.0,244.0,234.0},
		{247.0,252.0,253.0},{203.0,174.0,168.0},{185.0,225.0,63.0},{153.0,232.0,196.0},{227.0,252.0,233.0},{118.0,210.0,190.0},
		{178.0,213.0,160.0},{85.0,141.0,203.0},{235.0,108.0,212.0},{210.0,183.0,117.0},{99.0,173.0,181.0},{234.0,212.0,124.0},
		{132.0,105.0,194.0},{140.0,178.0,154.0},{157.0,188.0,208.0},{229.0,232.0,202.0},{226.0,207.0,80.0},{238.0,249.0,248.0},
		{224.0,244.0,227.0},{153.0,65.0,97.0},{156.0,58.0,224.0},{169.0,105.0,235.0},{212.0,24.0,133.0},{198.0,155.0,113.0}};
	
	LUT starDistLUT;
		
	//Progress Dialog
	ProgressDialog progress;	
	boolean processingDone = false;	
	boolean continueProcessing = true;
	
	//-----------------define params-----------------
	static final String[] taskVariant = {"active image in FIJI","multiple images (open multi-task manager)", "all images open in FIJI"};
	String selectedTaskVariant = taskVariant[1];
	int tasks = 1;

	final static String[] settingsMethod = {"manually enter preferences (default settings: histology)", "manually enter preferences (default settings: cultured cells)", "load preferences from existing AdipoQ Preparator metadata file"};
	String selectedSettingsVariant = settingsMethod [0];
	
	String loadSeries = "ALL";
	
	boolean includeDuplicateChannel = true;
	boolean deleteOtherChannels = true;
	int numberOfChannels = 1;

	int channelIDs [] = new int []{1,2,3};
	boolean subtractBackground [] = new boolean [] {false, false, false};
	double subtractBGRadius [] = new double [] {30.0, 30.0, 30.0};	
	boolean preBlur [] = new boolean []{true,true,true};
	double preBlurSigma [] = new double [] {1.8,1.8,1.8};
	boolean subtractBluredImage [] = new boolean []{true,true,true};
	double subtractBlurSigma [] = new double [] {2.7,2.7,2.7};
	
	boolean excludeZeroRegions [] = new boolean []{true,true,true};
	double closeGapsRadius [] = new double [] {2.2,2.2,2.2};
	boolean removeParticles [] = new boolean []{true,true,true};
	double removeRadius [] = new double [] {8.8,8.8,8.8};
	double linkGapsForRemoveRadius [] = new double [] {2.2,2.2,2.2};
	final static String [] algorithm = {"Default", "IJ_IsoData", "Huang", "Intermodes", "IsoData", "Li", "MaxEntropy", "Mean", 
			"MinError", "Minimum", "Moments", "Otsu", "Percentile", "RenyiEntropy", "Shanbhag", "Triangle", 
			"Yen", "CUSTOM threshold", "StarDist"};
	String chosenAlgorithm [] = new String [] {"Triangle","Triangle","Triangle"};
	double customThr [] = new double [] {0.0,0.0,0.0};
	boolean darkBackground [] = new boolean []{false,false,false};
	final static String [] bgMethod = new String []{"detect bright structures (dark background; e.g., immunofluorescence image)", "detect dark structures (bright background; e.g., histology)"};
	String selectedBgVariant [] = new String [] {bgMethod [0],bgMethod [0],bgMethod [0]};
	
	boolean despeckle [] = new boolean []{true,true,true};
	boolean linkForROI [] = new boolean []{false,false,false};
	boolean fillHoles [] = new boolean []{true,true,true};
	boolean watershed [] = new boolean []{false,false,false};
		
	static final String[] outputVariant = {"save as filename + suffix 'AQP'", "save as filename + suffix 'AQP' + date"};
	String chosenOutputName = outputVariant[0];
	
	static final String[] nrFormats = {"US (0.00...)", "Germany (0,00...)"};
	String ChosenNumberFormat = nrFormats[0];		

	Robot robo;
	boolean keepAwake = false;
	
	//StarDist (implemented from v0.2.0 on)
	final static String [] STARDISTMODELS = {"Versatile (fluorescent nuclei)","Versatile (H&E nuclei)", "DSB 2018 (from StarDist 2D paper)","Model (.zip) from File"};
	String selectedStarDistModel [] = new String [] {STARDISTMODELS[0],STARDISTMODELS[0],STARDISTMODELS[0]};
	boolean starDistNormalizeImage [] = new boolean [] {true,true,true};
	double starDistPercentileLow [] = new double [] {1.0,1.0,1.0};
	double starDistPercentileHigh [] = new double [] {99.8,99.8,99.8};
	double starDistProbabilityScore [] = new double [] {0.48,0.48,0.48};
	double starDistOverlapThreshold [] = new double [] {0.3,0.3,0.3};
	String starDistModelPath [] = new String [] {"","",""};
	int starDistNTiles [] = new int [] {1,1,1};
	//-----------------define params-----------------
	
	//Variables for processing of an individual task
//		enum channelType {PLAQUE,CELL,NEURITE};
	
public void run(String arg) {

	//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	//---------------------------read preferences---------------------------------
	//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	
	GenericDialog gd = new GenericDialog(PLUGINNAME + " on " + System.getProperty("os.name") + "");	
	//show Dialog-----------------------------------------------------------------
	//.setInsets(top, left, bottom)
	gd.setInsets(0,0,0);	gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2019-2022 JN Hansen", SuperHeadingFont);
	gd.setInsets(5,0,0);	gd.addChoice("process ", taskVariant, selectedTaskVariant);
	gd.setInsets(0,0,0);	gd.addMessage("The plugin processes .tif images or calls a BioFormats plugin to open different formats.", InstructionsFont);
	gd.setInsets(0,0,0);	gd.addMessage("The BioFormats plugin is preinstalled in FIJI / can be manually installed to ImageJ.", InstructionsFont);
	

	gd.setInsets(10,0,0);	gd.addStringField("Series to be processed (if multi-series files are loaded via BioFormats plugin)", loadSeries);
	gd.setInsets(0,0,0);	gd.addMessage("Notes:", InstructionsFont);
	gd.setInsets(0,0,0);	gd.addMessage("1. If not 'ALL' series shall be processed, enter the series numbers separated by commas.", InstructionsFont);
	gd.setInsets(0,0,0);	gd.addMessage("E.g., entering '1,7' will process series 1 and 7.", InstructionsFont);
	gd.setInsets(0,0,0);	gd.addMessage("2. If images from a Zeiss Slide Scanner shall be analyzed and only the best-resolved images shall be used,", InstructionsFont);
	gd.setInsets(0,0,0);	gd.addMessage("enter 'SLIDESCANNER'.", InstructionsFont);
	
	gd.setInsets(10,0,0);	gd.addChoice("Preferences: ", settingsMethod, selectedSettingsVariant);
	gd.setInsets(0,0,0);	gd.addMessage("Note: loading settings from previous analysis not yet implemented.", InstructionsFont);
	
	gd.setInsets(10,0,0);	gd.addMessage("GENERAL SETTINGS:", HeadingFont);	
	gd.setInsets(5,0,0);	gd.addChoice("Output image name: ", outputVariant, chosenOutputName);
	gd.setInsets(5,0,0);	gd.addChoice("output number format", nrFormats, nrFormats[0]);
	gd.setInsets(5,0,0);	gd.addCheckbox("Keep computer awake during processing", keepAwake);
	
	gd.showDialog();
	//show Dialog-----------------------------------------------------------------

	//read and process variables--------------------------------------------------
	selectedTaskVariant = gd.getNextChoice();
	loadSeries = gd.getNextString();
	selectedSettingsVariant = gd.getNextChoice();
	chosenOutputName = gd.getNextChoice();	
	ChosenNumberFormat = gd.getNextChoice();
	dfDialog.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	if(ChosenNumberFormat.equals(nrFormats[0])){ //US-Format
		df6.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		df3.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		df0.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	}else if (ChosenNumberFormat.equals(nrFormats[1])){
		df6.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.GERMANY));
		df3.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.GERMANY));
		df0.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.GERMANY));
	}
	keepAwake = gd.getNextBoolean();
	//read and process variables--------------------------------------------------
	if (gd.wasCanceled()) return;
	
	if(selectedSettingsVariant.equals(settingsMethod [0])){
		/** HISTOLOGY */
		if(!enterSettings(0)) {
			return;
		}
	}else if(selectedSettingsVariant.equals(settingsMethod [1])){
		/** CULTURED CELLS - DAPI */
		if(!enterSettings(1)) {
			return;
		}
	}else if(selectedSettingsVariant.equals(settingsMethod [1])){
		/** CULTURED CELLS - CELL CULTURE */
		if(!enterSettings(2)) {
			return;
		}
	}else if(!importSettings()) {
		IJ.error("Preferences could not be loaded due to file error...");
		return;
	}

	//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	//------------------------------processing------------------------------------
	//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

	String name [] = {"",""};
	String dir [] = {"",""};
	ImagePlus allImps [] = new ImagePlus [2];
//	RoiEncoder re;
	{
		//Improved file selector
		try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception e){}
		if(selectedTaskVariant.equals(taskVariant[1])){
			OpenFilesDialog od = new OpenFilesDialog ();
			od.setLocation(0,0);
			od.setVisible(true);
			
			od.addWindowListener(new java.awt.event.WindowAdapter() {
		        public void windowClosing(WindowEvent winEvt) {
		        	return;
		        }
		    });
		
			//Waiting for od to be done
			while(od.done==false){
				try{
					Thread.currentThread().sleep(50);
			    }catch(Exception e){
			    }
			}
			
			tasks = od.filesToOpen.size();
			name = new String [tasks];
			dir = new String [tasks];
			for(int task = 0; task < tasks; task++){
				name[task] = od.filesToOpen.get(task).getName();
				dir[task] = od.filesToOpen.get(task).getParent() + System.getProperty("file.separator");
			}		
		}else if(selectedTaskVariant.equals(taskVariant[0])){
			if(WindowManager.getIDList()==null){
				new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
				return;
			}
			FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
			name [0] = info.fileName;	//get name
			dir [0] = info.directory;	//get directory
			tasks = 1;
		}else if(selectedTaskVariant.equals(taskVariant[2])){	// all open images
			if(WindowManager.getIDList()==null){
				new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
				return;
			}
			int IDlist [] = WindowManager.getIDList();
			tasks = IDlist.length;	
			if(tasks == 1){
				selectedTaskVariant=taskVariant[0];
				FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
				name [0] = info.fileName;	//get name
				dir [0] = info.directory;	//get directory
			}else{
				name = new String [tasks];
				dir = new String [tasks];
				allImps = new ImagePlus [tasks];
				for(int i = 0; i < tasks; i++){
					allImps[i] = WindowManager.getImage(IDlist[i]); 
					FileInfo info = allImps[i].getOriginalFileInfo();
					name [i] = info.fileName;	//get name
					dir [i] = info.directory;	//get directory
				}		
			}
					
		}
	}
	
	//For BioFormats - screen for series and add tasks accordingly
	ImporterOptions bfOptions;
	int series [] = new int [tasks];
	int totSeries [] = new int [tasks];
	Arrays.fill(series, 0);
	Arrays.fill(totSeries, 1);
	String loadSeriesTemp;
	String removedFiles = "\n";
	
//	String filesList = "Files to process:\n";
	if(selectedTaskVariant.equals(taskVariant[1])){
		for(int i = tasks-1; i >= 0; i--){
			IJ.showProgress((tasks-i)/tasks);
			if(name [i].substring(name[i].lastIndexOf(".")).equals(".tif")
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".TIF")
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".tiff")
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".TIFF")
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".png")
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".PNG")
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".jpg")
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".JPG")
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".jpeg")
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".JPEG")) {
				continue;
			}
			try {
				bfOptions = new ImporterOptions();
				bfOptions.setId(""+dir[i]+name[i]+"");
				bfOptions.setVirtual(true);
				
				int nOfSeries = getNumberOfSeries(bfOptions);
//				IJ.log("nSeries: " + nOfSeries);
				
				if(loadSeries.equals("ALL") && nOfSeries > 1) {
					String [] nameTemp = new String [name.length+nOfSeries-1], 
							dirTemp = new String [name.length+nOfSeries-1];
					int [] seriesTemp = new int [nameTemp.length],
							totSeriesTemp = new int [nameTemp.length]; 
					for(int j = 0; j < i; j++) {
						nameTemp [j] = name [j]; 
						dirTemp [j] = dir [j];
						seriesTemp [j] = series [j];
						totSeriesTemp [j] = totSeries [j];
						
					}
					for(int j = 0; j < nOfSeries; j++) {
						nameTemp [i+j] = name [i]; 
						dirTemp [i+j] = dir [i];
						seriesTemp [i+j] = j;
						totSeriesTemp [i+j] = nOfSeries;
					}
					for(int j = i+1; j < name.length; j++) {
						nameTemp [j+nOfSeries-1] = name [j]; 
						dirTemp [j+nOfSeries-1] = dir [j];
						seriesTemp [j+nOfSeries-1] = series [j];
						totSeriesTemp [j+nOfSeries-1] = totSeries [j];
					}
					
					//copy arrays
					tasks = nameTemp.length;
					name = new String [tasks];
					dir = new String [tasks];
					series = new int [tasks];
					totSeries = new int [tasks];
					
					for(int j = 0; j < nameTemp.length; j++) {
						name [j] = nameTemp [j];
						dir [j] = dirTemp [j];
						series [j] = seriesTemp [j];
						totSeries [j] = totSeriesTemp [j];
//							filesList += name[j] + "\t" + dir[j] + "\t" + series[j] + "\t" + totSeries[j] + "\n";
					}
				}else if(!loadSeries.equals("ALL")){
					if(loadSeries.equals("SLIDESCANNER")){
						loadSeriesTemp = "";
						for(int ser = 0; ser < nOfSeries; ser++) {
							if(getSeriesName(bfOptions, ser).startsWith("Series_" + (ser+1) + ": " + name[i])) {
								if(ser == 0 || (getWidthHeigth(bfOptions, ser)[0] > getWidthHeigth(bfOptions, ser-1)[0]
										&& getWidthHeigth(bfOptions, ser)[1] > getWidthHeigth(bfOptions, ser-1)[1])
										&& getSeriesName(bfOptions, ser-1).startsWith("Series_" + (ser) + ": " + name[i])) {
									if(loadSeriesTemp.length() == 0) {
										loadSeriesTemp = "" + (ser+1);
									}else {
										loadSeriesTemp += "," + (ser+1);
									}
//								}else {
//									IJ.log("Name not matched " + ser + ": code = " 
//											+ getWidthHeigth(bfOptions, ser)[0] + ">" + getWidthHeigth(bfOptions, ser-1)[0] 
//													+ " and " + getWidthHeigth(bfOptions, ser)[1]  + ">"
//											+ getWidthHeigth(bfOptions, ser-1)[1] + " and " 
//													+ getSeriesName(bfOptions, ser-1).startsWith("Series_" + (ser) + ": " + name[i]));
								}
//							}else {
//								IJ.log("Name not matched " + ser + ": title = " + getSeriesName(bfOptions, ser) + " vs name = " + name[i]);
							}
						}
						IJ.log("LoadSeriesTemp: " + loadSeriesTemp);
					}else {
						loadSeriesTemp = loadSeries;						
					}
					

					int nrOfSeriesImages = 0;
					for(int j = 0; j < nOfSeries; j++) {
						if(!(","+loadSeriesTemp+",").contains(","+(j+1)+",")) {
							continue;
						}
						nrOfSeriesImages++;
					}
					

					if(nrOfSeriesImages>0) {
						//ADD SERIES TO LIST
						String [] nameTemp = new String [name.length+nrOfSeriesImages-1], 
								dirTemp = new String [name.length+nrOfSeriesImages-1];
						int [] seriesTemp = new int [nameTemp.length],
								totSeriesTemp = new int [nameTemp.length]; 
						for(int j = 0; j < i; j++) {
							nameTemp [j] = name [j]; 
							dirTemp [j] = dir [j];
							seriesTemp [j] = series [j];
							totSeriesTemp [j] = totSeries [j];
							
						}
						int k = 0;
						for(int j = 0; j < nOfSeries; j++) {
							if(!(","+loadSeriesTemp+",").contains(","+(j+1)+",")) {
								continue;
							}
							nameTemp [i+k] = name [i]; 
							dirTemp [i+k] = dir [i];
							seriesTemp [i+k] = j;
							totSeriesTemp [i+k] = nOfSeries;
							k++;
						}
						
						for(int j = i+1; j < name.length; j++) {
							nameTemp [j+nrOfSeriesImages-1] = name [j]; 
							dirTemp [j+nrOfSeriesImages-1] = dir [j];
							seriesTemp [j+nrOfSeriesImages-1] = series [j];
							totSeriesTemp [j+nrOfSeriesImages-1] = totSeries [j];
						}
						
						//copy arrays

						tasks = nameTemp.length;
						name = new String [tasks];
						dir = new String [tasks];
						series = new int [tasks];
						totSeries = new int [tasks];
						
						for(int j = 0; j < nameTemp.length; j++) {
							name [j] = nameTemp [j];
							dir [j] = dirTemp [j];
							series [j] = seriesTemp [j];
							totSeries [j] = totSeriesTemp [j];
//								filesList += name[j] + "\t" + dir[j] + "\t" + series[j] + "\t" + totSeries[j] + "\n";
						}
					}else {
						//REMOVE NAME FROM LIST
						removedFiles += name [i] + "\n";
						String [] nameTemp = new String [name.length-1], 
								dirTemp = new String [name.length-1];
						int [] seriesTemp = new int [nameTemp.length],
								totSeriesTemp = new int [nameTemp.length]; 
						for(int j = 0; j < i; j++) {
							nameTemp [j] = name [j]; 
							dirTemp [j] = dir [j];
							seriesTemp [j] = series [j];
							totSeriesTemp [j] = totSeries [j];
							
						}
						for(int j = i+1; j < name.length; j++) {
							nameTemp [j-1] = name [j]; 
							dirTemp [j-1] = dir [j];
							seriesTemp [j-1] = series [j];
							totSeriesTemp [j-1] = totSeries [j];
						}
						
						//copy arrays

						tasks = nameTemp.length;
						name = new String [tasks];
						dir = new String [tasks];
						series = new int [tasks];
						totSeries = new int [tasks];
						
						for(int j = 0; j < nameTemp.length; j++) {
							name [j] = nameTemp [j];
							dir [j] = dirTemp [j];
							series [j] = seriesTemp [j];
							totSeries [j] = totSeriesTemp [j];
//								filesList += name[j] + "\t" + dir[j] + "\t" + series[j] + "\t" + totSeries[j] + "\n";
						}
					}
				}
			} catch (Exception e) {
				IJ.log(e.getCause().getLocalizedMessage());
				IJ.log(e.getCause().getMessage());
				e.printStackTrace();
			}
		}
	}
	
	if(tasks == 0) {
		new WaitForUserDialog("The series preference '" + loadSeries + "' did not fit to any file to be processed - Plugin cancelled.").show();
		return;
	}
	
	//add progressDialog
		progress = new ProgressDialog(name, series, tasks, 1);
		progress.setLocation(0,0);
		progress.setVisible(true);
		progress.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	        	if(processingDone==false){
	        		IJ.error("Script stopped...");
	        	}
	        	continueProcessing = false;	        	
	        	return;
	        }
		});
		
		if(removedFiles.length()>1) {
			progress.notifyMessage("For some files, the series preference '" 
					+ loadSeries + "' did not fit - these files were excluded from analysis:" 
					+ removedFiles + "", ProgressDialog.NOTIFICATION);			
		}
		
//		if(selectedTaskVariant.equals(taskVariant[1])){
//			progress.notifyMessage(filesList, ProgressDialog.LOG);	
//		}
	
	
   	ImagePlus imp, mask;
   	ImagePlus [] tempImp;
   	CompositeImage outImp;
   	TextPanel tp1;
   	double threshold;
   	Date startDate;
   	LUT [] originalLuts;
   	LUT [] newLuts;
   	String sliceLabels [][][];
   	int indexOld, indexNew;

	boolean backgroundPref = Prefs.blackBackground;

	starDistLUT = getLUT(STARDISTLUTNUMBERS, false);
	
	if(keepAwake) {
   		try {
			robo = new Robot();
		} catch (AWTException e) {
			keepAwake = false;
			progress.notifyMessage("Robot that moves the mouse to keep the computer awake could not be hired - Stay-awake-mode was disabled.", ProgressDialog.NOTIFICATION);
		}
   	}
		
	for(int task = 0; task < tasks; task++){
		Prefs.blackBackground =  true;
		running: while(continueProcessing){
			startDate = new Date();
			progress.updateBarText("in progress...");
			//Check for problems
			if(name[task].contains(".") && name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".txt")){
				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}
			if(name[task].contains(".") && name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".zip")){	
				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}					
			//Check for problems

			//open Image
		   	try{
		   		if(selectedTaskVariant.equals(taskVariant[1])){
		   			if(name[task].contains(".tif") || name[task].contains(".tiff") || name[task].contains(".png") || name[task].contains(".jpeg")
		   					|| name[task].contains(".JPEG") || name[task].contains(".jpg") || name[task].contains(".JPG") 
		   					|| name[task].contains(".TIF") || name[task].contains(".TIFF") || name[task].contains(".PNG")){
		   				//TIFF file
		   				imp = IJ.openImage(""+dir[task]+name[task]+"");		
		   			}else{
		   				//bio format reader
		   				bfOptions = new ImporterOptions();
		   				bfOptions.setId(""+dir[task]+name[task]+"");
		   				bfOptions.setVirtual(false);
		   				bfOptions.setAutoscale(true);
		   				bfOptions.setColorMode(ImporterOptions.COLOR_MODE_COMPOSITE);
		   				for(int i = 0; i < totSeries[task]; i++) {
		   					if(i==series[task]) {
		   						bfOptions.setSeriesOn(i, true);
		   					}else {
		   						bfOptions.setSeriesOn(i, false);
		   					}
		   				}
		   				ImagePlus [] imps = BF.openImagePlus(bfOptions);
//		   				IJ.run("Bio-Formats", "open=[" +dir[task] + name[task]
//		   						+ "] autoscale color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT");
		   				imp = imps [0];	
		   				imp.setDisplayMode(IJ.COMPOSITE);
		   			}
		   			imp.hide();
					imp.deleteRoi();
//		   			imp = IJ.openImage(""+dir[task]+name[task]+"");			   			
//					imp.deleteRoi();
		   		}else if(selectedTaskVariant.equals(taskVariant[0])){
		   			imp = WindowManager.getCurrentImage();
		   			imp.deleteRoi();
		   		}else{
		   			imp = allImps[task];
		   			imp.deleteRoi();
		   		}
		   	}catch (Exception e) {
		   		progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": file is no image - could not be processed!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}
		   	//open Image
		   	
		   	//Check for problems with the image
		   	if(imp.getNFrames()>1){	
				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Could not be processed. Analysis of multi-frame images not yet implemented!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}			
			if(imp.getNSlices()>1){	
				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Could not be processed. Analysis of 3D images not yet implemented!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}			
			if(imp.getBitDepth() == 24) {
				//RGB image > Convert to RGB stack
				imp = CompositeConverter.makeComposite(imp);
				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Images was in RGB format. Thus, images was automatically converted into a 3-channel stack image.", ProgressDialog.LOG);	
			}
			if(channelIDs [0] < 1 || channelIDs [0] > imp.getNChannels()) {
				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Could not be processed. Selected channel does not exist in the image!"
						+ " Select a channel number between 1 and the total number of channels in the image.", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}			
		   	//Check for problems with the image
		   	
		   	//Create Outputfilename
		   	progress.updateBarText("Create output filename");				
			String filePrefix;
			if(name[task].contains(".")){
				filePrefix = name[task].substring(0,name[task].lastIndexOf("."));
			}else{
				filePrefix = name[task];
			}
			if(totSeries [task] > 1) {
				filePrefix += "_s" + (series[task] + 1);
			}
			
			filePrefix += "_AQP";
			
			if(chosenOutputName.equals(outputVariant[1])){
				//saveDate
				filePrefix += "_" + NameDateFormatter.format(startDate);
			}
			
			filePrefix = dir[task] + filePrefix;
		   	
			
		/******************************************************************
		*** 						Processing							***	
		*******************************************************************/
			//start logging metadata
			tp1 = new TextPanel("results");
			addSettingsBlockToPanel(tp1,  startDate, name[task], totSeries[task]>1, series[task], imp);
			tp1.append("");
			
			//processing			
			progress.updateBarText("Extract channel " + channelIDs [0] + " ...");
			
			originalLuts = new LUT [imp.getNChannels()];
			imp.setDisplayMode(IJ.COMPOSITE);
		   	for(int c = 0; c < imp.getNChannels(); c++){
		   		imp.setC(c+1);
		   		originalLuts [c] = imp.getChannelProcessor().getLut();
		   	}
		   	
		   	sliceLabels = new String [imp.getNChannels()][imp.getNFrames()][imp.getNSlices()];
		   	for(int c = 0; c < imp.getNChannels(); c++) {
		   		for(int s = 0; s < imp.getNSlices(); s++){
					for(int f = 0; f < imp.getNFrames(); f++){
					indexOld = imp.getStackIndex(c+1, s+1, f+1)-1;
						try{
							if(imp.getStack().getSliceLabel(indexOld+1).equals(null)){
	   						sliceLabels [c][f][s] = "C" + (c+1) + "/" + imp.getNChannels() + " S" + (s+1) + "/" + imp.getNSlices() 
	   							+  " T" + (f+1) + "/" + imp.getNFrames();
		   					}else if(imp.getStack().getSliceLabel(indexOld+1).isEmpty()){
		   						sliceLabels [c][f][s] = "C" + (c+1) + "/" + imp.getNChannels() + " S" + (s+1) + "/" + imp.getNSlices() 
									+  " T" + (f+1) + "/" + imp.getNFrames();
		   					}else{
		   						sliceLabels [c][f][s] = imp.getStack().getSliceLabel(indexOld+1);
		   					}
						}catch(Exception e){
							sliceLabels [c][f][s] =  "C" + (c+1) + "/" + imp.getNChannels() + " S" + (s+1) + "/" + imp.getNSlices() 
							+  " T" + (f+1) + "/" + imp.getNFrames();
						}
					}
		   		}
		   	}
		   	
		   	
		   	/*
		   	 * Process image
		   	 * */
		   	tempImp = new ImagePlus [numberOfChannels];
		   	for(int segmC = 0; segmC < numberOfChannels; segmC++) {
		   		tempImp [segmC] = copyChannel(imp, channelIDs [segmC], false, false);
		   		if(keepAwake) {
					stayAwake();
				}
		   	}
		   	
		   	/*
		   	 * Calculate 
		   	 * */
		   	double pixelWidth = imp.getCalibration().pixelWidth;
		   	double pixelHeight = imp.getCalibration().pixelHeight;
		   	double pixelDepth = imp.getCalibration().pixelDepth;
		   	String pixelUnit = imp.getCalibration().getUnit();
		   	
		   	for(int segmC = 0; segmC < numberOfChannels; segmC++) {
//	   			tempImp [segmC].show();
//				new WaitForUserDialog("before treatment").show();
//				tempImp [segmC].hide();
		   		
		   		if(subtractBackground [segmC]) {
		   			progress.updateBarText("Subtract background " + dfDialog.format(subtractBGRadius [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight)) + " " + pixelUnit + "");		
		   			if(darkBackground [segmC]) {
		   				IJ.run(tempImp [segmC], "Subtract Background...", "rolling=" + (subtractBGRadius [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight)) + "");
		   			}else {
			   			IJ.run(tempImp [segmC], "Subtract Background...", "rolling=" + (subtractBGRadius [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight)) + " light");
		   			}					
					progress.addToBar(1.0/(double)numberOfChannels*0.1);					

//					tempImp [segmC].show();
//					new WaitForUserDialog("sbgd").show();
//					tempImp [segmC].hide();
		   		}
				
				if(preBlur [segmC]) {
					progress.updateBarText("Bluring image ... " + dfDialog.format(preBlurSigma [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight)) + " " + pixelUnit + "");
					tempImp [segmC].getProcessor().blurGaussian(preBlurSigma [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight));
					progress.addToBar(1.0/(double)numberOfChannels*0.05);
					
//					tempImp [segmC].show();
//					new WaitForUserDialog("blurred").show();
//					tempImp [segmC].hide();
				}
				
				if(subtractBluredImage [segmC]) {
					progress.updateBarText("Subtract blured image ... " + dfDialog.format(subtractBlurSigma [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight)) + " " + pixelUnit + "");
					tempImp [segmC] = subtractABluredImage(tempImp [segmC], subtractBlurSigma [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight));
					progress.addToBar(1.0/(double)numberOfChannels*0.05);

//					tempImp.show();
//					new WaitForUserDialog("blur substr").show();
//					tempImp.hide();
				}
			
	   			if(keepAwake) {
					stayAwake();
				}
				
	   			
				Roi regionsAboveZero = null;
				if(excludeZeroRegions [segmC]) {
					progress.updateBarText("get non-zero-pixel ROI (close-gaps radius = " + dfDialog.format(closeGapsRadius [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight)) + " " + pixelUnit + ")...");
					regionsAboveZero = getRegionsAboveZeroAsROI(tempImp [segmC], 1, closeGapsRadius [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight));	
					tempImp [segmC].setRoi(regionsAboveZero);
					//save ROI
					RoiEncoder re;
					try{	
						re = new RoiEncoder(filePrefix + "_ROI_C" + (segmC+1) + ".roi");					
						re.write(regionsAboveZero);				
					}catch(Exception e){
						IJ.error("Failed to correctly save rois!");
					}
				}
				
				if(chosenAlgorithm [segmC].equals("CUSTOM threshold")) {
					progress.updateBarText("Set custom threshold " + customThr + " ...");
					threshold = customThr [segmC];
					tp1.append("Used " + chosenAlgorithm + " as intensity threshold - threshold value:	" + df6.format(threshold));

					progress.addToBar(1.0/(double)numberOfChannels*0.5);
					
					progress.updateBarText("Segment image with threshold " + dfDialog.format(threshold) + " ...");
					segmentImage(tempImp [segmC], threshold, 0, tempImp [segmC], 0, false, darkBackground [segmC]);

					progress.addToBar(1.0/(double)numberOfChannels*0.1);
				}else if (chosenAlgorithm [segmC].equals("StarDist")){
					tempImp [segmC].show();
					progress.updateBarText("StarDist detection running ...");
					String runText = ("command=[de.csbdresden.stardist.StarDist2D], "
							+ "args=['input':'" + tempImp[segmC].getTitle() +"', 'modelChoice':'" + selectedStarDistModel [segmC] + "', ");
							if(starDistNormalizeImage[segmC]) {
								runText += "'normalizeInput':'true', ";
							}else {
								runText += "'normalizeInput':'false', ";
							}							
							runText += "'percentileBottom':'" + dfDialog.format(starDistPercentileLow[segmC]) + "', 'percentileTop':'" + dfDialog.format(starDistPercentileHigh[segmC]) + "', "
									+ "'probThresh':'" + dfDialog.format(starDistProbabilityScore[segmC]) + "', " + "'nmsThresh':'" + dfDialog.format(starDistOverlapThreshold[segmC]) 
									+ "', 'outputType':'Label Image', ";
							if(selectedStarDistModel [segmC].equals("Model (.zip) from File")){
								runText += "'modelFile':'" + starDistModelPath [segmC] + "', ";
							}
							runText += "'nTiles':'" + starDistNTiles [segmC] + "', 'excludeBoundary':'2', 'roiPosition':'Automatic', "
							+ "'verbose':'false', 'showCsbdeepProgress':'false', 'showProbAndDist':'false'], process=[false]";
					
					IJ.run(tempImp[segmC], "Command From Macro",runText);
					
					tempImp [segmC].changes = false;
					tempImp [segmC].close();
					tempImp [segmC] = WindowManager.getImage("Label Image").duplicate();
					tempImp [segmC].hide();
					WindowManager.getImage("Label Image").changes = false;
					WindowManager.getImage("Label Image").close();
					
					progress.addToBar(1.0/(double)numberOfChannels*0.6);
				}else {
//					tempImp.show();
//					new WaitForUserDialog("before thr").show();
//					tempImp.hide();
					progress.updateBarText("Determine threshold using " + chosenAlgorithm [segmC] + " ...");
					if(darkBackground [segmC]) {
						threshold = getSingleSliceImageThresholds(tempImp [segmC], 1, chosenAlgorithm [segmC], darkBackground [segmC])[0];
					}else {
						threshold = getSingleSliceImageThresholds(tempImp [segmC], 1, chosenAlgorithm [segmC], darkBackground [segmC])[1];					
					}
					tp1.append("Used " + chosenAlgorithm [segmC] + " to determine the intensity threshold - threshold value:	" + df6.format(threshold));
					
					progress.addToBar(1.0/(double)numberOfChannels*0.1);
					
//					tempImp.show();
//					new WaitForUserDialog("after thr").show();
//					tempImp.hide();
					
					progress.updateBarText("Segment image with threshold " + dfDialog.format(threshold) + " ...");
					segmentImage(tempImp [segmC], threshold, 0, tempImp [segmC], 0, false, darkBackground [segmC]);
					
//					tempImp.show();
//					new WaitForUserDialog("bin").show();
//					tempImp.hide();
					
					progress.addToBar(1.0/(double)numberOfChannels*0.1);				
					
				}			
				
				if(excludeZeroRegions [segmC]) {
					progress.updateBarText("Set pixels outside non-zero-Pixel ROI to zero in mask...");
					setRegionsOutsideRoiToZero(tempImp [segmC], 1, regionsAboveZero);
				}
				progress.addToBar(1.0/(double)numberOfChannels*0.1);

	   			if(keepAwake) {
					stayAwake();
				}
				
	   			if(despeckle [segmC] || removeParticles [segmC] || fillHoles [segmC] || watershed [segmC]) {
	   				tempImp [segmC].deleteRoi();							   	
				   	if(tempImp [segmC].getBitDepth()!=8) {
						tempImp [segmC] = getOtherBitImageFromBinary32bit(tempImp [segmC], false, 8);
//					   	tempImp [segmC].show();
//						new WaitForUserDialog("bitconv").show();
//						tempImp [segmC].hide();	
					}		   	
			   	
					if(despeckle [segmC]) {
						progress.updateBarText("Despeckle mask");
						IJ.run(tempImp [segmC], "Despeckle", "");
					}
	
					progress.addToBar(1.0/(double)numberOfChannels*0.1);
	
		   			if(keepAwake) {
						stayAwake();
					}
					if(removeParticles [segmC]) {
						if(keepAwake) {
							stayAwake();
						}
						
						progress.updateBarText("Get mask with filled holes and closed gaps (radius " + dfDialog.format(removeRadius [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight)) + " " + pixelUnit + ")...");
						mask = getFillHolesAndRemoveNoise(tempImp [segmC], linkGapsForRemoveRadius [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight), removeRadius [segmC] / (0.5 * pixelWidth + 0.5 * pixelHeight), segmC);
						
						progress.addToBar(1.0/(double)numberOfChannels*0.1);
	
//					   	tempImp [segmC].show();
//						new WaitForUserDialog("calc").show();
//						tempImp [segmC].hide();	
					
					   	progress.updateBarText("Invert image...");
//						IJ.run(tempImp, "Invert", "");
						tempImp [segmC].getProcessor().invert();
	
			   			if(keepAwake) {
							stayAwake();
						}
			   			
//					   	tempImp [segmC].show();
//						new WaitForUserDialog("calc").show();
//						tempImp [segmC].hide();	
						
						progress.addToBar(1.0/(double)numberOfChannels*0.1);
						
						ImageCalculator ic = new ImageCalculator();
						tempImp [segmC] = ic.run("AND create", tempImp [segmC], mask);
	
						mask.changes = false;
						mask.close();
						progress.addToBar(1.0/(double)numberOfChannels*0.1);
					}else {
						progress.addToBar(1.0/(double)numberOfChannels*0.3);
					}
							
//			   		tempImp [segmC].show();
//					new WaitForUserDialog("Prefill").show();
//					tempImp [segmC].hide();	
				
					if(fillHoles [segmC]) {
			   			if(keepAwake) {
							stayAwake();
						}
						progress.updateBarText("Fill holes...");
						IJ.run(tempImp [segmC], "Fill Holes", "");
					}
	
					if(watershed [segmC]) {
			   			if(keepAwake) {
							stayAwake();
						}
						progress.updateBarText("Watershed...");
						IJ.run(tempImp [segmC], "Watershed", "");
					}

//				   	tempImp [segmC].show();
//					new WaitForUserDialog("calc").show();
//					tempImp [segmC].hide();
	   			}else {
	   				progress.addToBar(1.0/(double)numberOfChannels*0.4);
	   			}
				
	   			if(keepAwake) {
					stayAwake();
				}
		   	}
		   	
		   	/*
		   	 * Generate output image
		   	 * */

		   	tp1.append("");
			if(deleteOtherChannels) {
				if(includeDuplicateChannel){
					progress.updateBarText("Add duplicated channel");
			   		outImp = (CompositeImage) IJ.createHyperStack(imp.getTitle() + " cq", imp.getWidth(), imp.getHeight(), 
			   				2*numberOfChannels, imp.getNSlices(), imp.getNFrames(), imp.getBitDepth());
			   		outImp.setCalibration(imp.getCalibration());
			   		outImp.setDisplayMode(IJ.COMPOSITE);
			   		
			   		int c = 0;
				   	newLuts = new LUT [outImp.getNChannels()];

				   	tp1.append("Channels in output image:");
			   		for(int segmC = 0; segmC < numberOfChannels; segmC++) {
			   			newLuts [c] = originalLuts [channelIDs [segmC]-1];
			   			if(chosenAlgorithm[segmC].equals("StarDist")) {
			   				newLuts [c] = starDistLUT;
		   					outImp.setC(c+1);
		   					outImp.getProcessor().resetMinAndMax();
		   					outImp.setDisplayRange(outImp.getProcessor().getMin(), outImp.getProcessor().getMax());	
			   			}
				   		newLuts [c+1] = originalLuts [channelIDs [segmC]-1];

				   		tp1.append("Channel " + (c+1) + ":	" + "previous channel " + (channelIDs [segmC]) + " (segmented)");
						tp1.append("Channel " + (c+2) + ":	" + "previous channel " + (channelIDs [segmC]) + " (unsegmented)");
						
		   				for(int s = 0; s < imp.getNSlices(); s++){
		   					for(int f = 0; f < imp.getNFrames(); f++){
   								indexOld = tempImp[segmC].getStackIndex(1, s+1, f+1)-1;
		   						indexNew = outImp.getStackIndex(c+1, s+1, f+1)-1;
			   					outImp.getStack().setSliceLabel("segm " + sliceLabels [channelIDs[segmC]-1][f][s], indexNew+1);
			   					
		   						for(int x = 0; x < imp.getWidth(); x++){
						   			for(int y = 0; y < imp.getHeight(); y++){
								   		outImp.getStack().setVoxel(x, y, indexNew, tempImp [segmC].getStack().getVoxel(x, y, indexOld));
						   			}
		   						}
		   						
		   						indexOld = imp.getStackIndex(channelIDs [segmC], s+1, f+1)-1;
		   						indexNew = outImp.getStackIndex(c+2, s+1, f+1)-1;
			   					outImp.getStack().setSliceLabel("" + sliceLabels [channelIDs[segmC]-1][f][s], indexNew+1);
			   					
		   						for(int x = 0; x < imp.getWidth(); x++){
						   			for(int y = 0; y < imp.getHeight(); y++){
				   						outImp.getStack().setVoxel(x, y, indexNew, imp.getStack().getVoxel(x, y, indexOld));
				   					}					
				   				}
				   			}
				   		}
			   			tempImp [segmC].changes = false;
						tempImp [segmC].close();					
						
						c += 2;
			   		}
			   		
					outImp.setDisplayMode(IJ.COMPOSITE);
					outImp.setLuts(newLuts);
   					outImp.updateAllChannelsAndDraw();
					IJ.saveAsTiff(outImp, filePrefix + ".tif");
					outImp.changes = false;
					outImp.close();
			   	}else{
			   		outImp = (CompositeImage) IJ.createHyperStack(imp.getTitle() + " cq", imp.getWidth(), imp.getHeight(), 
			   				numberOfChannels, imp.getNSlices(), imp.getNFrames(), imp.getBitDepth());
			   		outImp.setCalibration(imp.getCalibration());
			   		outImp.setDisplayMode(IJ.COMPOSITE);
			   		
			   		int c = 0;
				   	newLuts = new LUT [outImp.getNChannels()];

				   	tp1.append("Channels in output image:");
			   		for(int segmC = 0; segmC < numberOfChannels; segmC++) {
			   			newLuts [c] = originalLuts [channelIDs [segmC]-1];
			   			if(chosenAlgorithm[segmC].equals("StarDist")) {
			   				newLuts [c] = starDistLUT;
		   					outImp.setC(c+1);
		   					outImp.getProcessor().resetMinAndMax();
		   					outImp.setDisplayRange(outImp.getProcessor().getMin(), outImp.getProcessor().getMax());
			   			}

				   		tp1.append("Channel " + (c+1) + ":	" + "previous channel " + (channelIDs [segmC]) + " (segmented)");			   			
		   				for(int s = 0; s < imp.getNSlices(); s++){
		   					for(int f = 0; f < imp.getNFrames(); f++){
   								indexOld = tempImp[segmC].getStackIndex(1, s+1, f+1)-1;
		   						indexNew = outImp.getStackIndex(c+1, s+1, f+1)-1;
		   						for(int x = 0; x < imp.getWidth(); x++){
						   			for(int y = 0; y < imp.getHeight(); y++){
				   						outImp.getStack().setVoxel(x, y, indexNew, tempImp [segmC].getStack().getVoxel(x, y, indexOld));
				   					}					
				   				}
			   					outImp.getStack().setSliceLabel("segm " + sliceLabels [channelIDs[segmC]-1][f][s], indexNew+1);
				   			}
				   		}
			   			tempImp [segmC].changes = false;
						tempImp [segmC].close();
						
						c++;
			   		}

					outImp.setDisplayMode(IJ.COMPOSITE);
					outImp.setLuts(newLuts);
   					outImp.updateAllChannelsAndDraw();
					IJ.saveAsTiff(outImp, filePrefix + ".tif");
					outImp.changes = false;
					outImp.close();
			   	}
			}else {
				if(includeDuplicateChannel){
					double maxValue = Math.pow(2.0, imp.getBitDepth())-1.0;
					
					outImp = (CompositeImage) IJ.createHyperStack(imp.getTitle() + " lq", imp.getWidth(), imp.getHeight(), 
			   				imp.getNChannels()+numberOfChannels,
			   				imp.getNSlices(), imp.getNFrames(), imp.getBitDepth());
			   		outImp.setCalibration(imp.getCalibration());
			   		outImp.setDisplayMode(IJ.COMPOSITE);
			   		
			   		int cNew = 0;
			   		for(int x = 0; x < imp.getWidth(); x++){
			   			for(int y = 0; y < imp.getHeight(); y++){
			   				for(int s = 0; s < imp.getNSlices(); s++){
			   					for(int f = 0; f < imp.getNFrames(); f++){
		   							cNew = 0;
			   						for(int c = 0; c < imp.getNChannels(); c++){
			   							for(int segmC = 0; segmC < channelIDs.length; segmC++){
			   								if(c+1 == channelIDs [segmC]){
			   									indexOld = tempImp [segmC].getStackIndex(1, s+1, f+1)-1;
						   						indexNew = outImp.getStackIndex(c+cNew+1, s+1, f+1)-1;
				   								if(outImp.getBitDepth() != tempImp[segmC].getBitDepth()) {
							   						if(tempImp [segmC].getStack().getVoxel(x, y, indexOld) != 0.0) {
							   							outImp.getStack().setVoxel(x, y, indexNew, maxValue);
							   						}							   						
				   						   		}else {
				   						   			outImp.getStack().setVoxel(x, y, indexNew, tempImp [segmC].getStack().getVoxel(x, y, indexOld));
				   						   		}
				   								cNew ++;
						   						break;						   						
				   							}
			   							}
			   							indexOld = imp.getStackIndex(c+1, s+1, f+1)-1;
				   						indexNew = outImp.getStackIndex(c+cNew+1, s+1, f+1)-1;
				   						outImp.getStack().setVoxel(x, y, indexNew, imp.getStack().getVoxel(x, y, indexOld));
			   						}
			   					}					
			   				}
			   			}
			   		}
			   		
				   	newLuts = new LUT [outImp.getNChannels()];
				   	
				   	tp1.append("Channels in output image:");
				   	cNew = 0;
				   	String copyStr;
				   	boolean search;
			   		for(int c = 0; c < imp.getNChannels(); c++){
			   			if(keepAwake) {
							stayAwake();
						}
						newLuts [c+cNew] = originalLuts [c];
			   			
			   			search = false;
						for(int i = 0; i < channelIDs.length; i++){
							if(c+1 == channelIDs [i]){
	   							search = true;
	   							break;
							}
						}
						
						if(search){
							tp1.append("Channel " + (c+1+cNew) + ":	" + "previous channel " + (c+1) + " (segmented)");
							for(int s = 0; s < imp.getNSlices(); s++){
			   					for(int f = 0; f < imp.getNFrames(); f++){
			   						indexOld = imp.getStackIndex(c+1, s+1, f+1)-1;
				   					indexNew = outImp.getStackIndex(c+cNew+1, s+1, f+1)-1;
				   					try{
				   						if(imp.getStack().getSliceLabel(indexOld+1).equals(null)){
					   						copyStr = "C" + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
					   							+  " T" + (f+1) + "/" + imp.getNFrames();
					   					}else if(imp.getStack().getSliceLabel(indexOld+1).isEmpty()){
					   						copyStr = "C" + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
				   							+  " T" + (f+1) + "/" + imp.getNFrames();
					   					}else{
					   						copyStr = imp.getStack().getSliceLabel(indexOld+1);
					   					}
				   					}catch(Exception e){
				   						copyStr = "C" + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
			   							+  " T" + (f+1) + "/" + imp.getNFrames();
				   					}				   					
				   					outImp.getStack().setSliceLabel("segm " + copyStr, indexNew+1);
			   					}
							}
							
							for(int i = 0; i < channelIDs.length; i++){
								if(c+1 == channelIDs [i]){
									if(chosenAlgorithm[i].equals("StarDist")) {
							   			newLuts [c+cNew] = starDistLUT;
							   		}
									cNew ++;
	   								newLuts [c+cNew] = originalLuts [c];	   								
	   								tp1.append("Channel " + (c+1+cNew) + ":	" + "previous channel " + (c+1) + "");
	   								
	   								for(int s = 0; s < imp.getNSlices(); s++){
	   				   					for(int f = 0; f < imp.getNFrames(); f++){
		   				   					indexOld = imp.getStackIndex(c+1, s+1, f+1)-1;
						   					indexNew = outImp.getStackIndex(c+cNew+1, s+1, f+1)-1;
						   					try{
						   						if(imp.getStack().getSliceLabel(indexOld+1).equals(null)){
							   						copyStr = "C" + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
							   							+  " T" + (f+1) + "/" + imp.getNFrames();
							   					}else if(imp.getStack().getSliceLabel(indexOld+1).isEmpty()){
							   						copyStr = "C" + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
						   							+  " T" + (f+1) + "/" + imp.getNFrames();
							   					}else{
							   						copyStr = imp.getStack().getSliceLabel(indexOld+1);
							   					}
						   					}catch(Exception e){
						   						copyStr = "C" + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
					   							+  " T" + (f+1) + "/" + imp.getNFrames();
						   					}					   					
						   					outImp.getStack().setSliceLabel(copyStr, indexNew+1);
	   				   					}
	   								}  
	   								break;
								}
							}
						}else{
							tp1.append("Channel " + (c+1+cNew) + ":	" + "previous channel " + (c+1) + "");
							for(int s = 0; s < imp.getNSlices(); s++){
			   					for(int f = 0; f < imp.getNFrames(); f++){
			   						indexOld = imp.getStackIndex(c+1, s+1, f+1)-1;
				   					indexNew = outImp.getStackIndex(c+cNew+1, s+1, f+1)-1;
				   					try{
				   						if(imp.getStack().getSliceLabel(indexOld+1).equals(null)){
					   						copyStr = "C" + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
					   							+  " T" + (f+1) + "/" + imp.getNFrames();
					   					}else if(imp.getStack().getSliceLabel(indexOld+1).isEmpty()){
					   						copyStr = "C" + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
				   							+  " T" + (f+1) + "/" + imp.getNFrames();
					   					}else{
					   						copyStr = imp.getStack().getSliceLabel(indexOld+1);
					   					}
				   					}catch(Exception e){
				   						copyStr = "C" + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
			   							+  " T" + (f+1) + "/" + imp.getNFrames();
				   					}			   					
				   					outImp.getStack().setSliceLabel(copyStr, indexNew+1);
			   					}
							}
						}
						
					}
			   		CompositeImage ci = (CompositeImage) outImp;
		   			ci.setDisplayMode(IJ.COMPOSITE);
		   			ci.setLuts(newLuts);
		   			for(int segmC = 0; segmC < channelIDs.length; segmC++) {
		   				if(chosenAlgorithm[segmC].equals("StarDist")) {
		   					ci.setC(channelIDs[segmC]);
							ci.getProcessor().resetMinAndMax();
							ci.setDisplayRange(ci.getProcessor().getMin(), ci.getProcessor().getMax());	
							ci.updateAllChannelsAndDraw();
		   				}						
					}
					IJ.saveAsTiff(ci, filePrefix + ".tif");
			   		outImp.changes = false;
					outImp.close();

					ci.changes = false;
					ci.close();				
			   	}else{
				   	tp1.append("Channels in output image: no change of order");
					for(int segmC = 0; segmC < channelIDs.length; segmC++){
						for(int s = 0; s < imp.getNSlices(); s++){
		   					for(int f = 0; f < imp.getNFrames(); f++){
		   						indexOld = tempImp [segmC].getStackIndex(1, s+1, f+1)-1;
	   							indexNew = imp.getStackIndex(channelIDs[segmC], s+1, f+1)-1;
		   						for(int x = 0; x < imp.getWidth(); x++){
		   							for(int y = 0; y < imp.getHeight(); y++){		
				   						imp.getStack().setVoxel(x, y, indexNew, tempImp [segmC].getStack().getVoxel(x, y, indexOld));
			   						}
			   					}	
		   	   					imp.getStack().setSliceLabel("segm " + sliceLabels [channelIDs[segmC]-1][f][s], indexNew+1);
			   				}
			   			}
						tempImp [segmC].changes = false;
						tempImp [segmC].close();
					}
					for(int segmC = 0; segmC < numberOfChannels; segmC++) {
						if(chosenAlgorithm[segmC].equals("StarDist")) {
							imp.setC(channelIDs[segmC]);
							imp.getProcessor().setLut(starDistLUT);
						}
					}
			   		IJ.saveAsTiff(imp, filePrefix + ".tif");
			   	}	
			}
	   		
	   		if(keepAwake) {
				stayAwake();
			}
			
		   	addFooter(tp1, startDate);				
			tp1.saveAs(filePrefix + ".txt");			

			progress.updateBarText("Finished ...");
			
			/******************************************************************
			*** 							Finish							***	
			*******************************************************************/			
			{
				imp.unlock();	
				if(selectedTaskVariant.equals(taskVariant[1])){
					imp.changes = false;
					imp.close();
				}
				processingDone = true;
				break running;
			}				
		}	
		progress.updateBarText("finished!");
		progress.setBar(1.0);
		progress.moveTask(task);
	}
	Prefs.blackBackground = backgroundPref;
}

/**
 * @return a ROI that contains the pixels in the image @param imp with non-zero intensity 
 * in the @param channel (1 <= channel <= number of channels in imp).
 * */
private Roi getRegionsAboveZeroAsROI(ImagePlus imp, int channel, double closeHolesRadius) {
	ImagePlus tempImp =  IJ.createHyperStack(imp.getTitle() + " temp", imp.getWidth(), imp.getHeight(), 1,
				imp.getNSlices(), imp.getNFrames(), 8);
	int index = 0;
	int indexTemp = 0;
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			for(int s = 0; s < imp.getNSlices(); s++){
				for(int f = 0; f < imp.getNFrames(); f++){
					index = imp.getStackIndex(channel, s+1, f+1)-1;
					indexTemp = tempImp.getStackIndex(1, s+1, f+1)-1;
					if(imp.getStack().getVoxel(x,y,index)>0) {
						tempImp.getStack().setVoxel(x, y, indexTemp, 255.0);
					}else {
						tempImp.getStack().setVoxel(x, y, indexTemp, 0.0);
					}
				}					
			}
		}
	}
	
//	tempImp.duplicate().show();
//	new WaitForUserDialog("binarized").show();
	
	IJ.run(tempImp, "Maximum...", "radius=" + dfDialog.format(closeHolesRadius));
	IJ.run(tempImp, "Minimum...", "radius=" + dfDialog.format(closeHolesRadius));
	
	IJ.run(tempImp, "Create Selection", "");
	
	Roi roi = tempImp.getRoi();
	if(roi.contains(0,0)) {
		if(tempImp.getStack().getVoxel(0,0,0) == 0) {
			IJ.run(tempImp, "Make Inverse", "");
			roi = tempImp.getRoi();
		}
	}else {
		if(tempImp.getStack().getVoxel(0,0,0) > 0) {
			IJ.run(tempImp, "Make Inverse", "");
			roi = tempImp.getRoi();
		}
	}
	
//	tempImp.duplicate().show();
//	new WaitForUserDialog("MinMaxedWithSelection " + "radius=" + dfDialog.format(closeHolesRadius)).show();	
	tempImp.changes = false;
	tempImp.close();
	
	return roi;	
}

/**
 * Sets all pixels in the @param channel (1 <= channel <= number of channels) of @param imp that are
 * not contained in the @param regionRoi to zero.
 * */
private void setRegionsOutsideRoiToZero(ImagePlus imp, int channel, Roi regionRoi) {
	int index = 0;
	double add = (1.0/(double)numberOfChannels*0.1/imp.getWidth());
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			for(int s = 0; s < imp.getNSlices(); s++){
				for(int f = 0; f < imp.getNFrames(); f++){
					index = imp.getStackIndex(channel, s+1, f+1)-1;
					if(!regionRoi.contains(x,y)) {
						imp.getStack().setVoxel(x, y, index, 0.0);
					}
				}					
			}
		}
		progress.addToBar(add);
	}
}

/**
 * Fill Holes and close gaps (using @param closeHolesRadius) in ImagePlus @param imp.
 * */
private ImagePlus getFillHolesAndRemoveNoise(ImagePlus imp, double closeGapsRadius, double closeHolesRadius, int segmC){
	ImagePlus tempImp = imp.duplicate();

	if(linkForROI [segmC]) {
		IJ.run(tempImp, "Maximum...", "radius=" + dfDialog.format(closeGapsRadius));
	}
	
	IJ.run(tempImp, "Fill Holes", "");
	
	if(linkForROI [segmC]) {
		IJ.run(tempImp, "Minimum...", "radius=" + dfDialog.format(closeGapsRadius));
		
	}
	
	IJ.run(tempImp, "Minimum...", "radius=" + dfDialog.format(closeHolesRadius));
	IJ.run(tempImp, "Maximum...", "radius=" + dfDialog.format(closeHolesRadius));
	
	return tempImp;
}

/**
 * Import settings from existing file
 */
private boolean importSettings() {
	java.awt.FileDialog fd = new java.awt.FileDialog((Frame) null, "Select files to add to list.");
	fd.setDirectory(System.getProperty("user.dir", "."));
	fd.setMultipleMode(false);
	fd.setMode(FileDialog.LOAD);
	fd.setVisible(true);
	File settingsFile = fd.getFiles()[0];
	
	if(settingsFile.equals(null)) {
		return false;
	}	
	
	String readVersion = "none";
	boolean uncalibratedVersion = false;
	
	IJ.log("READING PREFERENCES:");
	int nCs = 0;
	try {
		FileReader fr = new FileReader(settingsFile);
		BufferedReader br = new BufferedReader(fr);
		String line = "";							
		reading: while(true){
			try{
				line = br.readLine();	
				if(!line.equals("") && line.equals(null)){
					break reading;
				}
			}catch(Exception e){
				break reading;
			}	
			if(line.contains("Channel Nr:")){
				nCs++;
			}
			if(line.contains("'AdipoQ Preparator'")) {
				line = br.readLine();
				line = br.readLine();
				if(line.contains("Plug-in version:")) {
					readVersion = line.substring(line.lastIndexOf("	")+1);
					if(readVersion.equals("V0.0.1") || readVersion.equals("V0.0.2") || readVersion.equals("V0.0.3") || readVersion.equals("V0.0.4") || readVersion.equals("V0.0.5")
							|| readVersion.equals("V0.0.6") || readVersion.equals("V0.0.7") || readVersion.equals("V0.0.8") || readVersion.equals("V0.0.9") || readVersion.equals("V0.1.0")
							|| readVersion.equals("V0.1.1")) {
						uncalibratedVersion = true;
					}
					if(uncalibratedVersion) {
						IJ.log("Loaded file comes from a unc. version (" + readVersion + ")");
					}else {
						IJ.log("Loaded file comes from version " + readVersion);
					}					
				}else {
					IJ.error("File loading failed - Plugin version info missing in file.");
					return false;
				}
			}
		}					
		br.close();
		fr.close();
	}catch (IOException e) {
		IJ.error("Problem with loading preferences");
		e.printStackTrace();
		return false;
	}
	if(readVersion.equals("none")) {
		IJ.error("File loading failed - Plugin version info missing in file.");
		return false;
	}
	IJ.log("Number of channels to be segmented: " + nCs);
	
	//read individual channel settings
	String tempString;
	
	numberOfChannels = nCs;
	channelIDs = new int [nCs];	
	subtractBackground = new boolean [nCs];
	subtractBGRadius = new double [nCs];
	preBlur = new boolean [nCs];
	preBlurSigma = new double [nCs];
	subtractBluredImage = new boolean [nCs];
	subtractBlurSigma = new double [nCs];
	excludeZeroRegions = new boolean [nCs];
	includeDuplicateChannel = false;
	deleteOtherChannels = false;
	linkForROI = new boolean [nCs];
	removeParticles = new boolean [nCs];
	removeRadius = new double [nCs];
	despeckle = new boolean [nCs];
	fillHoles = new boolean [nCs];
	watershed  = new boolean [nCs];
	darkBackground = new boolean [nCs];

	//StarDist		
	selectedStarDistModel = new String [nCs];
	starDistNormalizeImage = new boolean [nCs];
	starDistPercentileLow = new double [nCs];
	starDistPercentileHigh = new double [nCs];
	starDistProbabilityScore = new double [nCs];
	starDistOverlapThreshold = new double [nCs];
	starDistModelPath = new String [nCs];
	starDistNTiles = new int [nCs];
		
	includeDuplicateChannel = false;
	deleteOtherChannels = false;
	
	for(int i = 0; i < nCs; i++) {
		channelIDs [i] = -1;
		subtractBackground [i] = false;
		subtractBGRadius [i] = -1.0;
		preBlur [i] = false;
		preBlurSigma [i] = -1.0;
		subtractBluredImage [i] = false;
		subtractBlurSigma [i] = -1.0;
		excludeZeroRegions [i] = false;
		linkForROI [i] = false;
		removeParticles [i] = false;
		removeRadius [i] = -1.0;
		despeckle [i] = false;
		fillHoles [i] = false;
		watershed [i] = false;
		darkBackground [i] = false;
				
		//StarDist
		selectedStarDistModel [i] = "None";
		starDistNormalizeImage [i] = false;
		starDistPercentileLow [i] = -1.0;
		starDistPercentileHigh [i] = -1.0;
		starDistProbabilityScore [i] = -1.0;
		starDistOverlapThreshold [i] = -1.0;
		starDistModelPath [i] = "None";
		starDistNTiles [i] = -1;
	}
		
	boolean readThrough = false;
	int segmC = -1;
	
	try {
		FileReader fr = new FileReader(settingsFile);
		BufferedReader br = new BufferedReader(fr);
		String line = "";							
		reading: while(true){
			try{
				line = br.readLine();	
				if(!line.equals("") && line.equals(null)){
					break reading;
				}
			}catch(Exception e){
				break reading;
			}					
			{
				if(line.contains("Channel Nr:")){
					segmC++;
					tempString = line.substring(line.lastIndexOf("	")+1);
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					channelIDs [segmC] = Integer.parseInt(tempString);	
					IJ.log("Channel nr = " + channelIDs [segmC]);
				}
				if(line.contains("Channel duplicated to include a copy of the channel that is not processed")){
					includeDuplicateChannel = true;
					IJ.log("Duplicate Channel");
				}
				if(line.contains("Deleted all channels except the channel(s) to be segmented")){
					deleteOtherChannels = true;
					IJ.log("Delete other channels");
				}
				
				if(line.contains("Subtract background before analysis - radius")){
					subtractBackground [segmC] = true;		
					tempString = line.substring(0, line.lastIndexOf("	"));
					tempString = tempString.substring(0, tempString.lastIndexOf("	"));
					tempString = tempString.substring(tempString.lastIndexOf("	")+1);
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					subtractBGRadius [segmC] = Double.parseDouble(tempString);					
					IJ.log("Subtract background before analysis - radius (calibrated unit) = " + subtractBGRadius [segmC]);
				}
				
				if(line.contains("Blur image before analysis - Gaussian sigma")){
					preBlur [segmC] = true;
					if(uncalibratedVersion) {
						tempString = line.substring(line.lastIndexOf("	")+1);
					}else {
						tempString = line.substring(0, line.lastIndexOf("	"));
						tempString = tempString.substring(0, tempString.lastIndexOf("	"));
						tempString = tempString.substring(tempString.lastIndexOf("	")+1);
					}
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					preBlurSigma [segmC] = Double.parseDouble(tempString);
					if(uncalibratedVersion) {
						IJ.log("Blur image before analysis - Gaussian sigma (px) = " + preBlurSigma [segmC]);							
					}else {
						IJ.log("Blur image before analysis - Gaussian sigma (calibrated unit) = " + preBlurSigma [segmC]);	
					}
				}
				
				if(line.contains("Subtract blurred copy of the image (for normalization) - Gaussian sigma")){
					subtractBluredImage [segmC] = true;
					if(uncalibratedVersion) {
						tempString = line.substring(line.lastIndexOf("	")+1);
					}else {
						tempString = line.substring(0, line.lastIndexOf("	"));
						tempString = tempString.substring(0, tempString.lastIndexOf("	"));
						tempString = tempString.substring(tempString.lastIndexOf("	")+1);						
					}
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					subtractBlurSigma [segmC] = Double.parseDouble(tempString);
					if(uncalibratedVersion) {
						IJ.log("Subtract blurred copy of the image (for normalization) - Gaussian sigma (px) = " + subtractBlurSigma [segmC]);						
					}else {
						IJ.log("Subtract blurred copy of the image (for normalization) - Gaussian sigma (calibrated unit) = " + subtractBlurSigma [segmC]);		
						
					}				
				}
				
				if(line.contains("Excluded zero intensity pixels in threshold calculation - radius of tolerated gaps")){
					excludeZeroRegions [segmC] = true;
					if(uncalibratedVersion) {
						tempString = line.substring(line.lastIndexOf("	")+1);
					}else {
						tempString = line.substring(0, line.lastIndexOf("	"));
						tempString = tempString.substring(0, tempString.lastIndexOf("	"));
						tempString = tempString.substring(tempString.lastIndexOf("	")+1);
					}					
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					closeGapsRadius [segmC] = Double.parseDouble(tempString);
					if(uncalibratedVersion) {
						IJ.log("Exclude zero intensity - close gaps rad (px) = " + closeGapsRadius [segmC]);	
					}else{
						IJ.log("Exclude zero intensity - close gaps rad (calibrated unit) = " + closeGapsRadius [segmC]);						
					}
				}
				
				if(line.contains("Segmentation method:")){
					if(line.contains("CUSTOM threshold")) {
						chosenAlgorithm [segmC] = algorithm [17];
						line = br.readLine();
						if(!line.contains("Custom threshold value:")){
							IJ.error("Could not find custom threshold value in settings - no preferences loaded!");
							return false;
						}
						tempString = line.substring(line.lastIndexOf("	")+1);
						if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
						customThr [segmC] = Double.parseDouble(tempString);
						IJ.log(chosenAlgorithm [segmC] + ": " + customThr [segmC]);
					}else if(line.contains("StarDist")) {
						chosenAlgorithm [segmC] = "StarDist";
						line = br.readLine();
						line = br.readLine();
						if(!line.contains("Model:")){
							IJ.error("Could not find complete StarDist settings in loaded file - no preferences loaded!");
							return false;
						}
						selectedStarDistModel [segmC] = line.substring(line.lastIndexOf("	")+1);
						IJ.log(chosenAlgorithm [segmC] + " - Model:" + selectedStarDistModel [segmC]);
						
						line = br.readLine();
						if(!line.contains("Normalize Image:")){
							IJ.error("Could not find complete StarDist settings in loaded file - no preferences loaded!");
							return false;
						}
						if(line.contains("Normalize Image:	TRUE") || line.contains("Normalize Image:	true") || line.contains("Normalize Image:	True")) {
							starDistNormalizeImage [segmC] = true;
						}else {
							starDistNormalizeImage [segmC] = false;
						}						
						IJ.log(chosenAlgorithm [segmC] + " - Normalize Image: " + starDistNormalizeImage [segmC]);
						
						line = br.readLine();
						if(!line.contains("Percentile low:")){
							IJ.error("Could not find complete StarDist settings in loaded file - no preferences loaded!");
							return false;
						}
						tempString = line.substring(line.lastIndexOf("	")+1);
						if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
						starDistPercentileLow [segmC] = Double.parseDouble(tempString);
						IJ.log(chosenAlgorithm [segmC] + " - Percentile low: " + starDistPercentileLow [segmC]);
						
						line = br.readLine();
						if(!line.contains("Percentile high:")){
							IJ.error("Could not find complete StarDist settings in loaded file - no preferences loaded!");
							return false;
						}
						tempString = line.substring(line.lastIndexOf("	")+1);
						if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
						starDistPercentileHigh [segmC] = Double.parseDouble(tempString);
						IJ.log(chosenAlgorithm [segmC] + " - Percentile high: " + starDistPercentileHigh [segmC]);
						
						line = br.readLine();
						if(!line.contains("Probability / Score threshold:")){
							IJ.error("Could not find complete StarDist settings in loaded file - no preferences loaded!");
							return false;
						}
						tempString = line.substring(line.lastIndexOf("	")+1);
						if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
						starDistProbabilityScore [segmC] = Double.parseDouble(tempString);
						IJ.log(chosenAlgorithm [segmC] + " - Probability / Score threshold: " + starDistProbabilityScore [segmC]);
						
						line = br.readLine();
						if(!line.contains("Overlap threshold:")){
							IJ.error("Could not find complete StarDist settings in loaded file - no preferences loaded!");
							return false;
						}
						tempString = line.substring(line.lastIndexOf("	")+1);
						if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
						starDistOverlapThreshold [segmC] = Double.parseDouble(tempString);
						IJ.log(chosenAlgorithm [segmC] + " - Overlap threshold: " + starDistOverlapThreshold [segmC]);
						
						line = br.readLine();
						if(!line.contains("File path (if load model from .zip file selected):")){
							IJ.error("Could not find complete StarDist settings in loaded file - no preferences loaded!");
							return false;
						}
						if(line.contains("Model (.zip) from File")) {
							starDistModelPath [segmC] = line.substring(line.lastIndexOf("	")+1);
						}else {
							starDistModelPath [segmC] = "";
						}						
						IJ.log(chosenAlgorithm [segmC] + " - Model Path (if custom model): " + starDistModelPath [segmC]);
						
						line = br.readLine();
						if(!line.contains("Nr of tiles:")){
							IJ.error("Could not find complete StarDist settings in loaded file - no preferences loaded!");
							return false;
						}
						tempString = line.substring(line.lastIndexOf("	")+1);
						starDistNTiles [segmC] = Integer.parseInt(tempString);
						IJ.log(chosenAlgorithm [segmC] + " - N Tiles: " + starDistNTiles [segmC]);
					}else if(line.contains("applying intensity threshold based ")) {
						for(int a = 0; a < algorithm.length; a++) {
							if(line.contains(algorithm[a])) {
								chosenAlgorithm [segmC] = algorithm [a];
								break;
							}
						}
						IJ.log("Segment with " + chosenAlgorithm [segmC]);	
					}
				}
				
				if(line.contains("Background definition: detect bright objects on dark background")) {
					IJ.log("Detect bright objects on dark background");
					darkBackground [segmC] = true;
				}else if(line.contains("Background definition: detect dark objects on bright background")) {
					IJ.log("Detect dark objects on bright background");
					darkBackground [segmC] = false;
				}
				
				
				if(line.contains("Radius of particles to be removed as noise while detecting adipose tissue regions") 
						|| line.contains("Auto detect the region of interest - radius of exluded regions")){
					removeParticles [segmC] = true;
					if(uncalibratedVersion) {
						tempString = line.substring(line.lastIndexOf("	")+1);
					}else {
						tempString = line.substring(0, line.lastIndexOf("	"));
						tempString = tempString.substring(0, tempString.lastIndexOf("	"));
						tempString = tempString.substring(tempString.lastIndexOf("	")+1);
					}					
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					removeRadius [segmC] = Double.parseDouble(tempString);
					if(uncalibratedVersion) {
						IJ.log("Auto detect - exclude regions (px) = " + removeRadius [segmC]);							
					}else {
						IJ.log("Auto detect - exclude regions (calibrated unit) = " + removeRadius [segmC]);							
					}					
				}
				
				if(line.contains("No auto-detection of region of interest applied.")){
					removeParticles [segmC] = false;
					removeRadius [segmC] = 0.0;
					IJ.log("Do not auto-detect regions and do not exclude regions");	
				}
				
				if(line.contains("Close gaps for detecting tissue regions")){
					if(uncalibratedVersion) {
						tempString = line.substring(line.lastIndexOf("	")+1);
					}else {
						tempString = line.substring(0, line.lastIndexOf("	"));
						tempString = tempString.substring(0, tempString.lastIndexOf("	"));
						tempString = tempString.substring(tempString.lastIndexOf("	")+1);
					}
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					linkForROI [segmC] = true;
					linkGapsForRemoveRadius [segmC] = Double.parseDouble(tempString);					
					if(uncalibratedVersion) {
						IJ.log("Close gaps for detecting tissue regions rad (calibrated unit) = " + linkGapsForRemoveRadius [segmC]);						
					}else {
						IJ.log("Close gaps for detecting tissue regions rad (calibrated unit) = " + linkGapsForRemoveRadius [segmC]);
					}
				}
				
				if(line.contains("Despeckle mask")){
					despeckle [segmC] = true;
					IJ.log("Despeckle mask");
				}
				if(line.contains("Fill holes in mask") || line.contains("Fill holes in segmented image")) {
					fillHoles [segmC] = true;
					IJ.log("Fill holes in mask");
				}
				
				if(line.contains("Apply watershed algorithm")) {
					watershed [segmC] = true;
					IJ.log("Apply watershed algorithm");
				}
				
				if(line.contains("'AdipoQ Preparator'")) {
					readThrough = true;
					line = br.readLine();
					line = br.readLine();
					if(line.contains("0.0.7")){
						deleteOtherChannels = true;
						IJ.log("Delete other channels");
						for(int db = 0; db < darkBackground.length; db++) {
							darkBackground [db] = false;						
						}
						IJ.log("Light background");
					}
					
					//If older versions than 0.1.2 were loaded > show a notice that values may need to be respecified!
					if(line.contains("0.0.1") || line.contains("0.0.2") || line.contains("0.0.3") || line.contains("0.0.4") || line.contains("0.0.5")
							|| line.contains("0.0.6") || line.contains("0.0.7") || line.contains("0.0.8") || line.contains("0.0.9") || line.contains("0.1.0")
							|| line.contains("0.1.1")) {
						{
							GenericDialog gd = new GenericDialog(PLUGINNAME + " on " + System.getProperty("os.name") + " - loading parameters");	
							//show Dialog-----------------------------------------------------------------
							//.setInsets(top, left, bottom)
							gd.setInsets(0,0,0);		gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2019-2022 JN Hansen", SuperHeadingFont);
							gd.setInsets(0,0,0);		gd.addMessage("You are loading settings from a previous version, in which parameter settings were given", HeadingFont);
							gd.setInsets(0,0,0);		gd.addMessage("in pixel and not in calibrated units (e.g., micron). Thus, please make sure to re-specify", HeadingFont);
							gd.setInsets(0,0,0);		gd.addMessage("the parameters in the following dialogs.", HeadingFont);
							gd.showDialog();
							//show Dialog-----------------------------------------------------------------
							
							if (gd.wasCanceled()) return false;	
						}
						
						for(int i = 0; i < nCs; i++){
							GenericDialog gd = new GenericDialog(PLUGINNAME + " on " + System.getProperty("os.name") + " - adapting parameters");	
							//show Dialog-----------------------------------------------------------------
							//.setInsets(top, left, bottom)
							gd.setInsets(0,0,0);			gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2019-2022 JN Hansen", SuperHeadingFont);
							gd.setInsets(0,0,0);			gd.addMessage("Settings for channel #" + (i+1) + " - Channel Nr: " + channelIDs [i], HeadingFont);			
							
							if(preBlur [i]) {
								gd.setInsets(0,0,0);	
								gd.addNumericField("Blur image before analysis - Gaussian sigma (calibrated unit, e.g., µm):", preBlurSigma [i], 3);
							}
							
							if(subtractBluredImage [i]) {
								gd.setInsets(0,0,0);		
								gd.addNumericField("Subtract blurred copy of image (to normalize) - Gauss sigma (calibrated unit, e.g., µm)", subtractBlurSigma [i], 3);
							}
							
							if(excludeZeroRegions [i]) {
								gd.setInsets(0,0,0);
								gd.addNumericField("Exclude zero-pixels in threshold calc. - tolerated gap radius (calibrated unit, e.g., µm)", closeGapsRadius [i], 3);
								
							}
							
							if(removeParticles [i]) {
								gd.setInsets(0,0,0);
								gd.addNumericField("Detect tissue regions and remove smaller regions | minimum radius (calibrated unit, e.g., µm)", removeRadius [i], 2);	
							}
							
							if(linkForROI [i]) {
								gd.setInsets(0,0,0);
								gd.addNumericField("During detecting tissue regions, close gaps | distance (calibrated unit, e.g., µm)", linkGapsForRemoveRadius [i], 2);								
							}
							gd.showDialog();
							//show Dialog-----------------------------------------------------------------

							//read and process variables--------------------------------------------------	
							{
								if(preBlur [i]) {
									preBlurSigma [i] = (double) gd.getNextNumber();
								}								
								if(subtractBluredImage [i]) {
									subtractBlurSigma [i] = (double) gd.getNextNumber();
								}								
								if(excludeZeroRegions [i]) {
									closeGapsRadius [i] = (double) gd.getNextNumber();	
									
								}								
								if(removeParticles [i]) {
									removeRadius [i] = gd.getNextNumber();	
								}								
								if(linkForROI [i]) {
									linkGapsForRemoveRadius [i] = gd.getNextNumber();								
								}
							}
							//read and process variables--------------------------------------------------
							
							if (gd.wasCanceled()) return false;	
						}
					}
					
					if(line.contains("0.0.1") || line.contains("0.0.2") || line.contains("0.0.3") || line.contains("0.0.4") || line.contains("0.0.5")
							|| line.contains("0.0.6") || line.contains("0.0.7") || line.contains("0.0.8") || line.contains("0.0.9") || line.contains("0.1.0")
							|| line.contains("0.1.1") || line.contains("0.1.2")  || line.contains("0.1.3")) {
						for(int i = 0; i < nCs; i++) {							
							subtractBackground [i] = false;
							subtractBGRadius [i] = 0.0;
							IJ.log("Do not subtract background as loaded version did not contain that function");
						}
					}
				}
				
				if(line.contains("'LipiroidQ'")) {
					readThrough = true;
					line = br.readLine();
					line = br.readLine();
					if(line.contains("0.0.1")){
						includeDuplicateChannel = true;
						IJ.log("Duplicate Channel");
						deleteOtherChannels = false;
						IJ.log("Do not delete other channels");
						for(int pos = 0; pos < darkBackground.length; pos++) {
							removeParticles [pos] = false;
							removeRadius [pos] = 0.0;
							darkBackground [pos] = true;						
						}
						IJ.log("Do not auto-detect regions and do not exclude regions");
						IJ.log("Dark background");
					}					
					for(int i = 0; i < nCs; i++) {						
						subtractBackground [i] = false;
						subtractBGRadius [i] = 0.0;
					}
				}
			}			
		}					
		br.close();
		fr.close();
	}catch (IOException e) {
		IJ.error("Problem with loading preferences");
		e.printStackTrace();
		return false;
	}
	for(int pos = 0; pos < removeRadius.length; pos++) {
		if(removeRadius [pos] == -1.0 || channelIDs [pos] == -1 || !readThrough) {
			IJ.error("Problem with loading preferences - parameters missing or inappropriate text file.");
			return false;
		}
		if(chosenAlgorithm [segmC] == "StarDist") {
			if(selectedStarDistModel [pos] == "None" || 
				starDistPercentileLow [pos] == -1.0 || starDistPercentileHigh [pos] == -1.0 || 
				starDistProbabilityScore [pos] == -1.0 || starDistOverlapThreshold [pos] == -1.0 || 
				starDistModelPath [pos] == "None" || starDistNTiles [pos] == -1) {
				IJ.error("Problem with loading StarDist preferences - parameters missing or inappropriate text file.");
				return false;
			}
		}
	}	
	return true;
}

/**
 * Show dialogs to enter settings
 * @param defaultType: 0 = histology, 1 = cell culture
 * */
private boolean enterSettings(int defaultType) {
	/*
	 * Find out general settings 
	 * */
	if(defaultType == 0) {
		/**Histology settings*/
		includeDuplicateChannel = true;
		deleteOtherChannels = true;
	}else if (defaultType == 1){
		/**DAPI settings*/
		includeDuplicateChannel = true;
		deleteOtherChannels = false;	
	}else {
		IJ.error("Incorrect default Type - plugin cancelled!");
	}
	{
		GenericDialog gd = new GenericDialog(PLUGINNAME + " on " + System.getProperty("os.name") + " - set parameters");	
		//show Dialog-----------------------------------------------------------------
		//.setInsets(top, left, bottom)
		gd.setInsets(0,0,0);		gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2019-2022 JN Hansen", SuperHeadingFont);
		gd.setInsets(0,0,0);		gd.addMessage("General settings", HeadingFont);
		gd.setInsets(0,10,0);		gd.addNumericField("Number of different channels to be segmented", numberOfChannels, 0);
		gd.setInsets(0,10,0);		gd.addCheckbox("Include raw copy of the channel in output image", includeDuplicateChannel);
		gd.setInsets(0,10,0);		gd.addCheckbox("Delete all non-segmented channels", deleteOtherChannels);
		gd.showDialog();
		//show Dialog-----------------------------------------------------------------

		//read and process variables--------------------------------------------------	
		{
			numberOfChannels = (int) gd.getNextNumber();
			includeDuplicateChannel = gd.getNextBoolean();
			deleteOtherChannels = gd.getNextBoolean();
		}
		//read and process variables--------------------------------------------------
		
		if (gd.wasCanceled()) return false;	
	}
		
	/*
		Depending on set variant show default settings for one or the other
	*/
	channelIDs = new int [numberOfChannels];
	subtractBackground = new boolean [numberOfChannels];
	subtractBGRadius = new double [numberOfChannels];
	preBlur = new boolean [numberOfChannels];
	preBlurSigma = new double [numberOfChannels];
	subtractBluredImage = new boolean [numberOfChannels];
	subtractBlurSigma = new double [numberOfChannels];		
	excludeZeroRegions = new boolean [numberOfChannels];
	closeGapsRadius = new double [numberOfChannels];
	removeParticles = new boolean [numberOfChannels];
	removeRadius = new double [numberOfChannels];
	linkGapsForRemoveRadius = new double [numberOfChannels];
	chosenAlgorithm = new String [numberOfChannels];
	customThr = new double [numberOfChannels];
	darkBackground = new boolean [numberOfChannels];
	selectedBgVariant = new String [numberOfChannels];
	despeckle = new boolean [numberOfChannels];
	linkForROI = new boolean [numberOfChannels];
	fillHoles = new boolean [numberOfChannels];
	watershed = new boolean [numberOfChannels];
	
	//StarDist		
	selectedStarDistModel = new String [numberOfChannels];
	starDistNormalizeImage = new boolean [numberOfChannels];
	starDistPercentileLow = new double [numberOfChannels];
	starDistPercentileHigh = new double [numberOfChannels];
	starDistProbabilityScore = new double [numberOfChannels];
	starDistOverlapThreshold = new double [numberOfChannels];
	starDistModelPath = new String [numberOfChannels];
	starDistNTiles = new int [numberOfChannels];
	
	for(int i = 0; i < numberOfChannels; i++) {
		channelIDs [i] = (i+1);
		
		if(defaultType == 0) {
			/**Histology settings*/
			subtractBackground [i] = false;
			subtractBGRadius [i] = 30.0;
			preBlur [i] = false;
			preBlurSigma [i] = 0.0;
			subtractBluredImage [i] = false;
			subtractBlurSigma [i] = 0.0;
			excludeZeroRegions [i] = true;
			closeGapsRadius [i] = 2.2;
			removeParticles [i] = true;
			removeRadius [i] = 8.8;
			despeckle [i] = true;
			linkForROI [i] = false;
			linkGapsForRemoveRadius [i] = 2.2;
			chosenAlgorithm [i] = "Triangle";
			customThr [i] = 0.0;
			darkBackground [i] = false;
			fillHoles [i] = true;
			watershed [i] = false;
			
			//StarDist
			selectedStarDistModel [i] = STARDISTMODELS[3];
			starDistNormalizeImage [i] = true;
			starDistPercentileLow [i] = 1.0;
			starDistPercentileHigh [i] = 99.8;
			starDistProbabilityScore [i] = 0.48;
			starDistOverlapThreshold [i] = 0.3;
			starDistModelPath [i] = "";
			starDistNTiles [i] = 1;
		}else if (defaultType == 1){
			/**DAPI settings*/
			subtractBackground [i] = false;
			subtractBGRadius [i] = 10.0;
			preBlur [i] = true;
			preBlurSigma [i] = 1.8;
			subtractBluredImage [i] = true;
			subtractBlurSigma [i] = 2.7;
			excludeZeroRegions [i] = false;
			closeGapsRadius [i] = 4.5;
			removeParticles [i] = false;
			removeRadius [i] = 18.0;
			despeckle [i] = false;
			linkForROI [i] = false;
			linkGapsForRemoveRadius [i] = 4.5;
			chosenAlgorithm [i] = "Otsu";
			customThr [i] = 0.0;
			darkBackground [i] = true;
			fillHoles [i] = true;
			watershed [i] = true;	
			
			//StarDist
			selectedStarDistModel [i] = STARDISTMODELS[0];
			starDistNormalizeImage [i] = true;
			starDistPercentileLow [i] = 1.0;
			starDistPercentileHigh [i] = 99.8;
			starDistProbabilityScore [i] = 0.48;
			starDistOverlapThreshold [i] = 0.3;
			starDistModelPath [i] = "";
			starDistNTiles [i] = 1;
		}else {
			IJ.error("Incorrect default Type - plugin cancelled!");
		}
		
		if(darkBackground [i]) {
			selectedBgVariant [i] = bgMethod[0];
		}else {
			selectedBgVariant [i] = bgMethod[1];
		}
	}
	

	for(int i = 0; i < numberOfChannels; i++) {
		while(true) {
			/*
			 * Show dialog
			 * */
			GenericDialog gd = new GenericDialog(PLUGINNAME + " on " + System.getProperty("os.name") + " - set parameters");	
			//show Dialog-----------------------------------------------------------------
			//.setInsets(top, left, bottom)
			gd.setInsets(0,0,0);			gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2019-2022 JN Hansen", SuperHeadingFont);
			gd.setInsets(0,0,0);			gd.addMessage("Settings for channel #" + (i+1), HeadingFont);			
			
			gd.setInsets(10,5,0);		gd.addNumericField("Channel Nr (>= 1 & <= nr of channels) to be segmented", channelIDs [i], 0);
			gd.setInsets(0,0,0);			gd.addCheckbox("Subtract background before analysis - radius (calibrated unit, e.g., µm)", subtractBackground [i]);
			gd.setInsets(-23,330,0);		gd.addNumericField("", subtractBGRadius [i], 3);
			
			gd.setInsets(0,0,0);			gd.addCheckbox("Blur image before analysis - Gaussian sigma (calibrated unit, e.g., µm)", preBlur [i]);
			gd.setInsets(-23,330,0);		gd.addNumericField("", preBlurSigma [i], 3);
			
			gd.setInsets(0,0,0);			gd.addCheckbox("Subtract blurred copy of image (to normalize) - Gauss sigma (calibrated unit, e.g., µm)", subtractBluredImage [i]);
			gd.setInsets(-23,330,0);		gd.addNumericField("", subtractBlurSigma [i], 3);
			
			
			gd.setInsets(0,0,0);			gd.addChoice("Segmentation method", algorithm, chosenAlgorithm [i]);
			gd.setInsets(0,5,0);			gd.addNumericField("If 'CUSTOM threshold' selected, specify threshold here", customThr [i], 2);
			gd.setInsets(0,0,0);			gd.addChoice("Background definition", bgMethod, selectedBgVariant [i]);
			gd.setInsets(0,0,0);			gd.addCheckbox("Exclude zero-pixels in threshold calc. - tolerated gap radius (calibrated unit, e.g., µm)", excludeZeroRegions [i]);
			gd.setInsets(-23,330,0);		gd.addNumericField("", closeGapsRadius [i], 3);
			
			gd.setInsets(0,0,0);			gd.addCheckbox("Despeckle segmented image", despeckle [i]);

			gd.setInsets(0,0,0);			gd.addCheckbox("Detect tissue regions and remove smaller regions | minimum radius (calibrated unit, e.g., µm)", removeParticles [i]);
			gd.setInsets(-23,330,0);		gd.addNumericField("", removeRadius [i], 2);
			
			gd.setInsets(0,0,0);			gd.addCheckbox("During detecting tissue regions, close gaps | distance (calibrated unit, e.g., µm)", linkForROI [i]);
			gd.setInsets(-23,330,0);		gd.addNumericField("", linkGapsForRemoveRadius [i], 2);
			
			gd.setInsets(0,0,0);			gd.addCheckbox("Fill holes in segmented image", fillHoles [i]);
			
			gd.setInsets(0,0,0);			gd.addCheckbox("Apply Watershed algorithm", watershed [i]);
			
			gd.setInsets(10,0,0);		gd.addMessage("NOTE: When using StarDist as segmentation method, we recommend to disable any post-processing of the segmentation (despecling, ..., watershed).", InstructionsFont);		
			gd.setInsets(0,0,0);		gd.addMessage("Pre-processing may disturb segmentation, as well. Thus, if you try StarDist, we recommend to first try it while disabling all checkboxes in this dialog.", InstructionsFont);
			
			gd.showDialog();
			//show Dialog-----------------------------------------------------------------

			//read and process variables--------------------------------------------------	
			{
				channelIDs [i] = (int) gd.getNextNumber();
				subtractBackground [i] = gd.getNextBoolean();
				subtractBGRadius [i] = (double) gd.getNextNumber();
				preBlur [i] = gd.getNextBoolean();
				preBlurSigma [i] = (double) gd.getNextNumber();
				subtractBluredImage [i] = gd.getNextBoolean();
				subtractBlurSigma [i] = (double) gd.getNextNumber();			
				
				chosenAlgorithm [i] = gd.getNextChoice();
				customThr [i] = gd.getNextNumber();
				selectedBgVariant [i] = gd.getNextChoice();
				if(selectedBgVariant [i] == bgMethod[0]) {
					darkBackground [i] = true;
				}else{
					darkBackground [i] = false;
				}
				
				excludeZeroRegions [i] = gd.getNextBoolean();
				closeGapsRadius [i] = (double) gd.getNextNumber();				
				despeckle [i] = gd.getNextBoolean();
				removeParticles [i] = gd.getNextBoolean();
				removeRadius [i] = gd.getNextNumber();
				linkForROI [i] = gd.getNextBoolean();
				linkGapsForRemoveRadius [i] = gd.getNextNumber();
				fillHoles [i] = gd.getNextBoolean();			
				watershed [i] = gd.getNextBoolean();
			}
			//read and process variables--------------------------------------------------
			
			if (gd.wasCanceled()) return false;	
			
			if(chosenAlgorithm [i].equals("StarDist")) {
				configureStarDist(i,channelIDs[i]);
			}
			
			if (linkForROI [i] && !removeParticles [i]) {
				new WaitForUserDialog("'Close gaps ...' option is only available when 'Detect tissue regions ...' option selected. Change of settings required. Dialog will be shown again!").show();
			}else {
				break;
			}
		}	
	}	
	return true;
}

/**
 * Show dialogs to enter settings
 * @param id: channel id in arrays (starting with 0)
 * @param channel: channel id in image
 * */
private boolean configureStarDist(int id, int channelID) {
	{
		GenericDialog gd = new GenericDialog(PLUGINNAME + " on " + System.getProperty("os.name") + " - StarDist parameters");	
		//show Dialog-----------------------------------------------------------------
		//.setInsets(top, left, bottom)
		gd.setInsets(0,0,0);		gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2019-2022 JN Hansen", SuperHeadingFont);
		gd.addHelp("https://imagej.net/plugins/stardist");
		
		gd.setInsets(20,0,0);	gd.addMessage("Information on StarDist - more at https://imagej.net/plugins/stardist", HeadingFont);
		gd.setInsets(0,0,0);		gd.addMessage("StarDist allows object detection with star-convex shapes using a neural-network-based prediction.", InstructionsFont);
		gd.setInsets(0,0,0);		gd.addMessage("To use StarDist in the AdipoQ workflow, you need to install StarDist as described at the webpage referenced above.", InstructionsFont);
		gd.setInsets(0,0,0);		gd.addMessage("To understand the StarDist workflow for segmentation and the parameters to be set, visit this webpage", InstructionsFont);
		gd.setInsets(0,0,0);		gd.addMessage("or click the Help button (which redirects to that link).", InstructionsFont);		

		gd.setInsets(10,0,0);		gd.addMessage("When using StarDist, also cite the StarDist paper referenced at https://imagej.net/plugins/stardist", InstructionsFont);

		gd.setInsets(10,0,0);	gd.addMessage("Note that while using StarDist, AdipoQ Preparator may open and close image windows. Make sure to not interfere with the", InstructionsFont);
		gd.setInsets(0,0,0);		gd.addMessage("windows that pop up to avoid a crash of the plugin.", InstructionsFont);
		
		gd.setInsets(20,0,0);	gd.addMessage("Set StarDist parameters for segmenting channel " + channelID + "", HeadingFont);
		gd.setInsets(0,0,0);		gd.addMessage("Neural Network Prediction", SubHeadingFont);
		gd.setInsets(0,0,0);		gd.addChoice("Model", STARDISTMODELS,  selectedStarDistModel [id]);
		gd.setInsets(0,0,0);		gd.addCheckbox("Normalize Image", starDistNormalizeImage [id]);
		gd.setInsets(0,0,0);		gd.addNumericField("Percentile low (>=0,<=100)",starDistPercentileLow[id], 1);
		gd.setInsets(0,0,0);		gd.addNumericField("Percentile high (>=0,<=100)", starDistPercentileHigh[id], 1);
		gd.setInsets(20,0,0);	gd.addMessage("NMS Postprocessing", SubHeadingFont);
		gd.setInsets(0,0,0);		gd.addSlider("Probability/Score Threshold", 0.00, 1.00, starDistProbabilityScore[id]);
		gd.setInsets(0,0,0);		gd.addSlider("Overlap Threshold", 0.00, 1.00, starDistOverlapThreshold[id]);

		gd.setInsets(20,0,0);	gd.addMessage("Advanced", SubHeadingFont);
		gd.setInsets(0,0,0);		gd.addMessage("If you selected to load a custom model from a .zip file, specify the path to the file here:", InstructionsFont);
		gd.setInsets(0,0,0);		gd.addStringField("File path to model (.zip)", "");
		gd.setInsets(0,0,0);		gd.addNumericField("Number of tiles (evtl. increase for large images)", starDistNTiles[id], 0);
		
		gd.showDialog();
		//show Dialog-----------------------------------------------------------------

		//read and process variables--------------------------------------------------	
		{
			selectedStarDistModel [id] = gd.getNextChoice();
			starDistNormalizeImage [id] =  gd.getNextBoolean();
			starDistPercentileLow [id] = gd.getNextNumber();
			starDistPercentileHigh [id] = gd.getNextNumber();
			starDistProbabilityScore [id] = gd.getNextNumber();
			starDistOverlapThreshold [id] = gd.getNextNumber();
			starDistModelPath [id] = gd.getNextString();	
			starDistNTiles [id] = (int) gd.getNextNumber();
		}
		//read and process variables--------------------------------------------------
		
		if (gd.wasCanceled()) return false;	
	}		
	return true;
}

private void addFooter(TextPanel tp, Date currentDate){
	tp.append("");
	tp.append("Datafile was generated on " + FullDateFormatter2.format(currentDate) + " by '"
			+PLUGINNAME+"', an ImageJ plug-in by Jan Niklas Hansen (jan.hansen@uni-bonn.de, https://github.com/hansenjn/AdipoQ_Preparator).");
	tp.append("The plug-in '"+PLUGINNAME+"' is distributed in the hope that it will be useful,"
			+ " but WITHOUT ANY WARRANTY; without even the implied warranty of"
			+ " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
	tp.append("Plug-in version:	V"+PLUGINVERSION);	
}

/**
 * @param channel: 1 <= channel <= # channels
 * */
private static ImagePlus copyChannel(ImagePlus imp, int channel, boolean adjustDisplayRangeTo16bit, boolean copyOverlay){
	ImagePlus impNew = IJ.createHyperStack("channel image", imp.getWidth(), imp.getHeight(), 1, imp.getNSlices(), imp.getNFrames(), imp.getBitDepth());
	int index = 0, indexNew = 0;
	
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			for(int s = 0; s < imp.getNSlices(); s++){
				for(int f = 0; f < imp.getNFrames(); f++){
					index = imp.getStackIndex(channel, s+1, f+1)-1;
					indexNew = impNew.getStackIndex(1, s+1, f+1)-1;
					impNew.getStack().setVoxel(x, y, indexNew, imp.getStack().getVoxel(x, y, index));
				}					
			}
		}
	}
	if(adjustDisplayRangeTo16bit)	impNew.setDisplayRange(0, 4095);
	if(copyOverlay)	impNew.setOverlay(imp.getOverlay().duplicate());
	
	imp.setC(channel);
   	impNew.setLut(imp.getChannelProcessor().getLut());
	
	impNew.setCalibration(imp.getCalibration());
	return impNew;
}

/**
 * @return a threshold for the slice image <s> in the ImagePlus <parImp> for the image <imp>
 * range: 1 <= z <= stacksize
 * */
private double[] getSingleSliceImageThresholds (ImagePlus imp, int s, String chosenAlg, boolean darkBackground){
	//calculate thresholds	
	imp.setSlice(s);
	imp.getProcessor().setRoi(imp.getRoi());
	imp.getProcessor().setSliceNumber(s);
	imp.getProcessor().setAutoThreshold(Method.valueOf(Method.class, chosenAlg), darkBackground);
	//Before: IJ.setAutoThreshold(imp, (chosenAlg + " dark"));
	imp.getProcessor().setSliceNumber(s);
	return new double [] {imp.getProcessor().getMinThreshold(),imp.getProcessor().getMaxThreshold()};
}

private void segmentImage(ImagePlus imp, double threshold, int z, ImagePlus impTemp, int zTemp, 
		boolean keepIntensities, boolean darkbackground){
	double maxValue = Math.pow(2.0, imp.getBitDepth()) - 1;		
	if(darkbackground) {
		for(int x = 0; x < imp.getWidth(); x++){
			for(int y = 0; y < imp.getHeight(); y++){
				double pxintensity = impTemp.getStack().getVoxel(x,y,zTemp);
				if(pxintensity < threshold){
					imp.getStack().setVoxel( x, y, z, 0.0);
				}else if(!keepIntensities){
					imp.getStack().setVoxel( x, y, z, maxValue);
				}
			}
		}		
	}else {
		for(int x = 0; x < imp.getWidth(); x++){
			for(int y = 0; y < imp.getHeight(); y++){
				double pxintensity = impTemp.getStack().getVoxel(x,y,zTemp);
				if(pxintensity > threshold){
					imp.getStack().setVoxel( x, y, z, 0.0);
				}else if(!keepIntensities){
					imp.getStack().setVoxel( x, y, z, maxValue);
				}
			}
		}	
	}
	
}

private void addSettingsBlockToPanel(TextPanel tp, Date startDate, String name, boolean multiSeries, int series, ImagePlus imp) {
	tp.append("Starting date:	" + FullDateFormatter.format(startDate));
	if(multiSeries) {
		tp.append("Image name:	" + name + "	series:	" + (series+1));
	}else{
		tp.append("Image name:	" + name);		
	}
	tp.append("Image metadata:");
	tp.append("	Width [voxel]:	" + imp.getWidth());
	tp.append("	Height [voxel]:	" + imp.getHeight());
	tp.append("	Number of channels:	" + imp.getNChannels());
	tp.append("	Number of Slices:	" + imp.getNSlices());
	tp.append("	Number of Frames:	" + imp.getNFrames());
	tp.append("	Voxel width:	" + imp.getCalibration().pixelWidth);
	tp.append("	Voxel height:	" + imp.getCalibration().pixelHeight);
	tp.append("	Voxel depth:	" + imp.getCalibration().pixelDepth);
	tp.append("	Frame interval:	" + imp.getCalibration().frameInterval);
	tp.append("	Spatial unit:	" + imp.getCalibration().getUnit());
	tp.append("	Temporal unit:	" + imp.getCalibration().getTimeUnit());
	
	tp.append("General settings:");
	{
		if(includeDuplicateChannel){
			tp.append("	Channel duplicated to include a copy of the channel that is not processed.");
		}else{tp.append("");}
		
		if(deleteOtherChannels){
			tp.append("	Deleted all channels except the channel(s) to be segmented.");
		}else{tp.append("");}
	}
	
	for(int i = 0; i < channelIDs.length; i++) {
		tp.append("Settings for channel #" + (i+1) + ":");
		{
			tp.append("	Channel Nr:	" + df0.format(channelIDs [i]));			
			
			if(subtractBackground [i]){
				tp.append("	Subtract background before analysis - radius (" + imp.getCalibration().getUnit() + "):	" + df6.format(subtractBGRadius [i])
				+ "	-> in pixel:	" + df6.format(subtractBGRadius[i] / (0.5 * imp.getCalibration().pixelWidth + 0.5 * imp.getCalibration().pixelHeight)) + "");
			}else{tp.append("");}
			
			if(preBlur [i]){
				tp.append("	Blur image before analysis - Gaussian sigma (" + imp.getCalibration().getUnit() + "):	" + df6.format(preBlurSigma [i])
				+ "	-> in pixel:	" + df6.format(preBlurSigma[i] / (0.5 * imp.getCalibration().pixelWidth + 0.5 * imp.getCalibration().pixelHeight)) + "");
			}else{tp.append("");}
			
			if(subtractBluredImage [i]){
				tp.append("	Subtract blurred copy of the image (for normalization) - Gaussian sigma (" + imp.getCalibration().getUnit() + "):	" + df6.format(subtractBlurSigma [i])
				+ "	-> in pixel:	" + df6.format(subtractBlurSigma [i] / (0.5 * imp.getCalibration().pixelWidth + 0.5 * imp.getCalibration().pixelHeight)) + "");
			}else{tp.append("");}
					
			if (chosenAlgorithm [i] == "CUSTOM threshold"){
				tp.append("	Segmentation method:	" + "CUSTOM threshold");
				tp.append("		Custom threshold value:	" + df6.format(customThr [i]));
			}else if(chosenAlgorithm [i] == "StarDist"){
				tp.append("	Segmentation method:	" + "StarDist");
				tp.append("		Cite StarDist when showing the results from this analysis - more information at https://imagej.net/plugins/stardist");
				tp.append("		Model:	" + selectedStarDistModel [i]);
				tp.append("		Normalize Image:	" + (starDistNormalizeImage [i]));
				tp.append("		Percentile low:	" + df6.format(starDistPercentileLow [i]));
				tp.append("		Percentile high:	" + df6.format(starDistPercentileHigh [i]));
				tp.append("		Probability / Score threshold:	" + df6.format(starDistProbabilityScore [i]));
				tp.append("		Overlap threshold:	" + df6.format(starDistOverlapThreshold [i]));
				tp.append("		File path (if load model from .zip file selected):	" + starDistModelPath [i]);
				tp.append("		Nr of tiles:	" + df0.format(starDistNTiles [i]));
			}else {
				tp.append("	Segmentation method:	applying intensity threshold based on the " + chosenAlgorithm [i] + " threshold algorithm.");
				tp.append("");				
			}	
			
			if(darkBackground [i]){
				tp.append("	Background definition: detect bright objects on dark background.");
			}else{
				tp.append("	Background definition: detect dark objects on bright background.");
			}

			if(excludeZeroRegions [i]){
				tp.append("	Excluded zero intensity pixels in threshold calculation - radius of tolerated gaps (" + imp.getCalibration().getUnit() + "):	" + df6.format(closeGapsRadius [i])
					+ "	-> in pixel:	" + df6.format(closeGapsRadius [i] / (0.5 * imp.getCalibration().pixelWidth + 0.5 * imp.getCalibration().pixelHeight)) + "");
			}else{tp.append("");}		
			
			
			if(despeckle [i]){
				tp.append("	Despeckle mask");
			}else{tp.append("");}
			
			if(removeParticles [i]) {
				tp.append("	Auto detect the region of interest - radius of exluded regions (" + imp.getCalibration().getUnit() + "):	" + df6.format(removeRadius [i])
					+ "	-> in pixel:	" + df6.format(removeRadius [i] / (0.5 * imp.getCalibration().pixelWidth + 0.5 * imp.getCalibration().pixelHeight)) + "");
				if(linkForROI [i]){
					tp.append("	Close gaps for detecting tissue regions (" + imp.getCalibration().getUnit() + "):	" + df6.format(linkGapsForRemoveRadius [i])
						+ "	-> in pixel:	" + df6.format(linkGapsForRemoveRadius [i] / (0.5 * imp.getCalibration().pixelWidth + 0.5 * imp.getCalibration().pixelHeight)) + "");
				}else{tp.append("");}		
			}else{
				tp.append("	No auto-detection of region of interest applied.");
				tp.append("");
			}	
					
			if(fillHoles [i]){
				tp.append("	Fill holes in mask");
			}else{tp.append("");}
			
			if(watershed [i]){
				tp.append("	Apply watershed algorithm");
			}else{tp.append("");}
		}
	}
	tp.append("");
}

private ImagePlus subtractABluredImage(ImagePlus imp, double radius) {
	ImagePlus outImp = IJ.createHyperStack("divided image", imp.getWidth(), imp.getHeight(), 1, imp.getNSlices(), imp.getNFrames(), 32);
	outImp.setCalibration(imp.getCalibration());
	outImp.setOverlay(imp.getOverlay());
	ImagePlus tempImp;
	for(int s = 0; s < imp.getNSlices(); s++) {
		for(int t = 0; t < imp.getNFrames(); t++) {
			tempImp = IJ.createHyperStack("temp", imp.getWidth(), imp.getHeight(), 1, 1, 1, imp.getBitDepth());
			for(int x = 0; x < imp.getWidth(); x++) {
				for(int y = 0; y < imp.getHeight(); y++) {
					tempImp.getStack().setVoxel(x, y, 0, imp.getStack().getVoxel(x, y, imp.getStackIndex(1 , s+1, t+1)-1));
				}
			}
			
			tempImp.getProcessor().blurGaussian(radius);
			for(int x = 0; x < imp.getWidth(); x++) {
				for(int y = 0; y < imp.getHeight(); y++) {
					outImp.getStack().setVoxel(x, y, outImp.getStackIndex(1, s+1, t+1)-1, 
							imp.getStack().getVoxel(x, y, imp.getStackIndex(1 , s+1, t+1)-1) 
							- tempImp.getStack().getVoxel(x, y, 0));
				}
			}
		}
	}
	return outImp;	
}

/**
 * get number of series 
 * */
private int getNumberOfSeries(ImporterOptions options) throws FormatException, IOException{
	ImportProcess process = new ImportProcess(options);
	if (!process.execute()) return -1;
	return process.getSeriesCount();
}

/**
 * @return width and height of a specific @param series (0 <= series < number of series)
 * */
private int [] getWidthHeigth(ImporterOptions options, int series) throws FormatException, IOException{
	ImportProcess process = new ImportProcess(options);
	if (!process.execute()) return new int [] {-1, -1};
	return new int [] {process.getCropRegion(series).width, process.getCropRegion(series).height};
}

/**
 * @return name of the @param series (0 <= series < number of series)
 * */
private String getSeriesName(ImporterOptions options, int series) throws FormatException, IOException{
	ImportProcess process = new ImportProcess(options);
	if (!process.execute()) return "NaN";
	return process.getSeriesLabel(series);
}

ImagePlus getOtherBitImageFromBinary32bit(ImagePlus imp, boolean copyOverlay, int bitDepth) {
	ImagePlus impNew = IJ.createHyperStack("channel image", imp.getWidth(), imp.getHeight(), 1,
			imp.getNSlices(), imp.getNFrames(), bitDepth);
	
	double min = Double.POSITIVE_INFINITY;
	double maxNew = Math.pow(2.0, (double) bitDepth)-1;
	int index, indexNew;
	

	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			for(int s = 0; s < imp.getNSlices(); s++){
				for(int f = 0; f < imp.getNFrames(); f++){
					index = imp.getStackIndex(1, s+1, f+1)-1;
					if(imp.getStack().getVoxel(x, y, index) < min) {
						min = imp.getStack().getVoxel(x, y, index);
					}
				}					
			}
		}
	}
	
	{
		for(int x = 0; x < imp.getWidth(); x++){
			for(int y = 0; y < imp.getHeight(); y++){
				for(int s = 0; s < imp.getNSlices(); s++){
					for(int f = 0; f < imp.getNFrames(); f++){
						index = imp.getStackIndex(1, s+1, f+1)-1;
						indexNew = impNew.getStackIndex(1, s+1, f+1)-1;
						if(imp.getStack().getVoxel(x, y, index) == min) {
							impNew.getStack().setVoxel(x, y, indexNew, 0.0);
						}else {
							impNew.getStack().setVoxel(x, y, indexNew, maxNew);
						}
					}					
				}
			}
		}
	}
	impNew.setDisplayRange(0,maxNew);
	if(copyOverlay)	impNew.setOverlay(imp.getOverlay().duplicate());
		
	impNew.setCalibration(imp.getCalibration());
	return impNew;	
}

private static LUT getLUT(double [][] array, boolean multiplyWith255) {
	byte [] red = new byte [array.length];
	byte [] green = new byte [array.length];
	byte [] blue = new byte [array.length];
	for(int i = 0; i < red.length; i++) {		
		if(multiplyWith255) {
			red [i] = (byte) (array [i][0] * 255.0);
			green [i] = (byte) (array [i][1] * 255.0);
			blue [i] = (byte) (array [i][2] * 255.0);
		}else {
			red [i] = (byte) (array [i][0]);
			green [i] = (byte) (array [i][1]);
			blue [i] = (byte) (array [i][2]);
		}
	}
	
	if(red.length < 256) {
		//Interpolate
		byte [] newRed = new byte [256];
		byte [] newGreen = new byte [256];
		byte [] newBlue = new byte [256];
		int oldPos;
		for (int i = 0; i < newRed.length; i++) {
			oldPos = (int)((double) i * (double) red.length / 256.0);
			newRed [i] = red [oldPos];
			newGreen [i] = green [oldPos];
			newBlue [i] = blue [oldPos];
		}
		return new LUT (8,newRed.length,newRed,newGreen,newBlue);
	}else {
		return new LUT (8,red.length,red,green,blue);
	}	
}

private void stayAwake() {
	try {
		robo.mouseMove(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);		
	}catch(Exception e) {		
	}
}
}//end main class
