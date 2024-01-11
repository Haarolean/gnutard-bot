# GNUTard Bot

An anti-spam & moderation telegram bot for custom needs 

Features:
1. Check messages for spam links
2. Ban command
3. Report command
4. Nuke command
5. Auto unpin channel's forwarded messages to a discussion group


## Building

`docker build -t gnutardbot:latest .`

## Running

`docker run gnutardbot:latest`

## Application properties

1. `bot.token` -- api token
2. `bot.admins` -- list of ids, comma separated