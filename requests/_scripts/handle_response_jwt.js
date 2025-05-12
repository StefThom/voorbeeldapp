client.global.set("app-sessietoken", response.body.jwtToken)
client.log("Saved token.")
client.log("Token: " + response.body.jwtToken)

