package enwp.bots;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.wpkit.text.WTP;
import fastily.wpkit.util.Toolbox;
import fastily.wpkit.util.WikiX;

/**
 * Finds files on enwp nominated for deletion on Commons and flags the local file.
 * 
 * @author Fastily
 *
 */
public class FindCommonsFFD
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki enwp = Toolbox.getFastilyBot();

	/**
	 * Matches wikitext usages of Template:Now Commons
	 */
	private static final String ncRegex = WTP.ncd.getRegex(enwp);

	/**
	 * Main driver
	 * 
	 * @param args Program args, not used
	 */
	public static void main(String[] args)
	{
		HashSet<String> fl = findComFFD();

		WikiX.getFirstOnlySharedDuplicate(enwp, enwp.whatTranscludesHere(WTP.ncd.title, NS.FILE)).forEach((k, v) -> {
			if (fl.contains(enwp.convertIfNotInNS(v, NS.FILE)))
				enwp.replaceText(k, ncRegex, String.format("{{Nominated for deletion on Commons|%s}}", enwp.nss(v)),
						"BOT: File is up for deletion on Commons");
		});
	}

	/**
	 * Fetches the Set of files currently nominated for deletion on Commons
	 * 
	 * @return The Set of files nominated for deletion on Commons.
	 */
	protected static HashSet<String> findComFFD()
	{
		Wiki wiki = new Wiki("commons.wikimedia.org");

		ArrayList<String> cats = FL.toSAL("Category:Copyright violations", "Category:Other speedy deletions");
		cats.addAll(FL
				.toAL(Stream.of("Category:Media missing permission", "Category:Media without a license", "Category:Media without a source")
						.flatMap(c -> wiki.getCategoryMembers(c, NS.CATEGORY).stream()).filter(s -> s.matches(".+?\\d{4}"))));

		HashSet<String> l = FL.toSet(cats.stream().flatMap(c -> wiki.getCategoryMembers(c, NS.FILE).stream()));
		l.addAll(wiki.whatTranscludesHere("Template:Delete"));

		return l;
	}
}