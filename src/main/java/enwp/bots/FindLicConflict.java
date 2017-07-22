package enwp.bots;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;

/**
 * Finds enwp files which are flagged as both free and non-free.
 * 
 * @author Fastily
 *
 */
public final class FindLicConflict
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki = BotUtils.getFastilyBot();

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args) throws Throwable
	{
		HashSet<String> fl = BotUtils.fetchLabsReportListAsFiles(wiki, "report2");
		
		for(String s : wiki.getLinksOnPage(String.format("User:%s/Task5/Ignore", wiki.whoami())))
			fl.removeAll(wiki.whatTranscludesHere(s, NS.FILE));

		for (String s : MQuery.exists(wiki, true, new ArrayList<>(fl)))
			wiki.addText(s, "{{Wrong-license}}\n", "BOT: Noting possible conflict in copyright status", true);
	}
}