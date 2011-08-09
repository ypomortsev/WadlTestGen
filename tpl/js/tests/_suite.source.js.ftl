<#import "_suite.common.js.ftl" as common>
<#list app.getResourcesArray() as resources>
/*
 * Configuration
 */

var source = {
<#escape x as x?js_string>
    appName: "${cfg.api.source.appName}",
    username: "${cfg.api.source.username}",
    password: "${cfg.api.source.password}"
</#escape>
};

var sourceApp;

QUnit.begin = function() {
    sourceApp = new SourceApplication(source.appName);
    sourceApp.logIn(source.username, source.password);
};

/*
 * Tests
 */

<@common.tests type=cfg.api.type resources=resources/>

</#list>