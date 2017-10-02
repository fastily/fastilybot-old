package enwp;

import enwp.bots.*;
import enwp.reports.*;
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
	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Print this message and exit")
	private boolean helpRequested;

	/**
	 * Runs the specified bot task, if possible.
	 */
	@Option(names = { "-b" }, description = "Causes this bot task to run")
	private int botNum;

	/**
	 * Runs the specified report task, if possible.
	 */
	@Option(names = { "-r" }, description = "Causes this report task run")
	private int repNum;

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

		String badNumberFmt = "'%d' is not a valid %s task number%n";
		String[] pArgs = new String[0];

		if (bmgr.botNum > 0)
			switch (bmgr.botNum)
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
					System.err.printf(badNumberFmt, bmgr.botNum, "bot");
			}
		else if (bmgr.repNum > 0)
			switch (bmgr.repNum)
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
					CountFfD.main(pArgs);
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
					System.err.printf(badNumberFmt, bmgr.repNum, "report");
			}
		else
			System.out.println("Invalid argument, please run with --help for usage instructions");
	}
}