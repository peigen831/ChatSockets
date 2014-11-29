package src.Dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.text.SimpleDateFormat;

public class Test {
	
	private static void loadFile(){
		BufferedReader br = null;
		
		try {
			File file = new File("src/File1");
			System.out.println("File1 before format: " + file.lastModified());
			SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss	");
			System.out.println("File1 after format: "+ format.format(file.lastModified()));
			
			File file2 = new File("src/File2");

			System.out.println("File2 before format: " + file2.lastModified());
			System.out.println("File2 after format: "+ format.format(file2.lastModified()));
			
			//br = new BufferedReader(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void checkFiles(){
		File folder = new File("src/Dropbox/Client1File");
		File[] fileList = folder.listFiles();
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < fileList.length; i++)
		{
			sb.append(fileList[i].lastModified() + " " + fileList[i].getName() + "\n");
		}
		System.out.println(sb.toString());
		//get all file list with date
		//send file name with date per line
	}

	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append("asdasdad\n");
		sb.append("adsad");
		System.out.println(sb.toString());
		
		
	}

}
