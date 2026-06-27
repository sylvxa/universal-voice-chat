# Universal Voice Chat

Discord-powered proximity voice chat that works without relying on a server.

## Notes

This mod is a bit of a proof-of-concept. If you are looking for something to use on your server, use [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) or [Plasmo Voice](https://modrinth.com/mod/plasmo-voice)— they will work better there.

Only use this mod with people you trust! There are several places where bad actors could impersonate others or crash your game due to how the Social SDK is designed with client-side lobbies. 

## Building

- Obtain the [Discord Social SDK C++ libraries](https://docs.discord.com/developers/discord-social-sdk/getting-started/using-c++#step-4-download-the-discord-sdk-for-c++) and extract them to `lib/discord_social_sdk` 
- Build normally with `./gradlew build`

## Credits

- [Discord](https://docs.discord.com/developers/intro) - UVC uses the Discord Social SDK to handle voice chat
- [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) - UI inspiration (nothing directly copied, but I've used it too much not to accidentally take inspiration)