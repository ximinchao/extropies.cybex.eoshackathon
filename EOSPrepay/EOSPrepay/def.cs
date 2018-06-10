using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace EOSPrepay
{
    static class def
    {
        public static string ip = @"http://47.75.154.248";
        public static string port_chain = @":8888/v1/chain/";
        public static string port_wallet = @":8900/v1/wallet/";

        public static string token_name = "SYS";
        public static string contract_name = "eosprepay";
        public static int Err = 1;
        public static int EOS_OK = 0;

        public static string json_to_bin = ip + port_chain + "abi_json_to_bin ";
        public static string get_info = ip + port_chain + "get_info";
        public static string get_block = ip + port_chain + "get_block";
        public static string unlock = ip + port_wallet + "unlock";
        public static string get_public_keys = ip + port_chain + "get_public_keys";
        public static string get_required_keys = ip + port_chain + "get_required_keys";
        public static string sign_transaction = ip + port_wallet + "sign_transaction";
        public static string push_transaction = ip + port_chain + "push_transaction";
        public static string get_table_rows = ip + port_chain + "get_table_rows";
        public static string get_balance = ip + port_chain + "get_currency_balance";

        public static string data_get_table = "{\"scope\":\"" + contract_name + "\", \"code\":\"" + contract_name + "\", \"table\":\"conference\", \"json\": true}";
        public static string data_get_block = "{\"block_num_or_id\":932622}";
        public static string data_unlock = "[\"ximc\",\"PW5JNcqr73uN2jzcp3cG6nXKErNY2bZrJexYUNCP1aRL4AqYGugQh\"]";
        public static string ori_pub = "EOS7uSoRUe451pwKhWftGL3orUFWsHdgVJd3bE6HC3ZJkJtrRuuEn";
        public static string data_sign_getlist = "[{\"expiration\":\"2018-06-09T11:55:55\",\"ref_block_num\":937719,\"ref_block_prefix\":77825472,\"max_net_usage_words\":0,\"max_cpu_usage_ms\":0,\"delay_sec\":0,\"context_free_actions\":[],\"actions\":[{\"account\":\"" + contract_name + "\",\"name\":\"getlist\",\"authorization\":[{\"actor\":\"alice\",\"permission\":\"active\"}],\"data\":\"\"}],\"transaction_extensions\":[],\"signatures\":[],\"context_free_data\":[]},[\"EOS7uSoRUe451pwKhWftGL3orUFWsHdgVJd3bE6HC3ZJkJtrRuuEn\"],\"0000000000000000000000000000000000000000000000000000000000000000\"]";

        public static string data_push_getlist = "{\"signatures\":[\"SIG_K1_JvRnehJPBdz5pb6mUxGWH85bJ8snegKQbYRmzaqwjmzrpQwybF2CBVS9Wm4bLUHWtrrvirVhurZsQ5JJHGmhR4Wkes21Vy\"],\"compression\":\"none\",\"packed_context_free_data\":\"\",\"packed_trx\":\"db46055b472836237f2800000000010000004045832745000000206317b362010000000000855c3400000000a8ed32320000\"}";
        public static string data_j2b_register = "";
        public static string data_sign_register = "";
        public static string data_push_register = "";
        public static string data_j2b_checkin = "{\"code\":\"" + contract_name + "\",\"action\":\"checkin\",\"args\":[\"beijing1\",\"alice\",\"chestercybex\",\"SIG_K1_JzVDDi2S9TRqG85CHXRwmyWquH9ziNF6TEEvwQ1ppapjjLWoBjmQxTNvhw2hfr8zbBsefvy5tHadZEx51Jqaht41JAuGYZ\",\"00000000000000000000000000000000000000000000000000000000000000007831015b4e53d77874bb00000000010000004045832745000000000090b18b01000000000080a4eb00000000a8ed323208000000000080a4eb000000000000000000000000000000000000000000000000000000000000000000\"]}";
        public static string data_sign_checkin = "[{\"expiration\":\"2018-06-09T04:15:41\",\"ref_block_num\":1783,\"ref_block_prefix\":4112473875,\"max_net_usage_words\":0,\"max_cpu_usage_ms\":0,\"delay_sec\":0,\"context_free_actions\":[],\"actions\":[{\"account\":\"" + contract_name + "\",\"name\":\"checkin\",\"authorization\":[{\"actor\":\"chestercybex\",\"permission\":\"active\"}],\"data\":\"086265696a696e67310000000000855c340000000000000e3d001f27fd10f3a5984c48e0e94d2d0cc1f1b94ddbb0a9f135e95c78ae8a7af8ce9bfa027c1ad874784986dac15cb30efe92b8920e78415b6a33e397c00a5b08fddbbe7a00000000000000000000000000000000000000000000000000000000000000007831015b4e53d77874bb00000000010000004045832745000000000090b18b01000000000080a4eb00000000a8ed323208000000000080a4eb000000000000000000000000000000000000000000000000000000000000000000\"}],\"transaction_extensions\":[],\"signatures\":[],\"context_free_data\":[]},[\"EOS7uSoRUe451pwKhWftGL3orUFWsHdgVJd3bE6HC3ZJkJtrRuuEn\"],\"0000000000000000000000000000000000000000000000000000000000000000\"]";
        public static string data_push_checkin = "{\"signatures\":[\"SIG_K1_KdNQL4432pWfFfwkKsug1JQTCzUhyyQRJFd8qbbP3ibShc7YvmZ6ny8qjHRSVgLdyniRk8FwBGaRhJdEzZ24qDMY8tGwL7\"],\"compression\":\"none\",\"packed_context_free_data\":\"\",\"packed_trx\":\"ed8d075bf706135f1ff500000000010000004045832745000000603a885443010000000000000e3d00000000a8ed3232d601086265696a696e67310000000000855c340000000000000e3d001f27fd10f3a5984c48e0e94d2d0cc1f1b94ddbb0a9f135e95c78ae8a7af8ce9bfa027c1ad874784986dac15cb30efe92b8920e78415b6a33e397c00a5b08fddbbe7a00000000000000000000000000000000000000000000000000000000000000007831015b4e53d77874bb00000000010000004045832745000000000090b18b01000000000080a4eb00000000a8ed323208000000000080a4eb00000000000000000000000000000000000000000000000000000000000000000000\"}";



    }
}