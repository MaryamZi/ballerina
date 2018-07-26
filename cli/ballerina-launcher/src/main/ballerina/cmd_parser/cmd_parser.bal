import ballerina/cmd;
import ballerina/io;

@final string DEFAULT_CMD_NAME = "default";
@final string RUN_CMD_NAME = "run";

function main(string... args) {
    DefaultCommand defaultCommand = new;
    cmd:CommandLine commandLine = new(defaultCommand);
    RunCommand runCommand = new;
    commandLine.addSubCommand(RUN_CMD_NAME, runCommand);

    // TODO: dynamic registration of commands

    //return commandLine.parseAndExecute(args);
    _ = commandLine.parseAndExecute(args);
}

@cmd:Command {
    name: DEFAULT_CMD_NAME,
    description: "Default Command"
}
public type DefaultCommand object {

    @cmd:Option {
        names: ["--help", "-h"],
        hidden: true
    }
    public boolean helpFlag;

    @cmd:Option {
        names: ["--debug"]
    }
    public string debugPort;

    @cmd:Option {
        names: ["--java.debug"],
        hidden: true
    }
    public string javaDebugPort;

    @cmd:Option {
        names: ["--version", "-v"],
        hidden: true
    }
    public boolean versionFlag;

    public function execute() returns any {
        any[] parseArgs = [helpFlag, versionFlag];
        return nativeExec(DEFAULT_CMD_NAME, parseArgs);
    }

};

@cmd:Command {
    name: RUN_CMD_NAME,
    description: "compile and run Ballerina programs"
}
public type RunCommand object {

    @cmd:Option {
        names: ["--sourceroot"],
        description: "path to the directory containing source files and packages"
    }
    public string sourceRoot;

    @cmd:Option {
        names: ["--help", "-h"],
        hidden: true
    }
    public boolean helpFlag;

    @cmd:Option {
        names: ["--offline"]
    }
    public boolean offline;

    @cmd:Option {
        names: ["--debug"],
        hidden: true
    }
    public string debugPort;

    @cmd:Option {
        names: ["--java.debug"],
        description: "remote java debugging port",
        hidden: true
    }
    public string javaDebugPort;

    @cmd:Option {
        names: ["--config", "-c"],
        description: "path to the Ballerina configuration file"
    }
    public string configFilePath;

    @cmd:Option {
        names: ["--observe"],
        description: "enable observability with default configs"
    }
    public boolean observeFlag;

    @cmd:DynamicOption {
        names: ["-e"],
        description: "Ballerina environment parameters"
    }
    public map<string> runtimeParams;

    @cmd:DynamicOption {
        names: ["-B"],
        description: "Ballerina VM options"
    }
    public map<string> vmOptions;

    @cmd:PositionalParam {
        position: 0
    }
    public string source;

    @cmd:PositionalParam {
        description: "arguments"
    }
    public string[] programArgs;

    public function execute() returns any {
        any[] parsedArgs = [sourceRoot, helpFlag, offline, debugPort, javaDebugPort, configFilePath, observeFlag,
                            runtimeParams, vmOptions, source, programArgs];
        return nativeExec(RUN_CMD_NAME, parsedArgs);
    }

};

native function nativeExec(string cmdName, any[] parsedArgs) returns any;
