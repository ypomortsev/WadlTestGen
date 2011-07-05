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
  * Excepts:
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
        var returnObject = {};
        var verb = verb || theApi.defaults.verb;
        var headers = headers || {};
        if (theApi.smartkey) headers = $.extend(true, headers, {"smartkey":theApi.smartkey});
        var settings = $.extend({}, theApi.defaults, settings);
        var fullRequest = request.split(".")
        var request = fullRequest[0];
        if (fullRequest.length > 1) settings.type = fullRequest[1];
        if (settings.type === 'jsonp') settings.type = 'json';
        $.ajax({
          url: settings.endpoint+request+'.'+settings.type.split(" ")[0],
          headers: headers,
          data: headers,
          type: settings.verb,
          dataType: settings.type,
          success: function(data,textStatus,jqXHR) {
            console.log(textStatus);
            returnObject.response_message = textStatus;
            returnObject.payload = data;
            returnObject.xhr = jqXHR;
            if (settings.callback) {
              var callbackFunction = new Function(settings.callback+'(\''+data+'\')');
              callbackFunction();
            }
          },
          error: function(jqXHR, textStatus, errorThrown) {
            responseMessage = textStatus+" ("+errorThrown+")";
            console.log(responseMessage);
            returnObject.response_message = responseMessage;
            returnObject.xhr = jqXHR;
          }
        });
        return returnObject;
      }
    }
/**
  * base64-encodes username and password and makes request for smartkey.  If response is successful, adds this to the ApigeeAPI object, which in turn adds it as a header to each subsequent request
*/
    this.init = function(username,password) {
      var username = username || "";
      var password = password || "";
      $.ajax({
        url: "./sample.json",
        headers: {"auth":$.base64Encode(username+':'+password)},
        type: "get",
        dataType: "json",
        success: function(data,textStatus,jqXHR) {
          if (data.smartkey) theApi.smartkey = data.smartkey;
        }
      });
    };
    if (username && password) this.init(username,password);
  }

  $.apigee_api = function(endpoint,username,password) {
    return new ApigeeApi(endpoint || false,username || false,password || false);
  }

})(jQuery);