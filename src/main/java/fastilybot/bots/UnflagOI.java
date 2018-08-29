package fastilybot.bots;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wptoolbox.BotUtils;
import fastily.wptoolbox.WTP;

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
		HashSet<String> oL = BotUtils.fetchLabsReportAsFiles(wiki, 3);
		oL.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, 4));
		
		// Get all files tagged with Orphan image which are not orphans
		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, 9);
		l.removeAll(oL);
		l.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));

		// Restrict working set to Free files only
		l.retainAll(BotUtils.fetchLabsReportAsFiles(wiki, 6));
		
		String oiRegex = WTP.orphan.getRegex(wiki);
		for(String s : l)
			wiki.replaceText(s, oiRegex, "BOT: File contains inbound links");
	}
}