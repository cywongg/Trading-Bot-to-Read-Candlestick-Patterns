import java.io.*; 
import java.util.Scanner;
import java.util.*;
import java.text.*;
import java.util.Date;

public class tradingbot 
{
	public static void main(String[] args)throws IOException
    {
		//define format of the input String date
		SimpleDateFormat abc= new SimpleDateFormat("dd/MM/yyyy"); 
		
		//Dates
		String startDate, endDate;
		startDate = args[0];
		endDate = args[1];
      
		/* Check start date and end date format */
		boolean ValidSFormat = checkValidformat(startDate);
		boolean ValidEFormat = checkValidformat(endDate);
	
		while(ValidSFormat == false)
		{
			System.out.println("Please enter start date again");
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Start Date: ");
			startDate = keyboard.nextLine();
			ValidSFormat = checkValidformat(startDate);
		}
	
		while(ValidEFormat == false)
		{
			System.out.println("Please enter end date again");
			Scanner keyboard = new Scanner(System.in);
			System.out.println("End Date: ");
			endDate = keyboard.nextLine();
			ValidEFormat = checkValidformat(endDate);
		}	
		
		//import file, file path
		Scanner inputFile = checkFile("C:\\data\\0005.HK.csv");
		
		//Build up ArrayList to store data from file
		ArrayList<Date>Date = new ArrayList<Date>();
		ArrayList<Double>Open = new ArrayList<Double>();
		ArrayList<Double>High = new ArrayList<Double>();
		ArrayList<Double>Low = new ArrayList<Double>();
		ArrayList<Double>Close = new ArrayList<Double>();
		
		// Read the first line
		String line = inputFile.nextLine();
		
		// Add data to the 1st arraylists from the file
		while(inputFile.hasNext())
		{
			String[] data = inputFile.nextLine().split(",");
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			try 
			{
				Date.add(sdf.parse(data[0]));
			}
			catch(ParseException e)
			{
				System.out.println("Invalid format in the file.");
			}
			Open.add(getDoubleFromData(data[1]));             
            High.add(getDoubleFromData(data[2]));
            Low.add(getDoubleFromData(data[3]));    
            Close.add(getDoubleFromData(data[4])); 
		}
		inputFile.close();
		
		
		/* Check date range */
		boolean validdaterange = isValiddaterange(startDate,endDate,Date); 

		while(validdaterange == false)
		{
			System.out.println("Please enter start date and end date again");
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Start Date: ");
			startDate = keyboard.nextLine();
			System.out.println("End Date: ");
			endDate = keyboard.nextLine();
			validdaterange = isValiddaterange(startDate,endDate,Date);
		}
		
		/* Define Startindex and Endindex */

		int Startindex,Endindex;
		Startindex = takeSIndex(Date,startDate);
		Endindex = takeEIndex(Date,endDate);


		/* Check Index out of question
		   When the start index is greater than the end index,
		   it means dates entered in start date and end date are 
		   not contained in the file.
		   That means no trading record (holiday) within that period.
		*/

		while (Startindex > Endindex)
		{
			System.out.println("There are no records in these dates.");
			System.out.println("Please enter again the start date and end date");
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Start Date:");
			startDate = keyboard.nextLine();
			System.out.println("End Date:");
			endDate = keyboard.nextLine();
			Startindex = takeSIndex(Date,startDate);
			Endindex = takeEIndex(Date,endDate);
		}
		
		/* Build ArrayList to store data in date range */
		/* Data are copied from the 1st ArrayList to 2nd ArrayList */

		ArrayList<Date>Date2 = new ArrayList<Date>();
		ArrayList<Double>Open2 = new ArrayList<Double>();
		ArrayList<Double>High2 = new ArrayList<Double>();
		ArrayList<Double>Low2 = new ArrayList<Double>();
		ArrayList<Double>Close2 = new ArrayList<Double>();
		
		for(int i = Startindex; i < Endindex + 1; i++)
		{
			Date2.add(Date.get(i));
			Open2.add(Open.get(i)); 
			High2.add(High.get(i));
			Low2.add(Low.get(i));
			Close2.add(Close.get(i));
		}
				
		//Select analyse function
		//display Menu
		
		int option;
		option = displayMenu();			/* call displayMenu method */
		
		  //enter different analysis
			switch (option)
			{
				case 1:
					System.out.println("Option: 1");
					hammer(Open2,Close2,High2,Low2,Date2);
					break; 
				case 2:
					System.out.println("Option: 2");
					morningStar(Open2,Close2,High2,Low2,Date2);
					break;
				case 3:
					System.out.println("Option: 3");
					sma3(Open2,Close2,High2,Low2,Date2);
					break;
			}	
	}	
	
