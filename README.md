# Make your advancement screen not suck

Paginated Advancements completely overhauls the advancement screen by adding a load of features:

- Makes the screen bigger, instead of a fixed size
- Adds Pagination (no "this modpack has too many tabs to display them all" anymore!)
- Pin the advancement tabs you are currently focusing on
- Remembering the last opened tab

Also adds some styling options, to make the screen exactly how you prefer it:
- Disable background fading
- Configurable Spacing between tabs. Allowing them to be condensed quite a bit, or giving them more room to breathe

![Pagination](./images/screenshot_pagination.png)

# Additional Advancement Frames & Colors
With version 2.0 comes the ability to register new advancement frames, including custom colors, via data pack / mods that support it. [Find out how in the Wiki](https://github.com/DaFuqs/PaginatedAdvancements/wiki)

![Custom Frames](./images/builtin_frames.png)

## Adding Custom Frames

Each Frame requires:

- 2 textures:
  - `assets/<mod_id>/textures/gui/sprites/advancements/<frame_name>_unobtained.png`
  - `assets/<mod_id>/textures/gui/sprites/advancements/<frame_name>_obtained.png`
- a metadata file under `assets/<mod_id>/advancement_frame_types\<frame_name>.json`

The json can have the following properties:

- style (optional): vanilla style specification for use in the advancement description text
- item_offset_x (optional): horizontal offset for rendering the item in the frame
- item_offset_y (optional): vertical offset for rendering the item in the frame

Example:

```json
{
  "style": {
    "color": "yellow",
    "strikethrough": true
  },
  "item_offset_x": 0,
  "item_offset_y": -4
}
```

## Assigning frames to advancements

Add a metadata file under `assets/<mod_id>/advancement_frames\<whatever_name_you_want>.json`.
That json should specify a list of advancement:frame entries.

You can pick from the vanilla frames (task, challenge, goal), one of the
ones [shipped by Paginated Advancements](./src/main/resources/assets/paginatedadvancements/advancement_frame_types/), or
your own (see above).

```json
[
  {
    "advancement": "minecraft:story/upgrade_tools",
    "frame": "minecraft:goal"
  },
  {
    "advancement": "minecraft:story/cure_zombie_villager",
    "frame": "paginatedadvancements:arrow_right"
  }
]
```