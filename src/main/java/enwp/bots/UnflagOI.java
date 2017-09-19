package enwp.bots;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wpkit.WTP;
import util.BotUtils;

/**
 * Removes {{Orphan image}} from freely licensed files which contain file links in the main space.
 * 
 * @author Fastily
 *
 */
public final class UnflagOI
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, unused.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
				
		// Generate the set of files with no links of any sort
		HashSet<String> oL = BotUtils.fetchLabsReportAsFiles(wiki, "report3");
		oL.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, "report4"));
		
		// Get all files tagged with Orphan image which are not orphans
		HashSet<String> l = WTP.orphan.getTransclusionSet(wiki, NS.FILE);
		l.removeAll(oL);
		l.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));

		// Restrict working set to Free files only
		l.retainAll(BotUtils.fetchLabsReportAsFiles(wiki, "report6"));
		
		String oiRegex = WTP.orphan.getRegex(wiki);
		for(String s : l)
			wiki.replaceText(s, oiRegex, "BOT: File contains inbound links");
	}
}