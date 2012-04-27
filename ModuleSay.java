import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import pl.shockah.StringTools;
import pl.shockah.shocky.Module;
import pl.shockah.shocky.Shocky;
import pl.shockah.shocky.cmds.Command;

public class ModuleSay extends Module {
	protected Command cmdSay, cmdAction;
	
	public String name() {return "say";}
	public void load() {
		Command.addCommands(cmdSay = new CmdSay(),cmdAction = new CmdAction());
	}
	public void unload() {
		Command.removeCommands(cmdSay,cmdAction);
	}
	
	public class CmdSay extends Command {
		public String command() {return "say";}
		public String help(PircBotX bot, EType type, Channel channel, User sender) {
			return "[r:controller] say {phrase} - makes the bot say {phrase}";
		}
		public boolean matches(PircBotX bot, EType type, String cmd) {return cmd.equals(command());}
		
		public void doCommand(PircBotX bot, EType type, Channel channel, User sender, String message) {
			if (!canUseController(bot,type,sender)) return;
			
			String[] args = message.split(" ");
			if (args.length >= 2) {
				Shocky.send(bot,type,EType.Channel,EType.Notice,EType.Notice,EType.Console,channel,sender,StringTools.implode(args,1," "));
			} else Shocky.sendNotice(bot,sender,help(bot,type,channel,sender));
		}
	}
	public class CmdAction extends Command {
		public String command() {return "action";}
		public String help(PircBotX bot, EType type, Channel channel, User sender) {
			return "[r:controller] action {action} - /me {action}";
		}
		public boolean matches(PircBotX bot, EType type, String cmd) {return cmd.equals(command()) || cmd.equals("act");}
		
		public void doCommand(PircBotX bot, EType type, Channel channel, User sender, String message) {
			if (!canUseController(bot,type,sender)) return;
			
			if (type != EType.Channel) return;
			String[] args = message.split(" ");
			if (args.length >= 2) {
				bot.sendAction(channel,StringTools.implode(args,1," "));
			} else Shocky.send(bot,type,EType.Notice,EType.Notice,EType.Notice,EType.Console,channel,sender,help(bot,type,channel,sender));
		}
	}
}