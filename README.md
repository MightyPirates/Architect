# Architect
Architect is a Minecraft mod that aims to make repetitive building tasks easier. It 

*This mod requires Java 8!*

## License / Use in Modpacks
This mod is [licensed under the **MIT license**](LICENSE). All **assets are public domain**, unless otherwise stated; all are free to be distributed as long as the license / source credits are kept. This means you can use this mod in any mod pack **as you please**. I'd be happy to hear about you using it, though, just out of curiosity.

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
