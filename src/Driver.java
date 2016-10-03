
/**
 * Driver class for project1 and all future projects
 * Project 1:
 * 				Checks for flags "-dir" and "-index" as well as the parameter for those flags
 * 				If the flags exist, an invertedIndex is created through the InvertedIndexBuilder,
 * 				and if "-index" flag exists, a JSON file will be created, and output based on the 
 * 				flags respective value (or default value) if there is no respective value.
 */

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Driver
{

	public static void main(String[] args)
	{
		InvertedIndexBuilder indexBuilder = new InvertedIndexBuilder();
		InvertedIndex index = new InvertedIndex();
		ArgumentParser parser = new ArgumentParser();
		String inputFile = null;
		String outputFile = null;
		Path input = null;
		Path output = null;
		
		if(args != null)
		{
			parser.parseArguments(args);
			if(parser.hasFlag("-dir"))
			{
				if(parser.getValue("-dir") != null)
				{
					inputFile = parser.getValue("-dir");
					input = Paths.get(inputFile);
				}
				else
				{
					System.out.println("No directory listed.");
					return;
				}
			}
			
			if(parser.hasFlag("-index"))
			{
				if(parser.getValue("-index", "index.json") != null)
				{
					outputFile = parser.getValue("-index", "index.json");
					output = Paths.get(outputFile);
				}
				else
				{
					System.out.println("No directory listed.");
					return;
				}
			}
			
			if(input!=null)
			{
				try
				{
					indexBuilder.directoryTraversal(input, index);
				}
				catch(IOException e)
				{
					System.out.println("Error reading file: " + input);
				}
			}
			if(output != null)
			{
				try
				{
					index.writeJSON(output);
				}
				catch(IOException e)
				{
					System.out.println("Error writing JSON to file: " + output);
				}
			}
		}
		else
		{
			System.out.println("No, or not enough, arguments provided");
			return;
		}
	}

}