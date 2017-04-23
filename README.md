# Architect
Architect is a Minecraft mod that aims to make repetitive building tasks easier. It provides a streamlined workflow for creating blueprints (patterns) from existing structures in the world, which can then be placed again at any position and rotation given the required materials are found in the player's inventory or accessible via a provider.

*This mod requires Java 8!*

## License / Use in Modpacks
This mod is [licensed under the **MIT license**](LICENSE). All **assets are public domain**, unless otherwise stated; all are free to be distributed as long as the license / source credits are kept. This means you can use this mod in any mod pack **as you please**. I'd be happy to hear about you using it, though, just out of curiosity.

## Configuration
Architects configuration is mainly done through JSON files controlling its built-in converters. Architect uses what I call converters to serialize blocks when converting a sketch into a blueprint, and again when deserializing a blueprint into the world. So in other words, a converter is responsible for providing a two-way transformation between in-world blocks and a serialized representation of the block. It also does some more stuff, like consuming materials, but the only other thing relevant here is that converters also provide a sort order for deserialization. This is important, because otherwise there'd be no guarantee solid blocks are placed before blocks that rely on them, such as torches and levers. These default converters operate on heuristics and a black- and whitelist to determine whether they are allowed to operate on any given block, and another heuristic determining whether the block is a "full" block or not, having to be placed before "attached" blocks. All of them serialize both the block's registry name and metadata.

### Built-in converters
- Simple block converter. Operates on blocks that *do not* have a tile entity, and that do have a known item representation. If the block state's `isFullBlock` or `isFullCube` property is true, it will use the sort index for solid blocks, otherwise that for attached blocks.
- Falling block converter. Uses the same serialization logic as the simple block converter, but only operates on blocks that can fall (like sand and gravel) by checking if the block extends `BlockFalling`. Uses the sort index for falling blocks.
- Fluid block converter. Uses the same serialization logic as the simple block converter, but only operates on vanilla liquid blocks and fluid blocks by checking if the block implements `IFluidBlock`. Uses the sort index of fluid blocks.
- Tile entity converter. Used to handle tile entities whitelisted in `whitelist.json`, taking care of proper tile entity NBT filtering, e.g. to prevent inventories or other unwanted state being copied. Uses the sort index defined in the whitelist, defaulting to that of solid blocks.

### JSON files
- `block_mappings.json` is used to replace blocks. This mapping is done before any normal converter is run. Typically used to use an equivalent "default state" block in the blueprint, e.g. `furnace` instead of `lit_furnace`. Note that when mapping to a different block *class*, metadata will not be kept.
- `item_mappings.json` is used to look up items for blocks. This takes priority, thus allows overriding the registered item for a block. Typically used for some blocks that have no such relation registered, e.g. `redstone_wire` to `redstone`. Note that this is used after block mapping has occurred, so mappings must be from mapped block to item where applicable.
- `blacklist.json` is used to completely forbid serialization of blocks. This is tested even before converters are looked up. Allows filtering by block state properties.
- `whitelist.json` is used to allow serialization of blocks with tile entities and defines NBT filtering and sort index configuration.

### JSON file structure
#### `block_mappings.json` and `item_mappings.json`
These contain a list of simple mappings of resource location to resource location, i.e. from registry name to registry name. For example:

##### Format
```
<mapping> == {
  "<registry_name>": "<registry_name>",
  ...
}
```

##### Example
```
{
  "minecraft:iron_block": "minecraft:redstone"
}
```

If this were in the `item_mappings.json`, blocks of iron could be placed using redstone dust.

#### `blacklist.json`
Contains a list of block state filters, which are made up of the registry name of the block and a list of property-value pairs. Properties not listed are considered as matching regardless of their value. If no property constraints are present, the definition can be reduced to just the block's registry name.

##### Format
```
<blacklist> == [<block_filter>, ...]
<block_filter> == "<registry_name>"
<block_filter> == {
  "block": "<registry_name>",
  "properties": {
    "<property_name>": "<property_value>",
    ...
  }
}
```

