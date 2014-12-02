package Dropbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	}
	
	public static void getFileContent(){
		StringBuilder sb = new StringBuilder();
		try {
			
			FileReader fr = new FileReader("src/Dropbox/Server/file3");
			BufferedReader reader = new BufferedReader(fr);
			String str;
			while((str = reader.readLine()) != null){

				sb.append("\n");
				sb.append(str);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(sb.toString());
	}
	
	public static void createFile(){
		String text = "Hello world";
        try {
          File file = new File("example.txt");
          BufferedWriter output = new BufferedWriter(new FileWriter(file));
          output.write(text+"\n");
          output.close();
        } catch ( IOException e ) {
           e.printStackTrace();
        }
	}

	public static void main(String[] args) {
		createFile();
	}

}
