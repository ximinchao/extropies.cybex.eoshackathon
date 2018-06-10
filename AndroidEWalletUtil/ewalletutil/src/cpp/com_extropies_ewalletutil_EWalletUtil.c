//
// Created by ximin on 2018/6/9.
//

#include "com_extropies_ewalletutil_EWalletUtil.h"

#include <string.h> //for strlen
#include <stdlib.h> //for malloc

#include "./EWallet/PA_EWallet.h"

#include <Android/log.h>
#define JNI_EWALLET_UTIL_TAG    "JNI_EWALLET_UTIL" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, JNI_EWALLET_UTIL_TAG ,__VA_ARGS__) // 定义LOGD类型

jstring charToJstring(JNIEnv* env, const char* szInput) {
    jclass      strClass = 0;
    jmethodID   strConID = 0;
    jbyteArray  byteData = 0;
    jstring     encoding = 0;
    jstring     retString = 0;

    if (!env || !szInput ) {
        retString = 0;
        goto END;
    }

    strClass = (*env)->FindClass(env, "Ljava/lang/String;");
    strConID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
    byteData = (*env)->NewByteArray(env, strlen(szInput));
    (*env)->SetByteArrayRegion(env, byteData, 0, strlen(szInput), (jbyte*)szInput);
    encoding = (*env)->NewStringUTF(env, "GB2312");

    retString = (jstring) (*env)->NewObject(env, strClass, strConID, byteData, encoding);

END:
    if (strClass) {
        (*env)->DeleteLocalRef(env, strClass);
        strClass = 0;
    }
    if (byteData) {
        (*env)->DeleteLocalRef(env, byteData);
        byteData = 0;
    }
    if (encoding) {
        (*env)->DeleteLocalRef(env, encoding);
        encoding = 0;
    }
    return retString;
}

char* jstringToChar(JNIEnv* env, jstring jstr) {
    char        *retChars = 0;
    jclass      strClass = 0;
    jstring     encoding = 0;
    jmethodID   getBytesID = 0;
    jbyteArray  byteArrayData = 0;
    jsize       arrayLen = 0;
    jbyte       *byteData = 0;

    if (!env || !jstr) {
        retChars = 0;
        goto END;
    }

    strClass = (*env)->FindClass(env, "java/lang/String");
    encoding = (*env)->NewStringUTF(env, "GB2312");
    getBytesID = (*env)->GetMethodID(env, strClass, "getBytes", "(Ljava/lang/String;)[B");
    byteArrayData = (jbyteArray) (*env)->CallObjectMethod(env, jstr, getBytesID, encoding);
    arrayLen = (*env)->GetArrayLength(env, byteArrayData);

    if (arrayLen > 0) {
        byteData = (*env)->GetByteArrayElements(env, byteArrayData, JNI_FALSE);

        retChars = (char *)malloc(arrayLen + 1);
        if (!retChars) {
            goto END;
        }
        memcpy(retChars, byteData, arrayLen);
        retChars[arrayLen] = '\0';
    }

END:
    if (strClass) {
        (*env)->DeleteLocalRef(env, strClass);
        strClass = 0;
    }
    if (encoding) {
        (*env)->DeleteLocalRef(env, encoding);
        encoding = 0;
    }
    if (byteData) {
        (*env)->ReleaseByteArrayElements(env, byteArrayData, byteData, 0);
        byteData = 0;
    }
    if (byteArrayData) {
        (*env)->DeleteLocalRef(env, byteArrayData);
        byteArrayData = 0;
    }

    return retChars;
}
/*
 * Class:     com_extropies_ewalletutil_EWalletUtil
 * Method:    add
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_extropies_ewalletutil_EWalletUtil_add(JNIEnv *env, jclass jobj, jint a, jint b) {
    return (jint)(a + b);
}

/*
 * Class:     com_extropies_ewalletutil_EWalletUtil
 * Method:    PAEW_EOS_TX_Serialize
 * Signature: (Ljava/lang/String;[B[I)I
 */
