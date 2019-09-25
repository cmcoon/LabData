package PlateData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

//TODO either in superclass or this class create method for excel export with sample name and amount to load

/**
 * BCAData objects are designed to calculate protein concentrations using a standard Pierce BSA protein assay kit.
 * An object oriented approach to this task was taken to ease integration with different user interfaces. Protein level measurement
 * is achieved by comparing colorometric absorbance measurements of known protein concentration standards with unknown protein
 * concentration standards. Unknown samples are compared to absorbance level vs protein conc. of the standards using
 * a line of best fit. Input requires plates to match layout specified in readme. Constructors exist allowing amount of protein and
 * sample triplicate or duplicate to be specified. 
 * 
 * @author Colin Coon
 * @version 1.1
 * @date 9/17/2019
 * 
 */

public class BCAData extends PlateData{
	private ArrayList<Double> stdAvgs; //List of averages of protein standards
	private ArrayList<Double> sampleAvgs; //List of averages for sample readings
	private ArrayList<Double> sampleProteinConcentrations; //Concentrations of samples ug/uL
	private ArrayList<Double> loadVolumes; //Volume required for specified protein level unit ug
	
	private ArrayList<String> sampleNames; //Optional sample names user input
	
	private int specifiedUg; //User specified micrograms of protein, default 20ug
	private double slope; 
	private double intercept;
	
	
	/**
	 * Constructs BCAData object given input excel file, no other parameters. All corresponding calculation sets 
	 * for both standards and unknowns are stored here. Default value for specifiedUg is 20 ug. Default is duplicate
	 * standards and samples.
	 * 
	 * Default constructor, does require file. Assumes duplicate and 20ug.
	 * 
	 * @param file
	 * @throws Exception
	 */
	public BCAData(FileInputStream file) throws Exception{
		this(file, 20, ReplicateNum.DUPLICATE, null);
	}
	
	
	/**
	 * Constructs BCAData object given input excel file, and amount of protein desired. All corresponding calculation sets 
	 * for both standards and unknowns are stored here. Default is duplicate standards and samples.
	 * 
	 * Constructor allowing micrograms of protein to be specified
	 * 
	 * @param file, protein
	 * @throws Exception
	 */
	public BCAData(FileInputStream file, int protein) throws Exception{
		this(file, protein, ReplicateNum.DUPLICATE, null);
	}
	
	
	/**
	 * Constructs BCAData object given input excel file and replicate number. All corresponding calculation sets 
	 * for both standards and unknowns are stored here. Default value for specifiedUg is 20 ug.
	 * 
	 * Constructor allowing replicates of duplicate or triplicate to be decided.
	 * 
	 * @param file, replicateNum
	 * @throws Exception
	 */
	public BCAData(FileInputStream file, ReplicateNum replicateNum) throws Exception{
		this(file, 20, replicateNum, null);
	}
	
	
	/**
	 * Constructs BCAData object given input excel file and list of sample names. All corresponding calculation sets 
	 * for both standards and unknowns are stored here. Default value for specifiedUg is 20 ug. Default is duplicate
	 * standards and samples.
	 * 
	 * Constructor using list of sample names.
	 * 
	 * @param file, sampleNames
	 * @throws Exception
	 */
	public BCAData(FileInputStream file, ArrayList<String> sampleNames) throws Exception{
		this(file, 20, ReplicateNum.DUPLICATE, sampleNames);
	}
	
	
	/**
	 * Constructs BCAData object given input excel file, protein amount, and replicate number. All corresponding
	 * calculation sets for both standards and unknowns are stored here.
	 * 
	 * Constructor using user defined protein amount and replicateNum
	 * 
	 * @param file, protein, replicateNum
	 * @throws Exception
	 */
	public BCAData(FileInputStream file, int protein, ReplicateNum replicateNum) throws Exception{
		this(file, protein, replicateNum, null);
	}
	
	
	/**
	 * Constructs BCAData object given input excel file, protein amount, and list of sample names. All corresponding 
	 * calculation sets for both standards and unknowns are stored here. Default is duplicate
	 * standards and samples.
	 * 
	 * Constructor when passed protein amount and list of sample names.
	 * 
	 * @param file, protein, sampleNames
	 * @throws Exception
	 */
	public BCAData(FileInputStream file, int protein, ArrayList<String> sampleNames) throws Exception{
		this(file, protein, ReplicateNum.DUPLICATE, sampleNames);
	}
	
	/**
	 * Constructs BCAData object given input excel file, replicate number, and list of sample names. All corresponding 
	 * calculation sets for both standards and unknowns are stored here. Default value for specifiedUg is 20 ug. 
	 * 
	 * Constructor when passed replicate number, and list of sample names
	 * 
	 * @param file, replicateNum, sampleNames
	 * @throws Exception
	 */
	public BCAData(FileInputStream file, ReplicateNum replicateNum, ArrayList<String> sampleNames) throws Exception{
		this(file, 20, replicateNum, sampleNames);
	}
	
