import ballerina/io;
import ballerina/reflect;

@final string ALL_REMAINING_ARGS_PARAM_KEY = "remaining_args";
@final string EQUALS = "=";
@final string COMMA = ",";
@final string BOOLEAN_FALSE = "false";

public type CommandLine object {

    private any defCommand;

    // Map of commands against name. For each subcommand registered, the command is added to the map with the name
    // of the command as the key.
    // When a command is identified in parseAndExecute, the command is retrieved by name
    private map commands;

    public new(any defaultCommand) {
        defCommand = defaultCommand;
    }

    public function addSubCommand(string commandName, any command) {
        match (<BaseCommand> command) {
            BaseCommand => {
                //valid command with an execute function
            }
            error err => {
                error e = { message: "invalid command: expected object with function `execute()`", cause: err };
                throw e;
            }
        }

        commands[commandName] = command;
    }

    public function parseAndExecute(string[] args) returns any? {
        // parse args
        // 1) identify the command - if first arg is not a command throw an error (unknown command), default to the
        //      default command if no args specified (i.e., ballerina)
        // 2) look up expected options, dynamic options and params and create a map with the values, if conditions are
        //      not satisfied (required options/params not specified, cannot parse positional params to expected type,
        //      etc.)
        // 3) invoke the execute command for the particular command, passing the created map of args, and upon
        //      completion if there is a return value return it, or return
        if (lengthof args == 0) {
            // print usage
            BaseCommand defaultCmdToExec = check <BaseCommand> defCommand;
            return defaultCmdToExec.execute();
        } else {
            string potentialCommand = args[0].trim();
            if (!commands.hasKey(potentialCommand)) {
                error unknownCommandError = { message: "unknown command: " + potentialCommand };
                throw unknownCommandError;
            } else {
                any commandObject = commands[potentialCommand];

                map<string> requiredOptions;
                map<string> optionUniqueIdMap;

                map<CommandOption> cmdOptionMap = processOptions(commandObject, requiredOptions, optionUniqueIdMap);
                map<CommandDynamicOption> cmdDynamicOptionMap = processDynamicOptions(commandObject, requiredOptions,
                                                                                      optionUniqueIdMap);

                int[] requiredParamPositions;
                int[] assignedPositionalParams = [];

                map<CommandPositionalParam> cmdParamMap;
                var processedPositionalParams = processPositionalParams(commandObject);
                (requiredParamPositions, cmdParamMap) = processedPositionalParams;

                int argCount = lengthof args;

                int index = 1;
                while (index < argCount) {
                    string nextArg = args[index];
                    if (optionUniqueIdMap.hasKey(nextArg)) {
                        string uniqueId = optionUniqueIdMap[nextArg] but { () => "" };
                        if (cmdOptionMap.hasKey(uniqueId)) {
                            match (cmdOptionMap[uniqueId]) {
                                CommandOption cmdOption => {
                                    _ = requiredOptions.remove(uniqueId);
                                    // todo: need to special case boolean options
                                    if (cmdOption.fieldInfo.fieldType == boolean
                                        && ((index + 1 == lengthof args) // i.e, last arg - need to set to true
                                            || (!BOOLEAN_FALSE.equalsIgnoreCase(args[index + 1])))) {
                                        assignValue(commandObject, cmdOption.fieldInfo, "true");
                                        index++;
                                        continue;
                                    }
                                    index++;
                                    assignValue(commandObject, cmdOption.fieldInfo, args[index]);
                                }
                                () => {} //ignore
                            }
                        } else {
                            // it's a dynamic option
                            match (cmdDynamicOptionMap[uniqueId]) {
                                CommandDynamicOption cmdDynamicOption => {
                                    index++;
                                    addMapEntry(commandObject, cmdDynamicOption.fieldInfo, args[index]);
                                    _ = requiredOptions.remove(uniqueId);
                                }
                                () => {} //ignore
                            }
                        }
                    } else {
                        // the rest is all positional params
                        int remainingPositionalIndex = 0;
                        string[] remainingPositionalArray = [];
                        int assignedPositionalParamIndex = 0;
                        int positionaIndex = index;

                        while (positionaIndex < argCount) {
                            int positionalParamIndex = positionaIndex - index;
                            if (isInArray(requiredParamPositions, positionalParamIndex)) {
                                string keyString = <string> positionalParamIndex;
                                match (cmdParamMap[keyString]) {
                                    CommandPositionalParam cmdPositionalParam =>
                                                assignValue(commandObject, cmdPositionalParam.fieldInfo,
                                                            args[positionaIndex]);
                                    () => {}
                                }
                                assignedPositionalParams[assignedPositionalParamIndex] = positionalParamIndex;
                                assignedPositionalParamIndex++;
                            } else {
                                remainingPositionalArray[remainingPositionalIndex] = args[positionaIndex];
                                remainingPositionalIndex++;
                            }
                            positionaIndex++;
                        }
                        if (cmdParamMap.hasKey(ALL_REMAINING_ARGS_PARAM_KEY)) {
                            match (cmdParamMap[ALL_REMAINING_ARGS_PARAM_KEY]) {
                                CommandPositionalParam remParam => {
                                    any remParamFieldVal = check remParam.fieldInfo.getValue(commandObject);
                                    // only supports a string array, todo:support all
                                    match(remParamFieldVal) {
                                        string[] =>
                                            check remParam.fieldInfo.setValue(commandObject, remainingPositionalArray);
                                        any => {
                                            // shouldn't reach here
                                            error invalidPositionalParam =
                                            { message: "invalid type found for remaining args, expected: string[]" };
                                            throw invalidPositionalParam;
                                        }
                                    }
                                }
                                () => {}
                            }
                        }
                        index = argCount;
                    }
                    index++;
                }

                string unspecifiedRequiredOptionsAndParamsError = "";
                if (lengthof requiredOptions.keys() > 0) {
                    unspecifiedRequiredOptionsAndParamsError = "required option(s) not specified: ";
                    foreach key in requiredOptions.keys() {
                        unspecifiedRequiredOptionsAndParamsError = unspecifiedRequiredOptionsAndParamsError + key + " ";
                    }
                }

                if (lengthof requiredParamPositions > 0) {
                    string unspecifiedRequiredPositions = "";
                    foreach i in requiredParamPositions {
                        if (!isInArray(assignedPositionalParams, i)) {
                            unspecifiedRequiredPositions = unspecifiedRequiredPositions + <string> i + " ";
                        }
                    }
                    if (unspecifiedRequiredPositions != "") {
                        if (unspecifiedRequiredOptionsAndParamsError != "") {
                            unspecifiedRequiredOptionsAndParamsError =
                                unspecifiedRequiredOptionsAndParamsError.trim() + ", ";
                        }
                        unspecifiedRequiredOptionsAndParamsError = unspecifiedRequiredOptionsAndParamsError
                                                        + "required param values not specified for position(s): "
                            + unspecifiedRequiredPositions;
                    }
                }

                if (unspecifiedRequiredOptionsAndParamsError != "") {
                    error requiredOptionsNotSpecified = { message: unspecifiedRequiredOptionsAndParamsError };
                    throw requiredOptionsNotSpecified;
                }


                BaseCommand cmdToExec = check <BaseCommand> commandObject;
                return cmdToExec.execute();
            }
        }
    }

};

