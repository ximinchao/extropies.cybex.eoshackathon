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

int eos_util_tx_action_get(const unsigned char * const pbData, const size_t nDataLen, eos_action * const pstAction, size_t * const pnProcessDataLen)
{
	int	iRtn = -1;

	size_t	i = 0, iOffset = 0, iProcessLen = 0;

	if (!pbData || !nDataLen || !pstAction)
	{
		iRtn = -1;
		goto END;
	}

	eos_util_tx_action_clear(pstAction);

	iOffset = 0;

	// 	account_name				account;
	iRtn = eos_util_tx_name_get(pbData + iOffset, nDataLen - iOffset, pstAction->account, &iProcessLen);
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	action_name					name;
	iRtn = eos_util_tx_name_get(pbData + iOffset, nDataLen - iOffset, pstAction->name, &iProcessLen);
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	unsigned_int				auth_count;
	iRtn = eos_util_tx_uint_get(pbData + iOffset, nDataLen - iOffset, &pstAction->auth_count, &iProcessLen);
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	eos_permission_level		*authorization;
	if (pstAction->auth_count > 0)
	{
		pstAction->authorization = (eos_permission_level *)malloc((size_t)(pstAction->auth_count) * sizeof(eos_permission_level));
		if (pstAction->authorization == 0)
		{
			iRtn = -1;
			goto END;
		}
		memset(pstAction->authorization, 0, sizeof(eos_permission_level) * (size_t)(pstAction->auth_count));

		for (i = 0; i < pstAction->auth_count; i++)
		{
			iRtn = eos_util_tx_permission_get(pbData + iOffset, nDataLen - iOffset, pstAction->authorization + i, &iProcessLen);
			if (iRtn)
			{
				iRtn = -1;
				goto END;
			}
			iOffset += iProcessLen;
		}
	}

	// 	unsigned_int				data_len;
	iRtn = eos_util_tx_uint_get(pbData + iOffset, nDataLen - iOffset, &pstAction->data_len, &iProcessLen);
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;
	// 	unsigned char				*data;
	if (pstAction->data_len > 0)
	{
		if (((size_t)pstAction->data_len) > (nDataLen - iOffset))
		{
			iRtn = -1;
			goto END;
		}

		pstAction->data = (unsigned char *)malloc((size_t)(pstAction->data_len));
		if (pstAction->data == 0)
		{
			iRtn = -1;
			goto END;
		}
		memset(pstAction->data, 0, (size_t)(pstAction->data_len));

		memcpy(pstAction->data, pbData + iOffset, (size_t)(pstAction->data_len));
		iOffset += (size_t)(pstAction->data_len);
	}

	if (pnProcessDataLen)
	{
		*pnProcessDataLen = iOffset;
	}

	iRtn = 0;
END:
	if (iRtn != 0)
	{
		eos_util_tx_action_clear(pstAction);
	}
	return iRtn;
}

