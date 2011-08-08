<#import "_util.js.ftl" as util>
<#import "_test.common.js.ftl" as common>

<#-- Test method base that uses a plain old jQuery XHR call -->
<#macro method method resource>
<@common.method method=method resource=resource>
    var url = endpoint + "${method.example.url?js_string}";

    <@util.methodParams method=method var="params"/>

    $.ajax({
         url: url,
         type: "${method.name}",

         success: function(data, textStatus, jqXHR) {
             ok(true, data);
         },
         error: function(jqXHR, textStatus, errorThrown) {
             ok(false, "Error: " + errorThrown);
         },
         complete: function(jqXHR, textStatus) { start(); },

         data: params,
         headers: {}
    });
</@common.method>
</#macro>