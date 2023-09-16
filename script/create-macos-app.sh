#!/usr/bin/env sh

sdkVersion='11.0.16.1.fx-zulu'
appName='groovy-pacman'
distRoot='build/distributions'

## tip: find out required modules with 'jdep -q ${appName}.jar'
requiredModules='java.base,java.desktop'

printSuggestedSolution() {
  printf "If you are currently using a JDK older than version 14, the jpackage tool is not available. However, you might purposely be building against this older JDK.\n"
  printf "To get jpackage anyway, you could manually install a newer JDK alongside the version(s) managed by sdkman. It will serve as a default:\n"
  printf " - Missing tools like jpackage will be found from that JDK.\n"
  printf " - If the active sdkman-managed version does provide jpackage, that one will be used.\n"
  printf "\nYou can find a suitable OpenJDK release on https://adoptium.net/temurin/releases/, or visit https://foojay.io/java-quick-start/ for a comprehensive list of other JDK's as well.\n"
  printf "Just make sure you select a JDK version 14 or later, for your OS and your hardware platform.\n\n"
}

## use sdkman to make sure we're using the right Java SDK
. "${HOME}/.sdkman/bin/sdkman-init.sh"
command -v sdk >/dev/null 2>&1 || { printf >&2 "\nERROR: The package manager 'sdkman' is required to manage JDK versions, but was not found on your path. Please get it from https://sdkman.io/.\n\n"; exit 1; }
sdk use java ${sdkVersion} >/dev/null 2>&1 || exit 1

## build distro
./gradlew --show-version clean distTar || exit 1

## unpack distro
tar xf "${distRoot}/${appName}.tar" -C ${distRoot} || exit 1

## create image of the proper Java runtime version
jlink \
   --output build/distributions/${sdkVersion}-runtime \
   --add-modules ${requiredModules}

command -v jpackage >/dev/null 2>&1 || { printf >&2 "\nERROR: The utility 'jpackage' is required to package the application, but was not found on your path.\n\n"; printSuggestedSolution; exit 1; }

jpackage -i build/distributions/${appName}/lib -n "${appName}" \
  --vendor 'Frank van Westerop' \
  --app-version '1.0.0' \
  --copyright '2017-2023 MIT License' \
  --description 'Pacman game implemented in the Apache Groovy programming language. Please visit https://github.com/fvanwesterop/groovy-pacman for details.' \
  --mac-package-name 'Groovy Pacman' \
  --icon 'src/external-resources/pacman-icon.icns' \
  --runtime-image build/distributions/${sdkVersion}-runtime \
  --main-class io.gfrank.pacman.Game \
  --main-jar groovy-pacman.jar \
  --dest ${distRoot}