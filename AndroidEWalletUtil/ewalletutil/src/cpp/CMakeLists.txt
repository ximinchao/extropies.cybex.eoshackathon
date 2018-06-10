# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Set Output Directory
# set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../../jniLibs/${ANDROID_ABI})

# cJSON library
add_library( cJSON
             STATIC
             IMPORTED )
set_target_properties( cJSON
                       PROPERTIES IMPORTED_LOCATION
                       ${PROJECT_SOURCE_DIR}/cJSON/libs/${ANDROID_ABI}/libcjson.a )
include_directories( cJSON/include/ )

# EWalletUtil library
add_library( EWalletUtil
             SHARED
             CommUtility/CommUtility.c
             EOS/EOSUtil.c
             EWallet/PA_EWallet.c
             com_extropies_ewalletutil_ewalletutil.c )
#include_directories( EWallet/include/ )
#add_custom_command( TARGET EWalletUtil
#                 POST_BUILD
#                 COMMAND ${CMAKE_COMMAND} -E copy "${CMAKE_CURRENT_SOURCE_DIR}/EWallet/libs/${ANDROID_ABI}/libEWallet.so" "${PROJECT_SOURCE_DIR}/../../libs/${ANDROID_ABI}/"
#                 )

# Build
target_link_libraries( EWalletUtil cJSON)