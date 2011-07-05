WadlTestGen
===========

WadlTestGen is a test generator for Apigee Source that uses a WADL definition to create a QUnit test suite for your API.

Usage
-----

You can use the `wadltestgen.sh` script to run the precompiled binary on OS X or Linux:

    ./wadltestgen.sh -cfg cfg/twitter.yaml

Running the above command will create a `gen/twitter` directory that contains the test suite. All configuration is specified using YAML and passed using the `-cfg` parameter. In the configuration file, you can specify the WADL file and paths used to generate the test suite, and pass arbitrary key/value pairs to be used in the test template.

The `example` directory contains a sample test suite for the Twitter Apigee Source API. Make sure to change the endpoint in `js/tests/twitter.js` to your own if you want to try it.
