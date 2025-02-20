// Permissions.js

import { PermissionsAndroid } from 'react-native';

// Function to request permissions from the user
async function RequestPermissions() {
  try {
    // Requesting multiple permissions at once
    const granted = await PermissionsAndroid.requestMultiple([
      PermissionsAndroid.PERMISSIONS.CALL_PHONE, // For making calls
      PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE, // To read phone state
      PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS, // For notifications
      PermissionsAndroid.PERMISSIONS.FOREGROUND_SERVICE, // For foreground services (Android 13+)
    ]);

    // Checking if all permissions were granted
    if (
      granted[PermissionsAndroid.PERMISSIONS.CALL_PHONE] === PermissionsAndroid.RESULTS.GRANTED &&
      granted[PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE] === PermissionsAndroid.RESULTS.GRANTED &&
      granted[PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS] === PermissionsAndroid.RESULTS.GRANTED &&
      granted[PermissionsAndroid.PERMISSIONS.FOREGROUND_SERVICE] === PermissionsAndroid.RESULTS.GRANTED
    ) {
      console.log('All permissions granted!');
    } else {
      console.log('Some permissions denied.');
    }
  } catch (err) {
    console.warn('Error while requesting permissions:', err); // Handling any errors that occur
  }
}


export default RequestPermissions; // Exporting the function for reuse if needed elsewhere
