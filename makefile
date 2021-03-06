all: usage

LIB=app/android/Sensorama/app/libs

#GRADLEFLAGS=-info --stacktrace

check:
	(cd app/android/Sensorama && ./gradlew build -x lint $@) | cat

rel:
	(cd app/android/Sensorama && ./gradlew build $(GRADLEFLAGS) -x lint | cat)
up:
	(cd app/android/Sensorama && gradle assembleRelease crashlyticsUploadDistributionRelease)
fetch:
	mkdir -p $(LIB) _tmp
	wget -O _tmp/parse.zip https://www.parse.com/downloads/android/Parse/latest
	(cd _tmp && unzip -o parse.zip)
	(cd _tmp && cp bolts-android*.jar Parse-*.jar ParseCrash*.jar ../$(LIB)/)
startsim:
	echo no | android create avd --force -n test -t android-19 --abi armeabi-v7a
	emulator -avd test -no-skin -no-audio -no-window &
	android-wait-for-emulator
	adb shell input keyevent 82 &
