package fastilybot;

import java.util.ArrayList;

import org.fastily.jwiki.core.Wiki;
import org.fastily.wptoolbox.WGen;
import org.fastily.wptoolbox.WikiX;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Entry point for running bots and reports
 * 
 * @author Fastily
 *
 */
@Command(name = "FastilyBot", description = "FastilyBot Bot Manager", version = "1.0.0", mixinStandardHelpOptions = true)
public class FastilyBot implements Runnable
{
	/**
	 * Flag which activates the WGen utility.
	 */
	@Option(names = "--wgen", description = "Runs the WGen credential management utility")
	private boolean runWGen;

	/**
	 * Runs the specified bot tasks. Accepts single and multiple params (separated by ',').
	 */
	@Option(names = "-b", split = ",", description = "Triggers these bot task numbers")
	private ArrayList<Integer> botNums;

	/**
	 * Runs the specified report tasks. Accepts single and multiple params (separated by ',').
	 */
	@Option(names = "-r", split = ",", description = "Triggers these bot report numbers")
	private ArrayList<Integer> repNums;

	/**
	 * No public constructors
	 */
	private FastilyBot()
	{

	}

	/**
	 * Main driver
	 * 
	 * @param args Program arguments
	 */
	public static void main(String[] args)
	{
		new CommandLine(new FastilyBot()).execute(args);
	}

	/**
	 * Runs actual bot tasks
	 */
	@Override
	public void run()
	{
		// option for WikiGen
		if (runWGen)
		{
			WGen.main(new String[0]);
			return;
		}

		// show usage if no args passed
		if (botNums == null && repNums == null)
		{
			CommandLine.usage(this, System.err);
			return;
		}

		Wiki wiki = WikiX.getUserWP("FastilyBot");

		// check if disabled on-wiki
		if (wiki.exists(String.format("User:%s/shutoff", wiki.whoami())))
		{
			System.err.println("SHUTOFF SWITCH ACTIVE, exiting");
			return;
		}

		if (botNums != null && !botNums.isEmpty())
		{
			Bots b = new Bots(wiki);

			for (int i : botNums)
				try
				{
					switch (i)
					{
						case 1:
							b.mtcHelper();
							break;
						case 2:
							b.removeBadMTC();
							break;
						// case 3:
						// See case 5 in the Reports section below
						case 4:
							b.unflagOI();
							break;
						case 5:
							b.findLicConflict();
							break;
						case 6:
							b.ddNotifier();
							break;
						case 7:
							b.findCommonsFFD();
							break;
						case 8:
							b.findDelComFFD();
							break;
						case 9:
							b.findKeptComFFD();
							break;
						case 10:
							b.flagOI();
							break;
						case 11:
							b.dateNowCommons();
							break;
						case 12:
							b.ffdNotifier();
							break;
						default:
							System.err.println("ERROR: Not a valid task number: " + i);
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
		}

		if (repNums != null && !repNums.isEmpty())
		{
			Reports r = new Reports(wiki);

			for (int i : repNums)
				try
				{
					switch (i)
					{
						case 1:
							r.shadowCommonsDescriptionPages();
							break;
						case 2:
							r.orphanedFFD();
							break;
						case 3:
							r.tallyLics();
							break;
						case 4:
							r.mtcRedirs();
							break;
						case 5:
							r.brokenSPI();
							break;
						case 6:
							r.orphanedKL();
							break;
						case 7:
							r.oversizedFU();
							break;
						case 8:
							r.fprodSum();
							break;
						case 9:
							r.missingFCT();
							break;
						case 10:
							r.dupeOnCom();
							break;
						case 11:
							r.lowResFreeFiles();
							break;
						case 12:
							r.possiblyUnsourcedFiles();
							break;
						case 13:
							r.impossibleDD();
							break;
						case 14:
							r.shadowCommonsNonFree();
							break;
						default:
							System.err.println("ERROR: Not a valid report number: " + i);
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
		}
	}
}