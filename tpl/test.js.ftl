<#ftl ns_prefixes={
    "D":"http://wadl.dev.java.net/2009/02",
    "apigee":"http://api.apigee.com/wadl/2010/07/"}
    strip_text=true>

<#list wadl.application.resources as resources>
// Resources ${resources.@base}

var endPoint = "${tpl.endpoint}";

<#list resources.resource as resource>
module("${resource.@path}");

<#list resource.method as method>
<@test method=method resource=resource/>
</#list>
</#list>
</#list>

<#macro test method resource>
<#assign displayName = method["@apigee:displayName"][0]!method.@name >
asyncTest("${displayName}", function() {
    <#if method.doc??>/* <#compress>${method.doc}</#compress> */</#if>

    var sourceCall = new ComApigeeApiCallerQUnit(endPoint);

    var apiRequest = "${method["apigee:example"].@url}";

    // response handling
    sourceCall.processCall = function (data, textStatus) {
        ok(false, "Unimplemented");
    };

    var callParams = {
        "callVerb" : "${method.@name}",
        "basicAuthCredentials": "Basic bWFyc2hAZWFydGgybWFyc2guY29tOnByb2R1Y3RvcHM=",
        "extraParams" : {
<#if method.request?exists>
<#list method.request.param as param>
            <#if param.@required == "false">// </#if>"${param.@name}": "${param.@fixed[0]!param.@default[0]!"undefined"}"<#if param_has_next>,</#if>
</#list>
</#if>
        }
    };

    sourceCall.callAPI(apiRequest, callParams);
});

</#macro>
