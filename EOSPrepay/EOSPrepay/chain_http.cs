using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net;
using System.IO;

namespace EOSPrepay
{
    class chain_http
    {
        public chain_http()
        {

        }
        public string get(string url)
        {
            string error = "error";
            var request = (HttpWebRequest)WebRequest.Create(url);
            HttpWebResponse response = null;

            try
            {
                response = (HttpWebResponse)request.GetResponse();
            }
            catch (Exception e)
            {
                Console.WriteLine("\nThe following Exception was raised : {0}", e.Message);
                error = error + e.Message;
                return error;
            }



            var responseString = new StreamReader(response.GetResponseStream()).ReadToEnd();
            return responseString;
        }

        public string post(string url, string str)
        {
            string error = "error";
            var request = (HttpWebRequest)WebRequest.Create(url);

            var data = Encoding.ASCII.GetBytes(str);

            request.Method = "POST";
            request.ContentType = "application/x-www-form-urlencoded";
            request.ContentLength = data.Length;

            using (var stream = request.GetRequestStream())
            {
                stream.Write(data, 0, data.Length);
            }

            HttpWebResponse response = null;
            try
            {
                response = (HttpWebResponse)request.GetResponse();
            }
            catch (WebException e)
            {

                response = (HttpWebResponse)e.Response;//get the detail 500 error
                var errstr = new StreamReader(response.GetResponseStream()).ReadToEnd();
                error = error + "  " + errstr;
                return error;
            }

            var responseString = new StreamReader(response.GetResponseStream()).ReadToEnd();

            return responseString;
        }
    }
}