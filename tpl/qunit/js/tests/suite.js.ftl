<#import "_suite.test.js.ftl" as test>

<#list app.getResourcesArray() as resources>
/*
 * Configuration
 */

var endpoint = "${resources.getBase()?js_string}";

/*
 * Tests
 */

<#list resources.getResourceArray() as resource>
module("${resource.getPath()}");

<#list resource.getMethodArray() as method>
<@test.test method=method resource=resource/>

</#list>
</#list>

</#list>