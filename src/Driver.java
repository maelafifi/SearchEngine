import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Driver
{
	private static FileReader readFiles = new FileReader();
	private static WordDatabase database = new WordDatabase();
	private static FileWriter writeFile = new FileWriter();
	
	public static void main(String[] args)
	{
		ArrayList<String> arguments = new ArrayList<String>();
		String inputFile = null;
		String outputFile = null;
		Path input = null;
		Path output = null;
		for(int i = 0; i < args.length; i++)
		{
			arguments.add(args[i]);
		}
		
		if(arguments.contains("-dir"))
		{
			int j = arguments.indexOf("-dir");
			j++;
			if(j >= arguments.size())
			{
				System.out.println("incorrect directory");
				return;
			}
			
			inputFile = arguments.get(j);
			input = Paths.get(inputFile);
		}
		else
		{
			System.out.println("No input file selected; goodbye!");
			return;
		}
		
		if(arguments.contains("-index"))
		{
			int j = arguments.indexOf("-index");
			j++;
			if(j >= arguments.size())
			{
				outputFile = "index.json";
			}
			else if(arguments.get(j).equalsIgnoreCase("-dir"))
			{
				outputFile = "index.json";
			}
			else
			{
				outputFile = arguments.get(j);
			}
			output = Paths.get(outputFile);
		}
		
		try
		{
			System.out.println(input);
			readFiles.addWordsFromFile(input);
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(output != null)
		{
			writeFile.writeToFile(output, database.getDatabase());
			System.out.println(output);
		}
		
		
	}
}
	