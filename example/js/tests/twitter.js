// Resources http://api.twitter.com/1

var endPoint = "https://demo-api.apigee.com/twitter";

module("help/test.json");

asyncTest("GET", function() {
    /* Returns the string "ok" in the requested format with a 200 OK HTTP status code. */

    var sourceCall = new ComApigeeApiCallerQUnit(endPoint);

    var apiRequest = "/help/test.json";

    // response handling
    sourceCall.processCall = function (data, textStatus) {
        same(data, "ok", 'Response data should be "ok"');
    };

    var callParams = {
        "callVerb" : "GET",
        "basicAuthCredentials": "Basic bWFyc2hAZWFydGgybWFyc2guY29tOnByb2R1Y3RvcHM=",
        "extraParams" : {
        }
    };

    sourceCall.callAPI(apiRequest, callParams);
});