		/* Hammer Characteristics
		1. 1 Day Pattern, white candle
		2. High of the day = Close of the day  
		3. Close of the day > Open of the day 
		4. Lower shadow size is at least 2 times the body 
		5. Close Day1 < Close Day2 (Black Candle)
		*/
		
		//Hammer
		public static void hammer(ArrayList<Double>Open2,ArrayList<Double>Close2,ArrayList<Double>High2,ArrayList<Double>Low2,ArrayList<Date>Date2)
		{
			int count = 0;
				
				for(int i = 0; i < Open2.size()- 1; i++)
				{
					double body, lshadow, topshadow, lowshadowreq;
					body = Close2.get(i) - Open2.get(i); // Body = close price - opening price
					lshadow = Open2.get(i) - Low2.get(i);
					topshadow = High2.get(i) - Close2.get(i);	
					lowshadowreq = (body*2);	 /* Lower shadow = open price - low price*/
					
					//Check to make sure that the close is greater than the open
					//Check to make sure that the close of the following day is greater than the current day
					if (Close2.get(i) > Open2.get(i))
					{
						//Make sure that the lower shadow is twice as long as the body and that the top shadow, if any, is shorter than the body
						if(lowshadowreq <= lshadow && topshadow < body)
						{
							printDate(Date2.get(i));
							count++;
						}
					}
				}
					if(count == 0)
					{
						System.out.println("No  Hammer pattern is found. ");
					}
					else if(count != 0)
					{
						System.out.println("Hammer patterns are found in the dates above.");
						System.out.println("There are " + count + " patterns found.");
					}
	
				
		}
	
		
			
			/* Morning star Characteristics
			1. 3rd day close > X where X is 1st day close + (1st day open - 1st day close) / 2
			2. 3rd day is a white candle, and
			3. 1st day close > 2nd day close and 1st day close > 2nd day open, and
			4. 1st day is a black candle and abs(2nd day open - 2nd day close) > 0
			*/
			
		//Morning star
		public static void morningStar(ArrayList<Double>Open2,ArrayList<Double>Close2,ArrayList<Double>High2,ArrayList<Double>Low2,ArrayList<Date>Date2)
		{
			int count = 0;
			for(int i = 0; i < Open2.size() - 3; i++)
			{
				if(Close2.get(i + 2) > (Close2.get(i) +(Open2.get(i) - Close2.get(i)))/2) /* 3rd day close > ( 1st day close + (1st day open - 1st day close) / 2 ) */	
				{
					if((Close2.get(i + 2) - Open2.get(i + 2)) >0 )  /*3rd day is a white candle */
					{
						if((Close2.get(i)) > Close2.get(i+1) && Close2.get(i) > Open2.get(i+1))  /* 1st day close > 2nd day close and 1st day close > 2nd day open*/ 
						{
							if((Open2.get(i) - Close2.get(i) > 0) && ((Open2.get(i+1)-Close2.get(i+1)) < 0 ))  /* 1st day is a black candle and (2nd day open - 2nd day close) > 0 */
							{
								if(Open2.get(i+2) > Open2.get(i+1) && Close2.get(i+2) > Close2.get(i+1)){ //Make sure that the open on day 3 is greater than day 2, and that the close on day 3 is greater than that on day 2
									printDate(Date2.get(i + 2));
									count++;
								}
							}
						}
					}
				}
			}
				//Check if morning star patterns were found 
				if (count == 0) 
				{	
					//Print that nothing was found
					 System.out.println("No morning stars pattern is found.");
				}
				else if (count != 0)
				{
				//Print results
				System.out.println("The above date is the last day of morning stars pattern.");
				System.out.println("There are " + count + " patterns found.");
				}
		}
			
		//SMA-3
		public static void sma3(ArrayList<Double>Open2,ArrayList<Double>Close2,ArrayList<Double>High2,ArrayList<Double>Low2,ArrayList<Date>Date2)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			DecimalFormat df = new DecimalFormat("#.000000");
			
			double totalPrice = 0.0;
			double averagePrice = 0.0;
			double previousAveragePrice = 0.0;
			int uptrendCount = 0;
			int downtrendCount = 0;
			
			for(int i = 0; i < Open2.size()-2; i++)
			{
				//Calculate average price
				totalPrice = Close2.get(i) + Close2.get(i+1) + Close2.get(i+2);
				averagePrice = totalPrice/3;

				if (previousAveragePrice == 0.0){
					System.out.println(sdf.format(Date2.get(i+2)) +  "   SMA-3: " + df.format(averagePrice) +  " Uptrend");
					uptrendCount++;
				}
				
				else if (previousAveragePrice < averagePrice) {
					System.out.println(sdf.format(Date2.get(i+2)) +  "   SMA-3: " + df.format(averagePrice) +  " Uptrend");
					uptrendCount++;
				}
				
				else if (previousAveragePrice > averagePrice) {
					System.out.println(sdf.format(Date2.get(i+2)) +  "   SMA-3: " + df.format(averagePrice) +  " Downtrend");
					downtrendCount++;
				}
				previousAveragePrice = averagePrice;
			}
			System.out.println("There are " + uptrendCount + " uptrend(s) found.");
			System.out.println("There are " + downtrendCount + " downtrend(s) found.");
		}
			
				
		/* print date of the pattern found */ 
		public static String printDate(Date Date)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String printDate = sdf.format(Date);
			System.out.println("Pattern found: " + printDate);
			
			return printDate;
		}
