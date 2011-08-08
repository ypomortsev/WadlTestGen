<#import "_util.js.ftl" as util>
<#import "_test.common.js.ftl" as common>

<#-- Test method base that uses Apigee Source to access the API -->
<#macro method method resource>
<@common.method method=method resource=resource>
    var url = "${method.example.url?js_string}";

    <@util.methodParams method=method var="params"/>

    var sourceCall = new ComApigeeApiCallerQUnit("${cfg.tpl.endpoint?js_string}");

    sourceCall.processCall = function (data, textStatus) {
        ok(false, "Unimplemented");
    };

    var callParams = {
        "callVerb" : "${method.name}",
        "basicAuthCredentials": "Basic bWFyc2hAZWFydGgybWFyc2guY29tOnByb2R1Y3RvcHM=",
        "extraParams": params
    };

    sourceCall.callAPI(url, callParams);
</@common.method>
</#macro>
