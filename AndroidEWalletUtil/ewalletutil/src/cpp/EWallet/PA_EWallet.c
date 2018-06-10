#include "PA_EWallet.h"
#include "../EOS/EOSUtil.h"

#include <stdlib.h> //for malloc/free
#include <string.h> //for memset
#include <stdio.h> //for sprintf

#define PA_SIG_DATA_MAX_LEN			2048

#define PA_ASSERT_EQ_EX(_rtn,_val,_err_num) \
do\
{\
	if(_rtn!=_val)\
	{\
			iRtn=_err_num;\
			goto END;\
	}\
} while(0)

#define PA_ASSERT_NEQ_EX(_rtn,_val,_err_num) \
do\
{\
	if(_rtn==_val)\
	{\
			iRtn=_err_num;\
			goto END;\
	}\
} while(0)

int PAEW_EOS_TX_Serialize(const char * const szTransactionString, unsigned char * const pbTransactionData, size_t * pnTransactionLen)
{
    int	iRtn = PAEW_RET_UNKNOWN_FAIL;

    eos_transaction	eos_tx;
    unsigned char	nTxInited = 0;
    size_t			nLocalTxBinLen = 0;

    PA_ASSERT_NEQ_EX(szTransactionString, 0, PAEW_RET_ARGUMENTBAD);
    PA_ASSERT_NEQ_EX(pnTransactionLen, 0, PAEW_RET_ARGUMENTBAD);

    memset(&eos_tx, 0, sizeof(eos_tx));
    nTxInited = 1;

    iRtn = eos_util_tx_from_string(szTransactionString, &eos_tx);
    PA_ASSERT_EQ_EX(iRtn, 0, PAEW_RET_TX_PARSE_FAIL);

    iRtn = eos_util_tx_set(&eos_tx, 0, &nLocalTxBinLen);
    PA_ASSERT_EQ_EX(iRtn, 0, PAEW_RET_TX_PARSE_FAIL);

    if (!pbTransactionData)
    {
        *pnTransactionLen = nLocalTxBinLen;
        iRtn = PAEW_RET_SUCCESS;
        goto END;
    }
    if (*pnTransactionLen < nLocalTxBinLen)
    {
        *pnTransactionLen = nLocalTxBinLen;
        iRtn = PAEW_RET_BUFFER_TOO_SAMLL;
        goto END;
    }

    iRtn = eos_util_tx_set(&eos_tx, pbTransactionData, &nLocalTxBinLen);
    PA_ASSERT_EQ_EX(iRtn, 0, PAEW_RET_TX_PARSE_FAIL);

    *pnTransactionLen = nLocalTxBinLen;

    iRtn = PAEW_RET_SUCCESS;
    END:
    if (nTxInited)
    {
        eos_util_tx_clear(&eos_tx);
    }
    return iRtn;
}

