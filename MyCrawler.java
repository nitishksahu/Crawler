import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {

	class DataDetails {
		private String url;
		private Integer size;
		private Integer noOfOutLinks;
		private String contentType;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Integer getSize() {
			return size;
		}

		public void setSize(Integer size) {
			this.size = size;
		}

		public Integer getNoOfOutLinks() {
			return noOfOutLinks;
		}

		public void setNoOfOutLinks(Integer noOfOutLinks) {
			this.noOfOutLinks = noOfOutLinks;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

	}

	public static int counter = 0;

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(html|htm|pdf|doc|docx|gif|png|jpg|jpeg|tiff))$");

	public static Map<String, Integer> urlAndStatusMap = new HashMap<String, Integer>();
	public static Map<String, DataDetails> detailMap = new HashMap<String, DataDetails>();
	public static int sucessStatus = 0;
	public static int failedStatus = 0;
	public static List<Integer> urlSize = new ArrayList<Integer>();
	public static List<Integer> outlinkList = new ArrayList<Integer>();
	public static List<String> contentEncodingList = new ArrayList<String>();
	public static List<String> outgoingUrlList = new ArrayList<String>();
	public static List<String> outgoingUrlStatuslList = new ArrayList<String>();

	public static Map<String, String> urlMap = new HashMap<String, String>();
	public static Map<Integer, Integer> statusMap = new HashMap<Integer, Integer>();
	public static Map<String, Integer> fileSizeMap = new HashMap<String, Integer>();
	public static Map<String, Integer> contentTypeMap = new HashMap<String, Integer>();

	public static synchronized void increamentCounter() {
		counter++;
	}

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		increamentCounter();
		String href = url.getURL().toLowerCase();

		// outgoingUrlList.add(url.toString());
		if (href.startsWith("https://www.nytimes.com/") || href.startsWith("http://www.nytimes.com/")) {
			outgoingUrlStatuslList.add("OK");
			if (urlMap.containsKey("OK"))
				urlMap.put("OK", Integer.toString(Integer.parseInt(urlMap.get("OK")) + 1));
			else
				urlMap.put("OK", Integer.toString(1));
		} else {
			outgoingUrlStatuslList.add("N_OK");
			if (urlMap.containsKey("N_OK"))
				urlMap.put("N_OK", Integer.toString(Integer.parseInt(urlMap.get("N_OK")) + 1));
			else
				urlMap.put("N_OK", Integer.toString(1));
		}
		return !FILTERS.matcher(href).matches()
				&& (href.startsWith("https://www.nytimes.com/") || href.startsWith("http://www.nytimes.com/"));
	}

	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();

		int statusCode = page.getStatusCode();
		urlAndStatusMap.put(url, statusCode);

		if (urlMap.containsKey("counter"))
			urlMap.put("counter", Integer.toString(Integer.parseInt(urlMap.get("counter")) + 1));
		else
			urlMap.put("counter", Integer.toString(1));
		DataDetails dataDetails = new DataDetails();
		dataDetails.setUrl(url);
		String contentType = page.getContentType().split(";")[0];

		dataDetails.setContentType(contentType);
		HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
		dataDetails.setNoOfOutLinks(htmlParseData.getOutgoingUrls().size());
		String html = htmlParseData.getHtml();
		dataDetails.setSize(html.length());

		detailMap.put(url, dataDetails);

		contentEncodingList.add(contentType);
		if (contentTypeMap.containsKey(contentType))
			contentTypeMap.put(contentType, contentTypeMap.get(contentType) + 1);
		else
			contentTypeMap.put(contentType, 1);

		if (statusCode >= 200 && statusCode <= 299) {
			sucessStatus++;
		} else {
			System.out.println(url);
			System.out.println(statusCode);
			System.exit(0);
			failedStatus++;
		}

		if (statusMap.containsKey(statusCode))
			statusMap.put(statusCode, statusMap.get(statusCode) + 1);
		else
			statusMap.put(statusCode, 1);

		if (html.length() < 1024) {
			if (fileSizeMap.containsKey("< 1KB"))
				fileSizeMap.put("< 1KB", fileSizeMap.get("< 1KB") + 1);
			else
				fileSizeMap.put("< 1KB", 1);
		} else if (html.length() >= 1024 && html.length() < (10240)) {
			if (fileSizeMap.containsKey("1KB ~ <10KB"))
				fileSizeMap.put("1KB ~ <10KB", fileSizeMap.get("1KB ~ <10KB") + 1);
			else
				fileSizeMap.put("1KB ~ <10KB", 1);
		} else if (html.length() >= (10240) && html.length() < (102400)) {
			if (fileSizeMap.containsKey("10KB ~ <100KB"))
				fileSizeMap.put("10KB ~ <100KB", fileSizeMap.get("10KB ~ <100KB") + 1);
			else
				fileSizeMap.put("10KB ~ <100KB", 1);
		} else if (html.length() >= (102400) && html.length() < (1024 * 1024)) {
			if (fileSizeMap.containsKey("100KB ~ <1MB"))
				fileSizeMap.put("100KB ~ <1MB", fileSizeMap.get("100KB ~ <1MB") + 1);
			else
				fileSizeMap.put("100KB ~ <1MB", 1);
		} else if (html.length() >= (1024 * 1024)) {
			if (fileSizeMap.containsKey(">= 1MB"))
				fileSizeMap.put(">= 1MB", fileSizeMap.get(">= 1MB") + 1);
			else
				fileSizeMap.put(">= 1MB", 1);
		}
	}

	public static void generate1stCSV() {
		System.out.println("******CSV CREATION STARTED******");
		try {
			PrintWriter pw = new PrintWriter(new File("fetch_NY_Times.csv"));
			StringBuilder builder = new StringBuilder();
			String columnNameList = "URL,HTTP STATUS";
			builder.append(columnNameList + "\n");

			for (String key : urlAndStatusMap.keySet()) {
				builder.append(key);
				builder.append(",");
				builder.append(urlAndStatusMap.get(key));
				builder.append('\n');
			}
			pw.write(builder.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static void generate2ndCSV() {
		System.out.println("******2nd CSV CREATION STARTED******");
		try {
			PrintWriter pw = new PrintWriter(new File("visit_NY_Times.csv"));
			StringBuilder builder = new StringBuilder();
			String columnNameList = "URL OF FILE,SIZE OF FILE, NO OF OUTLINKS FOUND, CONTENT-TYPE";
			builder.append(columnNameList + "\n");

			for (String key : detailMap.keySet()) {
				builder.append(key + ",");
				builder.append(detailMap.get(key).getSize() + ", ");
				builder.append(detailMap.get(key).getNoOfOutLinks() + ", ");
				builder.append(detailMap.get(key).getContentType() + ", ");
				builder.append('\n');

			}

			pw.write(builder.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void generate3rdCSV() {
		System.out.println("******3rd CSV CREATION STARTED******");
		try {
			PrintWriter pw = new PrintWriter(new File("urls_NY_Times.csv"));
			StringBuilder builder = new StringBuilder();
			String columnNameList = "URL,INDICATOR";
			builder.append(columnNameList + "\n");

			for (int i = 0; i < outgoingUrlList.size(); i++) {
				builder.append(outgoingUrlList.get(i) + ",");
				builder.append(outgoingUrlStatuslList.get(i) + ", ");
				builder.append('\n');
			}
			pw.write(builder.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void statisticGenerator() {
		try {
			System.out.println("Text File generation");
			PrintWriter writer = new PrintWriter("CrawlReport.txt");
			StringBuilder builder = new StringBuilder();
			writer.append("Name: Nitish Kumar Sahu");
			writer.append(System.lineSeparator());
			writer.append("USC ID: 9147143555");
			writer.append(System.lineSeparator());
			writer.append("News site crawled: nytimes.com");
			writer.append(System.lineSeparator());
			writer.append("Fetch Statistics:");
			writer.append(System.lineSeparator());
			writer.append("==================================================");
			writer.append(System.lineSeparator());
			writer.append("fetches attempted: " + urlMap.get("counter"));
			writer.append(System.lineSeparator());
			writer.append("fetches succeeded: " + sucessStatus);
			writer.append(System.lineSeparator());

			writer.append("fetches failed or aborted: " + failedStatus);
			writer.append(System.lineSeparator());
			writer.append(System.lineSeparator());

			writer.append("Outgoing URLs:");
			writer.append(System.lineSeparator());
			writer.append("==================================================");
			writer.append(System.lineSeparator());
			writer.append("Total URLs extracted: " + counter);
			writer.append(System.lineSeparator());
			int nOKUrl;
			if (urlMap.get("N_OK") == null)
				nOKUrl = 0;
			else
				nOKUrl = Integer.parseInt(urlMap.get("N_OK"));
			writer.append("# unique URLs extracted: " + (Integer.parseInt(urlMap.get("OK")) + nOKUrl));
			writer.append(System.lineSeparator());
			writer.append("# unique URLs within News Site: " + Integer.parseInt(urlMap.get("OK")));
			writer.append(System.lineSeparator());
			writer.append("# unique URLs outside News Site: " + nOKUrl);
			writer.append(System.lineSeparator());
			writer.append(System.lineSeparator());
			writer.append("Status Code:");
			writer.append(System.lineSeparator());
			writer.append("==================================================");
			writer.append(System.lineSeparator());
			Map<Integer, String> statusCodeMap = new HashMap<Integer, String>();
			statusCodeMap.put(200, "OK");
			statusCodeMap.put(301, "Moved Permanently");
			statusCodeMap.put(302, "Moved Temporarily");
			statusCodeMap.put(400, "Bad request");
			statusCodeMap.put(401, "Unauthorized");
			statusCodeMap.put(402, "Payment Required");
			statusCodeMap.put(403, "Forbidden");
			statusCodeMap.put(404, "Not Found");
			statusCodeMap.put(500, "Internal Error");

			for (Integer key : statusMap.keySet()) {
				System.out.println(key);
				writer.append(key + " " + statusCodeMap.get(key) + " : " + statusMap.get(key));
				writer.append(System.lineSeparator());
			}

			writer.append(System.lineSeparator());
			writer.append("File Sizes:");
			writer.append(System.lineSeparator());
			writer.append("==================================================");
			writer.append(System.lineSeparator());
			writer.append("< 1KB: " + fileSizeMap.get("< 1KB"));
			writer.append(System.lineSeparator());
			writer.append("1KB ~ <10KB: " + fileSizeMap.get("1KB ~ <10KB"));
			writer.append(System.lineSeparator());
			writer.append("10KB ~ <100KB: " + fileSizeMap.get("10KB ~ <100KB"));
			writer.append(System.lineSeparator());
			writer.append("100KB ~ <1MB: " + fileSizeMap.get("100KB ~ <1MB"));
			writer.append(System.lineSeparator());
			int mbFileCount;
			if (fileSizeMap.get(">1MB") == null)
				mbFileCount = 0;
			else
				mbFileCount = fileSizeMap.get(">1MB");
			writer.append(">1MB: " + mbFileCount);
			writer.append(System.lineSeparator());
			writer.append(System.lineSeparator());
			writer.append("Content Types:");
			writer.append(System.lineSeparator());
			writer.append("==================================================");
			writer.append(System.lineSeparator());
			for (String key : contentTypeMap.keySet()) {
				writer.append(key + ": " + contentTypeMap.get(key));
				writer.append(System.lineSeparator());
			}
			writer.append(System.lineSeparator());
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
