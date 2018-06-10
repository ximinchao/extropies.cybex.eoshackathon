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
using System.Diagnostics;
using System.Reflection;

namespace EOSPrepay
{
    class chain
    {
        chain_http s;
        private string head_block_num = "";
        private string block_num = "";
        private string exp_time = "";
        private string ref_block_prefix = "";
        private string chainID = "";

        private JObject got;
        private JObject sent;
        private string packed = "";
        private string sig = "";
        private string binargs = "";

        public string error_message = "";
        public string current_json = "";
        public chain()
        {
            s = new chain_http();
        }

        private int check_response(string str)
        {
            StackTrace stackTrace = new StackTrace();
            StackFrame stackFrame = stackTrace.GetFrame(1);
            MethodBase methodBase = stackFrame.GetMethod();

            if (str.IndexOf("error") == 0)
            {
                current_json = "";
                error_message = str + "   " + methodBase.Name;
                return def.Err;
            }
            else
            {
                current_json = str;
                error_message = "";
                return def.EOS_OK;
            }


        }

        public int get_table(string tablename)
        {
            JObject sent = JObject.Parse(def.data_get_table);
            sent["table"] = tablename;
            string str = s.post(def.get_table_rows, sent.ToString());
            if (check_response(str) == def.Err)
                return def.Err;
            return def.EOS_OK;
        }

        public int unlock(string name, string pwd)
        {
            string str = s.post(def.unlock, def.data_unlock);
            //   if (check_response(str) == def.Err)
            //      return def.Err;
            return def.EOS_OK;
        }
        public int json_to_bin(string[] args, int count, int index)
        {
            JObject sent = JObject.Parse(def.data_j2b_checkin);
            JArray sent_array = JArray.Parse(sent["args"].ToString());
            for (int i = 0; i < count; i++)
                sent_array[i] = args[i];
            sent["args"] = sent_array;
            /*===================================================================*/
            string str = s.post(def.json_to_bin, sent.ToString());
            if (check_response(str) == def.Err)
                return def.Err;
            /*===================================================================*/
            got = JObject.Parse(str);
            binargs = got["binargs"].ToString();

            return def.EOS_OK;
        }
        public int get_info()
        {
            /*===================================================================*/
            string str = s.get(def.get_info);
            if (check_response(str) == def.Err)
                return def.Err;
            /*===================================================================*/
            got = JObject.Parse(str);
            head_block_num = got["head_block_num"].ToString();
            chainID = got["chain_id"].ToString();
            return def.EOS_OK;
        }
        public int get_block()
        {
            string str = "";
            sent = JObject.Parse(def.data_get_block);
            sent["block_num_or_id"] = head_block_num;
            /*===================================================================*/
            str = s.post(def.get_block, sent.ToString());
            if (check_response(str) == def.Err)
                return def.Err;
            /*===================================================================*/
            got = JObject.Parse(str);
            var dateTimeConverter = new IsoDateTimeConverter { DateTimeFormat = "yyyy-MM-ddThh:mm:ss" };
            //获取时间
            exp_time = JsonConvert.SerializeObject(got["timestamp"]);
            exp_time = exp_time.Replace("\"", "");
            CultureInfo provider = CultureInfo.InvariantCulture;
            DateTime dt = Convert.ToDateTime(exp_time, provider);
            dt = dt.AddHours(1);
            exp_time = dt.ToString("yyyy-MM-ddTHH:mm:ss");

            block_num = got["block_num"].ToString();
            int a1 = int.Parse(block_num);
            int a2 = a1 % 65536;
            block_num = a2.ToString();
            //block_num = got["block_num"].ToString();
            //获取ref_block_prefix
            ref_block_prefix = got["ref_block_prefix"].ToString();

            return def.EOS_OK;

        }
        public int sign_transaction(string template)
        {
            string str = "";
            string strTran = template;
            JArray sent_array = JArray.Parse(strTran);
            //更改时间
            sent_array[0]["expiration"] = exp_time;
            //更改ref_block_num
            sent_array[0]["ref_block_num"] = block_num;
            //更改ref_block_prefix
            sent_array[0]["ref_block_prefix"] = ref_block_prefix;

            JArray sub_array = JArray.Parse(sent_array[0]["actions"].ToString());
            if (sub_array != null)
            {
                sub_array[0]["data"] = binargs;
                sent_array[0]["actions"] = sub_array;
            }
            sent_array[2] = chainID;

            /*===================================================================*/
            str = s.post(def.sign_transaction, sent_array.ToString());
            if (check_response(str) == def.Err)
                return def.Err;
            /*===================================================================*/
            JObject got = JObject.Parse(str);
            JArray asig = JArray.Parse(got["signatures"].ToString());
            sig = asig[0].ToString();

            JObject sent = JObject.Parse(str);

            // int a1 = int.Parse(block_num);
            // int a2 = a1 % 65536;
            // sent["ref_block_num"] = a2;
            sent["ref_block_prefix"] = ref_block_prefix;

            sent.Remove("context_free_data");
            sent.Remove("signatures");

            string topacked = sent.ToString().Replace("\r\n", "");
            packed = api.serialize(topacked);
            if (packed == "")
                return def.Err;

            return def.EOS_OK;
        }
        public int push_transation(string template)
        {
            string str = "";
            sent = JObject.Parse(template);
            sent["packed_trx"] = packed;
            JArray sig_array = JArray.Parse(sent["signatures"].ToString());
            sig_array[0] = sig;
            sent["signatures"] = sig_array;
            /*===================================================================*/
            str = s.post(def.push_transaction, sent.ToString());
            if (check_response(str) == def.Err)
                return def.Err;
            /*===================================================================*/
            return def.EOS_OK;
        }
        public int get_banlance(string account)
        {
            string str = "{\"account\":\"alice\",\"code\":\"eosio.token\",\"symbol\":\"" + def.token_name + "\"}";
            sent = JObject.Parse(str);
            sent["account"] = account;
            str = s.post(def.get_balance, sent.ToString());
            if (check_response(str) == def.Err)
                return def.Err;

            JArray arr = JArray.Parse(current_json);
            current_json = arr[0].ToString();
            current_json = current_json.Replace("\"", "");
            int index = current_json.IndexOf(".");
            current_json = current_json.Substring(0, index);
            return def.EOS_OK;
        }

        public int get_conf_asset_not(string id)
        {
            int rv = get_table("attable");
            int asset = 0;
            JObject obj = JObject.Parse(current_json);
            JArray jarray = JArray.Parse(obj["rows"].ToString());
            foreach (JObject items in jarray)
            {
                if (items["id"].ToString() == id)
                {
                    if (items["fee_locked"].ToString() == "0")
                    {
                        asset = asset + Convert.ToInt32(items["fee"].ToString().Substring(0, 1));
                    }
                }
            }
            return asset;
        }
        public int get_conf_asset_already(string id)
        {
            int rv = get_table("attable");
            int asset = 0;
            JObject obj = JObject.Parse(current_json);
            JArray jarray = JArray.Parse(obj["rows"].ToString());
            foreach (JObject items in jarray)
            {
                if (items["id"].ToString() == id)
                {
                    if (items["fee_locked"].ToString() == "1")
                    {
                        asset = asset + Convert.ToInt32(items["fee"].ToString().Substring(0, 1));
                    }
                }
            }
            return asset;
        }

    }
}