int eos_util_tx_action_set(const eos_action * const pstAction, unsigned char * const pbData, size_t * const pnDataLen)
{
	int	iRtn = -1;

	size_t	i = 0, iOffset = 0, iProcessLen = 0;

	if (!pstAction || !pnDataLen)
	{
		iRtn = -1;
		goto END;
	}

	iOffset = 0;

	// 	char						account[EOS_NAME_MAX_LEN];
	if (pbData)
	{
		iProcessLen = (*pnDataLen - iOffset);
		iRtn = eos_util_tx_name_set(pstAction->account, pbData + iOffset, &iProcessLen);
	}
	else
	{
		iRtn = eos_util_tx_name_set(pstAction->account, 0, &iProcessLen);
	}
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	char						name[EOS_NAME_MAX_LEN];
	if (pbData)
	{
		iProcessLen = (*pnDataLen - iOffset);
		iRtn = eos_util_tx_name_set(pstAction->name, pbData + iOffset, &iProcessLen);
	}
	else
	{
		iRtn = eos_util_tx_name_set(pstAction->name, 0, &iProcessLen);
	}
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	unsigned_int				auth_count;
	if (pbData)
	{
		iProcessLen = (*pnDataLen - iOffset);
		iRtn = eos_util_tx_uint_set(&pstAction->auth_count, pbData + iOffset, &iProcessLen);
	}
	else
	{
		iRtn = eos_util_tx_uint_set(&pstAction->auth_count, 0, &iProcessLen);
	}
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	eos_permission_level		*authorization;
	for (i = 0; i < pstAction->auth_count; i++)
	{
		if (pbData)
		{
			iProcessLen = (*pnDataLen - iOffset);
			iRtn = eos_util_tx_permission_set(pstAction->authorization + i, pbData + iOffset, &iProcessLen);
		}
		else
		{
			iRtn = eos_util_tx_permission_set(pstAction->authorization + i, 0, &iProcessLen);
		}
		if (iRtn)
		{
			iRtn = -1;
			goto END;
		}
		iOffset += iProcessLen;
	}

	// 	unsigned_int				data_len;
	if (pbData)
	{
		iProcessLen = (*pnDataLen - iOffset);
		iRtn = eos_util_tx_uint_set(&pstAction->data_len, pbData + iOffset, &iProcessLen);
	}
	else
	{
		iRtn = eos_util_tx_uint_set(&pstAction->data_len, 0, &iProcessLen);
	}
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	unsigned char				*data;
	if (pbData && pstAction->data_len)
	{
		if ((*pnDataLen - iOffset) < (size_t)pstAction->data_len)
		{
			iRtn = -1;
			goto END;
		}
		memcpy(pbData + iOffset, pstAction->data, (size_t)(pstAction->data_len));
	}
	iOffset += (size_t)(pstAction->data_len);

	*pnDataLen = iOffset;

	iRtn = 0;
END:
	return iRtn;
}

int eos_util_tx_action_clear(eos_action * const pstAction)
{
	int	iRtn = -1;

	size_t	i = 0;

	if (!pstAction)
	{
		iRtn = -1;
		goto END;
	}

	// 	char						account[EOS_NAME_MAX_LEN];
	memset(pstAction->account, 0, sizeof(pstAction->account));

	// 	char						name[EOS_NAME_MAX_LEN];
	memset(pstAction->account, 0, sizeof(pstAction->name));

	// 	eos_permission_level		*authorization;
	if (pstAction->authorization)
	{
		for (i = 0; i < pstAction->auth_count; i++)
		{
			memset(pstAction->authorization + i, 0, sizeof(eos_permission_level));
		}
		free(pstAction->authorization);
		pstAction->authorization = 0;
	}
	// 	unsigned_int				auth_count;
	pstAction->auth_count = 0;

	// 	unsigned char				*data;
	if (pstAction->data)
	{
		free(pstAction->data);
		pstAction->data = 0;
	}
	// 	unsigned_int				data_len;
	pstAction->data_len = 0;

	iRtn = 0;
END:
	return iRtn;
}

int eos_util_tx_action_from_json(const cJSON * const pJson, eos_action * const pstAction);

