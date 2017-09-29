package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;

/**
 * Lists enwp files with a duplicate on Commons.
 * 
 * @author Fastily
 *
 */
public class DupeOnCom
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		String rPage = "Wikipedia:Database reports/Local files with a duplicate on Commons";

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, "report1");

		for (String s : wiki.getLinksOnPage(rPage + "/Ignore", NS.CATEGORY))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		wiki.edit(rPage, BotUtils.listify(BotUtils.updatedAt, MQuery.exists(wiki, true, new ArrayList<>(l)), true), "Updating report");
	}
}