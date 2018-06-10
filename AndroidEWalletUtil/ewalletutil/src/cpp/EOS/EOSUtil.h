#ifndef _PA_EOS_UTIL_H_
#define _PA_EOS_UTIL_H_

#include <stdint.h>

#include "cJSON.h"

//time
typedef uint32_t	time_point_sec;
//names
typedef uint64_t	account_name;
typedef uint64_t	action_name;
typedef uint64_t	permission_name;
//unsined_int
typedef uint32_t	unsigned_int;

#define EOS_NAME_MAX_LEN	16

// typedef struct _eos_permission_level {
// 	account_name	actor;
// 	permission_name	permission;
// } eos_permission_level;
typedef struct _eos_permission_level {
	char	actor[EOS_NAME_MAX_LEN];
	char	permission[EOS_NAME_MAX_LEN];
} eos_permission_level;

// typedef struct _eos_action {
// 	account_name				account;
// 	action_name					name;
// 	unsigned_int				auth_count;
// 	eos_permission_level		*authorization;
// 	unsigned_int				data_len;
// 	unsigned char				*data;
// } eos_action;
typedef struct _eos_action {
	char						account[EOS_NAME_MAX_LEN];
	char						name[EOS_NAME_MAX_LEN];
	unsigned_int				auth_count;
	eos_permission_level		*authorization;
	unsigned_int				data_len;
	unsigned char				*data;
} eos_action;

typedef struct _eos_transaction_extension {
	uint16_t		ext_type;
	//vector
	unsigned_int	ext_value_count;
	char			*ext_value;
} eos_transaction_extension;

typedef struct _eos_transaction_header {
	time_point_sec		expiration;   ///< the time at which a transaction expires
	//uint16_t			region; ///< the computational memory region this transaction applies to.
	uint16_t			ref_block_num; ///< specifies a block num in the last 2^16 blocks.
	uint32_t			ref_block_prefix; ///< specifies the lower 32 bits of the block id at get_ref_blocknum
	unsigned_int		max_net_usage_words; /// upper limit on total network bandwidth (in 8 byte words) billed for this transaction
	uint8_t				max_cpu_usage_ms; /// upper limit on the total CPU time billed for this transaction
	unsigned_int		delay_sec; /// number of seconds to delay this transaction for during which it may be canceled.
} eos_transaction_header;

typedef struct _eos_transaction {
	eos_transaction_header		header;
	//vector
	unsigned_int				cf_actions_count;
	eos_action					*context_free_actions;
	//vector
	unsigned_int				actions_count;
	eos_action					*actions;
	//vector
	unsigned_int				trans_ext_count;
	eos_transaction_extension	*transaction_extensions;
} eos_transaction;

typedef struct _eos_signature_type {
	unsigned char	*signature;
	size_t			sig_len;
} eos_signature_type;

typedef struct _eos_signed_transaction {
	eos_transaction			transaction;
	eos_signature_type		*signatures;
	size_t					sig_count;
	unsigned char			*context_free_data; ///< for each context-free action, there is an entry here
	size_t					cf_data_len;
} eos_signed_transaction;

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
int eos_util_tx_from_string(const char * const szJsonString, eos_transaction * const pstTx);

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
int eos_util_tx_name_set(const char szName[EOS_NAME_MAX_LEN], unsigned char * const pbData, size_t * const pnDataLen);
int eos_util_tx_name_from_json(const cJSON * const pJson, char szName[EOS_NAME_MAX_LEN]);

#endif //_PA_EOS_UTIL_H_
