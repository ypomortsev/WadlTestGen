/**
  * This library depends on jQuery and the base64 jQuery plugin (http://plugins.jquery.com/project/base64)
*/

/**
  * Accepts the endpoint (ex: "http://user.apigee.com/") and builds the API Caller object
*/
function ComApigeeApiCallerQUnit(endPoint) {
    var theCall = this;
      /**
      * Takes an API Request (ex: "statuses/public_timeline.json") and an optional array of specified call parameters.
      * theCall's rawResponse is set to the raw JSON object response.
      * If a DOM container ID was provided, response is also sent through processCall.
    */
    this.callAPI = function (apiRequest, callParams) {
        if (!apiRequest) {
            var apiRequest = ""
        }
        if (!callParams) {
            var callParams = {}
        }
        if (!callParams.callVerb) {
            callParams.callVerb = "GET"
        }
        if (!callParams.basicAuthCredentials) {
            callParams.basicAuthCredentials = null
        }
        var authArg = null;
        if (callParams.basicAuthCredentials == null) {
            var callAuth = new ComApigeeAuthHandler();
            authArg = "Basic " + $.base64Encode(callAuth.userName + ":" + callAuth.userPass);
            if (callAuth.userName == null || callAuth.userName == "" || callAuth.userName == "null" || callAuth.userPass == null || callAuth.userPass == "" || callAuth.userPass == "null") {
                return
            }
        } else {
            authArg = callParams.basicAuthCredentials
        }
        if (!callParams.includeElements) {
            callParams.includeElements = {}
        }
        var urlParams = {
            apigee_auth: authArg,
            apigee_verb: callParams.callVerb,
            apigee_testbed: window.location.hostname,
            callback: '_jqjsp'
        };
        if (callParams.extraParams) {
            $.extend(true, urlParams, callParams.extraParams)
        }

        $.jsonp({
            url: endPoint + apiRequest,
            cache: false,
            data: urlParams,
            success: function (json, textStatus) {
                theCall.rawResponse = json;
                theCall.processCall(json, textStatus);
                start();
            },
            error: function (xOptions, textStatus) {
                ok(false, "Request error: " + textStatus);
                start();
            }
        });
    };
    /**
      * Filters response data based on submitted prototype object (callParams.includeElements)
    */
    this.pruneResponse = function (rawData) {
        var prunedData = {};
        var pruneLoop = function (rawObject, protoObject, prunedObject) {
                for (var rawKey in rawObject) {
                    if (rawObject.hasOwnProperty(rawKey) && protoObject.hasOwnProperty(rawKey)) {
                        var rawVal = rawObject[rawKey];
                        var protoVal = protoObject[rawKey];
                        if ((typeof (protoVal) === "object") && protoVal !== null) {
                            prunedObject[rawKey] = {};
                            pruneLoop(rawVal, protoVal, prunedObject[rawKey])
                        } else {
                            prunedObject[rawKey] = rawVal
                        }
                    }
                }
            };
        for (var key in rawData) {
            if (rawData.hasOwnProperty(key)) {
                prunedData[key] = {};
                pruneLoop(rawData[key], callParams.includeElements, prunedData[key])
            }
        }
        return prunedData
    };
    /**
      * Takes the request data, an optional DOM ID of the target container, and an HTML construct (ul, ol, div, or dl).
      * setConstruct and iterateData format the response; the target container is populated accordingly.
    */
    this.processCall = function (data, textStatus, jqXHR) {
        ok(true, "received data" + data);
        start();
    };
}

/**
  * Handles and stores authentication information.
*/
function ComApigeeAuthHandler() {
    var theHandler = this;
    this.userName = "";
    this.userPass = "";
    /**
      * Checks to see if the browser supports localStorage; returns true or false accordingly.
    */
    this.checkLocalStorage = function () {
        try {
            return "localStorage" in window && window.localStorage !== null
        } catch (e) {
            return false
        }
    };
    this.doesLocalStorage = this.checkLocalStorage();
    /**
      * If the browser supports localStorage and the credentials exist in LS, confirms that the user wants to keep using these, and either sets userName and userPass accordingly or calls getCredentials to query for them.
      * If the browser does not support localStorage (or the credentials do not exist), call getCredentials to query for them.
    */
    this.init = function () {
        var doSet = true;
        if (theHandler.doesLocalStorage) {
            if (localStorage.userName && localStorage.userName != null && localStorage.userName != "null" && localStorage.userPass && localStorage.userPass != null && localStorage.userPass != "null") {
                theHandler.userName = localStorage.userName;
                theHandler.userPass = localStorage.userPass;
                // always use existing credentials so we don't get prompted for every test
                // doSet = !(confirm('Existing Apigee credentials ("' + theHandler.userName + '") detected.\nUse these?'))
                doSet = false;
            }
        }
        if (doSet) {
            this.getCredentials()
        }
    };
    /**
      * Prompts for username and password.
      * Passes prompted data into setCredentials.
    */
    this.getCredentials = function () {
        theHandler.userName = prompt("Log into your Apigee Source account.\nEmail:", theHandler.userName);
        theHandler.userPass = prompt("Password:", theHandler.userPass);
        this.setCredentials()
    };
    /**
      * If the browser supports localStorage, store the prompted data.
    */
    this.setCredentials = function () {
        if (theHandler.doesLocalStorage && theHandler.userName != null && theHandler.userName != "null" && theHandler.userPass != null && theHandler.userPass != "null") {
            localStorage.userName = theHandler.userName;
            localStorage.userPass = theHandler.userPass
        }
    };
    this.init()
}