int PAEW_EOS_TX_Part_Serialize(const unsigned int nPartIndex, const char * const szTransactionString, unsigned char * const pbTransactionData, size_t * pnTransactionLen)
{
    int	iRtn = PAEW_RET_UNKNOWN_FAIL;

    eos_transaction	eos_tx;
    unsigned char	nTxInited = 0;
    size_t			nLocalTxBinLen = 0;

    unsigned char	pbInputData[PA_SIG_DATA_MAX_LEN] = { 0 };
    size_t			nInputDataLen = 0, nOffset = 0, i = 0;

    PA_ASSERT_NEQ_EX(szTransactionString, 0, PAEW_RET_ARGUMENTBAD);
    PA_ASSERT_NEQ_EX(pnTransactionLen, 0, PAEW_RET_ARGUMENTBAD);

    memset(&eos_tx, 0, sizeof(eos_tx));
    nTxInited = 1;

    iRtn = eos_util_tx_from_string(szTransactionString, &eos_tx);
    PA_ASSERT_EQ_EX(iRtn, 0, PAEW_RET_TX_PARSE_FAIL);

    if (nPartIndex == PAEW_SIG_EOS_TX_HEADER)
    {
        nOffset = 0;

        nInputDataLen = sizeof(pbInputData) - nOffset;
        iRtn = eos_util_tx_header_set(&eos_tx.header, pbInputData + nOffset, &nInputDataLen);
        PA_ASSERT_EQ_EX(iRtn, PAEW_RET_SUCCESS, PAEW_RET_TX_PARSE_FAIL);
        nOffset += nInputDataLen;

        nInputDataLen = sizeof(pbInputData) - nOffset;
        iRtn = eos_util_tx_uint_set(&eos_tx.cf_actions_count, pbInputData + nOffset, &nInputDataLen);
        PA_ASSERT_EQ_EX(iRtn, PAEW_RET_SUCCESS, PAEW_RET_TX_PARSE_FAIL);
        nOffset += nInputDataLen;

        for (i = 0; i < eos_tx.cf_actions_count; i++)
        {
            nInputDataLen = sizeof(pbInputData) - nOffset;
            iRtn = eos_util_tx_action_set(&eos_tx.context_free_actions[i], pbInputData + nOffset, &nInputDataLen);
            PA_ASSERT_EQ_EX(iRtn, PAEW_RET_SUCCESS, PAEW_RET_TX_PARSE_FAIL);
            nOffset += nInputDataLen;
        }

        nLocalTxBinLen = nOffset;
    }
    else if (nPartIndex == PAEW_SIG_EOS_TX_ACTION_COUNT)
    {
        nOffset = 0;

        nInputDataLen = sizeof(pbInputData) - nOffset;
        iRtn = eos_util_tx_uint_set(&eos_tx.actions_count, pbInputData + nOffset, &nInputDataLen);
        PA_ASSERT_EQ_EX(iRtn, PAEW_RET_SUCCESS, PAEW_RET_TX_PARSE_FAIL);
        nOffset += nInputDataLen;

        nLocalTxBinLen = nOffset;
    }
    else if (nPartIndex == PAEW_SIG_EOS_TX_ACTION)
    {
        nOffset = 0;
        for (i = 0; i < eos_tx.actions_count; i++)
        {
            nInputDataLen = sizeof(pbInputData) - nOffset;
            iRtn = eos_util_tx_action_set(&eos_tx.actions[i], pbInputData + nOffset, &nInputDataLen);
            PA_ASSERT_EQ_EX(iRtn, PAEW_RET_SUCCESS, PAEW_RET_TX_PARSE_FAIL);
            nOffset += nInputDataLen;
        }
        nLocalTxBinLen = nOffset;
    }
    else if (nPartIndex == PAEW_SIG_EOS_TX_CF_HASH)
    {
        //trans_ext_count
        nOffset = 0;

        nInputDataLen = sizeof(pbInputData) - nOffset;
        iRtn = eos_util_tx_uint_set(&eos_tx.trans_ext_count, pbInputData + nOffset, &nInputDataLen);
        PA_ASSERT_EQ_EX(iRtn, PAEW_RET_SUCCESS, PAEW_RET_TX_PARSE_FAIL);
        nOffset += nInputDataLen;

        //transaction_extensions
        for (i = 0; i < eos_tx.trans_ext_count; i++)
        {
            nInputDataLen = sizeof(pbInputData) - nOffset;
            iRtn = eos_util_tx_trans_ext_set(&eos_tx.transaction_extensions[i], pbInputData + nOffset, &nInputDataLen);
            PA_ASSERT_EQ_EX(iRtn, PAEW_RET_SUCCESS, PAEW_RET_TX_PARSE_FAIL);
            nOffset += nInputDataLen;
        }

        nLocalTxBinLen = nOffset;
    }
    else
    {
        iRtn = PAEW_RET_ARGUMENTBAD;
        goto END;
    }

    if (!pbTransactionData)
    {
        *pnTransactionLen = nLocalTxBinLen;
        iRtn = PAEW_RET_SUCCESS;
        goto END;
    }
    if (*pnTransactionLen < nLocalTxBinLen)
    {
        *pnTransactionLen = nLocalTxBinLen;
        iRtn = PAEW_RET_BUFFER_TOO_SAMLL;
        goto END;
    }

    memcpy(pbTransactionData, pbInputData, nLocalTxBinLen);
    PA_ASSERT_EQ_EX(iRtn, 0, PAEW_RET_TX_PARSE_FAIL);

    *pnTransactionLen = nLocalTxBinLen;

    iRtn = PAEW_RET_SUCCESS;
    END:
    if (nTxInited)
    {
        eos_util_tx_clear(&eos_tx);
    }
    return iRtn;
}
