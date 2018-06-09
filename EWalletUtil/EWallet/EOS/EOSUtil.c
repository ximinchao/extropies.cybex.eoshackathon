#include "EOSUtil.h"
#include "../CommUtility/CommUtility.h"

#include <stdlib.h>
#include <string.h>
#include <time.h> //for struct tm and time_t
#include <stdio.h> //for sscanf

int eos_util_tx_signed_get(const unsigned char * const pbData, const size_t nDataLen, eos_signed_transaction * const pstSingedTx, size_t * const pnProcessDataLen);
int eos_util_tx_signed_set(const eos_signed_transaction * const pstSingedTx, unsigned char * const pbData, size_t * const pnDataLen);
int eos_util_tx_signed_clear(eos_signed_transaction * const pstSingedTx);

int eos_util_tx_signature_get(const unsigned char * const pbData, const size_t nDataLen, eos_signature_type * const pstSingature, size_t * const pnProcessDataLen);
int eos_util_tx_signature_set(const eos_signature_type * const pstSingature, unsigned char * const pbData, size_t * const pnDataLen);
int eos_util_tx_signature_clear(eos_signature_type * const pstSingature);

int eos_util_tx_get(const unsigned char * const pbData, const size_t nDataLen, eos_transaction * const pstTx, size_t * const pnProcessDataLen);

int eos_util_tx_set(const eos_transaction * const pstTx, unsigned char * const pbData, size_t * const pnDataLen);

int eos_util_tx_clear(eos_transaction * const pstTx);

int eos_util_tx_from_json(const cJSON * const pJson, eos_transaction * const pstTx);

int eos_util_tx_header_get(const unsigned char * const pbData, const size_t nDataLen, eos_transaction_header * const pstHeader, size_t * const pnProcessDataLen);

int eos_util_tx_header_set(const eos_transaction_header * const pstHeader, unsigned char * const pbData, size_t * const pnDataLen);

int eos_util_tx_header_from_json(const cJSON * const pJson, eos_transaction_header * const pstHeader);

int eos_util_tx_action_get(const unsigned char * const pbData, const size_t nDataLen, eos_action * const pstAction, size_t * const pnProcessDataLen);

int eos_util_tx_action_set(const eos_action * const pstAction, unsigned char * const pbData, size_t * const pnDataLen);

int eos_util_tx_action_clear(eos_action * const pstAction);

int eos_util_tx_action_from_json(const cJSON * const pJson, eos_action * const pstAction);

int eos_util_tx_trans_ext_get(const unsigned char * const pbData, const size_t nDataLen, eos_transaction_extension * const pstTransExt, size_t * const pnProcessDataLen);

int eos_util_tx_trans_ext_set(const eos_transaction_extension * const pstTransExt, unsigned char * const pbData, size_t * const pnDataLen);

int eos_util_tx_trans_ext_clear(eos_transaction_extension * const pstTransExt);

int eos_util_tx_trans_ext_from_json(const cJSON * const pJson, eos_transaction_extension * const pstTransExt);

int eos_util_tx_permission_get(const unsigned char * const pbData, const size_t nDataLen, eos_permission_level * const pstPermission, size_t * const pnProcessDataLen);

int eos_util_tx_permission_set(const eos_permission_level * const pstPermission, unsigned char * const pbData, size_t * const pnDataLen);

int eos_util_tx_permission_from_json(const cJSON * const pJson, eos_permission_level * const pstPermission);

int eos_util_tx_uint_get(const unsigned char * const pbData, const size_t nDataLen, unsigned_int * const pstUint, size_t * const pnProcessDataLen);

int eos_util_tx_uint_set(const unsigned_int * const pstUint, unsigned char * const pbData, size_t * const pnDataLen);

int eos_util_tx_name_get(const unsigned char * const pbData, const size_t nDataLen, char szName[EOS_NAME_MAX_LEN], size_t * const pnProcessDataLen);

static uint64_t eos_util_tx_inner_char_to_symbol(const char c)
{
	if (c >= 'a' && c <= 'z')
		return (c - 'a') + 6;
	if (c >= '1' && c <= '5')
		return (c - '1') + 1;
	return 0;
}
// Each char of the string is encoded into 5-bit chunk and left-shifted
// to its 5-bit slot starting with the highest slot for the first char.
// The 13th char, if str is long enough, is encoded into 4-bit chunk
// and placed in the lowest 4 bits. 64 = 12 * 5 + 4
int eos_util_tx_name_set(const char szName[EOS_NAME_MAX_LEN], unsigned char * const pbData, size_t * const pnDataLen);

int eos_util_tx_name_from_json(const cJSON * const pJson, char szName[EOS_NAME_MAX_LEN]);

int eos_util_tx_from_string(const char * const szJsonString, eos_transaction * const pstTx);
