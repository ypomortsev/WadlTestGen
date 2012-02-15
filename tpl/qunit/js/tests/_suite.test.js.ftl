<#--
  -- Helper macros for generating parts of test methods
  -->

<#-- Returns a formatted method documentation comment -->
<#macro _doc method>
<#if (method.sizeOfDocArray() > 0)>
/** <#compress>${method.getDocArray(0).getStringValue()}</#compress> */
</#if>
</#macro>

<#-- Returns a name for the method's testcase -->
<#macro _name method>
    <#escape x as x?js_string>
    <#if method.displayName?has_content>
        ${method.displayName}<#t>
    <#else>
        ${method.name}<#t>
    </#if>
    </#escape>
</#macro>

<#-- Returns a object variable with the method's and resource's request parameters
    TODO: separate query/template/header parameters
-->
<#macro _params method resource var>
    var ${var} = {
<#list resource.getParamArray() as resourceParam>
<#if resourceParam.getStyle().toString() == "query">
        <@_param param=resourceParam/><#if resourceParam_has_next>,</#if>
</#if>
</#list>
<#if method.request??>
<#list method.request.getParamArray() as methodParam>
        <@_param param=methodParam/><#if methodParam_has_next>,</#if>
</#list>
</#if>
    };
</#macro>

<#-- Returns a parameter as a key-value entry in an object -->
<#macro _param param>
<#if !param.required>// </#if>"${param.name}": "${param.getFixed()!param.getDefault()!"undefined"}"<#t>
</#macro>

<#--
  -- Test body
  -->

<#macro test method resource>
<@_doc method=method/>
asyncTest("<@_name method=method/>", function() {
    expect(1);

    var url = endpoint + "${method.example.url?js_string}";

    <@_params method=method resource=resource var="params"/>

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
});
</#macro>