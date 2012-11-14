namespace WindowsFormsApplication1
{
    using Maple.Properties;
    using System;
    using System.ComponentModel;
    using System.Diagnostics;
    using System.Drawing;
    using System.Windows.Forms;

    public class Form1 : Form
    {
        private ToolStripMenuItem aboutToolStripMenuItem;
        private Button button1;
        private Button button2;
        private Button button3;
        private IContainer components;
        private ContextMenuStrip contextMenuStrip1;
        private ToolStripMenuItem exitToolStripMenuItem;
        private ToolStripMenuItem Launcher;
        private NotifyIcon notifyIcon1;
        private ToolStripMenuItem settingMenu;
        private ToolStripMenuItem toolStripMenuItem1;
        private ToolStripMenuItem toolStripMenuItem2;
        private ToolStripMenuItem toolStripMenuItem3;
        private ToolStripMenuItem toolStripMenuItem4;
        private ToolStripSeparator toolStripSeparator1;
        private ToolStripSeparator toolStripSeparator2;
        private ToolStripSeparator toolStripSeparator3;

        public Form1()
        {
            this.InitializeComponent();
        }

        private void aboutToolStripMenuItem_Click(object sender, EventArgs e)
        {
            MessageBox.Show("Thank you Jesus, for giving us this glorious program.");
        }

        private void button1_Click(object sender, EventArgs e)
        {
            Process.Start(Program.Website);
        }

        private void button2_Click(object sender, EventArgs e)
        {
            Process.Start(Program.Forums);
        }

        private void button3_Click(object sender, EventArgs e)
        {
            Program.SaveResourcesToDisk();
            Program.LaunchMaple();
            base.Hide();
            base.WindowState = FormWindowState.Minimized;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing && (this.components != null))
            {
                this.components.Dispose();
            }
            base.Dispose(disposing);
        }

        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Process[] processesByName = Process.GetProcessesByName("MapleStory.exe");
            if (processesByName.Length > 0)
            {
                processesByName[0].CloseMainWindow();
            }
            Application.Exit();
        }

        private void Form1_Resize(object sender, EventArgs e)
        {
            if (FormWindowState.Minimized == base.WindowState)
            {
                base.Hide();
            }
        }

        private void InitializeComponent() {
            this.components = new Container();
            ComponentResourceManager manager = new ComponentResourceManager(typeof(Form1));
            this.button1 = new Button();
            this.button2 = new Button();
            this.button3 = new Button();
            this.notifyIcon1 = new NotifyIcon(this.components);
            this.contextMenuStrip1 = new ContextMenuStrip(this.components);
            this.Launcher = new ToolStripMenuItem();
            this.toolStripSeparator1 = new ToolStripSeparator();
            this.toolStripMenuItem2 = new ToolStripMenuItem();
            this.toolStripMenuItem3 = new ToolStripMenuItem();
            this.toolStripSeparator2 = new ToolStripSeparator();
            this.aboutToolStripMenuItem = new ToolStripMenuItem();
            this.exitToolStripMenuItem = new ToolStripMenuItem();
            this.toolStripSeparator3 = new ToolStripSeparator();
            this.settingMenu = new ToolStripMenuItem();
            this.toolStripMenuItem1 = new ToolStripMenuItem();
            this.toolStripMenuItem4 = new ToolStripMenuItem();
            this.contextMenuStrip1.SuspendLayout();
            base.SuspendLayout();

            this.button1.BackColor = Color.Transparent;
            this.button1.FlatAppearance.MouseOverBackColor = Color.Transparent;
            this.button1.FlatStyle = FlatStyle.Flat;
            this.button1.Location = new Point(0xa7, 5);
            this.button1.Name = "button1";
            this.button1.Size = new Size(0xa2, 0x2f);
            this.button1.TabIndex = 0;
            this.button1.UseVisualStyleBackColor = false;
            this.button1.Click += new EventHandler(this.button1_Click);

            this.button2.BackColor = Color.Transparent;
            this.button2.FlatAppearance.MouseOverBackColor = Color.Transparent;
            this.button2.FlatStyle = FlatStyle.Flat;
            this.button2.Location = new Point(0xa7, 60);
            this.button2.Name = "button2";
            this.button2.Size = new Size(0xa2, 0x2f);
            this.button2.TabIndex = 1;
            this.button2.UseVisualStyleBackColor = true;
            this.button2.Click += new EventHandler(this.button2_Click);
           
            this.button3.Image = Resources._92QTK; //image for Launch
            this.button3.Location = new Point(8, 5);
            this.button3.Name = "button3";
            this.button3.Size = new Size(0x91, 0x66);
            this.button3.TabIndex = 2;
            this.button3.UseVisualStyleBackColor = true;
            this.button3.Click += new EventHandler(this.button3_Click);
           
            this.notifyIcon1.ContextMenuStrip = this.contextMenuStrip1;
            this.notifyIcon1.Icon = (Icon) manager.GetObject("notifyIcon1.Icon");
            this.notifyIcon1.Text = "MapleStory";
            this.notifyIcon1.Visible = true;
            this.notifyIcon1.MouseDoubleClick += new MouseEventHandler(this.notifyIcon1_MouseDoubleClick);

            this.contextMenuStrip1.Font = new Font("Segoe UI", 8.25f, FontStyle.Regular, GraphicsUnit.Point, 0);
            this.contextMenuStrip1.Items.AddRange(new ToolStripItem[] { this.Launcher, this.toolStripSeparator1, this.toolStripMenuItem2, this.toolStripMenuItem3, this.toolStripSeparator2, this.aboutToolStripMenuItem, this.exitToolStripMenuItem, this.toolStripSeparator3, this.settingMenu });
            this.contextMenuStrip1.Name = "contextMenuStrip1";
            this.contextMenuStrip1.Size = new Size(0x99, 0xb0);

            this.Launcher.Name = "Launcher";
            this.Launcher.Size = new Size(0x98, 0x16);
            this.Launcher.Text = "Launch";
            this.Launcher.Click += new EventHandler(this.toolStripMenuItem1_Click);

            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new Size(0x95, 6);

            this.toolStripMenuItem2.Name = "toolStripMenuItem2";
            this.toolStripMenuItem2.Size = new Size(0x98, 0x16);
            this.toolStripMenuItem2.Text = "Website";
            this.toolStripMenuItem2.Click += new EventHandler(this.toolStripMenuItem2_Click);

            this.toolStripMenuItem3.Name = "toolStripMenuItem3";
            this.toolStripMenuItem3.Size = new Size(0x98, 0x16);
            this.toolStripMenuItem3.Text = "Forums";
            this.toolStripMenuItem3.Click += new EventHandler(this.toolStripMenuItem3_Click);

            this.toolStripSeparator2.Name = "toolStripSeparator2";
            this.toolStripSeparator2.Size = new Size(0x95, 6);

            this.aboutToolStripMenuItem.Name = "aboutToolStripMenuItem";
            this.aboutToolStripMenuItem.Size = new Size(0x98, 0x16);
            this.aboutToolStripMenuItem.Text = "About";
            this.aboutToolStripMenuItem.Click += new EventHandler(this.aboutToolStripMenuItem_Click);

            this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
            this.exitToolStripMenuItem.Size = new Size(0x98, 0x16);
            this.exitToolStripMenuItem.Text = "Exit";
            this.exitToolStripMenuItem.Click += new EventHandler(this.exitToolStripMenuItem_Click);

            this.toolStripSeparator3.Name = "toolStripSeparator3";
            this.toolStripSeparator3.Size = new Size(0x95, 6);

            this.settingMenu.DropDownItems.AddRange(new ToolStripItem[] { this.toolStripMenuItem1, this.toolStripMenuItem4 });
            this.settingMenu.Name = "settingMenu";
            this.settingMenu.Size = new Size(0x98, 0x16);
            this.settingMenu.Text = "Server Select";

            this.toolStripMenuItem1.Name = "toolStripMenuItem1";
            this.toolStripMenuItem1.Size = new Size(0x98, 0x16);
            this.toolStripMenuItem1.Text = "GMS";
            this.toolStripMenuItem1.Click += new EventHandler(this.toolStripMenuItem1_Click_1);

            this.toolStripMenuItem4.Name = "toolStripMenuItem4";
            this.toolStripMenuItem4.Size = new Size(0x98, 0x16);
            this.toolStripMenuItem4.Text = Program.Name;
            this.toolStripMenuItem4.Click += new EventHandler(this.toolStripMenuItem4_Click);

            base.AutoScaleDimensions = new SizeF(6f, 13f);
            base.AutoScaleMode = AutoScaleMode.Font;
            this.BackgroundImage = Resources.bg1;
            base.ClientSize = new Size(0x152, 0x70);
            base.Controls.Add(this.button3);
            base.Controls.Add(this.button2);
            base.Controls.Add(this.button1);
            this.Font = new Font("Segoe UI", 8.25f, FontStyle.Regular, GraphicsUnit.Point, 0);
            base.FormBorderStyle = FormBorderStyle.FixedSingle;
            base.Icon = (Icon) manager.GetObject("$this.Icon");
            base.MaximizeBox = false;
            base.Name = "Form1";
            this.Text = Program.Name;
            base.Resize += new EventHandler(this.Form1_Resize);
            this.contextMenuStrip1.ResumeLayout(false);
            base.ResumeLayout(false);
        }

        private void notifyIcon1_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            if (base.WindowState == FormWindowState.Normal)
            {
                base.Hide();
                base.WindowState = FormWindowState.Minimized;
            }
            else
            {
                base.Show();
                base.WindowState = FormWindowState.Normal;
            }
        }

        private void Show_Click(object sender, EventArgs e)
        {
            base.Show();
            base.WindowState = FormWindowState.Normal;
        }

        private void toolStripMenuItem1_Click(object sender, EventArgs e)
        {
            this.button3_Click(sender, e);
        }

        private void toolStripMenuItem1_Click_1(object sender, EventArgs e)
        {
            if (Process.GetProcessesByName("MapleStory.exe").Length > 0)
            {
                MessageBox.Show("MapleStory is already running.\r\nClose the game and try again.");
            }
            else
            {
                Program.ReplaceOriginal();
            }
        }

        private void toolStripMenuItem2_Click(object sender, EventArgs e)
        {
            Process.Start(Program.Website);
        }

        private void toolStripMenuItem3_Click(object sender, EventArgs e)
        {
            Process.Start(Program.Forums);
        }

        private void toolStripMenuItem4_Click(object sender, EventArgs e)
        {
            if (Process.GetProcessesByName("MapleStory.exe").Length > 0)
            {
                MessageBox.Show("MapleStory is already running.\r\nClose the game and try again.");
            }
            else
            {
                Program.SaveResourcesToDisk();
            }
        }
    }
}

