import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Driver
{

	public static void main(String[] args)
	{
		FileReader readFiles = new FileReader();
		WordDatabase database = new WordDatabase();
		FileWriter writeFile = new FileWriter();
		ArrayList<String> arguments = new ArrayList<String>();
		String inputFile = null;
		String outputFile = null;
		Path input = null;
		Path output = null;
		
		for(int i = 0; i < args.length; i++)
		{
			arguments.add(args[i]);
		}

		if(arguments.contains("-dir")) // checks if there is a "-dir" flag
		{
			int j = arguments.indexOf("-dir");
			j++;
			if(j >= arguments.size()) // checks if there is no argument after flag; returns.
			{
				System.out.println("incorrect directory");
				return;
			}

			inputFile = arguments.get(j);
			input = Paths.get(inputFile);
		}
		
		else // returns if no "-dir" flag exists
		{
			System.out.println("No input file selected; goodbye!");
			return;
		}

		if(arguments.contains("-index")) // checks if there is a "-index" flag
		{
			int j = arguments.indexOf("-index");
			j++;
			
			if(j >= arguments.size()) // checks if there is an argument after flag -- if not, default output
			{
				outputFile = "index.json";
			}
			
			// checks if the argument after flag is another flag
			// if it is, default output file
			else if(arguments.get(j).equalsIgnoreCase("-dir"))
			{
				outputFile = "index.json";
			}
			
			else // if none of those are the case, the following argument is set to outputFile
			{
				outputFile = arguments.get(j);
			}
			output = Paths.get(outputFile);
		}

		try
		{
			readFiles.addWordsFromFile(input); // see addWordsFromFile() in WordDatabase.java
		}
		
		catch(IOException e)
		{
			System.out.println("Problem reading or writing file");
			return;
		}

		if(output != null) // if there is an output file, write the database.
		{
			writeFile.writeToFile(output, database.getDatabase()); // see writeToFile() in FileWriter.java
		}

	}

}