	/**
	 * Constructs BCAData object given all required parameters. On object creation all data sets are initialized.
	 * Rest of ethodology relies on how user wants to interface with data. If list of sample names is not supplied
	 * then generic Sample 1, Sample 2, ... Sample n will be used. 
	 * 
	 * @param file, protein, replicateNum, sampleNames
	 * @throws Exception
	 */
	public BCAData(FileInputStream file, int protein, ReplicateNum replicateNum, ArrayList<String> samplesNameList) throws Exception{
		super(file);
		specifiedUg = protein;
		
		//Calls methods based on duplicate or triplicate values
		if(replicateNum == ReplicateNum.DUPLICATE) {
			stdAvgs = new ArrayList<Double>();
			processStdAveragesDuplicate();
			lineOfBestFit();
			
			sampleAvgs = new ArrayList<Double>();
			processSampleAvgsDuplicate();
			
		}else if (replicateNum == ReplicateNum.TRIPLICATE){
			stdAvgs = new ArrayList<Double>();
			processStdAveragesTriplicate();
			lineOfBestFit();
			
			sampleAvgs = new ArrayList<Double>();
			processSampleAvgsTriplicate();
		}

		sampleProteinConcentrations = new ArrayList<Double>();
		processConcentrations();
		
		loadVolumes = new ArrayList<Double>();
		calculateLoadVolumes();	
		
		sampleNames = new ArrayList<String>();
		setSampleNames(samplesNameList);
	}
	
	
	/**
	 * Sets the array sampleNames with constructor supplied array of sample names.
	 * If no names were passed then an array with strings Sample 1, Sample 2, ... Sample n
	 * will be stored.
	 * 
	 */
	public void setSampleNames(ArrayList<String> sampleNameList) {
		// Find way to handles this
		if(sampleNameList == null || sampleNameList.isEmpty()) {
			for(int i = 0; i < this.getLoadVolumes().size(); i++) {
				this.sampleNames.add("Sample " + (i+1));
			}
		}else if(sampleNameList.size() < this.getLoadVolumes().size()){
			System.out.println("Provided list of names not of proper length, will autopopulate");
			for(int i = 0; i < this.getLoadVolumes().size(); i++) {
				this.sampleNames.add("Sample " + (i+1));
			}
		}else {
			this.sampleNames = sampleNameList;
		}
	}

	
	/**
	 * Returns an array of average values for standards' raw absorbance values.
	 * 
	 * @return stdAvgs
	 */
	public ArrayList<Double> getStdAvgs() {
		return this.stdAvgs;
	}


	/**
	 * Returns an array of average values for sample absorbance values
	 * 
	 * @return sampleAvgs
	 */
	public ArrayList<Double> getSampleAvgs() {
		return this.sampleAvgs;
	}


	/**
	 * Returns an array or protein concentrations for corresponding samples.
	 * Calculated using line of best fit of protein standards.
	 * 
	 * @return sampleProteinConcentrations
	 */
	public ArrayList<Double> getSampleProteinConcentrations() {
		return this.sampleProteinConcentrations;
	}


	/**
	 * Returns an array of all the load volumes(amount to load) for corresponding
	 * samples based on protein concentration and desired micrograms.
	 * 
	 * @return loadVolumes
	 */
	public ArrayList<Double> getLoadVolumes() {
		return this.loadVolumes;
	}


	/**
	 * Returns an array of corresponding protein sample names for samples(not standards).
	 * May be a null array as sample names not a required parameter.
	 * 
	 * @return sampleNames
	 */
	public ArrayList<String> getSampleNames() {
		return this.sampleNames;
	}


