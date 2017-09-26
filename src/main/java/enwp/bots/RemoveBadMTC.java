package enwp.bots;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;
import util.WTP;

/**
 * Untags non-eligible files for Commons.
 * 
 * @author Fastily
 *
 */
public final class RemoveBadMTC
{
	/**
	 * Main driver
	 * 
	 * @param args None, n/a
	 */
	public static void main(String[] args) throws Throwable
	{
		Wiki wiki = BotUtils.getFastilyBot();
		
		HashSet<String> mtcFiles = WTP.mtc.getTransclusionSet(wiki, NS.FILE);
		mtcFiles.removeAll(BotUtils.getCategoryMembersR(wiki, "Category:Copy to Wikimedia Commons reviewed by a human").y);
		mtcFiles.removeAll(wiki.getCategoryMembers("Category:Copy to Wikimedia Commons (inline-identified)"));
		
		String tRegex = WTP.mtc.getRegex(wiki);
		for (String blt : wiki.getLinksOnPage(String.format("User:%s/Task2/Blacklist", wiki.whoami())))
			wiki.getCategoryMembers(blt, NS.FILE).stream().filter(mtcFiles::contains)
					.forEach(s -> wiki.replaceText(s, tRegex, "BOT: file may not be eligible for Commons"));
	}
}