Sakura
===========
This is a fork of Paper to optimise cannoning and provide essential features for cannon testing and faction servers.

There are branches from the latest version to 1.19.3. If you are looking for a 1.8.8 server jar check out the project Sakura was based on [Blossom](https://github.com/Samsuik/Blossom).

## There are compiled binaries on the releases page
> https://github.com/Samsuik/Sakura/releases

## Compiling
### Requirements
* Git
* JDK 21

### Getting setup
#### Clone the repository

To get started you will need to clone the repository
``` 
git clone https://github.com/Samsuik/Sakura
```

Navigate into the newly cloned repository
```
cd Sakura
```

If you're looking for an older version of minecraft, you will need to switch branches.
```
git checkout <branch name>
```
The branches targeting minecraft versions before `1.21.4` have `legacy/` in front of their name.
You can skip this step if you're ok with the latest version.

#### Applying Patches
> If you're using Windows the file path limit causes the `applyPatches` task to fail.
> This is because of the highly nested structure of this repository and the use of long file names.
> There is a registry setting that can be changed to raise this limit.
> https://learn.microsoft.com/en-us/windows/win32/fileio/maximum-file-path-limitation

All you have to do is run the `applyPatches` task.
```
./gradlew applyPatches
```

If everything was successful, you should see that two directories have been created `sakura-api` and `sakura-server` these contain all the source code.

If you want to switch branches after running `applyPatches`, you will need to run the `cleanCache` task.
```
./gradlew cleanCache
```

### Building
The patches must be applied before building otherwise it will fail.

The task used for building is different on older versions, make sure that you use the correct command.

#### After 1.20.6
```
./gradlew createMojmapPaperclipJar
```

#### Before 1.20.6
```
./gradlew createReobfPaperclipJar
```

You can find the built paperclip jar under `build/libs`.

## Contributing
If you would like to contribute please read the upstream [Contributing Guideline](https://github.com/PaperMC/Paper-archive/blob/ver/1.21.3/CONTRIBUTING.md).

It contains a lot of useful information on how the project is structured and how to use the build tools.
