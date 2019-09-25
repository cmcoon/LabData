package PlateData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.time.LocalDateTime;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//TODO output to excel file super method or would just handle in subclasses ?

/**
 * PlateData objects are meant to hold an array of data values corresponding to standard laboratory plate reader output. 
 * By specifying a .xls document with raw plate reader out put, data will be processed without labels into 2D
 * array for down stream analysis via object extension. 
 * 
 * @author Colin Coon
 * @version 1.1
 * @date 9/17/2019
 * 
 */

public class PlateData {
	private double[][] rawData; //Excel input of plate read 
	private String creationDate; 
	
	/**
	 * Constructs PlateData object given no input excel file. No data processing
	 * occurs, assumes that will happen downstream. Just sets creation date etc. 
	 * 
	 * Default constructor, does require file
	 * 
	 * @param file
	 * @throws Exception
	 */
	public PlateData() throws Exception{
		LocalDateTime date = LocalDateTime.now(); //Store date of object creation
		creationDate = date.getMonth() + "/" + date.getDayOfMonth() + "/" + date.getYear();
	}
	
	/**
	 * Constructs PlateData object given input excel file, no other parameters.
	 * Object consists of 2D array just consisting of values, no labels. Downstream
	 * processing occurs in extended objects. 
	 * 
	 * @param file
	 * @throws Exception
	 */
	public PlateData(FileInputStream file) throws Exception{
		rawData = new double[8][12];
		processRawData(file);
		
		LocalDateTime date = LocalDateTime.now(); //Store date of object creation
		creationDate = date.getMonth() + "/" + date.getDayOfMonth() + "/" + date.getYear();
	}
	
	/**
	 * Raw excel file as specified is processed and stored in 2D
	 * array called rawData
	 * 
	 * @param file
	 * @return rawData
	 * @throws Exception
	 */
	public void processRawData(FileInputStream file) throws Exception{
		//Create workbook object using input stream object
		XSSFWorkbook workbook = new XSSFWorkbook(file);
				
		//Get first sheet of workbook/excel file
		XSSFSheet sheet = workbook.getSheetAt(0);
				
		//variables for counting 
		int i = 0;
		int j = 0;
				
		//Create Iterator interface for rows in excel document
		Iterator<Row> rowIterator = sheet.iterator();
				
		//Iterate skipping first row and through next 8 rows
		for(int p = 1; p < 9; p++) {
			Row rowAttempt = sheet.getRow(p);
					
			j = 0;
					
			//Loop through 12 cell values excluding first cell
			for(int k = 1; k < 13; k++) {
				Cell cellAttempt = rowAttempt.getCell(k);
						
				switch(cellAttempt.getCellType()) {
					case NUMERIC:
						rawData[i][j] = cellAttempt.getNumericCellValue();
						j++;
				}
			}
			i++;
		}		
	}
	
	/**
	 * Method will output a number array to Excel file, 2nd page, next blank column
	 * Logic will include outputtting new column for each array in this array.
	 * 
	 * @param file for output, meant to be same as input file
	 * @param array for output, suggest labels and values
	 */
	public void outputArrayExcel(FileOutputStream file, ArrayList<Number> array, int page, int column) {
		
	
	}
	
	/**
	 * Prints the date this object was created
	 */
	public String getCreationDate() {
		return creationDate;
	}	
	
	/**
	 * Get a specific value from rawData array using standard array conventions. 
	 * 
	 * @return rawData double array
	 */
	public double getRawDataValue(int x, int y){
		return rawData[x][y];
	}
	
	/** 
	 * Simple method to print 2D Array.
	 * 
	 * @param array 1D array will be printed
	 */
	public void print2DArray(double[][] array) {
		for(int k = 0; k < array.length; k++) {
			for(int p = 0; p < array[k].length; p++) {
				System.out.print(array[k][p] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	/** 
	 * Simple method to print 2D Array where values will be rounded to 4 decimal 
	 * places and separated by a tab.
	 * 
	 * @param array 1D array will be printed
	 */
	public void print2DArrayRoundedValues(double[][] array) {
		for(int k = 0; k < array.length; k++) {
			for(int p = 0; p < array[k].length; p++) {
				System.out.printf("%.4f \t", array[k][p]);
			}
			System.out.println();
		}
		System.out.println();
	}
	
	/**
	 * Print an ArrayList of type <Double>.
	 * 
	 * @param list
	 */
	public void printArrayListDoubles(ArrayList<Double> list) {
		System.out.println();
		for(double i: list) {
			System.out.println(i);
		}
	}
	
	/**
	 * Print the raw data values, unaltered using the
	 * printRawData method.
	 */
	public void printRawData() {
		print2DArray(rawData);
	}
	
	/**
	 * Prints the date this object was created
	 */
	public void printCreationDate() {
		System.out.println("Date Created: " + creationDate);
	}	
}
