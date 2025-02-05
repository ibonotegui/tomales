# tomales
Simple compose app to make an http call and render the result categorized by listId and sorted by name items.

App follows the repository pattern so switching data sources is very simple. The view model takes a repository and when passing the local data source to the repository the view model will use the local data instead (very convenient for UI testing different data sizes without making any network calls).

./gradlew installDebug will install the apk 

![screenshot](https://github.com/ibonotegui/tomales/blob/main/screenshots/screenshot_1738789804.png)
