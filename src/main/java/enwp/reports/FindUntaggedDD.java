package enwp.reports;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.Triple;
import fastily.wpkit.text.WPStrings;
import fastily.wpkit.util.Toolbox;

import static java.nio.file.StandardOpenOption.*;

/**
 * Finds and reports on files in daily deletion categories which have recently been untagged.
 * 
 * @author Fastily
 *
 */
public final class FindUntaggedDD
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * The title of the report page
	 */
	private static final String reportPage = "User:FastilyBot/Recently Untagged Dated Deletion Files";

	/**
	 * The list of root categories to inspect
	 */
	private static final ArrayList<String> ddCat = FL.toSAL("Category:Wikipedia files with unknown source",
			"Category:Wikipedia files with unknown copyright status", "Category:Wikipedia files missing permission",
			"Category:Disputed non-free Wikipedia files", "Category:Replaceable non-free use Wikipedia files");

	/**
	 * The regex matching eligible daily deletion categories for review
	 */
	private static final String ddCatRegex = ".*? " + WPStrings.DMYRegex;

	/**
	 * The local storage path for caching the previous run's daily deletion files
	 */
	private static final Path wpddfiles = Paths.get("WPDDFiles.txt");

	/**
	 * The maximum number of old reports to keep on {@code reportPage}.
	 */
	private static final int maxOldReports = 25;

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 * @throws Throwable On IO error
	 */
	public static void main(String[] args) throws Throwable
	{
		HashSet<String> l = FL.toSet(ddCat.stream().flatMap(rootCat -> wiki.getCategoryMembers(rootCat, NS.CATEGORY).stream())
				.filter(cat -> cat.matches(ddCatRegex)).flatMap(cat -> wiki.getCategoryMembers(cat, NS.FILE).stream()));

		if (!Files.exists(wpddfiles))
			dump(l, true);

		HashSet<String> cacheList = FL.toSet(Files.lines(wpddfiles));
		cacheList.removeAll(l);

		String text = wiki.getPageText(reportPage);
		ArrayList<Triple<Integer, String, Integer>> sections = wiki.getSectionHeaders(reportPage);
		if (sections.size() > maxOldReports)
			text = text.substring(0, sections.get(maxOldReports).z);

		wiki.edit(reportPage, Toolbox.listify("== ~~~~~ ==\n", MQuery.exists(wiki, true, new ArrayList<>(cacheList)), true) + text,
				"Updating report");

		dump(l, false);
	}

	/**
	 * Dumps a HashSet to <code>wpddfiles</code>
	 * 
	 * @param l The HashSet to use
	 * @param exit Set true to exit the program after this method completes.
	 * @throws Throwable IO Error.
	 */
	private static void dump(HashSet<String> l, boolean exit) throws Throwable
	{
		Files.write(wpddfiles, l, CREATE, WRITE, TRUNCATE_EXISTING);
		if (exit)
			System.exit(0);
	}
}