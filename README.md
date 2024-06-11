Sakura
===========
This is a fork of Paper to optimise cannoning and provide essential features for testing and faction servers.

There are branches from the latest version to 1.19.4. If you need a 1.8.8 server jar check out the project Sakura was based on [Blossom](https://github.com/Samsuik/Blossom).

## There are compiled binaries on the releases page

> https://github.com/Samsuik/Sakura/releases

## Requirements (Source, Compiling)

### Windows (you need [Git Bash](https://git-scm.com/download/win) and a JDK)
> You can use command prompt on if Git is installed, make sure you to not include the `./` at the start of the gradlew commands.
>
> You may also have trouble with the file path length limit, there is a registry tweak you can make to remove this limitation.
> - https://learn.microsoft.com/en-us/windows/win32/fileio/maximum-file-path-limitation

### Linux (git and a JDK)
> ...

## Obtaining the source code
> ```
> git clone https://github.com/Samsuik/Sakura
> ```
> Navigate to the locally cloned sakura repository
> ```
> cd Sakura
> ```
> You can use `git checkout ...` to choose between branches.
> ```
> git checkout 1.20.6
> ```
> To get the source code you can view, make changes etc.
> ```
> ./gradlew applyPatches
> ```

## Building the project
> ### 1.20.4 and earlier
> ```
> ./gradlew createReobfPaperclipJar
> ```
> ### 1.20.6 and later
> ```
> ./gradlew createMojmapPaperclipJar
> ```
