{
    "name": "sometestsuite",
    "version": "0.0.1",
    "dependencies": {
        "coffee-script": "1.1.x",
        "vows": "0.6.x",
        "api-easy": ">=0.3.2"
    },
    "engine": "node ~> 0.6.x",
    "scripts": {
        "test": "vows --spec test/suite.coffee"
    }
}
