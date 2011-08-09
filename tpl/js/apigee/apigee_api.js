/**
  * This library depends on jQuery and the base64 jQuery plugin (http://plugins.jquery.com/project/base64)
  * Sample Usage:
  * Working with the Apigee API
  * - Setting up an Apigee account:
  * var apigee = new $.apigee_api('https://api.apigee.com');
  * apigee.init("earth2marsh","supersecret"); // pwd will be base64 encoded
  *
  * - Adding an application:
  * apigee.request('post','/apps/myappname.json');
  *
  * Working with a proxied API
  * - Set up the app object:
  * var myapp = $.apigee_api('endpoint');
  *
  * - Casting an app to a user:
  * myapp.init("myappuser","myappuserpwd"); // base64 encode pwd
  *
  * - Making a request:
  * function renderTimeline(data) {
  *   // do clever things with that data, assume for now that rendering is outside the scope of the library
  * }
  * myapp.request('get','/1/statuses/home_timeline.json',{},{callback:'renderTimeline'}); // callback to custom function
*/

(function($) {
/**
  * Creates API object
  * Optionally accepts:
  * - endpoint
  * - username
  * - password
*/
  function ApigeeApi(endpoint,username,password) {
    var theApi = this;
/**
  * Default verb, response type, and endpoint; can be overridden by passing in settings to request
*/
    this.defaults = {
      verb:"get",
      type:"json",
      endpoint:endpoint || "https://api.apigee.com"
    };
/**
  * Makes request to endpoint + request
  * Accepts:
  * - verb (if not provided, defaults to the object's default verb (typically "get")
  * - request (NOT optional; request will not be made if not provided)
  * - headers (optional object - these are passed in as request headers or query parameters, depending on verb)
  * - settings (optional object - this allows defaults to be overridden, callback function to be provided, etc.)
  * Sample Request:
  * - (assuming "thisApi" is the name of an instance of the ApigeeApi object, and also that there is a function called "renderTimeline" that expects the response data as a parameter)
  * - thisApi.request('get','/1/statuses/home_timeline.json',{sort:'ascending'},{callback:'renderTimeline'});
*/
    this.request = function(verb,request,headers,settings) {
      if (request) {
        var request = encodeURI(request);
        var returnObject = {};
        var verb = verb || theApi.defaults.verb;
        var headers = headers || {};
        var requestArray = request.toString().split("?");
        var requestHeaders = (requestArray.length > 1) ? requestArray[1].split('&') : false;
        if (requestHeaders) {
          var extraHeaders = {
            'X-PINGOTHER':'pingpong',
                    'X-Requested-With':'XMLHttpRequest'
          };
          for (var i=0; i<requestHeaders.length; i++) {
            var thisPair = requestHeaders[i].split('=');
            if (thisPair.length > 1) {
              extraHeaders[thisPair[0]] = requestHeaders[i].substring(thisPair[0].length + 1);
            }
          }
          $.extend(extraHeaders, headers);
          headers = extraHeaders;
        }
        if (theApi.smartkey && (verb != 'get')) headers = $.extend(true, headers, {"smartkey":theApi.smartkey});
        if (theApi.authorization && (verb != 'get')) headers = $.extend(true, headers, {"Authorization":"Basic "+theApi.authorization});
        var settings = $.extend({}, theApi.defaults, settings);
        settings.verb = verb;
        var fullRequest = request.toString().split(".")
        request = fullRequest[0];
        if (fullRequest.length > 1) settings.type = fullRequest[1];
        if (settings.type === 'jsonp') settings.type = 'json';
        if (settings.popnewwin && (settings.popnewwin === 'true')) window.open(settings.endpoint+request+'.'+settings.type.split(" ")[0]);
        if (settings.verb === 'get') {
          $.ajax({
            url: settings.endpoint+request+'.'+settings.type.split(" ")[0],
            type: 'get',
            dataType: 'text',
            async: false,
            xhrFields: {
              withCredentials: true
            },
            beforeSend: function(xhr) {
              if (theApi.authorization && headers.Authorization) xhr.setRequestHeader('Authorization',headers.Authorization);
            },
            success: function(data,textStatus,jqXHR) {
              returnObject.response_message = textStatus;
              returnObject.payload = data;
              returnObject.xhr = jqXHR;
              theApi.returnObject = returnObject;
              if (settings.callback) settings.callback(returnObject);
              $.after_request();
            },
            error: function(jqXHR, textStatus, errorThrown) {
              responseMessage = textStatus+" ("+errorThrown+")";
              returnObject.response_message = responseMessage;
              returnObject.xhr = jqXHR;
              theApi.returnObject = returnObject;
              if (settings.callback) settings.callback(returnObject);
              $.after_request();
            }
          });
        } else {
          $.ajax({
            url: settings.endpoint+request+'.'+settings.type.split(" ")[0],
            headers: headers,
            data: JSON.stringify(headers),
            type: settings.verb,
            dataType: settings.type,
            contentType: "application/"+settings.type,
            async: false,
            xhrFields: {
              withCredentials: true
            },
            success: function(data,textStatus,jqXHR) {
              returnObject.response_message = textStatus;
              returnObject.payload = data;
              returnObject.xhr = jqXHR;
              theApi.returnObject = returnObject;
              if (settings.callback) settings.callback(returnObject);
              $.after_request();
            },
            error: function(jqXHR, textStatus, errorThrown) {
              responseMessage = textStatus+" ("+errorThrown+")";
              returnObject.response_message = responseMessage;
              returnObject.xhr = jqXHR;
              theApi.returnObject = returnObject;
              if (settings.callback) settings.callback(returnObject);
              $.after_request();
            }
          });
        }
        return returnObject;
      }
    }
/**
  * check if environment supports localStorage
*/
    this.checkLocalStorage = function () {
      try {
        return (('localStorage' in window) && (window.localStorage !== null));
      } catch (e) {
        return false;
      }
    }
    this.doesLocalStorage = this.checkLocalStorage();
/**
  * set theApi's smartkey, and, if supported, add smartkey to local storage
*/
    this.setSmartKey = function(theSmartKey) {
      theApi.smartkey = theSmartKey;
      if (theApi.doesLocalStorage) localStorage.smartkey = theApi.smartkey;
    }
/**
  * base64-encodes username and password, adds this to the ApigeeAPI object, which in turn adds it as a header to each subsequent request
*/
    this.init = function(username,password) {
      var username = username || "";
      var password = password || "";
      var authParam = $.base64Encode(username+':'+password);
      theApi.authorization = authParam;
    };
    var uid = getUrlParam('uid');
    if (username && password) {
      this.init(username,password);
    } else if (uid) {
      theApi.authorization = uid;
    } else if (this.doesLocalStorage && localStorage.smartkey) {
      theApi.setSmartKey(localStorage.smartkey);
    }
  }

  function getUrlParam(paramName) {
    var paramValue = false;
    var urlArray = document.location.href.toString().split("?");
    if (urlArray.length > 1) {
      var paramArray = urlArray[1].toString().split("&");
      for (var i=0; i<paramArray.length; i++) {
        var thisParamSet = paramArray[i].toString().split("=");
        if (thisParamSet[0] == paramName) {
          paramValue = paramArray[i].substring(thisParamSet[0].length + 1);
          break;
        }
      }
    }
    return paramValue;
  }

  $.after_request = function() {}

  $.apigee_api = function(endpoint,username,password) {
    return new ApigeeApi(endpoint || false,username || false,password || false);
  }

})(jQuery);
