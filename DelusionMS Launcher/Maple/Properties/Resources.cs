namespace Maple.Properties   {
    using System;
    using System.CodeDom.Compiler;
    using System.ComponentModel;
    using System.Diagnostics;
    using System.Drawing;
    using System.Globalization;
    using System.Resources;
    using System.Runtime.CompilerServices;

    [DebuggerNonUserCode, CompilerGenerated, GeneratedCode("System.Resources.Tools.StronglyTypedResourceBuilder", "2.0.0.0")]
    public class Resources    {
        private static CultureInfo resourceCulture;
        private static System.Resources.ResourceManager resourceMan;

        internal Resources()
        {
        }

        public static Bitmap _92QTK    {
            get
            {
                return (Bitmap) ResourceManager.GetObject("_92QTK", resourceCulture);
            }
        }

        public static Bitmap bg1    {
            get
            {
                return (Bitmap) ResourceManager.GetObject("bg1", resourceCulture);
            }
        }

        [EditorBrowsable(EditorBrowsableState.Advanced)]
        public static CultureInfo Culture    {
            get
            {
                return resourceCulture;
            }
            set
            {
                resourceCulture = value;
            }
        }

        public static byte[] ijl15    {
            get
            {
                return (byte[]) ResourceManager.GetObject("ijl15", resourceCulture);
            }
        }

        public static byte[] ijl151    {
            get
            {
                return (byte[]) ResourceManager.GetObject("ijl151", resourceCulture);
            }
        }

        public static string LEN     {
            get
            {
                return ResourceManager.GetString("LEN", resourceCulture);
            }
        }

        public static byte[] LEN1    {
            get
            {
                return (byte[]) ResourceManager.GetObject("LEN1", resourceCulture);
            }
        }

        [EditorBrowsable(EditorBrowsableState.Advanced)]
        public static System.Resources.ResourceManager ResourceManager    {
            get
            {
                if (object.ReferenceEquals(resourceMan, null))
                {
                    System.Resources.ResourceManager manager = new System.Resources.ResourceManager("Maple.Properties.Resources", typeof(Resources).Assembly);
                    resourceMan = manager;
                }
                return resourceMan;
            }
        }
    }
}

