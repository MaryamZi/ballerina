import ballerina.net.http;

@Description {value:"BasePath attribute associates a path to the service."}
@http:configuration {basePath:"/foo"}
service<http> echo {
    @Description {value:"Post annotation restricts the resource only to accept post requests. Similarly, for each HTTP verb there are different annotations."}
    @Description {value:"Path attribute associates a sub-path to the resource."}
    @http:resourceConfig {
        methods:["POST"],
        path:"/bar"
    }
    resource echo (http:Connection conn, http:Request req) {
        // A util method that can get the request payload.
        var payload, payloadError = req.getJsonPayload();
        http:Response res = {};
        if (payloadError == null) {
            res.setJsonPayload(payload);
        } else {
            res = {statusCode:500};
            res.setStringPayload(payloadError.message);
        }

        // Reply to the client with the response.
        _ = conn.respond(res);
    }
}
