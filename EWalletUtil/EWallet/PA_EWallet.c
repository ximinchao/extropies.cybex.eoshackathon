#include "config.h"

#include "PA_EWallet.h"
#include "EOS/EOSUtil.h"

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

int EWALLET_API PAEW_EOS_TX_Serialize(const char * const szTransactionString, unsigned char * const pbTransactionData, size_t * pnTransactionLen)
{
    int	iRtn = PAEW_RET_UNKNOWN_FAIL;

    iRtn = PAEW_RET_SUCCESS;
END:
    return iRtn;
}

int EWALLET_API PAEW_EOS_TX_Part_Serialize(const unsigned int nPartIndex, const char * const szTransactionString, unsigned char * const pbTransactionData, size_t * pnTransactionLen)
{
    int	iRtn = PAEW_RET_UNKNOWN_FAIL;

    iRtn = PAEW_RET_SUCCESS;
END:
    return iRtn;
}
