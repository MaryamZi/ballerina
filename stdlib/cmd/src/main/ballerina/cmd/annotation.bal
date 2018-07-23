public type CommandConfig record {
    string name;
    string? description;
    !...
};

public annotation <type> Command CommandConfig;

public type OptionConfig record {
    string[] names;
    string? description;
    boolean hidden = false;
    boolean required = false;
    (int, int)? arity;
    !...
};

public annotation <type> Option OptionConfig;

public type DynamicOptionConfig record {
    string[] names;
    string? description;
    boolean hidden = false;
    boolean required = false;
    (int, int)? arity;
    !...
};

public annotation <type> DynamicOption DynamicOptionConfig;

public type ParamConfig record {
    int? position;
    string? description;
    boolean hidden = false;
    (int, int)? arity;
    !...
};

public annotation <type> PositionalParam ParamConfig;
