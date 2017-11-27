@echo off
call amvn -q -Dandroid.device=usb clean android:generate-sources package android:redeploy android:run
