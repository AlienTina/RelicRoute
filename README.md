# RelicRoute
App for getting the average profit of your relics in Warframe

Current features:
 - Adding Relics to a collection
 - Collection stays on reboot
 - Calculating the average price of each relic
 - Sorting collection based on price

## Building

### Desktop
``./gradlew :composeApp:createDistributable``

The executable will be at composeApp/build/compose/binaries/main/app/org.tinya.relicroute/

### Android
Use Android Studio

### Web
``./gradlew wasmJsBrowserDistribution``

The executable will be at composeApp/build/dist/wasmJs/productionExecutable
