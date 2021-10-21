package adipoQ_preparator_jnh;
/** ===============================================================================
* AdipoQ Preparator Version 0.1.0
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
* Date: October 13, 2020 (This Version: October 19, 2021)
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
	static final String PLUGINVERSION = "0.1.0";
	
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
	
	//Progress Dialog
	ProgressDialog progress;	
	boolean processingDone = false;	
	boolean continueProcessing = true;
	
	//-----------------define params-----------------
	static final String[] taskVariant = {"active image in FIJI","multiple images (open multi-task manager)", "all images open in FIJI"};
	String selectedTaskVariant = taskVariant[1];
	int tasks = 1;

	final static String[] settingsMethod = {"manually enter preferences (show default settings for histology)", "manually enter preferences (show default settings for cultured cells)", "load preferences from existing AdipoQ Preparator metadata file"};
	String selectedSettingsVariant = settingsMethod [0];
	
	String loadSeries = "ALL";
	
//	int channelID = 1;
	int channelIDs [] = new int []{1};
	boolean includeDuplicateChannel = true;
	boolean deleteOtherChannels = true;

	boolean preBlur = true;
	double preBlurSigma = 2.0;
	boolean subtractBluredImage = true;
	double subtractBlurSigma = 3.0;
	
	boolean excludeZeroRegions = true;
	double closeGapsRadius = 5.0;
	boolean removeParticles = true;
	double removeRadius = 20.0;
	double linkGapsForRemoveRadius = 5.0;
	String [] algorithm = {"Default", "IJ_IsoData", "Huang", "Intermodes", "IsoData", "Li", "MaxEntropy", "Mean", 
			"MinError", "Minimum", "Moments", "Otsu", "Percentile", "RenyiEntropy", "Shanbhag", "Triangle", 
			"Yen", "CUSTOM threshold"};
	String chosenAlgorithm = "Triangle";
	double customThr = 0.0;
	boolean darkBackground = false;
	final static String[] bgMethod = {"detect bright structures (dark background; e.g. immunofluorescence image)", "detect dark structures (bright background; e.g. histology)"};
	String selectedBgVariant = bgMethod [0];
	
	boolean despeckle = true;
	boolean linkForROI = false;
	boolean fillHoles = true;
	boolean watershed = false;
		
	static final String[] outputVariant = {"save as filename + suffix 'AQP'", "save as filename + suffix 'AQP' + date"};
	String chosenOutputName = outputVariant[0];
	
	static final String[] nrFormats = {"US (0.00...)", "Germany (0,00...)"};
	String ChosenNumberFormat = nrFormats[0];		

	Robot robo;
	boolean keepAwake = false;
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
	gd.setInsets(0,0,0);	gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2019-2021 JN Hansen", SuperHeadingFont);
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
					|| name [i].substring(name[i].lastIndexOf(".")).equals(".TIFF")) {
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
	
	
   	ImagePlus imp, tempImp, mask;
   	CompositeImage outImp;
   	TextPanel tp1;
   	double threshold;
   	Date startDate;
   	LUT [] originalLuts;
   	LUT [] newLuts;
   	String sliceLabels [][];
   	int indexOld, indexNew;

	boolean backgroundPref = Prefs.blackBackground;
	
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
		   			if(name[task].contains(".tif")){
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
		   	
		   	sliceLabels = new String [imp.getNFrames()][imp.getNSlices()];
		   	for(int s = 0; s < imp.getNSlices(); s++){
					for(int f = 0; f < imp.getNFrames(); f++){
					indexOld = imp.getStackIndex(channelIDs [0], s+1, f+1)-1;
   					try{
   						if(imp.getStack().getSliceLabel(indexOld+1).equals(null)){
	   						sliceLabels [f][s] = "Channel " + (channelIDs [0]) + " S" + (s+1) + "/" + imp.getNSlices() 
	   							+  " T" + (f+1) + "/" + imp.getNFrames();
	   					}else if(imp.getStack().getSliceLabel(indexOld+1).isEmpty()){
	   						sliceLabels [f][s] = "Channel " + (channelIDs [0]) + " S" + (s+1) + "/" + imp.getNSlices() 
   							+  " T" + (f+1) + "/" + imp.getNFrames();
	   					}else{
	   						sliceLabels [f][s] = imp.getStack().getSliceLabel(indexOld+1);
	   					}
   					}catch(Exception e){
   						sliceLabels [f][s] =  "Channel " + (channelIDs [0]) + " S" + (s+1) + "/" + imp.getNSlices() 
							+  " T" + (f+1) + "/" + imp.getNFrames();
   					}
				}
			}
		   	
		   	tempImp = copyChannel(imp, channelIDs [0], false, false);
			if(deleteOtherChannels) {
			   	imp.close();
				imp = tempImp.duplicate();				
			}
			
   			if(keepAwake) {
				stayAwake();
			}
   			
//   		tempImp.show();
//			new WaitForUserDialog("before blur").show();
//			tempImp.hide();
			
			if(preBlur) {
				progress.updateBarText("Bluring image ...");
				tempImp.getProcessor().blurGaussian(preBlurSigma);
				progress.addToBar(0.1);
				
//				tempImp.show();
//				new WaitForUserDialog("blurred").show();
//				tempImp.hide();
			}
			
			if(subtractBluredImage) {
				progress.updateBarText("Subtract blured image ...");
				tempImp = subtractABluredImage(tempImp, subtractBlurSigma);
				progress.addToBar(0.1);

//				tempImp.show();
//				new WaitForUserDialog("blur substr").show();
//				tempImp.hide();
			}
		
   			if(keepAwake) {
				stayAwake();
			}
			
   			
			Roi regionsAboveZero = null;
			if(excludeZeroRegions) {
				progress.updateBarText("get non-zero-pixel ROI (close-gaps radius = " + dfDialog.format(closeGapsRadius) + ")...");
				regionsAboveZero = getRegionsAboveZeroAsROI(tempImp, 1, closeGapsRadius);	
//				tp1.append("Close gaps radius for non-zero-pixel ROI	" + df6.format(closeGapsRadius));	
				tempImp.setRoi(regionsAboveZero);
				//save ROI
				RoiEncoder re;
				try{	
					re = new RoiEncoder(filePrefix + "_ROI.roi");					
					re.write(regionsAboveZero);				
				}catch(Exception e){
					IJ.error("Failed to correctly save rois!");
				}
			}
			
			if(!chosenAlgorithm.equals("CUSTOM threshold")) {				
//				tempImp.show();
//				new WaitForUserDialog("before thr").show();
//				tempImp.hide();
				progress.updateBarText("Determine threshold using " + chosenAlgorithm + " ...");
				if(darkBackground) {
					threshold = getSingleSliceImageThresholds(tempImp, 1, chosenAlgorithm, darkBackground)[0];
				}else {
					threshold = getSingleSliceImageThresholds(tempImp, 1, chosenAlgorithm, darkBackground)[1];					
				}
				tp1.append("Used " + chosenAlgorithm + " to determine the intensity threshold - threshold value:	" + df6.format(threshold));
				
				progress.addToBar(0.1);
				
//				tempImp.show();
//				new WaitForUserDialog("after thr").show();
//				tempImp.hide();
				
				progress.updateBarText("Segment image with threshold " + dfDialog.format(threshold) + " ...");
				segmentImage(tempImp, threshold, 0, tempImp, 0, false, darkBackground);
				
//				tempImp.show();
//				new WaitForUserDialog("bin").show();
//				tempImp.hide();
				
				progress.addToBar(0.1);				
				
			}else{
				progress.updateBarText("Set custom threshold " + customThr + " ...");
				threshold = customThr;
				tp1.append("Used " + chosenAlgorithm + " as intensity threshold - threshold value:	" + df6.format(threshold));

				progress.addToBar(0.5);
				
				progress.updateBarText("Segment image with threshold " + dfDialog.format(threshold) + " ...");
				segmentImage(tempImp, threshold, 0, tempImp, 0, false, darkBackground);

				progress.addToBar(0.1);
			}			
			
			if(excludeZeroRegions) {
				progress.updateBarText("Set pixels outside non-zero-Pixel ROI to zero in mask...");
				setRegionsOutsideRoiToZero(tempImp, 1, regionsAboveZero);
			}
			progress.addToBar(0.1);

   			if(keepAwake) {
				stayAwake();
			}
			
		   	tempImp.deleteRoi();
			
		   	if(tempImp.getBitDepth()!=8) {
				tempImp = getOtherBitImageFromBinary32bit(tempImp, false, 8);
//			   	tempImp.show();
//				new WaitForUserDialog("bitconv").show();
//				tempImp.hide();	
			}
		   	
			if(despeckle) {
				progress.updateBarText("Despeckle mask");
				IJ.run(tempImp, "Despeckle", "");
			}

			progress.addToBar(0.1);

   			if(keepAwake) {
				stayAwake();
			}
			if(removeParticles) {
				if(keepAwake) {
					stayAwake();
				}
				
				progress.updateBarText("Get mask with filled holes and closed gaps (radius " + dfDialog.format(removeRadius) + " px)...");
				mask = getFillHolesAndRemoveNoise(tempImp, linkGapsForRemoveRadius, removeRadius);
				
				progress.addToBar(0.1);

//			   	tempImp.show();
//				new WaitForUserDialog("calc").show();
//				tempImp.hide();	
				
			   	progress.updateBarText("Invert image...");
//					IJ.run(tempImp, "Invert", "");
				tempImp.getProcessor().invert();

	   			if(keepAwake) {
					stayAwake();
				}
	   			
//			   	tempImp.show();
//				new WaitForUserDialog("calc").show();
//				tempImp.hide();	
				
				progress.addToBar(0.1);
				
				ImageCalculator ic = new ImageCalculator();
				tempImp = ic.run("AND create", tempImp, mask);

				mask.changes = false;
				mask.close();
				progress.addToBar(0.1);
			}else {
				progress.addToBar(0.3);
			}
						
//		   	tempImp.show();
//			new WaitForUserDialog("Prefill").show();
//			tempImp.hide();	
			
			if(fillHoles) {
	   			if(keepAwake) {
					stayAwake();
				}
				progress.updateBarText("Fill holes...");
				IJ.run(tempImp, "Fill Holes", "");
			}

			if(watershed) {
	   			if(keepAwake) {
					stayAwake();
				}
				progress.updateBarText("Watershed...");
				IJ.run(tempImp, "Watershed", "");
			}

//		   	tempImp.show();
//			new WaitForUserDialog("calc").show();
//			tempImp.hide();	
			
			//TODO deleteOtherChannels

   			if(keepAwake) {
				stayAwake();
			}
   			
			if(deleteOtherChannels) {
				if(includeDuplicateChannel){
					progress.updateBarText("Add duplicated channel");
			   		outImp = (CompositeImage)IJ.createHyperStack(imp.getTitle() + " cq", imp.getWidth(), imp.getHeight(), 
			   				2, imp.getNSlices(), imp.getNFrames(), imp.getBitDepth());
			   		outImp.setCalibration(imp.getCalibration());
			   		outImp.setDisplayMode(IJ.COMPOSITE);
			   		for(int x = 0; x < imp.getWidth(); x++){
			   			for(int y = 0; y < imp.getHeight(); y++){
			   				for(int s = 0; s < imp.getNSlices(); s++){
			   					for(int f = 0; f < imp.getNFrames(); f++){
	   								indexOld = tempImp.getStackIndex(1, s+1, f+1)-1;
			   						indexNew = outImp.getStackIndex(1, s+1, f+1)-1;
			   						outImp.getStack().setVoxel(x, y, indexNew, tempImp.getStack().getVoxel(x, y, indexOld));
	   								
	   								indexOld = imp.getStackIndex(1, s+1, f+1)-1;
			   						indexNew = outImp.getStackIndex(2, s+1, f+1)-1;
			   						outImp.getStack().setVoxel(x, y, indexNew, imp.getStack().getVoxel(x, y, indexOld));
			   					}					
			   				}
			   			}
			   		}
			   		
			   		tempImp.changes = false;
					tempImp.close();
			   		
				   	newLuts = new LUT [outImp.getNChannels()];
				   	
				   	tp1.append("Channels in output image:");
			   		
			   		newLuts [0] = originalLuts [channelIDs [0]-1];
			   		newLuts [1] = originalLuts [channelIDs [0]-1];
			   		
			   		tp1.append("Channel " + 1 + ":	" + "previous channel " + (channelIDs [0]) + " (segmented)");
					tp1.append("Channel " + 2 + ":	" + "previous channel " + (channelIDs [0]) + " (unsegmented)");
					for(int s = 0; s < outImp.getNSlices(); s++){
	   					for(int f = 0; f < outImp.getNFrames(); f++){
	   						indexNew = outImp.getStackIndex(1, s+1, f+1)-1;
		   					outImp.getStack().setSliceLabel("segm " + sliceLabels [f][s], indexNew+1);
		   					indexNew = outImp.getStackIndex(2, s+1, f+1)-1;
		   					outImp.getStack().setSliceLabel("" + sliceLabels [f][s], indexNew+1);
	   					}
					}
			   		
					outImp.setDisplayMode(IJ.COMPOSITE);
					outImp.setLuts(newLuts);
					IJ.saveAsTiff(outImp, filePrefix + ".tif");
					outImp.changes = false;
					outImp.close();
			   	}else{
					IJ.saveAsTiff(tempImp, filePrefix + ".tif");	
					tempImp.changes = false;
					tempImp.close();
			   	}
			}else {
				if(includeDuplicateChannel){
					int addC = 1;
					double maxValue = Math.pow(2.0, imp.getBitDepth())-1.0;
			   		
					ImagePlus tempImp2 = IJ.createHyperStack(imp.getTitle() + " lq", imp.getWidth(), imp.getHeight(), 
			   				imp.getNChannels()+addC,
			   				imp.getNSlices(), imp.getNFrames(), imp.getBitDepth());
			   		tempImp2.setCalibration(imp.getCalibration());
			   		tempImp2.setDisplayMode(IJ.COMPOSITE);
			   		
			   		int cNew = 0;
			   		for(int x = 0; x < imp.getWidth(); x++){
			   			for(int y = 0; y < imp.getHeight(); y++){
			   				for(int s = 0; s < imp.getNSlices(); s++){
			   					for(int f = 0; f < imp.getNFrames(); f++){
		   							cNew = 0;
			   						for(int c = 0; c < imp.getNChannels(); c++){
			   							for(int i = 0; i < channelIDs.length; i++){
			   								if(c+1 == channelIDs [i] && includeDuplicateChannel){
						   						indexOld = tempImp.getStackIndex(1, s+1, f+1)-1;
						   						indexNew = tempImp2.getStackIndex(c+cNew+1, s+1, f+1)-1;
						   						if(tempImp.getStack().getVoxel(x, y, indexOld) != 0.0) {
						   							tempImp2.getStack().setVoxel(x, y, indexNew, maxValue);
						   						}
						   						cNew ++;
				   							}
			   							}
			   							indexOld = imp.getStackIndex(c+1, s+1, f+1)-1;
				   						indexNew = tempImp2.getStackIndex(c+cNew+1, s+1, f+1)-1;
				   						tempImp2.getStack().setVoxel(x, y, indexNew, imp.getStack().getVoxel(x, y, indexOld));
			   						}
			   					}					
			   				}
			   			}
			   		}
			   		
			   		originalLuts = new LUT [imp.getNChannels()];
				   	for(int c = 0; c < imp.getNChannels(); c++){
				   		imp.setC(c+1);
				   		originalLuts[c] = imp.getChannelProcessor().getLut();
				   	}		   		
				   	newLuts = new LUT [tempImp2.getNChannels()];
				   	
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
				   					indexNew = tempImp2.getStackIndex(c+cNew+1, s+1, f+1)-1;
				   					try{
				   						if(imp.getStack().getSliceLabel(indexOld+1).equals(null)){
					   						copyStr = "Channel " + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
					   							+  " T" + (f+1) + "/" + imp.getNFrames();
					   					}else if(imp.getStack().getSliceLabel(indexOld+1).isEmpty()){
					   						copyStr = "Channel " + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
				   							+  " T" + (f+1) + "/" + imp.getNFrames();
					   					}else{
					   						copyStr = imp.getStack().getSliceLabel(indexOld+1);
					   					}
				   					}catch(Exception e){
				   						copyStr = "Channel " + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
			   							+  " T" + (f+1) + "/" + imp.getNFrames();
				   					}				   					
				   					tempImp2.getStack().setSliceLabel("segm " + copyStr, indexNew+1);
			   					}
							}
							
							for(int i = 0; i < channelIDs.length; i++){
								if(c+1 == channelIDs [i]){
									cNew ++;
	   								newLuts [c+cNew] = originalLuts [c];
	   								tp1.append("Channel " + (c+1+cNew) + ":	" + "previous channel " + (c+1) + "");
	   								
	   								for(int s = 0; s < imp.getNSlices(); s++){
	   				   					for(int f = 0; f < imp.getNFrames(); f++){
		   				   					indexOld = imp.getStackIndex(c+1, s+1, f+1)-1;
						   					indexNew = tempImp2.getStackIndex(c+cNew+1, s+1, f+1)-1;
						   					try{
						   						if(imp.getStack().getSliceLabel(indexOld+1).equals(null)){
							   						copyStr = "Channel " + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
							   							+  " T" + (f+1) + "/" + imp.getNFrames();
							   					}else if(imp.getStack().getSliceLabel(indexOld+1).isEmpty()){
							   						copyStr = "Channel " + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
						   							+  " T" + (f+1) + "/" + imp.getNFrames();
							   					}else{
							   						copyStr = imp.getStack().getSliceLabel(indexOld+1);
							   					}
						   					}catch(Exception e){
						   						copyStr = "Channel " + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
					   							+  " T" + (f+1) + "/" + imp.getNFrames();
						   					}					   					
						   					tempImp2.getStack().setSliceLabel(copyStr, indexNew+1);
	   				   					}
	   								}   								
								}
							}
						}else{
							tp1.append("Channel " + (c+1+cNew) + ":	" + "previous channel " + (c+1) + "");
							for(int s = 0; s < imp.getNSlices(); s++){
			   					for(int f = 0; f < imp.getNFrames(); f++){
			   						indexOld = imp.getStackIndex(c+1, s+1, f+1)-1;
				   					indexNew = tempImp2.getStackIndex(c+cNew+1, s+1, f+1)-1;
				   					try{
				   						if(imp.getStack().getSliceLabel(indexOld+1).equals(null)){
					   						copyStr = "Channel " + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
					   							+  " T" + (f+1) + "/" + imp.getNFrames();
					   					}else if(imp.getStack().getSliceLabel(indexOld+1).isEmpty()){
					   						copyStr = "Channel " + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
				   							+  " T" + (f+1) + "/" + imp.getNFrames();
					   					}else{
					   						copyStr = imp.getStack().getSliceLabel(indexOld+1);
					   					}
				   					}catch(Exception e){
				   						copyStr = "Channel " + (c+1) + " S" + (s+1) + "/" + imp.getNSlices() 
			   							+  " T" + (f+1) + "/" + imp.getNFrames();
				   					}			   					
				   					tempImp2.getStack().setSliceLabel(copyStr, indexNew+1);
			   					}
							}
						}
					}
			   		
			   		if(keepAwake) {
						stayAwake();
					}
			   			   				   		
		   			CompositeImage ci = (CompositeImage) tempImp2;
		   			ci.setDisplayMode(IJ.COMPOSITE);
		   			ci.setLuts(newLuts);
					IJ.saveAsTiff(ci, filePrefix + ".tif");
			   		tempImp2.changes = false;
					tempImp2.close();
					ci.changes = false;
					ci.close();					
			   	}else{
			   		for(int x = 0; x < imp.getWidth(); x++){
			   			for(int y = 0; y < imp.getHeight(); y++){
			   				for(int s = 0; s < imp.getNSlices(); s++){
			   					for(int f = 0; f < imp.getNFrames(); f++){
		   							for(int i = 0; i < channelIDs.length; i++){
//			   								if(c+1 == channelIDs [i] && includeDuplicateChannel){
//						   						indexOld = tempImp.getStackIndex(1, s+1, f+1)-1;
//						   						indexNew = tempImp2.getStackIndex(c+cNew+1, s+1, f+1)-1;
//						   						if(tempImp.getStack().getVoxel(x, y, indexOld) != 0.0) {
//						   							tempImp2.getStack().setVoxel(x, y, indexNew, maxValue);
//						   						}
//						   						cNew ++;
//				   							}
//			   							}			   							
			   							indexOld = tempImp.getStackIndex(1, s+1, f+1)-1;
			   							indexNew = imp.getStackIndex(channelIDs[i]+1, s+1, f+1)-1;
				   						imp.getStack().setVoxel(x, y, indexNew, tempImp.getStack().getVoxel(x, y, indexOld));
			   						}
			   					}					
			   				}
			   			}
			   		}
			   		IJ.saveAsTiff(imp, filePrefix + ".tif");
			   	}				
				tempImp.changes = false;
				tempImp.close();
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
	double add = (0.1/imp.getWidth());
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
private ImagePlus getFillHolesAndRemoveNoise(ImagePlus imp, double closeGapsRadius, double closeHolesRadius){
	ImagePlus tempImp = imp.duplicate();

	if(linkForROI) {
		IJ.run(tempImp, "Maximum...", "radius=" + dfDialog.format(closeGapsRadius));
	}
	
	IJ.run(tempImp, "Fill Holes", "");
	
	if(linkForROI) {
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
	
	//read individual channel settings
	String tempString;
	
	channelIDs [0] = -1;
	preBlur = false;
	preBlurSigma = -1.0;
	subtractBluredImage = false;
	subtractBlurSigma = -1.0;
	excludeZeroRegions = false;
	includeDuplicateChannel = false;
	deleteOtherChannels = false;
	linkForROI = false;
	removeParticles = false;
	removeRadius = -1.0;
	despeckle = false;
	fillHoles = false;
	watershed = false;
	darkBackground = false;
	boolean readThrough = false;
	
	IJ.log("READING PREFERENCES:");
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
					tempString = line.substring(line.lastIndexOf("	")+1);
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					channelIDs [0] = Integer.parseInt(tempString);	
					IJ.log("Channel nr = " + channelIDs [0]);
				}
				if(line.contains("Channel duplicated to include a copy of the channel that is not processed")){
					includeDuplicateChannel = true;
					IJ.log("Duplicate Channel");
				}
				if(line.contains("Deleted all channels except the channel(s) to be segmented")){
					deleteOtherChannels = true;
					IJ.log("Delete other channels");
				}
				
				if(line.contains("Blur image before analysis - Gaussian sigma (px):")){
					preBlur = true;
					tempString = line.substring(line.lastIndexOf("	")+1);
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					preBlurSigma = Double.parseDouble(tempString);
					IJ.log("Blur image before analysis - Gaussian sigma (px) = " + preBlurSigma);						
				}
				
				if(line.contains("Subtract blurred copy of the image (for normalization) - Gaussian sigma (px):")){
					subtractBluredImage = true;
					tempString = line.substring(line.lastIndexOf("	")+1);
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					subtractBlurSigma = Double.parseDouble(tempString);
					IJ.log("Subtract blurred copy of the image (for normalization) - Gaussian sigma (px) = " + subtractBlurSigma);						
				}
				
				if(line.contains("Excluded zero intensity pixels in threshold calculation - radius of tolerated gaps (px):")){
					excludeZeroRegions = true;
					tempString = line.substring(line.lastIndexOf("	")+1);
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					closeGapsRadius = Double.parseDouble(tempString);
					IJ.log("Exclude zero intensity - close gaps rad = " + closeGapsRadius);						
				}
				
				if(line.contains("Segmentation method:")){
					if(line.contains("CUSTOM threshold")) {
						chosenAlgorithm = algorithm [18];
						line = br.readLine();
						if(!line.contains("Custom threshold value:")){
							IJ.error("Could not find custom threshold value in settings - no preferences loaded!");
							return false;
						}
						tempString = line.substring(line.lastIndexOf("	")+1);
						if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
						customThr = Double.parseDouble(tempString);
						IJ.log("Custom thr" + customThr);
					}else if(line.contains("applying intensity threshold based ")) {
						for(int a = 0; a < algorithm.length; a++) {
							if(line.contains(algorithm[a])) {
								chosenAlgorithm = algorithm [a];
								break;
							}
						}
						IJ.log("Segment with " + chosenAlgorithm);	
					}
				}
				
				if(line.contains("Background definition: detect bright objects on dark background")) {
					IJ.log("Detect bright objects on dark background");
					darkBackground = true;
				}else if(line.contains("Background definition: detect dark objects on bright background")) {
					IJ.log("Detect dark objects on bright background");
					darkBackground = false;
				}
				
				
				if(line.contains("Radius of particles to be removed as noise while detecting adipose tissue regions (px)") 
						|| line.contains("Auto detect the region of interest - radius of exluded regions (px)")){
					removeParticles = true;
					tempString = line.substring(line.lastIndexOf("	")+1);
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					removeRadius = Double.parseDouble(tempString);
					
					IJ.log("Auto detect - exclude regions (px) = " + removeRadius);						
				}
				
				if(line.contains("No auto-detection of region of interest applied.")){
					removeParticles = false;
					removeRadius = 0.0;
					
					IJ.log("Do not auto-detect regions and do not exclude regions");	
				}
				
				if(line.contains("Close gaps for detecting tissue regions (px):")){
					tempString = line.substring(line.lastIndexOf("	")+1);
					if(tempString.contains(",") && !tempString.contains("."))	tempString = tempString.replace(",", ".");
					linkForROI = true;
					linkGapsForRemoveRadius = Double.parseDouble(tempString);
					
					IJ.log("Close gaps for detecting tissue regions rad = " + linkGapsForRemoveRadius);						
				}
				
				if(line.contains("Despeckle mask")){
					despeckle = true;
					IJ.log("Despeckle mask");
				}
				if(line.contains("Fill holes in mask") || line.contains("Fill holes in segmented image")) {
					fillHoles = true;
					IJ.log("Fill holes in mask");
				}
				
				if(line.contains("Apply watershed algorithm")) {
					watershed = true;
					IJ.log("Apply watershed algorithm");
				}
				
				if(line.contains("'AdipoQ Preparator'")) {
					readThrough = true;
					line = br.readLine();
					line = br.readLine();
					if(line.contains("0.0.7")){
						deleteOtherChannels = true;
						IJ.log("Delete other channels");
					}
					darkBackground = false;
					IJ.log("Light background");
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
						removeParticles = false;
						removeRadius = 0.0;
						IJ.log("Do not auto-detect regions and do not exclude regions");
						darkBackground = true;
						IJ.log("Dark background");
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
	if(removeRadius == -1.0 || channelIDs [0] == -1 || !readThrough) {
		IJ.error("Problem with loading preferences - parameters missing or inappropriate text file.");
		return false;
	}
	return true;
}

/**
 * Show dialogs to enter settings
 * @param defaultType: 0 = histology, 1 = DAPI nuclei, 2 = cell culture
 * */
