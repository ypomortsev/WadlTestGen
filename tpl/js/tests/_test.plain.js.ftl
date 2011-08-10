<#import "_test.common.js.ftl" as common>

<#-- Test method base that uses a plain old jQuery XHR call -->
<#macro test method resource>
<@common.test method=method resource=resource>
    expect(1);

    var url = endpoint + "${method.example.url?js_string}";

    <@common._test.params method=method resource=resource var="params"/>

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
</@common.test>
</#macro>