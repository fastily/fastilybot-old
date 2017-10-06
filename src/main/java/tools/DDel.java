package tools;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.dwrap.Revision;
import fastily.jwiki.tp.WParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import util.BotUtils;
import util.DateUtils;
import util.WTP;

/**
 * Assistant for enwp daily deletion categories. DO NOT use without first checking the items in each category.
 * 
 * @author Fastily
 *
 */
@Command(name = "DDel", description = "Enwp daily deletion category assistant")
public class DDel
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki;

	/**
	 * Time, eight days ago
	 */
	private static ZonedDateTime eightDaysAgo = DateUtils.getUTCofNow().minusDays(8);

	/**
	 * Corresponds to CLI option to request help
	 */
	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Print this message and exit")
	private boolean helpRequested;

	/**
	 * Corresponds to CLI option to run fprod
	 * 
	 * @see #fprod(ZonedDateTime)
	 */
	@Option(names = { "--fprod" }, description = "Run FPROD")
	private boolean doFPROD;

	/**
	 * Corresponds to CLI option to run ffd
	 * 
	 * @see #ffd(String)
	 */
	@Option(names = { "--ffd" }, description = "Run FFD")
	private boolean doFFD;

	/**
	 * Corresponds to CLI option to run emptyCats
	 * 
	 * @see #emptyCats()
	 */
	@Option(names = { "--ec" }, description = "Run emptyCats")
	private boolean doEC;

	/**
	 * Corresponds to CLI option to run emptyCats
	 * 
	 * @see #orfud(String)
	 */
	@Option(names = { "--orfud" }, description = "Run orfud")
	private boolean doOrfud;

	/**
	 * Corresponds to CLI option to run prod
	 * 
	 * @see #prod(String)
	 */
	@Option(names = { "--prod" }, description = "Run prod")
	private boolean doProd;

	/**
	 * Overrides the default process date with the specified date.
	 */
	@Option(names = { "-d", "--date" }, description = "Date to use")
	private ZonedDateTime date;

	/**
	 * No public constructors
	 */
	private DDel()
	{

	}

	/**
	 * Main driver
	 * 
	 * @param args Program arguments. Run with {@code --help} to get the full list.
	 */
	public static void main(String[] args)
	{
		DDel ddel = new DDel();
		new CommandLine(ddel).registerConverter(ZonedDateTime.class,
				s -> ZonedDateTime.of(LocalDate.parse(s, DateUtils.DMY), LocalTime.of(0, 0), ZoneOffset.UTC)).parse(args);
		if (ddel.helpRequested || args.length == 0)
		{
			CommandLine.usage(ddel, System.out);
			return;
		}

		wiki = BotUtils.getFastily();

		if (ddel.doFPROD)
			fprod(ddel.date != null ? ddel.date : eightDaysAgo);
		if (ddel.doFFD)
			ffd(ddel.date != null ? ddel.date : eightDaysAgo);
		if (ddel.doEC)
			emptyCats();
		if (ddel.doOrfud)
			orfud(ddel.date != null ? ddel.date : eightDaysAgo);
		if (ddel.doProd)
			prod(ddel.date != null ? ddel.date : eightDaysAgo);
	}

	/**
	 * Process specified date's file PROD
	 * 
	 * @param date The day of items to process.
	 */
	private static void fprod(ZonedDateTime date)
	{
		ArrayList<String> ftl = new ArrayList<>();
		String fprodTP = wiki.nss(WTP.fprod.title);

		ArrayList<String> pages = wiki.getCategoryMembers("Category:Proposed deletion as of " + DateUtils.dateAsDMY(date), NS.FILE);
		pages.removeAll(WTP.ffd.getTransclusionSet(wiki, NS.FILE));

		MQuery.getPageText(wiki, pages).forEach((k, v) -> {
			try
			{
				String summary = WParser.parseText(wiki, v).getTemplatesR().stream().filter(t -> t.title.equalsIgnoreCase(fprodTP))
						.findFirst().get().get("concern").toString().trim();
				if (!summary.isEmpty() && wiki.delete(k, "Expired [[Wikipedia:Proposed deletion|PROD]], concern was: " + summary))
					ftl.add(k);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		});

		BotUtils.talkDeleter(wiki, NS.FILE_TALK, ftl, BotUtils.csdG8talk);
	}

	/**
	 * Process the specified day's ffds.
	 * 
	 * @param date The day of items to process
	 */
	private static void ffd(ZonedDateTime date)
	{
		Pattern tsRegex = Pattern.compile("\\d{4} \\(UTC\\)");
		String ffdPage = "Wikipedia:Files for discussion/" + DateUtils.dateAsYMD(date);

		ArrayList<String> fl = new ArrayList<>();
		BotUtils.listPageSections(wiki, ffdPage).stream().forEach(t -> {
			if (t.x == 4 && wiki.whichNS(t.y).equals(NS.FILE) && !t.z.contains(wiki.whoami()))
			{
				// Skip threads with more than one post
				Matcher m = tsRegex.matcher(t.z);
				int i = 0;
				while (m.find())
					if (++i > 1)
						return;

				fl.add(t.y);
			}
		});

		ArrayList<String> ftpl = new ArrayList<>();
		for (String s : fl)
			if (wiki.delete(s, String.format("[[%s#%s]]", ffdPage, s)))
				ftpl.add(s);

		BotUtils.talkDeleter(wiki, NS.FILE_TALK, ftpl, BotUtils.csdG8talk);
	}

	/**
	 * Deletes empty categories in CSD.
	 */
	private static void emptyCats()
	{
		ArrayList<String> tpl = new ArrayList<>();
		MQuery.getCategorySize(wiki, wiki.getCategoryMembers("Category:Candidates for speedy deletion as empty categories", NS.CATEGORY))
				.forEach((k, v) -> {
					if (v.equals(0) && wiki.delete(k, "[[WP:CSD#C1|C1]]: Empty category"))
						tpl.add(k);
				});

		BotUtils.talkDeleter(wiki, NS.CATEGORY_TALK, tpl, BotUtils.csdG8talk);
	}

	/**
	 * Process the day's orfud files.
	 * 
	 * @param date The day of items to process.
	 */
	private static void orfud(ZonedDateTime date)
	{
		String cat = "Category:Orphaned non-free use Wikipedia files as of " + DateUtils.dateAsDMY(date);

		ArrayList<String> ftl = new ArrayList<>();
		MQuery.fileUsage(wiki, wiki.getCategoryMembers(cat, NS.FILE)).forEach((k, v) -> {
			if (v.isEmpty() && wiki.delete(k, "[[WP:CSD#F5|F5]]: Unused non-free media file for more than 7 days"))
				ftl.add(k);
		});

		BotUtils.talkDeleter(wiki, NS.FILE_TALK, ftl, BotUtils.csdG8talk);

		if (wiki.getCategorySize(cat) == 0)
			wiki.delete(cat, "[[WP:CSD#G6|G6]]: Housekeeping and routine (non-controversial) cleanup");
	}

	/**
	 * Process the day's PRODs
	 * 
	 * @param date The day of items to process, as DMY.
	 */
	private static void prod(ZonedDateTime date)
	{
		String cat = "Category:Proposed deletion as of " + DateUtils.dateAsDMY(date);
		Pattern pRgx = Pattern.compile(WTP.prod.getRegex(wiki));

		ArrayList<String> tpl = new ArrayList<>();
		for (String s : wiki.getCategoryMembers(cat, NS.MAIN))
			try
			{
				ArrayList<Revision> revs = wiki.getRevisions(s, 3, false, null, null);
				if (revs.size() < 3 || pRgx.matcher(revs.get(1).text).find() || pRgx.matcher(revs.get(2).text).find())
					continue; // prev revs should not contain prod

				String concern = WParser.parseText(wiki, revs.get(0).text).getTemplatesR().stream()
						.filter(t -> t.title.equalsIgnoreCase("Proposed deletion/dated")).findFirst().get().get("concern").toString().trim();

				if (!concern.isEmpty() && wiki.delete(s, "Expired [[Wikipedia:Proposed deletion|PROD]], concern was: " + concern))
					tpl.add(s);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}

		BotUtils.talkDeleter(wiki, NS.TALK, tpl, BotUtils.csdG8talk);
	}
}