JNIEXPORT jint JNICALL Java_com_extropies_ewalletutil_EWalletUtil_PAEW_1EOS_1TX_1Serialize(JNIEnv *env, jclass jobj, jstring jsonString, jbyteArray binData, jintArray binDataLen) {
    int     iRtn = -1;
    char    *szJsonString = 0;
    size_t   nBinDataSize = 0;

    jint    *pnBinDataLen = 0;
    jbyte   *pbBinData = 0;

    if (!jsonString || !binDataLen) {
        iRtn = -1;
        goto END;
    }

    pnBinDataLen = (*env)->GetIntArrayElements(env, binDataLen, 0);
    if (!pnBinDataLen) {
        iRtn = -1;
        goto END;
    }

    szJsonString = jstringToChar(env, jsonString);
    if (!szJsonString) {
        iRtn = -1;
        goto END;
    }

    iRtn = PAEW_EOS_TX_Serialize(szJsonString, 0, &nBinDataSize);
    if (iRtn != PAEW_RET_SUCCESS) {
        iRtn = -1;
        goto END;
    }

    if (!binData) {
        pnBinDataLen[0] = (jint)nBinDataSize;

        (*env)->ReleaseIntArrayElements(env, binDataLen, pnBinDataLen, JNI_COMMIT);
        pnBinDataLen = 0;

        iRtn = 0;
        goto END;
    }

    if (pnBinDataLen[0] < (jint)nBinDataSize) {
        (*env)->ReleaseIntArrayElements(env, binDataLen, pnBinDataLen, JNI_COMMIT);
        pnBinDataLen = 0;

        iRtn = -1;
        goto END;
    }

    pbBinData = (*env)->GetByteArrayElements(env, binData, 0);
    if (!pbBinData) {
        iRtn = -1;
        goto END;
    }

    nBinDataSize = pnBinDataLen[0];
    iRtn = PAEW_EOS_TX_Serialize(szJsonString, (unsigned char*)pbBinData, &nBinDataSize);
    if (iRtn != PAEW_RET_SUCCESS) {
        iRtn = -1;
        goto END;
    }
    pnBinDataLen[0] = (jint)nBinDataSize;

    (*env)->ReleaseIntArrayElements(env, binDataLen, pnBinDataLen, JNI_COMMIT);
    pnBinDataLen = 0;
    (*env)->ReleaseByteArrayElements(env, binData, pbBinData, JNI_COMMIT);
    pbBinData = 0;

END:
    if (pbBinData) {
        (*env)->ReleaseByteArrayElements(env, binData, pbBinData, JNI_ABORT);
    }
    if (pnBinDataLen) {
        (*env)->ReleaseIntArrayElements(env, binDataLen, pnBinDataLen, JNI_ABORT);
    }
    if (szJsonString) {
        free(szJsonString);
        szJsonString = 0;
    }
    return iRtn;
}

/*
 * Class:     com_extropies_ewalletutil_EWalletUtil
 * Method:    PAEW_EOS_TX_Serialize_part
 * Signature: (Ljava/lang/String;I[B[I)I
 */
