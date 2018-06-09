/**
 *  @file
 *  @copyright defined in eos/LICENSE.txt
 */
#include <eosiolib/eosio.hpp>
#include <eosiolib/asset.hpp>
#include <eosiolib/contract.hpp>
#include <eosiolib/print.hpp>
#include <eosiolib/crypto.h>
#include <eosiolib/time.hpp>

using namespace eosio;

#include <string>
using std::string;

#include <stdlib.h> //for malloc and free

class consale : public eosio::contract {
	private:
		//@abi table conference i64
		struct conference {
			uint64_t		id;
			account_name	organizer;
			string			conf_name;
			asset			fee;

			EOSLIB_SERIALIZE( conference, (id)(organizer)(conf_name)(fee) )
		};

		//@abi table attable i64
		struct attable {
			uint64_t		id;
			account_name	attendee;
			uint64_t		conf_id;
			public_key		attend_pub;
			signature		attend_sig;
			time_point_sec		attend_timestamp;
			bool			fee_locked;
			asset			fee;

			EOSLIB_SERIALIZE( attable, (id)(attendee)(conf_id)(attend_pub)(attend_sig)(attend_timestamp)(fee_locked)(fee) )
		};

	public:
		consale(account_name self)
			:eosio::contract(self)
		{}

		//@abi action
		void create(const string conf_name, const account_name organizer, const asset& fee)
		{
		}

		//@abi action
		void getlist()
		{
		}

		//@abi action
		void cancel(const string& conf_name, const account_name organizer)
		{
		}

		//@abi action
		void getinfo(const string& conf_name, const account_name organizer)
		{

		}

		//@abi action
		void regist(const string& conf_name, const account_name organizer, const account_name attendee, const public_key& attend_pub)
		{
		}

		//@abi action
		void withdraw(const string& conf_name, const account_name organizer, const account_name attendee)
		{
		}

		//@abi action
		void getreg(const account_name attendee)
		{
		}

		//@abi action
		void checkintest(const string& conf_name, const account_name organizer, const account_name attendee, const signature attend_sig, const bytes testdata)
		{

		}

		int uint_set(const unsigned_int * const pstUint, unsigned char * const pbData, size_t * const pnDataLen)
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

		//@abi action
		void checkin(const string& conf_name, const account_name organizer, const account_name attendee, const signature attend_sig)
		{

		}
};

EOSIO_ABI( consale, (create)(getlist)(cancel)(getinfo)(regist)(withdraw)(getreg)(checkintest)(checkin) )

