import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import pl.shockah.HTTPQuery;
import pl.shockah.StringTools;
import pl.shockah.shocky.Data;
import pl.shockah.shocky.ScriptModule;
import pl.shockah.shocky.cmds.Command;
import pl.shockah.shocky.cmds.CommandCallback;
import pl.shockah.shocky.cmds.Parameters;
import pl.shockah.shocky.cmds.Command.EType;

public class ModulePython extends ScriptModule {
	protected Command cmd;
	
	public String name() {return "python";}
	public String identifier() {return "py";}
	public void onEnable() {
		Data.config.setNotExists("python-url","http://eval.appspot.com/eval");
		Command.addCommands(this, cmd = new CmdPython());
		Data.protectedKeys.add("python-url");
	}
	public void onDisable() {
		Command.removeCommands(cmd);
	}
	
	public String parse(Map<Integer,Object> cache, PircBotX bot, EType type, Channel channel, User sender, String code, String message) {
		if (code == null) return "";
		
		StringBuilder sb = new StringBuilder("channel = \""+channel.getName()+"\";bot = \""+bot.getNick().replace("\"","\\\"")+"\";sender = \""+sender.getNick().replace("\"","\\\"")+"\";");
		if (message != null) {
			String[] args = message.split(" ");
			String argsImp = StringTools.implode(args,1," "); if (argsImp == null) argsImp = "";
			sb.append("argc = "+(args.length-1)+";args = \""+argsImp.replace("\"","\\\"")+"\";ioru = \""+(args.length-1 == 0 ? sender.getNick() : argsImp).replace("\"","\\\"")+"\";");
			sb.append("arg = [");
			for (int i = 1; i < args.length; i++) {
				if (i != 1) sb.append(",");
				sb.append("\""+args[i].replace("\"","\\\"")+"\"");
			}
			sb.append("];");
		}
		
		User[] users = channel.getUsers().toArray(new User[channel.getUsers().size()]);
		sb.append("randnick = \""+users[new Random().nextInt(users.length)].getNick().replace("\"","\\\"")+"\";");
		
		code = sb.toString()+code;
		
		System.out.println(Data.forChannel(channel).getString("python-url")+"?"+HTTPQuery.parseArgs("statement",code));
		HTTPQuery q = HTTPQuery.create(Data.forChannel(channel).getString("python-url")+"?"+HTTPQuery.parseArgs("statement",code));
		q.connect(true,false);
		
		sb = new StringBuilder();
		ArrayList<String> result = q.readLines();
		q.close();
		if (result.size()>0 && result.get(0).contentEquals("Traceback (most recent call last):"))
			return result.get(result.size()-1);
		
		for (String line : result) {
			if (sb.length() != 0) sb.append('\n');
			sb.append(line);
		}
		
		return sb.toString();
	}
	
	public class CmdPython extends Command {
		public String command() {return "python";}
		public String help(Parameters params) {
			return "python\npython {code} - runs Python code";
		}
		
		public void doCommand(Parameters params, CommandCallback callback) {
			if (params.tokenCount < 1) {
				callback.type = EType.Notice;
				callback.append(help(params));
				return;
			}
			
			String output = parse(null,params.bot,params.type,params.channel,params.sender,params.input,null);
			if (output != null && !output.isEmpty())
				callback.append(StringTools.limitLength(StringTools.formatLines(output)));
		}
	}
}