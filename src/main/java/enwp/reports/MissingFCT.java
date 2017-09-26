package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;

/**
 * Looks for files without a license tag.
 * 
 * @author Fastily
 *
 */
public class MissingFCT
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		String rPage = String.format("User:%s/Possibly missing license", wiki.whoami());

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, "report8");
		l.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, "report5"));
		l.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, "report6"));
		
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file"));

		for (String s : wiki.getLinksOnPage(rPage + "/Ignore"))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		wiki.edit(rPage, BotUtils.listify(BotUtils.updatedAt, MQuery.exists(wiki, true, new ArrayList<>(l)), true), "Updating report");
	}
}