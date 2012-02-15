<#import "_suite.coffee.helpers.ftl" as test>

vows = require 'vows'
assert = require 'assert'
APIeasy = require 'api-easy'

<#list app.getResourcesArray() as resources><#-- FIXME: multiple resources -->
suite = APIeasy.describe '${resources.getBase()?js_string}'

suite
  .use('localhost', 8000) # FIXME!!!!
<#list resources.getResourceArray() as resource>
  .discuss('When using resource "${resource.getPath()}"')
    .path('${resource.getPath()}')
<#list resource.getMethodArray() as method>
    .discuss('and method "<@test.name method=method/>"')
      <@test.doc method=method/><#nt/>
      .${method.name?lower_case}()
        .expect(200)
    .undiscuss()
</#list>
  .undiscuss().unpath()
</#list>
</#list>
  .exportTo(module)