public type BaseCommand object {
    public function execute() returns any {
        // do nothing - place holder
        return;
    }
};

function assignValue(any cmdObject, reflect:FieldInfo fieldInfo, string value) {
    any fieldValue = check fieldInfo.getValue(cmdObject);
    match (fieldValue) {
        string => check fieldInfo.setValue(cmdObject, value);
        int => check fieldInfo.setValue(cmdObject, check <int> value);
        boolean => check fieldInfo.setValue(cmdObject, <boolean> value);
        float => check fieldInfo.setValue(cmdObject, check <float> value);
        byte => {
            int intvalue = check <int> value;
            check fieldInfo.setValue(cmdObject, check <byte> intvalue);
        }
        any[] => {
            string arrayValue;
            if (value.hasPrefix("[") && value.hasSuffix("]")) {
                arrayValue = value.substring(1, lengthof value - 1);
            } else {
                error arrError = { message: "Expected array notation (\"[a, b, c]\") for array typed option/param, " 
                                                + "found: " + value };
                throw arrError;
            }

            match (fieldValue) {
                string[] => check fieldInfo.setValue(cmdObject, arrayValue.split(COMMA));
                int[] => {
                    int[] intArray = [];
                    int arrIndex = 0;
                    foreach s in arrayValue.split(COMMA) {
                        intArray[arrIndex] = check <int> s;
                        arrIndex++;
                    }
                    validateArity();
                    check fieldInfo.setValue(cmdObject, intArray);
                }
                boolean[] => {
                    boolean[] booleanArray = [];
                    int arrIndex = 0;
                    foreach s in arrayValue.split(COMMA) {
                        booleanArray[arrIndex] = <boolean> s;
                        arrIndex++;
                    }
                    validateArity();
                    check fieldInfo.setValue(cmdObject, booleanArray);
                }
                float[] => {
                    float[] floatArray = [];
                    int arrIndex = 0;
                    foreach s in arrayValue.split(COMMA) {
                        floatArray[arrIndex] = check <float> s;
                        arrIndex++;
                    }
                    validateArity();
                    check fieldInfo.setValue(cmdObject, floatArray);
                }
                byte[] => {
                    byte[] byteArray = [];
                    int arrIndex = 0;
                    foreach s in arrayValue.split(COMMA) {
                        int intVal  = check <int> s;
                        byteArray[arrIndex] = check <byte> intVal;
                        arrIndex++;
                    }
                    validateArity();
                    check fieldInfo.setValue(cmdObject, byteArray);
                }
                any => {
                    error unsupportedType =
                        { message: "unsupported CLI option/param type for field: " + fieldInfo.identifier };
                    throw unsupportedType;
                }
            }
        }
        any => {
            error unsupportedType = { message: "unsupported CLI option/param type for field: " + fieldInfo.identifier };
            throw unsupportedType;
        }
    }
}

