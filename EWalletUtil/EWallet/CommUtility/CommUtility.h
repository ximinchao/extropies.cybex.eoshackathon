#ifndef _COMMUTIL_H_
#define _COMMUTIL_H_

#include <stdint.h>
#ifndef _WIN32
#include <stddef.h> //for size_t
#endif //_WIN32

#define COMMUTIL_RET_OK						0x00000000
#define COMMUTIL_RET_UNKOWN					0x80000001
#define COMMUTIL_RET_ARGUMENTBAD			0x80000002
#define COMMUTIL_RET_BUFFER_TOO_SMALL		0x8000000B

#ifdef __cplusplus
extern "C" {
#endif

//String
int CommUtil_String2Hex(const char *szStringBuffer, unsigned char * const pucHexBuffer, size_t * const pnHexBufLen);

#ifdef __cplusplus
}
#endif

#endif // _COMMUTIL_H_
