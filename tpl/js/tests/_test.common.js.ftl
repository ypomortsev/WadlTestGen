<#-- Testcase body that is common to both plain and Source tests -->
<#macro test method resource>
<@doc method=method/>
asyncTest("<@name method=method/>", function() {
<#nested>
});
</#macro>

<#--
    Macros for generating parts of test methods
-->

<#-- Returns a formatted method documentation comment -->
<#macro doc method>
<#if (method.sizeOfDocArray() > 0)>
/** <#compress>${method.getDocArray(0).getStringValue()}</#compress> */
</#if>
</#macro>

<#-- Returns a name for the method's testcase -->
<#macro name method>
    <#escape x as x?js_string>
    <#if method.displayName?has_content>
        ${method.displayName}<#t>
    <#else>
        ${method.name}<#t>
    </#if>
    </#escape>
</#macro>

<#-- Returns a object variable with the method's request parameters
    TODO: separate query/template/header parameters
-->
<#macro params method var>
    var ${var} = {
<#if method.request??>
<#list method.request.getParamArray() as param>
        <#if !param.required>// </#if>"${param.name}": "${param.getFixed()!param.getDefault()!"undefined"}"<#if param_has_next>,</#if>
</#list>
</#if>
    };
</#macro>
