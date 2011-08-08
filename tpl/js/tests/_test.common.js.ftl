<#import "_util.js.ftl" as util>

<#-- Testcase body that is common to both plain and Source tests -->
<#macro method method resource>
<@util.methodDoc method=method/>
asyncTest("<@util.methodName method=method/>", function() {
<#nested>
});
</#macro>