/*
*********************************************************************************************************************************************
*********************************************************************************************************************************************
*********************************************************************************************************************************************
*********************************************************************************************************************************************
*********************************************************************************************************************************************
*********************************************************************************************************************************************
*/

		/* Check the start date and end date format */
		public static boolean checkValidformat(String date)
		{
			
			Date Date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			try
			{
				Date = sdf.parse(date);
			}
			catch(ParseException e)
			{
				System.out.println("Not valid format for date " + date);
				return false;
			}
			
			return true;
		}

		/* Check date range of the file to the user's input date range */
		public static boolean isValiddaterange(String startDate, String Enddate, ArrayList<Date> date)
		{
			Date Dates = new Date();
			Date Datee = new Date();
			Date firstdate = date.get(0);
			Date finaldate = date.get(date.size() - 1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			try
			{
				Dates = sdf.parse(startDate);
				Datee = sdf.parse(Enddate);
			}
			
			catch(ParseException e)
			{
			}
			
			if (Dates.after(Datee))
			{
				System.out.println("Your start date is greater than your end date");
				
				return false;
			}
			
			else if(Dates.before(firstdate) || Datee.after(finaldate))
			{
				System.out.println("The date you enter is out of range");
				System.out.print("Please enter date between ");
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
				String stringFormatDate1 = sdf1.format(firstdate);
				String stringFormatDate2 = sdf1.format(finaldate);
				System.out.println(stringFormatDate1 +  " and " + stringFormatDate2);
			
				return false;
			}
			
			else 
			{
				return true;
			}
		}
		
		/* Get the start date index number */
		public static int takeSIndex(ArrayList<Date>dategetindex, String dateinput)
		{
			int dindex;
			Date datee = new Date();
			Date minDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			try
			{
				datee = sdf.parse(dateinput);
			}
			catch(ParseException e)
			{
			}
			
			
			if(!dategetindex.contains(datee))
			{
			
				for (int i = 0; datee.after(dategetindex.get(i)); i++)
				{
					minDate = dategetindex.get(i + 1);
				}
				dindex = dategetindex.indexOf(minDate);
			}
			
			else
			{
				dindex = dategetindex.indexOf(datee);
			}
			
			return dindex;
		}
		
		/* Get the end date index number */ 
		public static int takeEIndex(ArrayList<Date>dategetindex, String dateinput)
		{
			int dindex;
			Date datee = new Date();
			Date minDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			try
			{
				datee = sdf.parse(dateinput);
			}
			catch(ParseException e)
			{
			}
			
			if(!dategetindex.contains(datee))
			{
				
				for (int i = 0; datee.after(dategetindex.get(i)); i++)
				{
					minDate = dategetindex.get(i);
				}
				dindex = dategetindex.indexOf(minDate);
			}
			
			else
			{
				dindex = dategetindex.indexOf(datee);
			}
			
			return dindex;
		}
		
		/* display Menu */
		public static int displayMenu()
		{
		int number = 0; 
		Scanner numberinput = new Scanner (System.in);
		System.out.println("Please enter 1, 2, 3 to choose the pattern you want to test.");
		System.out.println("1. Hammer\n2. Morning Star\n3. SMA-3");
		
		//Check if the input is an integer
		try
		{
			number = numberinput.nextInt();
		}
		catch(InputMismatchException e)
		{
			System.out.print("The input is not valid. ");
		}
		
		//Check if the integer is between 1 to 3
		
		while(number != 1 && number != 2 && number != 3)
		{
			System.out.println("Please enter an integer from 1 to 3.");
			Scanner integer = new Scanner(System.in);
			number=integer.nextInt();
		}
		return number;
		}
		
		
		/* Check the file */
		public static Scanner checkFile(String file)
		{
			Scanner inputfile = null;
			
			try
			{
				File myFile = new File(file);
				inputfile = new Scanner(myFile);
			}
			catch(FileNotFoundException e)
			{
				System.out.println("File not found");
			}
			
			return inputfile;
		}
		
		//Handle missing data
		public static double getDoubleFromData(String data)
		{
			double doubleValue = 0.0;
			try
			{
				doubleValue = Double.parseDouble(data);
			}
			catch(NumberFormatException e)
			{              
				doubleValue = 0.0;
			}
			return doubleValue;
		}
}