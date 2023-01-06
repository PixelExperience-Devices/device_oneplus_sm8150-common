/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/logging.h>
#include <android-base/properties.h>

#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <stdio.h>
#include <stdlib.h>
#include <sys/sysinfo.h>
#include <sys/system_properties.h>
#include <sys/_system_properties.h>

#include "property_service.h"
#include "vendor_init.h"

using android::base::GetProperty;
using std::string;

std::vector < std::string > ro_props_default_source_order = {
  "",
  "odm.",
  "product.",
  "system.",
  "system_ext.",
  "vendor.",
  "vendor_dlkm."
};

/*
 * SetProperty does not allow updating read only properties and as a result
 * does not work for our use case. Write "property_override" to do practically
 * the same thing as "SetProperty" without this restriction.
 */

void property_override(char const prop[], char const value[], bool add = true) {
    prop_info *pi;

    pi = (prop_info*) __system_property_find(prop);
    if (pi)
        __system_property_update(pi, value, strlen(value));
    else if (add)
        __system_property_add(prop, strlen(prop), value, strlen(value));
}

void set_ro_build_prop(const std::string &prop, const std::string &value, bool product = true) {
    string prop_name;

    for (const auto &source : ro_props_default_source_order) {
        if (product)
            prop_name = "ro.product." + source + prop;
        else
            prop_name = "ro." + source + "build." + prop;

        property_override(prop_name.c_str(), value.c_str());
    }
}

void vendor_load_properties() {

  string model;
  string device;
  string name;

  /*
   * Only for read-only properties. Properties that can be wrote to more
   * than once should be set in a typical init script (e.g. init.oplus.hw.rc)
   * after the original property has been set.
   */

  auto prj_version = std::stoi(GetProperty("ro.boot.prj_version", "0"));
  auto rf_version = std::stoi(GetProperty("ro.boot.rf_version", "0"));

  switch (prj_version) {
    /* OnePlus 7 */
  case 18857:
    device = "OnePlus7";
    switch (rf_version) {
      /* China */
    case 1:
      name = "OnePlus7";
      model = "GM1900";
      break;
    case 3:
      /* India */
      name = "OnePlus7";
      model = "GM1901";
      break;
    case 4:
      /* Europe */
      name = "OnePlus7";
      model = "GM1903";
      break;
    case 5:
      /* Global / US Unlocked */
      name = "OnePlus7";
      model = "GM1905";
      break;
    default:
      /* Generic */
      name = "OnePlus7";
      model = "GM1901";
      break;
    }
    break;
    /* OnePlus 7 Pro */
  case 18821:
    device = "OnePlus7Pro";
    switch (rf_version) {
      /* China */
    case 1:
      name = "OnePlus7Pro";
      model = "GM1910";
      break;
    case 3:
      /* India */
      name = "OnePlus7Pro";
      model = "GM1911";
      break;
    case 4:
      /* Europe */
      name = "OnePlus7Pro";
      model = "GM1913";
      break;
    case 5:
      /* Global / US Unlocked */
      name = "OnePlus7Pro";
      model = "GM1917";
      break;
    default:
      /* Generic */
      name = "OnePlus7Pro";
      model = "GM1911";
      break;
    }
    break;
  case 18831:
    /* OnePlus 7 Pro T-Mobile */
    name = "OnePlus7Pro_TMO";
    model = "GM1911";
    break;
  /* OnePlus 7T */
  case 18865:
    device = "OnePlus7T";
    switch (rf_version) {
      /* China */
    case 1:
      name = "OnePlus7T";
      model = "HD1900";
      break;
      /* India */
    case 3:
      name = "OnePlus7T";
      model = "HD1901";
      break;
      /* Europe */
    case 4:
      name = "OnePlus7T";
      model = "HD1903";
      break;
      /* Global / US Unlocked */
    case 5:
      name = "OnePlus7T";
      model = "HD1905";
      break;
      /* Generic */
    default:
      name = "OnePlus7T";
      model = "HD1901";
      break;
    }
    break;
  case 19863:
    /* OnePlus 7T T-Mobile */
    name = "OnePlus7T_TMO";
    model = "HD1907";
    break;
    /* OnePlus 7T Pro */
  case 19801:
    device = "OnePlus7TPro";
    switch (rf_version) {
      /* China */
    case 1:
      name = "OnePlus7TPro";
      model = "HD1910";
      break;
      /* India */
    case 3:
      name = "OnePlus7TPro";
      model = "HD1911";
      break;
      /* Europe */
    case 4:
      name = "OnePlus7TPro";
      model = "HD1913";
      break;
      /* Global / US Unlocked */
    case 5:
      name = "OnePlus7TPro";
      model = "HD1917";
      break;
      /* Generic */
    default:
      name = "OnePlus7TPro";
      model = "HD1911";
      break;
    }
    break;
  case 19861:
    /* OnePlus 7T T-Mobile */
    name = "OnePlus7TPro_TMO";
    model = "HD1925";
    /* Force SIM configuration to ssss only on the TMO variant */
    property_override("persist.radio.multisim.config", "ssss");
    break;
  }

  set_ro_build_prop("device", device);
  set_ro_build_prop("model", model);
  set_ro_build_prop("name", name);
  set_ro_build_prop("product", model, false);
}