int eos_util_tx_trans_ext_get(const unsigned char * const pbData, const size_t nDataLen, eos_transaction_extension * const pstTransExt, size_t * const pnProcessDataLen)
{
	int	iRtn = -1;

	size_t	i = 0, iOffset = 0, iProcessLen = 0;

	if (!pbData || !nDataLen || !pstTransExt)
	{
		iRtn = -1;
		goto END;
	}

	eos_util_tx_trans_ext_clear(pstTransExt);

	iOffset = 0;

	// 	uint16_t		ext_type;
	if (sizeof(pstTransExt->ext_type) > (nDataLen - iOffset))
	{
		iRtn = -1;
		goto END;
	}
	pstTransExt->ext_type = 0;
	for (i = 0; i < sizeof(pstTransExt->ext_type); i++)
	{
		pstTransExt->ext_type |= ((uint16_t)pbData[iOffset + i]) << (i * 8);
	}
	iOffset += sizeof(pstTransExt->ext_type);

	// 	unsigned_int	ext_value_count;
	iRtn = eos_util_tx_uint_get(pbData + iOffset, nDataLen - iOffset, &pstTransExt->ext_value_count, &iProcessLen);
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	char			*ext_value;
	if (pstTransExt->ext_value_count > 0)
	{
		if (((size_t)pstTransExt->ext_value_count) > (nDataLen - iOffset))
		{
			iRtn = -1;
			goto END;
		}

		pstTransExt->ext_value = (char *)malloc((size_t)(pstTransExt->ext_value_count));
		if (pstTransExt->ext_value == 0)
		{
			iRtn = -1;
			goto END;
		}
		memset(pstTransExt->ext_value, 0, (size_t)(pstTransExt->ext_value_count));

		memcpy(pstTransExt->ext_value, pbData + iOffset, (size_t)(pstTransExt->ext_value_count));
		iOffset += (size_t)(pstTransExt->ext_value_count);
	}

	if (pnProcessDataLen)
	{
		*pnProcessDataLen = iOffset;
	}

	iRtn = 0;
END:
	if (iRtn != 0)
	{
		eos_util_tx_trans_ext_clear(pstTransExt);
	}
	return iRtn;
}

int eos_util_tx_trans_ext_set(const eos_transaction_extension * const pstTransExt, unsigned char * const pbData, size_t * const pnDataLen)
{
	int	iRtn = -1;

	const int32_t	iFilterMask = 0xff;
	size_t			i = 0, iOffset = 0, iProcessLen = 0;

	if (!pstTransExt || !pnDataLen)
	{
		iRtn = -1;
		goto END;
	}

	iOffset = 0;

	// 	uint16_t		ext_type;
	if (pbData)
	{
		if ((*pnDataLen - iOffset) < sizeof(pstTransExt->ext_type))
		{
			iRtn = -1;
			goto END;
		}
		for (i = 0; i < sizeof(pstTransExt->ext_type); i++)
		{
			pbData[iOffset + i] = (unsigned char)((pstTransExt->ext_type >> (i * 8)) & iFilterMask);
		}
	}
	iOffset += sizeof(pstTransExt->ext_type);

	// 	unsigned_int	ext_value_count;
	if (pbData)
	{
		iProcessLen = (*pnDataLen - iOffset);
		iRtn = eos_util_tx_uint_set(&pstTransExt->ext_value_count, pbData + iOffset, &iProcessLen);
	}
	else
	{
		iRtn = eos_util_tx_uint_set(&pstTransExt->ext_value_count, 0, &iProcessLen);
	}
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	char			*ext_value;
	if (pbData && pstTransExt->ext_value_count)
	{
		if ((*pnDataLen - iOffset) < (size_t)pstTransExt->ext_value_count)
		{
			iRtn = -1;
			goto END;
		}
		memcpy(pbData + iOffset, pstTransExt->ext_value, (size_t)(pstTransExt->ext_value_count));
	}
	iOffset += (size_t)(pstTransExt->ext_value_count);

	*pnDataLen = iOffset;

	iRtn = 0;
END:
	return iRtn;
}

int eos_util_tx_trans_ext_clear(eos_transaction_extension * const pstTransExt)
{
	int	iRtn = -1;

	size_t	i = 0;

	if (!pstTransExt)
	{
		iRtn = -1;
		goto END;
	}

	// 	uint16_t		ext_type;
	pstTransExt->ext_type = 0;

	// 	char			*ext_value;
	if (pstTransExt->ext_value)
	{
		free(pstTransExt->ext_value);
		pstTransExt->ext_value = 0;
	}
	// 	unsigned_int	ext_value_count;
	pstTransExt->ext_value_count = 0;

	iRtn = 0;
END:
	return iRtn;
}

int eos_util_tx_trans_ext_from_json(const cJSON * const pJson, eos_transaction_extension * const pstTransExt);

