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

int eos_util_tx_name_get(const unsigned char * const pbData, const size_t nDataLen, char szName[EOS_NAME_MAX_LEN], size_t * const pnProcessDataLen)
{
	int	iRtn = -1;

	size_t				i = 0, iOffset = 0;

	uint64_t			value = 0, tmp = 0;
	char				c = 0;
	static const char	*charmap = ".12345abcdefghijklmnopqrstuvwxyz";

	if (!pbData || !nDataLen || !szName)
	{
		iRtn = -1;
		goto END;
	}

	iOffset = 0;

	if (sizeof(value) > (nDataLen - iOffset))
	{
		iRtn = -1;
		goto END;
	}
	value = 0;
	for (i = 0; i < sizeof(value); i++)
	{
		value |= ((uint64_t)pbData[iOffset + i]) << (i * 8);
	}
	iOffset += sizeof(value);

	memset(szName, 0, EOS_NAME_MAX_LEN);
	tmp = value;
	for (i = 0; i <= 12; i++)
	{
		c = charmap[tmp & (i == 0 ? 0x0f : 0x1f)];
		szName[12 - i] = c;
		tmp >>= (i == 0 ? 4 : 5);
	}

	i = 12;
	do
	{
		if (szName[i] == '.')
		{
			szName[i] = '\0';
		}
		else
		{
			break;
		}

		if (i == 0)
		{
			break;
		}
		i--;
	} while (1);

	if (pnProcessDataLen)
	{
		*pnProcessDataLen = iOffset;
	}

	iRtn = 0;
END:
	return iRtn;
}

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
int eos_util_tx_name_set(const char szName[EOS_NAME_MAX_LEN], unsigned char * const pbData, size_t * const pnDataLen)
{
	int	iRtn = -1;

	size_t			i = 0, iOffset = 0;
	uint64_t		name = 0;
	const uint64_t	iFilterMask = 0xff;

	if (!szName || !pnDataLen)
	{
		iRtn = -1;
		goto END;
	}

	name = 0;
	for (; szName[i] && i < 12; ++i)
	{
		// NOTE: char_to_symbol() returns char type, and without this explicit
		// expansion to uint64 type, the compilation fails at the point of usage
		// of string_to_name(), where the usage requires constant (compile time) expression.
		name |= (eos_util_tx_inner_char_to_symbol(szName[i]) & 0x1f) << (64 - 5 * (i + 1));
	}

	// The for-loop encoded up to 60 high bits into uint64 'name' variable,
	// if (strlen(str) > 12) then encode str[12] into the low (remaining)
	// 4 bits of 'name'
	if (i == 12)
		name |= eos_util_tx_inner_char_to_symbol(szName[12]) & 0x0F;

	iOffset = 0;
	if (pbData)
	{
		if ((*pnDataLen - iOffset) < sizeof(name))
		{
			iRtn = -1;
			goto END;
		}
		for (i = 0; i < sizeof(name); i++)
		{
			pbData[iOffset + i] = (unsigned char)((name >> (i * 8)) & iFilterMask);
		}
	}
	iOffset += sizeof(name);

	*pnDataLen = iOffset;

	iRtn = 0;
END:
	return iRtn;
}

int eos_util_tx_name_from_json(const cJSON * const pJson, char szName[EOS_NAME_MAX_LEN]);

int eos_util_tx_from_string(const char * const szJsonString, eos_transaction * const pstTx);
