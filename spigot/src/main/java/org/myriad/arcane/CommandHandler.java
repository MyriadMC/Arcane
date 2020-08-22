package org.myriad.arcane;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CommandHandler {
    // For example:
    // The class for the command /heal would be:
    // Package.Path.To.The.Commands.Package.CommandInfo

    private CommandSender sender;
    private String command, label;
    private String[] args;
    private Settings settings;

    private HashMap<String, CommandClass> cmdClasses = new HashMap<>();

    private Class[] setParamsParameters = new Class[] { CommandSender.class, String.class, String.class, String[].class };
    private Class[] argsParam = new Class[] { String[].class };

    private static HashMap<String, String> cmdAlternatives = new HashMap<>();


    public CommandHandler(CommandSender sender, String label, String[] args, Settings settings) {
        String command = getAlternative(label);
        new CommandHandler(sender, command, label, args, settings);
    }

    public CommandHandler(CommandSender sender, String command, String label, String[] args, Settings settings) {

        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = args;
        this.settings = settings;

        try {
            Class<?> setupClass = Class.forName(this.settings.PACKAGE_PATH + "." + this.settings.COMMAND_PREFIX + label);
            Object classInstance = setupClass.newInstance();


            execute();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void execute() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if(!cmdClasses.containsKey(command)) {
            addCommand(command);
        }

        Object classInstance = cmdClasses.get(command).cmdclass.newInstance();

        cmdClasses.get(command).setParams.invoke(classInstance, sender, command, label, args);

        switch(findCase()) {

            //Console no args
            case 1: {
                cmdClasses.get(command).runConsole.invoke(classInstance);
                break;
            }

            //Console args
            case 2: {
                cmdClasses.get(command).runConsoleArgs.invoke(classInstance, args);
                break;
            }

            //Player no args
            case 11: {
                cmdClasses.get(command).run.invoke(classInstance);
                break;
            }

            //Player args
            case 12: {
                cmdClasses.get(command).runArgs.invoke(classInstance, args);
                break;
            }
        }
    }

    private int findCase() {
        int _case = 0;
        if(sender instanceof Player) {
            _case += 10;
        }
        if(args.length > 0) {
            _case += 2;
        } else {
            _case += 1;
        }

        return _case;

    }

    private void addCommand(String name) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        CommandClass commandClass = new CommandClass();

        commandClass.cmdclass = Class.forName(this.settings.PACKAGE_PATH + "." + this.settings.COMMAND_PREFIX + name);
        commandClass.setParams = commandClass.cmdclass.getSuperclass().getDeclaredMethod("setParams", setParamsParameters);

        try {
            commandClass.run = commandClass.cmdclass.getDeclaredMethod("Run");
        } catch(NoSuchMethodException exception) {
            commandClass.run = commandClass.cmdclass.getSuperclass().getDeclaredMethod("Run");
        }

        try {
            commandClass.runArgs = commandClass.cmdclass.getDeclaredMethod("Run", argsParam);
        } catch(NoSuchMethodException exception) {
            commandClass.runArgs = commandClass.cmdclass.getSuperclass().getDeclaredMethod("Run", argsParam);
        }

        try {
            commandClass.runConsole = commandClass.cmdclass.getDeclaredMethod("RunConsole");
        } catch(NoSuchMethodException exception) {
            commandClass.runConsole = commandClass.cmdclass.getSuperclass().getDeclaredMethod("RunConsole");
        }

        try {
            commandClass.runConsoleArgs = commandClass.cmdclass.getDeclaredMethod("RunConsole", argsParam);
        } catch(NoSuchMethodException exception) {
            commandClass.runConsoleArgs = commandClass.cmdclass.getSuperclass().getDeclaredMethod("RunConsole", argsParam);
        }

        cmdClasses.put(name, commandClass);

    }

    private class CommandClass {
        private Class<?> cmdclass;
        private Method setParams, run, runArgs, runConsole, runConsoleArgs;
    }

    public static void setAlternatives(Plugin plugin) {
        Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();
        for(String command : commands.keySet()) {

            Object aliases = commands.get(command).get("aliases");
            for(String alias : String.valueOf(aliases).split(",")) {
                cmdAlternatives.put(alias.replace("[", "").replace("]", "").replace(" ", ""), command);
            }
            cmdAlternatives.put(command, command);
        }
    }

    private String getAlternative(String label) {
        return cmdAlternatives.get(label).toLowerCase();
    }

    public String getPackagePath() {
        return this.settings.PACKAGE_PATH;
    }

    public String getCommandPrefix() {
        return this.settings.COMMAND_PREFIX;
    }

    public static class CommandInfo {

        protected CommandSender sender;
        protected String command, label;
        protected String[] args;

        protected Player player = null;

        public void setParams(CommandSender sender, String command, String label, String[] args) {
            this.sender = sender;
            this.command = command;
            this.label = label;
            this.args = args;

            if(sender instanceof Player) {
                player = Bukkit.getPlayer(sender.getName());
            }
        }

        void Run() {
            sender.sendMessage("Default Message With Args!");
        }

        void Run(String[] args) {
            sender.sendMessage("Default Message!");
        }

        void RunConsole() {
            sender.sendMessage("Default Message!");
        }

        void RunConsole(String[] args) {
            sender.sendMessage("Default Message With Args!");
        }
    }
}