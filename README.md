# Chinese Hat (Fabric, Minecraft 1.21.11)

A conical Chinese hat on your character, with 11 styles and a full config menu.
Client-side only: nobody else needs the mod, and nothing is sent to servers.

## Use it
- Type `/hat` in chat to open the menu (tabs: Style, Shape, Colors, Options).
- `/hat toggle` turns the hat on/off, `/hat reset` restores defaults.
- You see your own hat in third person (F5), as layers are not drawn in first person.
- Settings are saved to `config/chinesehat.json`.

## Styles
Bamboo Weave, Imperial Silk (purple-to-gold), Carved Jade, Crystal Glass (see-through),
Hitbox, Solid Hitbox, Box Only, Neon Wire, Rainbow (animated), Ghost, Void.

## Build with GitHub (same as your other mods)
1. Create a new GitHub repository and upload **everything in this folder**, including the
   hidden `.github` folder and the `gradle` folder.
2. Open the **Actions** tab, wait for the `build` run to turn green.
3. Open the run, download the **Artifacts** zip, and take `chinese-hat-1.0.0.jar`
   (not the `-sources` jar). Put it in your `mods` folder with Fabric API.

Requires: Minecraft 1.21.11, Fabric Loader 0.18+, Fabric API.
