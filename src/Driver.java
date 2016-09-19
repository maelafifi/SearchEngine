import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

public class Driver {

    public static void main(String[] args) 
    {
    	private static WordDatabase all = new WordDatabase();
    	TreeMap<String, TreeMap<String, TreeSet<Integer>>> write = all.copyForFileWriter();
        System.out.println(Arrays.toString(args));
    }
}
