package enwp.bots;

import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wpkit.WTP;
import fastily.wpkit.util.Toolbox;

/**
 * Removes {{Orphan image}} from freely licensed files which contain file links in the main space.
 * 
 * @author Fastily
 *
 */
public final class UnflagOI
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * A regex matching the Orphan image template
	 */
	private static final String oiRegex = WTP.orphan.getRegex(wiki);

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, unused.
	 */
	public static void main(String[] args)
	{
		HashSet<String> oL = Toolbox.fetchLabsReportListAsFiles(wiki, "report3");
		oL.removeAll(Toolbox.fetchLabsReportListAsFiles(wiki, "report4"));
		
		HashSet<String> l = WTP.orphan.getTransclusionSet(wiki, NS.FILE);
		l.removeAll(oL);
		l.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));

		
		System.out.println(l.size());
		
		for(String s : l)
			System.out.println(s);
		
		
//		MQuery.fileUsage(wiki, wiki.whatTranscludesHere(WTP.orphan.title, NS.FILE)).forEach((k, v) -> {
//			if(!wiki.filterByNS(v, NS.MAIN).isEmpty())
//				wiki.replaceText(k, oiRegex, "BOT: File contains inbound links");
//		});
	}
}