package org.eu.hanana.reimu.tnmc;

import cn.fhyjs.cirno.Callback;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Base {
    public static String host;
    public static Map<String,FuncBase> funcs = new HashMap<>();
    public static void start(String[] arg,Runnable runnable) {
        Options options=new Options();
        options.addOption(Option
                .builder("h")
                .longOpt("host")
                .argName("host")
                .hasArg()
                .required(true)
                .desc("set host name")
                .build());
        options.addOption(Option
                .builder("?")
                .longOpt("help")
                .argName("help")
                .required(false)
                .desc("cmd help")
                .build());
        CommandLine cmd = null;
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();
        try {
            cmd = parser.parse(options, arg);
            if (cmd.hasOption("?")) {
                throw new ParseException("help");
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options);
            System.exit(-1);
        }
        try {
             host=cmd.getOptionValue("h");
             runnable.run();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("usage: --host <ip>:<port>");
        }
    }
    public static void regFunc(String name,FuncBase funcBase) throws IOException {
        funcs.put(name,funcBase);
        new Thread(new Watcher(name)).start();
    }
    public static class Watcher implements Runnable{
        private final String name;

        public Watcher(String name){
            this.name = name;
        }
        @Override
        public void run() {
            ExTelnet telnet;
            try {
                telnet = new ExTelnet(Base.host);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            telnet.setCallback(new Callback() {
                @Override
                public void OnReceive(String s) {
                    if (s.startsWith("#")){
                        if (name.equals(s.substring(1).split(" ")[0]))
                            funcs.get(s.substring(1).split(" ")[0]).run(s.substring(1).split(" "));
                    }
                }

                @Override
                public void OnExit() {

                }
            });
            try {
                telnet.send("#"+name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (telnet.socket.isConnected()){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}