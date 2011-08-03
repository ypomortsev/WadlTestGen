<#include "_test.source.js.ftl">

<#--<#ftl ns_prefixes={
    "D":"http://wadl.dev.java.net/2009/02",
    "apigee":"http://api.apigee.com/wadl/2010/07/"}
    strip_text=true>-->

<#list wadl.resources as resources>
<#--// Resources ${resources.@base}

var endPoint = "${tpl.endpoint}";-->

<#list resources.resource as resource>
module("${resource.@path}");

<#list resource.method as method>
<@test method=method resource=resource/>

</#list>

</#list>
</#list>
