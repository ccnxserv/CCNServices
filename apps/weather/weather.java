import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.xpath.XPathConstants;

public class weather {
	private static final Logger log = Logger.getLogger(weather.class.getName());
	public static final String SERVICE_NAME = "weather";

	public String run(String filePrefix, String file) {
		
		Map<String, String> env = System.getenv();
		String path = env.get("VIDEODIR");

		log.info("Loading video directory path = " + path);
		overlayInfo o = new overlayInfo();
		String weather = o.lookupWeather();

		String qt = "\"";
		String font = "serif";
		String fontsize = "16px";
		String wrap = "noWrap";
		String bgcolor = "yellow";
		String textcolor = "black";
		String tr_top = "260"; /* tr = textregion */
		String tr_width = "360";
		String tr_height = "20";
		String tr_left = "10";
		String tr_textmode = "crawl";
		String tr_textrate = "20px";
		String textregion_1 = "weather";
		String style = "mystyle";

		String weatherSmil = "<smil>\n" + "<head>\n" + "<textStyling>\n" +

		"<textStyle xml:id=" + qt + style + qt + " textFontFamily=" + qt + font
				+ qt + " textFontSize=" + qt + fontsize + qt
				+ " textWrapOption=" + qt + wrap + qt + " textColor=" + qt
				+ textcolor + qt + " textBackgroundColor=" + qt + bgcolor + qt
				+ "/>\n" +

				"</textStyling>\n\n" + "<layout>\n" + "<region id=" + qt
				+ textregion_1 + qt + " top=" + qt + tr_top + qt + " width="
				+ qt + tr_width + qt + " left=" + qt + tr_left + qt
				+ " height=" + qt + tr_height + qt + " textRate=" + qt
				+ tr_textrate + qt + " textStyle=" + qt + style + qt + "/>\n" +

				"</layout>\n\n" + "</head>\n\n" +

				"<body><par>\n" + "<video src=\"" + path + "/" + file + "\" />\n"
				+ "<smilText region=" + qt + textregion_1 + qt + ">\n"
				+ weather + "</smilText>\n" +

				"</par>" + "</body>\n" + "</smil>";

		String outFilePath = filePrefix + "/" + file + "%2B" + SERVICE_NAME;
		String outFile = file + "%2B" + SERVICE_NAME;
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
			out.write(weatherSmil);
			out.flush();
			out.close();
		} catch (IOException e) {
			log.warning("Cannot write to output file for service.." + SERVICE_NAME);
			return null;
		}
		return outFile;
	}
}
