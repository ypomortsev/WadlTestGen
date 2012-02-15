express = require 'express'

app = express.createServer()
app.use express.bodyParser()

app.get '/', (req, resp) ->
  resp.send 'Hello, world!'

app.get '/status/:code([0-9]+)', (req, resp) ->
  resp.send (Number) req.params.code

app.get '/echo', (req, resp) ->
  resp.send req.query

app.post '/echo', (req, resp) ->
  resp.send req.body

app.listen 8000, -> console.log 'Listening...'
