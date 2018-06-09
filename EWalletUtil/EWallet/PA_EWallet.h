#ifndef _PA_EWALLET_H_
#define _PA_EWALLET_H_

#include <stdint.h>

#if (defined(_WIN32) && defined(_USRDLL))

#ifdef _EWALLET_DLL_
#define EWALLET_API	__declspec(dllexport)
#else //_EWALLET_DLL_
#define EWALLET_API
#endif //_EWALLET_DLL_

#else //_WIN32

#define EWALLET_API

#endif //_WIN32

#ifndef _WIN32
#include <stddef.h> //for size_t
#endif //_WIN32

#define PAEW_RET_SUCCESS					0x00000000 //success
#define PAEW_RET_UNKNOWN_FAIL				0x80000001 //unknown failure

#ifdef __cplusplus
extern "C"
{
#endif

int EWALLET_API PAEW_EOS_TX_Serialize(const char * const szTransactionString, unsigned char * const pbTransactionData, size_t * pnTransactionLen);

int EWALLET_API PAEW_EOS_TX_Part_Serialize(const unsigned int nPartIndex, const char * const szTransactionString, unsigned char * const pbTransactionData, size_t * pnTransactionLen);

#ifdef __cplusplus
};
#endif
#endif //_PA_EWALLET_H_