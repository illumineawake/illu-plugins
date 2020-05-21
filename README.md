# OpenOSRS-external-example

You can use this repository as base for your external plugins, and host it on GitHub to make your external plugins available for everybody through the external manager plugin panel in the OpenOSRS client.

First of all you need to build the client (refer to the steps in this [guide][1])
After building you need to upload all the artifacts to your local maven repository.
You can do this within intellij by going to the gradle panel at the right hand side and click on OpenOSRS -> Tasks -> publishing -> publishToMavenLocal

In this repository you'll find two examples one is written in kotlin and the other one is written in java.
Before you start you need to make a couple changes:

1. Go to the file "build.gradle.kts" in the main folder
2. Change the value of "project.extra["GithubUrl"]" to your github name and repository (only needed if you want to upload the plugins)
3. Change the value of "project.extra["PluginProvider"]" to your name or your alias.
4. Change the value of "project.extra["ProjectSupportUrl"]" to your discord channel or leave it empty.

The file "{project}/{projectname}.gradle.kts" (for example "javaexample/javaexample.gradle.kts") has two values you'll need to change "project.extra["PluginName"]" and "project.extra["PluginDescription"]"

After building your project you can find the plugin jar in the build/libs folder of that project.
This jar can be used directly by copying it into the "externalmanager" folder in the ".runelite" directory.

If you want to bootstrap your plugins to make them available on GitHub you can just easily run the following task from the gradle panel in intellij Tasks -> other -> bootstrapPlugin
This will copy your plugin to the release folder in the main directory and fill the plugins.json file with the needed information.

You should always run the clean task before running the bootstrapPlugins task!
Before bootstrapping make sure you've changed the version number of your project, duplicate version numbers are not allowed and the bootstrap task will fail.


[1]: https://github.com/open-osrs/runelite/wiki/Building-with-IntelliJ-IDEA