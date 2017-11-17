package enwp.reports;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.Triple;
import util.BotUtils;

/**
 * Reports files in daily deletion categories which were untagged since the previous run.
 * 
 * @author Fastily
 *
 */
public class UntaggedDD
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 * @throws Throwable On IO error
	 */
	public static void main(String[] args) throws Throwable
	{
		Wiki wiki = BotUtils.getFastilyBot();
		String rPage = "Wikipedia:Database reports/Recently Untagged Files for Dated Deletion";
		int maxOldReports = 49;
		Path ddFL = Paths.get("WPDDFiles.txt");

		HashSet<String> l = FL.toSet(wiki.getLinksOnPage(rPage + "/Config", NS.CATEGORY).stream().flatMap(s -> wiki.getCategoryMembers(s, NS.FILE).stream()));
		
		if (!Files.exists(ddFL))
		{
			BotUtils.writeStringsToFile(ddFL, l);
			return;
		}

		HashSet<String> cacheList = FL.toSet(Files.lines(ddFL));
		cacheList.removeAll(l);

		String text = wiki.getPageText(rPage);
		ArrayList<Triple<Integer, String, Integer>> sections = wiki.getSectionHeaders(rPage);
		if (sections.size() > maxOldReports)
			text = text.substring(0, sections.get(maxOldReports).z);

		wiki.edit(rPage, BotUtils.listify("== ~~~~~ ==\n", MQuery.exists(wiki, true, cacheList), true) + text,
				"Updating report");

		BotUtils.writeStringsToFile(ddFL, l);
	}
}