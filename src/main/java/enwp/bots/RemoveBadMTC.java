package enwp.bots;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wpkit.WTP;
import fastily.wpkit.util.Toolbox;
import fastily.wpkit.util.WikiX;

/**
 * Untags non-eligible files for Commons.
 * 
 * @author Fastily
 *
 */
public final class RemoveBadMTC
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * Regular expression matching {@code Template:Copy to Wikimedia Commons}
	 */
	private static final String tRegex = WTP.mtc.getRegex(wiki);

	/**
	 * Main driver
	 * 
	 * @param args None, n/a
	 */
	public static void main(String[] args) throws Throwable
	{
		HashSet<String> mtcFiles = WTP.mtc.getTransclusionSet(wiki, NS.FILE);
		mtcFiles.removeAll(WikiX.getCategoryMembersR(wiki, "Category:Copy to Wikimedia Commons reviewed by a human").y);
		mtcFiles.removeAll(wiki.getCategoryMembers("Category:Copy to Wikimedia Commons (inline-identified)"));
		
		for (String blt : wiki.getLinksOnPage(String.format("User:%s/Task2/Blacklist", wiki.whoami())))
			wiki.getCategoryMembers(blt, NS.FILE).stream().filter(mtcFiles::contains)
					.forEach(s -> wiki.replaceText(s, tRegex, "BOT: file may not be eligible for Commons"));
	}
}