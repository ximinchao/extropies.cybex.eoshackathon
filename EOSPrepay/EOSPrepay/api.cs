using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Runtime.InteropServices;

namespace EOSPrepay
{
    using size_t = System.UIntPtr;
    static class api
    {

        [DllImport("EWallet.dll", SetLastError = true)]
        private static extern int PAEW_EOS_TX_Serialize(byte[] b, byte[] o, size_t[] s);


        [DllImport("EWallet.dll", SetLastError = true)]
        private static extern int PAEW_Format(byte[] b, size_t[] s);

        public static string serialize(string json)
        {

            byte[] byteArray = System.Text.Encoding.Default.GetBytes(json);
            byte[] b = new byte[json.Length + 1];
            byte[] o = new byte[1024];
            size_t[] s = new size_t[1];
            s[0] = (UIntPtr)1024;
            for (int i = 0; i < json.Length; i++)
            {
                b[i] = byteArray[i];
            }

            b[json.Length] = 0;

            int rv = PAEW_EOS_TX_Serialize(b, o, s);

            if (rv != 0)
                return ""; 

            return ToHexString(o, (int)s[0]).ToLower();

        }

        public static string ToHexString(byte[] bytes, int len) // 0xae00cf => "AE00CF "
        {
            string hexString = string.Empty;

            if (bytes != null)
            {

                StringBuilder strB = new StringBuilder();

                for (int i = 0; i < len; i++)
                {

                    strB.Append(bytes[i].ToString("X2"));

                }

                hexString = strB.ToString();

            } return hexString;

        }
    }
}
