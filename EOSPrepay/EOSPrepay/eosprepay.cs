using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Runtime.Serialization;
using System.IO;
using Newtonsoft.Json.Converters;
using System.Globalization;


namespace EOSPrepay
{
    class eosprepay
    {
        string error_stirng;
        chain_http s;
        chain c;
        public eosprepay()
        {
            s = new chain_http();
            c = new chain();
        }

        public string create(string name, string exp_date, string fee, string ori_account)
        {
            return error_stirng;
        }

        public string cancel(string name, string ori_account)
        {
            return error_stirng;
        }
        public string get_attendee()
        {
            int rv = 0;
            rv = c.get_table("attable");
            if (rv == def.Err)
                return c.error_message;
            return c.current_json;
        }

        public string get_conference()
        {
            int rv = 0;
            rv = c.get_table("conference");
            if (rv == def.Err)
                return c.error_message;
            return c.current_json;
        }

        public string getlist(string ori_account)
        {
            int rv = 0;
            rv = c.get_info();
            if (rv == def.Err)
                return c.error_message;
            rv = c.get_block();
            if (rv == def.Err)
                return c.error_message;
            rv = c.unlock("ximc", "PW5JNcqr73uN2jzcp3cG6nXKErNY2bZrJexYUNCP1aRL4AqYGugQh");
            if (rv == def.Err)
                return c.error_message;
            rv = c.sign_transaction(def.data_sign_getlist);
            if (rv == def.Err)
                return c.error_message;
            rv = c.push_transation(def.data_push_getlist);
            if (rv == def.Err)
                return c.error_message;

            return c.current_json;

        }

        public string register(string att_account, string ori_account, string conf_name, string pubkey)
        {
            return error_stirng;
        }

        public string checkin(string att_account, string ori_account, string conf_name, string sign, string testdata)
        {
            int rv = 0;

            string[] args = new string[5];
            args[0] = conf_name;
            args[1] = ori_account;
            args[2] = att_account;
            args[3] = sign;
            args[4] = testdata;

            rv = c.json_to_bin(args, 5, 0);
            if (rv == def.Err)
                return c.error_message;
            rv = c.get_info();
            if (rv == def.Err)
                return c.error_message;
            rv = c.get_block();
            if (rv == def.Err)
                return c.error_message;
            rv = c.unlock("ximc", "PW5JNcqr73uN2jzcp3cG6nXKErNY2bZrJexYUNCP1aRL4AqYGugQh");
            if (rv == def.Err)
                return c.error_message;
            rv = c.sign_transaction(def.data_sign_checkin);
            if (rv == def.Err)
                return c.error_message;
            rv = c.push_transation(def.data_push_checkin);
            if (rv == def.Err)
                return c.error_message;

            return c.current_json;


        }

        public string withdraw(string att_account)
        {
            return error_stirng;
        }

        public string get_banlance(string account)
        {
            int rv = 0;
            rv = c.get_banlance(account);
            if (rv == def.Err)
                return c.error_message;
            return c.current_json;
        }

        public string get_conf_asset_not(string id)
        {
            int rv = 0;
            rv = c.get_conf_asset_not(id);
            return rv.ToString();
        }
        public string get_conf_asset_already(string id)
        {
            int rv = 0;
            rv = c.get_conf_asset_already(id);
            return rv.ToString();
        }
    }
}
