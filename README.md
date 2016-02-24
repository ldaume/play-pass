# Abstract
Play-Pass is a very simple web based password manager which uses [Play](https://www.playframework.com/)-Java 2.4.6 and [ArangoDB](https://www.arangodb.com/).

All passwords are rendered with [DataTables](https://www.datatables.net/) to provide great pagination, instant search and multi-column ordering.

## Demo
====
![Visual demonstration of play-pass (loading gif..)](https://s3-eu-west-1.amazonaws.com/ldaume-public/play_pass/play-pass.gif)

## Roadmap
Check the [Roadmap](https://github.com/ldaume/play-pass/blob/master/CHANGELOG.md) to see changes made in the past and specific plans for upcoming releases.

# Imports
## KeePass
The CSV exports from [KeePass](http://keepass.info/) can be imported into this app.

# Usage
The App is available @ `http[s]://<host>/play-pass`.
## Docker
The App and all dependencies are available with [docker](https://hub.docker.com/r/ldaume/play-pass/) [![](https://badge.imagelayers.io/ldaume/play-pass:latest.svg)](https://imagelayers.io/?images=ldaume/play-pass:latest 'Get your own badge on imagelayers.io').
### Starting via Docker Compose
The easiest way to run the App is via [Docker Compose](https://docs.docker.com/compose/)
#### Compose File
One could create the `docker-compose.yml` file and start the App with `docker-compose up`.
```yaml
play-pass-arango:
  image: arangodb/arangodb:2.8.1
  container_name: play-pass-arangodb
  environment:
    - ARANGODB_USERNAME=user
    - ARANGODB_PASSWORD=pw
#  volumes:
#    - /path/to/host/dir:/var/lib/arangodb
#  restart: always
play-pass:
  image: ldaume/play-pass:latest
  container_name: play-pass
  links:
    - play-pass-arango
  ports:
    - "80:9000"
# Uncomment for SSL usage
#    - "443:9443"
  environment:
    - ARANGO_HOST=play-pass-arango
    - ARANGO_PORT=8529
    - ARANGO_USER=user
    - ARANGO_PASSWORD=password
    - AUTHORISED_USERS=[{email:'test@example.com',password:'test'}]
#  Change the enrtrypoint to use SSL
#  entrypoint: bin/play-pass -Dhttps.port=9443

```
## Linux & Windows Binary
Play provides an executable binary for linux and windows.
### Prerequisites
* Java SE 8
* One need to run a [ArangoDB](https://www.arangodb.com/) on any reachable host.

### Starting
Just start the the latest [binary](https://github.com/ldaume/play-pass/releases) via `bin/play-pass [params]`
### Parameter
#### Play
All available play parameter are valid.
#### ArangoDB
| param | description | type | default |
--- | --- | --- | ---
| `-Darango.host` | the host of the db | string | localhost |
| `-Darango.port` | the port of the db | integer |  8529 |
| `-Darango.username` | the db username | string |  root |
| `-Darango.password` | the user's password | string |  pw |
| `-Dauthorised.users` | the default users for the app | json-array |  `[{email:'test@example.com',password:'test'}]` |
| `-Dsession.timeout` | The session timeout in minutes | integer | 30 |