function addMapEntry(any cmdObject, reflect:FieldInfo fieldInfo, string value) {
    string[] keyValueEntry = value.split(EQUALS);
    if (lengthof keyValueEntry != 2) {
        //todo: handle > 1 "=" scenarios better
        error invalidMapEntryErr = { message: "invalid entry found for map type, expected key:value" };
        throw invalidMapEntryErr;
    }

    any|map valueMap = check fieldInfo.getValue(cmdObject); //workaround for map match issue #9705

    // todo: address key already exists scenarios
    match(valueMap) {
        map<string> strMap => {
            validateArity();
            strMap[keyValueEntry[0]] = keyValueEntry[1];
        }
        map<int> intMap => {
            validateArity();
            intMap[keyValueEntry[0]] = check <int> keyValueEntry[1];
        }
        map<boolean> booleanMap => {
            validateArity();
            booleanMap[keyValueEntry[0]] = <boolean> keyValueEntry[1];
        }
        map<float> floatMap => {
            validateArity();
            floatMap[keyValueEntry[0]] = check <float> keyValueEntry[1];
        }
        map<byte> byteMap => {
            validateArity();
            int intVal = check <int> keyValueEntry[1];
            byteMap[keyValueEntry[0]] = check <byte> intVal;
        }
        map<any> anyMap => {
            validateArity();
            anyMap[keyValueEntry[0]] = keyValueEntry[1]; //add as string for any constrained todo: revisit!
        }
        any => {
            error unsupportedType = { message: "unsupported CLI dynamic option type for field: "
                                                + fieldInfo.identifier } ;
            throw unsupportedType;
        }
    }
}

//todo:required fields - diff data structure?
function processOptions(any commandObject, map<string> requiredOptions, map<string> optionUniqueIdentifierMap)
             returns map<CommandOption> {

    map<CommandOption> commandOptionMap;

    reflect:AnnotatedFieldInfo[] annotatedOptions = reflect:getAnnotatedFieldInfo(commandObject, OptionConfig);

    foreach annotatedFieldInfo in annotatedOptions  {
        reflect:FieldInfo fieldInfo = <reflect:FieldInfo> annotatedFieldInfo.fieldInfo;
        reflect:annotationData annotData = <reflect:annotationData> annotatedFieldInfo.annotData;
        OptionConfig fieldOptionConfig = check <OptionConfig> annotData.value;

        string[] optionNames = fieldOptionConfig.names;

        if (lengthof optionNames == 0) {
            // Ideally shouldn't reach here --> CompilerPlugin?
            error unnamedOptionError = { message: "option cannot be specified without a name" };
            throw unnamedOptionError;
        }

        string uniqueId = addUniqueIdentifierEntries(optionUniqueIdentifierMap, optionNames);

        if (fieldOptionConfig.required) {
            requiredOptions[uniqueId] = uniqueId;
        }

        CommandOption cmdOption = { fieldInfo: fieldInfo, optionConfig: fieldOptionConfig };
        commandOptionMap[uniqueId] = cmdOption;
    }

    return commandOptionMap;
}

