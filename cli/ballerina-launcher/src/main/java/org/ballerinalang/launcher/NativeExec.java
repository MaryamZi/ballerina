package org.ballerinalang.launcher;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.VMOptions;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.ballerinalang.launcher.BallerinaCliCommands.DEFAULT;
import static org.ballerinalang.launcher.BallerinaCliCommands.HELP;
import static org.ballerinalang.launcher.BallerinaCliCommands.RUN;
import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;

/**
 * Temporary function to link current command execution logic to the Ballerina implementation.
 *
 * @since 0.981.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "cmd_parser",
        functionName = "nativeExec",
        args = {@Argument(name = "cmdName", type = TypeKind.STRING),
                @Argument(name = "parsedArgs", type = TypeKind.ARRAY, elementType = TypeKind.ANY)},
        returnType = {@ReturnType(type = TypeKind.ANY)}
)
public class NativeExec extends BlockingNativeCallableUnit {

    private static PrintStream outStream = System.err;

    @Override
    public void execute(Context context) {
        BRefValueArray parsedArgs = (BRefValueArray) context.getRefArgument(0);
        switch (context.getStringArgument(0)) {
            case DEFAULT:
                execDefault(parsedArgs);
                context.setReturnValues();
                return;
            case RUN:
                execRun(parsedArgs);
                context.setReturnValues(); //TODO: eventually return that returned by main
                return;
            default:
                throw new BallerinaException("execute() called for unknown command: " + context.getStringArgument(0));

        }

    }

    private void execDefault(BRefValueArray parsedArgs) {
        boolean helpFlag = ((BBoolean) parsedArgs.get(0)).booleanValue();
        boolean versionFlag = ((BBoolean) parsedArgs.get(1)).booleanValue();

        if (helpFlag) {
            printUsageInfo(HELP);
            return;
        }

        if (versionFlag) {
            printVersionInfo();
            return;
        }

        printUsageInfo(DEFAULT);
    }

    private void execRun(BRefValueArray parsedArgs) {
        String sourceRoot = parsedArgs.get(0).stringValue();
        boolean helpFlag = ((BBoolean) parsedArgs.get(1)).booleanValue();
        boolean offline = ((BBoolean) parsedArgs.get(2)).booleanValue();
        String debugPort = parsedArgs.get(3).stringValue();
        String javaDebugPort = parsedArgs.get(4).stringValue();
        String configFilePath = parsedArgs.get(5).stringValue();
        boolean observeFlag = ((BBoolean) parsedArgs.get(6)).booleanValue();

        BMap<String, BString> runtimeParamsMap = (BMap<String, BString>) parsedArgs.get(7);
        Map<String, String> runtimeParams = new HashMap<>();
        runtimeParamsMap.getMap().entrySet().forEach(entry -> {
            runtimeParams.put(entry.getKey(), entry.getValue().stringValue());
        });

        BMap<String, BString> vmOptionsMap = (BMap<String, BString>) parsedArgs.get(8);
        Map<String, String> vmOptions = new HashMap<>();
        vmOptionsMap.getMap().entrySet().forEach(entry -> {
            vmOptions.put(entry.getKey(), entry.getValue().stringValue());
        });

        String source = parsedArgs.get(9).stringValue();
        String[] programArgs = ((BStringArray) parsedArgs.get(10)).getStringArray();

        if (helpFlag) {
            printUsageInfo(RUN);
            return;
        }

        if (source.isEmpty()) {
            throw LauncherUtils.createUsageException("no ballerina program given");
        }

        // Enable remote debugging
        if (!debugPort.isEmpty()) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        System.setProperty("ballerina.source.root", sourceRootPath.toString());
        VMOptions.getInstance().addOptions(vmOptions);

        Path sourcePath = Paths.get(source);

        LauncherUtils.runProgram(sourceRootPath, sourcePath, false, runtimeParams, configFilePath,
                                 programArgs, offline, observeFlag);
    }

    private static void printUsageInfo(String commandName) {
        String usageInfo = BLauncherCmd.getCommandUsageInfo(commandName);
        outStream.println(usageInfo);
    }

    private static void printVersionInfo() {
        try (InputStream inputStream = Main.class.getResourceAsStream("/META-INF/launcher.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            String version = "Ballerina " + properties.getProperty("ballerina.version") + "\n";
            outStream.print(version);
        } catch (Throwable ignore) {
            // Exception is ignored
            throw LauncherUtils.createUsageException("version info not available");
        }
    }
}
