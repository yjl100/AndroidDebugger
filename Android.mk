# Copyright (C) 2011 Skyworth Open Source Project
# Copyright (C) 2011 Skyworth Inc.
#
# Author: Liyc <liyicai@skyworth.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

# This is the target being built.
LOCAL_PACKAGE_NAME := SkyworthDebugger

#LOCAL_STATIC_JAVA_LIBRARIES := ant
# Only compile source java files in this apk.
LOCAL_SRC_FILES := $(call all-java-files-under, src) 

# Link against the current Android SDK.
# LOCAL_SDK_VERSION := current

# Also link against our own custom library.
LOCAL_JAVA_LIBRARIES :=  framework
LOCAL_DEX_PREOPT := false

LOCAL_SRC_FILES += src/com/skyworthdigital/debugger/aidl/ILogcatCallback.aidl
LOCAL_SRC_FILES += src/com/skyworthdigital/debugger/aidl/ILogcatService.aidl

#LOCAL_PROGUARD_ENABLED := full
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

# We need to assign platform key to use ServiceManager.addService.
LOCAL_CERTIFICATE := platform


include $(BUILD_PACKAGE)
include $(CLEAR_VARS)

include $(BUILD_MULTI_PREBUILT)  