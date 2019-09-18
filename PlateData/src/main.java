import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * This class/method demonstrates some of the functionalities of this library/API.
 */
public class main {

	public static void main(String[] args) throws Exception {
		//Create input stream with file object for excel file
		FileInputStream file = new FileInputStream(new File("template.xlsx"));
		
		// Create an ArrayList with corresponding sample name
		ArrayList<String> names = new ArrayList<>();
		names.add("HCT-116 negative");
		names.add("HCT-116 positive");
		names.add("TC-71 negative");
		names.add("TC-71 positive");
		names.add("Kelly negative");
		names.add("Kelly positive");
		names.add("Generic positive control");
		
		// Create instance and print some data
		BCAData plate = new BCAData(file, 15, ReplicateNum.DUPLICATE, names);
		plate.printRawData();
		plate.printCreationDate();
		plate.printStdAvgs();
		
		System.out.println(plate.getSlope());
		System.out.println();
		
		plate.printSampleAvgs();
		plate.printConcentrations();
		plate.printLoadVolumes();
		
		System.out.println();
		System.out.println(plate.getCreationDate());
		plate.printCreationDate();
		
		// Kind of the format that will be output to file
		plate.printLoadVolumesFormatted();
		
		file.close();
	}
}