int eos_util_tx_permission_get(const unsigned char * const pbData, const size_t nDataLen, eos_permission_level * const pstPermission, size_t * const pnProcessDataLen)
{
	int	iRtn = -1;

	size_t	i = 0, iOffset = 0, iProcessLen = 0;

	if (!pbData || !nDataLen || !pstPermission)
	{
		iRtn = -1;
		goto END;
	}

	iOffset = 0;

	// 	char	actor[EOS_NAME_MAX_LEN];
	iRtn = eos_util_tx_name_get(pbData + iOffset, nDataLen - iOffset, pstPermission->actor, &iProcessLen);
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	char	permission[EOS_NAME_MAX_LEN];
	iRtn = eos_util_tx_name_get(pbData + iOffset, nDataLen - iOffset, pstPermission->permission, &iProcessLen);
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	if (pnProcessDataLen)
	{
		*pnProcessDataLen = iOffset;
	}

	iRtn = 0;
END:
	return iRtn;
}

int eos_util_tx_permission_set(const eos_permission_level * const pstPermission, unsigned char * const pbData, size_t * const pnDataLen)
{
	int	iRtn = -1;

	size_t			i = 0, iOffset = 0, iProcessLen = 0;

	if (!pstPermission || !pnDataLen)
	{
		iRtn = -1;
		goto END;
	}

	iOffset = 0;

	// 	char	actor[EOS_NAME_MAX_LEN];
	if (pbData)
	{
		iProcessLen = (*pnDataLen - iOffset);
		iRtn = eos_util_tx_name_set(pstPermission->actor, pbData + iOffset, &iProcessLen);
	}
	else
	{
		iRtn = eos_util_tx_name_set(pstPermission->actor, 0, &iProcessLen);
	}
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	// 	char	permission[EOS_NAME_MAX_LEN];
	if (pbData)
	{
		iProcessLen = (*pnDataLen - iOffset);
		iRtn = eos_util_tx_name_set(pstPermission->permission, pbData + iOffset, &iProcessLen);
	}
	else
	{
		iRtn = eos_util_tx_name_set(pstPermission->permission, 0, &iProcessLen);
	}
	if (iRtn)
	{
		iRtn = -1;
		goto END;
	}
	iOffset += iProcessLen;

	*pnDataLen = iOffset;

	iRtn = 0;
END:
	return iRtn;
}

int eos_util_tx_permission_from_json(const cJSON * const pJson, eos_permission_level * const pstPermission);

int eos_util_tx_uint_get(const unsigned char * const pbData, const size_t nDataLen, unsigned_int * const pstUint, size_t * const pnProcessDataLen)
{
	int	iRtn = -1;

	size_t			i = 0, iOffset = 0;
	uint64_t		v = 0;
	unsigned char	b = 0;
	uint8_t			by = 0;

	if (!pbData || !nDataLen || !pstUint)
	{
		iRtn = -1;
		goto END;
	}

	iOffset = 0;

	v = 0;
	by = 0;
	do
	{
		b = pbData[iOffset++];
		v |= (uint32_t)(b & 0x7f) << by;
		by += 7;
	} while (b & 0x80);
	*pstUint = (unsigned_int)(v);

	if (pnProcessDataLen)
	{
		*pnProcessDataLen = iOffset;
	}

	iRtn = 0;
END:
	return iRtn;
}

int eos_util_tx_uint_set(const unsigned_int * const pstUint, unsigned char * const pbData, size_t * const pnDataLen)
{
	int	iRtn = -1;

	size_t			iOffset = 0;
	uint64_t		val = 0;
	uint8_t			b = 0;

	if (!pstUint || !pnDataLen)
	{
		iRtn = -1;
		goto END;
	}

	iOffset = 0;

	val = *pstUint;
	do
	{
		b = ((uint8_t)val) & 0x7f;
		val >>= 7;
		b |= ((val > 0) << 7);

		if (pbData)
		{
			pbData[iOffset] = b;
		}
		iOffset += 1;
	} while (val);

	*pnDataLen = iOffset;

	iRtn = 0;
END:
	return iRtn;
}

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
