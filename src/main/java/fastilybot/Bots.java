package fastilybot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.WParser;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.MultiMap;
import fastily.wptoolbox.BotUtils;
import fastily.wptoolbox.WTP;
import fastilybot.shared.Settings;
import fastilybot.shared.TemplateTools;

/**
 * Fastily's Wikipedia Bots
 * 
 * @author Fastily
 *
 */
public class Bots
{
	/**
	 * The main Wiki object to use
	 */
	private Wiki wiki;

	/**
	 * Constructor, takes a logged-in Wiki object to enwp.
	 * 
	 * @param wiki The Wiki object to use
	 */
	protected Bots(Wiki wiki)
	{
		this.wiki = wiki;
	}
	
	/**
	 * Finds broken SPI pages on enwp and reports on them.
	 */
	public void brokenSPI()
	{
		String report = "Wikipedia:Sockpuppet investigations/SPI/Malformed Cases Report";
		
		HashSet<String> spiCases = FL.toSet(wiki.prefixIndex(NS.PROJECT, "Sockpuppet investigations/").stream()
				.filter(s -> !(s.endsWith("/Archive") || s.startsWith("Wikipedia:Sockpuppet investigations/SPI/"))));

		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI case status", NS.PROJECT));
		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI archive notice", NS.PROJECT));
		spiCases.removeAll(wiki.getLinksOnPage(report + "/Ignore"));

		ArrayList<String> l = new ArrayList<>();
		MQuery.resolveRedirects(wiki, spiCases).forEach((k, v) -> {
			if (k.equals(v)) // filter redirects
				l.add(v);
		});

		wiki.edit(report, BotUtils.listify("{{/Header}}\n" + Settings.updatedAt, l, false),
				String.format("BOT: Update list (%d items)", l.size()));
	}

	/**
	 * Fills in date parameter (and other missing parameters) for files in Category:Wikipedia files with the same name
	 * on Wikimedia Commons as of unknown date.
	 */
	public void dateNowCommons()
	{
		Wiki wiki = BotUtils.getFastilyBot();

		ArrayList<String> l = wiki
				.getCategoryMembers("Category:Wikipedia files with the same name on Wikimedia Commons as of unknown date", NS.FILE);
		l.removeAll(BotUtils.getCategoryMembersR(wiki, "Category:Wikipedia files reviewed on Wikimedia Commons").y);

		String ncRegex = WTP.ncd.getRegex(wiki);
		for (String s : l)
			wiki.replaceText(s, ncRegex, "{{Subst:Ncd}}", "BOT: Dating {{Now Commons}}");
	}

	/**
	 * Leaves courtesy notifications (where possible) for users whose files were nominated at FfD.
	 */
	public void ffdNotifier()
	{
		// Constants
		ZonedDateTime today = ZonedDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.of(0, 0), ZoneOffset.UTC);
		Instant start = today.toInstant(), end = Instant.now();

		String targetFFD = String.format("Wikipedia:Files for discussion/%d %s %d", today.getYear(),
				today.getMonth().getDisplayName(TextStyle.FULL, Locale.US), today.getDayOfMonth());
		HashSet<String> noBots = WTP.nobots.getTransclusionSet(wiki, NS.USER_TALK);

		// Associate possibly eligible files by user
		MultiMap<String, String> l = new MultiMap<>();
		wiki.splitPageByHeader(targetFFD).stream().filter(t -> t.level == 4 && wiki.whichNS(t.header).equals(NS.FILE)).forEach(t -> {
			String author = wiki.getPageCreator(t.header);
			if (author != null && !noBots.contains(author = wiki.convertIfNotInNS(author, NS.USER_TALK)))
				l.put(author, t.header);
		});

