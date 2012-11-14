namespace WindowsFormsApplication1
{
    using System;
    using System.Diagnostics;
    using System.IO;
    using System.Reflection;
    using System.Text.RegularExpressions;
    using System.Windows.Forms;

    internal static class Program
    {
        public static Form1 Form;
        public static string Forums = "http://forum.delusionms.net";
        public static Process Maple;
        public static string Name = "FuckMS";
        public static string Website = "http://delusionms.net";

        public static void LaunchMaple()
        {
            Maple = new Process();
            Maple.StartInfo.FileName = "MapleStory.exe";
            Maple.StartInfo.Arguments = "GameLaunching";
            Maple.Start();
        }

        [STAThread]
        private static void Main()
        {
            AppDomain.CurrentDomain.ProcessExit += new EventHandler(Program.OnProcessExit);
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Form = new Form1();
            Application.Run(Form);
        }

        private static void OnProcessExit(object sender, EventArgs e)
        {
            ReplaceOriginal();
        }

        public static void ReplaceOriginal()
        {
            string directoryName = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            string[] files = Directory.GetFiles(directoryName, "*.dll");
            string[] strArray2 = Directory.GetFiles(directoryName, "*.ini");
            foreach (string str3 in files)
            {
                if (str3.Contains("ijl15") || str3.Contains("LEN"))
                {
                    File.Delete(str3);
                }
            }
            foreach (string str4 in strArray2)
            {
                if (str4.Contains("LEN"))
                {
                    File.Delete(str4);
                }
            }
            foreach (string str5 in Assembly.GetExecutingAssembly().GetManifestResourceNames())
            {
                if (str5.Contains("ijl15.o"))
                {
                    Regex.Split(str5, ".Resources.");
                    Stream manifestResourceStream = Assembly.GetExecutingAssembly().GetManifestResourceStream(str5);
                    FileStream stream2 = new FileStream("ijl15.dll", FileMode.Create);
                    byte[] buffer = new byte[manifestResourceStream.Length + 1L];
                    manifestResourceStream.Read(buffer, 0, Convert.ToInt32(manifestResourceStream.Length));
                    stream2.Write(buffer, 0, Convert.ToInt32((int) (buffer.Length - 1)));
                    stream2.Flush();
                    stream2.Close();
                    stream2 = null;
                }
            }
        }

        public static void SaveResourcesToDisk()
        {
            foreach (string str in Assembly.GetExecutingAssembly().GetManifestResourceNames())
            {
                if (str.Contains(".ini") || str.Contains(".dll"))
                {
                    string[] strArray = Regex.Split(str, ".Resources.");
                    Stream manifestResourceStream = Assembly.GetExecutingAssembly().GetManifestResourceStream(str);
                    FileStream stream2 = new FileStream(strArray[strArray.Length - 1], FileMode.Create);
                    byte[] buffer = new byte[manifestResourceStream.Length + 1L];
                    manifestResourceStream.Read(buffer, 0, Convert.ToInt32(manifestResourceStream.Length));
                    stream2.Write(buffer, 0, Convert.ToInt32((int) (buffer.Length - 1)));
                    stream2.Flush();
                    stream2.Close();
                    stream2 = null;
                }
            }
        }
    }
}

