<#-- Macros for generating parts of test methods -->

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

<#-- Returns a object variable with the method's and resource's request parameters
    TODO: separate query/template/header parameters
-->
<#macro params method resource var>
    var ${var} = {
<#list resource.getParamArray() as resourceParam>
<#if resourceParam.getStyle().toString() == "query">
        <@param param=resourceParam/><#if resourceParam_has_next>,</#if>
</#if>
</#list>
<#if method.request??>
<#list method.request.getParamArray() as methodParam>
        <@param param=methodParam/><#if methodParam_has_next>,</#if>
</#list>
</#if>
    };
</#macro>

<#-- Returns a parameter as a key-value entry in an object -->
<#macro param param>
<#if !param.required>// </#if>"${param.name}": "${param.getFixed()!param.getDefault()!"undefined"}"<#t>
</#macro>