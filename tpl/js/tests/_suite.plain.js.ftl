<#import "_suite.common.js.ftl" as common>
<#list app.getResourcesArray() as resources>
/*
 * Configuration
 */

var resourcesBase = "${resources.getBase()?js_string}";

<#if cfg.api.proxy??>
var proxy = {
    enabled: ${cfg.api.proxy.enabled?string},
    base: "${cfg.api.proxy.base?js_string}"
};

var endpoint = proxy.enabled ? (proxy.base + resourcesBase) : resourcesBase;
<#else>
var endpoint = resourcesBase;
</#if>

/*
 * Tests
 */

<@common.tests type=cfg.api.type resources=resources/>

</#list>