package com.profiler.agent;

import com.profiler.agent.advice.MemoryAdvice;
import com.profiler.agent.advice.TimingAdvice;
import com.profiler.agent.annotation.Profile;
import com.profiler.agent.exporter.HttpServer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/**
 * Main Java Agent class.
 *
 * 2 modes:
 * 1. premain — loaded at JVM startup
 * 2. agentmain — attached at runtime
 */
public class ProfilerAgent {

    /**
     * Called at JVM start (with -javaagent: flag)
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println("[Profiler] Agent starting (premain)...");
        installAgent(args, inst);
    }

    /**
     * Called when attached at runtime
     */
    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("[Profiler] Agent starting (agentmain)...");
        installAgent(args, inst);
    }

    private static void installAgent(String args, Instrumentation inst) {
        // Parse configuration parameters
        AgentConfig config = AgentConfig.parse(args);

        System.out.println("[Profiler] Config: " + config);

        // ByteBuddy transformation
        new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)

                // 1. Types annotated with @Profile
                .type(ElementMatchers.isAnnotatedWith(Profile.class)
                        .or(ElementMatchers.declaresMethod(
                                ElementMatchers.isAnnotatedWith(Profile.class))))

                .transform((builder, typeDescription, classLoader, module, pd) ->
                        builder
                                // Add TimingAdvice to @Profile methods
                                .visit(Advice.to(TimingAdvice.class)
                                        .on(ElementMatchers.isAnnotatedWith(Profile.class)))
                )

                // Additional: filter by package
                .type(ElementMatchers.nameStartsWith(config.getPackagePrefix()))
                .transform((builder, typeDescription, classLoader, module, pd) ->
                        builder
                                .visit(Advice.to(TimingAdvice.class)
                                        .on(ElementMatchers.isPublic()
                                                .and(ElementMatchers.not(ElementMatchers.isStatic()))
                                                .and(ElementMatchers.not(ElementMatchers.isConstructor()))))
                )

                .installOn(inst);

        System.out.println("[Profiler] Agent installed successfully!");

        // Start HTTP server
        try {
            HttpServer server = new HttpServer();
            server.start(config.getHttpPort());

            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[Profiler] Shutting down...");
                server.stop();
            }));
        } catch (Exception e) {
            System.err.println("[Profiler] Failed to start HTTP server: " + e.getMessage());
        }
    }

    /**
     * Agent configuration
     * Format: "package=com.example,port=9999"
     */
    static class AgentConfig {
        private String packagePrefix = "com.demo";
        private int httpPort = 9999;

        public static AgentConfig parse(String args) {
            AgentConfig config = new AgentConfig();
            if (args == null || args.isEmpty()) return config;

            for (String pair : args.split(",")) {
                String[] kv = pair.split("=");
                if (kv.length != 2) continue;

                switch (kv[0].trim()) {
                    case "package":
                        config.packagePrefix = kv[1].trim();
                        break;
                    case "port":
                        config.httpPort = Integer.parseInt(kv[1].trim());
                        break;
                }
            }
            return config;
        }

        public String getPackagePrefix() { return packagePrefix; }
        public int getHttpPort() { return httpPort; }

        @Override
        public String toString() {
            return "package=" + packagePrefix + ", port=" + httpPort;
        }
    }
}