import React, { useState } from 'react';
import { View, Text, Alert, PermissionsAndroid, Platform, ToastAndroid, NativeModules, TouchableOpacity, StyleSheet } from 'react-native';
import Geolocation from '@react-native-community/geolocation';

const { SmsModule } = NativeModules;

const PermissionsAndLocation = () => {
  const requestPermissions = async () => {
    if (Platform.OS === 'android') {
      const smsStatus = await requestPermission(PermissionsAndroid.PERMISSIONS.RECEIVE_SMS, 'SMS');
      ToastAndroid.show(smsStatus ? 'SMS permission granted' : 'SMS permission denied', ToastAndroid.SHORT);

      const callStatus = await requestPermission(PermissionsAndroid.PERMISSIONS.CALL_PHONE, 'Call');
      ToastAndroid.show(callStatus ? 'Call permission granted' : 'Call permission denied', ToastAndroid.SHORT);
    }
  };

  const requestPermission = async (permission, permissionType) => {
    try {
      const granted = await PermissionsAndroid.request(permission, {
        title: `${permissionType} Permission`,
        message: `This app needs access to ${permissionType}.`,
        buttonNeutral: 'Ask Me Later',
        buttonNegative: 'Cancel',
        buttonPositive: 'OK',
      });
      return granted === PermissionsAndroid.RESULTS.GRANTED;
    } catch (err) {
      console.warn(err);
      return false;
    }
  };

  const requestLocationPermission = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          {
            title: 'Location Permission',
            message: 'This app needs access to your location to send it via SMS.',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          }
        );
        return granted === PermissionsAndroid.RESULTS.GRANTED;
      } catch (err) {
        console.warn(err);
        return false;
      }
    }
    return true;
  };

  const getLocation = async () => {
    return new Promise((resolve, reject) => {
      Geolocation.getCurrentPosition(
        (position) => resolve(position.coords),
        (error) => reject(error),
        { enableHighAccuracy: false, timeout: 30000, maximumAge: 10000 }
      );
    });
  };

  const sendSMS = async () => {
    const hasLocationPermission = await requestLocationPermission();
    if (!hasLocationPermission) {
      Alert.alert('Permission Denied', 'Location permission is required to send location.');
      return;
    }

    try {
      const coords = await getLocation();
      const { latitude, longitude } = coords;
      const message = `My Location: https://www.google.com/maps?q=${latitude},${longitude}`;

      if (Platform.OS === 'android') {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.SEND_SMS,
          {
            title: 'SMS Permission',
            message: 'This app needs access to send SMS messages directly.',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          }
        );

        if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
          Alert.alert('Permission Denied', 'SMS permission is required to send messages.');
          return;
        }
      }

      try {
        const result = await SmsModule.sendDirectSms('+918263891140', message);
        Alert.alert('Success', `SMS Sent: ${result}`);
      } catch (error) {
        Alert.alert('Error', `Failed to send SMS: ${error.message}`);
      }
    } catch (error) {
      Alert.alert('Error', `Failed to fetch location: ${error.message}. Make sure GPS is on.`);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.heading}>Permissions and Location</Text>

      <TouchableOpacity style={styles.button} onPress={requestPermissions}>
        <Text style={styles.buttonText}>Request SMS & Call Permissions</Text>
      </TouchableOpacity>

      <View style={styles.spacer} />

      <TouchableOpacity style={styles.button} onPress={sendSMS}>
        <Text style={styles.buttonText}>Send SMS with Location</Text>
      </TouchableOpacity>
    </View>
  );
};

// Styles
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#004d26', // Dark Green background
    padding: 20,
  },
  heading: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#FFD700', // Gold color for the heading
    marginBottom: 20,
    textAlign: 'center',
  },
  spacer: {
    height: 20, // Space between the buttons
  },
  button: {
    backgroundColor: '#FFD700', // Gold color
    paddingVertical: 12,
    paddingHorizontal: 25,
    borderRadius: 25, // Rounded corners
    alignItems: 'center',
    justifyContent: 'center',
    width: '80%', // Make buttons wide
  },
  buttonText: {
    color: 'black',
    fontSize: 13,
    fontWeight: 'bold',
  },
});

export default PermissionsAndLocation;