JNIEXPORT jint JNICALL Java_com_extropies_ewalletutil_EWalletUtil_PAEW_1EOS_1TX_1Serialize_1part(JNIEnv *env, jclass jobj, jstring jsonString, jint nPart, jbyteArray binData, jintArray binDataLen) {
    int     iRtn = -1;

    char    *szJsonString = 0;
    size_t   nBinDataSize = 0;

    jint    *pnBinDataLen = 0;
    jbyte   *pbBinData = 0;

    if (!jsonString || !binDataLen) {
        iRtn = -1;
        goto END;
    }

    pnBinDataLen = (*env)->GetIntArrayElements(env, binDataLen, 0);
    if (!pnBinDataLen) {
        iRtn = -1;
        goto END;
    }

    szJsonString = jstringToChar(env, jsonString);
    if (!szJsonString) {
        iRtn = -1;
        goto END;
    }

    if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_ALL) {
        iRtn = PAEW_EOS_TX_Serialize(szJsonString, 0, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS) {
            iRtn = -1;
            goto END;
        }
    } else if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_1) {
        iRtn = PAEW_EOS_TX_Part_Serialize(PAEW_SIG_EOS_TX_HEADER, szJsonString, 0, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS)
        {
            iRtn = -1;
            goto END;
        }
    } else if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_2) {
        iRtn = PAEW_EOS_TX_Part_Serialize(PAEW_SIG_EOS_TX_ACTION_COUNT, szJsonString, 0, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS)
        {
            iRtn = -1;
            goto END;
        }
    } else if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_3) {
        iRtn = PAEW_EOS_TX_Part_Serialize(PAEW_SIG_EOS_TX_ACTION, szJsonString, 0, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS)
        {
            iRtn = -1;
            goto END;
        }
    } else if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_4) {
        iRtn = PAEW_EOS_TX_Part_Serialize(PAEW_SIG_EOS_TX_CF_HASH, szJsonString, 0, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS)
        {
            iRtn = -1;
            goto END;
        }
    }

    if (!binData) {
        pnBinDataLen[0] = (jint)nBinDataSize;

        (*env)->ReleaseIntArrayElements(env, binDataLen, pnBinDataLen, JNI_COMMIT);
        pnBinDataLen = 0;

        iRtn = 0;
        goto END;
    }

    if (pnBinDataLen[0] < (jint)nBinDataSize) {
        (*env)->ReleaseIntArrayElements(env, binDataLen, pnBinDataLen, JNI_COMMIT);
        pnBinDataLen = 0;

        iRtn = -1;
        goto END;
    }

    pbBinData = (*env)->GetByteArrayElements(env, binData, 0);
    if (!pbBinData) {
        iRtn = -1;
        goto END;
    }

    nBinDataSize = pnBinDataLen[0];
    if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_ALL) {
        iRtn = PAEW_EOS_TX_Serialize(szJsonString, pbBinData, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS) {
            iRtn = -1;
            goto END;
        }
    } else if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_1) {
        iRtn = PAEW_EOS_TX_Part_Serialize(PAEW_SIG_EOS_TX_HEADER, szJsonString, pbBinData, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS)
        {
            iRtn = -1;
            goto END;
        }
    } else if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_2) {
        iRtn = PAEW_EOS_TX_Part_Serialize(PAEW_SIG_EOS_TX_ACTION_COUNT, szJsonString, pbBinData, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS)
        {
            iRtn = -1;
            goto END;
        }
    } else if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_3) {
        iRtn = PAEW_EOS_TX_Part_Serialize(PAEW_SIG_EOS_TX_ACTION, szJsonString, pbBinData, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS)
        {
            iRtn = -1;
            goto END;
        }
    } else if (nPart == com_extropies_ewalletutil_EWalletUtil_EOS_SER_PART_4) {
        iRtn = PAEW_EOS_TX_Part_Serialize(PAEW_SIG_EOS_TX_CF_HASH, szJsonString, pbBinData, &nBinDataSize);
        if (iRtn != PAEW_RET_SUCCESS)
        {
            iRtn = -1;
            goto END;
        }
    }
    pnBinDataLen[0] = (jint)nBinDataSize;

    (*env)->ReleaseIntArrayElements(env, binDataLen, pnBinDataLen, JNI_COMMIT);
    pnBinDataLen = 0;
    (*env)->ReleaseByteArrayElements(env, binData, pbBinData, JNI_COMMIT);
    pbBinData = 0;

    END:
    if (pbBinData) {
        (*env)->ReleaseByteArrayElements(env, binData, pbBinData, JNI_ABORT);
    }
    if (pnBinDataLen) {
        (*env)->ReleaseIntArrayElements(env, binDataLen, pnBinDataLen, JNI_ABORT);
    }
    if (szJsonString) {
        free(szJsonString);
        szJsonString = 0;
    }
    return iRtn;
}