		// Skip files if user(s) have been notified, then notify accordingly
		l.l.forEach((k, v) -> {
			ArrayList<String> rl = BotUtils.detLinksInHist(wiki, k, v, start, end);
			if (rl.isEmpty())
				return;

			String x = String.format("%n{{subst:User:FastilyBot/Task12Note|%s|%s}}", rl.get(0), targetFFD);
			if (rl.size() > 1)
				x += BotUtils.listify("\nAlso:\n", rl.subList(1, rl.size()), true);
			wiki.addText(k, x + Settings.botNote, "BOT: Notify user of FfD", false);
		});
	}

	/**
	 * Finds files on enwp nominated for deletion on Commons and flags the local file.
	 */
	public void findCommonsFFD()
	{
		String ncRegex = WTP.ncd.getRegex(wiki);
		HashSet<String> fl = new HashSet<>(BotUtils.getCommons(wiki).whatTranscludesHere("Template:Deletion template tag", NS.FILE));

		BotUtils.getFirstOnlySharedDuplicate(wiki, wiki.whatTranscludesHere(WTP.ncd.title, NS.FILE)).forEach((k, v) -> {
			if (fl.contains(wiki.convertIfNotInNS(v, NS.FILE)))
				wiki.replaceText(k, ncRegex, String.format("{{Nominated for deletion on Commons|%s}}", wiki.nss(v)),
						"BOT: File is up for deletion on Commons");
		});
	}

	/**
	 * Finds local enwp files transferred to Commons which have then been deleted on Commons.
	 */
	public void findDelComFFD()
	{
		Wiki com = BotUtils.getCommons(wiki);

		Pattern nomDelTemplPattern = Pattern.compile(WTP.nomDelOnCom.getRegex(wiki));
		HashMap<String, String> pageTexts = MQuery.getPageText(wiki, wiki.whatTranscludesHere(WTP.nomDelOnCom.title, NS.FILE));

		HashMap<String, String> comPairs = new HashMap<>();
		pageTexts.forEach((k, v) -> {
			try
			{

				String comFile = WParser.parseText(wiki, BotUtils.extractTemplate(nomDelTemplPattern, v)).getTemplates().get(0).get("1")
						.toString();
				if (comFile != null)
					comPairs.put(k, wiki.convertIfNotInNS(comFile, NS.FILE));
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		});

		FL.toHM(MQuery.exists(com, false, comPairs.keySet()).stream()
				.filter(s -> !com.getLogs(comPairs.get(s), null, "delete", 1).isEmpty()), Function.identity(), comPairs::get)
				.forEach((k, v) -> wiki.edit(k,
						pageTexts.get(k).replaceAll(WTP.nomDelOnCom.getRegex(wiki), String.format("{{Deleted on Commons|%s}}", wiki.nss(v))),
						"BOT: Adding note that file has been deleted on Commons"));

	}

	/**
	 * Finds local enwp files which were nominated for deletion on Commons but kept.
	 */
	public void findKeptComFFD()
	{
		String nfdcRegex = WTP.nomDelOnCom.getRegex(wiki);
		String ncd = TemplateTools.ncdTemplateFor(wiki.whoami());

		HashSet<String> cffdl = new HashSet<>(BotUtils.getCommons(wiki).whatTranscludesHere("Template:Deletion template tag", NS.FILE));

		BotUtils.getFirstOnlySharedDuplicate(wiki,
				wiki.getCategoryMembers("Category:Files nominated for deletion on Wikimedia Commons", NS.FILE)).forEach((k, v) -> {
					if (!cffdl.contains(wiki.convertIfNotInNS(v, NS.FILE)))
						wiki.replaceText(k, nfdcRegex, String.format(ncd, v), "BOT: File is not up for deletion on Commons");
				});
	}

	/**
	 * Finds enwp files which are flagged as both free and non-free.
	 */
	public void findLicConflict()
	{
		HashSet<String> fl = BotUtils.fetchLabsReportAsFiles(wiki, 2);

		for (String s : wiki.getLinksOnPage(String.format("User:%s/Task5/Ignore", wiki.whoami())))
			fl.removeAll(wiki.whatTranscludesHere(s, NS.FILE));

		for (String s : MQuery.exists(wiki, true, fl))
			wiki.addText(s, "{{Wrong-license}}\n", "BOT: Possible conflict in copyright status", true);
	}

	/**
	 * Finds and flags orphaned free media files on enwp.
	 */
	public void flagOI()
	{
		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, 3);
		l.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, 9)); // omit flagged orphaned files

		for (String c : wiki.getLinksOnPage(String.format("User:%s/Task10/Ignore", wiki.whoami())))
			l.removeAll(wiki.getCategoryMembers(c, NS.FILE));

		l.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));

		l.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, 4));
		l.removeAll(MQuery.exists(wiki, false, l));

		for (String s : l)
			wiki.addText(s, "\n{{Orphan image}}", "BOT: Noting that file has no inbound file usage", false);
	}

	/**
	 * Find and fix tags for files tagged for transfer to Commons which have already transferred.
	 */
	public void mtcHelper()
	{
		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, 1);
		l.retainAll(WTP.mtc.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(WTP.keeplocal.getTransclusionSet(wiki, NS.FILE)); // lots of in-line tags

		String tRegex = WTP.mtc.getRegex(wiki);
		HashSet<String> ncdL = WTP.ncd.getTransclusionSet(wiki, NS.FILE);
		String ncdT = TemplateTools.ncdTemplateFor(wiki.whoami());

		BotUtils.getFirstOnlySharedDuplicate(wiki, l).forEach((k, v) -> {
			if (ncdL.contains(k))
				wiki.replaceText(k, tRegex, "BOT: File has already been copied to Commons");
			else
			{
				String oText = wiki.getPageText(k);
				String nText = oText.replaceAll(tRegex, "");
				if (oText.equals(nText)) // avoid in-line tags
					return;

				wiki.edit(k, String.format(ncdT, v) + nText, "BOT: File is available on Commons");
			}
		});
	}

	/**
	 * Untags non-eligible files for Commons.
	 */
	public void removeBadMTC()
	{
		HashSet<String> l = WTP.mtc.getTransclusionSet(wiki, NS.FILE);
		l.removeAll(BotUtils.getCategoryMembersR(wiki, "Category:Copy to Wikimedia Commons reviewed by a human").y);
		l.removeAll(wiki.getCategoryMembers("Category:Copy to Wikimedia Commons (inline-identified)"));

		String tRegex = WTP.mtc.getRegex(wiki);

		wiki.getLinksOnPage(String.format("User:%s/Task2/Blacklist", wiki.whoami())).stream()
				.flatMap(s -> wiki.getCategoryMembers(s, NS.FILE).stream()).filter(l::contains)
				.forEach(s -> wiki.replaceText(s, tRegex, "BOT: file may not be eligible for Commons"));
	}

	/**
	 * Removes {@code Orphan image} from freely licensed files which contain file links in the main space.
	 */
	public void unflagOI()
	{
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
		for (String s : l)
			wiki.replaceText(s, oiRegex, "BOT: File contains inbound links");
	}
}