function isInArray(int[] intArray, int element) returns boolean {
    foreach i in intArray {
        if (i == element) {
            return true;
        }
    }
    return false;
}

function validateArity() {
    // TODO: impl.
}

function processDynamicOptions(any commandObject, map<string> requiredOptions, map<string> optionUniqueIdentifierMap)
             returns map<CommandDynamicOption> {

    map<CommandDynamicOption> commandDynamicOptionMap;

    reflect:AnnotatedFieldInfo[] annotatedOptions = reflect:getAnnotatedFieldInfo(commandObject, DynamicOptionConfig );

    foreach annotatedFieldInfo in annotatedOptions  {
        reflect:FieldInfo fieldInfo = <reflect:FieldInfo> annotatedFieldInfo.fieldInfo;
        reflect:annotationData annotData = <reflect:annotationData> annotatedFieldInfo.annotData;
        DynamicOptionConfig fieldOptionConfig = check <DynamicOptionConfig> annotData.value;

        string[] optionNames = fieldOptionConfig.names;

        if (lengthof optionNames == 0) {
            // Ideally shouldn't reach here --> CompilerPlugin?
            error unnamedOptionError = { message: "dynamic option cannot be specified without a name" };
            throw unnamedOptionError;
        }

        string uniqueId = addUniqueIdentifierEntries(optionUniqueIdentifierMap, optionNames);

        if (fieldOptionConfig.required) {
            requiredOptions[uniqueId] = uniqueId;
        }

        CommandDynamicOption cmdDynamicOption = { fieldInfo: fieldInfo, dynamicOptionConfig: fieldOptionConfig };
        commandDynamicOptionMap[uniqueId] = cmdDynamicOption;
    }

    return commandDynamicOptionMap;
}

function processPositionalParams(any commandObject) returns (int[], map<CommandPositionalParam>) {

    int[] requiredParamPositions = [];
    map<CommandPositionalParam> commandPositionalParamMap;

    reflect:AnnotatedFieldInfo[] annotatedOptions = reflect:getAnnotatedFieldInfo(commandObject, ParamConfig);

    int index = 0;
    foreach annotatedFieldInfo in annotatedOptions  {
        string id = ALL_REMAINING_ARGS_PARAM_KEY;

        reflect:FieldInfo fieldInfo = <reflect:FieldInfo> annotatedFieldInfo.fieldInfo;
        reflect:annotationData annotData = <reflect:annotationData> annotatedFieldInfo.annotData;
        ParamConfig fieldParamConfig = check <ParamConfig> annotData.value;

        match (fieldParamConfig.position) {
            int i => {
                id = <string> i;
                if (commandPositionalParamMap.hasKey(id)) {
                    // Again, shouldn't reach here
                    error err = { message: " > 1 positional params referring to the same position " };
                    throw err;
                }
                requiredParamPositions[index] = i;
            }
            () => {
                if (commandPositionalParamMap.hasKey(ALL_REMAINING_ARGS_PARAM_KEY)) {
                    // Again, shouldn't reach here
                    error err = { message: " > 1 positional params specified without position " };
                    throw err;
                }
            }
        }

        CommandPositionalParam cmdPositionalParam = { fieldInfo: fieldInfo, paramConfig: fieldParamConfig };
        commandPositionalParamMap[id] = cmdPositionalParam;

        index++;
    }

    return (requiredParamPositions, commandPositionalParamMap);
}

function addUniqueIdentifierEntries(map<string> uniqueIdentifierMap, string[] names) returns string {
    // Identify the unique identifier
    string optionUniqueId = names[0].trim();
    uniqueIdentifierMap[optionUniqueId] = optionUniqueId;

    // Add entries for other identifiers
    foreach i in 1 ..< lengthof names {
        uniqueIdentifierMap[names[i].trim()] = optionUniqueId;
    }

    return optionUniqueId;
}

type CommandOption record {
    reflect:FieldInfo fieldInfo;
    OptionConfig optionConfig;
};

type CommandDynamicOption record {
    reflect:FieldInfo fieldInfo;
    DynamicOptionConfig dynamicOptionConfig;
};

type CommandPositionalParam record {
    reflect:FieldInfo fieldInfo;
    ParamConfig paramConfig;
};
