import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class next {
	private static final Logger log = Logger.getLogger(next.class.getName());
	public static final String SERVICE_NAME = "next";

	public String run(String filePrefix, String file) {
		String inFilePath = filePrefix + "/" + file;
		String outFilePath = filePrefix + "/" + file + "%2B" + SERVICE_NAME;
		String outFile = file + "%2B" + SERVICE_NAME;
		log.info("Saving output file .." + outFile);
		try {
			File in = new File(inFilePath);
			if(!in.exists()){
				log.warning("File does not exit.. " + inFilePath);
				return null;
			}
			BufferedReader buf_in = new BufferedReader(new FileReader(in));
			BufferedWriter out = new BufferedWriter(new FileWriter(outFilePath));
			String line;
			while(( line = buf_in.readLine()) != null){
				out.write(line);
			}
			out.write("Added by next service");
			out.close();
		} catch (IOException e) {
			log.warning("Cannot write to output file for service.." + SERVICE_NAME);
			return null;
		}
		return outFile;
	}
}
