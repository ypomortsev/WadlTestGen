<#macro test method resource>
<#--<#assign displayName = method["@apigee:displayName"][0]!method.@name>-->
asyncTest("${method.@name}", function() {
    <#--<#if method.doc??>/* <#compress>${method.doc}</#compress> */</#if>-->

    // TODO
});
</#macro>