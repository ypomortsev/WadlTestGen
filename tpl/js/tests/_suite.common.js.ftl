<#-- Common test suite body. Uses the specified test type -->
<#macro tests type resources>
<#import "_test.${type}.js.ftl" as test>
<#list resources.getResourceArray() as resource>
module("${resource.getPath()}");

<#list resource.getMethodArray() as method>
<@test.test method=method resource=resource/>

</#list>
</#list>
</#macro>
