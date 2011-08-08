<#import "_test.${cfg.api.type}.js.ftl" as test>
<#list app.getResourcesArray() as resources>
/*
 * Configuration
 */

var resourcesBase = "${resources.getBase()?js_string}";

<#if cfg.proxy??>
var proxy = {
    enabled: ${cfg.proxy.enabled?string},
    base: "${cfg.proxy.base?js_string}"
};

var endpoint = proxy.enabled ? (proxy.base + resourcesBase) : resourcesBase;
<#else>
var endpoint = resourcesBase;
</#if>

/*
 * Tests
 */

<#list resources.getResourceArray() as resource>
module("${resource.getPath()}");

<#list resource.getMethodArray() as method>
<@test.method method=method resource=resource/>

</#list>
</#list>
</#list>
