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
			checksum256		conf_name_hash;
			asset			fee;

			uint64_t primary_key() const { return id; }

			static key256 get_key256_from_checksum256(const checksum256& hash)
			{
				const uint64_t *p64 = reinterpret_cast<const uint64_t *>(&hash);
				return key256::make_from_word_sequence<uint64_t>(p64[0], p64[1], p64[2], p64[3]);

			}

			static key256 get_key256_from_string(const string& content)
			{
				checksum256    res256;

				sha256((char *)content.c_str(), content.length(), &res256);
				return get_key256_from_checksum256(res256);
			}	


			key256 by_conf_name_hash() const
			{
				return get_key256_from_checksum256(conf_name_hash);
			}

			uint64_t by_organizer() const
			{
				return (uint64_t)organizer;
			}

			EOSLIB_SERIALIZE( conference, (id)(organizer)(conf_name)(conf_name_hash)(fee) )
		};

		typedef eosio::multi_index< N(conference), conference,
				indexed_by< N(organizer), const_mem_fun< conference, uint64_t, &conference::by_organizer > >,
				indexed_by< N(conf_name_hash), const_mem_fun< conference, key256, &conference::by_conf_name_hash > >
					> confs;

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

			uint64_t primary_key() const { return id; }
			uint64_t by_conf_id() const { return conf_id; }
			uint64_t by_attendee() const { return (uint64_t)attendee; }

			EOSLIB_SERIALIZE( attable, (id)(attendee)(conf_id)(attend_pub)(attend_sig)(attend_timestamp)(fee_locked)(fee) )

				typedef eosio::multi_index< N(attable), attable,
						indexed_by< N(attendee), const_mem_fun< attable, uint64_t, &attable::by_attendee > >,
						indexed_by< N(conf_id), const_mem_fun< attable, uint64_t, &attable::by_conf_id > >
							> attables;
		};

	public:
		consale(account_name self)
			:eosio::contract(self)
		{}

		//@abi action
		void create(const string conf_name, const account_name organizer, const asset& fee)
		{
			//eosio_assert( fee.symbol == S(4,SYS) , "only SYS token allowed" );
			eosio_assert( fee.is_valid(), "invalid fee" );
			eosio_assert( fee.amount >= 0, "fee must be positive or zero quantity" );

			require_auth( organizer );

			confs	cur_confs(_self, _self);
			auto	idx = cur_confs.template get_index<N(conf_name_hash)>();
			key256	dest_hash = conference::get_key256_from_string(conf_name);
			auto	cur_conf_itr = idx.find(dest_hash);

			while ((cur_conf_itr != idx.end()) && (conference::get_key256_from_checksum256(cur_conf_itr->conf_name_hash) == dest_hash))
			{
				if (cur_conf_itr->organizer == organizer)
				{
					eosio_assert((cur_conf_itr->conf_name != conf_name), "conference already exists");
				}
				++cur_conf_itr; //MUST NOT BE cur_conf_itr++, which won't change cur_conf_itr's value
			}

			// Store new confer
			auto new_conf_itr = cur_confs.emplace(organizer, [&](auto& conf) {
					conf.id = cur_confs.available_primary_key();
					conf.organizer = organizer;
					conf.conf_name = conf_name;
					sha256((char *)conf_name.c_str(), conf_name.length(), &conf.conf_name_hash);
					conf.fee = fee;
					});
		}

		//@abi action
		void getlist()
		{
			confs	cur_confs(_self, _self);
			auto	cur_conf_itr = cur_confs.begin();

			while (cur_conf_itr != cur_confs.end())
			{
				print("Conference id: ", cur_conf_itr->id, ", ", "Conference name: ", cur_conf_itr->conf_name.c_str(), ", ", "Organizer name: ", name{cur_conf_itr->organizer}, ", ", "Fee: ", cur_conf_itr->fee, "\n");
				++cur_conf_itr; //MUST NOT BE cur_conf_itr++, which won't change cur_conf_itr's value
			}
		}

		//@abi action
		void cancel(const string& conf_name, const account_name organizer)
		{
			require_auth( organizer );

			confs	cur_confs(_self, _self);
			auto	idx = cur_confs.template get_index<N(conf_name_hash)>();
			key256	dest_hash = conference::get_key256_from_string(conf_name);
			auto	cur_conf_itr = idx.find(dest_hash);
			uint8_t	bFound = 0;

			while ((cur_conf_itr != idx.end()) && (conference::get_key256_from_checksum256(cur_conf_itr->conf_name_hash) == dest_hash))
			{
				if ((cur_conf_itr->conf_name == conf_name) && (cur_conf_itr->organizer == organizer))
				{
					bFound = 1;
					break;
				}
				++cur_conf_itr; //MUST NOT BE cur_conf_itr++, which won't change cur_conf_itr's value
			}

			eosio_assert(bFound != 0, "conference not found");

			print("Cancelled:\n");
			print("Conference id: ", cur_conf_itr->id, ", ", "Conference name: ", cur_conf_itr->conf_name.c_str(), ", ", "Organizer name: ", name{cur_conf_itr->organizer}, ", ", "Fee: ", cur_conf_itr->fee, "\n");
			idx.erase(cur_conf_itr);
		}

		//@abi action
		void getinfo(const string& conf_name, const account_name organizer)
		{
			confs	cur_confs(_self, _self);
			auto	idx = cur_confs.template get_index<N(conf_name_hash)>();
			key256	dest_hash = conference::get_key256_from_string(conf_name);
			auto	cur_conf_itr = idx.find(dest_hash);
			uint8_t	bFound = 0;

			while ((cur_conf_itr != idx.end()) && (conference::get_key256_from_checksum256(cur_conf_itr->conf_name_hash) == dest_hash))
			{
				if ((cur_conf_itr->conf_name == conf_name) && (cur_conf_itr->organizer == organizer))
				{
					bFound = 1;
					break;
				}
				++cur_conf_itr; //MUST NOT BE cur_conf_itr++, which won't change cur_conf_itr's value
			}

			eosio_assert(bFound != 0, "conference not found");

			print("Conference id: ", cur_conf_itr->id, ", ", "Conference name: ", cur_conf_itr->conf_name.c_str(), ", ", "Organizer name: ", name{cur_conf_itr->organizer}, ", ", "Fee: ", cur_conf_itr->fee, "\n");
		}

		//@abi action
		void regist(const string& conf_name, const account_name organizer, const account_name attendee, const public_key& attend_pub)
		{
			//require_auth( attendee );

			//get conf
			confs	cur_confs(_self, _self);
			auto	idx = cur_confs.template get_index<N(conf_name_hash)>();
			key256	dest_hash = conference::get_key256_from_string(conf_name);
			auto	cur_conf_itr = idx.find(dest_hash);
			uint8_t	bFound = 0;

			while ((cur_conf_itr != idx.end()) && (conference::get_key256_from_checksum256(cur_conf_itr->conf_name_hash) == dest_hash))
			{
				if ((cur_conf_itr->conf_name == conf_name) && (cur_conf_itr->organizer == organizer))
				{
					bFound = 1;
					break;
				}
				++cur_conf_itr; //MUST NOT BE cur_conf_itr++, which won't change cur_conf_itr's value
			}

			eosio_assert(bFound != 0, "conference not found");

			print("Conference id: ", cur_conf_itr->id, ", ", "Conference name: ", cur_conf_itr->conf_name.c_str(), ", ", "Organizer name: ", name{cur_conf_itr->organizer}, ", ", "Fee: ", cur_conf_itr->fee, "\n");

			//check attable
			attables	cur_attables(_self, _self);
			auto	reg_idx = cur_attables.template get_index<N(attendee)>();
			auto	cur_attable_itr = reg_idx.find(attendee);

			bFound = 0;
			while ((cur_attable_itr != reg_idx.end()) && (cur_attable_itr->attendee == attendee))
			{
				if (cur_attable_itr->conf_id == cur_conf_itr->id)
				{
					bFound = 1;
					break;
				}
				++cur_attable_itr; //MUST NOT BE cur_attable_itr++, which won't change cur_attable_itr's value
			}

			eosio_assert(bFound == 0, "conference already registered");

			// Store new attable
			auto	new_attable_itr = cur_attables.emplace(attendee, [&](auto& attable) {
					attable.id = cur_attables.available_primary_key();
					attable.attendee = attendee;
					attable.conf_id = cur_conf_itr->id;
					memcpy(&attable.attend_pub, &attend_pub, sizeof(attend_pub));
					memset(&attable.attend_sig, 0, sizeof(attable.attend_sig));
					attable.attend_timestamp = time_point_sec();
					attable.fee_locked = false;
					attable.fee = cur_conf_itr->fee;
					});
		}

		//@abi action
		void withdraw(const string& conf_name, const account_name organizer, const account_name attendee)
		{
			require_auth( attendee );

			//get conf
			confs	cur_confs(_self, _self);
			auto	idx = cur_confs.template get_index<N(conf_name_hash)>();
			key256	dest_hash = conference::get_key256_from_string(conf_name);
			auto	cur_conf_itr = idx.find(dest_hash);
			uint8_t	bFound = 0;

			while ((cur_conf_itr != idx.end()) && (conference::get_key256_from_checksum256(cur_conf_itr->conf_name_hash) == dest_hash))
			{
				if ((cur_conf_itr->conf_name == conf_name) && (cur_conf_itr->organizer == organizer))
				{
					bFound = 1;
					break;
				}
				++cur_conf_itr; //MUST NOT BE cur_conf_itr++, which won't change cur_conf_itr's value
			}

			eosio_assert(bFound != 0, "conference not found");

			print("Conference id: ", cur_conf_itr->id, ", ", "Conference name: ", cur_conf_itr->conf_name.c_str(), ", ", "Organizer name: ", name{cur_conf_itr->organizer}, ", ", "Fee: ", cur_conf_itr->fee, "\n");

			//check attable
			attables	cur_attables(_self, _self);
			auto	reg_idx = cur_attables.template get_index<N(attendee)>();
			auto	cur_attable_itr = reg_idx.find(attendee);

			bFound = 0;
			while ((cur_attable_itr != reg_idx.end()) && (cur_attable_itr->attendee == attendee))
			{
				if (cur_attable_itr->conf_id == cur_conf_itr->id)
				{
					bFound = 1;
					break;
				}
				++cur_attable_itr; //MUST NOT BE cur_attable_itr++, which won't change cur_attable_itr's value
			}

			eosio_assert(bFound == 1, "conference not registered yet");
			eosio_assert((cur_attable_itr->fee_locked == false), "conference already checked in");
			action	act(
					permission_level{ _self, N(active) }, 
					N(eosio.token), 
					N(transfer), 
					std::make_tuple( _self, attendee, cur_attable_itr->fee, std::string("") )
					);
			act.send();

			reg_idx.erase(cur_attable_itr);
		}

		//@abi action
		void getreg(const account_name attendee)
		{
			attables	cur_attables(_self, _self);
			auto	reg_idx = cur_attables.template get_index<N(attendee)>();
			auto	cur_attable_itr = reg_idx.find(attendee);

			confs	cur_confs(_self, _self);

			while ((cur_attable_itr != reg_idx.end()) && (cur_attable_itr->attendee == attendee))
			{
				auto	cur_conf_itr = cur_confs.find(cur_attable_itr->conf_id);
				while ((cur_conf_itr != cur_confs.end()) && (cur_attable_itr->conf_id == cur_conf_itr->id))
				{
					print("Conference id: ", cur_conf_itr->id, ", ", "Conference name: ", cur_conf_itr->conf_name.c_str(), ", ", "Organizer name: ", name{cur_conf_itr->organizer}, ", ", "Fee: ", cur_conf_itr->fee, "\n");
					++cur_conf_itr; //MUST NOT BE cur_conf_itr++, which won't change cur_conf_itr's value
				}

				print("attendee fee locked: ");
				print((cur_attable_itr->fee_locked == false) ? ("false") : ("true"), "\n");

				print("attendee public key: ");
				printhex(cur_attable_itr->attend_pub.data, sizeof(cur_attable_itr->attend_pub.data));
				print("\n");

				print("attendee signature: ");
				printhex(cur_attable_itr->attend_sig.data, sizeof(cur_attable_itr->attend_sig.data));
				print("\n");

				//print("attendee timestamp: ", cur_attable_itr->attend_timestamp);
				print("attendee timestamp: ", cur_attable_itr->attend_timestamp.sec_since_epoch(), "\n");

				++cur_attable_itr; //MUST NOT BE cur_attable_itr++, which won't change cur_attable_itr's value
			}

			//@abi action
			void checkintest(const string& conf_name, const account_name organizer, const account_name attendee, const signature attend_sig, const bytes testdata)
			{
				require_auth( attendee );

				//get conf
				confs	cur_confs(_self, _self);
				auto	idx = cur_confs.template get_index<N(conf_name_hash)>();
				key256	dest_hash = conference::get_key256_from_string(conf_name);
				auto	cur_conf_itr = idx.find(dest_hash);
				uint8_t	bFound = 0;

				while ((cur_conf_itr != idx.end()) && (conference::get_key256_from_checksum256(cur_conf_itr->conf_name_hash) == dest_hash))
				{
					if ((cur_conf_itr->conf_name == conf_name) && (cur_conf_itr->organizer == organizer))
					{
						bFound = 1;
						break;
					}
					++cur_conf_itr; //MUST NOT BE cur_conf_itr++, which won't change cur_conf_itr's value
				}

				eosio_assert(bFound != 0, "conference not found");

				print("Conference id: ", cur_conf_itr->id, ", ", "Conference name: ", cur_conf_itr->conf_name.c_str(), ", ", "Organizer name: ", name{cur_conf_itr->organizer}, ", ", "Fee: ", cur_conf_itr->fee, "\n");

				//check attable
				attables	cur_attables(_self, _self);
				auto	reg_idx = cur_attables.template get_index<N(attendee)>();
				auto	cur_attable_itr = reg_idx.find(attendee);

				bFound = 0;
				while ((cur_attable_itr != reg_idx.end()) && (cur_attable_itr->attendee == attendee))
				{
					if (cur_attable_itr->conf_id == cur_conf_itr->id)
					{
						bFound = 1;
						break;
					}
					++cur_attable_itr; //MUST NOT BE cur_attable_itr++, which won't change cur_attable_itr's value
				}

				eosio_assert(bFound == 1, "conference not registered yet");

				//eosio_assert(cur_attable_itr->fee_locked == false, "conference already checked in");

				//verify signature
				checksum256 desthash;

				sha256((char *)testdata.data(), testdata.size(), &desthash);

				print("testdata: ");
				printhex(testdata.data(), testdata.size());
				print("\n");

				print("hash: ");
				printhex(desthash.hash, sizeof(desthash.hash));
				print("\n");

				print("public key: ");
				printhex(cur_attable_itr->attend_pub.data, sizeof(cur_attable_itr->attend_pub.data));
				print("\n");

				print("signature: ");
				printhex(attend_sig.data, sizeof(attend_sig.data));
				print("\n");

				public_key pubkey;
				recover_key( &desthash, (const char*)(attend_sig.data), sizeof(attend_sig.data), (char*)(pubkey.data), sizeof(pubkey.data) );
				//assert_recover_key ( &hash, (const char*)(attend_sig.data), sizeof(attend_sig.data), (const char*)(cur_attable_itr->attend_pub.data), sizeof(cur_attable_itr->attend_pub.data));

				print("recovered public key: ");
				printhex(pubkey.data, sizeof(pubkey.data));
				print("\n");
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