private boolean enterSettings(int defaultType) {
	/*
		Depending on set variant show default settings for one or the other
	*/
	if(defaultType == 0) {
		/**Histology settings*/
		channelIDs [0] = 1;
		includeDuplicateChannel = true;
		deleteOtherChannels = true;
		preBlur = false;
		preBlurSigma = 0.0;
		subtractBluredImage = false;
		subtractBlurSigma = 0.0;
		excludeZeroRegions = true;
		closeGapsRadius = 5.0;
		removeParticles = true;
		removeRadius = 20.0;
		despeckle = true;
		linkForROI = false;
		linkGapsForRemoveRadius = 5.0;
		chosenAlgorithm = "Triangle";
		customThr = 0.0;
		darkBackground = false;
		fillHoles = true;
		watershed = false;		
	}else if (defaultType == 1){
		/**DAPI settings*/
		channelIDs [0] = 1;
		includeDuplicateChannel = true;
		deleteOtherChannels = false;
		preBlur = true;
		preBlurSigma = 2.0;
		subtractBluredImage = true;
		subtractBlurSigma = 3.0;
		excludeZeroRegions = false;
		closeGapsRadius = 5.0;
		removeParticles = false;
		removeRadius = 20.0;
		despeckle = false;
		linkForROI = false;
		linkGapsForRemoveRadius = 5.0;
		chosenAlgorithm = "Otsu";
		customThr = 0.0;
		darkBackground = true;
		fillHoles = true;
		watershed = true;		
	}else if (defaultType == 1){
		/**Cell culture settings*/ //TODO
		/**DAPI settings*/
		channelIDs [0] = 1;
		includeDuplicateChannel = true;
		deleteOtherChannels = false;
		preBlur = true;
		preBlurSigma = 2.0;
		subtractBluredImage = true;
		subtractBlurSigma = 3.0;
		excludeZeroRegions = false;
		closeGapsRadius = 5.0;
		removeParticles = false;
		removeRadius = 20.0;
		despeckle = false;
		linkForROI = false;
		linkGapsForRemoveRadius = 5.0;
		chosenAlgorithm = "Otsu";
		customThr = 0.0;
		darkBackground = true;
		fillHoles = true;
		watershed = false;
	}else {
		IJ.error("Incorrect default Type - plugin cancelled!");
	}
	
	if(darkBackground) {
		selectedBgVariant = bgMethod[0];
	}else {
		selectedBgVariant = bgMethod[1];
	}
	
	while(true) {
		/*
		 * Show dialog
		 * */
		GenericDialog gd = new GenericDialog(PLUGINNAME + " on " + System.getProperty("os.name") + " - set parameters");	
		//show Dialog-----------------------------------------------------------------
		//.setInsets(top, left, bottom)
		gd.setInsets(0,0,0);		gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2019-2021 JN Hansen", SuperHeadingFont);
		gd.setInsets(0,10,0);		gd.addNumericField("Channel Nr (>= 1 & <= nr of channels) for quantification", channelIDs [0], 0);
		gd.setInsets(0,10,0);		gd.addCheckbox("Include raw copy of the channel in output image", includeDuplicateChannel);
		gd.setInsets(0,10,0);		gd.addCheckbox("Delete all non-segmented channels", deleteOtherChannels);

		gd.setInsets(0,10,0);		gd.addCheckbox("Blur image before analysis - Gaussian sigma (px)", preBlur);
		gd.setInsets(-23,100,0);		gd.addNumericField("", preBlurSigma, 3);
		
		gd.setInsets(0,10,0);		gd.addCheckbox("Subtract blurred copy of image (to normalize) - Gauss sigma (px)", subtractBluredImage);
		gd.setInsets(-23,100,0);		gd.addNumericField("", subtractBlurSigma, 3);
		
		
		gd.setInsets(0,10,0);		gd.addChoice("Segmentation method", algorithm, chosenAlgorithm);
		gd.setInsets(0,0,0);		gd.addNumericField("If 'CUSTOM threshold' was selected, specify threshold here", customThr, 2);
		gd.setInsets(0,10,0);		gd.addChoice("Background definition", bgMethod, selectedBgVariant);
		gd.setInsets(0,10,0);		gd.addCheckbox("Exclude zero-pixels in threshold calc. - tolerated gap radius (px)", excludeZeroRegions);
		gd.setInsets(-23,100,0);	gd.addNumericField("", closeGapsRadius, 3);
		
		gd.setInsets(0,10,0);		gd.addCheckbox("Despeckle segmented image", despeckle);

		gd.setInsets(0,10,0);	gd.addCheckbox("Detect tissue regions and remove smaller regions | minimum radius", removeParticles);
		gd.setInsets(-23,100,0);		gd.addNumericField("", removeRadius, 2);
		
		gd.setInsets(0,10,0);	gd.addCheckbox("During detecting tissue regions, close gaps | distance", linkForROI);
		gd.setInsets(-23,100,0);	gd.addNumericField("", linkGapsForRemoveRadius, 2);
		
		gd.setInsets(0,10,0);		gd.addCheckbox("Fill holes in segmented image", fillHoles);
		
		gd.setInsets(0,10,0);		gd.addCheckbox("Apply Watershed algorithm", watershed);
		
		/*
		 * TODO Add option to segment an additional channel
		 * */	
		
		gd.showDialog();
		//show Dialog-----------------------------------------------------------------

		//read and process variables--------------------------------------------------	
		{
			channelIDs [0] = (int) gd.getNextNumber();
			includeDuplicateChannel = gd.getNextBoolean();
			deleteOtherChannels = gd.getNextBoolean();
			preBlur = gd.getNextBoolean();
			preBlurSigma = (double) gd.getNextNumber();
			subtractBluredImage = gd.getNextBoolean();
			subtractBlurSigma = (double) gd.getNextNumber();			
			
			chosenAlgorithm = gd.getNextChoice();
			customThr = gd.getNextNumber();
			selectedBgVariant = gd.getNextChoice();
			if(selectedBgVariant == bgMethod[0]) {
				darkBackground = true;
			}else{
				darkBackground = false;
			}
			
			excludeZeroRegions = gd.getNextBoolean();
			closeGapsRadius = (double) gd.getNextNumber();
			
			despeckle = gd.getNextBoolean();
			removeParticles = gd.getNextBoolean();
			removeRadius = gd.getNextNumber();
			linkForROI = gd.getNextBoolean();
			linkGapsForRemoveRadius = gd.getNextNumber();
			fillHoles = gd.getNextBoolean();			
			watershed = gd.getNextBoolean();
		}
		//read and process variables--------------------------------------------------
		
		if (gd.wasCanceled()) return false;	
		
		if (linkForROI && !removeParticles) {
			new WaitForUserDialog("'Close gaps ...' option is only available when 'Detect tissue regions ...' option selected. Change of settings required.").show();
		}else {
			break;
		}
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
	tp.append("Preparation settings:	");
	
	{
		tp.append("	Channel Nr:	" + df0.format(channelIDs [0]));
		
		if(includeDuplicateChannel){
			tp.append("	Channel duplicated to include a copy of the channel that is not processed.");
		}else{tp.append("");}
		
		if(deleteOtherChannels){
			tp.append("	Deleted all channels except the channel(s) to be segmented.");
		}else{tp.append("");}
		
		if(preBlur){
			tp.append("	Blur image before analysis - Gaussian sigma (px):	" + df6.format(preBlurSigma));
		}else{tp.append("");}
		
		if(subtractBluredImage){
			tp.append("	Subtract blurred copy of the image (for normalization) - Gaussian sigma (px):	" + df6.format(subtractBlurSigma));
		}else{tp.append("");}
				
		if (chosenAlgorithm == "CUSTOM threshold"){
			tp.append("	Segmentation method:	" + "CUSTOM threshold");
			tp.append("		Custom threshold value:	" + df6.format(customThr));
		}else{
			tp.append("	Segmentation method:	applying intensity threshold based on the " + chosenAlgorithm + " threshold algorithm.");
			tp.append("");				
		}	
		
		if(darkBackground){
			tp.append("	Background definition: detect bright objects on dark background.");
		}else{
			tp.append("	Background definition: detect dark objects on bright background.");
		}

		if(excludeZeroRegions){
			tp.append("	Excluded zero intensity pixels in threshold calculation - radius of tolerated gaps (px):	" + df6.format(closeGapsRadius));
		}else{tp.append("");}		
		
		
		if(despeckle){
			tp.append("	Despeckle mask");
		}else{tp.append("");}
		
		if(removeParticles) {
			tp.append("	Auto detect the region of interest - radius of exluded regions (px):	" + df6.format(removeRadius));
			if(linkForROI){
				tp.append("	Close gaps for detecting tissue regions (px):	" + df6.format(linkGapsForRemoveRadius));
			}else{tp.append("");}		
		}else{
			tp.append("	No auto-detection of region of interest applied.");
			tp.append("");
		}	
				
		if(fillHoles){
			tp.append("	Fill holes in mask");
		}else{tp.append("");}
		
		if(watershed){
			tp.append("	Apply watershed algorithm");
		}else{tp.append("");}	

				
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

private void stayAwake() {
	try {
		robo.mouseMove(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);		
	}catch(Exception e) {		
	}
}
}//end main class
