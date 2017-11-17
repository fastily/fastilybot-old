package enwp.bots;

import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;
import util.WTP;

/**
 * Finds and flags orphaned free media files on enwp.
 * 
 * @author Fastily
 *
 */
public class FlagOI
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		
		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, "report3");
		
		for(String c : wiki.getLinksOnPage(String.format("User:%s/Task10/Ignore", wiki.whoami())))
			l.removeAll(wiki.getCategoryMembers(c, NS.FILE));
		
		l.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));
		
		l.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, "report4"));
		l.removeAll(MQuery.exists(wiki, false, l));
		
		for(String s : l)
			wiki.addText(s, "\n{{Orphan image}}", "BOT: Noting that file has no inbound file usage", false);
	}
}