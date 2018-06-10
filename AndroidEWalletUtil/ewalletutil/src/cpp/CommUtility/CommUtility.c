#include "CommUtility.h"

#include <string.h>

int CommUtil_String2Hex(const char *szStringBuffer, unsigned char * const pucHexBuffer, size_t * const pnHexBufLen)
{
    int				iRtn = COMMUTIL_RET_UNKOWN;
    const char		*pucString = (char *)0;
    size_t			nStringBufferLen = 0;
    size_t			i = 0, j = 0, k = 0;

    nStringBufferLen = strlen(szStringBuffer);

    if (!szStringBuffer || !nStringBufferLen || (nStringBufferLen % 2) || !pnHexBufLen)
    {
        iRtn = COMMUTIL_RET_ARGUMENTBAD;
        goto END;
    }

    if (!pucHexBuffer)
    {
        *pnHexBufLen = nStringBufferLen / 2;
        iRtn = COMMUTIL_RET_OK;
        goto END;
    }

    if ((*pnHexBufLen) < (nStringBufferLen / 2))
    {
        iRtn = COMMUTIL_RET_BUFFER_TOO_SMALL;
        goto END;
    }

    k = 0;
    pucString = szStringBuffer;
    for (i = 0; i < nStringBufferLen / 2; i++)
    {
        pucHexBuffer[k] = 0;
        for (j = 0;j < 2;j++)
        {
            if (pucString[j] >= '0' && pucString[j] <= '9')
            {
                pucHexBuffer[k] += pucString[j] - '0';
            }
            else if (pucString[j] >= 'A' && pucString[j] <= 'F')
            {
                pucHexBuffer[k] += (10 + pucString[j] - 'A');
            }
            else if (pucString[j] >= 'a' && pucString[j] <= 'f')
            {
                pucHexBuffer[k] += (10 + pucString[j] - 'a');
            }
            else
            {
                iRtn = COMMUTIL_RET_ARGUMENTBAD;
                goto END;
            }

            if (j == 0)
            {
                pucHexBuffer[k] = (pucHexBuffer[k] << 4);
            }
        }
        pucString += 2;
        k++;
    }

    *pnHexBufLen = k;
    iRtn = COMMUTIL_RET_OK;
    END:
    return iRtn;
}