##### Example
```
[
  {
    "block": "minecraft:chest",
    "properties": {
      "facing": "east"
    }
  },
  "minecraft:furnace"
]
```
This would only blacklist chests facing east, but all furnaces, regardless of orientation.
**Important**: the blacklist is checked against the *original* block state of the block in question, *not* the mapped one.

#### `whitelist.json`
Contains a list of tile entity filters, which are made up of a block state filter (see blacklist), a sort index and a NBT filter and strip definition. Similar to block filters a tile entity filter can be condensed into just the block's registry name if there are no additional settings.
NBT filters are defined as a list of keys to keep. The values can be used for comments or just be an empty string. However, they can also be a nested filter definition, allowing to filter nested NBT tag compounds. Keep in mind that the present filter keys are the NBT entries that are *kept*. All others are not serialized. Thus, an empty filter means no NBT is serialized at all.
The NBT stripping definition is used before applying serialized NBT to a newly deserialized tile entity. This is typically not needed, but some mods use marker entries in their tile entities' NBT that lead to ignoring serialized values otherwise (for example, EnderIO does this with its `__null` keys).

##### Format
```
<whitelist> == [<tile_entity_filter>, ...]
<tile_entity_filter> == "<registry_name>"
<tile_entity_filter> == {
  "block": <block_filter>,
  "sortIndex": <sort_index>,
  "filter": {
    "<nbt_key>": <nbt_filter>
  },
  "strip": {
    "<nbt_key>": <nbt_filter>
  }
}
<sort_index> == number | "solid" | "falling" | "attached" | "fluid"
<nbt_filter> == free text, use for commenting for example
<nbt_filter> == {
  "<nbt_key>": <nbt_filter>
}
```

##### Example
```
[
  "minecraft:furnace",
  {
    "block": "minecraft:standing_sign",
    "sortIndex": "attached",
    "filter": {
      "Text4": "STRING",
      "Text3": "STRING",
      "Text2": "STRING",
      "Text1": "STRING"
    }
  },
  {
    "block": "enderio:blockzombiegenerator",
    "filter": {
      "facing": "INT",
      "faceModes": "LONG",
      "paintSource": "COMPOUND",
      "redstoneControlMode": "INT"
    },
    "strip": {
      "faceModes__null": "If present, always ignores faceModes.",
      "paintSource__null": "If present, always ignores paintSource.",
      "facing__null": "If present, always ignores facing."
    }
  },
  {
    "block": {
      "block": "immersiveengineering:metalDevice1",
      "properties": {
        "type": "fluid_pipe"
      }
    },
    "filter": {
      "sideConfig": "INT[]"
    }
  }
]
```
These four entries demonstrate the varying types of whitelist entries. From top to bottom we have
- the `minecraft:furnace` entry, which whitelists furnaces with any block state but completely strips their NBT data.
- the `minecraft:standing_sign` entry, which keeps the text lines on the sign and makes sure it is deserialized after solid blocks.
- the `enderio:blockzombiegenerator` entry, which keeps simple configurations and strips out aforementioned "null entries".
- the `immersiveengineering:metalDevice1` entry, which allows only block states with the "type" property set to "fluid_pipe", keeping the basic configuration NBT.

## Extending
In general, please refer to [the API](src/main/java/li/cil/architect/api), everything you need to know should be explained in the Javadoc of the API classes and interfaces. The converter API allows registering custom converters, which allows inclusion of blocks that would otherwise not be supported in blueprints.

### Gradle
To add a dependency to Architect for use in your mod, add the following to your `build.gradle`:

```groovy
repositories {
  maven {
    url = "http://maven.cil.li/"
  }
}
dependencies {
  compile "li.cil.architect:Architect:${config.architect.version}"
}
```

Where `${config.architect.version}` is the version you'd like to build against.
