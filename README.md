# garage-door-app

## To Build a JAR file
`gradlew shadowJar`

## To Create Config
From the root of the project, run: `cp sampleconfig\config.yaml .`
Edit config.yaml and change the `myQUsername` and `myQPassword`

## To Run Application
From the root directory of the project:
`java -jar build\libs\garage-door-app-all.jar`

## Alternative Running Method
You can actually double click the JAR from, for example, Microsoft Windows!
Just place the `config.yaml` file in the same directory as the JAR file and click away!!

## Future Enhancements
* Remove calls from AWT thread (see TODO statement)
* Create a GUI with configs