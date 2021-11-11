import { NativeModules, Platform } from 'react-native';

export function get(dim) {
  if (Platform.OS !== 'android') {

    console.warn('react-native-extra-dimensions-android is only available on Android. Trying to access', dim);
    return 0;
  } else { // android
    try {
      if (!NativeModules.RNAndroidRealHeight) {
        throw "RNAndroidRealHeight not defined. Try rebuilding your project. e.g. react-native run-android";
      }
      const result = NativeModules.RNAndroidRealHeight[dim];

      if (typeof result !== 'number') {
        return result;
      }
      return result;
    } catch (e) {
      console.error(e);
    }
  }
}

export function getRealWindowHeight() {
  return get('REAL_WINDOW_HEIGHT');
}

export default {
  getRealWindowHeight
}
