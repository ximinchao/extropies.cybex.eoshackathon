using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;



namespace EOSPrepay
{

    public partial class Form1 : Form
    {
        eosprepay b = new eosprepay();
        int count = 0;
        public Form1()
        {
            InitializeComponent();
        }

        public void clear_show(string str)
        {
            TB_ACCOUNT.Text = "";
            TB_SIGN.Text = "";
            TB_CONSOLE.Text = "";
            TB_QR.Text = "";
            lb_time.Text = "";
            LB_Status.Text = "Waiting";
            LB_Status.ForeColor = Color.DodgerBlue;
            if (str != "")
            {
                TB_CONSOLE.Text = str;
            }
        }

        private void btn_checkin_Click(object sender, EventArgs e)
        {

            clear_show("");
            TB_QR.Focus();
            //TB_CONSOLE.Text = b.checkin("bob", "extropies", "hackathonhongkong", "SIG_K1_JzVDDi2S9TRqG85CHXRwmyWquH9ziNF6TEEvwQ1ppapjjLWoBjmQxTNvhw2hfr8zbBsefvy5tHadZEx51Jqaht41JAuGYZ", "00000000000000000000000000000000000000000000000000000000000000007831015b4e53d77874bb00000000010000004045832745000000000090b18b01000000000080a4eb00000000a8ed323208000000000080a4eb000000000000000000000000000000000000000000000000000000000000000000");
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            TB_QR.Focus();
            lb_balance.Text = b.get_banlance(tb_org.Text);
            lb_not.Text = b.get_conf_asset_not(tb_id.Text).ToString();
            lb_already.Text = b.get_conf_asset_already(tb_id.Text).ToString();

        }
        private void textBox_FormKeydown(object sender, KeyEventArgs e)
        {



        }
        private void textBox_KeyPress(object sender, KeyPressEventArgs e)
        {
            string qr = "";

            if (e.KeyChar == (char)Keys.Enter)
            {
                qr = TB_QR.Text;
                if (qr.Substring(0, 1) != "~")
                {
                    clear_show("wrong qr code");
                    return;
                }

                int flag = qr.IndexOf('|');
                if (flag > 55612 || flag < 0)
                {
                    clear_show("wrong qr code");
                    return;
                }
                TB_ACCOUNT.Text = qr.Substring(1, flag - 1) + "cybex";
                TB_SIGN.Text = "SIG_K1_" + qr.Substring(flag + 1, qr.Length - 1 - flag);
                count = 0;
                DateTime t1 = DateTime.Now;
                //TB_CONSOLE.Text = b.getlist("extropies");
                TB_CONSOLE.Text = b.checkin(TB_ACCOUNT.Text, tb_org.Text, tb_conf.Text, TB_SIGN.Text, "00000000000000000000000000000000000000000000000000000000000000008e5fd85a0000890fea4dfcda0000000001000000005c052fe5000000572d3ccdcd01000000005c95b1ca00000000a8ed323222000000005c95b1ca000000405c95b1ca102700000000000004454f5300000000016d");
                DateTime t2 = DateTime.Now;

                TimeSpan time = t2 - t1;
                float mm = time.Milliseconds;
                int ss = time.Seconds;
                float f = mm / 1000;
                f = ss + f;
                lb_time.Text = f.ToString();
                TB_QR.Text = "";
                if (TB_CONSOLE.Text.IndexOf("error") == 0)
                {
                    LB_Status.Text = "Error";
                    LB_Status.ForeColor = Color.Red;
                }
                else
                {
                    LB_Status.ForeColor = Color.Lime;
                    LB_Status.Text = "Checked";
                }

                lb_balance.Text = b.get_banlance(tb_org.Text);
                lb_not.Text = b.get_conf_asset_not(tb_id.Text).ToString();
                lb_already.Text = b.get_conf_asset_already(tb_id.Text).ToString();


            }
        }
        private void textBox_KeyUp(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
                TB_QR.Text = "";
            if (e.KeyCode == Keys.Space)
            {
                clear_show("");
            }
        }
        private void textBox_KeyDown(object sender, KeyEventArgs e)
        {
            LB_Status.Text = "Waiting";
            LB_Status.ForeColor = Color.DodgerBlue;
        }

        private void btn_gettable_Click(object sender, EventArgs e)
        {
            DateTime t1 = DateTime.Now;
            TB_CONSOLE.Text = b.get_conference();
            DateTime t2 = DateTime.Now;
            TimeSpan time = t2 - t1;
            float mm = time.Milliseconds;
            int ss = time.Seconds;
            float f = mm / 1000;
            f = ss + f;
            lb_time.Text = f.ToString();

        }

        private void btn_att_Click(object sender, EventArgs e)
        {
            DateTime t1 = DateTime.Now;
            TB_CONSOLE.Text = b.get_attendee();
            DateTime t2 = DateTime.Now;
            TimeSpan time = t2 - t1;
            float mm = time.Milliseconds;
            int ss = time.Seconds;
            float f = mm / 1000;
            f = ss + f;
            lb_time.Text = f.ToString();
        }

        private void btn_f5_Click(object sender, EventArgs e)
        {
            lb_balance.Text = b.get_banlance(tb_org.Text);
            lb_not.Text = b.get_conf_asset_not(tb_id.Text).ToString();
            lb_already.Text = b.get_conf_asset_already(tb_id.Text).ToString();
        }

    }
}
