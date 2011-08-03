<#macro test method resource>
<#--<#assign displayName = method["@apigee:displayName"][0]!method.@name>-->
asyncTest("${method.@name}", function() {
    <#--<#if method.doc??>/* <#compress>${method.doc}</#compress> */</#if>-->

    var sourceCall = new ComApigeeApiCallerQUnit(endPoint);

    var apiRequest = "${method.@url}";

    sourceCall.processCall = function (data, textStatus) {
        ok(false, "Unimplemented");
    };

    var callParams = {
        "callVerb" : "${method.@name}",
        "basicAuthCredentials": "Basic bWFyc2hAZWFydGgybWFyc2guY29tOnByb2R1Y3RvcHM=",
        "extraParams" : {
<#if method.request??>
<#list method.request.param as param>
            <#if param.@required == "false">// </#if>"${param.@name}": "${param.@fixed[0]!param.@default[0]!"undefined"}"<#if param_has_next>,</#if>
</#list>
</#if>
        }
    };

    sourceCall.callAPI(apiRequest, callParams);
});
</#macro>