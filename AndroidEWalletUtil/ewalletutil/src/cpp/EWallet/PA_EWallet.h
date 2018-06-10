#ifndef _PA_EWALLET_H_
#define _PA_EWALLET_H_

#include <stdint.h>

#ifndef _WIN32
#include <stddef.h> //for size_t
#endif //_WIN32

#define PAEW_RET_SUCCESS					0x00000000 //success
#define PAEW_RET_UNKNOWN_FAIL				0x80000001 //unknown failure
#define PAEW_RET_ARGUMENTBAD				0x80000002 //argument bad
#define PAEW_RET_BUFFER_TOO_SAMLL			0x80000003 //size of input buffer not enough to store return data
#define PAEW_RET_TX_PARSE_FAIL				0x80000004 //input transaction parse failed

#define PAEW_SIG_EOS_TX_HEADER			0x00
#define PAEW_SIG_EOS_TX_ACTION_COUNT	0x01
#define PAEW_SIG_EOS_TX_ACTION			0x02
#define PAEW_SIG_EOS_TX_CF_HASH			0x03 //context_free hash

#ifdef __cplusplus
extern "C"
{
#endif

int PAEW_EOS_TX_Serialize(const char * const szTransactionString, unsigned char * const pbTransactionData, size_t * pnTransactionLen);

int PAEW_EOS_TX_Part_Serialize(const unsigned int nPartIndex, const char * const szTransactionString, unsigned char * const pbTransactionData, size_t * pnTransactionLen);

#ifdef __cplusplus
};
#endif
#endif //_PA_EWALLET_H_