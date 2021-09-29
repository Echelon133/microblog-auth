# Microblog auth

Simple auth server that lets users:
* exchange their login and password for tokens (access tokens and refresh tokens)
* get fresh access tokens by providing refresh tokens that they own

The auth flow in this app is very simple, thus not the most secure. 
In production environment, auth servers that use solutions like **PKCE** should be used.


## API

| Endpoint               | Method | Request Body              | Description                                                                                                                                |
|------------------------|--------|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| /api/token             | POST   | -                         | Returns a pair of tokens - a refresh token and an access token, that are owned by the user who sent his login & password together with the request (using Basic Auth). |
| /api/token/renew       | POST   | 'refreshToken' (optional) | Returns a fresh access token that belongs to the user whose refresh token was sent in the request.                                       |
| /api/token/clearTokens | POST   | -                         | Returns a pair of empty tokens (as cookies) to force the browser to replace token cookies that might currently be stored.                  |

### POST /api/token

Requires auth: yes

Needs the 'Authorization' header set because it uses HTTP Basic Authentication to determine who wants to receive tokens.

###### Example request:
**POST /api/token**

Response body:
```JSON
{
  "refreshToken": "vR7SQb7G7daDOFBHzczUy6cRtwv5I87WdAWPWg3AgzaJYz62Ym2cdN1KwHyz0a854SfaK6y3RpRd4qvF",
  "accessToken": "nwYNXAshsTyLdirjOyLPkbHmxNRwfHFmrKMpRiSJWG043jxyQw9BPwWeeXJjxSW3"
}
```

Both tokens are also sent by the server as cookies.

*** 

### POST /api/token/renew

Requires auth: no, but the user needs to provide a valid refresh token

This can be done in two ways:
* sending a 'refreshToken' cookie with the request
* sending a 'refreshToken' field in the request body in a form of a JSON object

###### Request body:

Optional request body. This is one of the ways of sending the refresh token to the server.

```JSON
{
  "refreshToken":"vR7SQb7G7daDOFBHzczUy6cRtwv5I87WdAWPWg3AgzaJYz62Ym2cdN1KwHyz0a854SfaK6y3RpRd4qvF"
}
```

###### Example request:
**POST /api/token/renew**

Response body:
```JSON
{
    "accessToken": "MtJP4ZU11n47yjEzrmRkRFdrBEKqavn4kNxFsKadqAJ6hmRw1QXoCc5VEjvoh26q"
}
```

***

### POST /api/token/clearTokens

Requires auth: no

###### Example request:

Every response to this request has an empty response body. Two cookies that are set by the server with the response are 
empty (they don't contain any tokens).