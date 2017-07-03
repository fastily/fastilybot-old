package enwp.bots;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wpkit.WPStrings;
import fastily.wpkit.WTP;
import fastily.wpkit.util.Toolbox;
import fastily.wpkit.util.WikiX;

/**
 * Finds local enwp files which were nominated for deletion on Commons but kept.
 * 
 * @author Fastily
 *
 */
public final class FindKeptComFFD
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * The regex matching Template:Nominated for deletion on Commons.
	 */
	private static final String nfdcRegex = WTP.nomDelOnCom.getRegex(wiki);

	/**
	 * The ncd template to fill out
	 */
	private static final String ncd = WPStrings.makeNCDBotTemplate(wiki.whoami());
	
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		HashSet<String> cffdl = FindCommonsFFD.findComFFD();

		WikiX.getFirstOnlySharedDuplicate(wiki,
				wiki.getCategoryMembers("Category:Files nominated for deletion on Wikimedia Commons", NS.FILE)).forEach((k, v) -> {
					if (!cffdl.contains(wiki.convertIfNotInNS(v, NS.FILE)))
						wiki.replaceText(k, nfdcRegex, String.format(ncd, v), "BOT: File is not up for deletion on Commons");
				});
	}
}