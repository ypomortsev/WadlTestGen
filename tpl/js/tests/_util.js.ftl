<#--
    Useful macros for converting WADL elements to JS
-->

<#-- Returns a formatted method documentation comment -->
<#macro methodDoc method>
<#if (method.sizeOfDocArray() > 0)>
/** <#compress>${method.getDocArray(0).getStringValue()}</#compress> */
</#if>
</#macro>

<#-- Returns a name for the method's testcase -->
<#macro methodName method>
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
<#macro methodParams method var>
    var ${var} = {
<#if method.request??>
<#list method.request.getParamArray() as param>
        <#if !param.required>// </#if>"${param.name}": "${param.getFixed()!param.getDefault()!"undefined"}"<#if param_has_next>,</#if>
</#list>
</#if>
    };
</#macro>
