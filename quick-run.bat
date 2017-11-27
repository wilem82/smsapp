@echo off
call amvn -q -Dandroid.device=usb package android:redeploy android:run
