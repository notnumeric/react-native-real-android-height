
# react-native-android-real-height

## Getting started

`$ npm install react-native-android-real-height --save`

### Mostly automatic installation

`$ react-native link react-native-android-real-height`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.ranna.arh.RNAndroidRealHeightPackage;` to the imports at the top of the file
  - Add `new RNAndroidRealHeightPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-android-real-height'
  	project(':react-native-android-real-height').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-android-real-height/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-android-real-height')
  	```


## Usage
```javascript
import RNAndroidRealHeight from 'react-native-android-real-height';

// TODO: What to do with the module?
RNAndroidRealHeight;
```
  