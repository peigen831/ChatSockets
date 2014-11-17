package Helper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class AverageComputer {
	
	public static void printAverageTimeA(String path){
		BufferedReader br = null;
		
		try {
			FileReader reader = new FileReader("src/Helper/" + path);
			br = new BufferedReader(reader);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
	    try 
	    {
	    	String value;
	    	int nItem = 0;
	    	int sum = 0;
	    	
	        while ((value = br.readLine()) != null) 
	        {
	        	sum += Integer.parseInt(value);
	        	nItem++;
	        }
	        
	        System.out.println(nItem + ": " + sum/nItem);

			br.close();
	        
	    }catch(Exception e){
	    	System.out.println("IO Error");
	    }
	}
	
	public static void printAverageTimeB(String path){
		BufferedReader br = null;
		
		try {
			FileReader reader = new FileReader("src/Helper/" + path);
			br = new BufferedReader(reader);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
	    try 
	    {
	    	String value;
	    	int nItem = 0;
	    	int sum = 0;
	    	
	        while ((value = br.readLine()) != null) 
	        {
	        	value = value.split(": ")[1];
	        	sum += Integer.parseInt(value);
	        	nItem++;
	        }
	        
	        System.out.println(nItem + ": " + sum/nItem);

			br.close();
	        
	    }catch(Exception e){
	    	System.out.println("IO Error");
	    }
	}
	

	
	public static void main(String args[])
	{
		printAverageTimeB("improved/10.txt");
		printAverageTimeB("improved/20.txt");
		printAverageTimeB("improved/30.txt");
		printAverageTimeB("improved/40.txt");
		printAverageTimeB("improved/50.txt");
		printAverageTimeB("improved/100.txt");
		printAverageTimeB("improved/200.txt");
		printAverageTimeB("improved/400.txt");
		printAverageTimeB("improved/800.txt");
		printAverageTimeB("improved/1600.txt");
		printAverageTimeB("improved/3200.txt");
		printAverageTimeB("improved/6400.txt");
		
	}
}

