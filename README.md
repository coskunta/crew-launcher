# CREW Launcher

CREW is an independent Minecraft: Java Edition desktop launcher being developed for personal use by a small group of friends.

The project includes Fabric installation, performance mods such as Sodium and Lithium, configurable in-game HUD modules, custom cape imports, wings and original emotes.

## Microsoft authentication and Minecraft API access

The launcher uses Microsoft's OAuth device authorization flow. Players sign in through Microsoft's website using their own accounts. The launcher does not collect Microsoft passwords.

Minecraft Services API access is requested to authenticate players, retrieve their Minecraft Java profiles and launch the game with a valid session. Each player must have access to Minecraft: Java Edition through their own account. The launcher does not bypass authentication or game ownership requirements.

## Development status

CREW is an early development alpha. Minecraft Services API approval is pending, and end-to-end gameplay testing is not yet complete.

This repository provides project information for the application review.

## Contact

GitHub: https://github.com/coskunta

Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.

## Source code

This repository now includes the Electron launcher (`launcher/`) and the Fabric mod (`client/`). The source is provided for inspection and development; this is not a stable release. The existing README description above remains applicable.

### Development requirements

- Node.js 24 or newer and npm for the launcher.
- Java 25 for Minecraft 26.3 and the Fabric mod.
- A Microsoft application registration configured for personal accounts and public client/device-code authentication. Minecraft Services may require application approval. HTTP 403 currently blocks our end-to-end login test.

### Build the mod and start the launcher

From the repository root on macOS/Linux:

```sh
cd client
chmod +x gradlew
./gradlew build
cd ..
node scripts/stage-client.cjs client/build/libs/crew-client-0.2.0-alpha.1.jar
cd launcher
npm ci
npm test
npm start
```

On Windows, use `gradlew.bat build` in place of `./gradlew build`. The staging script copies the newly built JAR into the launcher's bundle and updates its checksum. Generated game files, runtime downloads, dependencies and account files are excluded from Git.

After starting the launcher, enter your application's client ID in Settings. Do not put client secrets or account tokens into this repository. The application ID is a public identifier, not a password.

### Authentication review

Relevant files:

- `launcher/lib/auth.cjs`: Microsoft device authorization, Xbox Live/XSTS exchange and Minecraft profile request.
- `launcher/lib/auth-link.cjs`: validation of the verification URL returned by Microsoft.
- `launcher/main.cjs`: desktop IPC, encrypted refresh-token persistence where available, and direct Java process launch.
- `launcher/lib/installer.cjs`: official game/runtime downloads and dependency checksum verification.

The Microsoft login button now uses the verification URL supplied with the current device-code response. The previously distributed alpha mistakenly opened a fixed `devicelogin` URL even when Microsoft returned `https://www.microsoft.com/link` for the account flow. This source fix does not modify previously downloaded desktop binaries or remove the Minecraft API approval requirement.

### In-game controls and cosmetics

Right Shift opens the settings menu. Hold C to zoom, B toggles fullbright, and G opens emotes. Bindings can be changed in Minecraft.

Personal capes accept PNG files at 64×32, 128×64, 256×128 or 512×256 pixels, up to 1 MB. Imports are normalized to 64×32. Cape sharing transmits the texture to a CREW-enabled server and its CREW players when enabled. It is not an account-level Mojang cape or a global cosmetics service.

For synchronized capes, wings and emotes, the Fabric server and clients need the same CREW mod version and Fabric API. Sodium is client-only. The built-in animations are original implementations, not extracted Lunar or Fortnite assets.

### Validation and known limits

The original mod sources compiled against Minecraft 26.3 and Fabric with Java 25. Protocol round trips, PNG conversion and click-counter behavior were tested. The Gradle/Loom build was not completed in the original restricted build environment; it still needs validation on a normal development machine. Java gameplay and multiplayer have not been tested end to end.

Run the launcher checks with `npm test` inside `launcher/`. These are unit tests and do not replace a real Microsoft login, native desktop UI or Minecraft gameplay test.

There is no automatic updater. No live credentials, personal launcher settings, Minecraft game JARs or bundled Electron runtimes are included in the source repository. The Gradle wrapper JAR is a build tool.

## License

The project source uses the existing MIT license in `LICENSE`. Dependencies retain their own licenses. Minecraft is downloaded separately from official sources and is not included.
