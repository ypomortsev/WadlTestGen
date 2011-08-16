var SourceApplication = Class.extend({
    init: function(appName) {
        this.api = $.apigee_api('https://' + appName + '-api.apigee.com/v1');
        this.appName = appName;

        this.userObject = {};
        this.user_authenticated = false;
    },

    getAuth: function() {
        if (this.api && this.api.doesLocalStorage) this.logIn();
    },

    processSmartKey: function(data) {
        if (data && data.payload) {
            data = $.parseJSON(data.payload);
            this.setAuth(data);
            this.logIn();
        }
    },

    processKeyAndSecret: function(data) {
        if (data && data.payload) {
            data = $.parseJSON(data.payload);
            if (data.hasOwnProperty('oauthToken') && data.hasOwnProperty('oauthTokenSecret')) {
                this.setAuth(data);
                this.logIn();
            } else {
                this.showResponseMessage('Please re-try your login credentials or create a new account');
            }
        }
    },

    setAuth: function(authObject) {
        for (var key in authObject) {
            if (authObject.hasOwnProperty(key)) {
                this.userObject[key] = authObject[key];
                if (this.api && this.api.doesLocalStorage) localStorage[key] = authObject[key];
                if (key == 'smartKey') this.api.setSmartKey(authObject.smartKey);
            }
        }
    },

    logIn: function(username, password) {
        if (this.api) {
            var goodUser = true;
            var userInfo = [];

            if (username && password) {
                userInfo = [username, password];
            } else if (this.api.doesLocalStorage && localStorage.authorization) {
                userInfo = $.base64Decode(localStorage.authorization).split(':');
            }

            if (userInfo.length == 2) {
                this.setAuth({
                    'displayname': userInfo[0],
                    'username': userInfo[0],
                    'password': userInfo[1],
                    'authorization': $.base64Encode(userInfo[0] + ':' + userInfo[1])
                });

                this.api.init(userInfo[0], userInfo[1]);
            } else {
                this.showResponseMessage('Please include a username and a password.');
                goodUser = false;
            }

            if ((this.api.doesLocalStorage && (localStorage.smartkey || localStorage.smartKey)) || (this.userObject.smartKey || this.userObject.smartkey)) {
                var theSmartKey;
                if (this.api.doesLocalStorage && (localStorage.smartkey || localStorage.smartKey)) {
                    theSmartKey = (localStorage.smartkey) ? localStorage.smartkey : localStorage.smartKey;
                } else {
                    theSmartKey = (this.userObject.smartkey) ? this.userObject.smartkey : this.userObject.smartKey;
                }
                this.setAuth({'smartKey':theSmartKey,'smartkey':theSmartKey});
            } else if (this.api.authorization) {
                this.api.request('get', '/smartkeys/me.json?uid=' + this.api.authorization, {}, {callback: this.processSmartKey});
                goodUser = false;
            } else {
                this.showResponseMessage('Please re-try your login credentials or create a new account');
                goodUser = false;
            }

            if ((this.api.doesLocalStorage && (localStorage.oauthToken && localStorage.oauthTokenSecret)) || (this.userObject.oauthToken && this.userObject.oauthTokenSecret)) {
                var theToken, theSecret;
                if (this.api.doesLocalStorage && (localStorage.oauthToken && localStorage.oauthTokenSecret)) {
                    theToken = localStorage.oauthToken;
                    theSecret = localStorage.oauthTokenSecret;
                } else {
                    theToken = this.userObject.oauthToken;
                    theSecret = this.userObject.oauthTokenSecret;
                }
                this.setAuth({'oauthToken':theToken,'oauthTokenSecret':theSecret});
            } else if (this.userObject.smartKey) {
                this.api.request('get', '/smartkeys/' + this.userObject.smartKey + '/providers/twitter.json', {}, {callback:this.processKeyAndSecret});
                goodUser = false;
            } else {
                goodUser = false;
            }

            this.user_authenticated = goodUser;
        }
    },

    logOut: function() {
        var userCredentials = ['authorization'];

        for (var key in this.userObject) {
            if (this.userObject.hasOwnProperty(key)) {
                userCredentials.push(key);
            }
        }

        for (var i = 0; i < userCredentials.length; i++) {
            var thisCredential = userCredentials[i];
            if (this.api) {
                if (this.api.doesLocalStorage) localStorage.removeItem(thisCredential);
                if (this.api[thisCredential]) delete this.api[thisCredential];
                if (this.userObject[thisCredential]) delete this.userObject[thisCredential];
            }
        }

        this.user_authenticated = false;
    },

    createAccount: function(username, password) {
        if (this.api && username && password) {
            if (this.api.doesLocalStorage)
                localStorage.authorization = $.base64Encode(username + ':' + password);

            this.api.request(
                'post',
                '/users.json',
                {
                    'userName': username,
                    'fullName': username,
                    'password': password
                },
                { callback: this.newAccount }
            );
        } else {
            this.showResponseMessage('Please include a username and a password.');
        }
    },

    newAccount: function(data) {
        var responsePayload = ((typeof this.api.returnObject.payload) === "string") ? $.parseJSON(this.api.returnObject.payload) : this.api.returnObject.payload;
        if (responsePayload.hasOwnProperty('smartKey')) {
            if (this.api.doesLocalStorage) localStorage.smartkey = responsePayload.smartKey;
            var currentHref = document.location.href;
            document.location.href = this.api.defaults.endpoint + '/providers/twitter/authorize?smartkey=' + responsePayload.smartKey + '&app_callback=' + currentHref;
        }
    },

    sendRequest: function(verb, request, headers, settings) {
        verb = verb || "get";
        headers = headers || {};
        settings = settings || {};

        if (this.api && request) {
            if (this.api.smartkey && (verb == 'get')) {
                var smartKeyDivider = (request.indexOf('?') != -1) ? '&' : '?';
                request += (smartKeyDivider + 'smartkey=' + this.api.smartkey);
            }
            this.api.request(verb, request, headers, settings);
        }
    },

    /** @private */
    showResponseMessage: function(theMessage) {
        theMessage = theMessage.replace(/<\/?[^>]+>/gi, '');

        if (this.api && this.api.returnObject && this.api.returnObject.xhr && this.api.returnObject.xhr.responseText) {
            function parseAndReturn(theText) {
                var theJson = '';
                try {
                    theJson = $.parseJSON(theText);
                } catch (e) {
                    theJson = theText;
                }
                return theJson;
            }

            var responseMessage = parseAndReturn(this.api.returnObject.xhr.responseText);
            if (responseMessage.hasOwnProperty('message')) theMessage = responseMessage.message;
        }

        console.log(theMessage);
    }
});