	/**
	 * Returns the single value for amount of protein being to load and used for calculations.
	 * Cannot change this value, would need to make a new object. Future functionality should include
	 * setter that will recalculate neccesary values.
	 * 
	 * @return specified micrograms amount
	 */
	public int getSpecifiedUg() {
		return this.specifiedUg;
	}
	
	
	/**This method fills the array holding average calculations
	 * between the two replicate standards for samples 1 through 7. Default
	 * plate set up assumes duplicates. 
	 */
	private void processStdAveragesDuplicate() {
		double background = ((super.getRawDataValue(0, 0) + super.getRawDataValue(0, 1))/2);

		for(int i = 0; i < 7; i++) {
			stdAvgs.add((((super.getRawDataValue(i, 0) + super.getRawDataValue(i, 1))/2)-background));
		}
	}
	
	
	/**This method fills the array holding average calculations
	 * between the three replicate standards for samples 1 through 7
	 */
	private void processStdAveragesTriplicate() {
		double background = ((super.getRawDataValue(0, 0) + super.getRawDataValue(0, 1) + super.getRawDataValue(0, 2))/3);
		
		for(int i = 0; i < 7; i++) {
			stdAvgs.add((((super.getRawDataValue(i, 0) + super.getRawDataValue(i, 1) + super.getRawDataValue(i, 2))/3)-background));
		}
	}
	
	
	/**
	 * Method reads in and processes samples by taking their average and subtracting background noise
	 * placing these values into an ArrayList called samplesDataAvg. Default plate set up assumes duplicate 
	 * values. Gates for no sample by requiring value to be above 0.
	 * 
	 */
	private void processSampleAvgsDuplicate() {
		double background = ((super.getRawDataValue(0, 0) + super.getRawDataValue(0, 1))/2);
		double value = 0;
		
		//Loop through each pair of columns for duplicates, avg both values
		for(int i =2; i < 12; i+=2) {
			for(int j = 0; j < 8; j++) {
				value = (((super.getRawDataValue(j, i) + super.getRawDataValue(j, i+1))/2) - background);
				if(value > 0) {
					sampleAvgs.add(value);
				}	
			}
		}
	}
	
	
	/**
	 * Method reads in and processes samples by taking their average and subtracting background noise
	 * placing these values into an ArrayList called samplesDataAvg. Takes in triplicate samples. 
	 * Gates for no sample by requiring value to be above 0.
	 */
	private void processSampleAvgsTriplicate() {
		double background = ((super.getRawDataValue(0, 0) + super.getRawDataValue(0, 1) + super.getRawDataValue(0, 2))/3 );
		double value = 0;
		
		//Loop through each pair of columns for duplicates, avg both values
		for(int i = 3; i < 12; i += 3) {
			for(int j = 0; j < 8; j++) {
				value = (((super.getRawDataValue(j, i) + super.getRawDataValue(j, i+1) + super.getRawDataValue(j, i+2))/3) - background);
				if(value > 0) {
					sampleAvgs.add(value);
				}	
			}
		}
	}
	
	
	/**
	 * Using the averages of your standard samples and the corresponding
	 * micrograms of protein in each standard. Compute line of best fit and store
	 * line slope and intercept, used to calculate unknown concentrations. 
	 */
	private void lineOfBestFit() {
		SimpleRegression simpleRegression = new SimpleRegression(true);
		
		simpleRegression.addData(new double[][] {
			{stdAvgs.get(0),0},
			{stdAvgs.get(1),1},
			{stdAvgs.get(2),2},
			{stdAvgs.get(3),5},
			{stdAvgs.get(4),10},
			{stdAvgs.get(5),20},
			{stdAvgs.get(6),40},
		});
		
		slope = simpleRegression.getSlope();
		intercept = simpleRegression.getIntercept();	
	}
	
	
	/**
	 * Will compute concentration of protein in samples (ug/uL) using sample averages
	 */
	private void processConcentrations() {
		//Plug in averages to line of best fit from standards and divide by 2.5 since loading 2.5 uL
		for(double i : sampleAvgs) {
			 sampleProteinConcentrations.add(((i*this.getSlope())+this.getIntercept())/2.5);
		}
	}
	
	
	/**
	 * Create a list for the amount to load based on protein concentration and desired amount to load.
	 * Units for load volume is in uL.
	 */
	private void calculateLoadVolumes(){
		for(double i: sampleProteinConcentrations) {
			loadVolumes.add(this.getSpecifiedUg()/i);
		}
	}
	
	
	/**
	 * Will output load volumes and sample names to 2nd page in workbook
	 * 
	 * Method is incomplete, will rely on superclass
	 * 
	 * @param file
	 */
	public void BCAExcelOutput(FileOutputStream file) {
		//super.outputArrayExcel(file, this.getloadVolumes());
		
	}
	
	
	/**
	 * Getter method for slope of standard absorptions
	 * 
	 * @return slope
	 */
	public double getSlope() {
		return this.slope;
	}
	
	
	/**
	 * Getter method for y intercept of standard absorptions
	 * 
	 * @return intercept
	 */
	public double getIntercept() {
		return this.intercept;
	} 
	
	
	/**
	 * Print the array containing the average of the standard
	 * sample replicates.
	 */
	public void printStdAvgs() {
		super.printArrayListDoubles(stdAvgs);
	}
	
	
	/**
	 * Print the array containing the average of the sample replicates.
	 */
	public void printSampleAvgs() {
		super.printArrayListDoubles(sampleAvgs);
	}
	
	
	/**
	 * Print the array containing the sample concentrations
	 */
	public void printConcentrations() {
		super.printArrayListDoubles(sampleProteinConcentrations);
	}
	
	
	/**
	 * Print the array containing the volume to load for each sample in uL.
	 */
	public void printLoadVolumes() {
		super.printArrayListDoubles(loadVolumes);
	}
	
	
	/**
	 * Method will print out load volumes for each sample as specified by desired protein amount in ug.
	 * This method uses default names for each sample, ie Sample 1, Sample 2... Sample n rounded to two decimal points. 
	 * 
	 */
	public void printLoadVolumesFormatted() {
		System.out.println("\n" + super.getCreationDate());
		System.out.println();
		System.out.println("Load volumes based on " + specifiedUg + " ug of protein");
		System.out.println();
		
		for(int i = 0; i < loadVolumes.size(); i++) {
			System.out.printf("" + sampleNames.get(i) + ":   %.2f ug \n" , loadVolumes.get(i));
		}
	}
}
