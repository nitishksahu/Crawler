import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {

	public static void main(String[] args) {
		String crawlStorageFolder = "data/crawl";
		int numberOfCrawlers = 7;

		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		// config.setMaxDepthOfCrawling(16);
		// config.setMaxPagesToFetch(20000);

		config.setMaxDepthOfCrawling(4);
		config.setMaxPagesToFetch(300);
		config.setPolitenessDelay(200);
		config.setUserAgentString("Nitish's Crawler");
		config.setIncludeHttpsPages(true);
		config.setIncludeBinaryContentInCrawling(true);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		try {
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			controller.addSeed("http://www.nytimes.com/");
			controller.start(MyCrawler.class, numberOfCrawlers);
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		MyCrawler.generate1stCSV();
		MyCrawler.generate2ndCSV();
		MyCrawler.generate3rdCSV();
		MyCrawler.statisticGenerator();

	}

}
