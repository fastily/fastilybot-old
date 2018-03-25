package fastilybot;

import java.util.ArrayList;

import fastily.jwiki.util.WGen;
import fastilybot.bots.*;
import fastilybot.reports.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * CLI interface which makes it easy to launch enwp bots/reports
 * 
 * @author Fastily
 *
 */
@Command(name = "BMgr", description = "FastilyBot Bot Manager")
public final class BMgr
{
	/**
	 * Flag which triggers help output
	 */
	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Print this message")
	private boolean helpRequested;

	/**
	 * Flag which activates the WGen utility.
	 */
	@Option(names = { "--wgen" }, description = "Runs the WGen credential management utility")
	private boolean runWGen;

	/**
	 * Runs the specified bot task, if possible.
	 */
	@Option(names = { "-b" }, description = "Causes this bot task to run")
	private ArrayList<Integer> botNums;

	/**
	 * Runs the specified report task, if possible.
	 */
	@Option(names = { "-r" }, description = "Causes this report task run")
	private ArrayList<Integer> repNums;

	/**
	 * No public constructors
	 */
	private BMgr()
	{

	}

	/**
	 * Main driver
	 * 
	 * @param args Program arguments. Additional arguments will be passed to tasks/reports.
	 * @throws Throwable If one of the called bots/reports had an error.
	 */
	public static void main(String[] args) throws Throwable
	{
		BMgr bmgr = CommandLine.populateCommand(new BMgr(), args);
		if (bmgr.helpRequested || args.length == 0)
		{
			CommandLine.usage(bmgr, System.out);
			return;
		}
		else if (bmgr.runWGen)
		{
			WGen.main(new String[0]);
			return;
		}

		String badNumberFmt = "'%d' is not a valid %s task number%n";
		String[] pArgs = new String[0];

		if (bmgr.botNums != null && !bmgr.botNums.isEmpty())
			for (int i : bmgr.botNums)
				switch (i)
				{
					case 1:
						MTCHelper.main(pArgs);
						break;
					case 2:
						RemoveBadMTC.main(pArgs);
						break;
					case 3:
						BrokenSPI.main(pArgs);
						break;
					case 4:
						UnflagOI.main(pArgs);
						break;
					case 5:
						FindLicConflict.main(pArgs);
						break;
					case 6:
						DDNotifier.main(pArgs);
						break;
					case 7:
						FindCommonsFFD.main(pArgs);
						break;
					case 8:
						FindDelComFFD.main(pArgs);
						break;
					case 9:
						FindKeptComFFD.main(pArgs);
						break;
					case 10:
						FlagOI.main(pArgs);
						break;
					case 11:
						DateNowCommons.main(pArgs);
						break;
					case 12:
						FFDNotifier.main(pArgs);
						break;
					default:
						System.err.printf(badNumberFmt, i, "bot");
				}
		
		if (bmgr.repNums != null && !bmgr.repNums.isEmpty())
			for (int i : bmgr.repNums)
				switch (i)
				{
					case 1:
						UntaggedDD.main(pArgs);
						break;
					case 2:
						OrphanedFfD.main(pArgs);
						break;
					case 3:
						TallyLics.main(pArgs);
						break;
					case 4:
						MTCRedirs.main(pArgs);
						break;
					case 5:
						// CountFfD.main(pArgs);
						break;
					case 6:
						OrphanedKL.main(pArgs);
						break;
					case 7:
						OversizedFU.main(pArgs);
						break;
					case 8:
						FprodSum.main(pArgs);
						break;
					case 9:
						MissingFCT.main(pArgs);
						break;
					case 10:
						DupeOnCom.main(pArgs);
						break;
					default:
						System.err.printf(badNumberFmt, i, "report");
				}
	}
}