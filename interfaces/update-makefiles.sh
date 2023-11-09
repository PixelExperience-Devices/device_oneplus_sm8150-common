#!/bin/bash

source $ANDROID_BUILD_TOP/system/tools/hidl/update-makefiles-helper.sh

do_makefiles_update \
  "vendor.goodix:device/oneplus/sm8150-common/interfaces/goodix"

do_makefiles_update \
  "vendor.oneplus:device/oneplus/sm8150-common/interfaces/oneplus"
