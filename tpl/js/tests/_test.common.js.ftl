<#import "_test.common.test.js.ftl" as _test>

<#-- Testcase body that is common to both plain and Source tests -->
<#macro test method resource>
<@_test.doc method=method/>
asyncTest("<@_test.name method=method/>", function() {
<#nested>
});
</#macro>
