<#import "_test.common.js.ftl" as common>

<#-- Test method base that uses Apigee Source to access the API -->
<#macro test method resource>
<@common.test method=method resource=resource>
    var url = "/twitter/1${method.example.url?js_string}";

    <@common.params method=method var="params"/>

    sourceApp.sendRequest("${method.name?lower_case}", url, params, {
        callback: function(response) {
            if (response.payload)
                ok(true, "Success!");
            else
                ok(false, response.response_message);

            start();
        }
    });
</@common.test>
</#macro>
