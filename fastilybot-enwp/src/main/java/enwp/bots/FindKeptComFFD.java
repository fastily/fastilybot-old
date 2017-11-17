package enwp.bots;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BStrings;
import util.BotUtils;
import util.WTP;

/**
 * Finds local enwp files which were nominated for deletion on Commons but kept.
 * 
 * @author Fastily
 *
 */
public final class FindKeptComFFD
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		String nfdcRegex = WTP.nomDelOnCom.getRegex(wiki);
		String ncd = BStrings.ncdTemplateFor(wiki.whoami());
		
		HashSet<String> cffdl = new HashSet<>(BotUtils.getCommons(wiki).whatTranscludesHere("Template:Deletion template tag", NS.FILE));

		BotUtils.getFirstOnlySharedDuplicate(wiki,
				wiki.getCategoryMembers("Category:Files nominated for deletion on Wikimedia Commons", NS.FILE)).forEach((k, v) -> {
					if (!cffdl.contains(wiki.convertIfNotInNS(v, NS.FILE)))
						wiki.replaceText(k, nfdcRegex, String.format(ncd, v), "BOT: File is not up for deletion on Commons");
				});
	}
}