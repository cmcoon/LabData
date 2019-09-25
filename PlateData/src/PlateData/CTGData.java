package PlateData;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * CTGData objects are meant to mimic the data structures resulting from cell titer glow assay data analysis. Object creation
 * requires a standard CTG plate data file with values including labeled lanes starting in first top left cell in
 * Excel file. CTGData handles multiple plate reads separated by one row of cells and also breaks up top and bottom 4 rows of 
 * data into separate arrays for distinct analysis. Each data set array is normalized to control wells and stored in an ArrayList
 * called dataSets.This class provides implementation for displaying resulting data for comparison in cell metabolism between
 * control untreated wells and drug treated wells. 
 * 
 * @author Colin Coon
 * @date   9.22.19
 *
 */

public class CTGData extends PlateData{
	private ArrayList<double[][]> dataSets;	// holds each data set top/bottom for each plate separately
	private ArrayList<Double> dataSetControlAverages;	// Average value for control well raw reads corresponding to each data set
	
	private ArrayList<String> dataSetNames;	// Optionally provided name for each data set
	
	private String[] columnLabels = {"control", "control", "0.003uM", "0.01uM", "0.03uM", "0.1uM", "0.3uM", "1uM", "3uM", "10uM", "control", "control"};
	
	
	/**
	 * Constructor for CTGData object that just requires acceptable CTG excel data. 
	 * Top and bottom normalization and percent viability calculated on object creation. 
	 * 
	 * @param file Excel File with plate read CTG data
	 * @throws Exception 
	 */
	public CTGData(FileInputStream file) throws Exception{
		this(file, null);
	}
	
	/**
	 * Constructor for CTGData object that just requires acceptable CTG excel data. 
	 * Top and bottom normalization and percent viability calculated on object creation. 
	 * 
	 * @param file Excel File with plate read CTG data
	 * @throws Exception 
	 */
	public CTGData(FileInputStream file, ArrayList<String> dataNames) throws Exception{
		super();
		
		dataSets = new ArrayList<>();
		processRawData(file);
		
		dataSetControlAverages = new ArrayList<>();
		processControlAverages();
		
		normalizeDataSets();
	}
	
	/**
	 * Normalize each array data set. Normalization includes dividing each value by
	 * corresponding control value average multiplied by 100 to get a percent
	 * of existing metabolism in comparison to control untreated wells.
	 */
	private void normalizeDataSets() {
		// Iterate through each data set and normalize to control average value
		for(int k = 0; k < dataSets.size(); k++) {
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 12; j++) {
					dataSets.get(k)[i][j] = ((dataSets.get(k)[i][j] / dataSetControlAverages.get(k))*100);
					//System.out.println(dataSets.get(k)[i][j]);
				}
			}
		}
	}
	
	/**
	 * Takes average of all top control wells for corresponding data set and stores in ArrayList
	 * dataSetControlAverages. Index of average will be same as index for corresponding data set and name. 
	 */
	private void processControlAverages() {
		double controlAverage = 0;
		
		// Loop through all data sets and compute average (left two columns nd right two columns)
		for(double[][] e: dataSets) {
			controlAverage = 0;
			
			// Averaging first two and last two columns
			for(int i = 0; i < 12; i++) {
				// Skip over 8 rows to last control values
				if(i == 2)
					i += 8;
				for(int j = 0; j < 4; j++) {
					//System.out.println(e[j][i]);
					controlAverage += (e[j][i]);
				}
			}
			
			// Average for 16 control wells
			controlAverage /= 16;
			dataSetControlAverages.add(controlAverage);
		}
	}
	
	/**
	 * 
	 * @param file
	 * @throws IOException
	 */
	@Override
	public void processRawData(FileInputStream file) throws IOException{
		//Create workbook object using input stream object
		XSSFWorkbook workbook = new XSSFWorkbook(file);
				
		//Get first sheet of workbook/excel file
		XSSFSheet sheet = workbook.getSheetAt(0);
				
		//Create 2D array to store values, fixed to store values of specified input
		//Since iterating through the whole file we need to account for blank rows
		double[][] rawDataTop;
		double[][] rawDataBot;
				
		//variables for counting 
		int i = 0;
		int j = 0;
				
		//Create Iterator interface for rows in excel document
		Iterator<Row> rowIterator = sheet.iterator();
		
		// Wil grab excel data starting from first plate
		int firstRowCount = 1;
		
		// Set first row and cell to first row and column cell
		Row row = sheet.getRow(firstRowCount);
		Cell cell = row.getCell(1);
		
		boolean continueRunning = true;
		
		// Loop runs until an empty cell/ plate value is encountered
		while(continueRunning) {
			rawDataTop = new double[4][12];
			rawDataBot = new double[4][12];

			i = 0;	// Reset storage array to first row
			
			//Iterate skipping first row and through next 8 rows
			for(int p = firstRowCount; p < firstRowCount + 8; p++) {
				Row rowAttempt = sheet.getRow(p);
						
				j = 0;	// Reset storage array to first column
						
				//Loop through 12 cell values excluding first cell
				for(int k = 1; k < 13; k++) {
					Cell cellAttempt = rowAttempt.getCell(k);
							
					// After getting cell type and value we set in top or bottom rawData array respectively
					switch(cellAttempt.getCellType()) {
						case NUMERIC:
							if(i < 4) {
								rawDataTop[i][j] = cellAttempt.getNumericCellValue();
							}else {
								rawDataBot[i - 4][j] = cellAttempt.getNumericCellValue();
							}
						j++;
					}
				}
				i++;
			}	
			
			// Once top and bottom arrays have been digested we add them consecutively to all dataSets ArrayList
			dataSets.add(rawDataTop);
			dataSets.add(rawDataBot);
			
			firstRowCount += 10;	// After each plate is consumed we increment 10 rows to next plate
			
			// Stop loop from running if incremented cell is empty
			try {
				row = sheet.getRow(firstRowCount);
				cell = row.getCell(1);
			} catch (Exception e) {
				continueRunning = false;
			}
		}
	}
	
	/**
	 * Returns the ArrayList holding all corresponding data set control values
	 * 
	 * @return dataSets
	 */
	public ArrayList<double[][]> getDataSets(){
		return dataSets;
	}
	
	/**
	 * Returns the average value for all control wells in top data set
	 * 
	 * @return topControlAverage
	 */
	public ArrayList<Double> getDataSetControlAverages() {
		return dataSetControlAverages;
	}
	
	/**
	 * Return 1D array holding string values for uM drug amount in each column
	 * 
	 * @return columnLabels array
	 */
	public String[] getColumnLabels() {
		return columnLabels;
	}
	
	/**
	 * Prints the normalized values held in array topNormaizedValues.
	 * Method will first print column label values for amount of drug.
	 */
	public void printNormalizedDataSets() {
		System.out.println();
		for(double[][] e: dataSets) {
			this.printColumnLabels();
			super.print2DArrayRoundedValues(e);
		}
	}
	
	/**
	 * Method prints column labels for corresponding drug amount in wells
	 */
	public void printColumnLabels() {
		for(int i = 0; i < columnLabels.length; i++) {
			System.out.print(columnLabels[i] + "\t\t");
		}
		System.out.println();
	}	
}