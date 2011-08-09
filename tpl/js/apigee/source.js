function SourceApplication(appName) {
    var theApp = this;
    this.userObject = {};
    this.user_authenticated = false;

    this.getAuth = function() {
        if (theApp.api && theApp.api.doesLocalStorage) theApp.logIn();
    };

    this.processSmartKey = function(data) {
        if (data && data.payload) {
            data = $.parseJSON(data.payload);
            theApp.setAuth(data);
            theApp.logIn();
        }
    };

    this.processKeyAndSecret = function(data) {
        if (data && data.payload) {
            data = $.parseJSON(data.payload);
            if (data.hasOwnProperty('oauthToken') && data.hasOwnProperty('oauthTokenSecret')) {
                theApp.setAuth(data);
                theApp.logIn();
            } else {
                theApp.showResponseMessage('Please re-try your login credentials or create a new account');
            }
        }
    };

    this.setAuth = function(authObject) {
        for (var key in authObject) {
            if (authObject.hasOwnProperty(key)) {
                theApp.userObject[key] = authObject[key];
                if (theApp.api && theApp.api.doesLocalStorage) localStorage[key] = authObject[key];
                if (key == 'smartKey') theApp.api.setSmartKey(authObject.smartKey);
            }
        }
    };

    this.logIn = function(username, password) {
        if (theApp.api) {
            var goodUser = true;
            var userInfo = [];

            if (username && password) {
                userInfo = [username, password];
            } else if (theApp.api.doesLocalStorage && localStorage.authorization) {
                userInfo = $.base64Decode(localStorage.authorization).split(':');
            }

            if (userInfo.length == 2) {
                theApp.setAuth({
                    'displayname': userInfo[0],
                    'username': userInfo[0],
                    'password': userInfo[1],
                    'authorization': $.base64Encode(userInfo[0] + ':' + userInfo[1])
                });

                theApp.api.init(userInfo[0], userInfo[1]);
            } else {
                theApp.showResponseMessage('Please include a username and a password.');
                goodUser = false;
            }

            if ((theApp.api.doesLocalStorage && (localStorage.smartkey || localStorage.smartKey)) || (theApp.userObject.smartKey || theApp.userObject.smartkey)) {
                var theSmartKey;
                if (theApp.api.doesLocalStorage && (localStorage.smartkey || localStorage.smartKey)) {
                    theSmartKey = (localStorage.smartkey) ? localStorage.smartkey : localStorage.smartKey;
                } else {
                    theSmartKey = (theApp.userObject.smartkey) ? theApp.userObject.smartkey : theApp.userObject.smartKey;
                }
                theApp.setAuth({'smartKey':theSmartKey,'smartkey':theSmartKey});
            } else if (theApp.api.authorization) {
                theApp.api.request('get', '/smartkeys/me.json?uid=' + theApp.api.authorization, {}, {callback: theApp.processSmartKey});
                goodUser = false;
            } else {
                theApp.showResponseMessage('Please re-try your login credentials or create a new account');
                goodUser = false;
            }

            if ((theApp.api.doesLocalStorage && (localStorage.oauthToken && localStorage.oauthTokenSecret)) || (theApp.userObject.oauthToken && theApp.userObject.oauthTokenSecret)) {
                var theToken, theSecret;
                if (theApp.api.doesLocalStorage && (localStorage.oauthToken && localStorage.oauthTokenSecret)) {
                    theToken = localStorage.oauthToken;
                    theSecret = localStorage.oauthTokenSecret;
                } else {
                    theToken = theApp.userObject.oauthToken;
                    theSecret = theApp.userObject.oauthTokenSecret;
                }
                theApp.setAuth({'oauthToken':theToken,'oauthTokenSecret':theSecret});
            } else if (theApp.userObject.smartKey) {
                theApp.api.request('get', '/smartkeys/' + theApp.userObject.smartKey + '/providers/twitter.json', {}, {callback:theApp.processKeyAndSecret});
                goodUser = false;
            } else {
                goodUser = false;
            }

            theApp.user_authenticated = goodUser;
        }
    };

    this.logOut = function() {
        var userCredentials = ['authorization'];

        for (var key in theApp.userObject) {
            if (theApp.userObject.hasOwnProperty(key)) {
                userCredentials.push(key);
            }
        }

        for (var i = 0; i < userCredentials.length; i++) {
            var thisCredential = userCredentials[i];
            if (theApp.api) {
                if (theApp.api.doesLocalStorage) localStorage.removeItem(thisCredential);
                if (theApp.api[thisCredential]) delete theApp.api[thisCredential];
                if (theApp.userObject[thisCredential]) delete theApp.userObject[thisCredential];
            }
        }

        theApp.user_authenticated = false;
    };

    this.createAccount = function(username, password) {
        if (theApp.api && username && password) {
            if (theApp.api.doesLocalStorage)
                localStorage.authorization = $.base64Encode(username + ':' + password);

            theApp.api.request(
                'post',
                '/users.json',
                {
                    'userName': username,
                    'fullName': username,
                    'password': password
                },
                { callback: theApp.newAccount }
            );
        } else {
            theApp.showResponseMessage('Please include a username and a password.');
        }
    };

    this.newAccount = function(data) {
        var responsePayload = ((typeof theApp.api.returnObject.payload) === "string") ? $.parseJSON(theApp.api.returnObject.payload) : theApp.api.returnObject.payload;
        if (responsePayload.hasOwnProperty('smartKey')) {
            if (theApp.api.doesLocalStorage) localStorage.smartkey = responsePayload.smartKey;
            var currentHref = document.location.href;
            document.location.href = theApp.api.defaults.endpoint + '/providers/twitter/authorize?smartkey=' + responsePayload.smartKey + '&app_callback=' + currentHref;
        }
    };

    this.sendRequest = function(verb, request, headers, settings) {
        verb = verb || "get";
        headers = headers || {};
        settings = settings || {};

        if (theApp.api && request) {
            if (theApp.api.smartkey && (verb == 'get')) {
                var smartKeyDivider = (request.indexOf('?') != -1) ? '&' : '?';
                request += (smartKeyDivider + 'smartkey=' + theApp.api.smartkey);
            }
            theApp.api.request(verb, request, headers, settings);
        }
    };

    /** @private */
    this.showResponseMessage = function(theMessage) {
        theMessage = theMessage.replace(/<\/?[^>]+>/gi, '');

        if (theApp.api && theApp.api.returnObject && theApp.api.returnObject.xhr && theApp.api.returnObject.xhr.responseText) {
            function parseAndReturn(theText) {
                var theJson = '';
                try {
                    theJson = $.parseJSON(theText);
                } catch (e) {
                    theJson = theText;
                }
                return theJson;
            }

            var responseMessage = parseAndReturn(theApp.api.returnObject.xhr.responseText);
            if (responseMessage.hasOwnProperty('message')) theMessage = responseMessage.message;
        }

        console.log(theMessage);
    };

    this.init = function(appName) {
        theApp.api = $.apigee_api('https://' + appName + '-api.apigee.com/v1');
        theApp.appName = appName;
    };

    if (appName) theApp.init(appName);
}
